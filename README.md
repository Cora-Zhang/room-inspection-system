# 机房巡检系统 - 架构升级版

## 📋 项目概述

机房巡检系统是一个企业级的智能监控与运维管理平台，采用现代化的前后端分离架构，支持主流单点认证协议和企业系统集成。

## ✨ 核心特性

### 🔐 企业级认证
- ✅ 多种认证方式：本地认证、OAuth2/OIDC、SAML 2.0、CAS
- ✅ 支持Azure AD、Keycloak、Auth0等主流SSO平台
- ✅ JWT令牌机制，自动刷新
- ✅ 账号安全策略（密码强度、登录锁定）

### 👥 组织与用户管理
- ✅ 灵活的组织架构树形结构
- ✅ 基于RBAC的权限控制
- ✅ 支持LDAP/AD同步
- ✅ 支持SCIM标准协议
- ✅ 支持自定义HR系统集成

### 🏗️ 技术架构
- ✅ 前后端完全分离
- ✅ RESTful API设计
- ✅ TypeScript全栈开发
- ✅ 数据库ORM (Prisma)
- ✅ 统一错误处理和日志
- ✅ 请求速率限制
- ✅ 审计日志记录

### 🎨 前端特性
- ✅ Next.js 16 (App Router)
- ✅ React 19 + Hooks
- ✅ Tailwind CSS 4 科幻风格UI
- ✅ 响应式设计
- ✅ 自动Token刷新
- ✅ 统一API调用封装

## 📁 目录结构

```
.
├── frontend/                    # 前端应用 (Next.js)
│   ├── src/
│   │   ├── app/                # App Router页面
│   │   ├── components/         # React组件
│   │   ├── lib/               # 工具库
│   │   │   ├── api.ts         # API客户端
│   │   │   ├── auth.ts        # 认证管理
│   │   │   └── config.ts      # 配置管理
│   │   └── types/             # TypeScript类型
│   └── package.json
│
├── backend/                     # 后端API服务 (Express)
│   ├── src/
│   │   ├── config/            # 配置管理
│   │   ├── controllers/       # 控制器层
│   │   ├── middlewares/       # 中间件
│   │   │   ├── auth.middleware.ts
│   │   │   ├── rbac.middleware.ts
│   │   │   └── error.middleware.ts
│   │   ├── services/          # 业务逻辑层
│   │   │   ├── auth.service.ts
│   │   │   └── sync.service.ts
│   │   ├── strategies/        # 认证策略
│   │   ├── adapters/          # 数据适配器
│   │   ├── routes/            # 路由定义
│   │   ├── utils/             # 工具函数
│   │   └── app.ts             # Express应用入口
│   ├── prisma/
│   │   └── schema.prisma      # 数据库模型
│   └── package.json
│
├── docs/                       # 文档
│   ├── deployment.md          # 部署文档
│   └── integration.md         # 集成文档
│
├── ARCHITECTURE.md             # 架构设计文档
├── README.md                   # 本文件
└── .env.local.example          # 环境变量示例
```

## 🚀 快速开始

### 环境要求

- Node.js 18+
- PostgreSQL 14+
- pnpm 8+

### 1. 安装依赖

```bash
# 前端
pnpm install

# 后端
cd backend
pnpm install
```

### 2. 配置环境变量

#### 前端配置

```bash
cp .env.local.example .env.local
# 编辑 .env.local 设置后端API地址
```

#### 后端配置

```bash
cd backend
cp .env.example .env
# 编辑 .env 设置数据库、JWT密钥等
```

### 3. 初始化数据库

```bash
cd backend

# 生成Prisma客户端
npx prisma generate

# 运行数据库迁移
npx prisma migrate dev --name init
```

### 4. 启动服务

#### 启动后端

```bash
cd backend
pnpm dev
```

后端将运行在 `http://localhost:3000`

#### 启动前端

```bash
# 在项目根目录
pnpm dev
```

前端将运行在 `http://localhost:5000`

### 5. 访问系统

打开浏览器访问: `http://localhost:5000`

默认管理员账号需要通过API创建:

```bash
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'
```

## 🔧 配置说明

### 后端关键配置 (backend/.env)

```bash
# 服务器
PORT=3000
NODE_ENV=development

# 数据库
DATABASE_URL=postgresql://postgres:password@localhost:5432/inspection

# JWT (必须修改!)
JWT_SECRET=your-production-secret-key
JWT_EXPIRES_IN=7d

# SSO配置
SSO_TYPE=local  # local|oauth2|saml|cas

# OAuth2 (如启用)
OAUTH_CLIENT_ID=
OAUTH_CLIENT_SECRET=
OAUTH_CALLBACK_URL=http://localhost:3000/api/v1/auth/callback/oauth2

# 数据同步
SYNC_ENABLED=true
SYNC_ADAPTER=ldap  # ldap|scim|custom
```

### 前端关键配置 (.env.local)

```bash
# 后端API地址
NEXT_PUBLIC_API_BASE_URL=http://localhost:3000

# SSO配置
NEXT_PUBLIC_SSO_ENABLED=false
NEXT_PUBLIC_SSO_TYPE=local
```

## 📚 文档

- [架构设计](ARCHITECTURE.md) - 系统架构详细说明
- [部署文档](docs/deployment.md) - 生产环境部署指南
- [集成文档](docs/integration.md) - SSO和HR系统集成指南

## 🔐 SSO集成示例

### OAuth2/OIDC 集成 (Azure AD)

```bash
# 后端配置
SSO_TYPE=oauth2
OAUTH_CLIENT_ID=your-azure-ad-client-id
OAUTH_CLIENT_SECRET=your-azure-ad-client-secret
OAUTH_AUTHORIZATION_URL=https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize
OAUTH_TOKEN_URL=https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token
OAUTH_USER_INFO_URL=https://graph.microsoft.com/v1.0/me

# 前端配置
NEXT_PUBLIC_SSO_ENABLED=true
NEXT_PUBLIC_SSO_TYPE=oauth2
NEXT_PUBLIC_OAUTH_CLIENT_ID=your-azure-ad-client-id
NEXT_PUBLIC_OAUTH_AUTHORIZATION_URL=https://login.microsoftonline.com/{tenant}/oauth2/v2.0/authorize
```

### SAML 集成

```bash
SSO_TYPE=saml
SAML_ENTRY_POINT=https://sso.example.com/saml/sso
SAML_ISSUER=room-inspection-system
SAML_CALLBACK_URL=https://your-domain.com/api/v1/auth/callback/saml
SAML_CERT=-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----
```

## 👥 组织同步示例

### LDAP/AD 同步

```bash
SYNC_ENABLED=true
SYNC_ADAPTER=ldap
SYNC_SCHEDULE=0 2 * * *  # 每日凌晨2点

LDAP_URL=ldap://ldap.example.com:389
LDAP_BIND_DN=cn=admin,dc=example,dc=com
LDAP_BIND_PASSWORD=admin-password
LDAP_SEARCH_BASE=ou=users,dc=example,dc=com
LDAP_SEARCH_FILTER=(objectClass=person)
```

### SCIM 同步

```bash
SYNC_ADAPTER=scim
SCIM_BASE_URL=https://hr.example.com/scim/v2
SCIM_TOKEN=your-scim-bearer-token
```

## 🛡️ 安全建议

1. **生产环境必须修改JWT_SECRET**
2. 使用HTTPS部署
3. 启用速率限制
4. 定期备份数据库
5. 监控审计日志
6. 使用强密码策略

## 📊 API文档

### 认证接口

- `POST /api/v1/auth/login` - 本地登录
- `POST /api/v1/auth/refresh` - 刷新token
- `GET /api/v1/auth/me` - 获取当前用户信息
- `POST /api/v1/auth/change-password` - 修改密码
- `POST /api/v1/auth/logout` - 登出

### 用户管理

- `GET /api/v1/users` - 获取用户列表
- `GET /api/v1/users/:id` - 获取用户详情
- `POST /api/v1/users` - 创建用户
- `PUT /api/v1/users/:id` - 更新用户
- `DELETE /api/v1/users/:id` - 删除用户

### 权限管理

- `GET /api/v1/roles` - 获取角色列表
- `GET /api/v1/permissions` - 获取权限列表
- `POST /api/v1/roles` - 创建角色
- `PUT /api/v1/roles/:id` - 更新角色

### 组织管理

- `GET /api/v1/departments` - 获取部门树

## 🐛 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查 `DATABASE_URL` 配置
   - 确认PostgreSQL服务已启动

2. **JWT验证失败**
   - 检查前后端 `JWT_SECRET` 是否一致
   - 确认token未过期

3. **CORS错误**
   - 检查后端 `CORS_ORIGIN` 配置

4. **同步失败**
   - 检查LDAP/SCIM配置
   - 查看后端日志 `backend/logs/app.log`

## 📝 开发规范

### Git提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 代码风格

- 使用ESLint + Prettier
- TypeScript严格模式
- 遵循Airbnb JavaScript风格指南

## 🚀 部署方案

支持多种部署方式：

1. **Docker Compose** - 快速部署
2. **Kubernetes** - 生产环境
3. **传统服务器** - PM2 + Nginx

详见 [部署文档](docs/deployment.md)

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交变更 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 📄 许可证

MIT License

## 📞 技术支持

如有问题，请联系技术团队或查看:
- [部署文档](docs/deployment.md)
- [集成文档](docs/integration.md)

---

**注意**: 本系统仅供学习参考，生产部署请务必修改所有默认配置和密钥。
