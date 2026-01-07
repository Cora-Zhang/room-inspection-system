package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roominspection.backend.entity.DoorAccessLog;
import com.roominspection.backend.entity.PhotoVerification;
import com.roominspection.backend.mapper.PhotoVerificationMapper;
import com.roominspection.backend.service.DoorAccessLogService;
import com.roominspection.backend.service.PhotoVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * 照片核验服务实现类
 */
@Slf4j
@Service
public class PhotoVerificationServiceImpl extends ServiceImpl<PhotoVerificationMapper, PhotoVerification>
        implements PhotoVerificationService {

    @Autowired
    private DoorAccessLogService doorAccessLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${photo.quality.clarity.threshold:60}")
    private int clarityThreshold;

    @Value("${photo.quality.brightness.min:30}")
    private int brightnessMin;

    @Value("${photo.quality.brightness.max:220}")
    private int brightnessMax;

    @Override
    public Page<PhotoVerification> queryPage(Long inspectionTaskId, Long deviceId, String status,
                                               int pageNum, int pageSize) {
        LambdaQueryWrapper<PhotoVerification> wrapper = new LambdaQueryWrapper<>();
        
        if (inspectionTaskId != null) {
            wrapper.eq(PhotoVerification::getInspectionTaskId, inspectionTaskId);
        }
        if (deviceId != null) {
            wrapper.eq(PhotoVerification::getDeviceId, deviceId);
        }
        if (status != null) {
            wrapper.eq(PhotoVerification::getVerificationStatus, status);
        }
        
        wrapper.orderByDesc(PhotoVerification::getPhotoTime);
        
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PhotoVerification uploadAndVerify(Long inspectionTaskId, Long deviceId,
                                             Long uploaderId, String uploaderName,
                                             String photoPath, String photoUrl) {
        log.info("上传并核验照片，任务ID：{}，设备ID：{}", inspectionTaskId, deviceId);
        
        PhotoVerification verification = new PhotoVerification();
        verification.setInspectionTaskId(inspectionTaskId);
        verification.setDeviceId(deviceId);
        verification.setPhotoPath(photoPath);
        verification.setPhotoUrl(photoUrl);
        verification.setUploaderId(uploaderId);
        verification.setUploaderName(uploaderName);
        verification.setPhotoTime(LocalDateTime.now());
        verification.setVerificationStatus("pending");
        
        try {
            // 1. 读取照片信息
            File photoFile = new File(photoPath);
            BufferedImage image = ImageIO.read(photoFile);
            
            verification.setResolution(image.getWidth() + "x" + image.getHeight());
            verification.setFileSize(photoFile.length());
            
            // 2. 质量检查
            Map<String, Object> qualityCheck = checkPhotoQuality(photoPath);
            verification.setClarityScore((Integer) qualityCheck.get("clarityScore"));
            verification.setBrightnessScore((Integer) qualityCheck.get("brightnessScore"));
            verification.setBlurStatus((String) qualityCheck.get("blurStatus"));
            
            // 3. OCR识别设备标签
            Map<String, Object> ocrResult = ocrDeviceLabel(photoPath);
            verification.setOcrResult(objectMapper.writeValueAsString(ocrResult));
            verification.setOcrConfidence((Double) ocrResult.getOrDefault("confidence", 0.0));
            verification.setLabelRecognizable((Boolean) ocrResult.getOrDefault("recognizable", false));
            
            // 4. 时间和位置核验（稍后异步处理）
            verification.setTimeConsistent(null);
            verification.setLocationConsistent(null);
            
            // 5. 判断核验状态
            String status = determineVerificationStatus(verification);
            verification.setVerificationStatus(status);
            verification.setVerificationSummary(generateVerificationSummary(verification));
            
            // 6. 保存记录
            this.save(verification);
            
            log.info("照片核验完成，状态：{}", status);
            return verification;
            
        } catch (Exception e) {
            log.error("照片核验失败", e);
            verification.setVerificationStatus("failed");
            verification.setVerificationSummary("核验失败：" + e.getMessage());
            this.save(verification);
            return verification;
        }
    }

    @Override
    public Map<String, Object> checkPhotoQuality(String photoPath) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File photoFile = new File(photoPath);
            BufferedImage image = ImageIO.read(photoFile);
            
            // 1. 计算清晰度（基于拉普拉斯方差）
            int clarityScore = calculateClarityScore(image);
            result.put("clarityScore", clarityScore);
            
            // 2. 计算亮度
            int brightnessScore = calculateBrightnessScore(image);
            result.put("brightnessScore", brightnessScore);
            
            // 3. 判断模糊度
            String blurStatus = "normal";
            List<String> abnormalities = new ArrayList<>();
            
            if (clarityScore < clarityThreshold) {
                blurStatus = clarityScore < clarityThreshold / 2 ? "very_blurry" : "blurry";
                abnormalities.add("blur");
            }
            
            if (brightnessScore < brightnessMin || brightnessScore > brightnessMax) {
                abnormalities.add(brightnessScore < brightnessMin ? "dark" : "too_bright");
            }
            
            result.put("blurStatus", blurStatus);
            result.put("abnormalities", abnormalities);
            
            return result;
            
        } catch (Exception e) {
            log.error("照片质量检查失败：{}", photoPath, e);
            result.put("clarityScore", 0);
            result.put("brightnessScore", 0);
            result.put("blurStatus", "error");
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> ocrDeviceLabel(String photoPath) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // TODO: 集成OCR服务（如百度OCR、阿里云OCR等）
            // 这里提供模拟实现，实际需要调用OCR API
            
            // 模拟OCR识别结果
            Map<String, String> recognizedText = new HashMap<>();
            recognizedText.put("deviceName", "Server-001");
            recognizedText.put("serialNumber", "SN12345678");
            recognizedText.put("model", "Dell R740");
            recognizedText.put("ipAddress", "192.168.1.10");
            
            // 模拟置信度
            double confidence = 0.95;
            
            result.put("recognizedText", recognizedText);
            result.put("confidence", confidence);
            result.put("recognizable", confidence > 0.8);
            
            return result;
            
        } catch (Exception e) {
            log.error("OCR识别失败：{}", photoPath, e);
            result.put("recognizedText", new HashMap<>());
            result.put("confidence", 0.0);
            result.put("recognizable", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyTimeConsistency(Long photoId, LocalDateTime expectedTime) {
        PhotoVerification photo = this.getById(photoId);
        if (photo == null) {
            return false;
        }
        
        LocalDateTime photoTime = photo.getPhotoTime();
        if (photoTime == null) {
            photo.setTimeConsistent(false);
            photo.setTimeDeviation(null);
            this.updateById(photo);
            return false;
        }
        
        // 计算时间偏差（秒）
        long deviation = Math.abs(java.time.Duration.between(expectedTime, photoTime).getSeconds());
        
        // 判断是否在合理范围内（前后30分钟内）
        boolean consistent = deviation <= 1800;
        
        photo.setTimeConsistent(consistent);
        photo.setTimeDeviation((int) deviation);
        
        // 更新核验状态
        String status = determineVerificationStatus(photo);
        photo.setVerificationStatus(status);
        photo.setVerificationSummary(generateVerificationSummary(photo));
        
        this.updateById(photo);
        return consistent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyLocationConsistency(Long photoId, Long roomId, LocalDateTime photoTime) {
        PhotoVerification photo = this.getById(photoId);
        if (photo == null) {
            return false;
        }
        
        try {
            // 查询人员在指定时间范围内的门禁记录
            LocalDateTime startTime = photoTime.minusMinutes(5);
            LocalDateTime endTime = photoTime.plusMinutes(5);
            
            List<DoorAccessLog> accessLogs = doorAccessLogService.queryPage(roomId, photo.getUploaderId(),
                    null, startTime, endTime, 1, 10).getRecords();
            
            // 判断是否有进入记录
            boolean consistent = !accessLogs.isEmpty();
            
            photo.setLocationConsistent(consistent);
            
            // 更新核验状态
            String status = determineVerificationStatus(photo);
            photo.setVerificationStatus(status);
            photo.setVerificationSummary(generateVerificationSummary(photo));
            
            this.updateById(photo);
            return consistent;
            
        } catch (Exception e) {
            log.error("位置一致性核验失败", e);
            photo.setLocationConsistent(false);
            this.updateById(photo);
            return false;
        }
    }

    @Override
    public String addWatermark(String photoPath, String watermarkInfo) {
        try {
            File photoFile = new File(photoPath);
            BufferedImage image = ImageIO.read(photoFile);
            
            // 创建带水印的图片
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            
            // 设置水印样式
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            
            // 添加水印
            String[] lines = watermarkInfo.split("\n");
            int y = image.getHeight() - 50;
            for (String line : lines) {
                g2d.drawString(line, 20, y);
                y -= 25;
            }
            
            g2d.dispose();
            
            // 保存带水印的图片
            String watermarkPath = photoPath.replace(".jpg", "_watermarked.jpg");
            ImageIO.write(image, "jpg", new File(watermarkPath));
            
            return watermarkPath;
            
        } catch (Exception e) {
            log.error("添加水印失败", e);
            return photoPath;
        }
    }

    @Override
    public Map<String, Object> batchVerifyPhotos(Long inspectionTaskId) {
        log.info("批量核验巡检任务{}的照片", inspectionTaskId);
        
        Map<String, Object> result = new HashMap<>();
        
        // 查询所有待核验的照片
        List<PhotoVerification> photos = baseMapper.findByInspectionTaskId(inspectionTaskId);
        
        int passedCount = 0;
        int failedCount = 0;
        int manualCount = 0;
        
        for (PhotoVerification photo : photos) {
            try {
                // 重新执行核验
                // TODO: 这里可以添加更多的核验逻辑
                String status = determineVerificationStatus(photo);
                photo.setVerificationStatus(status);
                this.updateById(photo);
                
                switch (status) {
                    case "passed":
                        passedCount++;
                        break;
                    case "failed":
                        failedCount++;
                        break;
                    case "manual":
                        manualCount++;
                        break;
                }
            } catch (Exception e) {
                log.error("核验照片{}失败", photo.getId(), e);
                failedCount++;
            }
        }
        
        result.put("totalCount", photos.size());
        result.put("passedCount", passedCount);
        result.put("failedCount", failedCount);
        result.put("manualCount", manualCount);
        result.put("passRate", photos.isEmpty() ? 0 : (double) passedCount / photos.size() * 100);
        
        return result;
    }

    @Override
    public Map<String, Object> getStatistics(Long inspectionTaskId) {
        return baseMapper.getStatistics(inspectionTaskId);
    }

    @Override
    public List<PhotoVerification> findAbnormalPhotos() {
        return baseMapper.findAbnormalPhotos();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean manualReview(Long photoId, Long verifierId, String status, String comment) {
        PhotoVerification photo = this.getById(photoId);
        if (photo == null) {
            return false;
        }
        
        photo.setVerificationStatus(status);
        photo.setVerifierId(verifierId);
        photo.setVerificationTime(LocalDateTime.now());
        if (comment != null && !comment.isEmpty()) {
            photo.setVerificationSummary(comment);
        }
        
        return this.updateById(photo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePhoto(Long photoId) {
        return this.removeById(photoId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算清晰度评分（基于拉普拉斯方差）
     */
    private int calculateClarityScore(BufferedImage image) {
        // TODO: 实现更精确的清晰度计算算法
        // 这里提供简化实现
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 简单的边缘检测来评估清晰度
        int edgeCount = 0;
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = image.getRGB(x, y) & 0xFF;
                int top = image.getRGB(x, y - 1) & 0xFF;
                int bottom = image.getRGB(x, y + 1) & 0xFF;
                int left = image.getRGB(x - 1, y) & 0xFF;
                int right = image.getRGB(x + 1, y) & 0xFF;
                
                int diff = Math.abs(center - top) + Math.abs(center - bottom) +
                          Math.abs(center - left) + Math.abs(center - right);
                
                if (diff > 50) {
                    edgeCount++;
                }
            }
        }
        
        // 归一化到0-100分
        int score = (int) (edgeCount * 100.0 / (width * height) * 4);
        return Math.min(100, score);
    }

    /**
     * 计算亮度评分
     */
    private int calculateBrightnessScore(BufferedImage image) {
        long totalBrightness = 0;
        int pixelCount = image.getWidth() * image.getHeight();
        
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                totalBrightness += (r + g + b) / 3;
            }
        }
        
        int avgBrightness = (int) (totalBrightness / pixelCount);
        return avgBrightness;
    }

    /**
     * 确定核验状态
     */
    private String determineVerificationStatus(PhotoVerification photo) {
        List<String> issues = new ArrayList<>();
        
        // 检查清晰度
        if (photo.getClarityScore() != null && photo.getClarityScore() < clarityThreshold) {
            issues.add("blur");
        }
        
        // 检查亮度
        if (photo.getBrightnessScore() != null &&
            (photo.getBrightnessScore() < brightnessMin || photo.getBrightnessScore() > brightnessMax)) {
            issues.add(photo.getBrightnessScore() < brightnessMin ? "dark" : "too_bright");
        }
        
        // 检查标签识别
        if (!Boolean.TRUE.equals(photo.getLabelRecognizable())) {
            issues.add("no_label");
        }
        
        // 检查时间一致性
        if (!Boolean.TRUE.equals(photo.getTimeConsistent())) {
            issues.add("time_inconsistent");
        }
        
        // 检查位置一致性
        if (!Boolean.TRUE.equals(photo.getLocationConsistent())) {
            issues.add("location_inconsistent");
        }
        
        // 判断状态
        if (issues.isEmpty()) {
            return "passed";
        } else if (issues.size() == 1 && ("time_inconsistent".equals(issues.get(0)) ||
                                           "location_inconsistent".equals(issues.get(0)))) {
            return "manual";  // 时间或位置不一致需要人工复核
        } else {
            photo.setAbnormalType(String.join(",", issues));
            return "failed";
        }
    }

    /**
     * 生成核验摘要
     */
    private String generateVerificationSummary(PhotoVerification photo) {
        StringBuilder summary = new StringBuilder();
        
        if ("passed".equals(photo.getVerificationStatus())) {
            summary.append("照片质量良好，设备标签可识别，时间地点一致");
        } else if ("failed".equals(photo.getVerificationStatus())) {
            summary.append("照片核验未通过：");
            if ("blurry".equals(photo.getBlurStatus()) || "very_blurry".equals(photo.getBlurStatus())) {
                summary.append("照片模糊");
            }
            if (!Boolean.TRUE.equals(photo.getLabelRecognizable())) {
                summary.append("设备标签不可识别");
            }
        } else if ("manual".equals(photo.getVerificationStatus())) {
            summary.append("需要人工复核：");
            if (!Boolean.TRUE.equals(photo.getTimeConsistent())) {
                summary.append("拍摄时间与计划时间偏差较大");
            }
            if (!Boolean.TRUE.equals(photo.getLocationConsistent())) {
                summary.append("拍摄位置与门禁记录不符");
            }
        }
        
        return summary.toString();
    }
}
