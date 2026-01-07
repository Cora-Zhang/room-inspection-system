import axios from 'axios';
import crypto from 'crypto';
import { config } from '../config';
import { ssoLogin } from '../services/auth.service';
import prisma from '../utils/prisma';

// OAuth2配置接口
export interface OAuth2Config {
  // 应用信息
  appId?: string;
  appSecret?: string;
  clientId?: string;
  clientSecret?: string;

  // API地址
  authorizationUrl: string;
  tokenUrl: string;
  userInfoUrl: string;

  // 回调地址
  callbackUrl: string;

  // 作用域
  scope?: string;

  // 用户信息映射
  userMapping?: {
    userId?: string;
    username?: string;
    email?: string;
    realName?: string;
    phone?: string;
  };

  // 额外参数（API Key等）
  extraParams?: Record<string, string>;
}

// OAuth2令牌响应
interface OAuth2TokenResponse {
  access_token: string;
  token_type?: string;
  expires_in?: number;
  refresh_token?: string;
}

// OAuth2用户信息响应
interface OAuth2UserInfo {
  [key: string]: any;
}

/**
 * OAuth2策略类
 */
export class OAuth2Strategy {
  private config: OAuth2Config;
  private provider: string;

  constructor(provider: string, oauthConfig: OAuth2Config) {
    this.provider = provider;
    this.config = this.mergeWithDefaultConfig(oauthConfig);
  }

  /**
   * 合并默认配置
   */
  private mergeWithDefaultConfig(userConfig: OAuth2Config): OAuth2Config {
    return {
      appId: userConfig.appId || userConfig.clientId,
      appSecret: userConfig.appSecret || userConfig.clientSecret,
      clientId: userConfig.clientId || userConfig.appId,
      clientSecret: userConfig.clientSecret || userConfig.appSecret,
      authorizationUrl: userConfig.authorizationUrl,
      tokenUrl: userConfig.tokenUrl,
      userInfoUrl: userConfig.userInfoUrl,
      callbackUrl: userConfig.callbackUrl || `${config.frontendUrl}/auth/callback`,
      scope: userConfig.scope || 'openid profile email',
      userMapping: userConfig.userMapping || {
        userId: 'sub' || 'id',
        username: 'preferred_username' || 'user_name',
        email: 'email',
        realName: 'name',
        phone: 'phone_number',
      },
      extraParams: userConfig.extraParams || {},
    };
  }

  /**
   * 生成授权URL
   */
  getAuthorizationUrl(state?: string): string {
    const params = new URLSearchParams({
      client_id: this.config.clientId || this.config.appId!,
      response_type: 'code',
      redirect_uri: this.config.callbackUrl,
      scope: this.config.scope!,
      state: state || crypto.randomBytes(16).toString('hex'),
    });

    // 添加额外参数（如app_key等）
    if (this.config.extraParams) {
      Object.entries(this.config.extraParams).forEach(([key, value]) => {
        params.append(key, value);
      });
    }

    return `${this.config.authorizationUrl}?${params.toString()}`;
  }

  /**
   * 用授权码换取访问令牌
   */
  async exchangeCodeForToken(code: string): Promise<OAuth2TokenResponse> {
    try {
      const params: any = {
        grant_type: 'authorization_code',
        code,
        redirect_uri: this.config.callbackUrl,
      };

      // 根据配置使用appId或clientId
      if (this.config.appId) {
        params.appId = this.config.appId;
        params.appSecret = this.config.appSecret;
      } else {
        params.client_id = this.config.clientId;
        params.client_secret = this.config.clientSecret;
      }

      // 添加额外参数
      if (this.config.extraParams) {
        Object.entries(this.config.extraParams).forEach(([key, value]) => {
          params[key] = value;
        });
      }

      const response = await axios.post<OAuth2TokenResponse>(
        this.config.tokenUrl,
        new URLSearchParams(params).toString(),
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        }
      );

      return response.data;
    } catch (error: any) {
      console.error('OAuth2 token exchange error:', error.response?.data || error.message);
      throw new Error('获取访问令牌失败');
    }
  }

  /**
   * 获取用户信息
   */
  async getUserInfo(accessToken: string): Promise<OAuth2UserInfo> {
    try {
      const response = await axios.get(this.config.userInfoUrl, {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });

      return response.data;
    } catch (error: any) {
      console.error('OAuth2 get user info error:', error.response?.data || error.message);
      throw new Error('获取用户信息失败');
    }
  }

  /**
   * 映射用户信息
   */
  mapUserInfo(userInfo: OAuth2UserInfo): {
    username: string;
    email?: string;
    realName?: string;
    phone?: string;
    externalId: string;
  } {
    const mapping = this.config.userMapping!;

    return {
      username: userInfo[mapping.username!] || userInfo[mapping.userId!] || userInfo['sub'] || userInfo['id'],
      email: userInfo[mapping.email!] || userInfo['email'],
      realName: userInfo[mapping.realName!] || userInfo['name'] || userInfo['displayName'],
      phone: userInfo[mapping.phone!] || userInfo['phone'] || userInfo['telephoneNumber'],
      externalId: userInfo[mapping.userId!] || userInfo['sub'] || userInfo['id'] || userInfo['oid'],
    };
  }

  /**
   * 完整的认证流程
   */
  async authenticate(code: string): Promise<any> {
    // 1. 换取访问令牌
    const tokenResponse = await this.exchangeCodeForToken(code);

    // 2. 获取用户信息
    const userInfo = await this.getUserInfo(tokenResponse.access_token);

    // 3. 映射用户信息
    const mappedUser = this.mapUserInfo(userInfo);

    // 4. SSO登录（自动创建用户）
    const loginResult = await ssoLogin(
      {
        ...mappedUser,
        externalId: mappedUser.externalId,
      },
      'SSO'
    );

    return loginResult;
  }
}

/**
 * 从数据库获取SSO配置并创建OAuth2策略
 */
export async function createOAuth2Strategy(provider: string): Promise<OAuth2Strategy> {
  const configRecord = await prisma.sSOConfig.findUnique({
    where: { provider },
  });

  if (!configRecord) {
    throw new Error(`未找到SSO配置: ${provider}`);
  }

  if (!configRecord.enabled) {
    throw new Error(`SSO配置未启用: ${provider}`);
  }

  const oauthConfig: OAuth2Config = JSON.parse(configRecord.config);
  oauthConfig.callbackUrl = `${config.frontendUrl}/auth/callback/${provider}`;

  return new OAuth2Strategy(provider, oauthConfig);
}

/**
 * 获取所有启用的SSO提供商
 */
export async function getEnabledSSOProviders(): Promise<Array<{ provider: string; name: string; type: string }>> {
  const configs = await prisma.sSOConfig.findMany({
    where: { enabled: true },
    select: { provider: true, name: true, type: true },
    orderBy: { sort: 'asc' },
  });

  return configs;
}
