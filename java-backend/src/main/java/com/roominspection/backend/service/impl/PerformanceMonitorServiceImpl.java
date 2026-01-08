package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.PerformanceMetric;
import com.roominspection.backend.mapper.PerformanceMetricMapper;
import com.roominspection.backend.service.PerformanceMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 性能监控服务实现
 */
@Slf4j
@Service
public class PerformanceMonitorServiceImpl extends ServiceImpl<PerformanceMetricMapper, PerformanceMetric>
        implements PerformanceMonitorService {

    @Autowired
    private PerformanceMetricMapper performanceMetricMapper;

    /**
     * 记录性能指标
     */
    @Override
    public void recordMetric(String metricName, String metricType, Double metricValue,
                            String unit, String objectId, String objectType) {
        PerformanceMetric metric = new PerformanceMetric();
        metric.setMetricName(metricName);
        metric.setMetricType(metricType);
        metric.setMetricValue(metricValue);
        metric.setUnit(unit);
        metric.setObjectId(objectId);
        metric.setObjectType(objectType);
        metric.setRecordTime(LocalDateTime.now());
        metric.setCreatedAt(LocalDateTime.now());

        // 判断是否超出阈值
        checkThreshold(metric);

        save(metric);

        log.debug("记录性能指标: metricName={}, metricValue={}, unit={}",
                metricName, metricValue, unit);
    }

    /**
     * 记录API响应时间
     */
    @Override
    public void recordApiResponse(String apiPath, Long duration, String extraInfo) {
        recordMetric("API响应时间", "API_RESPONSE", duration.doubleValue(), "ms", apiPath, "API");

        // 如果响应时间超过阈值，记录告警
        if (duration > 2000) {
            log.warn("API响应时间过长: apiPath={}, duration={}ms", apiPath, duration);
        }
    }

    /**
     * 记录数据库查询时间
     */
    @Override
    public void recordDbQuery(String sqlStatement, Long duration, String extraInfo) {
        String objectId = sqlStatement.length() > 100 ? sqlStatement.substring(0, 100) + "..." : sqlStatement;
        recordMetric("数据库查询时间", "DB_QUERY", duration.doubleValue(), "ms", objectId, "SQL");

        // 如果查询时间超过阈值，记录告警
        if (duration > 1000) {
            log.warn("数据库查询时间过长: sql={}, duration={}ms", objectId, duration);
        }
    }

    /**
     * 记录监控数据采集时间
     */
    @Override
    public void recordMonitorCollect(String deviceId, Long duration, Integer dataCount) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("dataCount", dataCount);

        recordMetric("监控数据采集", "MONITOR_COLLECT", duration.doubleValue(), "ms", deviceId, "DEVICE");

        // 如果采集时间超过阈值，记录告警
        if (duration > 5000) {
            log.warn("监控数据采集时间过长: deviceId={}, duration={}ms", deviceId, duration);
        }
    }

    /**
     * 记录工单处理时间
     */
    @Override
    public void recordWorkOrderProcess(String workOrderId, String action, Long duration) {
        recordMetric("工单处理-" + action, "WORK_ORDER_PROCESS", duration.doubleValue(), "ms", workOrderId, "WORK_ORDER");

        // 如果处理时间超过阈值，记录告警
        if (duration > 3000) {
            log.warn("工单处理时间过长: workOrderId={}, action={}, duration={}ms",
                    workOrderId, action, duration);
        }
    }

    /**
     * 记录缓存命中率
     */
    @Override
    public void recordCacheHit(String cacheName, Double hitRate) {
        recordMetric("缓存命中率-" + cacheName, "CACHE_HIT", hitRate, "%", cacheName, "CACHE");

        // 如果缓存命中率过低，记录告警
        if (hitRate < 50) {
            log.warn("缓存命中率过低: cacheName={}, hitRate={}%", cacheName, hitRate);
        }
    }

    /**
     * 检查阈值
     */
    private void checkThreshold(PerformanceMetric metric) {
        Double threshold = null;

        switch (metric.getMetricType()) {
            case "API_RESPONSE":
                threshold = 2000.0; // 2秒
                break;
            case "DB_QUERY":
                threshold = 1000.0; // 1秒
                break;
            case "MONITOR_COLLECT":
                threshold = 5000.0; // 5秒
                break;
            case "WORK_ORDER_PROCESS":
                threshold = 3000.0; // 3秒
                break;
            case "CACHE_HIT":
                threshold = 50.0; // 50%
                break;
        }

        if (threshold != null) {
            boolean exceeded = false;
            if (metric.getMetricValue() != null) {
                if ("CACHE_HIT".equals(metric.getMetricType())) {
                    // 缓存命中率低于阈值为异常
                    exceeded = metric.getMetricValue() < threshold;
                } else {
                    // 其他指标高于阈值为异常
                    exceeded = metric.getMetricValue() > threshold;
                }
            }

            metric.setThresholdUpper(threshold);
            metric.setExceededThreshold(exceeded ? 1 : 0);
        }
    }

    /**
     * 获取性能统计
     */
    @Override
    public Map<String, Object> getPerformanceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<PerformanceMetric> metrics = performanceMetricMapper.selectList(
                new QueryWrapper<PerformanceMetric>()
                        .between("record_time", startTime, endTime)
        );

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalRecords", metrics.size());

        // 按类型分组统计
        Map<String, List<PerformanceMetric>> byType = metrics.stream()
                .collect(Collectors.groupingBy(PerformanceMetric::getMetricType));

        Map<String, Object> byTypeStats = new HashMap<>();
        for (Map.Entry<String, List<PerformanceMetric>> entry : byType.entrySet()) {
            List<PerformanceMetric> typeMetrics = entry.getValue();
            double avgValue = typeMetrics.stream()
                    .filter(m -> m.getMetricValue() != null)
                    .mapToDouble(PerformanceMetric::getMetricValue)
                    .average()
                    .orElse(0);

            double maxValue = typeMetrics.stream()
                    .filter(m -> m.getMetricValue() != null)
                    .mapToDouble(PerformanceMetric::getMetricValue)
                    .max()
                    .orElse(0);

            double minValue = typeMetrics.stream()
                    .filter(m -> m.getMetricValue() != null)
                    .mapToDouble(PerformanceMetric::getMetricValue)
                    .min()
                    .orElse(0);

            long exceededCount = typeMetrics.stream()
                    .filter(m -> m.getExceededThreshold() != null && m.getExceededThreshold() == 1)
                    .count();

            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("count", typeMetrics.size());
            typeStat.put("avgValue", avgValue);
            typeStat.put("maxValue", maxValue);
            typeStat.put("minValue", minValue);
            typeStat.put("exceededCount", exceededCount);

            byTypeStats.put(entry.getKey(), typeStat);
        }

        statistics.put("byType", byTypeStats);

        return statistics;
    }

    /**
     * 获取指定类型的指标
     */
    @Override
    public List<PerformanceMetric> getMetricsByType(String metricType, LocalDateTime startTime, LocalDateTime endTime) {
        return performanceMetricMapper.selectList(
                new QueryWrapper<PerformanceMetric>()
                        .eq("metric_type", metricType)
                        .between("record_time", startTime, endTime)
                        .orderByDesc("record_time")
        );
    }

    /**
     * 获取慢查询列表
     */
    @Override
    public List<Map<String, Object>> getSlowQueries(Long threshold, LocalDateTime startTime, LocalDateTime endTime) {
        List<PerformanceMetric> metrics = performanceMetricMapper.selectList(
                new QueryWrapper<PerformanceMetric>()
                        .eq("metric_type", "DB_QUERY")
                        .gt("metric_value", threshold)
                        .between("record_time", startTime, endTime)
                        .orderByDesc("metric_value")
                        .last("LIMIT 50")
        );

        return metrics.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("objectId", m.getObjectId());
                    map.put("duration", m.getMetricValue());
                    map.put("recordTime", m.getRecordTime());
                    map.put("extraInfo", m.getExtraInfo());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 清理历史性能数据
     */
    @Override
    public int cleanHistoryMetrics(int days) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        int count = performanceMetricMapper.delete(
                new QueryWrapper<PerformanceMetric>()
                        .lt("record_time", beforeTime)
        );
        log.info("清理历史性能数据: days={}, count={}", days, count);
        return count;
    }
}
