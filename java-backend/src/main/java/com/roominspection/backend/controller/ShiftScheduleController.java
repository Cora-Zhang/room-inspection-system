package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.ShiftSchedule;
import com.roominspection.backend.service.ShiftScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 值班排班Controller
 */
@Tag(name = "值班排班管理")
@RestController
@RequestMapping("/api/shift/schedule")
@RequiredArgsConstructor
public class ShiftScheduleController {

    private final ShiftScheduleService shiftScheduleService;

    /**
     * 查询排班列表
     */
    @Operation(summary = "查询排班列表")
    @GetMapping("/list")
    public Result<List<ShiftSchedule>> getScheduleList(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String staffId) {

        List<ShiftSchedule> list = shiftScheduleService.getScheduleList(startDate, endDate, roomId, staffId);
        return Result.success(list);
    }

    /**
     * 查询排班详情
     */
    @Operation(summary = "查询排班详情")
    @GetMapping("/{id}")
    public Result<ShiftSchedule> getScheduleDetail(@PathVariable String id) {
        ShiftSchedule schedule = shiftScheduleService.getById(id);
        return Result.success(schedule);
    }

    /**
     * 创建排班
     */
    @Operation(summary = "创建排班")
    @PostMapping("/create")
    public Result<Boolean> createSchedule(@RequestBody ShiftSchedule schedule) {
        boolean success = shiftScheduleService.createSchedule(schedule);
        return success ? Result.success(true) : Result.error("创建排班失败");
    }

    /**
     * 批量创建排班
     */
    @Operation(summary = "批量创建排班")
    @PostMapping("/batch-create")
    public Result<Boolean> batchCreateSchedules(@RequestBody List<ShiftSchedule> schedules) {
        boolean success = shiftScheduleService.batchCreateSchedules(schedules);
        return success ? Result.success(true) : Result.error("批量创建排班失败");
    }

    /**
     * 更新排班
     */
    @Operation(summary = "更新排班")
    @PutMapping("/update")
    public Result<Boolean> updateSchedule(@RequestBody ShiftSchedule schedule) {
        boolean success = shiftScheduleService.updateSchedule(schedule);
        return success ? Result.success(true) : Result.error("更新排班失败");
    }

    /**
     * 删除排班
     */
    @Operation(summary = "删除排班")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteSchedule(@PathVariable String id) {
        boolean success = shiftScheduleService.deleteSchedule(id);
        return success ? Result.success(true) : Result.error("删除排班失败");
    }

    /**
     * Excel导入排班
     */
    @Operation(summary = "Excel导入排班")
    @PostMapping("/import")
    public Result<Map<String, Object>> importFromExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = shiftScheduleService.importFromExcel(file);
        return Result.success(result);
    }

    /**
     * 导出排班Excel模板
     */
    @Operation(summary = "导出排班Excel模板")
    @GetMapping("/template")
    public Result<byte[]> downloadTemplate() {
        // TODO: 实现模板下载
        return Result.success(new byte[0]);
    }

    /**
     * 周期性排班
     */
    @Operation(summary = "周期性排班")
    @PostMapping("/periodic")
    public Result<Map<String, Object>> createPeriodicSchedule(
            @RequestParam String staffId,
            @RequestParam Long roomId,
            @RequestParam String shift,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam Integer scheduleType) {

        Map<String, Object> result = shiftScheduleService.createPeriodicSchedule(
                staffId, roomId, shift, startDate, endDate, scheduleType);
        return Result.success(result);
    }

    /**
     * 从钉钉多维表同步排班
     */
    @Operation(summary = "从钉钉多维表同步排班")
    @PostMapping("/sync/dingtalk")
    public Result<Map<String, Object>> syncFromDingTalk(
            @RequestParam String appId,
            @RequestParam String tableId) {

        Map<String, Object> result = shiftScheduleService.syncFromDingTalk(appId, tableId);
        return Result.success(result);
    }
}
