package com.roominspection.backend.protocol;

import java.util.Map;

/**
 * 设备协议接口
 * 定义了所有IDC设备协议适配器必须实现的方法
 */
public interface DeviceProtocol {

    /**
     * 协议名称
     */
    String getProtocolName();

    /**
     * 协议版本
     */
    String getProtocolVersion();

    /**
     * 连接设备
     */
    void connect(String host, int port, Map<String, Object> params) throws Exception;

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 检查连接状态
     */
    boolean isConnected();

    /**
     * 读取设备数据
     */
    Map<String, Object> readData(String address, String dataType) throws Exception;

    /**
     * 写入设备数据
     */
    void writeData(String address, String dataType, Object value) throws Exception;

    /**
     * 读取设备状态
     */
    Map<String, Object> readDeviceStatus() throws Exception;

    /**
     * 批量读取数据
     */
    Map<String, Object> batchRead(Map<String, String> addressMap) throws Exception;

    /**
     * 设置告警阈值
     */
    void setAlarmThreshold(String address, String alarmType, double threshold) throws Exception;

    /**
     * 获取设备信息
     */
    Map<String, Object> getDeviceInfo() throws Exception;

    /**
     * 获取协议错误信息
     */
    String getLastError();

    /**
     * 获取连接超时时间（毫秒）
     */
    int getTimeout();

    /**
     * 设置连接超时时间（毫秒）
     */
    void setTimeout(int timeout);
}
