package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.DoorAccessPermission;
import com.roominspection.backend.entity.TempAccessRequest;
import com.roominspection.backend.service.DoorAccessPermissionService;
import com.roominspection.backend.service.TempAccessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 门禁权限管理Controller
 */
@Tag(name = "门禁权限管理")
@RestController
@RequestMapping("/api/door/access")
@RequiredArgsConstructor
public class DoorAccessController {

    private final DoorAccessPermissionService doorAccessPermissionService;
    private final TempAccessRequestService tempAccessRequestService;

    // ==================== 门禁权限管理 ====================

    /**
     * 查询门禁权限列表
     */
    @Operation(summary = "查询门禁权限列表")
    @GetMapping("/permission/list")
    public Result<List<DoorAccessPermission>> getPermissionList(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Integer status) {

        List<DoorAccessPermission> list = doorAccessPermissionService.getPermissionList(roomId, status);
        return Result.success(list);
    }

    /**
     * 查询值班人员的门禁权限
     */
    @Operation(summary = "查询值班人员的门禁权限")
    @GetMapping("/permission/staff/{staffId}")
    public Result<List<DoorAccessPermission>> getStaffPermissions(@PathVariable String staffId) {
        List<DoorAccessPermission> list = doorAccessPermissionService.getActivePermissionsByStaff(staffId);
        return Result.success(list);
    }

    /**
     * 回收门禁权限
     */
    @Operation(summary = "回收门禁权限")
    @PostMapping("/permission/revoke/{id}")
    public Result<Boolean> revokePermission(
            @PathVariable Long id,
            @RequestParam String reason) {

        boolean success = doorAccessPermissionService.revokePermission(id, reason);
        return success ? Result.success(true) : Result.error("回收门禁权限失败");
    }

    // ==================== 临时权限申请 ====================

    /**
     * 创建临时权限申请
     */
    @Operation(summary = "创建临时权限申请")
    @PostMapping("/temp-request/create")
    public Result<Boolean> createTempRequest(@RequestBody TempAccessRequest request) {
        boolean success = tempAccessRequestService.createRequest(request);
        return success ? Result.success(true) : Result.error("创建临时权限申请失败");
    }

    /**
     * 查询临时权限申请列表
     */
    @Operation(summary = "查询临时权限申请列表")
    @GetMapping("/temp-request/list")
    public Result<List<TempAccessRequest>> getRequestList(
            @RequestParam(required = false) Integer approvalStatus) {

        List<TempAccessRequest> list = tempAccessRequestService.getRequestList(approvalStatus);
        return Result.success(list);
    }

    /**
     * 查询临时权限申请详情
     */
    @Operation(summary = "查询临时权限申请详情")
    @GetMapping("/temp-request/{id}")
    public Result<TempAccessRequest> getRequestDetail(@PathVariable Long id) {
        TempAccessRequest request = tempAccessRequestService.getById(id);
        return Result.success(request);
    }

    /**
     * 审批临时权限申请
     */
    @Operation(summary = "审批临时权限申请")
    @PostMapping("/temp-request/approve/{id}")
    public Result<Boolean> approveRequest(
            @PathVariable Long id,
            @RequestParam Long approverId,
            @RequestParam String approverName,
            @RequestParam Integer approvalStatus,
            @RequestParam(required = false) String comment) {

        boolean success = tempAccessRequestService.approveRequest(
                id, approverId, approverName, approvalStatus, comment);
        return success ? Result.success(true) : Result.error("审批失败");
    }

    /**
     * 撤销临时权限申请
     */
    @Operation(summary = "撤销临时权限申请")
    @PostMapping("/temp-request/revoke/{id}")
    public Result<Boolean> revokeRequest(
            @PathVariable Long id,
            @RequestParam String reason) {

        boolean success = tempAccessRequestService.revokeRequest(id, reason);
        return success ? Result.success(true) : Result.error("撤销失败");
    }
}
