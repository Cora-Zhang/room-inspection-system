package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.DatabaseBackup;
import com.roominspection.backend.entity.HealthCheck;
import com.roominspection.backend.entity.ServiceInstance;
import com.roominspection.backend.service.DatabaseBackupService;
import com.roominspection.backend.service.HealthCheckService;
import com.roominspection.backend.service.LocalCacheService;
import com.roominspection.backend.service.SystemMetricsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 可用性管理Controller
 * 提供健康检查、系统监控、数据库备份、本地缓存等接口
 */
@Slf4j
@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    @Autowired
    private HealthCheckService healthCheckService;

    @Autowired
    private SystemMetricsService systemMetricsService;

    @Autowired
    private DatabaseBackupService databaseBackupService;

    @Autowired
    private LocalCacheService localCacheService;

    // ==================== 健康检查相关接口 ====================

    /**
     * 获取健康检查记录
     */
    @GetMapping("/health-checks")
    public Result<List<HealthCheck>> getHealthChecks(
            @RequestParam(defaultValue = "room-inspection-backend") String serviceName,
            @RequestParam(defaultValue = "24") Integer hours) {
        try {
            List<HealthCheck> healthChecks = healthCheckService.getHealthChecks(serviceName, hours);
            return Result.success(healthChecks);
        } catch (Exception e) {
            log.error("获取健康检查记录失败", e);
            return Result.error("获取健康检查记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取健康状态统计
     */
    @GetMapping("/health-stats")
    public Result<List<Map<String, Object>>> getHealthStats(
            @RequestParam(defaultValue = "room-inspection-backend") String serviceName,
            @RequestParam(defaultValue = "24") Integer hours) {
        try {
            List<Map<String, Object>> stats = healthCheckService.getHealthStats(serviceName, hours);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取健康状态统计失败", e);
            return Result.error("获取健康状态统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取健康的服务实例列表
     */
    @GetMapping("/healthy-instances")
    public Result<List<ServiceInstance>> getHealthyInstances(
            @RequestParam(defaultValue = "room-inspection-backend") String serviceName) {
        try {
            List<ServiceInstance> instances = healthCheckService.getHealthyInstances(serviceName);
            return Result.success(instances);
        } catch (Exception e) {
            log.error("获取健康服务实例失败", e);
            return Result.error("获取健康服务实例失败: " + e.getMessage());
        }
    }

    // ==================== 系统监控相关接口 ====================

    /**
     * 获取系统健康状态
     */
    @GetMapping("/system-health")
    public Result<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = systemMetricsService.getSystemHealth();
            return Result.success(health);
        } catch (Exception e) {
            log.error("获取系统健康状态失败", e);
            return Result.error("获取系统健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取监控指标统计
     */
    @GetMapping("/metrics-summary")
    public Result<Map<String, Object>> getMetricsSummary(
            @RequestParam(defaultValue = "room-inspection-backend") String serviceName,
            @RequestParam(defaultValue = "24") Integer hours) {
        try {
            Map<String, Object> summary = systemMetricsService.getMetricsSummary(serviceName, hours);
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取监控指标统计失败", e);
            return Result.error("获取监控指标统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取CPU使用率趋势
     */
    @GetMapping("/cpu-trend")
    public Result<List<Map<String, Object>>> getCpuTrend(
            @RequestParam(defaultValue = "room-inspection-backend") String serviceName,
            @RequestParam(defaultValue = "24") Integer hours) {
        try {
            List<Map<String, Object>> trend = systemMetricsService.getCpuTrend(serviceName, hours);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取CPU使用率趋势失败", e);
            return Result.error("获取CPU使用率趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取内存使用率趋势
     */
    @GetMapping("/memory-trend")
    public Result<List<Map<String, Object>>> getMemoryTrend(
            @RequestParam(defaultValue = "room-inspection-backend") String serviceName,
            @RequestParam(defaultValue = "24") Integer hours) {
        try {
            List<Map<String, Object>> trend = systemMetricsService.getMemoryTrend(serviceName, hours);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取内存使用率趋势失败", e);
            return Result.error("获取内存使用率趋势失败: " + e.getMessage());
        }
    }

    // ==================== 数据库备份相关接口 ====================

    /**
     * 手动执行数据库备份
     */
    @PostMapping("/backup")
    public Result<String> performBackup(
            @RequestParam(defaultValue = "FULL") String backupType) {
        try {
            String backupName = databaseBackupService.performBackup(backupType, false);
            return Result.success("备份成功: " + backupName);
        } catch (Exception e) {
            log.error("数据库备份失败", e);
            return Result.error("数据库备份失败: " + e.getMessage());
        }
    }

    /**
     * 获取备份记录列表
     */
    @GetMapping("/backups")
    public Result<List<DatabaseBackup>> getBackups(
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            List<DatabaseBackup> backups = databaseBackupService.getRecentBackups(limit);
            return Result.success(backups);
        } catch (Exception e) {
            log.error("获取备份记录失败", e);
            return Result.error("获取备份记录失败: " + e.getMessage());
        }
    }

    /**
     * 恢复数据库
     */
    @PostMapping("/restore")
    public Result<String> restoreDatabase(
            @RequestParam String backupFileName,
            @RequestParam(required = false) String description) {
        try {
            databaseBackupService.restoreDatabase(backupFileName, description);
            return Result.success("数据库恢复成功");
        } catch (Exception e) {
            log.error("数据库恢复失败", e);
            return Result.error("数据库恢复失败: " + e.getMessage());
        }
    }

    /**
     * 删除备份
     */
    @DeleteMapping("/backup/{backupId}")
    public Result<String> deleteBackup(@PathVariable Long backupId) {
        try {
            databaseBackupService.deleteBackup(backupId);
            return Result.success("删除备份成功");
        } catch (Exception e) {
            log.error("删除备份失败", e);
            return Result.error("删除备份失败: " + e.getMessage());
        }
    }

    // ==================== 本地缓存相关接口 ====================

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache-stats")
    public Result<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = localCacheService.getCacheStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return Result.error("获取缓存统计失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发缓存同步
     */
    @PostMapping("/cache/sync")
    public Result<String> triggerCacheSync() {
        try {
            localCacheService.triggerSync();
            return Result.success("缓存同步已触发");
        } catch (Exception e) {
            log.error("触发缓存同步失败", e);
            return Result.error("触发缓存同步失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定类型的缓存
     */
    @DeleteMapping("/cache/{cacheType}")
    public Result<String> clearCache(@PathVariable String cacheType) {
        try {
            localCacheService.clearCache(cacheType);
            return Result.success("清除缓存成功");
        } catch (Exception e) {
            log.error("清除缓存失败", e);
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }

    // ==================== 系统维护相关接口 ====================

    /**
     * 获取系统可用性概览
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        try {
            // 系统健康状态
            Map<String, Object> systemHealth = systemMetricsService.getSystemHealth();

            // 健康状态统计
            List<Map<String, Object>> healthStats = healthCheckService.getHealthStats("room-inspection-backend", 24);

            // 缓存统计
            Map<String, Object> cacheStats = localCacheService.getCacheStats();

            // 监控指标统计
            Map<String, Object> metricsSummary = systemMetricsService.getMetricsSummary("room-inspection-backend", 24);

            // 最近的备份记录
            List<DatabaseBackup> recentBackups = databaseBackupService.getRecentBackups(5);

            Map<String, Object> overview = new java.util.HashMap<>();
            overview.put("systemHealth", systemHealth);
            overview.put("healthStats", healthStats);
            overview.put("cacheStats", cacheStats);
            overview.put("metricsSummary", metricsSummary);
            overview.put("recentBackups", recentBackups);
            overview.put("timestamp", java.time.LocalDateTime.now().toString());

            return Result.success(overview);
        } catch (Exception e) {
            log.error("获取系统可用性概览失败", e);
            return Result.error("获取系统可用性概览失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查端点（用于负载均衡器探活）
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        try {
            Map<String, Object> health = systemMetricsService.getSystemHealth();
            String status = (String) health.get("status");

            if ("HEALTHY".equals(status)) {
                return Result.success(Map.of(
                        "status", "UP",
                        "timestamp", java.time.LocalDateTime.now().toString()
                ));
            } else {
                return Result.success(Map.of(
                        "status", "DEGRADED",
                        "message", "系统状态: " + status,
                        "timestamp", java.time.LocalDateTime.now().toString()
                ));
            }
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Result.success(Map.of(
                    "status", "DOWN",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * 就绪检查端点（用于K8s等容器编排平台）
     */
    @GetMapping("/ready")
    public Result<Map<String, Object>> ready() {
        try {
            // 检查数据库连接
            healthCheckService.checkDatabase();

            // 检查Redis连接
            healthCheckService.checkRedis();

            return Result.success(Map.of(
                    "status", "READY",
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            log.error("就绪检查失败", e);
            return Result.success(Map.of(
                    "status", "NOT_READY",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }
}
