package com.roominspection.backend.accesscontrol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 海康威视门禁系统适配器
 * 支持海康威视iVMS-4200、DS-K系列门禁控制器等设备
 * 使用REST API进行通信
 */
@Slf4j
public class HikvisionDoorAccessSystem implements DoorAccessSystem {

    private static final String PROTOCOL_NAME = "Hikvision Door Access";
    private static final String PROTOCOL_VERSION = "2.0";

    private CloseableHttpClient httpClient;
    private String baseUrl;
    private String username;
    private String password;
    private String sessionToken;
    private int timeout = 5000; // 默认超时5秒
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

            httpClient = HttpClients.createDefault();

            // 登录获取session token
            login();

            log.info("海康威视门禁系统连接成功: {}:{}", host, port);
        } catch (Exception e) {
            lastError = e.getMessage();
            log.error("海康威视门禁系统连接失败", e);
            throw new Exception("海康威视门禁系统连接失败: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        try {
            if (sessionToken != null) {
                logout();
            }
            if (httpClient != null) {
                httpClient.close();
            }
            log.info("海康威视门禁系统已断开连接");
        } catch (Exception e) {
            log.error("断开海康威视门禁系统连接失败", e);
        }
    }

    @Override
    public boolean isConnected() {
        return httpClient != null && sessionToken != null;
    }

    /**
     * 登录系统
     */
    private void login() throws Exception {
        String url = baseUrl + "/artemis/api/v1/login";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userName", username);
        requestBody.put("password", password);

        String responseBody = doPost(url, requestBody);
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

        if (response.containsKey("token")) {
            sessionToken = response.get("token").toString();
        } else {
            throw new Exception("登录失败：未获取到token");
        }
    }

    /**
     * 登出系统
     */
    private void logout() throws Exception {
        String url = baseUrl + "/artemis/api/v1/logout";
        doGet(url);
        sessionToken = null;
    }

    @Override
    public boolean authenticate(String userId, String password) throws Exception {
        if (!isConnected()) {
            throw new Exception("系统未连接");
        }

        try {
            String url = baseUrl + "/artemis/api/v1/user/authenticate";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("password", password);

            String responseBody = doPost(url, requestBody);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            return response.containsKey("success") && (Boolean) response.get("success");
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
            String url = baseUrl + "/artemis/api/v1/door/open";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("doorId", doorId);
            requestBody.put("userId", userId);

            String responseBody = doPost(url, requestBody);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            boolean success = response.containsKey("success") && (Boolean) response.get("success");

            if (success) {
                // 触发开门事件
                notifyEventListeners(DoorAccessEventListener.EventType.DOOR_OPEN,
                        doorId, userId, null, System.currentTimeMillis(), null);
            }

            return success;
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
            String url = baseUrl + "/artemis/api/v1/door/close";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("doorId", doorId);
            requestBody.put("userId", userId);

            String responseBody = doPost(url, requestBody);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            boolean success = response.containsKey("success") && (Boolean) response.get("success");

            if (success) {
                // 触发关门事件
                notifyEventListeners(DoorAccessEventListener.EventType.DOOR_CLOSE,
                        doorId, userId, null, System.currentTimeMillis(), null);
            }

            return success;
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
            String url = baseUrl + "/artemis/api/v1/permission/grant";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("doorIds", doorIds);
            requestBody.put("schedule", schedule);

            String responseBody = doPost(url, requestBody);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            return response.containsKey("success") && (Boolean) response.get("success");
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
            String url = baseUrl + "/artemis/api/v1/permission/revoke";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", userId);
            requestBody.put("doorIds", doorIds);

            String responseBody = doPost(url, requestBody);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            return response.containsKey("success") && (Boolean) response.get("success");
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
            String url = baseUrl + "/artemis/api/v1/user/permissions?userId=" + userId;
            String responseBody = doGet(url);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            if (response.containsKey("permissions")) {
                return (List<Map<String, Object>>) response.get("permissions");
            } else {
                return new ArrayList<>();
            }
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
            String url = baseUrl + "/artemis/api/v1/access/logs";
            if (doorId != null) {
                url += "?doorId=" + doorId;
                if (startTime != null && endTime != null) {
                    url += "&startTime=" + startTime + "&endTime=" + endTime;
                }
            }

            String responseBody = doGet(url);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            if (response.containsKey("logs")) {
                return (List<Map<String, Object>>) response.get("logs");
            } else {
                return new ArrayList<>();
            }
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
            String url = baseUrl + "/artemis/api/v1/door/status?doorId=" + doorId;
            String responseBody = doGet(url);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            if (response.containsKey("status")) {
                return (Map<String, Object>) response.get("status");
            } else {
                return new HashMap<>();
            }
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
            String url = baseUrl + "/artemis/api/v1/system/info";
            String responseBody = doGet(url);
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            response.put("manufacturer", "Hikvision");
            return response;
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

    /**
     * 执行GET请求
     */
    private String doGet(String url) throws Exception {
        HttpGet request = new HttpGet(url);
        request.addHeader("Cookie", "session=" + sessionToken);
        request.addHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * 执行POST请求
     */
    private String doPost(String url, Map<String, Object> body) throws Exception {
        HttpPost request = new HttpPost(url);
        request.addHeader("Cookie", "session=" + sessionToken);
        request.addHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(objectMapper.writeValueAsString(body)));

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * 通知所有事件监听器
     */
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
