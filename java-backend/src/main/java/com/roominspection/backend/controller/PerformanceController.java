package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.service.PerformanceMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 性能监控Controller
 */
@Slf4j
@RestController
@RequestMapping("/performance")
@CrossOrigin(origins = "*")
public class PerformanceController {

    @Autowired
    private PerformanceMonitorService performanceMonitorService;

    /**
     * 获取性能统计
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('system:monitor:view')")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Map<String, Object> statistics = performanceMonitorService.getPerformanceStatistics(startTime, endTime);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取性能统计失败", e);
            return Result.error("获取性能统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定类型的指标
     */
    @GetMapping("/metrics/{metricType}")
    @PreAuthorize("hasAuthority('system:monitor:view')")
    public Result<List<Map<String, Object>>> getMetricsByType(
            @PathVariable String metricType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            var metrics = performanceMonitorService.getMetricsByType(metricType, startTime, endTime);
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("获取指标失败", e);
            return Result.error("获取指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取慢查询列表
     */
    @GetMapping("/slow-queries")
    @PreAuthorize("hasAuthority('system:monitor:view')")
    public Result<List<Map<String, Object>>> getSlowQueries(
            @RequestParam(defaultValue = "1000") Long threshold,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            List<Map<String, Object>> slowQueries = performanceMonitorService.getSlowQueries(
                    threshold, startTime, endTime);
            return Result.success(slowQueries);
        } catch (Exception e) {
            log.error("获取慢查询列表失败", e);
            return Result.error("获取慢查询列表失败: " + e.getMessage());
        }
    }

    /**
     * 清理历史性能数据
     */
    @DeleteMapping("/clean")
    @PreAuthorize("hasAuthority('system:monitor:delete')")
    public Result<Integer> cleanHistoryMetrics(@RequestParam(defaultValue = "30") int days) {
        try {
            int count = performanceMonitorService.cleanHistoryMetrics(days);
            return Result.success(count);
        } catch (Exception e) {
            log.error("清理历史性能数据失败", e);
            return Result.error("清理历史性能数据失败: " + e.getMessage());
        }
    }

    /**
     * 手动记录性能指标
     */
    @PostMapping("/record")
    @PreAuthorize("hasAuthority('system:monitor:view')")
    public Result<Boolean> recordMetric(
            @RequestParam String metricName,
            @RequestParam String metricType,
            @RequestParam Double metricValue,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String objectId,
            @RequestParam(required = false) String objectType) {
        try {
            performanceMonitorService.recordMetric(metricName, metricType, metricValue,
                    unit, objectId, objectType);
            return Result.success(true);
        } catch (Exception e) {
            log.error("记录性能指标失败", e);
            return Result.error("记录性能指标失败: " + e.getMessage());
        }
    }
}
