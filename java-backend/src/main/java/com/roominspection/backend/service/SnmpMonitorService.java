package com.roominspection.backend.service;

import com.roominspection.backend.entity.Device;
import com.roominspection.backend.entity.DeviceMetric;
import java.util.List;
import java.util.Map;

/**
 * SNMP监控服务接口
 */
public interface SnmpMonitorService {

    /**
     * 通过SNMP采集设备指标
     *
     * @param device 设备信息
     * @return 采集的指标列表
     */
    List<DeviceMetric> collectMetrics(Device device);

    /**
     * 采集CPU使用率
     *
     * @param device 设备信息
     * @return CPU使用率（%）
     */
    Double collectCpuUsage(Device device);

    /**
     * 采集内存使用率
     *
     * @param device 设备信息
     * @return 内存使用率（%）
     */
    Double collectMemoryUsage(Device device);

    /**
     * 采集磁盘使用率
     *
     * @param device 设备信息
     * @return 磁盘使用率信息
     */
    Map<String, Object> collectDiskUsage(Device device);

    /**
     * 采集网络端口状态
     *
     * @param device 设备信息
     * @return 端口状态列表
     */
    List<Map<String, Object>> collectPortStatus(Device device);

    /**
     * 采集网络流量
     *
     * @param device 设备信息
     * @return 流量信息
     */
    Map<String, Object> collectTraffic(Device device);

    /**
     * 测试SNMP连接
     *
     * @param ipAddress IP地址
     * @param port      端口
     * @param community Community
     * @param version   版本
     * @return 测试结果
     */
    Map<String, Object> testConnection(String ipAddress, Integer port, String community, String version);
}
