package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * SSO配置实体（OAuth2.0单点登录配置）
 */
@Data
@TableName("sso_config")
public class SSOConfig {

    /**
     * 配置ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 是否启用：Y-启用，N-禁用
     */
    private String enabled;

    /**
     * 客户端ID（client_id）
     */
    private String clientId;

    /**
     * 客户端密钥（client_secret，加密存储）
     */
    private String clientSecret;

    /**
     * 认证服务器地址（如：https://iam.example.com）
     */
    private String authServerUrl;

    /**
     * 授权端点（如：/esc-sso/oauth2.0/authorize）
     */
    private String authorizeEndpoint;

    /**
     * 获取Token端点（如：/env-201/open-apiportal/iamopen/oauth/accessToken）
     */
    private String tokenEndpoint;

    /**
     * 获取用户信息端点（如：/env-201/open-apiportal/iamopen/oauth/profile）
     */
    private String userInfoEndpoint;

    /**
     * 单点退出端点（如：/esc-sso/logout）
     */
    private String logoutEndpoint;

    /**
     * 回调地址（redirect_uri）
     */
    private String redirectUri;

    /**
     * 应用入口地址（用于触发SSO）
     */
    private String entryUrl;

    /**
     * Token有效期（秒）
     */
    private Integer tokenExpireTime;

    /**
     * 会话保持端点（可选）
     */
    private String sessionKeepEndpoint;

    /**
     * SSO协议类型：OAUTH2、SAML2
     */
    private String protocolType;

    /**
     * SAML元数据URL（SAML协议使用）
     */
    private String samlMetadataUrl;

    /**
     * JWT Secret（用于数据同步接口鉴权）
     */
    private String jwtSecret;

    /**
     * 允许的时间偏差（秒，默认60秒）
     */
    private Integer jwtTimeOffset;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;
}
