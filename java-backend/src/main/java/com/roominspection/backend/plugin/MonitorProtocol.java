package com.roominspection.backend.plugin;

import java.util.List;
import java.util.Map;

/**
 * 监控协议接口
 * 所有自定义监控协议需要实现此接口
 */
public interface MonitorProtocol {

    /**
     * 初始化协议
     * @param config 配置参数
     * @throws Exception 初始化异常
     */
    void init(Map<String, Object> config) throws Exception;

    /**
     * 连接设备
     * @param deviceConfig 设备配置
     * @return 连接是否成功
     */
    boolean connect(Map<String, Object> deviceConfig);

    /**
     * 断开连接
     * @param deviceId 设备ID
     */
    void disconnect(String deviceId);

    /**
     * 读取数据
     * @param deviceId 设备ID
     * @param metrics 要读取的指标列表
     * @return 监控数据
     */
    Map<String, Object> readData(String deviceId, List<String> metrics);

    /**
     * 写入数据
     * @param deviceId 设备ID
     * @param data 要写入的数据
     * @return 写入是否成功
     */
    boolean writeData(String deviceId, Map<String, Object> data);

    /**
     * 获取设备状态
     * @param deviceId 设备ID
     * @return 设备状态
     */
    String getDeviceStatus(String deviceId);

    /**
     * 健康检查
     * @param deviceId 设备ID
     * @return 是否健康
     */
    boolean healthCheck(String deviceId);

    /**
     * 销毁协议
     */
    void destroy();

    /**
     * 获取协议名称
     * @return 协议名称
     */
    String getProtocolName();

    /**
     * 获取协议版本
     * @return 协议版本
     */
    String getProtocolVersion();

    /**
     * 获取支持的指标列表
     * @return 支持的指标列表
     */
    List<String> getSupportedMetrics();
}
