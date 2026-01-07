package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roominspection.backend.entity.DoorAccessLog;
import com.roominspection.backend.entity.InspectionVerification;
import com.roominspection.backend.entity.PhotoVerification;
import com.roominspection.backend.mapper.InspectionVerificationMapper;
import com.roominspection.backend.service.DoorAccessLogService;
import com.roominspection.backend.service.InspectionVerificationService;
import com.roominspection.backend.service.PhotoVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * 巡检核验服务实现类
 */
@Slf4j
@Service
public class InspectionVerificationServiceImpl extends ServiceImpl<InspectionVerificationMapper, InspectionVerification>
        implements InspectionVerificationService {

    @Autowired
    private DoorAccessLogService doorAccessLogService;

    @Autowired
    private PhotoVerificationService photoVerificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<InspectionVerification> queryPage(Long roomId, Long inspectorId, String status,
                                                    int pageNum, int pageSize) {
        LambdaQueryWrapper<InspectionVerification> wrapper = new LambdaQueryWrapper<>();
        
        if (roomId != null) {
            wrapper.eq(InspectionVerification::getRoomId, roomId);
        }
        if (inspectorId != null) {
            wrapper.eq(InspectionVerification::getInspectorId, inspectorId);
        }
        if (status != null) {
            wrapper.eq(InspectionVerification::getVerificationStatus, status);
        }
        
        wrapper.orderByDesc(InspectionVerification::getCreateTime);
        
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionVerification createVerification(Long inspectionTaskId, String taskNo, Long roomId,
                                                      String roomName, Long inspectorId, String inspectorName,
                                                      String plannedRoute, Integer plannedDeviceCount) {
        log.info("创建巡检核验记录，任务ID：{}", inspectionTaskId);
        
        InspectionVerification verification = new InspectionVerification();
        verification.setInspectionTaskId(inspectionTaskId);
        verification.setTaskNo(taskNo);
        verification.setRoomId(roomId);
        verification.setRoomName(roomName);
        verification.setInspectorId(inspectorId);
        verification.setInspectorName(inspectorName);
        verification.setPlannedRoute(plannedRoute);
        verification.setPlannedDeviceCount(plannedDeviceCount);
        verification.setVerificationStatus("processing");
        
        // 解析计划时间（从计划路由中获取）
        // TODO: 根据实际情况解析计划开始和结束时间
        
        this.save(verification);
        return verification;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InspectionVerification analyzeCompleteness(Long verificationId) {
        log.info("分析巡检完整性，核验ID：{}", verificationId);
        
        InspectionVerification verification = this.getById(verificationId);
        if (verification == null) {
            throw new RuntimeException("核验记录不存在");
        }
        
        List<String> abnormalBehaviors = new ArrayList<>();
        
        // 1. 核验进出记录
        LocalDateTime plannedStartTime = verification.getPlannedStartTime();
        LocalDateTime plannedEndTime = verification.getPlannedEndTime();
        
        if (plannedStartTime != null && plannedEndTime != null) {
            Map<String, Object> accessAnalysis = doorAccessLogService.analyzeInspectionAccess(
                    verification.getInspectionTaskId(),
                    verification.getInspectorId(),
                    verification.getRoomId(),
                    plannedStartTime,
                    plannedEndTime
            );
            
            verification.setAccessVerified(true);
            verification.setEnterLogId((Long) ((DoorAccessLog) accessAnalysis.get("entryLog")).getId());
            verification.setExitLogId((Long) ((DoorAccessLog) accessAnalysis.get("exitLog")).getId());
            
            DoorAccessLog entryLog = (DoorAccessLog) accessAnalysis.get("entryLog");
            DoorAccessLog exitLog = (DoorAccessLog) accessAnalysis.get("exitLog");
            
            if (entryLog != null) {
                verification.setActualStartTime(entryLog.getAccessTime());
                
                // 判断是否按时进入
                long entryDelay = java.time.Duration.between(plannedStartTime, entryLog.getAccessTime()).toMinutes();
                verification.setOnTimeEntry(Math.abs(entryDelay) <= 10);
                verification.setEntryDelay((int) entryDelay);
            }
            
            if (exitLog != null) {
                verification.setActualEndTime(exitLog.getAccessTime());
                
                // 判断是否按时完成
                long completionDelay = java.time.Duration.between(plannedEndTime, exitLog.getAccessTime()).toMinutes();
                verification.setOnTimeCompletion(Math.abs(completionDelay) <= 30);
                verification.setCompletionDelay((int) completionDelay);
            }
            
            // 计算停留时长
            if (entryLog != null && exitLog != null) {
                long stayMinutes = java.time.Duration.between(entryLog.getAccessTime(), exitLog.getAccessTime()).toMinutes();
                verification.setStayDuration((int) stayMinutes);
                
                // 检测快速通过异常
                if (stayMinutes < 5) {
                    abnormalBehaviors.add("fast_pass");
                }
            }
        }
        
        // 2. 核验照片记录
        List<PhotoVerification> photos = photoVerificationService.queryPage(
                verification.getInspectionTaskId(), null, null, 1, 1000
        ).getRecords();
        
        verification.setActualDeviceCount(photos.size());
        
        int photoPassedCount = 0;
        int photoFailedCount = 0;
        
        for (PhotoVerification photo : photos) {
            if ("passed".equals(photo.getVerificationStatus())) {
                photoPassedCount++;
            } else if ("failed".equals(photo.getVerificationStatus())) {
                photoFailedCount++;
            }
        }
        
        verification.setPhotoPassedCount(photoPassedCount);
        verification.setPhotoFailedCount(photoFailedCount);
        
        if (photos.size() > 0) {
            double passRate = (double) photoPassedCount / photos.size() * 100;
            verification.setPhotoPassRate(java.math.BigDecimal.valueOf(passRate));
            
            // 检测未完成异常
            if (photoPassedCount < verification.getPlannedDeviceCount()) {
                abnormalBehaviors.add("incomplete");
            }
        }
        
        // 3. 识别其他异常行为
        if (abnormalBehaviors.isEmpty()) {
            verification.setAbnormalBehaviors(null);
        } else {
            try {
                verification.setAbnormalBehaviors(objectMapper.writeValueAsString(abnormalBehaviors));
            } catch (Exception e) {
                log.error("序列化异常行为失败", e);
            }
        }
        
        // 4. 计算质量评分
        Integer qualityScore = calculateQualityScore(verificationId);
        verification.setQualityScore(qualityScore);
        verification.setGradeLevel(determineGradeLevel(qualityScore));
        
        // 5. 生成核验报告
        String report = generateVerificationReport(verificationId);
        verification.setVerificationReport(report);
        
        // 6. 更新状态
        verification.setVerificationStatus("completed");
        this.updateById(verification);
        
        return verification;
    }

    @Override
    public Integer calculateQualityScore(Long verificationId) {
        InspectionVerification verification = this.getById(verificationId);
        if (verification == null) {
            return 0;
        }
        
        int totalScore = 100;
        int deduction = 0;
        
        // 1. 进出核验（20分）
        if (!Boolean.TRUE.equals(verification.getAccessVerified())) {
            deduction += 20;
        } else {
            if (!Boolean.TRUE.equals(verification.getOnTimeEntry())) {
                deduction += 5;
            }
            if (!Boolean.TRUE.equals(verification.getOnTimeCompletion())) {
                deduction += 5;
            }
        }
        
        // 2. 照片核验（40分）
        double photoPassRate = verification.getPhotoPassRate() != null ?
                verification.getPhotoPassRate().doubleValue() : 0;
        deduction += (int) ((1 - photoPassRate / 100) * 40);
        
        // 3. 停留时长合理性（20分）
        if (verification.getStayDuration() != null) {
            int stayDuration = verification.getStayDuration();
            if (stayDuration < 5) {
                deduction += 20;  // 快速通过
            } else if (stayDuration < 15) {
                deduction += 10;  // 停留时间偏短
            } else if (stayDuration > 180) {
                deduction += 10;  // 停留时间过长
            }
        }
        
        // 4. 设备完成率（20分）
        if (verification.getPlannedDeviceCount() != null && verification.getActualDeviceCount() != null) {
            double completionRate = (double) verification.getActualDeviceCount() / verification.getPlannedDeviceCount();
            deduction += (int) ((1 - completionRate) * 20);
        }
        
        int finalScore = Math.max(0, totalScore - deduction);
        return finalScore;
    }

    @Override
    public String generateVerificationReport(Long verificationId) {
        InspectionVerification verification = this.getById(verificationId);
        if (verification == null) {
            return "";
        }
        
        StringBuilder report = new StringBuilder();
        
        report.append("# 巡检核验报告\n\n");
        report.append("## 基本信息\n");
        report.append("- 任务编号：").append(verification.getTaskNo()).append("\n");
        report.append("- 机房名称：").append(verification.getRoomName()).append("\n");
        report.append("- 巡检人员：").append(verification.getInspectorName()).append("\n");
        report.append("- 计划开始时间：").append(formatDateTime(verification.getPlannedStartTime())).append("\n");
        report.append("- 计划结束时间：").append(formatDateTime(verification.getPlannedEndTime())).append("\n");
        report.append("- 实际开始时间：").append(formatDateTime(verification.getActualStartTime())).append("\n");
        report.append("- 实际结束时间：").append(formatDateTime(verification.getActualEndTime())).append("\n\n");
        
        report.append("## 进出核验\n");
        report.append("- 进入核验：").append(Boolean.TRUE.equals(verification.getAccessVerified()) ? "✅ 已验证" : "❌ 未验证").append("\n");
        report.append("- 是否按时进入：").append(Boolean.TRUE.equals(verification.getOnTimeEntry()) ? "✅ 是" : "❌ 否").append("\n");
        report.append("- 是否按时完成：").append(Boolean.TRUE.equals(verification.getOnTimeCompletion()) ? "✅ 是" : "❌ 否").append("\n");
        report.append("- 停留时长：").append(verification.getStayDuration()).append(" 分钟\n\n");
        
        report.append("## 照片核验\n");
        report.append("- 计划设备数：").append(verification.getPlannedDeviceCount()).append("\n");
        report.append("- 实际设备数：").append(verification.getActualDeviceCount()).append("\n");
        report.append("- 通过数量：").append(verification.getPhotoPassedCount()).append("\n");
        report.append("- 失败数量：").append(verification.getPhotoFailedCount()).append("\n");
        report.append("- 通过率：").append(String.format("%.2f%%", verification.getPhotoPassRate())).append("\n\n");
        
        report.append("## 质量评分\n");
        report.append("- 综合评分：").append(verification.getQualityScore()).append(" 分\n");
        report.append("- 评级等级：").append(getGradeLevelText(verification.getGradeLevel())).append("\n\n");
        
        if (verification.getAbnormalBehaviors() != null && !verification.getAbnormalBehaviors().isEmpty()) {
            report.append("## 异常行为\n");
            try {
                List<String> behaviors = objectMapper.readValue(verification.getAbnormalBehaviors(), List.class);
                for (String behavior : behaviors) {
                    report.append("- ").append(getAbnormalBehaviorText(behavior)).append("\n");
                }
            } catch (Exception e) {
                log.error("解析异常行为失败", e);
            }
            report.append("\n");
        }
        
        report.append("## 改进建议\n");
        if (verification.getQualityScore() >= 90) {
            report.append("✅ 巡检质量优秀，继续保持\n");
        } else if (verification.getQualityScore() >= 80) {
            report.append("⚠️ 巡检质量良好，可适当优化流程\n");
        } else if (verification.getQualityScore() >= 60) {
            report.append("⚠️ 巡检质量合格，建议加强培训\n");
        } else {
            report.append("❌ 巡检质量不合格，需要立即整改\n");
        }
        
        return report.toString();
    }

    @Override
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<InspectionVerification> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null) {
            wrapper.ge(InspectionVerification::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InspectionVerification::getCreateTime, endTime);
        }
        
        List<InspectionVerification> verifications = this.list(wrapper);
        
        stats.put("totalCount", verifications.size());
        
        long passedCount = verifications.stream()
                .filter(v -> "completed".equals(v.getVerificationStatus()) && v.getQualityScore() != null && v.getQualityScore() >= 80)
                .count();
        long failedCount = verifications.stream()
                .filter(v -> "completed".equals(v.getVerificationStatus()) && (v.getQualityScore() == null || v.getQualityScore() < 60))
                .count();
        
        stats.put("passedCount", passedCount);
        stats.put("failedCount", failedCount);
        
        double avgScore = verifications.stream()
                .filter(v -> v.getQualityScore() != null)
                .mapToInt(InspectionVerification::getQualityScore)
                .average()
                .orElse(0.0);
        stats.put("avgScore", avgScore);
        
        return stats;
    }

    // ==================== 私有辅助方法 ====================

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "未记录";
    }

    private String determineGradeLevel(Integer score) {
        if (score == null) return "unknown";
        if (score >= 90) return "excellent";
        if (score >= 80) return "good";
        if (score >= 60) return "average";
        if (score >= 40) return "poor";
        return "fail";
    }

    private String getGradeLevelText(String grade) {
        Map<String, String> map = new HashMap<>();
        map.put("excellent", "优秀");
        map.put("good", "良好");
        map.put("average", "合格");
        map.put("poor", "较差");
        map.put("fail", "不合格");
        map.put("unknown", "未知");
        return map.getOrDefault(grade, "未知");
    }

    private String getAbnormalBehaviorText(String behavior) {
        Map<String, String> map = new HashMap<>();
        map.put("fast_pass", "快速通过（停留时间过短）");
        map.put("missed_route", "未按计划路线巡检");
        map.put("incomplete", "未完成所有设备巡检");
        map.put("repeated", "重复巡检同一设备");
        return map.getOrDefault(behavior, behavior);
    }
}
