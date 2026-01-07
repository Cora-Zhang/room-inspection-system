package com.roominspection.backend.controller;

import com.roominspection.backend.entity.EnvironmentData;
import com.roominspection.backend.entity.EnvironmentSensor;
import com.roominspection.backend.service.EnvironmentDataService;
import com.roominspection.backend.service.EnvironmentSensorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境监控控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/environment")
public class EnvironmentMonitoringController {

    @Autowired
    private EnvironmentSensorService environmentSensorService;

    @Autowired
    private EnvironmentDataService environmentDataService;

    // ==================== 传感器管理 ====================

    /**
     * 添加传感器
     */
    @PostMapping("/sensor")
    public Map<String, Object> addSensor(@RequestBody EnvironmentSensor sensor) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long id = environmentSensorService.addSensor(sensor);
            result.put("success", true);
            result.put("message", "添加传感器成功");
            result.put("data", id);
        } catch (Exception e) {
            log.error("添加传感器失败", e);
            result.put("success", false);
            result.put("message", "添加传感器失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新传感器
     */
    @PutMapping("/sensor")
    public Map<String, Object> updateSensor(@RequestBody EnvironmentSensor sensor) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = environmentSensorService.updateSensor(sensor);
            result.put("success", success);
            result.put("message", success ? "更新传感器成功" : "更新传感器失败");
        } catch (Exception e) {
            log.error("更新传感器失败", e);
            result.put("success", false);
            result.put("message", "更新传感器失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除传感器
     */
    @DeleteMapping("/sensor/{id}")
    public Map<String, Object> deleteSensor(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = environmentSensorService.deleteSensor(id);
            result.put("success", success);
            result.put("message", success ? "删除传感器成功" : "删除传感器失败");
        } catch (Exception e) {
            log.error("删除传感器失败", e);
            result.put("success", false);
            result.put("message", "删除传感器失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批量删除传感器
     */
    @DeleteMapping("/sensor/batch")
    public Map<String, Object> batchDeleteSensors(@RequestBody List<Long> ids) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = environmentSensorService.batchDeleteSensors(ids);
            result.put("success", success);
            result.put("message", success ? "批量删除传感器成功" : "批量删除传感器失败");
        } catch (Exception e) {
            log.error("批量删除传感器失败", e);
            result.put("success", false);
            result.put("message", "批量删除传感器失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取传感器详情
     */
    @GetMapping("/sensor/{id}")
    public Map<String, Object> getSensorDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            EnvironmentSensor sensor = environmentSensorService.getSensorDetail(id);
            result.put("success", true);
            result.put("data", sensor);
        } catch (Exception e) {
            log.error("获取传感器详情失败", e);
            result.put("success", false);
            result.put("message", "获取传感器详情失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 查询传感器列表
     */
    @GetMapping("/sensor/list")
    public Map<String, Object> getSensorList(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("roomId", roomId);
            params.put("sensorType", sensorType);
            params.put("status", status);
            params.put("keyword", keyword);
            
            List<EnvironmentSensor> list = environmentSensorService.getSensorList(params);
            result.put("success", true);
            result.put("data", list);
        } catch (Exception e) {
            log.error("查询传感器列表失败", e);
            result.put("success", false);
            result.put("message", "查询传感器列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取机房传感器列表
     */
    @GetMapping("/sensor/room/{roomId}")
    public Map<String, Object> getSensorsByRoomId(@PathVariable Long roomId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<EnvironmentSensor> list = environmentSensorService.getSensorsByRoomId(roomId);
            result.put("success", true);
            result.put("data", list);
        } catch (Exception e) {
            log.error("获取机房传感器列表失败", e);
            result.put("success", false);
            result.put("message", "获取机房传感器列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 批量采集传感器数据
     */
    @PostMapping("/sensor/batch-collect")
    public Map<String, Object> batchCollectData() {
        Map<String, Object> result = new HashMap<>();
        try {
            environmentSensorService.batchCollectData();
            result.put("success", true);
            result.put("message", "批量采集成功");
        } catch (Exception e) {
            log.error("批量采集传感器数据失败", e);
            result.put("success", false);
            result.put("message", "批量采集失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 手动触发数据采集
     */
    @PostMapping("/sensor/collect/{sensorId}")
    public Map<String, Object> manualCollectData(@PathVariable Long sensorId) {
        Map<String, Object> result = new HashMap<>();
        try {
            environmentSensorService.manualCollectData(sensorId);
            result.put("success", true);
            result.put("message", "采集成功");
        } catch (Exception e) {
            log.error("手动采集传感器数据失败", e);
            result.put("success", false);
            result.put("message", "采集失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取传感器最新数据
     */
    @GetMapping("/sensor/{sensorId}/latest-data")
    public Map<String, Object> getSensorLatestData(@PathVariable Long sensorId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = environmentSensorService.getSensorLatestData(sensorId);
            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取传感器最新数据失败", e);
            result.put("success", false);
            result.put("message", "获取传感器最新数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取机房传感器统计
     */
    @GetMapping("/sensor/room/{roomId}/statistics")
    public Map<String, Object> getRoomSensorStatistics(@PathVariable Long roomId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> statistics = environmentSensorService.getRoomSensorStatistics(roomId);
            result.put("success", true);
            result.put("data", statistics);
        } catch (Exception e) {
            log.error("获取机房传感器统计失败", e);
            result.put("success", false);
            result.put("message", "获取机房传感器统计失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 环境数据 ====================

    /**
     * 获取机房最新数据
     */
    @GetMapping("/data/room/{roomId}/latest")
    public Map<String, Object> getLatestDataByRoomId(@PathVariable Long roomId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<EnvironmentData> list = environmentDataService.getLatestDataByRoomId(roomId);
            result.put("success", true);
            result.put("data", list);
        } catch (Exception e) {
            log.error("获取机房最新数据失败", e);
            result.put("success", false);
            result.put("message", "获取机房最新数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取数据趋势
     */
    @GetMapping("/data/trend")
    public Map<String, Object> getDataTrend(
            @RequestParam Long roomId,
            @RequestParam String dataType,
            @RequestParam(defaultValue = "24") Integer hours) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> trend = environmentDataService.getDataTrend(roomId, dataType, hours);
            result.put("success", true);
            result.put("data", trend);
        } catch (Exception e) {
            log.error("获取数据趋势失败", e);
            result.put("success", false);
            result.put("message", "获取数据趋势失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取告警数据列表
     */
    @GetMapping("/data/alarm")
    public Map<String, Object> getAlarmDataList(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<EnvironmentData> list = environmentDataService.getAlarmDataList(roomId, startTime, endTime);
            result.put("success", true);
            result.put("data", list);
        } catch (Exception e) {
            log.error("获取告警数据列表失败", e);
            result.put("success", false);
            result.put("message", "获取告警数据列表失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 热力图 ====================

    /**
     * 生成热力图数据
     */
    @GetMapping("/heatmap/generate")
    public Map<String, Object> generateHeatmap(
            @RequestParam Long roomId,
            @RequestParam String heatmapType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime dataTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<?> heatmapData = environmentDataService.generateHeatmapData(roomId, heatmapType, dataTime);
            result.put("success", true);
            result.put("data", heatmapData);
        } catch (Exception e) {
            log.error("生成热力图数据失败", e);
            result.put("success", false);
            result.put("message", "生成热力图数据失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 识别异常区域
     */
    @GetMapping("/heatmap/abnormal")
    public Map<String, Object> identifyAbnormalAreas(
            @RequestParam Long roomId,
            @RequestParam String heatmapType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime dataTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> abnormalAreas = environmentDataService.identifyAbnormalAreas(roomId, heatmapType, dataTime);
            result.put("success", true);
            result.put("data", abnormalAreas);
        } catch (Exception e) {
            log.error("识别异常区域失败", e);
            result.put("success", false);
            result.put("message", "识别异常区域失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取数据统计
     */
    @GetMapping("/data/statistics")
    public Map<String, Object> getDataStatistics(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> statistics = environmentDataService.getDataStatistics(roomId, startTime, endTime);
            result.put("success", true);
            result.put("data", statistics);
        } catch (Exception e) {
            log.error("获取数据统计失败", e);
            result.put("success", false);
            result.put("message", "获取数据统计失败: " + e.getMessage());
        }
        return result;
    }
}
