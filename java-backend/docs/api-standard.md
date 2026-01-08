# 机房巡检系统 API 接口规范

## 1. 概述

本规范定义机房巡检系统对外提供的RESTful API接口标准，支持第三方系统集成。

### 1.1 设计原则

- **RESTful风格**：遵循REST架构风格，使用HTTP方法表示操作类型
- **版本控制**：所有API接口支持版本控制，通过URL路径或请求头指定版本
- **统一响应格式**：所有接口返回统一的JSON格式响应
- **无状态**：API接口无状态，认证信息通过Token传递
- **幂等性**：GET、PUT、DELETE操作保证幂等性
- **安全性**：所有接口需要认证，敏感接口需要授权

### 1.2 版本管理

| 版本 | 路径前缀 | 状态 | 说明 |
|------|----------|------|------|
| v1 | /api/v1 | 当前版本 | 稳定版本，向后兼容 |
| v2 | /api/v2 | 开发中 | 新特性版本 |

## 2. 通用规范

### 2.1 基础URL

```
开发环境：http://dev.api.roominspection.com
测试环境：http://test.api.roominspection.com
生产环境：http://api.roominspection.com
```

### 2.2 请求方法

| 方法 | 说明 | 幂等性 |
|------|------|--------|
| GET | 查询资源 | 是 |
| POST | 创建资源 | 否 |
| PUT | 完整更新资源 | 是 |
| PATCH | 部分更新资源 | 否 |
| DELETE | 删除资源 | 是 |

### 2.3 请求头

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer {token}
X-API-Version: v1
X-Request-ID: {uuid}
X-Tenant-ID: {tenantId}
```

### 2.4 响应格式

#### 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890123
}
```

#### 错误响应
```json
{
  "code": 400,
  "message": "参数错误",
  "data": null,
  "timestamp": 1234567890123
}
```

### 2.5 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

## 3. 认证授权

### 3.1 Token认证

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3.2 OAuth2.0认证

系统支持OAuth2.0认证流程，包括授权码模式、简化模式、密码模式、客户端模式。

## 4. 核心接口

### 4.1 设备管理

#### 获取设备列表
```http
GET /api/v1/devices
```

**请求参数：**
- page: 页码
- size: 每页数量
- roomId: 机房ID（可选）
- deviceType: 设备类型（可选）

**响应示例：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 100,
    "records": [
      {
        "id": "1",
        "deviceName": "精密空调01",
        "deviceType": "AC",
        "roomId": "1",
        "status": "online",
        "ipAddress": "192.168.1.100"
      }
    ],
    "current": 1,
    "size": 10,
    "pages": 10
  },
  "timestamp": 1234567890123
}
```

#### 获取设备详情
```http
GET /api/v1/devices/{id}
```

#### 创建设备
```http
POST /api/v1/devices
```

**请求体：**
```json
{
  "deviceName": "精密空调02",
  "deviceType": "AC",
  "roomId": "1",
  "ipAddress": "192.168.1.101",
  "port": 161,
  "protocol": "SNMP",
  "community": "public"
}
```

#### 更新设备
```http
PUT /api/v1/devices/{id}
```

#### 删除设备
```http
DELETE /api/v1/devices/{id}
```

### 4.2 监控数据

#### 获取实时监控数据
```http
GET /api/v1/monitor/realtime/{deviceId}
```

#### 获取历史监控数据
```http
GET /api/v1/monitor/history
```

**请求参数：**
- deviceId: 设备ID
- metric: 指标名称
- startTime: 开始时间
- endTime: 结束时间
- interval: 采样间隔（1s/5s/1m/5m/1h）

**响应示例：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "deviceId": "1",
    "metric": "temperature",
    "dataPoints": [
      {
        "timestamp": 1234567890000,
        "value": 22.5
      },
      {
        "timestamp": 1234567891000,
        "value": 22.6
      }
    ]
  },
  "timestamp": 1234567890123
}
```

### 4.3 告警管理

#### 获取告警列表
```http
GET /api/v1/alarms
```

**请求参数：**
- page: 页码
- size: 每页数量
- level: 告警级别（info/warning/error/critical）
- status: 状态（pending/acknowledged/resolved）
- startTime: 开始时间
- endTime: 结束时间

#### 创建告警
```http
POST /api/v1/alarms
```

**请求体：**
```json
{
  "deviceId": "1",
  "alarmType": "temperature_high",
  "level": "warning",
  "message": "温度超过阈值",
  "threshold": 25,
  "currentValue": 28
}
```

#### 确认告警
```http
PATCH /api/v1/alarms/{id}/acknowledge
```

### 4.4 巡检管理

#### 获取巡检计划列表
```http
GET /api/v1/inspection/plans
```

#### 创建巡检计划
```http
POST /api/v1/inspection/plans
```

**请求体：**
```json
{
  "planName": "每日巡检",
  "planType": "daily",
  "roomId": "1",
  "devices": ["1", "2", "3"],
  "cronExpression": "0 9 * * ?",
  "inspectors": ["user1", "user2"]
}
```

#### 提交巡检结果
```http
POST /api/v1/inspection/results
```

**请求体：**
```json
{
  "planId": "1",
  "inspectorId": "user1",
  "inspectionTime": "2024-01-01T09:00:00",
  "items": [
    {
      "deviceId": "1",
      "checkItem": "temperature",
      "status": "normal",
      "value": "22",
      "remark": "正常"
    }
  ]
}
```

### 4.5 数据同步

#### 同步组织数据
```http
POST /api/v1/sync/organizations
```

**请求头：**
```
Authorization: Bearer {HMAC签名Token}
```

**请求体：**
```json
{
  "syncType": "incremental",
  "data": [
    {
      "orgId": "org1",
      "orgName": "技术部",
      "parentId": "",
      "level": 1,
      "sort": 1,
      "status": "active"
    }
  ]
}
```

#### 同步用户数据
```http
POST /api/v1/sync/users
```

#### 同步职位数据
```http
POST /api/v1/sync/jobs
```

### 4.6 AI识别接口

#### 设备指示灯识别
```http
POST /api/v1/ai/device-light-recognize
```

**请求体：**
```json
{
  "deviceId": "1",
  "imageBase64": "data:image/jpeg;base64,...",
  "lightPositions": [
    {
      "name": "power",
      "x": 100,
      "y": 200,
      "width": 20,
      "height": 20
    }
  ]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "识别成功",
  "data": {
    "deviceId": "1",
    "lights": [
      {
        "name": "power",
        "status": "on",
        "color": "green",
        "confidence": 0.98
      }
    ]
  },
  "timestamp": 1234567890123
}
```

#### 巡检路线优化建议
```http
POST /api/v1/ai/route-optimize
```

**请求体：**
```json
{
  "roomId": "1",
  "deviceIds": ["1", "2", "3", "4"],
  "startLocation": {
    "x": 0,
    "y": 0
  },
  "constraints": {
    "maxTime": 30,
    "priorityDevices": ["1", "3"]
  }
}
```

#### 预测性维护分析
```http
POST /api/v1/ai/predictive-maintenance
```

**请求体：**
```json
{
  "deviceId": "1",
  "predictionType": "failure",
  "timeRange": 30
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "分析成功",
  "data": {
    "deviceId": "1",
    "failureProbability": 0.75,
    "predictedFailureDate": "2024-02-01",
    "riskLevel": "high",
    "recommendations": [
      "建议在1月15日前进行维护",
      "检查设备运行参数"
    ]
  },
  "timestamp": 1234567890123
}
```

## 5. 监控协议插件接口

### 5.1 注册自定义协议
```http
POST /api/v1/plugins/protocols/register
```

**请求体：**
```json
{
  "protocolName": "CustomProtocol",
  "protocolType": "tcp",
  "version": "1.0",
  "description": "自定义监控协议",
  "configSchema": {
    "host": "string",
    "port": "integer",
    "timeout": "integer"
  }
}
```

### 5.2 上传协议插件
```http
POST /api/v1/plugins/protocols/upload
```

**请求体：** multipart/form-data
- file: 插件JAR文件
- protocolName: 协议名称

### 5.3 获取已注册协议列表
```http
GET /api/v1/plugins/protocols
```

### 5.4 删除协议插件
```http
DELETE /api/v1/plugins/protocols/{id}
```

## 6. 巡检模板接口

### 6.1 获取模板列表
```http
GET /api/v1/inspection/templates
```

### 6.2 创建巡检模板
```http
POST /api/v1/inspection/templates
```

**请求体：**
```json
{
  "templateName": "空调巡检模板",
  "deviceType": "AC",
  "items": [
    {
      "itemName": "温度检查",
      "itemType": "numeric",
      "unit": "℃",
      "minValue": 18,
      "maxValue": 25,
      "required": true
    },
    {
      "itemName": "指示灯检查",
      "itemType": "status",
      "options": ["normal", "abnormal"],
      "required": true
    }
  ]
}
```

### 6.3 更新巡检模板
```http
PUT /api/v1/inspection/templates/{id}
```

### 6.4 删除巡检模板
```http
DELETE /api/v1/inspection/templates/{id}
```

## 7. 多数据中心接口

### 7.1 获取数据中心列表
```http
GET /api/v1/datacenters
```

### 7.2 获取跨数据中心汇总数据
```http
GET /api/v1/datacenters/summary
```

**响应示例：**
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalDevices": 500,
    "onlineDevices": 480,
    "offlineDevices": 20,
    "totalAlarms": 15,
    "criticalAlarms": 2,
    "dataCenters": [
      {
        "id": "dc1",
        "name": "北京数据中心",
        "devices": 200,
        "alarms": 5
      },
      {
        "id": "dc2",
        "name": "上海数据中心",
        "devices": 300,
        "alarms": 10
      }
    ]
  },
  "timestamp": 1234567890123
}
```

## 8. 分级分权接口

### 8.1 获取用户权限
```http
GET /api/v1/users/{id}/permissions
```

### 8.2 分配权限
```http
POST /api/v1/users/{id}/permissions
```

**请求体：**
```json
{
  "permissions": ["device:view", "device:edit", "alarm:acknowledge"]
}
```

### 8.3 获取角色列表
```http
GET /api/v1/roles
```

### 8.4 创建角色
```http
POST /api/v1/roles
```

**请求体：**
```json
{
  "roleName": "巡检员",
  "description": "负责设备巡检",
  "permissions": [
    "device:view",
    "inspection:submit",
    "alarm:view"
  ]
}
```

## 9. 错误码规范

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 40001 | 参数校验失败 | 检查请求参数格式 |
| 40002 | Token无效 | 重新登录获取Token |
| 40003 | Token过期 | 刷新Token或重新登录 |
| 40301 | 无权限访问 | 联系管理员分配权限 |
| 40401 | 资源不存在 | 检查资源ID是否正确 |
| 40901 | 资源冲突 | 检查资源是否已存在 |
| 50001 | 数据库操作失败 | 联系技术支持 |
| 50002 | 第三方接口调用失败 | 检查第三方服务状态 |
| 50003 | 文件上传失败 | 检查文件格式和大小 |

## 10. 接口限流规则

| 限流维度 | 限流规则 |
|----------|----------|
| IP限流 | 1000次/分钟 |
| 用户限流 | 500次/分钟 |
| 接口限流 | 根据接口重要性配置不同限流值 |

## 11. 第三方系统集成指南

### 11.1 申请API密钥

联系系统管理员申请API密钥，包括：
- App ID
- App Secret

### 11.2 生成签名

```java
public static String generateSignature(String appId, String appSecret, String timestamp) {
    String data = appId + appSecret + timestamp;
    return DigestUtils.md5Hex(data);
}
```

### 11.3 调用示例

```javascript
const axios = require('axios');

const appId = 'your_app_id';
const appSecret = 'your_app_secret';
const timestamp = Date.now().toString();
const signature = generateSignature(appId, appSecret, timestamp);

axios.get('http://api.roominspection.com/api/v1/devices', {
  headers: {
    'X-App-ID': appId,
    'X-Signature': signature,
    'X-Timestamp': timestamp
  }
}).then(response => {
  console.log(response.data);
});
```

## 12. 附录

### 12.1 监控指标字典

| 指标编码 | 指标名称 | 单位 | 说明 |
|----------|----------|------|------|
| temperature | 温度 | ℃ | 设备运行温度 |
| humidity | 湿度 | %RH | 环境湿度 |
| voltage | 电压 | V | 电压值 |
| current | 电流 | A | 电流值 |
| power | 功率 | W | 功率值 |
| airflow | 风量 | m³/h | 风量 |
| pressure | 压力 | Pa | 压力值 |

### 12.2 设备类型字典

| 类型编码 | 类型名称 | 说明 |
|----------|----------|------|
| AC | 精密空调 | 空调设备 |
| UPS | UPS电源 | 不间断电源 |
| PDU | 配电柜 | 配电设备 |
| FIRE | 消防设备 | 消防监控设备 |
| ENV | 环境传感器 | 环境监测设备 |
| SWITCH | 网络交换机 | 网络设备 |
| SERVER | 服务器 | 计算设备 |
| STORAGE | 存储设备 | 存储设备 |

### 12.3 告警级别字典

| 级别编码 | 级别名称 | 处理时限 |
|----------|----------|----------|
| info | 信息 | 不处理 |
| warning | 警告 | 24小时内 |
| error | 错误 | 4小时内 |
| critical | 严重 | 立即处理 |
