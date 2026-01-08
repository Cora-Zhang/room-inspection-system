package com.roominspection.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * 从请求头中提取JWT Token并验证，设置认证信息到Spring Security上下文
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 从请求头中获取Token
            String jwt = extractJwtFromRequest(request);

            // 验证Token并设置认证信息
            if (StringUtils.hasText(jwt) && !jwtTokenUtil.isTokenExpired(jwt)) {
                String username = jwtTokenUtil.getUsernameFromToken(jwt);
                String userId = jwtTokenUtil.getUserIdFromToken(jwt);
                String roles = jwtTokenUtil.getRolesFromToken(jwt);

                // 构建用户权限列表
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (StringUtils.hasText(roles)) {
                    for (String role : roles.split(",")) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }

                // 创建认证对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证信息到Spring Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("无法设置用户认证: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT Token
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
