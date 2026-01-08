package com.roominspection.backend.service;

import java.util.Map;

/**
 * WebSocket消息推送服务接口
 */
public interface WebSocketMessageService {

    /**
     * 推送告警消息
     *
     * @param alarmData 告警数据
     */
    void pushAlarm(Object alarmData);

    /**
     * 推送告警统计
     *
     * @param statistics 统计数据
     */
    void pushAlarmStatistics(Map<String, Object> statistics);

    /**
     * 推送设备指标数据
     *
     * @param metricsData 指标数据
     */
    void pushDeviceMetrics(Object metricsData);

    /**
     * 推送采集任务状态
     *
     * @param taskData 任务数据
     */
    void pushTaskStatus(Object taskData);

    /**
     * 推送性能统计
     *
     * @param statistics 统计数据
     */
    void pushPerformanceStatistics(Map<String, Object> statistics);

    /**
     * 获取告警WebSocket连接数
     *
     * @return 连接数
     */
    int getAlarmConnectionCount();

    /**
     * 获取监控数据WebSocket连接数
     *
     * @return 连接数
     */
    int getMonitorConnectionCount();
}
