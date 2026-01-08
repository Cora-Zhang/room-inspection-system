package com.roominspection.backend.ai;

import java.util.List;
import java.util.Map;

/**
 * AI识别服务接口
 * 支持图像识别、预测性维护等AI功能
 */
public interface AIRecognitionService {

    /**
     * 设备指示灯识别
     * @param deviceId 设备ID
     * @param imageBase64 图片Base64编码
     * @param lightPositions 指示灯位置列表
     * @return 识别结果
     */
    Map<String, Object> recognizeDeviceLights(String deviceId, String imageBase64, List<Map<String, Object>> lightPositions);

    /**
     * 巡检路线优化
     * @param roomId 机房ID
     * @param deviceIds 设备ID列表
     * @param startLocation 起始位置
     * @param constraints 约束条件
     * @return 优化后的路线
     */
    Map<String, Object> optimizeInspectionRoute(String roomId, List<String> deviceIds, Map<String, Integer> startLocation, Map<String, Object> constraints);

    /**
     * 预测性维护分析
     * @param deviceId 设备ID
     * @param predictionType 预测类型（failure/performance/health）
     * @param timeRange 时间范围（天数）
     * @return 预测结果
     */
    Map<String, Object> predictiveMaintenanceAnalysis(String deviceId, String predictionType, Integer timeRange);

    /**
     * 异常检测
     * @param deviceId 设备ID
     * @param metrics 指标数据
     * @return 检测结果
     */
    Map<String, Object> anomalyDetection(String deviceId, Map<String, Object> metrics);

    /**
     * 智能告警分析
     * @param deviceId 设备ID
     * @param alarmData 告警数据
     * @return 分析结果
     */
    Map<String, Object> intelligentAlarmAnalysis(String deviceId, Map<String, Object> alarmData);

    /**
     * 能效优化建议
     * @param roomId 机房ID
     * @param period 时间周期
     * @return 优化建议
     */
    Map<String, Object> energyEfficiencyOptimization(String roomId, String period);
}
