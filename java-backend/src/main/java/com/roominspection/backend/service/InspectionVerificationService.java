package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.InspectionVerification;

/**
 * 巡检核验服务接口
 * 支持巡检完整性分析、质量评分、报告生成
 */
public interface InspectionVerificationService extends IService<InspectionVerification> {

    /**
     * 分页查询巡检核验记录
     */
    Page<InspectionVerification> queryPage(Long roomId, Long inspectorId, String status,
                                             int pageNum, int pageSize);

    /**
     * 创建巡检核验记录
     */
    InspectionVerification createVerification(Long inspectionTaskId, String taskNo, Long roomId,
                                             String roomName, Long inspectorId, String inspectorName,
                                             String plannedRoute, Integer plannedDeviceCount);

    /**
     * 分析巡检完整性
     */
    InspectionVerification analyzeCompleteness(Long verificationId);

    /**
     * 计算巡检质量评分
     */
    Integer calculateQualityScore(Long verificationId);

    /**
     * 生成巡检核验报告
     */
    String generateVerificationReport(Long verificationId);

    /**
     * 获取巡检核验统计
     */
    java.util.Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime);
}
