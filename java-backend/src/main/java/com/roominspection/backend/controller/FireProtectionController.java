package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.*;
import com.roominspection.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消防保障系统监控Controller
 */
@RestController
@RequestMapping("/api/fire-protection")
@CrossOrigin
public class FireProtectionController {

    @Autowired
    private FireExtinguisherService extinguisherService;

    @Autowired
    private FireExtinguisherCheckService extinguisherCheckService;

    @Autowired
    private FireHostLogService fireHostLogService;

    @Autowired
    private GasPressureMonitorService gasPressureMonitorService;

    @Autowired
    private FireInspectionReminderService fireInspectionReminderService;

    // ============================================
    // 灭火器管理
    // ============================================

    /**
     * 查询灭火器列表
     */
    @GetMapping("/extinguishers/list")
    public Result<IPage<FireExtinguisher>> listExtinguishers(
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FireExtinguisher> page = new Page<>(current, size);
        IPage<FireExtinguisher> result = extinguisherService.page(page, null);
        return Result.success(result);
    }

    /**
     * 根据机房ID查询灭火器列表
     */
    @GetMapping("/extinguishers/listByRoom")
    public Result<List<FireExtinguisher>> listExtinguishersByRoom(@RequestParam Long roomId) {
        List<FireExtinguisher> list = extinguisherService.listByRoomId(roomId);
        return Result.success(list);
    }

    /**
     * 获取灭火器详情
     */
    @GetMapping("/extinguishers/detail")
    public Result<FireExtinguisher> getExtinguisherDetail(@RequestParam Long id) {
        FireExtinguisher extinguisher = extinguisherService.getById(id);
        return Result.success(extinguisher);
    }

    /**
     * 新增灭火器
     */
    @PostMapping("/extinguishers/add")
    public Result<Void> addExtinguisher(@RequestBody FireExtinguisher extinguisher) {
        extinguisherService.save(extinguisher);
        return Result.success();
    }

    /**
     * 更新灭火器
     */
    @PostMapping("/extinguishers/update")
    public Result<Void> updateExtinguisher(@RequestBody FireExtinguisher extinguisher) {
        extinguisherService.updateById(extinguisher);
        return Result.success();
    }

    /**
     * 删除灭火器
     */
    @PostMapping("/extinguishers/delete")
    public Result<Void> deleteExtinguisher(@RequestParam Long id) {
        extinguisherService.removeById(id);
        return Result.success();
    }

    /**
     * 批量采集灭火器数据
     */
    @PostMapping("/extinguishers/batchCollect")
    public Result<Void> batchCollectExtinguisherData() {
        extinguisherService.batchCollectData();
        return Result.success();
    }

    /**
     * 手动触发数据采集
     */
    @PostMapping("/extinguishers/collect")
    public Result<Void> collectExtinguisherData(@RequestParam Long extinguisherId) {
        extinguisherService.collectData(extinguisherId);
        return Result.success();
    }

    /**
     * 获取灭火器统计数据
     */
    @GetMapping("/extinguishers/statistics")
    public Result<Map<String, Object>> getExtinguisherStatistics(
            @RequestParam(required = false) Long roomId) {
        Map<String, Object> stats = extinguisherService.getStatistics(roomId);
        return Result.success(stats);
    }

    /**
     * 生成月度检查工单
     */
    @PostMapping("/extinguishers/generateMonthlyCheck")
    public Result<Void> generateMonthlyCheck() {
        extinguisherService.generateMonthlyCheckOrder();
        return Result.success();
    }

    // ============================================
    // 灭火器检查记录
    // ============================================

    /**
     * 查询灭火器检查记录列表
     */
    @GetMapping("/extinguisherChecks/list")
    public Result<IPage<FireExtinguisherCheck>> listExtinguisherChecks(
            @RequestParam(required = false) Long extinguisherId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer checkType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FireExtinguisherCheck> page = new Page<>(current, size);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FireExtinguisherCheck> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(extinguisherId != null, FireExtinguisherCheck::getExtinguisherId, extinguisherId);
        wrapper.eq(roomId != null, FireExtinguisherCheck::getRoomId, roomId);
        wrapper.eq(checkType != null, FireExtinguisherCheck::getCheckType, checkType);
        wrapper.orderByDesc(FireExtinguisherCheck::getCheckTime);

        IPage<FireExtinguisherCheck> result = extinguisherCheckService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 完成检查
     */
    @PostMapping("/extinguisherChecks/complete")
    public Result<Void> completeExtinguisherCheck(@RequestBody Map<String, Object> params) {
        Long checkId = Long.valueOf(params.get("checkId").toString());
        String checker = (String) params.get("checker");
        Double pressureValue = params.get("pressureValue") != null ?
                Double.valueOf(params.get("pressureValue").toString()) : null;
        Double weightValue = params.get("weightValue") != null ?
                Double.valueOf(params.get("weightValue").toString()) : null;
        Integer pressureStatus = params.get("pressureStatus") != null ?
                Integer.valueOf(params.get("pressureStatus").toString()) : null;
        Integer weightStatus = params.get("weightStatus") != null ?
                Integer.valueOf(params.get("weightStatus").toString()) : null;
        Integer appearanceStatus = params.get("appearanceStatus") != null ?
                Integer.valueOf(params.get("appearanceStatus").toString()) : null;
        String appearanceDescription = (String) params.get("appearanceDescription");
        String photos = (String) params.get("photos");
        Integer needRefill = params.get("needRefill") != null ?
                Integer.valueOf(params.get("needRefill").toString()) : null;
        String refillRemark = (String) params.get("refillRemark");
        Integer checkResult = params.get("checkResult") != null ?
                Integer.valueOf(params.get("checkResult").toString()) : null;
        String handlingMeasures = (String) params.get("handlingMeasures");

        extinguisherCheckService.completeCheck(checkId, checker, pressureValue, weightValue,
                pressureStatus, weightStatus, appearanceStatus, appearanceDescription,
                photos, needRefill, refillRemark, checkResult, handlingMeasures);
        return Result.success();
    }

    // ============================================
    // 消防主机日志
    // ============================================

    /**
     * 查询消防主机日志列表
     */
    @GetMapping("/fireHostLogs/list")
    public Result<IPage<FireHostLog>> listFireHostLogs(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer signalType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FireHostLog> page = new Page<>(current, size);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FireHostLog> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, FireHostLog::getRoomId, roomId);
        wrapper.eq(signalType != null, FireHostLog::getSignalType, signalType);
        if (startTime != null) {
            wrapper.ge(FireHostLog::getSignalTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(FireHostLog::getSignalTime, endTime);
        }
        wrapper.orderByDesc(FireHostLog::getSignalTime);

        IPage<FireHostLog> result = fireHostLogService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 接收消防主机信号
     */
    @PostMapping("/fireHostLogs/receive")
    public Result<Void> receiveFireHostSignal(@RequestBody Map<String, Object> params) {
        Long hostId = params.get("hostId") != null ? Long.valueOf(params.get("hostId").toString()) : null;
        Integer signalType = params.get("signalType") != null ?
                Integer.valueOf(params.get("signalType").toString()) : null;
        String loopNo = (String) params.get("loopNo");
        String detectorAddress = (String) params.get("detectorAddress");
        Integer detectorType = params.get("detectorType") != null ?
                Integer.valueOf(params.get("detectorType").toString()) : null;
        String detectorLocation = (String) params.get("detectorLocation");
        String signalDescription = (String) params.get("signalDescription");

        fireHostLogService.receiveSignal(hostId, signalType, loopNo, detectorAddress,
                detectorType, detectorLocation, signalDescription);
        return Result.success();
    }

    /**
     * 确认信号
     */
    @PostMapping("/fireHostLogs/confirm")
    public Result<Void> confirmFireHostSignal(
            @RequestParam Long logId,
            @RequestParam String confirmer,
            @RequestParam(required = false) String confirmRemark) {
        fireHostLogService.confirmSignal(logId, confirmer, confirmRemark);
        return Result.success();
    }

    /**
     * 处理信号
     */
    @PostMapping("/fireHostLogs/handle")
    public Result<Void> handleFireHostSignal(
            @RequestParam Long logId,
            @RequestParam String handler,
            @RequestParam String handleResult) {
        fireHostLogService.handleSignal(logId, handler, handleResult);
        return Result.success();
    }

    /**
     * 获取未确认的信号
     */
    @GetMapping("/fireHostLogs/unconfirmed")
    public Result<List<FireHostLog>> listUnconfirmedSignals() {
        List<FireHostLog> list = fireHostLogService.listUnconfirmedSignals();
        return Result.success(list);
    }

    /**
     * 获取未处理的信号
     */
    @GetMapping("/fireHostLogs/unhandled")
    public Result<List<FireHostLog>> listUnhandledSignals() {
        List<FireHostLog> list = fireHostLogService.listUnhandledSignals();
        return Result.success(list);
    }

    // ============================================
    // 气体压力监控
    // ============================================

    /**
     * 查询气体压力监控列表
     */
    @GetMapping("/gasPressureMonitors/list")
    public Result<IPage<GasPressureMonitor>> listGasPressureMonitors(
            @RequestParam(required = false) Long roomId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<GasPressureMonitor> page = new Page<>(current, size);
        IPage<GasPressureMonitor> result = gasPressureMonitorService.page(page, null);
        return Result.success(result);
    }

    /**
     * 根据机房ID查询气体压力监控列表
     */
    @GetMapping("/gasPressureMonitors/listByRoom")
    public Result<List<GasPressureMonitor>> listGasPressureMonitorsByRoom(@RequestParam Long roomId) {
        List<GasPressureMonitor> list = gasPressureMonitorService.listByRoomId(roomId);
        return Result.success(list);
    }

    /**
     * 批量采集气体压力数据
     */
    @PostMapping("/gasPressureMonitors/batchCollect")
    public Result<Void> batchCollectGasPressure() {
        gasPressureMonitorService.batchCollectData();
        return Result.success();
    }

    /**
     * 获取气体压力统计数据
     */
    @GetMapping("/gasPressureMonitors/statistics")
    public Result<Map<String, Object>> getGasPressureStatistics(
            @RequestParam(required = false) Long roomId) {
        Map<String, Object> stats = gasPressureMonitorService.getStatistics(roomId);
        return Result.success(stats);
    }

    // ============================================
    // 消防设施审验提醒
    // ============================================

    /**
     * 查询审验提醒列表
     */
    @GetMapping("/inspectionReminders/list")
    public Result<IPage<FireInspectionReminder>> listInspectionReminders(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer facilityType,
            @RequestParam(required = false) LocalDate reminderDate,
            @RequestParam(required = false) Integer handled,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<FireInspectionReminder> page = new Page<>(current, size);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FireInspectionReminder> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, FireInspectionReminder::getRoomId, roomId);
        wrapper.eq(facilityType != null, FireInspectionReminder::getFacilityType, facilityType);
        wrapper.eq(reminderDate != null, FireInspectionReminder::getReminderDate, reminderDate);
        wrapper.eq(handled != null, FireInspectionReminder::getHandled, handled);
        wrapper.orderByDesc(FireInspectionReminder::getReminderDate);

        IPage<FireInspectionReminder> result = fireInspectionReminderService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 查询未处理的提醒
     */
    @GetMapping("/inspectionReminders/unhandled")
    public Result<List<FireInspectionReminder>> listUnhandledReminders() {
        List<FireInspectionReminder> list = fireInspectionReminderService.listUnhandledReminders();
        return Result.success(list);
    }

    /**
     * 生成审验提醒
     */
    @PostMapping("/inspectionReminders/generate")
    public Result<Void> generateInspectionReminders() {
        fireInspectionReminderService.generateInspectionReminders();
        return Result.success();
    }

    /**
     * 处理提醒
     */
    @PostMapping("/inspectionReminders/handle")
    public Result<Void> handleReminder(
            @RequestParam Long reminderId,
            @RequestParam String handler,
            @RequestParam String handleResult) {
        fireInspectionReminderService.handleReminder(reminderId, handler, handleResult);
        return Result.success();
    }

    /**
     * 添加消防设施审验信息
     */
    @PostMapping("/inspectionReminders/addFacility")
    public Result<Void> addFacilityInspection(@RequestBody Map<String, Object> params) {
        Long facilityId = params.get("facilityId") != null ?
                Long.valueOf(params.get("facilityId").toString()) : null;
        String facilityName = (String) params.get("facilityName");
        Integer facilityType = params.get("facilityType") != null ?
                Integer.valueOf(params.get("facilityType").toString()) : null;
        Long roomId = params.get("roomId") != null ?
                Long.valueOf(params.get("roomId").toString()) : null;
        String facilityCode = (String) params.get("facilityCode");
        String facilityLocation = (String) params.get("facilityLocation");
        Integer inspectionType = params.get("inspectionType") != null ?
                Integer.valueOf(params.get("inspectionType").toString()) : null;
        LocalDate lastInspectionDate = params.get("lastInspectionDate") != null ?
                LocalDate.parse(params.get("lastInspectionDate").toString()) : null;
        Integer inspectionPeriod = params.get("inspectionPeriod") != null ?
                Integer.valueOf(params.get("inspectionPeriod").toString()) : null;

        fireInspectionReminderService.addFacilityInspection(facilityId, facilityName, facilityType,
                roomId, facilityCode, facilityLocation, inspectionType,
                lastInspectionDate, inspectionPeriod);
        return Result.success();
    }
}
