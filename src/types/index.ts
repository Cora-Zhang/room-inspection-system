// 基础数据类型
export interface BaseEntity {
  id: number;
  createTime: string;
  updateTime: string;
  creator?: string;
  updater?: string;
}

// 用户类型
export interface User extends BaseEntity {
  username: string;
  password?: string;
  name: string;
  email?: string;
  phone?: string;
  avatar?: string;
  status: 'active' | 'inactive' | 'locked';
  departmentId?: number;
  position?: string;
  lastLoginTime?: string;
}

// 角色类型
export interface Role extends BaseEntity {
  name: string;
  code: string;
  description?: string;
  status: 'active' | 'inactive';
  sort?: number;
}

// 权限类型
export interface Permission extends BaseEntity {
  name: string;
  code: string;
  type: 'menu' | 'button' | 'api';
  parentId?: number;
  path?: string;
  component?: string;
  icon?: string;
  sort?: number;
  status: 'active' | 'inactive';
  children?: Permission[];
}

// 角色权限关联
export interface RolePermission {
  roleId: number;
  permissionId: number;
}

// 数据字典类型
export interface Dictionary extends BaseEntity {
  name: string;
  code: string;
  description?: string;
  status: 'active' | 'inactive';
  sort?: number;
}

// 数据字典项
export interface DictionaryItem extends BaseEntity {
  dictionaryId: number;
  label: string;
  value: string;
  sort?: number;
  status: 'active' | 'inactive';
  extra?: string;
}

// 数据权限规则
export interface DataPermission extends BaseEntity {
  name: string;
  code: string;
  type: 'department' | 'user' | 'custom';
  scope: 'all' | 'department' | 'self' | 'custom';
  rules: string; // JSON字符串
  status: 'active' | 'inactive';
}

// UI配置类型
export interface UIConfig extends BaseEntity {
  type: 'logo' | 'background' | 'theme' | 'custom';
  key: string;
  value: string;
  description?: string;
  category: string;
}

// 主题配置
export interface ThemeConfig {
  id: number;
  name: string;
  primaryColor: string;
  secondaryColor: string;
  backgroundColor: string;
  textColor: string;
  accentColor: string;
  isDefault: boolean;
}

// 菜单类型
export interface Menu extends Permission {
  children?: Menu[];
}

// API响应
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

// 分页参数
export interface PageParams {
  page: number;
  pageSize: number;
}

// 分页响应
export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

// 登录请求
export interface LoginRequest {
  username: string;
  password: string;
  captcha?: string;
}

// 登录响应
export interface LoginResponse {
  token: string;
  user: User;
  permissions: Permission[];
  roles: Role[];
}

// 报表配置
export interface ReportConfig extends BaseEntity {
  name: string;
  code: string;
  type: 'chart' | 'table' | 'mixed';
  dataSource: string;
  config: string; // JSON配置
  refreshInterval?: number;
  status: 'active' | 'inactive';
}

// 接口配置
export interface ApiConfig extends BaseEntity {
  name: string;
  path: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  description?: string;
  module: string;
  authRequired: boolean;
  rateLimit?: number;
  cache?: boolean;
  cacheTime?: number;
}

// 元数据配置
export interface Metadata extends BaseEntity {
  name: string;
  code: string;
  type: string;
  schema: string; // JSON Schema
  status: 'active' | 'inactive';
}
