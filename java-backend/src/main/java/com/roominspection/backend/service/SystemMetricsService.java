package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.SystemMetrics;
import com.roominspection.backend.mapper.SystemMetricsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统监控指标服务
 */
@Slf4j
@Service
public class SystemMetricsService extends ServiceImpl<SystemMetricsMapper, SystemMetrics> {

    /**
     * 获取最近的监控指标
     */
    public List<SystemMetrics> getRecentMetrics(String serviceName, Integer hours) {
        return baseMapper.selectRecentMetrics(serviceName, hours);
    }

    /**
     * 获取系统健康状态
     */
    public Map<String, Object> getSystemHealth() {
        try {
            List<SystemMetrics> recentMetrics = getRecentMetrics("room-inspection-backend", 1);

            if (recentMetrics.isEmpty()) {
                return Map.of(
                        "status", "UNKNOWN",
                        "message", "暂无监控数据"
                );
            }

            // 获取最新的指标
            SystemMetrics latestMetrics = recentMetrics.get(recentMetrics.size() - 1);

            // 判断系统健康状态
            String healthStatus = determineHealthStatus(latestMetrics);

            return Map.of(
                    "status", healthStatus,
                    "cpuUsage", latestMetrics.getCpuUsage(),
                    "memoryUsage", latestMetrics.getMemoryUsage(),
                    "diskUsage", latestMetrics.getDiskUsage(),
                    "jvmHeapUsage", latestMetrics.getJvmHeapUsage(),
                    "threadCount", latestMetrics.getThreadCount(),
                    "requestCount", latestMetrics.getRequestCount(),
                    "errorCount", latestMetrics.getErrorCount(),
                    "avgResponseTime", latestMetrics.getAvgResponseTime(),
                    "metricsTime", latestMetrics.getMetricsTime().toString()
            );
        } catch (Exception e) {
            log.error("获取系统健康状态失败", e);
            return Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            );
        }
    }

    /**
     * 判断系统健康状态
     */
    private String determineHealthStatus(SystemMetrics metrics) {
        // CPU使用率超过90% -> DEGRADED
        if (metrics.getCpuUsage() > 90) {
            return "DEGRADED";
        }

        // 内存使用率超过90% -> DEGRADED
        if (metrics.getMemoryUsage() > 90) {
            return "DEGRADED";
        }

        // JVM堆内存使用率超过90% -> DEGRADED
        if (metrics.getJvmHeapUsage() > 90) {
            return "DEGRADED";
        }

        // 磁盘使用率超过90% -> DEGRADED
        if (metrics.getDiskUsage() > 90) {
            return "DEGRADED";
        }

        // 错误数超过阈值 -> UNHEALTHY
        if (metrics.getErrorCount() > 100) {
            return "UNHEALTHY";
        }

        // 平均响应时间超过3秒 -> DEGRADED
        if (metrics.getAvgResponseTime() > 3000) {
            return "DEGRADED";
        }

        // 默认健康
        return "HEALTHY";
    }

    /**
     * 获取监控指标统计
     */
    public Map<String, Object> getMetricsSummary(String serviceName, Integer hours) {
        try {
            List<SystemMetrics> metricsList = getRecentMetrics(serviceName, hours);

            if (metricsList.isEmpty()) {
                return Map.of("message", "暂无监控数据");
            }

            // 计算平均值
            double avgCpuUsage = metricsList.stream()
                    .mapToDouble(SystemMetrics::getCpuUsage)
                    .average()
                    .orElse(0);

            double avgMemoryUsage = metricsList.stream()
                    .mapToDouble(SystemMetrics::getMemoryUsage)
                    .average()
                    .orElse(0);

            double avgDiskUsage = metricsList.stream()
                    .mapToDouble(SystemMetrics::getDiskUsage)
                    .average()
                    .orElse(0);

            double avgJvmHeapUsage = metricsList.stream()
                    .mapToDouble(SystemMetrics::getJvmHeapUsage)
                    .average()
                    .orElse(0);

            int avgThreadCount = (int) metricsList.stream()
                    .mapToInt(SystemMetrics::getThreadCount)
                    .average()
                    .orElse(0);

            long totalRequestCount = metricsList.stream()
                    .mapToLong(SystemMetrics::getRequestCount)
                    .sum();

            long totalErrorCount = metricsList.stream()
                    .mapToLong(SystemMetrics::getErrorCount)
                    .sum();

            double avgResponseTime = metricsList.stream()
                    .mapToDouble(SystemMetrics::getAvgResponseTime)
                    .average()
                    .orElse(0);

            return Map.of(
                    "serviceName", serviceName,
                    "timeRange", hours + "小时",
                    "avgCpuUsage", String.format("%.2f%%", avgCpuUsage),
                    "avgMemoryUsage", String.format("%.2f%%", avgMemoryUsage),
                    "avgDiskUsage", String.format("%.2f%%", avgDiskUsage),
                    "avgJvmHeapUsage", String.format("%.2f%%", avgJvmHeapUsage),
                    "avgThreadCount", avgThreadCount,
                    "totalRequestCount", totalRequestCount,
                    "totalErrorCount", totalErrorCount,
                    "avgResponseTime", String.format("%.2fms", avgResponseTime)
            );
        } catch (Exception e) {
            log.error("获取监控指标统计失败", e);
            return Map.of("message", e.getMessage());
        }
    }

    /**
     * 获取CPU使用率趋势
     */
    public List<Map<String, Object>> getCpuTrend(String serviceName, Integer hours) {
        List<SystemMetrics> metricsList = getRecentMetrics(serviceName, hours);

        return metricsList.stream()
                .map(m -> Map.of(
                        "time", m.getMetricsTime().toString(),
                        "cpuUsage", m.getCpuUsage()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 获取内存使用率趋势
     */
    public List<Map<String, Object>> getMemoryTrend(String serviceName, Integer hours) {
        List<SystemMetrics> metricsList = getRecentMetrics(serviceName, hours);

        return metricsList.stream()
                .map(m -> Map.of(
                        "time", m.getMetricsTime().toString(),
                        "memoryUsage", m.getMemoryUsage(),
                        "jvmHeapUsage", m.getJvmHeapUsage()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 清理过期的监控指标
     */
    public void cleanupExpiredMetrics() {
        try {
            // 删除7天前的指标数据
            LocalDateTime expireTime = LocalDateTime.now().minusDays(7);
            int deletedCount = baseMapper.deleteExpiredMetrics(expireTime);

            log.info("清理过期监控指标完成，删除数量: {}", deletedCount);
        } catch (Exception e) {
            log.error("清理过期监控指标失败", e);
        }
    }
}
