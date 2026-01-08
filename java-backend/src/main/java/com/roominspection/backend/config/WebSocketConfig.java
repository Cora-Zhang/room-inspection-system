package com.roominspection.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(alarmsWebSocketHandler(), "/ws/alarms")
                .setAllowedOrigins("*");
        registry.addHandler(monitorDataWebSocketHandler(), "/ws/monitor")
                .setAllowedOrigins("*");
    }

    @Bean
    public AlarmsWebSocketHandler alarmsWebSocketHandler() {
        return new AlarmsWebSocketHandler();
    }

    @Bean
    public MonitorDataWebSocketHandler monitorDataWebSocketHandler() {
        return new MonitorDataWebSocketHandler();
    }
}
