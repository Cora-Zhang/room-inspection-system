package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roominspection.backend.entity.DoorAccessLog;
import com.roominspection.backend.mapper.DoorAccessLogMapper;
import com.roominspection.backend.service.DoorAccessLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 门禁日志服务实现类
 * 支持对接海康、大华等门禁系统API
 */
@Slf4j
@Service
public class DoorAccessLogServiceImpl extends ServiceImpl<DoorAccessLogMapper, DoorAccessLog>
        implements DoorAccessLogService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<DoorAccessLog> queryPage(Long roomId, Long staffId, String direction,
                                           LocalDateTime startTime, LocalDateTime endTime,
                                           int pageNum, int pageSize) {
        LambdaQueryWrapper<DoorAccessLog> wrapper = new LambdaQueryWrapper<>();
        
        if (roomId != null) {
            wrapper.eq(DoorAccessLog::getRoomId, roomId);
        }
        if (staffId != null) {
            wrapper.eq(DoorAccessLog::getStaffId, staffId);
        }
        if (direction != null) {
            wrapper.eq(DoorAccessLog::getDirection, direction);
        }
        if (startTime != null) {
            wrapper.ge(DoorAccessLog::getAccessTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(DoorAccessLog::getAccessTime, endTime);
        }
        
        wrapper.orderByDesc(DoorAccessLog::getAccessTime);
        
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromHikvision(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("开始从海康门禁系统同步门禁日志，时间范围：{} - {}", startTime, endTime);
        
        try {
            // 1. 获取海康API配置（从配置表）
            String apiUrl = getHikvisionApiUrl();
            String appKey = getHikvisionAppKey();
            String appSecret = getHikvisionAppSecret();
            
            // 2. 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("startTime", startTime.format(DATE_FORMATTER));
            params.put("endTime", endTime.format(DATE_FORMATTER));
            params.put("pageNo", 1);
            params.put("pageSize", 1000);
            
            // 3. 调用海康API
            // 注意：这里需要根据实际的海康API接口文档调整
            String response = restTemplate.postForObject(apiUrl, params, String.class);
            
            // 4. 解析响应数据
            JsonNode root = objectMapper.readTree(response);
            JsonNode dataList = root.path("data").path("list");
            
            int syncCount = 0;
            for (JsonNode item : dataList) {
                DoorAccessLog log = convertFromHikvision(item);
                if (log != null) {
                    this.save(log);
                    syncCount++;
                }
            }
            
            log.info("海康门禁日志同步完成，共同步{}条记录", syncCount);
            return syncCount;
            
        } catch (Exception e) {
            log.error("同步海康门禁日志失败", e);
            throw new RuntimeException("同步海康门禁日志失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromDahua(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("开始从大华门禁系统同步门禁日志，时间范围：{} - {}", startTime, endTime);
        
        try {
            // 1. 获取大华API配置
            String apiUrl = getDahuaApiUrl();
            String username = getDahuaUsername();
            String password = getDahuaPassword();
            
            // 2. 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("startTime", startTime.format(DATE_FORMATTER));
            params.put("endTime", endTime.format(DATE_FORMATTER));
            params.put("pageNum", 1);
            params.put("pageSize", 1000);
            
            // 3. 调用大华API
            String response = restTemplate.postForObject(apiUrl, params, String.class);
            
            // 4. 解析响应数据
            JsonNode root = objectMapper.readTree(response);
            JsonNode dataList = root.path("data").path("records");
            
            int syncCount = 0;
            for (JsonNode item : dataList) {
                DoorAccessLog log = convertFromDahua(item);
                if (log != null) {
                    this.save(log);
                    syncCount++;
                }
            }
            
            log.info("大华门禁日志同步完成，共同步{}条记录", syncCount);
            return syncCount;
            
        } catch (Exception e) {
            log.error("同步大华门禁日志失败", e);
            throw new RuntimeException("同步大华门禁日志失败：" + e.getMessage());
        }
    }

    @Override
    public DoorAccessLog verifyEntryBeforeInspection(Long inspectionTaskId, Long staffId,
                                                      Long roomId, LocalDateTime plannedStartTime) {
        log.info("核验巡检任务{}人员{}是否已进入机房{}，计划开始时间：{}", 
                inspectionTaskId, staffId, roomId, plannedStartTime);
        
        // 查询计划开始时间前30分钟到后30分钟内的进入记录
        LocalDateTime startTime = plannedStartTime.minusMinutes(30);
        LocalDateTime endTime = plannedStartTime.plusMinutes(30);
        
        List<DoorAccessLog> entryRecords = baseMapper.findEntryRecords(staffId, startTime, endTime);
        
        if (entryRecords.isEmpty()) {
            log.warn("未找到人员{}在指定时间范围内的进入记录", staffId);
            return null;
        }
        
        // 过滤出指定机房的记录
        List<DoorAccessLog> roomRecords = entryRecords.stream()
                .filter(log -> log.getRoomId().equals(roomId))
                .collect(Collectors.toList());
        
        if (roomRecords.isEmpty()) {
            log.warn("人员{}进入的机房与计划机房不符", staffId);
            return null;
        }
        
        // 返回最早的进入记录
        DoorAccessLog entryLog = roomRecords.get(0);
        
        // 更新核验信息
        entryLog.setInspectionTaskId(inspectionTaskId);
        entryLog.setVerified(true);
        entryLog.setVerificationResult("match");
        this.updateById(entryLog);
        
        log.info("核验通过：人员{}于{}进入机房{}", staffId, entryLog.getAccessTime(), roomId);
        return entryLog;
    }

    @Override
    public DoorAccessLog verifyExitAfterInspection(Long inspectionTaskId, Long staffId,
                                                    Long roomId, LocalDateTime plannedEndTime) {
        log.info("核验巡检任务{}人员{}是否已离开机房{}，计划结束时间：{}", 
                inspectionTaskId, staffId, roomId, plannedEndTime);
        
        // 查询计划结束时间前30分钟到后60分钟内的离开记录
        LocalDateTime startTime = plannedEndTime.minusMinutes(30);
        LocalDateTime endTime = plannedEndTime.plusMinutes(60);
        
        List<DoorAccessLog> exitRecords = baseMapper.findExitRecords(staffId, startTime, endTime);
        
        if (exitRecords.isEmpty()) {
            log.warn("未找到人员{}在指定时间范围内的离开记录", staffId);
            return null;
        }
        
        // 过滤出指定机房的记录
        List<DoorAccessLog> roomRecords = exitRecords.stream()
                .filter(log -> log.getRoomId().equals(roomId))
                .collect(Collectors.toList());
        
        if (roomRecords.isEmpty()) {
            log.warn("人员{}离开的机房与计划机房不符", staffId);
            return null;
        }
        
        // 返回最晚的离开记录
        DoorAccessLog exitLog = roomRecords.get(roomRecords.size() - 1);
        
        // 更新核验信息
        exitLog.setInspectionTaskId(inspectionTaskId);
        exitLog.setVerified(true);
        exitLog.setVerificationResult("match");
        this.updateById(exitLog);
        
        log.info("核验通过：人员{}于{}离开机房{}", staffId, exitLog.getAccessTime(), roomId);
        return exitLog;
    }

    @Override
    public Map<String, Object> calculateStayDuration(Long staffId, Long roomId,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        log.info("计算人员{}在机房{}的停留时长，时间范围：{} - {}", staffId, roomId, startTime, endTime);
        
        List<Map<String, Object>> results = baseMapper.calculateStayDuration(staffId, startTime, endTime);
        
        // 过滤出指定机房的记录
        List<Map<String, Object>> roomResults = results.stream()
                .filter(result -> result.get("room_id") != null && result.get("room_id").equals(roomId))
                .collect(Collectors.toList());
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("staffId", staffId);
        summary.put("roomId", roomId);
        summary.put("records", roomResults);
        
        if (!roomResults.isEmpty()) {
            // 计算总停留时长
            int totalStayMinutes = roomResults.stream()
                    .mapToInt(result -> (Integer) result.getOrDefault("stay_minutes", 0))
                    .sum();
            
            // 计算平均停留时长
            double avgStayMinutes = roomResults.stream()
                    .mapToInt(result -> (Integer) result.getOrDefault("stay_minutes", 0))
                    .average()
                    .orElse(0.0);
            
            summary.put("totalStayMinutes", totalStayMinutes);
            summary.put("avgStayMinutes", avgStayMinutes);
            summary.put("entryCount", roomResults.size());
        } else {
            summary.put("totalStayMinutes", 0);
            summary.put("avgStayMinutes", 0.0);
            summary.put("entryCount", 0);
        }
        
        return summary;
    }

    @Override
    public Map<String, Object> analyzeInspectionAccess(Long inspectionTaskId, Long staffId,
                                                         Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("分析巡检任务{}的进出记录", inspectionTaskId);
        
        Map<String, Object> analysis = new HashMap<>();
        
        // 1. 核验进入记录
        DoorAccessLog entryLog = verifyEntryBeforeInspection(inspectionTaskId, staffId, roomId, startTime);
        analysis.put("entryVerified", entryLog != null);
        analysis.put("entryLog", entryLog);
        
        // 2. 核验离开记录
        DoorAccessLog exitLog = verifyExitAfterInspection(inspectionTaskId, staffId, roomId, endTime);
        analysis.put("exitVerified", exitLog != null);
        analysis.put("exitLog", exitLog);
        
        // 3. 计算停留时长
        if (entryLog != null && exitLog != null) {
            long stayMinutes = java.time.Duration.between(entryLog.getAccessTime(), exitLog.getAccessTime()).toMinutes();
            analysis.put("stayDuration", stayMinutes);
            
            // 判断停留时长是否合理（一般巡检不少于15分钟）
            analysis.put("stayDurationReasonable", stayMinutes >= 15);
        }
        
        // 4. 检查是否有快速通过异常（进入后短时间内离开）
        if (entryLog != null && exitLog != null) {
            long stayMinutes = java.time.Duration.between(entryLog.getAccessTime(), exitLog.getAccessTime()).toMinutes();
            analysis.put("fastPassDetected", stayMinutes < 5);
        }
        
        // 5. 查询该时间段内的所有进出记录
        List<DoorAccessLog> allRecords = baseMapper.findByStaffIdAndTimeRange(staffId, startTime, endTime);
        analysis.put("allRecords", allRecords);
        
        return analysis;
    }

    @Override
    public List<Map<String, Object>> getRoomAccessStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.getRoomAccessStatistics(startTime, endTime);
    }

    @Override
    public String generateAccessReport(Long inspectionTaskId) {
        // 根据inspectionTaskId获取详细信息，生成Markdown格式的报告
        StringBuilder report = new StringBuilder();
        report.append("# 巡检门禁核验报告\n\n");
        report.append("## 巡检任务ID：").append(inspectionTaskId).append("\n\n");
        
        // TODO: 根据实际情况完善报告内容
        
        return report.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualAdd(DoorAccessLog doorAccessLog) {
        doorAccessLog.setDataSource("manual");
        doorAccessLog.setCreateTime(LocalDateTime.now());
        return this.save(doorAccessLog);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将海康API返回的数据转换为DoorAccessLog对象
     */
    private DoorAccessLog convertFromHikvision(JsonNode item) {
        try {
            DoorAccessLog log = new DoorAccessLog();
            log.setSystemType("hikvision");
            
            // 根据海康API的实际返回字段映射
            log.setDeviceId(item.path("deviceIndexCode").asText());
            log.setDeviceName(item.path("deviceName").asText());
            log.setStaffId(item.path("personId").asLong());
            log.setStaffName(item.path("personName").asText());
            log.setStaffCode(item.path("cardNo").asText());
            
            String direction = item.path("direction").asText();
            log.setDirection("in".equals(direction) ? "in" : "out");
            
            String accessTimeStr = item.path("time").asText();
            log.setAccessTime(LocalDateTime.parse(accessTimeStr, DATE_FORMATTER));
            
            String accessMethod = item.path("method").asText();
            log.setAccessMethod(accessMethod);
            
            String status = item.path("status").asText();
            log.setStatus("success".equals(status) ? "success" : "failed");
            
            log.setPhotoUrl(item.path("picUri").asText());
            log.setDataSource("sync");
            log.setVerified(false);
            log.setCreateTime(LocalDateTime.now());
            
            return log;
            
        } catch (Exception e) {
            log.error("转换海康门禁数据失败", e);
            return null;
        }
    }

    /**
     * 将大华API返回的数据转换为DoorAccessLog对象
     */
    private DoorAccessLog convertFromDahua(JsonNode item) {
        try {
            DoorAccessLog log = new DoorAccessLog();
            log.setSystemType("dahua");
            
            // 根据大华API的实际返回字段映射
            log.setDeviceId(item.path("deviceId").asText());
            log.setDeviceName(item.path("deviceName").asText());
            log.setStaffId(item.path("userId").asLong());
            log.setStaffName(item.path("userName").asText());
            log.setStaffCode(item.path("cardNo").asText());
            
            String direction = item.path("direction").asText();
            log.setDirection("entry".equals(direction) ? "in" : "out");
            
            String accessTimeStr = item.path("accessTime").asText();
            log.setAccessTime(LocalDateTime.parse(accessTimeStr, DATE_FORMATTER));
            
            String accessMethod = item.path("verifyType").asText();
            log.setAccessMethod(accessMethod);
            
            String status = item.path("result").asText();
            log.setStatus("success".equals(status) ? "success" : "failed");
            
            log.setPhotoUrl(item.path("imageUrl").asText());
            log.setDataSource("sync");
            log.setVerified(false);
            log.setCreateTime(LocalDateTime.now());
            
            return log;
            
        } catch (Exception e) {
            log.error("转换大华门禁数据失败", e);
            return null;
        }
    }

    // ==================== 配置获取方法 ====================
    
    private String getHikvisionApiUrl() {
        // 从配置表或配置文件获取
        return "http://hikvision-api-url";
    }

    private String getHikvisionAppKey() {
        // 从配置表或配置文件获取
        return "your-app-key";
    }

    private String getHikvisionAppSecret() {
        // 从配置表或配置文件获取
        return "your-app-secret";
    }

    private String getDahuaApiUrl() {
        // 从配置表或配置文件获取
        return "http://dahua-api-url";
    }

    private String getDahuaUsername() {
        // 从配置表或配置文件获取
        return "admin";
    }

    private String getDahuaPassword() {
        // 从配置表或配置文件获取
        return "password";
    }
}
