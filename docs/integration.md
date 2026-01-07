# 第三方系统集成文档

## 目录
- [单点登录 (SSO) 集成](#单点登录-sso-集成)
- [组织架构同步](#组织架构同步)
- [HR系统集成](#hr系统集成)
- [认证协议支持](#认证协议支持)

---

## 单点登录 (SSO) 集成

系统支持多种主流的单点认证协议，可根据企业现有基础设施选择合适的方案。

### 支持的SSO类型

1. **OAuth2/OIDC** (推荐)
   - Azure AD
   - Keycloak
   - Auth0
   - Okta
   - 自定义OAuth2服务

2. **SAML 2.0**
   - 企业AD FS
   - Shibboleth
   - 标准SAML服务

3. **CAS**
   - CAS Server 3.x/4.x

### OAuth2/OIDC 集成

#### 1. 在SSO平台注册应用

以 **Azure AD** 为例:

```bash
1. 登录 Azure Portal
2. 进入 Azure Active Directory > 应用注册
3. 点击"新注册"
4. 填写应用信息:
   - 名称: 机房巡检系统
   - 重定向URI: http://your-domain.com/auth/callback/oauth2
5. 获取:
   - 应用程序(客户端) ID (CLIENT_ID)
   - 客户端密钥 (CLIENT_SECRET)
   - 租户ID (TENANT_ID)
```

以 **Keycloak** 为例:

```bash
1. 登录 Keycloak 管理控制台
2. 创建 Realm
3. 创建 Client:
   - Client ID: inspection-system
   - Client Protocol: openid-connect
   - Access Type: confidential
   - Valid Redirect URIs: http://your-domain.com/auth/callback/*
4. 获取:
   - Client ID
   - Client Secret
```

#### 2. 配置后端环境变量

编辑 `backend/.env`:

```bash
# 启用OAuth2
SSO_TYPE=oauth2

# OAuth2配置
OAUTH_CLIENT_ID=your-client-id
OAUTH_CLIENT_SECRET=your-client-secret
OAUTH_CALLBACK_URL=https://your-domain.com/api/v1/auth/callback/oauth2
OAUTH_AUTHORIZATION_URL=https://sso.example.com/oauth/authorize
OAUTH_TOKEN_URL=https://sso.example.com/oauth/token
OAUTH_USER_INFO_URL=https://sso.example.com/oauth/userinfo
OAUTH_SCOPE=openid profile email

# 各平台特定配置
# Azure AD
OAUTH_AUTHORIZATION_URL=https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/authorize
OAUTH_TOKEN_URL=https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token
OAUTH_USER_INFO_URL=https://graph.microsoft.com/v1.0/me

# Keycloak
OAUTH_AUTHORIZATION_URL=https://keycloak.example.com/realms/your-realm/protocol/openid-connect/auth
OAUTH_TOKEN_URL=https://keycloak.example.com/realms/your-realm/protocol/openid-connect/token
OAUTH_USER_INFO_URL=https://keycloak.example.com/realms/your-realm/protocol/openid-connect/userinfo
```

#### 3. 配置前端环境变量

编辑 `.env.local`:

```bash
NEXT_PUBLIC_SSO_ENABLED=true
NEXT_PUBLIC_SSO_TYPE=oauth2
NEXT_PUBLIC_OAUTH_CLIENT_ID=your-client-id
NEXT_PUBLIC_OAUTH_AUTHORIZATION_URL=https://sso.example.com/oauth/authorize
```

#### 4. 配置用户映射

创建 `backend/src/config/oauth-mapping.ts`:

```typescript
export const userMapping = {
  // 用户ID字段映射
  userId: 'sub' || 'id' || 'oid',
  
  // 用户名映射
  username: 'preferred_username' || 'user_name' || 'upn',
  
  // 邮箱映射
  email: 'email' || 'mail',
  
  // 真实姓名映射
  realName: (profile: any) => {
    return profile.name || `${profile.given_name} ${profile.family_name}`;
  },
  
  // 外部系统ID
  externalId: 'sub' || 'oid',
};
```

### SAML 2.0 集成

#### 1. 获取SAML元数据

从企业的SAML身份提供商(IdP)获取元数据文件或配置信息。

#### 2. 配置后端

编辑 `backend/.env`:

```bash
SSO_TYPE=saml

# SAML配置
SAML_ENTRY_POINT=https://sso.example.com/saml/sso
SAML_ISSUER=room-inspection-system
SAML_CALLBACK_URL=https://your-domain.com/api/v1/auth/callback/saml
SAML_CERT=-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----
```

#### 3. 配置SAML属性映射

在 `backend/src/strategies/saml.strategy.ts` 中配置:

```typescript
const profile = {
  id: profile.nameID || profile.attributes['uid'],
  username: profile.attributes['mailnickname'] || profile.nameID,
  email: profile.attributes['mail'] || profile.attributes['email'],
  realName: profile.attributes['displayname'],
  externalId: profile.nameID,
};
```

#### 4. 在IdP注册服务提供方(SP)

提供以下信息给IdP管理员:
- ACS URL: `https://your-domain.com/api/v1/auth/callback/saml`
- Entity ID: `room-inspection-system`
- 断言消费者服务 (ACS)
- Name ID 格式

### CAS 集成

```bash
SSO_TYPE=cas
CAS_BASE_URL=https://sso.example.com/cas
CAS_SERVICE_URL=https://your-domain.com/api/v1/auth/callback/cas
```

---

## 组织架构同步

系统支持从LDAP/AD或HR系统自动同步组织架构和用户数据。

### LDAP/AD 同步

#### 1. 配置LDAP连接

编辑 `backend/.env`:

```bash
# 启用数据同步
SYNC_ENABLED=true
SYNC_ADAPTER=ldap
SYNC_SCHEDULE=0 2 * * *  # 每日凌晨2点同步

# LDAP配置
LDAP_URL=ldap://ldap.example.com:389
LDAP_BIND_DN=cn=admin,dc=example,dc=com
LDAP_BIND_PASSWORD=admin-password
LDAP_SEARCH_BASE=ou=users,dc=example,dc=com
LDAP_SEARCH_FILTER=(objectClass=person)
LDAP_ORG_BASE=ou=groups,dc=example,dc=com

# 启用TLS (可选)
LDAP_STARTTLS=true
LDAP_TLS_CERT_PATH=/path/to/cert.pem
```

#### 2. 配置用户字段映射

编辑 `backend/src/adapters/ldap.adapter.ts`:

```typescript
export const ldapUserMapping = {
  // LDAP属性 -> 系统字段
  username: 'sAMAccountName',      // Active Directory
  // username: 'uid',               // OpenLDAP
  email: 'mail',
  realName: 'displayName',
  phone: 'telephoneNumber',
  employeeId: 'employeeNumber',
  departmentId: 'department',      // 关联部门
};

export const ldapOrgMapping = {
  code: 'ou',                      // 组织单元
  name: 'description',
  parentId: 'memberOf',
};
```

#### 3. 同步策略

全量同步流程:

```
1. 连接LDAP服务器
2. 读取所有组织单元
3. 构建组织树
4. 读取所有用户
5. 映射用户字段
6. 创建/更新用户
7. 记录同步日志
```

增量同步流程:

```
1. 查询上次同步时间后的变更
2. 处理新增/修改的用户
3. 处理删除的用户
4. 记录同步日志
```

#### 4. 手动触发同步

```bash
curl -X POST http://localhost:3000/api/v1/sync/trigger \
  -H "Authorization: Bearer <admin-token>"
```

### SCIM 同步

SCIM (System for Cross-domain Identity Management) 是行业标准协议。

#### 1. 配置SCIM

```bash
SYNC_ADAPTER=scim
SCIM_BASE_URL=https://hr.example.com/scim/v2
SCIM_TOKEN=your-scim-bearer-token
SCIM_SYNC_ON_STARTUP=true
```

#### 2. SCIM端点

```bash
# 获取所有用户
GET /scim/v2/Users

# 获取所有群组
GET /scim/v2/Groups

# 创建用户
POST /scim/v2/Users

# 更新用户
PUT /scim/v2/Users/{id}

# 删除用户
DELETE /scim/v2/Users/{id}
```

#### 3. SCIM适配器实现

创建 `backend/src/adapters/scim.adapter.ts`:

```typescript
export class ScimAdapter implements DataSyncAdapter {
  async fetchUsers(): Promise<User[]> {
    const response = await axios.get(`${this.baseUrl}/Users`, {
      headers: { Authorization: `Bearer ${this.token}` },
    });

    return response.data.Resources.map((resource: any) =>
      this.mapUser(resource)
    );
  }

  mapUser(scimUser: any): User {
    return {
      externalId: scimUser.id,
      username: scimUser.userName,
      email: scimUser.emails?.[0]?.value,
      realName: scimUser.displayName,
      // ...
    };
  }
}
```

---

## HR系统集成

对于使用自定义API的HR系统，创建自定义适配器。

### 1. 配置自定义API

```bash
SYNC_ADAPTER=custom
CUSTOM_API_BASE_URL=https://hr.example.com/api
CUSTOM_API_TOKEN=your-api-token
CUSTOM_API_USER_ENDPOINT=/users
CUSTOM_API_ORG_ENDPOINT=/organizations
```

### 2. 创建自定义适配器

创建 `backend/src/adapters/custom.adapter.ts`:

```typescript
export class CustomAdapter implements DataSyncAdapter {
  private baseUrl: string;
  private token: string;

  constructor() {
    this.baseUrl = config.sync.custom.apiUrl;
    this.token = config.sync.custom.apiToken;
  }

  async connect(): Promise<boolean> {
    try {
      await axios.get(`${this.baseUrl}/health`);
      return true;
    } catch {
      return false;
    }
  }

  async fetchUsers(): Promise<User[]> {
    const response = await axios.get(`${this.baseUrl}/users`, {
      headers: { Authorization: `Bearer ${this.token}` },
    });

    return response.data.data.map((item: any) => this.mapUser(item));
  }

  mapUser(hrUser: any): User {
    return {
      username: hrUser.username,
      email: hrUser.email,
      realName: hrUser.full_name,
      employeeId: hrUser.emp_id,
      phone: hrUser.mobile,
      departmentId: hrUser.dept_id,
      externalId: hrUser.id.toString(),
      source: 'HR_SYSTEM',
    };
  }

  async fetchOrganizations(): Promise<Organization[]> {
    const response = await axios.get(`${this.baseUrl}/organizations`, {
      headers: { Authorization: `Bearer ${this.token}` },
    });

    return response.data.data.map((item: any) => this.mapOrganization(item));
  }

  mapOrganization(hrOrg: any): Organization {
    return {
      code: hrOrg.code,
      name: hrOrg.name,
      parentId: hrOrg.parent_id || null,
      description: hrOrg.description,
      externalId: hrOrg.id.toString(),
    };
  }
}
```

### 3. 注册适配器

在 `backend/src/services/sync.service.ts` 中注册:

```typescript
import { CustomAdapter } from '../adapters/custom.adapter';

const adapters: Record<string, DataSyncAdapter> = {
  ldap: new LdapAdapter(),
  scim: new ScimAdapter(),
  custom: new CustomAdapter(),
};
```

---

## 认证协议支持

### OAuth2 2.0 授权流程

#### 授权码模式 (推荐)

```
1. 用户点击"SSO登录"
2. 前端重定向到: {authorization_url}?client_id={client_id}&redirect_uri={callback_url}&response_type=code&scope={scope}
3. 用户在SSO平台登录
4. SSO平台回调: {callback_url}?code={authorization_code}
5. 后端用code换取access_token: POST {token_url}
6. 后端用access_token获取用户信息: GET {user_info_url}
7. 后端生成JWT返回给前端
```

### SAML 2.0 断言流程

```
1. 前端发起登录请求
2. 后端生成SAML AuthnRequest
3. 前端重定向到IdP
4. 用户在IdP登录
5. IdP返回SAML Response (断言)
6. 后端验证断言签名和有效期
7. 后端提取用户信息并生成JWT
```

### CAS 认证流程

```
1. 前端重定向到: {cas_base_url}/login?service={service_url}
2. 用户在CAS登录
3. CAS重定向回: {service_url}?ticket={service_ticket}
4. 后端验证ticket: {cas_base_url}/validate?ticket={ticket}&service={service_url}
5. 验证成功后生成JWT
```

---

## 用户自动创建策略

SSO登录时自动创建用户的逻辑:

### 1. 匹配规则

```typescript
// 优先级从高到低
1. externalId 匹配 (最准确)
2. username 匹配
3. email 匹配
```

### 2. 创建逻辑

```typescript
if (!existingUser) {
  const defaultRole = await getDefaultRole(); // 'user' 角色
  
  const newUser = await prisma.user.create({
    data: {
      username: profile.username,
      email: profile.email,
      realName: profile.realName,
      externalId: profile.externalId,
      source: 'SSO',
      status: 'ACTIVE',
      roles: {
        create: [{
          role: { connect: { id: defaultRole.id } }
        }]
      }
    }
  });
}
```

### 3. 自动更新

每次SSO登录时更新用户信息:

```typescript
await prisma.user.update({
  where: { id: user.id },
  data: {
    email: profile.email,
    realName: profile.realName,
    lastLoginAt: new Date()
  }
});
```

---

## 测试SSO集成

### 1. 本地测试

使用Keycloak Docker容器:

```bash
docker run -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

### 2. 测试工具

- **SAML**: https://samltest.id/
- **OAuth2**: https://oauth-playground.com/
- **CAS**: https://apereo.github.io/cas/

### 3. 调试日志

启用调试日志:

```bash
# backend/.env
LOG_LEVEL=debug
```

查看日志:

```bash
tail -f backend/logs/app.log
```

---

## 安全最佳实践

### 1. 密钥管理

- 使用强密钥 (至少32字节)
- 定期轮换JWT密钥
- 使用环境变量或密钥管理服务存储密钥

### 2. Token安全

- 使用HTTPS传输token
- 设置合理的过期时间
- 实现token刷新机制

### 3. 防止CSRF

- 验证state参数 (OAuth2)
- 使用SameSite Cookie

### 4. 审计日志

记录所有SSO登录事件:

```typescript
await prisma.auditLog.create({
  data: {
    userId: user.id,
    action: 'SSO_LOGIN',
    resource: 'auth',
    method: 'POST',
    path: '/auth/sso/callback',
    ip: req.ip,
    statusCode: 200
  }
});
```

---

## 故障排查

### 问题1: 回调URL不匹配

**原因**: SSO平台配置的回调URL与实际不符

**解决**: 检查 `OAUTH_CALLBACK_URL` 或 `SAML_CALLBACK_URL` 配置

### 问题2: 用户信息获取失败

**原因**: API端点配置错误或权限不足

**解决**: 
- 检查 `OAUTH_USER_INFO_URL`
- 确认scope包含所需权限
- 检查token有效性

### 问题3: 同步失败

**原因**: LDAP/SCIM连接失败

**解决**:
- 检查网络连接
- 验证认证凭据
- 查看后端日志

### 问题4: 用户无法自动创建

**原因**: 缺少默认角色或字段映射错误

**解决**:
- 确保 'user' 角色存在
- 检查字段映射配置
- 启用调试日志查看详细信息

---

## 技术支持

如有集成问题，请提供以下信息:

1. SSO平台类型和版本
2. 后端日志 (logs/app.log)
3. 前端控制台错误
4. 配置文件 (隐藏敏感信息)
