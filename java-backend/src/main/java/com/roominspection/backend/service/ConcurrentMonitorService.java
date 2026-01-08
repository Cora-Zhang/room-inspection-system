package com.roominspection.backend.service;

import com.roominspection.backend.entity.Device;
import com.roominspection.backend.entity.DeviceMetric;
import com.roominspection.backend.entity.MonitorTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 并发监控数据采集服务接口
 */
public interface ConcurrentMonitorService {

    /**
     * 并发采集指定设备的监控数据
     *
     * @param devices 设备列表
     * @return 采集任务Future列表
     */
    List<CompletableFuture<MonitorTask>> collectDeviceMetrics(List<Device> devices);

    /**
     * 并发采集指定机房的监控数据
     *
     * @param roomId 机房ID
     * @return 采集结果统计
     */
    Map<String, Object> collectRoomMetrics(String roomId);

    /**
     * 并发采集所有设备的监控数据
     *
     * @return 采集结果统计
     */
    Map<String, Object> collectAllDeviceMetrics();

    /**
     * 启动定时监控任务
     */
    void startScheduledTasks();

    /**
     * 停止定时监控任务
     */
    void stopScheduledTasks();

    /**
     * 获取采集性能统计
     *
     * @return 性能统计
     */
    Map<String, Object> getPerformanceStatistics();

    /**
     * 获取当前运行的采集任务数
     *
     * @return 任务数
     */
    int getRunningTaskCount();

    /**
     * 采集单个设备指标
     *
     * @param device 设备
     * @return 指标列表
     */
    List<DeviceMetric> collectSingleDevice(Device device);
}
