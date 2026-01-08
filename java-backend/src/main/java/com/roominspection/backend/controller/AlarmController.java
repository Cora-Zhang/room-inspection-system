package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.AlarmRecord;
import com.roominspection.backend.service.AlarmRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警管理Controller
 */
@RestController
@RequestMapping("/api/alarms")
@CrossOrigin
public class AlarmController {

    @Autowired
    private AlarmRecordService alarmRecordService;

    /**
     * 分页查询告警列表
     */
    @GetMapping("/list")
    public Result<List<AlarmRecord>> list(
            @RequestParam(required = false) String roomId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<AlarmRecord> alarms;
        if (startTime != null && endTime != null) {
            if (roomId != null) {
                alarms = alarmRecordService.listByRoomAndTimeRange(roomId, startTime, endTime);
            } else if (level != null) {
                alarms = alarmRecordService.listByLevel(level, startTime, endTime);
            } else {
                alarms = alarmRecordService.listByTimeRange(startTime, endTime);
            }
        } else {
            // 默认查询最近的告警
            LocalDateTime now = LocalDateTime.now();
            alarms = alarmRecordService.listByTimeRange(now.minusDays(7), now);
        }

        return Result.success(alarms);
    }

    /**
     * 获取未处理告警列表
     */
    @GetMapping("/unresolved")
    public Result<List<AlarmRecord>> getUnresolved() {
        List<AlarmRecord> alarms = alarmRecordService.getUnresolvedAlarms();
        return Result.success(alarms);
    }

    /**
     * 获取紧急告警列表
     */
    @GetMapping("/critical")
    public Result<List<AlarmRecord>> getCritical() {
        List<AlarmRecord> alarms = alarmRecordService.getCriticalAlarms();
        return Result.success(alarms);
    }

    /**
     * 创建告警记录
     */
    @PostMapping("/create")
    public Result<Boolean> createAlarm(@RequestBody AlarmRecord alarm) {
        boolean result = alarmRecordService.createAlarm(alarm);
        return Result.success(result);
    }

    /**
     * 确认告警
     */
    @PutMapping("/{id}/acknowledge")
    public Result<Boolean> acknowledgeAlarm(
            @PathVariable String id,
            @RequestParam String acknowledgedBy,
            @RequestParam String acknowledgedByName) {
        boolean result = alarmRecordService.acknowledgeAlarm(id, acknowledgedBy, acknowledgedByName);
        return Result.success(result);
    }

    /**
     * 解决告警
     */
    @PutMapping("/{id}/resolve")
    public Result<Boolean> resolveAlarm(
            @PathVariable String id,
            @RequestParam String handledBy,
            @RequestParam String handledByName,
            @RequestParam String handleResult) {
        boolean result = alarmRecordService.resolveAlarm(id, handledBy, handledByName, handleResult);
        return Result.success(result);
    }

    /**
     * 获取告警统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Map<String, Object> stats = alarmRecordService.getStatistics(startTime, endTime);
        return Result.success(stats);
    }

    /**
     * 按级别统计告警数量
     */
    @GetMapping("/count/level")
    public Result<List<Map<String, Object>>> countByLevel(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Map<String, Object>> count = alarmRecordService.countByLevel(startTime, endTime);
        return Result.success(count);
    }

    /**
     * 按类型统计告警数量
     */
    @GetMapping("/count/type")
    public Result<List<Map<String, Object>>> countByType(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Map<String, Object>> count = alarmRecordService.countByType(startTime, endTime);
        return Result.success(count);
    }

    /**
     * 按状态统计告警数量
     */
    @GetMapping("/count/status")
    public Result<List<Map<String, Object>>> countByStatus(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<Map<String, Object>> count = alarmRecordService.countByStatus(startTime, endTime);
        return Result.success(count);
    }

    /**
     * 关联工单
     */
    @PutMapping("/{id}/link-workorder")
    public Result<Boolean> linkWorkOrder(
            @PathVariable String id,
            @RequestParam String workOrderId) {
        boolean result = alarmRecordService.linkWorkOrder(id, workOrderId);
        return Result.success(result);
    }
}
