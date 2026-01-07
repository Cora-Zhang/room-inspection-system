package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.PhotoVerification;

import java.util.List;
import java.util.Map;

/**
 * 照片核验服务接口
 * 支持照片质量检查、OCR识别、时间地点一致性核验
 */
public interface PhotoVerificationService extends IService<PhotoVerification> {

    /**
     * 分页查询照片核验记录
     */
    Page<PhotoVerification> queryPage(Long inspectionTaskId, Long deviceId, String status,
                                        int pageNum, int pageSize);

    /**
     * 上传并核验照片
     */
    PhotoVerification uploadAndVerify(Long inspectionTaskId, Long deviceId,
                                      Long uploaderId, String uploaderName,
                                      String photoPath, String photoUrl);

    /**
     * 照片质量检查（清晰度、亮度、模糊度等）
     */
    Map<String, Object> checkPhotoQuality(String photoPath);

    /**
     * OCR识别设备标签信息
     */
    Map<String, Object> ocrDeviceLabel(String photoPath);

    /**
     * 时间一致性核验
     */
    boolean verifyTimeConsistency(Long photoId, LocalDateTime expectedTime);

    /**
     * 位置一致性核验（基于门禁记录）
     */
    boolean verifyLocationConsistency(Long photoId, Long roomId, LocalDateTime photoTime);

    /**
     * 添加照片水印
     */
    String addWatermark(String photoPath, String watermarkInfo);

    /**
     * 批量核验巡检任务的照片
     */
    Map<String, Object> batchVerifyPhotos(Long inspectionTaskId);

    /**
     * 统计巡检任务照片核验情况
     */
    Map<String, Object> getStatistics(Long inspectionTaskId);

    /**
     * 查询异常照片
     */
    List<PhotoVerification> findAbnormalPhotos();

    /**
     * 人工复核照片
     */
    boolean manualReview(Long photoId, Long verifierId, String status, String comment);

    /**
     * 删除照片记录
     */
    boolean deletePhoto(Long photoId);
}
