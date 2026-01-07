/**
 * 登录表单
 */
export interface LoginForm {
  username: string
  password: string
  code?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  refreshToken?: string
  userInfo: UserInfo
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: string
  username: string
  realName: string
  avatar?: string
  email?: string
  phone?: string
  department?: string
  position?: string
  roles: string[]
  permissions: string[]
}

/**
 * 分页参数
 */
export interface PageParams {
  page: number
  pageSize: number
}

/**
 * 分页响应
 */
export interface PageResult<T> {
  total: number
  records: T[]
  current: number
  size: number
  pages: number
}

/**
 * 通用响应
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}
