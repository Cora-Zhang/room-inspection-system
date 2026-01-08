package com.roominspection.backend.service;

import com.roominspection.backend.entity.SSOConfig;
import com.roominspection.backend.entity.User;

import java.util.Map;

/**
 * SSO服务接口
 */
public interface SSOService {

    /**
     * 生成SSO登录URL
     *
     * @param ssoConfigId SSO配置ID
     * @param state      状态参数（可选）
     * @return SSO登录URL
     */
    String generateSsoLoginUrl(String ssoConfigId, String state);

    /**
     * 获取SSO配置
     *
     * @return 启用的SSO配置
     */
    SSOConfig getEnabledSSOConfig();

    /**
     * 使用授权码获取访问令牌
     *
     * @param code       授权码
     * @param ssoConfig  SSO配置
     * @return 访问令牌信息
     */
    Map<String, Object> getAccessToken(String code, SSOConfig ssoConfig);

    /**
     * 使用访问令牌获取用户信息
     *
     * @param accessToken 访问令牌
     * @param ssoConfig   SSO配置
     * @return 用户信息
     */
    Map<String, Object> getUserInfo(String accessToken, SSOConfig ssoConfig);

    /**
     * 通过SSO登录用户
     *
     * @param code  授权码
     * @param state 状态参数
     * @return 登录后的用户信息
     */
    User loginBySso(String code, String state);

    /**
     * 单点退出
     *
     * @param ssoConfig SSO配置
     * @return 退出URL
     */
    String getLogoutUrl(SSOConfig ssoConfig);

    /**
     * 单点退出（带回调地址）
     *
     * @param ssoConfig   SSO配置
     * @param redirectUrl 退出后跳转地址
     * @return 退出URL
     */
    String getLogoutUrl(SSOConfig ssoConfig, String redirectUrl);

    /**
     * 会话保持
     *
     * @param accessToken 访问令牌
     * @param ssoConfig   SSO配置
     * @return 会话信息
     */
    Map<String, Object> keepSession(String accessToken, SSOConfig ssoConfig);
}
