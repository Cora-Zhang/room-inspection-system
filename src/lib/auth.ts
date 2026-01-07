import { http, tokenManager } from './api';

// 用户信息类型
export interface User {
  id: string;
  username: string;
  email?: string;
  realName: string;
  avatar?: string;
  phone?: string;
  employeeId?: string;
  department?: any;
  roles: string[];
  permissions: string[];
}

// 登录请求类型
export interface LoginRequest {
  username: string;
  password: string;
}

// 登录响应类型
export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
}

// 认证服务
export const authService = {
  // 登录
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await http.post<LoginResponse>('/auth/login', credentials);

    if (response.success && response.data) {
      // 保存token
      tokenManager.setToken(response.data.token);
      tokenManager.setRefreshToken(response.data.refreshToken);
      localStorage.setItem('user', JSON.stringify(response.data.user));
    }

    return response.data!;
  },

  // 登出
  async logout(): Promise<void> {
    try {
      await http.post('/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // 清除本地存储
      tokenManager.removeTokens();
    }
  },

  // 获取当前用户信息
  async getCurrentUser(): Promise<User | null> {
    try {
      const response = await http.get<User>('/auth/me');
      if (response.success && response.data) {
        localStorage.setItem('user', JSON.stringify(response.data));
        return response.data;
      }
      return null;
    } catch (error) {
      console.error('Get current user error:', error);
      return null;
    }
  },

  // 检查是否已登录
  isAuthenticated(): boolean {
    return !!tokenManager.getToken();
  },

  // 获取本地存储的用户信息
  getLocalUser(): User | null {
    if (typeof window === 'undefined') return null;
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  // 检查权限
  hasPermission(permission: string): boolean {
    const user = this.getLocalUser();
    if (!user) return false;
    return user.permissions.includes(permission) || user.roles.includes('admin');
  },

  // 检查角色
  hasRole(role: string): boolean {
    const user = this.getLocalUser();
    if (!user) return false;
    return user.roles.includes(role);
  },

  // 修改密码
  async changePassword(oldPassword: string, newPassword: string): Promise<void> {
    await http.post('/auth/change-password', {
      oldPassword,
      newPassword,
    });
  },

  // 刷新token
  async refreshAccessToken(): Promise<string | null> {
    const refreshToken = tokenManager.getRefreshToken();
    if (!refreshToken) return null;

    try {
      const response = await http.post<{ token: string }>('/auth/refresh', {
        refreshToken,
      });

      if (response.success && response.data) {
        tokenManager.setToken(response.data.token);
        return response.data.token;
      }
      return null;
    } catch (error) {
      console.error('Refresh token error:', error);
      return null;
    }
  },
};

// 权限Hook (用于组件中)
export const useAuth = () => {
  const user = authService.getLocalUser();
  const isAuthenticated = authService.isAuthenticated();

  return {
    user,
    isAuthenticated,
    hasPermission: authService.hasPermission,
    hasRole: authService.hasRole,
    login: authService.login,
    logout: authService.logout,
  };
};
