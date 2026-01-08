package com.roominspection.backend.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.roominspection.backend.entity.User;
import com.roominspection.backend.entity.UserRole;
import com.roominspection.backend.mapper.UserMapper;
import com.roominspection.backend.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义UserDetailsService
 * 用于从数据库加载用户信息和权限
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getStatus, "ACTIVE")
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 检查用户是否被锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new UsernameNotFoundException("账户已被锁定，请稍后再试");
        }

        // 查询用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, user.getId())
        );

        // 构建权限列表（角色列表）
        List<String> roles = userRoles.stream()
                .map(ur -> "ROLE_" + ur.getRoleId())
                .collect(Collectors.toList());

        return CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .departmentId(user.getDepartmentId())
                .position(user.getPosition())
                .employeeId(user.getEmployeeId())
                .status(user.getStatus())
                .authorities(roles)
                .build();
    }

    /**
     * 根据用户ID加载用户信息
     */
    public UserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
        // 查询用户
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new UsernameNotFoundException("账户已被锁定，请稍后再试");
        }

        // 查询用户角色
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, user.getId())
        );

        // 构建权限列表（角色列表）
        List<String> roles = userRoles.stream()
                .map(ur -> "ROLE_" + ur.getRoleId())
                .collect(Collectors.toList());

        return CustomUserDetails.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .departmentId(user.getDepartmentId())
                .position(user.getPosition())
                .employeeId(user.getEmployeeId())
                .status(user.getStatus())
                .authorities(roles)
                .build();
    }
}
