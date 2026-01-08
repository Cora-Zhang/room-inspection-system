package com.roominspection.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 告警WebSocket处理器
 * 用于实时推送告警信息
 */
@Slf4j
@Component
public class AlarmsWebSocketHandler extends TextWebSocketHandler {

    // 存储所有会话
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("告警WebSocket连接建立: sessionId={}, currentSessions={}", sessionId, sessions.size());

        // 发送欢迎消息
        Map<String, Object> welcomeMessage = Map.of(
                "type", "connected",
                "message", "告警WebSocket连接成功",
                "timestamp", System.currentTimeMillis()
        );
        sendMessage(session, welcomeMessage);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: sessionId={}, payload={}", session.getId(), payload);

        // 可以处理客户端发送的消息，比如订阅特定类型的告警
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("告警WebSocket连接关闭: sessionId={}, status={}, currentSessions={}",
                sessionId, status, sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("告警WebSocket传输错误: sessionId={}", session.getId(), exception);
        sessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 发送消息到指定会话
     */
    private void sendMessage(WebSocketSession session, Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 广播告警消息到所有会话
     */
    public void broadcastAlarm(Object alarmData) {
        Map<String, Object> message = Map.of(
                "type", "alarm",
                "data", alarmData,
                "timestamp", System.currentTimeMillis()
        );

        log.info("广播告警消息: alarmType={}, sessions={}",
                alarmData.getClass().getSimpleName(), sessions.size());

        sessions.forEach((sessionId, session) -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            } else {
                sessions.remove(sessionId);
            }
        });
    }

    /**
     * 广播告警统计
     */
    public void broadcastAlarmStatistics(Map<String, Object> statistics) {
        Map<String, Object> message = Map.of(
                "type", "alarmStatistics",
                "data", statistics,
                "timestamp", System.currentTimeMillis()
        );

        sessions.forEach((sessionId, session) -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            } else {
                sessions.remove(sessionId);
            }
        });
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }
}
