package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.DoorAccessLog;
import com.roominspection.backend.entity.InspectionVerification;
import com.roominspection.backend.entity.PhotoVerification;
import com.roominspection.backend.service.DoorAccessLogService;
import com.roominspection.backend.service.InspectionVerificationService;
import com.roominspection.backend.service.PhotoVerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 巡检核验控制器
 */
@Slf4j
@Api(tags = "巡检核验管理")
@RestController
@RequestMapping("/api/inspection-verification")
public class InspectionVerificationController {

    @Autowired
    private DoorAccessLogService doorAccessLogService;

    @Autowired
    private PhotoVerificationService photoVerificationService;

    @Autowired
    private InspectionVerificationService inspectionVerificationService;

    // ==================== 门禁日志核验 ====================

    @ApiOperation("分页查询门禁日志")
    @GetMapping("/door-access/page")
    public Result<Page<DoorAccessLog>> queryDoorAccessPage(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<DoorAccessLog> page = doorAccessLogService.queryPage(roomId, staffId, direction, startTime, endTime, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("同步海康门禁日志")
    @PostMapping("/door-access/sync/hikvision")
    public Result<Integer> syncHikvision(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        int count = doorAccessLogService.syncFromHikvision(startTime, endTime);
        return Result.success(count, "成功同步" + count + "条记录");
    }

    @ApiOperation("同步大华门禁日志")
    @PostMapping("/door-access/sync/dahua")
    public Result<Integer> syncDahua(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        int count = doorAccessLogService.syncFromDahua(startTime, endTime);
        return Result.success(count, "成功同步" + count + "条记录");
    }

    @ApiOperation("核验巡检人员进出记录")
    @GetMapping("/door-access/verify/{inspectionTaskId}")
    public Result<Map<String, Object>> verifyInspectionAccess(@PathVariable Long inspectionTaskId) {
        // TODO: 实现完整的核验逻辑
        return Result.success();
    }

    // ==================== 照片核验 ====================

    @ApiOperation("分页查询照片核验记录")
    @GetMapping("/photo/page")
    public Result<Page<PhotoVerification>> queryPhotoPage(
            @RequestParam(required = false) Long inspectionTaskId,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<PhotoVerification> page = photoVerificationService.queryPage(inspectionTaskId, deviceId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("上传并核验照片")
    @PostMapping("/photo/upload")
    public Result<PhotoVerification> uploadAndVerify(
            @RequestParam Long inspectionTaskId,
            @RequestParam Long deviceId,
            @RequestParam Long uploaderId,
            @RequestParam String uploaderName,
            @RequestParam String photoPath,
            @RequestParam String photoUrl) {
        PhotoVerification verification = photoVerificationService.uploadAndVerify(
                inspectionTaskId, deviceId, uploaderId, uploaderName, photoPath, photoUrl);
        return Result.success(verification);
    }

    @ApiOperation("批量核验照片")
    @PostMapping("/photo/batch-verify/{inspectionTaskId}")
    public Result<Map<String, Object>> batchVerifyPhotos(@PathVariable Long inspectionTaskId) {
        Map<String, Object> result = photoVerificationService.batchVerifyPhotos(inspectionTaskId);
        return Result.success(result);
    }

    @ApiOperation("获取照片核验统计")
    @GetMapping("/photo/statistics/{inspectionTaskId}")
    public Result<Map<String, Object>> getPhotoStatistics(@PathVariable Long inspectionTaskId) {
        Map<String, Object> stats = photoVerificationService.getStatistics(inspectionTaskId);
        return Result.success(stats);
    }

    @ApiOperation("查询异常照片")
    @GetMapping("/photo/abnormal")
    public Result<java.util.List<PhotoVerification>> findAbnormalPhotos() {
        java.util.List<PhotoVerification> photos = photoVerificationService.findAbnormalPhotos();
        return Result.success(photos);
    }

    @ApiOperation("人工复核照片")
    @PostMapping("/photo/manual-review")
    public Result<Boolean> manualReview(
            @RequestParam Long photoId,
            @RequestParam Long verifierId,
            @RequestParam String status,
            @RequestParam(required = false) String comment) {
        boolean result = photoVerificationService.manualReview(photoId, verifierId, status, comment);
        return Result.success(result);
    }

    // ==================== 巡检核验 ====================

    @ApiOperation("分页查询巡检核验记录")
    @GetMapping("/page")
    public Result<Page<InspectionVerification>> queryPage(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long inspectorId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<InspectionVerification> page = inspectionVerificationService.queryPage(roomId, inspectorId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("创建巡检核验记录")
    @PostMapping("/create")
    public Result<InspectionVerification> createVerification(
            @RequestParam Long inspectionTaskId,
            @RequestParam String taskNo,
            @RequestParam Long roomId,
            @RequestParam String roomName,
            @RequestParam Long inspectorId,
            @RequestParam String inspectorName,
            @RequestParam String plannedRoute,
            @RequestParam Integer plannedDeviceCount) {
        InspectionVerification verification = inspectionVerificationService.createVerification(
                inspectionTaskId, taskNo, roomId, roomName, inspectorId, inspectorName, plannedRoute, plannedDeviceCount);
        return Result.success(verification);
    }

    @ApiOperation("分析巡检完整性")
    @PostMapping("/analyze-completeness/{verificationId}")
    public Result<InspectionVerification> analyzeCompleteness(@PathVariable Long verificationId) {
        InspectionVerification verification = inspectionVerificationService.analyzeCompleteness(verificationId);
        return Result.success(verification);
    }

    @ApiOperation("生成核验报告")
    @GetMapping("/report/{verificationId}")
    public Result<String> generateReport(@PathVariable Long verificationId) {
        String report = inspectionVerificationService.generateVerificationReport(verificationId);
        return Result.success(report);
    }

    @ApiOperation("获取核验统计")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Map<String, Object> stats = inspectionVerificationService.getStatistics(startTime, endTime);
        return Result.success(stats);
    }
}
