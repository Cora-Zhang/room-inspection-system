package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.DeviceMetric;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备监控指标服务接口
 */
public interface DeviceMetricService extends IService<DeviceMetric> {

    /**
     * 批量保存指标
     *
     * @param metrics 指标列表
     * @return 是否成功
     */
    boolean saveBatchMetrics(List<DeviceMetric> metrics);

    /**
     * 获取设备最新指标
     *
     * @param deviceId 设备ID
     * @return 指标列表
     */
    List<DeviceMetric> getLatestMetricsByDeviceId(String deviceId);

    /**
     * 获取机房所有最新指标
     *
     * @param roomId 机房ID
     * @return 指标列表
     */
    List<DeviceMetric> getLatestMetricsByRoomId(String roomId);

    /**
     * 获取告警指标
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 指标列表
     */
    List<DeviceMetric> getAlarmMetrics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理历史指标数据
     *
     * @param days 保留天数
     * @return 删除数量
     */
    int cleanHistoryMetrics(int days);

    /**
     * 根据阈值判断指标状态
     *
     * @param metric          指标
     * @param thresholdUpper  阈值上限
     * @param thresholdLower  阈值下限
     * @return 状态
     */
    String evaluateMetricStatus(DeviceMetric metric, Double thresholdUpper, Double thresholdLower);
}
