import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { getApiUrl, API_CONFIG } from './config';

// 响应数据类型
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  error?: {
    code: string;
    message: string;
    details?: any;
  };
  pagination?: {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
  };
}

// Token管理
const tokenManager = {
  getToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('token');
  },

  getRefreshToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('refreshToken');
  },

  setToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem('token', token);
  },

  setRefreshToken(token: string): void {
    if (typeof window === 'undefined') return;
    localStorage.setItem('refreshToken', token);
  },

  removeTokens(): void {
    if (typeof window === 'undefined') return;
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },
};

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: getApiUrl(''),
  timeout: API_CONFIG.timeout,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 打印配置信息（仅开发环境）
if (typeof window !== 'undefined' && process.env.NODE_ENV === 'development') {
  console.log('API Client Configuration:');
  console.log('Base URL:', getApiUrl(''));
  console.log('Environment:', process.env.NODE_ENV);
}

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    const token = tokenManager.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    return response;
  },
  async (error: AxiosError<ApiResponse>) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    // Token过期，尝试刷新
    if (error.response?.status === 401 && !originalRequest._retry) {
      // 检查是否是登录请求，如果是则不刷新token
      if (originalRequest.url?.includes('/login')) {
        return Promise.reject(error);
      }

      originalRequest._retry = true;

      try {
        const refreshToken = tokenManager.getRefreshToken();
        if (!refreshToken) {
          throw new Error('No refresh token');
        }

        const response = await axios.post(`${getApiUrl('')}/auth/refresh`, {
          refreshToken,
        });

        if (response.data.success) {
          const { token } = response.data.data;
          tokenManager.setToken(token);
          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${token}`;
          }
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // 刷新失败，清除token并跳转登录
        tokenManager.removeTokens();
        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }
        return Promise.reject(refreshError);
      }
    }

    // 返回错误信息
    return Promise.reject(error);
  }
);

// HTTP方法封装
export const http = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return apiClient.get(url, config).then((res) => res.data);
  },

  post<T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> {
    return apiClient.post(url, data, config).then((res) => res.data);
  },

  put<T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> {
    return apiClient.put(url, data, config).then((res) => res.data);
  },

  patch<T = any>(
    url: string,
    data?: any,
    config?: AxiosRequestConfig
  ): Promise<ApiResponse<T>> {
    return apiClient.patch(url, data, config).then((res) => res.data);
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return apiClient.delete(url, config).then((res) => res.data);
  },

  // 文件上传
  upload<T = any>(url: string, file: File, onProgress?: (progress: number) => void): Promise<ApiResponse<T>> {
    const formData = new FormData();
    formData.append('file', file);

    return apiClient.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      },
    }).then((res) => res.data);
  },
};

export { tokenManager };
export default apiClient;
