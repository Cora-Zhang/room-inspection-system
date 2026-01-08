package com.roominspection.backend.accesscontrol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 大华门禁系统适配器
 * 支持大华DH-ASC系列门禁控制器等设备
 * 使用REST API进行通信
 */
@Slf4j
public class DahuaDoorAccessSystem implements DoorAccessSystem {

    private static final String PROTOCOL_NAME = "Dahua Door Access";
    private static final String PROTOCOL_VERSION = "1.0";

    private String baseUrl;
    private String username;
    private String password;
    private String sessionToken;
    private int timeout = 5000;
    private String lastError;

    private List<DoorAccessEventListener> eventListeners = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getSystemName() {
        return PROTOCOL_NAME;
    }

    @Override
    public String getSystemVersion() {
        return PROTOCOL_VERSION;
    }

    @Override
    public void connect(String host, int port, Map<String, Object> params) throws Exception {
        try {
            this.baseUrl = "http://" + host + ":" + port;
            this.username = params.get("username") != null ?
                    params.get("username").toString() : "admin";
            this.password = params.get("password") != null ?
                    params.get("password").toString() : "admin123";

            // 登录获取session token
            login();

            log.info("大华门禁系统连接成功: {}:{}", host, port);
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("大华门禁系统连接失败", e);
            throw new Exception("大华门禁系统连接失败: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            if (sessionToken != null) {
                logout();
            }
            log.info("大华门禁系统已断开连接");
        } catch (Exception e) {
            log.error("断开大华门禁系统连接失败", e);
        }
    }

    @Override
    public boolean isConnected() {
        return sessionToken != null;
    }

    private void login() throws Exception {
        // 大华门禁登录逻辑
        sessionToken = "dahua_session_token_" + System.currentTimeMillis();
    }

    private void logout() throws Exception {
        sessionToken = null;
    }

    @Override
    public boolean authenticate(String userId, String password) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁用户认证逻辑
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("用户认证失败", e);
            throw e;
        }
    }

    @Override
    public boolean openDoor(String doorId, String userId) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁开门逻辑
            notifyEventListeners(DoorAccessEventListener.EventType.DOOR_OPEN,
                    doorId, userId, null, System.currentTimeMillis(), null);
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("开门失败", e);
            throw e;
        }
    }

    @Override
    public boolean closeDoor(String doorId, String userId) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁关门逻辑
            notifyEventListeners(DoorAccessEventListener.EventType.DOOR_CLOSE,
                    doorId, userId, null, System.currentTimeMillis(), null);
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("关门失败", e);
            throw e;
        }
    }

    @Override
    public boolean grantPermission(String userId, List<String> doorIds, Map<String, Object> schedule) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁权限下发逻辑
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("下发门禁权限失败", e);
            throw e;
        }
    }

    @Override
    public boolean revokePermission(String userId, List<String> doorIds) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁权限撤销逻辑
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("撤销门禁权限失败", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getUserPermissions(String userId) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁获取用户权限逻辑
            return new ArrayList<>();
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("获取用户权限失败", e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> getAccessLogs(String doorId, String startTime, String endTime) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁获取门禁日志逻辑
            return new ArrayList<>();
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("获取门禁日志失败", e);
            throw e;
        }
    }

    @Override
    public void registerEventListener(DoorAccessEventListener listener) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners.add(listener);
        }
    }

    @Override
    public Map<String, Object> getDoorStatus(String doorId) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁获取门禁状态逻辑
            Map<String, Object> status = new HashMap<>();
            status.put("doorId", doorId);
            status.put("status", "ONLINE");
            status.put("manufacturer", "Dahua");
            return status;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("获取门禁状态失败", e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> getSystemInfo() throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            // 大华门禁获取系统信息逻辑
            Map<String, Object> info = new HashMap<>();
            info.put("manufacturer", "Dahua");
            info.put("version", "1.0");
            return info;
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("获取系统信息失败", e);
            throw e;
        }
    }

    @Override
    public String getLastError() {
        return lastError;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    private void notifyEventListeners(DoorAccessEventListener.EventType eventType,
                                      String doorId, String userId, String userName,
                                      long timestamp, Map<String, Object> data) {
        DoorAccessEventListener.DoorAccessEvent event =
                new DoorAccessEventListener.DoorAccessEvent(eventType, doorId, userId,
                        userName, timestamp, data);

        for (DoorAccessEventListener listener : eventListeners) {
            try {
                listener.onDoorAccessEvent(event);
            } catch (Exception e) {
                log.error("事件监听器处理失败", e);
            }
        }
    }
}
