package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.UserDataCenterPermission;

import java.util.List;

/**
 * 用户数据中心权限管理服务接口
 */
public interface UserDataCenterPermissionService {

    /**
     * 分页查询用户权限列表
     * @param page 页码
     * @param size 每页数量
     * @param userId 用户ID（可选）
     * @param datacenterId 数据中心ID（可选）
     * @return 分页结果
     */
    Page<UserDataCenterPermission> listPermissions(Integer page, Integer size, Long userId, Long datacenterId);

    /**
     * 获取用户在指定数据中心的权限
     * @param userId 用户ID
     * @param datacenterId 数据中心ID
     * @return 权限信息
     */
    UserDataCenterPermission getUserPermission(Long userId, Long datacenterId);

    /**
     * 获取用户所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<UserDataCenterPermission> getUserPermissions(Long userId);

    /**
     * 分配用户权限
     * @param permission 权限信息
     * @return 分配是否成功
     */
    boolean grantPermission(UserDataCenterPermission permission);

    /**
     * 更新用户权限
     * @param permission 权限信息
     * @return 更新是否成功
     */
    boolean updatePermission(UserDataCenterPermission permission);

    /**
     * 撤销用户权限
     * @param id 权限ID
     * @return 撤销是否成功
     */
    boolean revokePermission(Long id);

    /**
     * 批量分配权限
     * @param userIds 用户ID列表
     * @param datacenterId 数据中心ID
     * @param permissions 权限列表
     * @return 分配是否成功
     */
    boolean batchGrantPermissions(List<Long> userIds, Long datacenterId, List<String> permissions);

    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param datacenterId 数据中心ID
     * @param permission 权限名称
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, Long datacenterId, String permission);

    /**
     * 获取用户可访问的数据中心列表
     * @param userId 用户ID
     * @return 数据中心ID列表
     */
    List<Long> getUserDataCenterIds(Long userId);

    /**
     * 获取用户在指定数据中心可访问的机房列表
     * @param userId 用户ID
     * @param datacenterId 数据中心ID
     * @return 机房ID列表
     */
    List<Long> getUserRoomIds(Long userId, Long datacenterId);

    /**
     * 获取用户在指定数据中心可访问的设备列表
     * @param userId 用户ID
     * @param datacenterId 数据中心ID
     * @return 设备ID列表
     */
    List<Long> getUserDeviceIds(Long userId, Long datacenterId);

    /**
     * 复制权限
     * @param sourceUserId 源用户ID
     * @param targetUserIds 目标用户ID列表
     * @return 复制是否成功
     */
    boolean copyPermissions(Long sourceUserId, List<Long> targetUserIds);
}
