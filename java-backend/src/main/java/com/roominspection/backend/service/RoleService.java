package com.roominspection.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.Permission;
import com.roominspection.backend.entity.Role;
import com.roominspection.backend.entity.RolePermission;
import com.roominspection.backend.entity.UserRole;
import com.roominspection.backend.mapper.RoleMapper;
import com.roominspection.backend.mapper.RolePermissionMapper;
import com.roominspection.backend.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务
 */
@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    /**
     * 创建角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(Role role, String createdBy) {
        role.setCreatedBy(createdBy);
        return save(role);
    }

    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Role role, String updatedBy) {
        role.setUpdatedBy(updatedBy);
        return updateById(role);
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long roleId) {
        // 先删除角色权限关联
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));

        // 再删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getRoleId, roleId));

        // 最后删除角色
        return removeById(roleId);
    }

    /**
     * 为角色分配权限
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds, String createdBy) {
        // 先删除原有权限
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId));

        // 再添加新权限
        for (Long permissionId : permissionIds) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRoleId(roleId);
            rolePermission.setPermissionId(permissionId);
            rolePermission.setCreatedBy(createdBy);
            rolePermissionMapper.insert(rolePermission);
        }

        return true;
    }

    /**
     * 获取角色的权限列表
     */
    public List<Permission> getRolePermissions(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
        );

        if (rolePermissions.isEmpty()) {
            return List.of();
        }

        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 假设有PermissionService
        // return permissionService.listByIds(permissionIds);
        return List.of();
    }

    /**
     * 获取用户的角色列表
     */
    public List<Role> getUserRoles(String userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
        );

        if (userRoles.isEmpty()) {
            return List.of();
        }

        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        return listByIds(roleIds);
    }

    /**
     * 为用户分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(String userId, List<Long> roleIds, String createdBy) {
        // 先删除原有角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));

        // 再添加新角色
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreatedBy(createdBy);
            userRoleMapper.insert(userRole);
        }

        return true;
    }

    /**
     * 检查角色是否存在
     */
    public boolean existsByRoleCode(String roleCode) {
        return count(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, roleCode)) > 0;
    }

    /**
     * 获取所有激活的角色
     */
    public List<Role> getActiveRoles() {
        return list(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, "ACTIVE")
                .orderByAsc(Role::getSortOrder));
    }
}
