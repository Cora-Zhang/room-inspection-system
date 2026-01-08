package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roominspection.backend.entity.SSOConfig;
import com.roominspection.backend.entity.User;
import com.roominspection.backend.mapper.SSOConfigMapper;
import com.roominspection.backend.mapper.UserMapper;
import com.roominspection.backend.service.SSOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SSO服务实现类
 */
@Service
public class SSOServiceImpl implements SSOService {

    private static final Logger logger = LoggerFactory.getLogger(SSOServiceImpl.class);

    @Autowired
    private SSOConfigMapper ssoConfigMapper;

    @Autowired
    private UserMapper userMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateSsoLoginUrl(String ssoConfigId, String state) {
        SSOConfig ssoConfig = getSSOConfigById(ssoConfigId);
        if (ssoConfig == null) {
            throw new RuntimeException("SSO配置不存在");
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(ssoConfig.getAuthServerUrl())
                .append(ssoConfig.getAuthorizeEndpoint())
                .append("?client_id=").append(ssoConfig.getClientId())
                .append("&response_type=code")
                .append("&redirect_uri=").append(encodeUrl(ssoConfig.getRedirectUri()))
                .append("&oauth_timestamp=").append(System.currentTimeMillis());

        if (state != null && !state.isEmpty()) {
            urlBuilder.append("&state=").append(encodeUrl(state));
        }

        return urlBuilder.toString();
    }

    @Override
    public SSOConfig getEnabledSSOConfig() {
        LambdaQueryWrapper<SSOConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SSOConfig::getEnabled, "Y")
                .orderByDesc(SSOConfig::getCreatedAt)
                .last("LIMIT 1");
        return ssoConfigMapper.selectOne(wrapper);
    }

    @Override
    public Map<String, Object> getAccessToken(String code, SSOConfig ssoConfig) {
        try {
            String url = ssoConfig.getAuthServerUrl() + ssoConfig.getTokenEndpoint();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("oauth_timestamp", String.valueOf(System.currentTimeMillis()));
            params.add("client_id", ssoConfig.getClientId());
            params.add("client_secret", ssoConfig.getClientSecret());
            params.add("code", code);
            params.add("redirect_uri", ssoConfig.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            logger.info("Requesting access token from IAM: {}", url);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                logger.info("Access token response: {}", responseBody);

                JsonNode jsonNode = objectMapper.readTree(responseBody);

                Map<String, Object> result = new HashMap<>();
                result.put("access_token", jsonNode.get("access_token").asText());
                if (jsonNode.has("token_type")) {
                    result.put("token_type", jsonNode.get("token_type").asText());
                }
                if (jsonNode.has("expires_in")) {
                    result.put("expires_in", jsonNode.get("expires_in").asInt());
                }
                if (jsonNode.has("refresh_token")) {
                    result.put("refresh_token", jsonNode.get("refresh_token").asText());
                }

                return result;
            } else {
                logger.error("Failed to get access token, status: {}", response.getStatusCode());
                throw new RuntimeException("获取访问令牌失败");
            }
        } catch (Exception e) {
            logger.error("Get access token error", e);
            throw new RuntimeException("获取访问令牌失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken, SSOConfig ssoConfig) {
        try {
            String url = ssoConfig.getAuthServerUrl() + ssoConfig.getUserInfoEndpoint();
            url = url + "?access_token=" + accessToken;

            logger.info("Requesting user info from IAM: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                logger.info("User info response: {}", responseBody);

                JsonNode jsonNode = objectMapper.readTree(responseBody);

                Map<String, Object> result = new HashMap<>();

                if (jsonNode.has("id")) {
                    result.put("id", jsonNode.get("id").asText());
                }
                if (jsonNode.has("attributes")) {
                    JsonNode attributes = jsonNode.get("attributes");
                    if (attributes.has("accountNo")) {
                        result.put("accountNo", attributes.get("accountNo").asText());
                    }
                    if (attributes.has("userName")) {
                        result.put("userName", attributes.get("userName").asText());
                    }
                    if (attributes.has("token_expired")) {
                        result.put("token_expired", attributes.get("token_expired").asText());
                    }
                    if (attributes.has("token_gtime")) {
                        result.put("token_gtime", attributes.get("token_gtime").asLong());
                    }
                }

                return result;
            } else {
                logger.error("Failed to get user info, status: {}", response.getStatusCode());
                throw new RuntimeException("获取用户信息失败");
            }
        } catch (Exception e) {
            logger.error("Get user info error", e);
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public User loginBySso(String code, String state) {
        SSOConfig ssoConfig = getEnabledSSOConfig();
        if (ssoConfig == null) {
            throw new RuntimeException("未启用的SSO配置");
        }

        // 1. 使用授权码获取访问令牌
        Map<String, Object> tokenInfo = getAccessToken(code, ssoConfig);
        String accessToken = (String) tokenInfo.get("access_token");

        // 2. 使用访问令牌获取用户信息
        Map<String, Object> userInfo = getUserInfo(accessToken, ssoConfig);

        // 3. 根据用户信息创建或更新本地用户
        String accountNo = (String) userInfo.get("accountNo");
        String userName = (String) userInfo.get("userName");

        if (accountNo == null || accountNo.isEmpty()) {
            accountNo = (String) userInfo.get("id");
        }

        // 查询本地用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getExternalId, accountNo)
                .or()
                .eq(User::getUsername, accountNo);

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            // 创建新用户
            user = new User();
            user.setUsername(accountNo);
            user.setRealName(userName);
            user.setExternalId(accountNo);
            user.setSource("SSO");
            user.setStatus("ACTIVE");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // 设置初始密码（根据IAM规范，不同步密码，使用默认密码）
            user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");

            userMapper.insert(user);
            logger.info("Created new SSO user: {}", accountNo);
        } else {
            // 更新现有用户
            user.setRealName(userName);
            user.setSource("SSO");
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            logger.info("Updated SSO user: {}", accountNo);
        }

        // 更新最后登录信息
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        return user;
    }

    @Override
    public String getLogoutUrl(SSOConfig ssoConfig) {
        return ssoConfig.getAuthServerUrl() + ssoConfig.getLogoutEndpoint();
    }

    @Override
    public String getLogoutUrl(SSOConfig ssoConfig, String redirectUrl) {
        return ssoConfig.getAuthServerUrl() + ssoConfig.getLogoutEndpoint() + "?service=" + encodeUrl(redirectUrl);
    }

    @Override
    public Map<String, Object> keepSession(String accessToken, SSOConfig ssoConfig) {
        try {
            String url = ssoConfig.getSessionKeepEndpoint();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> params = new HashMap<>();
            params.put("accesstoken", accessToken);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                Map<String, Object> result = new HashMap<>();
                result.put("errorCode", jsonNode.get("errorCode").asText());
                result.put("errorMsg", jsonNode.get("errorMsg").asText());
                result.put("version", jsonNode.get("version").asText());
                result.put("timestamp", jsonNode.get("timestamp").asText());
                result.put("data", jsonNode.get("data").asText());

                return result;
            } else {
                throw new RuntimeException("会话保持失败");
            }
        } catch (Exception e) {
            logger.error("Keep session error", e);
            throw new RuntimeException("会话保持失败: " + e.getMessage());
        }
    }

    /**
     * URL编码
     */
    private String encodeUrl(String url) {
        try {
            return java.net.URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            logger.error("URL encode error", e);
            return url;
        }
    }

    /**
     * 根据ID获取SSO配置
     */
    private SSOConfig getSSOConfigById(String ssoConfigId) {
        return ssoConfigMapper.selectById(ssoConfigId);
    }
}
