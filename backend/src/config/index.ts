import dotenv from 'dotenv';
import path from 'path';

// 加载环境变量
const envPath = path.join(__dirname, '../../.env');
dotenv.config({ path: envPath });

export const config = {
  // 服务器配置
  nodeEnv: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT || '3000', 10),
  api: {
    prefix: process.env.API_PREFIX || '/api/v1',
  },

  // 前端URL
  frontendUrl: process.env.FRONTEND_URL || 'http://localhost:5000',

  // 数据库配置
  database: {
    url: process.env.DATABASE_URL || 'postgresql://postgres:password@localhost:5432/inspection',
  },

  // JWT配置
  jwt: {
    secret: process.env.JWT_SECRET || 'your-secret-key-change-in-production',
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
    refreshExpiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '30d',
  },

  // Redis配置
  redis: {
    host: process.env.REDIS_HOST || 'localhost',
    port: parseInt(process.env.REDIS_PORT || '6379', 10),
    password: process.env.REDIS_PASSWORD || undefined,
    enabled: !!process.env.REDIS_HOST,
  },

  // S3对象存储配置
  s3: {
    accessKeyId: process.env.S3_ACCESS_KEY_ID || '',
    secretAccessKey: process.env.S3_SECRET_ACCESS_KEY || '',
    bucket: process.env.S3_BUCKET || 'inspection-files',
    region: process.env.S3_REGION || 'us-east-1',
    endpoint: process.env.S3_ENDPOINT,
  },

  // SSO配置
  sso: {
    type: process.env.SSO_TYPE || 'local',
    oauth: {
      clientId: process.env.OAUTH_CLIENT_ID || '',
      clientSecret: process.env.OAUTH_CLIENT_SECRET || '',
      callbackUrl: process.env.OAUTH_CALLBACK_URL || '',
      authorizationUrl: process.env.OAUTH_AUTHORIZATION_URL || '',
      tokenUrl: process.env.OAUTH_TOKEN_URL || '',
      userInfoUrl: process.env.OAUTH_USER_INFO_URL || '',
    },
    saml: {
      entryPoint: process.env.SAML_ENTRY_POINT || '',
      issuer: process.env.SAML_ISSUER || 'room-inspection-system',
      callbackUrl: process.env.SAML_CALLBACK_URL || '',
      cert: process.env.SAML_CERT || '',
    },
    cas: {
      baseUrl: process.env.CAS_BASE_URL || '',
      serviceUrl: process.env.CAS_SERVICE_URL || '',
    },
  },

  // 数据同步配置
  sync: {
    enabled: process.env.SYNC_ENABLED === 'true',
    schedule: process.env.SYNC_SCHEDULE || '0 2 * * *',
    adapter: process.env.SYNC_ADAPTER || 'ldap',
    intervalMinutes: parseInt(process.env.SYNC_INTERVAL_MINUTES || '60', 10),
    ldap: {
      url: process.env.LDAP_URL || 'ldap://localhost',
      bindDn: process.env.LDAP_BIND_DN || '',
      bindPassword: process.env.LDAP_BIND_PASSWORD || '',
      searchBase: process.env.LDAP_SEARCH_BASE || '',
      searchFilter: process.env.LDAP_SEARCH_FILTER || '(objectClass=person)',
      orgBase: process.env.LDAP_ORG_BASE || '',
    },
    scim: {
      baseUrl: process.env.SCIM_BASE_URL || '',
      token: process.env.SCIM_TOKEN || '',
      syncOnStartup: process.env.SCIM_SYNC_ON_STARTUP === 'true',
    },
    custom: {
      apiUrl: process.env.CUSTOM_API_BASE_URL || '',
      apiToken: process.env.CUSTOM_API_TOKEN || '',
      userEndpoint: process.env.CUSTOM_API_USER_ENDPOINT || '/users',
      orgEndpoint: process.env.CUSTOM_API_ORG_ENDPOINT || '/organizations',
    },
  },

  // 日志配置
  log: {
    level: process.env.LOG_LEVEL || 'info',
    filePath: process.env.LOG_FILE_PATH || 'logs/app.log',
  },

  // 速率限制配置
  rateLimit: {
    enabled: process.env.NODE_ENV !== 'development',
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000', 10),
    maxRequests: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100', 10),
  },

  // CORS配置
  cors: {
    origin: process.env.CORS_ORIGIN || 'http://localhost:5000',
  },

  // 文件上传配置
  upload: {
    maxFileSize: parseInt(process.env.MAX_FILE_SIZE || '10485760', 10), // 10MB
    allowedTypes: (process.env.ALLOWED_FILE_TYPES || '.jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx').split(','),
  },

  // 密码策略
  password: {
    minLength: 8,
    requireUppercase: true,
    requireLowercase: true,
    requireNumber: true,
    requireSpecialChar: true,
  },

  // 账号锁定策略
  account: {
    maxFailedAttempts: 5,
    lockDurationMinutes: 30,
  },
};

// 配置验证
export const validateConfig = (): void => {
  const requiredFields = ['jwt.secret'];

  for (const field of requiredFields) {
    const value = field.split('.').reduce((obj, key) => obj?.[key], config);
    if (!value) {
      throw new Error(`Missing required configuration: ${field}`);
    }
  }

  if (config.nodeEnv === 'production' && config.jwt.secret === 'your-secret-key-change-in-production') {
    throw new Error('JWT_SECRET must be changed in production');
  }
};
