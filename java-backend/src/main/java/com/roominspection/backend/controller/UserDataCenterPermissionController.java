package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.ApiVersion;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.UserDataCenterPermission;
import com.roominspection.backend.service.UserDataCenterPermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户数据中心权限管理控制器
 */
@RestController
@RequestMapping("/api/v1/permissions")
@Api(tags = "用户权限管理")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserDataCenterPermissionController {

    @Autowired
    private UserDataCenterPermissionService permissionService;

    /**
     * 分页查询用户权限列表
     */
    @GetMapping
    @ApiOperation(value = "分页查询用户权限列表", notes = "支持按用户ID和数据中心ID筛选")
    @ApiVersion("v1")
    public Result<Page<UserDataCenterPermission>> listPermissions(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "用户ID") @RequestParam(required = false) Long userId,
            @ApiParam(value = "数据中心ID") @RequestParam(required = false) Long datacenterId) {
        Page<UserDataCenterPermission> result = permissionService.listPermissions(page, size, userId, datacenterId);
        return Result.page(result);
    }

    /**
     * 获取用户在指定数据中心的权限
     */
    @GetMapping("/user/{userId}/datacenter/{datacenterId}")
    @ApiOperation(value = "获取用户数据中心权限", notes = "查询用户在指定数据中心的权限")
    @ApiVersion("v1")
    public Result<UserDataCenterPermission> getUserPermission(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "数据中心ID", required = true) @PathVariable Long datacenterId) {
        UserDataCenterPermission permission = permissionService.getUserPermission(userId, datacenterId);
        if (permission == null) {
            return Result.error("权限不存在");
        }
        return Result.success(permission);
    }

    /**
     * 获取用户所有权限
     */
    @GetMapping("/user/{userId}")
    @ApiOperation(value = "获取用户所有权限", notes = "查询用户的所有数据中心权限")
    @ApiVersion("v1")
    public Result<List<UserDataCenterPermission>> getUserPermissions(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        List<UserDataCenterPermission> permissions = permissionService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    /**
     * 分配用户权限
     */
    @PostMapping
    @ApiOperation(value = "分配用户权限", notes = "为用户分配数据中心访问权限")
    @ApiVersion("v1")
    public Result<String> grantPermission(
            @ApiParam(value = "权限信息", required = true) @RequestBody UserDataCenterPermission permission) {
        boolean success = permissionService.grantPermission(permission);
        return success ? Result.success("分配成功") : Result.error("分配失败");
    }

    /**
     * 更新用户权限
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "更新用户权限", notes = "更新用户的数据中心权限")
    @ApiVersion("v1")
    public Result<String> updatePermission(
            @ApiParam(value = "权限ID", required = true) @PathVariable Long id,
            @ApiParam(value = "权限信息", required = true) @RequestBody UserDataCenterPermission permission) {
        permission.setId(id);
        boolean success = permissionService.updatePermission(permission);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 撤销用户权限
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "撤销用户权限", notes = "撤销用户的数据中心权限")
    @ApiVersion("v1")
    public Result<String> revokePermission(
            @ApiParam(value = "权限ID", required = true) @PathVariable Long id) {
        boolean success = permissionService.revokePermission(id);
        return success ? Result.success("撤销成功") : Result.error("撤销失败");
    }

    /**
     * 批量分配权限
     */
    @PostMapping("/batch")
    @ApiOperation(value = "批量分配权限", notes="为多个用户分配相同的数据中心权限")
    @ApiVersion("v1")
    public Result<String> batchGrantPermissions(
            @ApiParam(value = "用户ID列表", required = true) @RequestBody List<Long> userIds,
            @ApiParam(value = "数据中心ID", required = true) @RequestParam Long datacenterId,
            @ApiParam(value = "权限列表", required = true) @RequestBody List<String> permissions) {
        boolean success = permissionService.batchGrantPermissions(userIds, datacenterId, permissions);
        return success ? Result.success("批量分配成功") : Result.error("批量分配失败");
    }

    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/check")
    @ApiOperation(value = "检查用户权限", notes = "检查用户是否有指定数据中心的指定权限")
    @ApiVersion("v1")
    public Result<Boolean> hasPermission(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "数据中心ID", required = true) @RequestParam Long datacenterId,
            @ApiParam(value = "权限名称", required = true) @RequestParam String permission) {
        boolean hasPermission = permissionService.hasPermission(userId, datacenterId, permission);
        return Result.success(hasPermission);
    }

    /**
     * 获取用户可访问的数据中心列表
     */
    @GetMapping("/user/{userId}/datacenters")
    @ApiOperation(value = "获取用户可访问的数据中心", notes = "查询用户可访问的所有数据中心")
    @ApiVersion("v1")
    public Result<List<Long>> getUserDataCenterIds(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        List<Long> dataCenterIds = permissionService.getUserDataCenterIds(userId);
        return Result.success(dataCenterIds);
    }

    /**
     * 获取用户在指定数据中心可访问的机房列表
     */
    @GetMapping("/user/{userId}/datacenter/{datacenterId}/rooms")
    @ApiOperation(value = "获取用户可访问的机房", notes = "查询用户在指定数据中心可访问的机房")
    @ApiVersion("v1")
    public Result<List<Long>> getUserRoomIds(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "数据中心ID", required = true) @PathVariable Long datacenterId) {
        List<Long> roomIds = permissionService.getUserRoomIds(userId, datacenterId);
        return Result.success(roomIds);
    }

    /**
     * 获取用户在指定数据中心可访问的设备列表
     */
    @GetMapping("/user/{userId}/datacenter/{datacenterId}/devices")
    @ApiOperation(value = "获取用户可访问的设备", notes = "查询用户在指定数据中心可访问的设备")
    @ApiVersion("v1")
    public Result<List<Long>> getUserDeviceIds(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "数据中心ID", required = true) @PathVariable Long datacenterId) {
        List<Long> deviceIds = permissionService.getUserDeviceIds(userId, datacenterId);
        return Result.success(deviceIds);
    }

    /**
     * 复制权限
     */
    @PostMapping("/copy")
    @ApiOperation(value = "复制权限", notes = "将源用户的权限复制给目标用户")
    @ApiVersion("v1")
    public Result<String> copyPermissions(
            @ApiParam(value = "源用户ID", required = true) @RequestParam Long sourceUserId,
            @ApiParam(value = "目标用户ID列表", required = true) @RequestBody List<Long> targetUserIds) {
        boolean success = permissionService.copyPermissions(sourceUserId, targetUserIds);
        return success ? Result.success("复制成功") : Result.error("复制失败");
    }
}
