package com.roominspection.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.Permission;
import com.roominspection.backend.mapper.PermissionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务
 */
@Service
public class PermissionService extends ServiceImpl<PermissionMapper, Permission> {

    /**
     * 创建权限
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createPermission(Permission permission, String createdBy) {
        permission.setCreatedBy(createdBy);
        return save(permission);
    }

    /**
     * 更新权限
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(Permission permission, String updatedBy) {
        permission.setUpdatedBy(updatedBy);
        return updateById(permission);
    }

    /**
     * 删除权限
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(Long permissionId) {
        // 递归删除子权限
        deletePermissionAndChildren(permissionId);
        return true;
    }

    /**
     * 递归删除权限及其子权限
     */
    private void deletePermissionAndChildren(Long permissionId) {
        // 查询子权限
        List<Permission> children = list(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getParentId, permissionId));

        // 递归删除子权限
        for (Permission child : children) {
            deletePermissionAndChildren(child.getId());
        }

        // 删除当前权限
        removeById(permissionId);
    }

    /**
     * 获取权限树
     */
    public List<Permission> getPermissionTree() {
        // 查询所有权限
        List<Permission> allPermissions = list(new LambdaQueryWrapper<Permission>()
                .orderByAsc(Permission::getSortOrder));

        // 构建树形结构
        return buildPermissionTree(allPermissions, 0L);
    }

    /**
     * 构建权限树
     */
    private List<Permission> buildPermissionTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
                .filter(permission -> permission.getParentId().equals(parentId))
                .peek(permission -> {
                    List<Permission> children = buildPermissionTree(permissions, permission.getId());
                    permission.setChildren(children);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据类型获取权限
     */
    public List<Permission> getPermissionsByType(String type) {
        return list(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getType, type)
                .eq(Permission::getStatus, "ACTIVE")
                .orderByAsc(Permission::getSortOrder));
    }

    /**
     * 检查权限编码是否存在
     */
    public boolean existsByPermissionCode(String permissionCode) {
        return count(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermissionCode, permissionCode)) > 0;
    }

    /**
     * 获取菜单权限（用于前端路由）
     */
    public List<Permission> getMenuPermissions() {
        return list(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getType, "MENU")
                .eq(Permission::getStatus, "ACTIVE")
                .eq(Permission::getVisible, true)
                .orderByAsc(Permission::getSortOrder));
    }
}
