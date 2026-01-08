package com.roominspection.backend.controller;

import com.roominspection.backend.ai.AIRecognitionService;
import com.roominspection.backend.common.ApiVersion;
import com.roominspection.backend.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI识别控制器
 */
@RestController
@RequestMapping("/api/v1/ai")
@Api(tags = "AI识别服务")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AIRecognitionController {

    @Autowired
    private AIRecognitionService aiRecognitionService;

    /**
     * 设备指示灯识别
     */
    @PostMapping("/device-light-recognize")
    @ApiOperation(value = "设备指示灯识别", notes = "识别设备指示灯的状态、颜色和亮度")
    @ApiVersion("v1")
    public Result<Map<String, Object>> recognizeDeviceLights(
            @ApiParam(value = "设备ID", required = true) @RequestParam String deviceId,
            @ApiParam(value = "图片Base64编码", required = true) @RequestParam String imageBase64,
            @ApiParam(value = "指示灯位置列表", required = true) @RequestBody List<Map<String, Object>> lightPositions) {
        Map<String, Object> result = aiRecognitionService.recognizeDeviceLights(deviceId, imageBase64, lightPositions);
        return Result.success(result);
    }

    /**
     * 巡检路线优化
     */
    @PostMapping("/route-optimize")
    @ApiOperation(value = "巡检路线优化", notes = "优化巡检路线，减少巡检时间")
    @ApiVersion("v1")
    public Result<Map<String, Object>> optimizeInspectionRoute(
            @ApiParam(value = "机房ID", required = true) @RequestParam String roomId,
            @ApiParam(value = "设备ID列表", required = true) @RequestBody List<String> deviceIds,
            @ApiParam(value = "起始位置", required = true) @RequestBody Map<String, Integer> startLocation,
            @ApiParam(value = "约束条件") @RequestBody(required = false) Map<String, Object> constraints) {
        Map<String, Object> result = aiRecognitionService.optimizeInspectionRoute(roomId, deviceIds, startLocation, constraints);
        return Result.success(result);
    }

    /**
     * 预测性维护分析
     */
    @PostMapping("/predictive-maintenance")
    @ApiOperation(value = "预测性维护分析", notes = "预测设备故障、性能趋势和健康度")
    @ApiVersion("v1")
    public Result<Map<String, Object>> predictiveMaintenanceAnalysis(
            @ApiParam(value = "设备ID", required = true) @RequestParam String deviceId,
            @ApiParam(value = "预测类型（failure/performance/health）", required = true) @RequestParam String predictionType,
            @ApiParam(value = "时间范围（天数）", defaultValue = "30") @RequestParam(defaultValue = "30") Integer timeRange) {
        Map<String, Object> result = aiRecognitionService.predictiveMaintenanceAnalysis(deviceId, predictionType, timeRange);
        return Result.success(result);
    }

    /**
     * 异常检测
     */
    @PostMapping("/anomaly-detection")
    @ApiOperation(value = "异常检测", notes = "检测设备运行数据的异常情况")
    @ApiVersion("v1")
    public Result<Map<String, Object>> anomalyDetection(
            @ApiParam(value = "设备ID", required = true) @RequestParam String deviceId,
            @ApiParam(value = "指标数据", required = true) @RequestBody Map<String, Object> metrics) {
        Map<String, Object> result = aiRecognitionService.anomalyDetection(deviceId, metrics);
        return Result.success(result);
    }

    /**
     * 智能告警分析
     */
    @PostMapping("/alarm-analysis")
    @ApiOperation(value = "智能告警分析", notes = "分析告警原因并提供处理建议")
    @ApiVersion("v1")
    public Result<Map<String, Object>> intelligentAlarmAnalysis(
            @ApiParam(value = "设备ID", required = true) @RequestParam String deviceId,
            @ApiParam(value = "告警数据", required = true) @RequestBody Map<String, Object> alarmData) {
        Map<String, Object> result = aiRecognitionService.intelligentAlarmAnalysis(deviceId, alarmData);
        return Result.success(result);
    }

    /**
     * 能效优化建议
     */
    @GetMapping("/energy-efficiency-optimization")
    @ApiOperation(value = "能效优化建议", notes = "分析机房能效并提供优化建议")
    @ApiVersion("v1")
    public Result<Map<String, Object>> energyEfficiencyOptimization(
            @ApiParam(value = "机房ID", required = true) @RequestParam String roomId,
            @ApiParam(value = "时间周期（daily/weekly/monthly）", defaultValue = "weekly") @RequestParam(defaultValue = "weekly") String period) {
        Map<String, Object> result = aiRecognitionService.energyEfficiencyOptimization(roomId, period);
        return Result.success(result);
    }
}
