package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.PowerMetric;
import com.roominspection.backend.entity.PowerSystem;
import com.roominspection.backend.service.PowerSystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 电力监控控制器
 */
@Slf4j
@Api(tags = "电力系统监控")
@RestController
@RequestMapping("/api/power-monitoring")
public class PowerMonitoringController {

    @Autowired
    private PowerSystemService powerSystemService;

    @ApiOperation("分页查询电力设备")
    @GetMapping("/devices/page")
    public Result<Page<PowerSystem>> queryDevicesPage(
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<PowerSystem> page = powerSystemService.queryPage(deviceType, roomId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("获取电力设备统计")
    @GetMapping("/devices/statistics")
    public Result<Map<String, Object>> getDeviceStatistics() {
        Map<String, Object> stats = powerSystemService.getDeviceStatistics();
        return Result.success(stats);
    }

    @ApiOperation("通过SNMP采集设备数据")
    @PostMapping("/collect/snmp/{deviceId}")
    public Result<PowerMetric> collectBySnmp(@PathVariable Long deviceId) {
        PowerMetric metric = powerSystemService.collectBySnmp(deviceId);
        return Result.success(metric);
    }

    @ApiOperation("通过Modbus采集设备数据")
    @PostMapping("/collect/modbus/{deviceId}")
    public Result<PowerMetric> collectByModbus(@PathVariable Long deviceId) {
        PowerMetric metric = powerSystemService.collectByModbus(deviceId);
        return Result.success(metric);
    }

    @ApiOperation("批量采集所有设备数据")
    @PostMapping("/collect/batch")
    public Result<Map<String, Object>> batchCollectAll() {
        Map<String, Object> result = powerSystemService.batchCollectAll();
        return Result.success(result);
    }

    @ApiOperation("获取设备最新指标")
    @GetMapping("/metric/latest/{deviceId}")
    public Result<PowerMetric> getLatestMetric(@PathVariable Long deviceId) {
        PowerMetric metric = powerSystemService.getLatestMetric(deviceId);
        return Result.success(metric);
    }

    @ApiOperation("查询设备历史指标")
    @GetMapping("/metric/history/{deviceId}")
    public Result<Page<PowerMetric>> queryMetrics(
            @PathVariable Long deviceId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<PowerMetric> page = powerSystemService.queryMetrics(deviceId, startTime, endTime, pageNum, pageSize);
        return Result.success(page);
    }
}
