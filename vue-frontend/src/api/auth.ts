import request from './index'
import type { LoginForm, LoginResponse, UserInfo } from './types'

/**
 * 用户登录
 */
export const login = (data: LoginForm) => {
  return request<LoginResponse>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 获取SSO登录URL
 */
export const getSsoLoginUrl = (ssoConfigId?: string, state?: string) => {
  return request<{ data: string }>({
    url: '/sso/login-url',
    method: 'get',
    params: { ssoConfigId, state }
  })
}

/**
 * 获取SSO退出URL
 */
export const getSsoLogoutUrl = (ssoConfigId?: string, redirectUrl?: string) => {
  return request<{ data: string }>({
    url: '/sso/logout-url',
    method: 'get',
    params: { ssoConfigId, redirectUrl }
  })
}

/**
 * 获取SSO配置信息
 */
export const getSsoConfig = () => {
  return request<{ data: any }>({
    url: '/sso/config',
    method: 'get'
  })
}

/**
 * 会话保持
 */
export const keepSession = (accessToken: string) => {
  return request<{ data: any }>({
    url: '/sso/keep-session',
    method: 'post',
    params: { accessToken }
  })
}

/**
 * OAuth2.0 SSO登录回调
 */
export const oauthCallback = (provider: string, code: string, state: string) => {
  return request<LoginResponse>({
    url: `/auth/callback/${provider}`,
    method: 'get',
    params: { code, state }
  })
}

/**
 * 刷新Token
 */
export const refreshToken = (refreshToken: string) => {
  return request<LoginResponse>({
    url: '/auth/refresh',
    method: 'post',
    data: refreshToken
  })
}

/**
 * 用户登出
 */
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 */
export const getUserInfo = () => {
  return request<UserInfo>({
    url: '/auth/user/info',
    method: 'get'
  })
}
