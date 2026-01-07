# 机房巡检系统 - 前后端分离架构设计

## 架构概述

采用主流的前后端分离架构，提升系统可扩展性和可维护性，支持企业级单点认证和数据集成。

## 技术栈

### 前端
- **框架**: Next.js 16 (App Router, 纯前端模式)
- **语言**: TypeScript 5
- **UI**: Tailwind CSS 4 + React 19
- **状态管理**: React Context + Hooks
- **HTTP客户端**: Axios

### 后端
- **运行时**: Node.js 20+
- **框架**: Express.js + TypeScript
- **ORM**: Prisma (PostgreSQL)
- **认证**: JWT + Passport.js (OAuth2/OIDC/SAML)
- **验证**: Joi / Zod
- **日志**: Winston
- **文档**: Swagger/OpenAPI

### 基础设施
- **数据库**: PostgreSQL (集成服务)
- **对象存储**: S3 (集成服务)
- **缓存**: Redis (可选)

## 目录结构

```
.
├── frontend/                    # 前端应用 (Next.js)
│   ├── src/
│   │   ├── app/                # App Router页面
│   │   ├── components/         # React组件
│   │   ├── lib/               # 工具库
│   │   │   ├── api.ts         # API客户端封装
│   │   │   ├── auth.ts        # 认证管理
│   │   │   └── config.ts      # 配置管理
│   │   └── types/             # TypeScript类型
│   ├── package.json
│   └── .coze
│
├── backend/                     # 后端API服务
│   ├── src/
│   │   ├── config/            # 配置管理
│   │   ├── controllers/       # 控制器层
│   │   ├── middlewares/       # 中间件
│   │   │   ├── auth.middleware.ts
│   │   │   ├── rbac.middleware.ts
│   │   │   ├── validation.middleware.ts
│   │   │   └── error.middleware.ts
│   │   ├── models/            # 数据模型 (Prisma)
│   │   │   ├── user.model.ts
│   │   │   ├── role.model.ts
│   │   │   └── organization.model.ts
│   │   ├── services/          # 业务逻辑层
│   │   │   ├── auth.service.ts
│   │   │   ├── user.service.ts
│   │   │   └── sync.service.ts  # 数据同步服务
│   │   ├── strategies/        # 认证策略
│   │   │   ├── local.strategy.ts
│   │   │   ├── oauth.strategy.ts
│   │   │   └── saml.strategy.ts
│   │   ├── adapters/          # 数据适配器 (HR/主数据集成)
│   │   │   ├── base.adapter.ts
│   │   │   ├── ldap.adapter.ts
│   │   │   ├── scim.adapter.ts
│   │   │   └── custom.adapter.ts
│   │   ├── routes/            # 路由定义
│   │   ├── utils/             # 工具函数
│   │   └── app.ts             # Express应用入口
│   ├── prisma/
│   │   └── schema.prisma      # 数据库模型
│   ├── package.json
│   └── .coze
│
├── shared/                     # 共享代码
│   ├── types/                 # 共享类型定义
│   └── constants/             # 共享常量
│
└── docs/                       # 文档
    ├── api.md                 # API文档
    ├── deployment.md          # 部署文档
    └── integration.md         # 集成文档
```

## 核心模块设计

### 1. 认证模块

#### 支持的认证方式
- **本地认证**: 用户名/密码
- **OAuth2/OIDC**: 支持企业AD、Azure AD、Keycloak等
- **SAML**: 企业级单点登录
- **CAS**: 中央认证服务

#### 认证流程
1. 用户访问系统，跳转认证中心
2. 认证中心验证身份，返回token
3. 后端验证token，生成JWT
4. 前端存储JWT，后续请求携带Authorization头
5. JWT过期自动刷新或重新登录

### 2. 权限控制 (RBAC)

#### 权限模型
```
User (用户)
  └── Roles (角色，多对多)
      └── Permissions (权限，多对多)
          └── Resources (资源)
          └── Actions (操作: create, read, update, delete)
```

#### 数据权限
- 部门级数据隔离
- 自定义数据范围（本部门、下属部门、全部数据）

### 3. 组织架构同步

#### 支持的集成方式
- **LDAP/AD**: 轻量级目录访问协议
- **SCIM**: System for Cross-domain Identity Management
- **自定义API**: 企业HR系统API
- **文件导入**: Excel/CSV批量导入

#### 同步策略
- **全量同步**: 定期全量拉取（每日）
- **增量同步**: Webhook回调或定时增量
- **手动同步**: 管理员触发

### 4. 数据适配器模式

使用适配器模式实现可插拔的数据集成：

```typescript
interface DataSyncAdapter {
  name: string;
  connect(): Promise<boolean>;
  fetchUsers(): Promise<User[]>;
  fetchOrganizations(): Promise<Organization[]>;
  mapUser(data: any): User;
  mapOrganization(data: any): Organization;
}
```

## API设计规范

### RESTful API规范
- 使用HTTP动词: GET, POST, PUT, DELETE
- 资源命名: 复数形式 `/api/users`
- 版本控制: `/api/v1/users`
- 统一响应格式

### 统一响应格式
```typescript
// 成功响应
{
  "success": true,
  "data": { ... },
  "message": "操作成功"
}

// 错误响应
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "参数验证失败",
    "details": [...]
  }
}

// 分页响应
{
  "success": true,
  "data": [...],
  "pagination": {
    "page": 1,
    "pageSize": 20,
    "total": 100,
    "totalPages": 5
  }
}
```

## 安全设计

### 前端安全
- XSS防护: React自动转义
- CSRF防护: SameSite Cookie + CSRF Token
- Content Security Policy (CSP)
- HTTPS强制

### 后端安全
- JWT签名验证
- Rate Limiting (速率限制)
- SQL注入防护 (Prisma参数化查询)
- 敏感数据加密存储
- 审计日志

## 部署架构

### 开发环境
```
Frontend (localhost:5000) ←→ Backend (localhost:3000) ←→ Database (PostgreSQL)
```

### 生产环境
```
Nginx (反向代理)
  ├── Frontend (Next.js 静态部署)
  └── Backend (Express API)
        ├── PostgreSQL (主数据库)
        ├── Redis (缓存 + Session)
        └── S3 (文件存储)
```

## 配置管理

### 环境变量
```bash
# 数据库
DATABASE_URL=postgresql://user:pass@localhost:5432/dbname

# JWT配置
JWT_SECRET=your-secret-key
JWT_EXPIRES_IN=7d
JWT_REFRESH_EXPIRES_IN=30d

# OAuth配置
OAUTH_CLIENT_ID=xxx
OAUTH_CLIENT_SECRET=xxx
OAUTH_CALLBACK_URL=https://app.com/api/auth/callback

# SSO配置
SSO_TYPE=oauth2|saml|cas
SSO_ISSUER=https://sso.company.com
SSO_METADATA_URL=https://sso.company.com/metadata

# 同步配置
SYNC_ENABLED=true
SYNC_SCHEDULE=0 2 * * *  # 每日凌晨2点
SYNC_ADAPTER=ldap|scim|custom
```

## 扩展性设计

### 插件化架构
- 认证策略可插拔
- 数据适配器可扩展
- 中间件可组合

### 微服务预留
- 模块化设计，支持拆分为微服务
- API网关统一入口
- 服务间通信 (gRPC/REST)

## 监控与日志

### 日志级别
- ERROR: 错误信息
- WARN: 警告信息
- INFO: 关键操作日志
- DEBUG: 调试信息

### 日志格式
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "level": "INFO",
  "message": "User login",
  "context": {
    "userId": "123",
    "ip": "192.168.1.1",
    "userAgent": "Mozilla/5.0..."
  }
}
```

## 性能优化

### 前端
- 代码分割 (React.lazy)
- 图片优化 (Next.js Image)
- CDN加速
- 缓存策略

### 后端
- 数据库连接池
- Redis缓存
- 响应压缩 (gzip)
- N+1查询优化

## 开发规范

### Git工作流
- main: 生产分支
- develop: 开发分支
- feature/*: 功能分支
- hotfix/*: 紧急修复

### Code Review
- 必须经过Code Review才能合并
- 使用ESLint + Prettier统一代码风格
- 单元测试覆盖率 > 80%
