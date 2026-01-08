package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.MonitorConfig;
import com.roominspection.backend.service.MonitorConfigService;
import com.roominspection.backend.service.ConcurrentMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 监控配置Controller
 */
@Slf4j
@RestController
@RequestMapping("/monitor/config")
@CrossOrigin(origins = "*")
public class MonitorConfigController {

    @Autowired
    private MonitorConfigService monitorConfigService;

    @Autowired
    private ConcurrentMonitorService concurrentMonitorService;

    /**
     * 获取所有监控配置
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('monitor:config:view')")
    public Result<List<MonitorConfig>> listConfigs() {
        try {
            List<MonitorConfig> configs = monitorConfigService.list();
            return Result.success(configs);
        } catch (Exception e) {
            log.error("获取监控配置列表失败", e);
            return Result.error("获取监控配置列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取监控配置
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('monitor:config:view')")
    public Result<MonitorConfig> getConfigById(@PathVariable Long id) {
        try {
            MonitorConfig config = monitorConfigService.getById(id);
            if (config == null) {
                return Result.error("配置不存在");
            }
            return Result.success(config);
        } catch (Exception e) {
            log.error("获取监控配置失败", e);
            return Result.error("获取监控配置失败: " + e.getMessage());
        }
    }

    /**
     * 创建监控配置
     */
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('monitor:config:create')")
    public Result<Boolean> createConfig(@RequestBody MonitorConfig config) {
        try {
            boolean success = monitorConfigService.save(config);
            return success ? Result.success(true) : Result.error("创建配置失败");
        } catch (Exception e) {
            log.error("创建监控配置失败", e);
            return Result.error("创建监控配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新监控配置
     */
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('monitor:config:update')")
    public Result<Boolean> updateConfig(@RequestBody MonitorConfig config) {
        try {
            boolean success = monitorConfigService.updateById(config);
            return success ? Result.success(true) : Result.error("更新配置失败");
        } catch (Exception e) {
            log.error("更新监控配置失败", e);
            return Result.error("更新监控配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除监控配置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('monitor:config:delete')")
    public Result<Boolean> deleteConfig(@PathVariable Long id) {
        try {
            boolean success = monitorConfigService.removeById(id);
            return success ? Result.success(true) : Result.error("删除配置失败");
        } catch (Exception e) {
            log.error("删除监控配置失败", e);
            return Result.error("删除监控配置失败: " + e.getMessage());
        }
    }

    /**
     * 设置采集频率
     */
    @PutMapping("/interval")
    @PreAuthorize("hasAuthority('monitor:config:update')")
    public Result<Boolean> setCollectionInterval(
            @RequestParam Long configId,
            @RequestParam Integer interval,
            @RequestParam Long updatedBy,
            @RequestParam(required = false) String updatedByName) {
        try {
            boolean success = monitorConfigService.setCollectionInterval(
                    configId, interval, updatedBy, updatedByName);
            return success ? Result.success(true) : Result.error("设置采集频率失败");
        } catch (Exception e) {
            log.error("设置采集频率失败", e);
            return Result.error("设置采集频率失败: " + e.getMessage());
        }
    }

    /**
     * 获取采集频率
     */
    @GetMapping("/interval/{deviceType}")
    @PreAuthorize("hasAuthority('monitor:config:view')")
    public Result<Integer> getCollectionInterval(@PathVariable String deviceType) {
        try {
            Integer interval = monitorConfigService.getCollectionInterval(deviceType);
            return Result.success(interval);
        } catch (Exception e) {
            log.error("获取采集频率失败", e);
            return Result.error("获取采集频率失败: " + e.getMessage());
        }
    }

    /**
     * 获取配置统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('monitor:config:view')")
    public Result<Map<String, Object>> getConfigStatistics() {
        try {
            Map<String, Object> statistics = monitorConfigService.getConfigStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取配置统计信息失败", e);
            return Result.error("获取配置统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 启动定时监控任务
     */
    @PostMapping("/tasks/start")
    @PreAuthorize("hasAuthority('monitor:task:control')")
    public Result<Boolean> startScheduledTasks() {
        try {
            concurrentMonitorService.startScheduledTasks();
            return Result.success(true);
        } catch (Exception e) {
            log.error("启动定时监控任务失败", e);
            return Result.error("启动定时监控任务失败: " + e.getMessage());
        }
    }

    /**
     * 停止定时监控任务
     */
    @PostMapping("/tasks/stop")
    @PreAuthorize("hasAuthority('monitor:task:control')")
    public Result<Boolean> stopScheduledTasks() {
        try {
            concurrentMonitorService.stopScheduledTasks();
            return Result.success(true);
        } catch (Exception e) {
            log.error("停止定时监控任务失败", e);
            return Result.error("停止定时监控任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取性能统计
     */
    @GetMapping("/performance")
    @PreAuthorize("hasAuthority('monitor:config:view')")
    public Result<Map<String, Object>> getPerformanceStatistics() {
        try {
            Map<String, Object> statistics = concurrentMonitorService.getPerformanceStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取性能统计失败", e);
            return Result.error("获取性能统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前运行任务数
     */
    @GetMapping("/running-count")
    @PreAuthorize("hasAuthority('monitor:config:view')")
    public Result<Integer> getRunningTaskCount() {
        try {
            int count = concurrentMonitorService.getRunningTaskCount();
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取运行任务数失败", e);
            return Result.error("获取运行任务数失败: " + e.getMessage());
        }
    }
}
