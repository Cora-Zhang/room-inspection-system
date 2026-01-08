package com.roominspection.backend.controller;

import com.roominspection.backend.entity.SSOConfig;
import com.roominspection.backend.entity.User;
import com.roominspection.backend.service.SSOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * SSO单点登录控制器
 */
@RestController
@RequestMapping("/api/sso")
public class SSOController {

    private static final Logger logger = LoggerFactory.getLogger(SSOController.class);

    @Autowired
    private SSOService ssoService;

    /**
     * 获取SSO登录URL
     *
     * @param ssoConfigId SSO配置ID（可选，不传则使用默认启用的配置）
     * @param state      状态参数（可选）
     * @return SSO登录URL
     */
    @GetMapping("/login-url")
    public ResponseEntity<Map<String, Object>> getSsoLoginUrl(
            @RequestParam(required = false) String ssoConfigId,
            @RequestParam(required = false) String state) {

        try {
            SSOConfig ssoConfig = ssoService.getEnabledSSOConfig();
            if (ssoConfig == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未找到启用的SSO配置");
                return ResponseEntity.badRequest().body(result);
            }

            String actualSsoConfigId = ssoConfigId != null ? ssoConfigId : ssoConfig.getId();
            String loginUrl = ssoService.generateSsoLoginUrl(actualSsoConfigId, state);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", loginUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Get SSO login URL error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取SSO登录URL失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * SSO登录回调接口
     * IAM认证成功后会重定向到这个接口，携带code参数
     *
     * @param code  授权码
     * @param state 状态参数
     * @param response HttpServletResponse
     */
    @GetMapping("/callback")
    public void ssoCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) {

        try {
            logger.info("SSO callback received, code: {}, state: {}", code, state);

            // 通过SSO登录用户
            User user = ssoService.loginBySso(code, state);

            // 重定向到前端首页，携带token
            String redirectUrl = "/?sso=success&username=" + user.getUsername();
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            logger.error("SSO callback error", e);
            try {
                String redirectUrl = "/login?error=" + e.getMessage();
                response.sendRedirect(redirectUrl);
            } catch (IOException ex) {
                logger.error("Redirect error", ex);
            }
        }
    }

    /**
     * 获取SSO退出URL
     *
     * @param ssoConfigId SSO配置ID（可选）
     * @param redirectUrl 退出后跳转地址（可选）
     * @return SSO退出URL
     */
    @GetMapping("/logout-url")
    public ResponseEntity<Map<String, Object>> getSsoLogoutUrl(
            @RequestParam(required = false) String ssoConfigId,
            @RequestParam(required = false) String redirectUrl) {

        try {
            SSOConfig ssoConfig = ssoService.getEnabledSSOConfig();
            if (ssoConfig == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未找到启用的SSO配置");
                return ResponseEntity.badRequest().body(result);
            }

            String logoutUrl;
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                logoutUrl = ssoService.getLogoutUrl(ssoConfig, redirectUrl);
            } else {
                logoutUrl = ssoService.getLogoutUrl(ssoConfig);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", logoutUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Get SSO logout URL error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取SSO退出URL失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 会话保持
     *
     * @param accessToken 访问令牌
     * @return 会话信息
     */
    @PostMapping("/keep-session")
    public ResponseEntity<Map<String, Object>> keepSession(@RequestParam String accessToken) {
        try {
            SSOConfig ssoConfig = ssoService.getEnabledSSOConfig();
            if (ssoConfig == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未找到启用的SSO配置");
                return ResponseEntity.badRequest().body(result);
            }

            Map<String, Object> sessionInfo = ssoService.keepSession(accessToken, ssoConfig);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", sessionInfo);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Keep session error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "会话保持失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 获取SSO配置信息
     *
     * @return SSO配置信息（不包含敏感信息）
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getSsoConfig() {
        try {
            SSOConfig ssoConfig = ssoService.getEnabledSSOConfig();
            if (ssoConfig == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "未找到启用的SSO配置");
                return ResponseEntity.badRequest().body(result);
            }

            // 移除敏感信息
            SSOConfig safeConfig = new SSOConfig();
            safeConfig.setId(ssoConfig.getId());
            safeConfig.setConfigName(ssoConfig.getConfigName());
            safeConfig.setEnabled(ssoConfig.getEnabled());
            safeConfig.setAuthServerUrl(ssoConfig.getAuthServerUrl());
            safeConfig.setRedirectUri(ssoConfig.getRedirectUri());
            safeConfig.setEntryUrl(ssoConfig.getEntryUrl());
            safeConfig.setProtocolType(ssoConfig.getProtocolType());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", safeConfig);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Get SSO config error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取SSO配置失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
