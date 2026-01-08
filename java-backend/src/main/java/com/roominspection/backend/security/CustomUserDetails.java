package com.roominspection.backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义UserDetails
 * 扩展Spring Security的UserDetails，添加更多用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private String userId;
    private String username;
    private String password;
    private String email;
    private String realName;
    private String phone;
    private String avatar;
    private String departmentId;
    private String position;
    private String employeeId;
    private String status;

    /**
     * 权限列表（角色列表）
     */
    @Builder.Default
    private List<String> authorities = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"LOCKED".equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }

    /**
     * 获取角色ID列表
     */
    public List<Long> getRoleIds() {
        return authorities.stream()
                .map(auth -> auth.replace("ROLE_", ""))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
