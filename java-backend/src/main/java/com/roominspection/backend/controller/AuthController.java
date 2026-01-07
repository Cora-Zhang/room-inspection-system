package com.roominspection.backend.controller;

import com.roominspection.backend.common.Result;
import com.roominspection.backend.dto.LoginRequest;
import com.roominspection.backend.dto.LoginResponse;
import com.roominspection.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录（支持OAuth2.0 SSO）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * OAuth2.0 SSO登录回调
     */
    @GetMapping("/callback/{provider}")
    public Result<LoginResponse> oauthCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state) {
        LoginResponse response = authService.oauthCallback(provider, code, state);
        return Result.success(response);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestBody String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/info")
    public Result<LoginResponse.UserInfo> getCurrentUser() {
        LoginResponse.UserInfo userInfo = authService.getCurrentUser();
        return Result.success(userInfo);
    }
}
