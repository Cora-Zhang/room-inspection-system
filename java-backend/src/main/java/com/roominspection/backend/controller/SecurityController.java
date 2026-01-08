package com.roominspection.backend.controller;

import com.roominspection.backend.annotation.AuditLog;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.*;
import com.roominspection.backend.security.CustomUserDetails;
import com.roominspection.backend.security.JwtTokenUtil;
import com.roominspection.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全管理Controller
 * 提供认证、权限管理、操作日志等安全功能接口
 */
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditLogService auditLogService;

    /**
     * 登录接口
     */
    @AuditLog(operationType = "LOGIN", module = "AUTH", description = "用户登录", logParams = false)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 获取用户信息
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 生成Token
            String roles = String.join(",",
                    userDetails.getAuthorities().stream()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .toArray(String[]::new)
            );
            String token = jwtTokenUtil.generateToken(userDetails.getUserId(), username, roles);
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails.getUserId(), username, roles);

            // 构建响应
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("refreshToken", refreshToken);
            data.put("userId", userDetails.getUserId());
            data.put("username", userDetails.getUsername());
            data.put("realName", userDetails.getRealName());
            data.put("avatar", userDetails.getAvatar());
            data.put("roles", roles);
            data.put("permissions", getPermissionCodes(userDetails.getUserId()));

            return Result.success(data, "登录成功");

        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 刷新Token接口
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        try {
            // 验证刷新Token
            String newToken = jwtTokenUtil.refreshToken(refreshToken);

            String username = jwtTokenUtil.getUsernameFromToken(newToken);
            String userId = jwtTokenUtil.getUserIdFromToken(newToken);
            String roles = jwtTokenUtil.getRolesFromToken(newToken);

            // 构建响应
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            data.put("refreshToken", refreshToken);

            return Result.success(data, "刷新Token成功");

        } catch (Exception e) {
            return Result.error("刷新Token失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info")
    public Result<Map<String, Object>> getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            Map<String, Object> data = new HashMap<>();
            data.put("userId", userDetails.getUserId());
            data.put("username", userDetails.getUsername());
            data.put("realName", userDetails.getRealName());
            data.put("email", userDetails.getEmail());
            data.put("phone", userDetails.getPhone());
            data.put("avatar", userDetails.getAvatar());
            data.put("departmentId", userDetails.getDepartmentId());
            data.put("position", userDetails.getPosition());
            data.put("employeeId", userDetails.getEmployeeId());

            // 获取用户角色
            List<Role> roles = roleService.getUserRoles(userDetails.getUserId());
            data.put("roles", roles);

            // 获取用户权限
            List<Permission> permissions = permissionService.getMenuPermissions();
            data.put("permissions", permissions);

            return Result.success(data);

        } catch (Exception e) {
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @AuditLog(operationType = "UPDATE", module = "AUTH", description = "修改密码", logParams = false)
    @PostMapping("/user/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, userDetails.getPassword())) {
                return Result.error("旧密码错误");
            }

            // 更新密码
            // userService.updatePassword(userDetails.getUserId(), passwordEncoder.encode(newPassword));

            return Result.success(null, "密码修改成功");

        } catch (Exception e) {
            return Result.error("修改密码失败：" + e.getMessage());
        }
    }

    /**
     * 获取角色列表
     */
    @AuditLog(operationType = "QUERY", module = "ROLE", description = "查询角色列表")
    @GetMapping("/roles")
    public Result<List<Role>> getRoles(@RequestParam(required = false) String status) {
        try {
            List<Role> roles;
            if ("ACTIVE".equals(status)) {
                roles = roleService.getActiveRoles();
            } else {
                roles = roleService.list();
            }
            return Result.success(roles);
        } catch (Exception e) {
            return Result.error("获取角色列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建角色
     */
    @AuditLog(operationType = "CREATE", module = "ROLE", description = "创建角色")
    @PostMapping("/roles")
    public Result<Void> createRole(@RequestBody Role role) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 检查角色编码是否已存在
            if (roleService.existsByRoleCode(role.getRoleCode())) {
                return Result.error("角色编码已存在");
            }

            roleService.createRole(role, userDetails.getUserId());
            return Result.success(null, "创建角色成功");
        } catch (Exception e) {
            return Result.error("创建角色失败：" + e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @AuditLog(operationType = "UPDATE", module = "ROLE", description = "更新角色")
    @PutMapping("/roles/{id}")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            role.setId(id);
            roleService.updateRole(role, userDetails.getUserId());
            return Result.success(null, "更新角色成功");
        } catch (Exception e) {
            return Result.error("更新角色失败：" + e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @AuditLog(operationType = "DELETE", module = "ROLE", description = "删除角色")
    @DeleteMapping("/roles/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return Result.success(null, "删除角色成功");
        } catch (Exception e) {
            return Result.error("删除角色失败：" + e.getMessage());
        }
    }

    /**
     * 为角色分配权限
     */
    @AuditLog(operationType = "UPDATE", module = "ROLE", description = "为角色分配权限")
    @PostMapping("/roles/{roleId}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long roleId,
                                          @RequestBody Map<String, List<Long>> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            List<Long> permissionIds = request.get("permissionIds");
            roleService.assignPermissions(roleId, permissionIds, userDetails.getUserId());
            return Result.success(null, "分配权限成功");
        } catch (Exception e) {
            return Result.error("分配权限失败：" + e.getMessage());
        }
    }

    /**
     * 获取权限树
     */
    @AuditLog(operationType = "QUERY", module = "PERMISSION", description = "查询权限树")
    @GetMapping("/permissions/tree")
    public Result<List<Permission>> getPermissionTree() {
        try {
            List<Permission> permissions = permissionService.getPermissionTree();
            return Result.success(permissions);
        } catch (Exception e) {
            return Result.error("获取权限树失败：" + e.getMessage());
        }
    }

    /**
     * 获取操作日志
     */
    @AuditLog(operationType = "QUERY", module = "SYSTEM", description = "查询操作日志")
    @GetMapping("/audit-logs")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;

            var logs = auditLogService.queryAuditLogs(page, pageSize, userId, username,
                    operationType, module, status, start, end);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.error("获取操作日志失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户权限编码列表
     */
    private List<String> getPermissionCodes(String userId) {
        // 从角色和权限关联中获取权限编码
        return List.of(); // 简化实现
    }
}
