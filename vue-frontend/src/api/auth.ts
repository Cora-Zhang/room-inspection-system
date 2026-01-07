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
