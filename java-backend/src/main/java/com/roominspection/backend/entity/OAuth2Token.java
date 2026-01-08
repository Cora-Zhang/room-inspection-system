package com.roominspection.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * OAuth2.0令牌实体
 */
@Data
@TableName("oauth2_token")
public class OAuth2Token {

    /**
     * 令牌ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 授权码（code）
     */
    private String authorizationCode;

    /**
     * 访问令牌（access_token）
     */
    private String accessToken;

    /**
     * 刷新令牌（refresh_token，如有）
     */
    private String refreshToken;

    /**
     * Token类型（通常为bearer）
     */
    private String tokenType;

    /**
     * Token过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * Token有效期（秒）
     */
    private Integer expiresIn;

    /**
     * Scope（授权范围）
     */
    private String scope;

    /**
     * 状态：ACTIVE-有效，EXPIRED-已过期，REVOKED-已撤销
     */
    private String status;

    /**
     * 关联的SSO配置ID
     */
    private String ssoConfigId;

    /**
     * 状态参数（state）
     */
    private String state;

    /**
     * 回调地址
     */
    private String redirectUri;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * User-Agent
     */
    private String userAgent;
}
