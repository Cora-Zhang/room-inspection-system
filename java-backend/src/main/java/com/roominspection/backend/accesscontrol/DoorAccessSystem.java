package com.roominspection.backend.accesscontrol;

import java.util.List;
import java.util.Map;

/**
 * 门禁系统接口
 * 定义了所有门禁系统必须实现的方法
 */
public interface DoorAccessSystem {

    /**
     * 系统名称
     */
    String getSystemName();

    /**
     * 系统版本
     */
    String getSystemVersion();

    /**
     * 连接门禁系统
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
     * 用户认证
     */
    boolean authenticate(String userId, String password) throws Exception;

    /**
     * 开门
     */
    boolean openDoor(String doorId, String userId) throws Exception;

    /**
     * 关门
     */
    boolean closeDoor(String doorId, String userId) throws Exception;

    /**
     * 下发门禁权限
     */
    boolean grantPermission(String userId, List<String> doorIds, Map<String, Object> schedule) throws Exception;

    /**
     * 撤销门禁权限
     */
    boolean revokePermission(String userId, List<String> doorIds) throws Exception;

    /**
     * 获取用户权限
     */
    List<Map<String, Object>> getUserPermissions(String userId) throws Exception;

    /**
     * 获取门禁日志
     */
    List<Map<String, Object>> getAccessLogs(String doorId, String startTime, String endTime) throws Exception;

    /**
     * 实时监听门禁事件
     */
    void registerEventListener(DoorAccessEventListener listener);

    /**
     * 获取门禁状态
     */
    Map<String, Object> getDoorStatus(String doorId) throws Exception;

    /**
     * 获取系统信息
     */
    Map<String, Object> getSystemInfo() throws Exception;

    /**
     * 获取错误信息
     */
    String getLastError();

    /**
     * 设置连接超时时间（毫秒）
     */
    void setTimeout(int timeout);

    /**
     * 获取连接超时时间（毫秒）
     */
    int getTimeout();
}
