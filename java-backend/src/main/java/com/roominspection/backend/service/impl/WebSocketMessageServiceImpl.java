package com.roominspection.backend.service.impl;

import com.roominspection.backend.config.AlarmsWebSocketHandler;
import com.roominspection.backend.config.MonitorDataWebSocketHandler;
import com.roominspection.backend.service.WebSocketMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * WebSocket消息推送服务实现
 */
@Slf4j
@Service
public class WebSocketMessageServiceImpl implements WebSocketMessageService {

    @Autowired
    private AlarmsWebSocketHandler alarmsWebSocketHandler;

    @Autowired
    private MonitorDataWebSocketHandler monitorDataWebSocketHandler;

    /**
     * 推送告警消息
     */
    @Override
    public void pushAlarm(Object alarmData) {
        try {
            if (alarmsWebSocketHandler.getConnectionCount() > 0) {
                alarmsWebSocketHandler.broadcastAlarm(alarmData);
                log.debug("推送告警消息成功");
            }
        } catch (Exception e) {
            log.error("推送告警消息失败", e);
        }
    }

    /**
     * 推送告警统计
     */
    @Override
    public void pushAlarmStatistics(Map<String, Object> statistics) {
        try {
            if (alarmsWebSocketHandler.getConnectionCount() > 0) {
                alarmsWebSocketHandler.broadcastAlarmStatistics(statistics);
                log.debug("推送告警统计成功");
            }
        } catch (Exception e) {
            log.error("推送告警统计失败", e);
        }
    }

    /**
     * 推送设备指标数据
     */
    @Override
    public void pushDeviceMetrics(Object metricsData) {
        try {
            if (monitorDataWebSocketHandler.getConnectionCount() > 0) {
                monitorDataWebSocketHandler.broadcastDeviceMetrics(metricsData);
                log.debug("推送设备指标数据成功");
            }
        } catch (Exception e) {
            log.error("推送设备指标数据失败", e);
        }
    }

    /**
     * 推送采集任务状态
     */
    @Override
    public void pushTaskStatus(Object taskData) {
        try {
            if (monitorDataWebSocketHandler.getConnectionCount() > 0) {
                monitorDataWebSocketHandler.broadcastTaskStatus(taskData);
                log.debug("推送采集任务状态成功");
            }
        } catch (Exception e) {
            log.error("推送采集任务状态失败", e);
        }
    }

    /**
     * 推送性能统计
     */
    @Override
    public void pushPerformanceStatistics(Map<String, Object> statistics) {
        try {
            if (monitorDataWebSocketHandler.getConnectionCount() > 0) {
                monitorDataWebSocketHandler.broadcastPerformanceStatistics(statistics);
                log.debug("推送性能统计成功");
            }
        } catch (Exception e) {
            log.error("推送性能统计失败", e);
        }
    }

    /**
     * 获取告警WebSocket连接数
     */
    @Override
    public int getAlarmConnectionCount() {
        return alarmsWebSocketHandler.getConnectionCount();
    }

    /**
     * 获取监控数据WebSocket连接数
     */
    @Override
    public int getMonitorConnectionCount() {
        return monitorDataWebSocketHandler.getConnectionCount();
    }
}
