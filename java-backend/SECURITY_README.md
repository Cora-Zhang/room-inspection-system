# 机房巡检系统安全功能实现文档

## 概述

本文档描述了机房巡检系统的安全功能实现，包括数据传输加密、操作日志审计、用户权限管理、门禁接口双向认证和敏感数据加密存储。

## 1. 数据传输加密

### 1.1 HTTPS支持

系统支持HTTPS加密传输，在SecurityConfig中已配置：

```java
// 强制HTTPS（生产环境启用）
// http.requiresChannel().anyRequest().requiresSecure();
```

**生产环境启用方式：**
- 在`application.yml`中配置SSL证书
- 取消注释SecurityConfig中的HTTPS强制代码

### 1.2 VPN支持

系统支持通过VPN访问，确保远程访问的安全性。

## 2. 操作日志审计

### 2.1 功能特性

- 完整记录用户所有操作
- 支持审计追溯
- 异步记录，不影响性能
- 敏感数据加密存储

### 2.2 使用方式

在Controller方法上添加`@AuditLog`注解：

```java
@AuditLog(operationType = "CREATE", module = "DEVICE", description = "创建设备")
@PostMapping("/devices")
public Result<Void> createDevice(@RequestBody Device device) {
    // 业务逻辑
}
```

### 2.3 日志内容

记录内容包括：
- 操作人信息（ID、用户名、姓名）
- 操作时间
- 操作类型（登录、创建、更新、删除等）
- 操作模块（用户管理、设备管理等）
- 请求URL和方法
- 请求参数（加密存储）
- 响应结果（加密存储）
- 执行耗时
- IP地址、浏览器、操作系统

### 2.4 查询接口

```
GET /api/security/audit-logs
```

## 3. 用户权限管理（RBAC）

### 3.1 RBAC模型

系统采用基于角色的访问控制（RBAC）模型：
- 用户（User）
- 角色（Role）
- 权限（Permission）
- 用户角色关联（UserRole）
- 角色权限关联（RolePermission）

### 3.2 预置角色

| 角色编码 | 角色名称 | 权限级别 | 数据范围 |
|---------|---------|---------|---------|
| ADMIN | 超级管理员 | 1 | 全部数据 |
| MANAGER | 管理员 | 2 | 本部门及以下 |
| SUPERVISOR | 主管 | 3 | 本部门及以下 |
| USER | 普通用户 | 4 | 仅本人 |
| INSPECTOR | 巡检员 | 4 | 仅本人 |
| MAINTAINER | 维修员 | 4 | 仅本人 |
| VIEWER | 查看员 | 5 | 全部数据（只读）|

### 3.3 权限类型

- **菜单权限（MENU）**：控制前端菜单和路由
- **按钮权限（BUTTON）**：控制页面按钮显示
- **接口权限（API）**：控制后端接口访问

### 3.4 权限验证

在Controller方法上使用`@PreAuthorize`注解：

```java
@PreAuthorize("hasAuthority('user:create')")
@PostMapping("/users")
public Result<Void> createUser(@RequestBody User user) {
    // 业务逻辑
}
```

## 4. 门禁接口双向认证

### 4.1 双向SSL认证

系统实现了双向SSL认证，用于与门禁系统进行安全通信。

**配置方式：**
在`application.yml`中配置SSL密钥库和信任库：

```yaml
security:
  ssl:
    key-store: /path/to/client.jks
    key-store-password: changeit
    trust-store: /path/to/truststore.jks
    trust-store-password: changeit
```

### 4.2 签名机制

系统为门禁接口请求生成签名，防止数据篡改：

```java
public String generateSignature(String apiSecret, String timestamp,
                               String nonce, String params) {
    String signData = apiSecret + timestamp + nonce + params;
    return encryptionUtil.sha256Hash(signData);
}
```

### 4.3 使用示例

```java
// 下发门禁权限
JSONObject result = doorAccessAuthService.grantPermission(
    "hikvision",  // 系统类型
    staffId,      // 人员ID
    cardNumber,   // 卡号
    deviceId,     // 设备ID
    startTime,    // 开始时间
    endTime       // 结束时间
);

// 回收门禁权限
JSONObject result = doorAccessAuthService.revokePermission(
    "hikvision",  // 系统类型
    staffId,      // 人员ID
    deviceId      // 设备ID
);
```

## 5. 敏感数据加密存储

### 5.1 加密算法

- **AES-GCM**：对称加密，用于敏感字段加密
- **SHA-256**：哈希算法，用于密码加密

### 5.2 加密工具类

```java
@Autowired
private EncryptionUtil encryptionUtil;

// 加密
String encrypted = encryptionUtil.encrypt("sensitive-data");

// 解密
String decrypted = encryptionUtil.decrypt(encrypted);

// 哈希
String hashed = encryptionUtil.sha256Hash("password");
```

### 5.3 加密字段

以下字段已实现加密存储：
- 操作日志的请求参数
- 操作日志的响应结果
- 值班人员手机号（部分掩码）
- 门禁日志的敏感信息

## 6. 密码策略

系统支持密码强度策略配置：

```yaml
password:
  min-length: 8                    # 最小长度
  require-digit: true             # 必须包含数字
  require-lowercase: true          # 必须包含小写字母
  require-uppercase: true         # 必须包含大写字母
  require-special-char: true      # 必须包含特殊字符
  expire-days: 90                 # 密码有效期
  max-failed-attempts: 5          # 最大失败次数
  lock-time: 30                   # 锁定时间（分钟）
```

## 7. JWT认证

### 7.1 登录流程

1. 用户提交用户名和密码
2. 后端验证用户信息
3. 生成JWT Token和Refresh Token
4. 返回Token给前端

### 7.2 Token使用

前端在请求头中携带Token：

```
Authorization: Bearer <token>
```

### 7.3 Token刷新

Token过期后，使用Refresh Token获取新的Token：

```
POST /api/security/refresh
{
  "refreshToken": "<refresh-token>"
}
```

## 8. 数据库初始化

执行以下SQL脚本初始化安全功能表和数据：

```bash
mysql -u root -p room_inspection < java-backend/src/main/resources/sql/security_init.sql
```

## 9. 接口列表

### 9.1 认证接口

- `POST /api/security/login` - 用户登录
- `POST /api/security/refresh` - 刷新Token
- `GET /api/security/user/info` - 获取当前用户信息
- `POST /api/security/user/change-password` - 修改密码

### 9.2 角色管理接口

- `GET /api/security/roles` - 获取角色列表
- `POST /api/security/roles` - 创建角色
- `PUT /api/security/roles/{id}` - 更新角色
- `DELETE /api/security/roles/{id}` - 删除角色
- `POST /api/security/roles/{roleId}/permissions` - 为角色分配权限

### 9.3 权限管理接口

- `GET /api/security/permissions/tree` - 获取权限树

### 9.4 操作日志接口

- `GET /api/security/audit-logs` - 查询操作日志

## 10. 安全配置参数

| 参数 | 说明 | 默认值 |
|-----|------|-------|
| JWT_SECRET | JWT密钥 | - |
| JWT_EXPIRATION | Token有效期（毫秒） | 86400000（24小时） |
| JWT_REFRESH_EXPIRATION | Refresh Token有效期（毫秒） | 604800000（7天） |
| ENCRYPTION_SECRET_KEY | 数据加密密钥 | - |
| SSL_KEY_STORE | SSL密钥库路径 | - |
| SSL_TRUST_STORE | SSL信任库路径 | - |

## 11. 生产环境注意事项

1. **修改默认密钥**：务必修改JWT_SECRET和ENCRYPTION_SECRET_KEY
2. **启用HTTPS**：取消SecurityConfig中HTTPS强制代码的注释
3. **配置SSL证书**：为HTTPS和双向SSL认证配置有效证书
4. **密码策略**：根据安全需求调整密码策略
5. **日志保留**：设置合理的操作日志保留天数
6. **定期备份**：定期备份加密密钥和SSL证书

## 12. 性能优化

- 操作日志异步记录，不影响业务性能
- 使用Redis缓存用户权限信息
- 使用线程池处理异步任务
- 数据库索引优化查询性能

## 13. 故障排查

### 13.1 Token验证失败

检查：
- JWT_SECRET配置是否正确
- Token是否过期
- 用户状态是否正常

### 13.2 双向SSL认证失败

检查：
- SSL密钥库和信任库配置是否正确
- 证书是否有效
- 证书链是否完整

### 13.3 权限验证失败

检查：
- 用户是否分配了角色
- 角色是否分配了权限
- 权限编码是否正确

## 14. 总结

本系统实现了完整的安全功能体系：
- ✅ 数据传输加密（HTTPS、VPN）
- ✅ 操作日志完整记录（审计追溯）
- ✅ 用户权限分级管理（RBAC）
- ✅ 门禁接口双向认证
- ✅ 敏感数据加密存储

所有安全功能均已实现并经过测试，满足机房巡检系统的安全需求。
