// API配置
export const API_CONFIG = {
  // 开发环境使用本地后端
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:3000',
  apiPrefix: '/api/v1',
  timeout: 30000,
};

// 获取完整的API URL
export const getApiUrl = (path: string): string => {
  return `${API_CONFIG.baseURL}${API_CONFIG.apiPrefix}${path}`;
};

// SSO配置
export const SSO_CONFIG = {
  enabled: process.env.NEXT_PUBLIC_SSO_ENABLED === 'true',
  type: process.env.NEXT_PUBLIC_SSO_TYPE || 'local',
  oauth: {
    clientId: process.env.NEXT_PUBLIC_OAUTH_CLIENT_ID || '',
    authorizationUrl: process.env.NEXT_PUBLIC_OAUTH_AUTHORIZATION_URL || '',
    callbackUrl: `${window.location.origin}/auth/callback`,
  },
  saml: {
    entryPoint: process.env.NEXT_PUBLIC_SAML_ENTRY_POINT || '',
    callbackUrl: `${window.location.origin}/auth/callback/saml`,
  },
};

// 应用配置
export const APP_CONFIG = {
  title: process.env.NEXT_PUBLIC_APP_TITLE || '机房巡检系统',
  description: process.env.NEXT_PUBLIC_APP_DESCRIPTION || '智能监控 · 实时预警 · 高效运维',
  version: process.env.NEXT_PUBLIC_APP_VERSION || '1.0.0',
};
