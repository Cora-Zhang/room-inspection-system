package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.Device;
import com.roominspection.backend.entity.DeviceInspectionRecord;
import com.roominspection.backend.entity.DeviceMetric;
import com.roominspection.backend.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 设备巡检Controller
 */
@Slf4j
@Api(tags = "设备巡检管理")
@RestController
@RequestMapping("/api/device-inspection")
public class DeviceInspectionController {

    @Autowired
    private DeviceAssetService deviceAssetService;

    @Autowired
    private DeviceInspectionService deviceInspectionService;

    @Autowired
    private SnmpMonitorService snmpMonitorService;

    @Autowired
    private AlertRuleService alertRuleService;

    @Autowired
    private DeviceMetricService deviceMetricService;

    // ============================================
    // 设备资产管理
    // ============================================

    /**
     * 分页查询设备
     */
    @ApiOperation("分页查询设备")
    @GetMapping("/devices/page")
    public Result<IPage<Device>> queryDevicePage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam("机房ID") @RequestParam(required = false) String roomId,
            @ApiParam("设备类型") @RequestParam(required = false) String type,
            @ApiParam("设备子类型") @RequestParam(required = false) String subType,
            @ApiParam("设备状态") @RequestParam(required = false) String status,
            @ApiParam("设备名称") @RequestParam(required = false) String deviceName,
            @ApiParam("IP地址") @RequestParam(required = false) String ipAddress) {
        try {
            Page<Device> page = new Page<>(current, size);
            IPage<Device> result = deviceAssetService.queryDevicePage(page, roomId, type, subType, status, deviceName, ipAddress);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询设备失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入设备
     */
    @ApiOperation("批量导入设备")
    @PostMapping("/devices/import")
    public Result<Map<String, Object>> importDevices(@RequestParam("file") MultipartFile file) {
        try {
            // TODO: 从session或token中获取当前操作人信息
            Long operatorId = 1L;
            String operatorName = "系统管理员";
            Map<String, Object> result = deviceAssetService.importDevices(file, operatorId, operatorName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量导入设备失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出设备
     */
    @ApiOperation("导出设备")
    @GetMapping("/devices/export")
    public Result<List<Map<String, Object>>> exportDevices(
            @ApiParam("设备类型") @RequestParam(required = false) String deviceType,
            @ApiParam("机房ID") @RequestParam(required = false) String roomId) {
        try {
            List<Map<String, Object>> result = deviceAssetService.exportDevices(deviceType, roomId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导出设备失败", e);
            return Result.error("导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备统计信息
     */
    @ApiOperation("获取设备统计信息")
    @GetMapping("/devices/stats")
    public Result<Map<String, Object>> getDeviceStats(
            @ApiParam("机房ID") @RequestParam(required = false) String roomId) {
        try {
            Map<String, Object> stats = deviceAssetService.getDeviceStats(roomId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取设备统计信息失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取关键设备列表
     */
    @ApiOperation("获取关键设备列表")
    @GetMapping("/devices/key")
    public Result<List<Device>> getKeyDevices(
            @ApiParam("机房ID") @RequestParam(required = false) String roomId) {
        try {
            List<Device> devices = deviceAssetService.getKeyDevices(roomId);
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取关键设备列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    // ============================================
    // SNMP监控
    // ============================================

    /**
     * 测试SNMP连接
     */
    @ApiOperation("测试SNMP连接")
    @PostMapping("/snmp/test")
    public Result<Map<String, Object>> testSnmpConnection(
            @RequestBody Map<String, Object> params) {
        try {
            String ipAddress = (String) params.get("ipAddress");
            Integer port = params.get("port") != null ? Integer.parseInt(params.get("port").toString()) : 161;
            String community = (String) params.get("community");
            String version = (String) params.get("version");

            Map<String, Object> result = snmpMonitorService.testConnection(ipAddress, port, community, version);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试SNMP连接失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 采集设备指标
     */
    @ApiOperation("采集设备指标")
    @PostMapping("/snmp/collect/{deviceId}")
    public Result<List<DeviceMetric>> collectMetrics(@ApiParam("设备ID") @PathVariable String deviceId) {
        try {
            // TODO: 从session获取当前用户
            Device device = deviceAssetService.getById(deviceId);
            if (device == null) {
                return Result.error("设备不存在");
            }

            List<DeviceMetric> metrics = snmpMonitorService.collectMetrics(device);
            return Result.success(metrics);
        } catch (Exception e) {
            log.error("采集设备指标失败", e);
            return Result.error("采集失败: " + e.getMessage());
        }
    }

    // ============================================
    // 设备巡检
    // ============================================

    /**
     * 分页查询巡检记录
     */
    @ApiOperation("分页查询巡检记录")
    @GetMapping("/records/page")
    public Result<IPage<DeviceInspectionRecord>> queryInspectionPage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Integer size,
            @ApiParam("设备ID") @RequestParam(required = false) String deviceId,
            @ApiParam("机房ID") @RequestParam(required = false) String roomId,
            @ApiParam("巡检结果") @RequestParam(required = false) String result,
            @ApiParam("巡检类型") @RequestParam(required = false) String inspectionType) {
        try {
            Page<DeviceInspectionRecord> page = new Page<>(current, size);
            IPage<DeviceInspectionRecord> result = deviceInspectionService.queryInspectionPage(
                    page, deviceId, roomId, result, inspectionType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询巡检记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 执行自动巡检
     */
    @ApiOperation("执行自动巡检")
    @PostMapping("/execute/{deviceId}")
    public Result<DeviceInspectionRecord> executeAutoInspection(@ApiParam("设备ID") @PathVariable String deviceId) {
        try {
            // TODO: 从session获取当前用户
            String inspectorId = "admin";
            DeviceInspectionRecord record = deviceInspectionService.executeAutoInspection(deviceId, inspectorId);
            return Result.success(record, "巡检成功");
        } catch (Exception e) {
            log.error("执行自动巡检失败", e);
            return Result.error("巡检失败: " + e.getMessage());
        }
    }

    /**
     * 提交拍照巡检
     */
    @ApiOperation("提交拍照巡检")
    @PostMapping("/photo")
    public Result<DeviceInspectionRecord> submitPhotoInspection(@RequestBody Map<String, Object> params) {
        try {
            String deviceId = (String) params.get("deviceId");
            List<String> photoUrls = (List<String>) params.get("photoUrls");
            String remark = (String) params.get("remark");
            String inspectorId = "admin";

            DeviceInspectionRecord record = deviceInspectionService.submitPhotoInspection(
                    deviceId, inspectorId, photoUrls, remark);
            return Result.success(record, "提交成功");
        } catch (Exception e) {
            log.error("提交拍照巡检失败", e);
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * OCR识别设备标签
     */
    @ApiOperation("OCR识别设备标签")
    @PostMapping("/ocr/{recordId}")
    public Result<Map<String, Object>> ocrRecognize(@ApiParam("巡检记录ID") @PathVariable Long recordId) {
        try {
            Map<String, Object> result = deviceInspectionService.ocrRecognize(recordId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("OCR识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 批量执行巡检
     */
    @ApiOperation("批量执行巡检")
    @PostMapping("/batch-execute")
    public Result<Map<String, Object>> batchExecuteInspection(@RequestBody Map<String, Object> params) {
        try {
            List<String> deviceIds = (List<String>) params.get("deviceIds");
            String inspectorId = "admin";

            Map<String, Object> result = deviceInspectionService.batchExecuteInspection(deviceIds, inspectorId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量执行巡检失败", e);
            return Result.error("批量巡检失败: " + e.getMessage());
        }
    }

    // ============================================
    // 告警规则
    // ============================================

    /**
     * 获取告警规则列表
     */
    @ApiOperation("获取告警规则列表")
    @GetMapping("/alert-rules")
    public Result<List<com.roominspection.backend.entity.AlertRule>> getAlertRules(
            @ApiParam("设备ID") @RequestParam(required = false) String deviceId) {
        try {
            List<com.roominspection.backend.entity.AlertRule> rules = alertRuleService.getActiveAlertRules(deviceId);
            return Result.success(rules);
        } catch (Exception e) {
            log.error("获取告警规则列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 测试告警规则
     */
    @ApiOperation("测试告警规则")
    @PostMapping("/alert-rules/test/{ruleId}")
    public Result<Boolean> testAlertRule(@ApiParam("规则ID") @PathVariable Long ruleId) {
        try {
            boolean result = alertRuleService.testAlertRule(ruleId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试告警规则失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
}
