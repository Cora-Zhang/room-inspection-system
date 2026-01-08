package com.roominspection.backend.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.roominspection.backend.entity.IAMUser;
import com.roominspection.backend.entity.Job;
import com.roominspection.backend.entity.Organization;
import com.roominspection.backend.entity.SSOConfig;
import com.roominspection.backend.mapper.SSOConfigMapper;
import com.roominspection.backend.service.DataSyncService;
import com.roominspection.backend.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据同步控制器
 * 接收IAM系统推送的组织、用户、职位数据
 */
@RestController
@RequestMapping("/api/sync")
public class DataSyncController {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncController.class);

    @Autowired
    private DataSyncService dataSyncService;

    @Autowired
    private SSOConfigMapper ssoConfigMapper;

    /**
     * 组织数据同步接口
     *
     * @param orgData      组织数据
     * @param accessToken  访问令牌（JWT）
     * @return 同步结果
     */
    @PostMapping("/org")
    public ResponseEntity<Map<String, Object>> syncOrganization(
            @RequestBody Organization orgData,
            @RequestParam String accessToken) {

        try {
            logger.info("Received organization sync request: {}", orgData.getOrgCode());

            // JWT Token鉴权
            verifyJwtToken(accessToken);

            // 同步组织数据
            Map<String, Object> result = dataSyncService.syncOrganization(orgData);

            return ResponseEntity.ok(result);

        } catch (JWTVerificationException e) {
            logger.error("JWT verification failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "-1");
            error.put("msg", "Token验证失败");
            return ResponseEntity.status(401).body(error);
        } catch (Exception e) {
            logger.error("Sync organization error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "-1");
            error.put("msg", "同步失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 用户数据同步接口
     *
     * @param userData     用户数据
     * @param accessToken  访问令牌（JWT）
     * @return 同步结果
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> syncUser(
            @RequestBody IAMUser userData,
            @RequestParam String accessToken) {

        try {
            logger.info("Received user sync request: {}", userData.getAccountNo());

            // JWT Token鉴权
            verifyJwtToken(accessToken);

            // 同步用户数据
            Map<String, Object> result = dataSyncService.syncUser(userData);

            return ResponseEntity.ok(result);

        } catch (JWTVerificationException e) {
            logger.error("JWT verification failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "-1");
            error.put("msg", "Token验证失败");
            return ResponseEntity.status(401).body(error);
        } catch (Exception e) {
            logger.error("Sync user error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "-1");
            error.put("msg", "同步失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 职位数据同步接口
     *
     * @param jobData      职位数据
     * @param accessToken  访问令牌（JWT）
     * @return 同步结果
     */
    @PostMapping("/job")
    public ResponseEntity<Map<String, Object>> syncJob(
            @RequestBody Job jobData,
            @RequestParam String accessToken) {

        try {
            logger.info("Received job sync request: {}", jobData.getCode());

            // JWT Token鉴权
            verifyJwtToken(accessToken);

            // 同步职位数据
            Map<String, Object> result = dataSyncService.syncJob(jobData);

            return ResponseEntity.ok(result);

        } catch (JWTVerificationException e) {
            logger.error("JWT verification failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "-1");
            error.put("msg", "Token验证失败");
            return ResponseEntity.status(401).body(error);
        } catch (Exception e) {
            logger.error("Sync job error", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", "-1");
            error.put("msg", "同步失败: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * 验证JWT Token
     */
    private void verifyJwtToken(String accessToken) {
        // 获取启用的SSO配置
        SSOConfig ssoConfig = getEnabledSSOConfig();
        if (ssoConfig == null || ssoConfig.getJwtSecret() == null || ssoConfig.getJwtSecret().isEmpty()) {
            throw new JWTVerificationException("SSO配置不存在或JWT Secret未配置");
        }

        // 验证Token
        int jwtTimeOffset = ssoConfig.getJwtTimeOffset() != null ? ssoConfig.getJwtTimeOffset() : 60;
        JwtUtil.verifyToken(accessToken, ssoConfig.getJwtSecret(), jwtTimeOffset);
    }

    /**
     * 获取启用的SSO配置
     */
    private SSOConfig getEnabledSSOConfig() {
        LambdaQueryWrapper<SSOConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SSOConfig::getEnabled, "Y")
                .orderByDesc(SSOConfig::getCreatedAt)
                .last("LIMIT 1");
        return ssoConfigMapper.selectOne(wrapper);
    }
}
