# 机房巡检系统 - 技术方案文档

## 📋 文档说明

本文档详细说明机房巡检系统的技术架构、核心功能、系统集成方案、安全设计等内容，为系统的开发、运维和扩展提供技术参考。

**版本**: v1.0.0  
**最后更新**: 2024-01-20

---

## 🏗️ 一、系统总体架构

### 1.1 架构设计原则

- **前后端分离**: 采用RESTful API，前端与后端独立部署、独立扩展
- **高可用性**: 支持多实例部署、负载均衡、故障自动转移
- **可扩展性**: 插件化架构，支持协议、功能、规模的灵活扩展
- **安全性**: 多层安全防护，包括认证、授权、加密、审计
- **高性能**: 异步处理、缓存优化、分布式架构

### 1.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                           客户端层                                │
├─────────────────────────────────────────────────────────────────┤
│  Web浏览器    │  移动端H5   │  管理终端    │  告警通知渠道        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         接入层 (Nginx)                            │
├─────────────────────────────────────────────────────────────────┤
│      HTTPS/SSL终结    │    负载均衡    │    静态资源缓存         │
└─────────────────────────────────────────────────────────────────┘
              │                             │
              ▼                             ▼
┌──────────────────────────┐    ┌──────────────────────────────┐
│     应用层 - 前端        │    │     应用层 - 后端              │
├──────────────────────────┤    ├──────────────────────────────┤
│   Next.js 16             │    │   Spring Boot 2.7.18         │
│   React 19               │    │   Java 8                     │
│   Tailwind CSS 4         │    │   MyBatis Plus               │
└──────────────────────────┘    └──────────────────────────────┘
                                            │
                    ┌───────────────────────┼───────────────────────┐
                    ▼                       ▼                       ▼
        ┌───────────────────┐    ┌─────────────────┐    ┌─────────────────┐
        │   数据服务层       │    │   外部集成层    │    │   监控采集层    │
├──────────────────────────┤    ├─────────────────┤    ├─────────────────┤
│ MySQL 5.7  │  Redis      │    │ SSO/OAuth2.0   │    │ SNMP/Modbus    │
│            │  Cache      │    │ 门禁系统API    │    │ BMS接口        │
│            │  Session    │    │ 钉钉/邮件/短信  │    │ 传感器网络     │
└───────────────────────────┘    └─────────────────┘    └─────────────────┘
                                            │
                    ┌───────────────────────┼───────────────────────┐
                    ▼                       ▼                       ▼
        ┌───────────────────┐    ┌─────────────────┐    ┌─────────────────┐
        │   基础设施层       │    │   运维监控层    │    │   数据中心     │
├──────────────────────────┤    ├─────────────────┤    ├─────────────────┤
│ Docker/K8s  │  虚拟机   │    │ Prometheus      │    │ 数据中心1       │
│ 负载均衡    │  防火墙   │    │ Grafana         │    │ 数据中心2       │
│ VPN隧道     │  备份     │    │ 日志系统        │    │ 数据中心N       │
└───────────────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## 💻 二、技术栈详解

### 2.1 前端技术栈

#### 核心框架
| 技术 | 版本 | 用途 |
|-----|------|------|
| Next.js | 16.0.10 | React框架，支持SSR/SSG |
| React | 19.2.1 | UI库 |
| TypeScript | 5.3 | 类型安全 |
| Tailwind CSS | 4.0 | CSS框架 |

#### 特性说明

**1. Next.js App Router**
- 采用最新的App Router架构
- 支持服务端组件和客户端组件
- 优化的路由和布局系统
- 内置API路由

**2. 科幻风格UI**
- 深色主题设计
- 霓虹光效（`shadow-[...]`）
- 渐变色背景（`bg-gradient-to-br`）
- 玻璃态效果（`backdrop-blur`）
- 响应式设计，支持多设备

**3. 状态管理**
- 使用 React Hooks (useState, useEffect)
- Context API 用于全局状态
- 本地存储（localStorage）用于持久化

**4. API 通信**
- Axios 封装
- 自动 Token 刷新
- 请求拦截器（添加认证）
- 响应拦截器（统一错误处理）

### 2.2 后端技术栈

#### 核心框架
| 技术 | 版本 | 用途 |
|-----|------|------|
| Java | 8 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| Spring Security | - | 安全框架 |
| MyBatis Plus | - | ORM框架 |
| Druid | 1.2.20 | 数据库连接池 |
| Hutool | 5.8.24 | 工具类库 |
| JWT | 0.12.3 | Token认证 |

#### 架构分层

```
┌─────────────────────────────────┐
│   Controller Layer (控制器层)    │  - 接收HTTP请求，参数验证
├─────────────────────────────────┤
│   Service Layer (业务逻辑层)     │  - 核心业务逻辑处理
├─────────────────────────────────┤
│   Mapper Layer (数据访问层)      │  - 数据库CRUD操作
├─────────────────────────────────┤
│   Entity Layer (实体层)         │  - 数据模型定义
└─────────────────────────────────┘
```

#### 核心组件

**1. 认证授权**
- JWT Token机制
- OAuth2.0 单点登录
- RBAC权限模型
- API拦截器

**2. 数据缓存**
- Redis缓存
- Spring Cache抽象
- 本地缓存
- 分布式缓存支持

**3. 任务调度**
- Spring @Scheduled
- 异步任务执行
- 定时数据采集
- 告警检查任务

**4. 消息通知**
- WebSocket实时推送
- 钉钉机器人
- 邮件发送
- 短信通知

### 2.3 数据库技术栈

#### MySQL 5.7
- 主数据库，存储业务数据
- InnoDB引擎，支持事务
- 主从复制支持
- 分库分表支持

#### Redis 6.0+
- 缓存热点数据
- Session存储
- 分布式锁
- 消息队列

---

## 🔐 三、安全设计方案

### 3.1 认证授权体系

#### 1. 认证方式

**本地认证**
```java
@Service
public class AuthService {
    // 用户名密码登录
    public LoginResponse login(LoginRequest request) {
        // 验证用户名密码
        // 生成JWT Token
        // 返回Token和用户信息
    }
}
```

**OAuth2.0 SSO认证**
```java
@RestController
@RequestMapping("/sso")
public class SSOController {
    // OAuth2授权回调
    @GetMapping("/callback/{provider}")
    public Result<LoginResponse> oauthCallback(
        @PathVariable String provider,
        @RequestParam String code,
        @RequestParam String state
    ) {
        // 根据不同提供商处理回调
        // 获取用户信息
        // 创建或更新本地用户
        // 生成Token
    }
}
```

#### 2. 权限模型（RBAC）

```
用户 (User)
  ↓ N:1
角色 (Role) ←→ 权限 (Permission)
  ↓ 1:N
资源 (Resource)
```

**权限控制实现**
```java
@Aspect
@Component
public class RBACAspect {
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) {
        // 获取当前用户
        // 检查用户权限
        // 决定是否放行
    }
}
```

### 3.2 数据安全

#### 1. 传输加密
- HTTPS/TLS 1.2+
- SSL证书
- 证书自动更新

#### 2. 数据加密
```yaml
encryption:
  secret-key: ${ENCRYPTION_SECRET_KEY:your-256-bit-key}
  enabled: true
```

- 敏感字段加密存储
- AES-256-GCM算法
- 密钥管理

#### 3. SQL注入防护
- MyBatis参数化查询
- 输入验证
- SQL防火墙（Druid）

### 3.3 审计日志

```java
@AuditLog(operation = "登录", module = "认证")
public LoginResponse login(LoginRequest request) {
    // 业务逻辑
}
```

**审计内容**
- 操作用户
- 操作时间
- 操作模块
- 操作类型
- 请求参数
- 响应结果

---

## 📡 四、系统集成方案

### 4.1 SSO单点登录

#### 1. 支持的协议
- OAuth2.0 / OpenID Connect
- SAML 2.0
- CAS

#### 2. 集成流程

```
┌─────────┐     1.登录请求      ┌─────────┐
│  前端   │ ──────────────────> │  SSO    │
└─────────┘                     └─────────┘
    │                               │
    │ 2.授权码                      │ 3.获取用户信息
    │<──────────────────────────────│
    │                               │
    │ 4.回调带授权码                 │
    │─────────────────────────────> │
    │                               │
    │ 6.返回Token                    │ 5.验证授权码
    │<──────────────────────────────│
    │                               │
    │ 7.使用Token访问                │
    ▼                               ▼
```

#### 3. 数据同步接口

遵循《IAM应用系统集成规范标准》：

```java
@RestController
@RequestMapping("/sync")
public class DataSyncController {
    
    // 组织同步
    @PostMapping("/organizations")
    public Result<Void> syncOrganizations(@RequestBody List<Organization> orgs) {
        // HMAC256签名验证
        // 批量创建/更新组织
    }
    
    // 用户同步
    @PostMapping("/users")
    public Result<Void> syncUsers(@RequestBody List<IAMUser> users) {
        // 验证签名
        // 批量创建/更新用户
    }
}
```

### 4.2 监控协议集成

#### 1. 插件化架构

```java
public interface MonitorProtocol {
    String getName();
    void connect(Properties config) throws Exception;
    void disconnect();
    List<MetricData> collect(MonitorTask task) throws Exception;
}
```

**支持的协议**
- SNMP (v1/v2c/v3)
- Modbus TCP
- BMS接口
- 传感器网络
- 消防主机通信

#### 2. 协议注册与使用

```java
@Component
public class ProtocolRegistry {
    private Map<String, MonitorProtocol> protocols = new ConcurrentHashMap<>();
    
    public void register(MonitorProtocol protocol) {
        protocols.put(protocol.getName(), protocol);
    }
    
    public MonitorProtocol getProtocol(String name) {
        return protocols.get(name);
    }
}
```

### 4.3 门禁系统集成

#### 1. 支持的厂商
- 海康威视
- 大华
- 宇视

#### 2. 统一接口

```java
public interface DoorAccessSystem {
    // 开门
    boolean openDoor(String doorId);
    
    // 查询门禁记录
    List<DoorAccessLog> queryLogs(Date startTime, Date endTime);
    
    // 临时授权
    void grantTempAccess(String userId, Date startTime, Date endTime);
}
```

#### 3. 事件监听

```java
@Component
public class DoorAccessEventListener {
    @EventListener
    public void onDoorAccessEvent(DoorAccessEvent event) {
        // 记录门禁日志
        // 触发告警检查
        // 推送通知
    }
}
```

### 4.4 告警通知集成

#### 1. 多渠道告警

**钉钉机器人**
```java
@Service
public class DingTalkAlertService {
    public void sendAlert(AlertMessage message) {
        // 调用钉钉Webhook
        // 发送Markdown消息
    }
}
```

**邮件发送**
```java
@Service
public class EmailAlertService {
    public void sendAlert(AlertMessage message) {
        // 使用JavaMail
        // 发送HTML邮件
    }
}
```

**短信通知**
```java
@Service
public class SMSAlertService {
    public void sendAlert(AlertMessage message) {
        // 阿里云短信
        // 腾讯云短信
    }
}
```

#### 2. 告警策略

```java
@Entity
public class AlertRule {
    // 告警条件
    private String metricName;
    private String operator;  // >, <, =, >=, <=, !=
    private Double threshold;
    
    // 告警级别
    private AlertLevel level;  // INFO, WARNING, CRITICAL
    
    // 通知渠道
    private List<String> channels;  // ["dingtalk", "email", "sms"]
    
    // 通知对象
    private List<String> recipients;
}
```

---

## 🎯 五、核心功能模块

### 5.1 巡检管理

#### 1. 巡检计划
```java
@Entity
public class Inspection {
    private Long id;
    private String name;
    private Date plannedTime;
    private Date actualTime;
    private String status;  // PLANNED, IN_PROGRESS, COMPLETED
    private Long inspectorId;
}
```

#### 2. 自定义巡检模板

```java
@Entity
public class InspectionTemplate {
    private String name;
    private String description;
    private List<InspectionTemplateItem> items;
}

@Entity
public class InspectionTemplateItem {
    private String itemName;
    private ItemType type;  // NUMERIC, STATUS, TEXT, IMAGE
    private String defaultValue;
    private Boolean required;
    private String validationRule;
}
```

#### 3. 巡检验证
- 拍照验证
- 定位验证
- 语音记录
- 签名确认

### 5.2 设备管理

#### 1. 设备信息
```java
@Entity
public class Device {
    private String name;
    private String type;
    private String ip;
    private String location;
    private String status;
    private Date installDate;
    private Date nextMaintenanceDate;
}
```

#### 2. 设备监控
- 实时数据采集
- 历史数据查询
- 性能分析
- 预测性维护

#### 3. 设备告警
- 阈值告警
- 状态变化告警
- 离线告警
- 维护提醒

### 5.3 机房管理

#### 1. 机房信息
```java
@Entity
public class Room {
    private String name;
    private String code;
    private String location;
    private Integer floor;
    private String description;
    private List<Device> devices;
}
```

#### 2. 机房布局
- 2D平面图
- 设备位置标记
- 温度热力图
- 空气质量分布

### 5.4 值班管理

#### 1. 班次管理
```java
@Entity
public class ShiftSchedule {
    private String name;
    private Date startDate;
    private Date endDate;
    private String shiftType;  // DAY, NIGHT, MORNING, AFTERNOON
    private List<Long> staffIds;
}
```

#### 2. 交接班记录
- 交接内容
- 异常情况
- 待处理事项
- 确认签名

---

## 🚀 六、扩展性设计

### 6.1 接口扩展

#### 1. API版本控制

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    String value();
}
```

```java
@RestController
@RequestMapping("/v1/devices")
@ApiVersion("v1")
public class DeviceControllerV1 {
    // v1版本接口
}

@RestController
@RequestMapping("/v2/devices")
@ApiVersion("v2")
public class DeviceControllerV2 {
    // v2版本接口，可能有不兼容变更
}
```

#### 2. 自定义协议插件

```java
@Component
public class CustomModbusProtocol implements MonitorProtocol {
    
    @Override
    public String getName() {
        return "custom-modbus";
    }
    
    @Override
    public List<MetricData> collect(MonitorTask task) {
        // 实现自定义Modbus采集逻辑
        return metrics;
    }
}
```

### 6.2 功能扩展

#### 1. AI图像识别

```java
@Service
public class AIRecognitionService {
    
    // 设备指示灯识别
    public DeviceStatus recognizeIndicator(String imagePath) {
        // 调用AI模型识别指示灯状态
        return status;
    }
    
    // 异常检测
    public List<Anomaly> detectAnomalies(String imagePath) {
        // 检测图片中的异常
        return anomalies;
    }
}
```

#### 2. 巡检路线优化

```java
@Service
public class RouteOptimizationService {
    
    public List<Room> optimizeRoute(List<Room> rooms, Point startLocation) {
        // 使用遗传算法或TSP算法优化巡检路线
        return optimizedRooms;
    }
}
```

#### 3. 预测性维护

```java
@Service
public class PredictiveMaintenanceService {
    
    public MaintenancePrediction predictMaintenance(Device device) {
        // 基于历史数据预测维护时间
        // 分析设备性能趋势
        return prediction;
    }
}
```

### 6.3 规模扩展

#### 1. 分布式数据采集

```java
@Entity
public class CollectorNode {
    private String nodeId;
    private String ip;
    private Integer port;
    private String status;  // ONLINE, OFFLINE
    private Integer deviceCount;
}

@Service
public class DistributedCollectionService {
    
    public void distributeTasks(List<CollectionTask> tasks) {
        // 根据节点负载分配采集任务
        // 负载均衡策略
    }
}
```

#### 2. 多数据中心管理

```java
@Entity
public class DataCenter {
    private String name;
    private String code;
    private String location;
    private String apiEndpoint;
    private String status;
}
```

#### 3. 分级分权管理

```java
@Entity
public class UserDataCenterPermission {
    private Long userId;
    private Long dataCenterId;
    private String permissionType;  // READ, WRITE, ADMIN
}
```

---

## 📊 七、性能优化方案

### 7.1 数据库优化

#### 1. 索引优化
```sql
-- 为常用查询字段创建索引
CREATE INDEX idx_device_status ON device(status);
CREATE INDEX idx_inspection_time ON inspection(planned_time);
CREATE INDEX idx_alarm_level ON alarm_record(level, create_time);
```

#### 2. 分表策略
- 按时间分表（月表、年表）
- 按业务分表（告警记录、监控数据）
- 历史数据归档

#### 3. 读写分离
- 主库写操作
- 从库读操作
- MyCat中间件

### 7.2 缓存优化

#### 1. Redis缓存策略
```java
@Service
@CacheConfig(cacheNames = "device")
public class DeviceService {
    
    @Cacheable(key = "#id")
    public Device getDevice(Long id) {
        return deviceMapper.selectById(id);
    }
    
    @CacheEvict(key = "#device.id")
    public void updateDevice(Device device) {
        deviceMapper.updateById(device);
    }
}
```

#### 2. 多级缓存
- 一级缓存：本地缓存（Caffeine）
- 二级缓存：Redis分布式缓存
- 热点数据预加载

### 7.3 异步处理

#### 1. 异步任务
```java
@Service
public class AsyncAlertService {
    
    @Async
    public void sendAlert(AlertMessage message) {
        // 异步发送告警，不阻塞主流程
        alertService.send(message);
    }
}
```

#### 2. 消息队列
- 使用Redis Pub/Sub
- 或使用RabbitMQ/Kafka
- 解耦告警发送

---

## 🔧 八、运维监控

### 8.1 应用监控

#### 1. 健康检查
```java
@RestController
@RequestMapping("/health")
public class HealthCheckController {
    
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("database", checkDatabase());
        status.put("redis", checkRedis());
        return Result.success(status);
    }
}
```

#### 2. 性能指标
- JVM内存使用
- 线程池状态
- 数据库连接池
- 接口响应时间

### 8.2 日志管理

#### 1. 日志级别
```yaml
logging:
  level:
    com.roominspection: DEBUG  # 开发环境
    com.roominspection: INFO   # 生产环境
```

#### 2. 日志分类
- 应用日志（application.log）
- 错误日志（error.log）
- 访问日志（access.log）
- 审计日志（audit.log）

### 8.3 告警监控

#### 1. Prometheus监控
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'room-inspection'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

#### 2. Grafana仪表盘
- 系统概览
- 接口性能
- 数据库性能
- 告警统计

---

## 🎨 九、UI/UX设计

### 9.1 设计风格

#### 科幻风格特点
- **深色主题**: 以深蓝、深紫为主色调
- **霓虹光效**: 使用 `shadow-[...]` 实现发光效果
- **渐变色**: `bg-gradient-to-br from-cyan-500 to-blue-600`
- **玻璃态**: `backdrop-blur` 毛玻璃效果
- **动画过渡**: 平滑的过渡动画

### 9.2 页面布局

#### 主布局
```tsx
<MainLayout>
  {/* 左侧菜单栏 */}
  <Sidebar />
  
  {/* 右侧内容区 */}
  <div className="flex-1">
    {/* 顶部导航 */}
    <Header />
    
    {/* 页面内容 */}
    <Content />
  </div>
</MainLayout>
```

### 9.3 组件设计

#### 1. 数据卡片
```tsx
<div className="bg-gradient-to-br from-gray-800/80 to-gray-900/80 
                 backdrop-blur-xl border border-cyan-500/30 
                 p-6 rounded-xl">
  <div className="text-3xl font-bold text-cyan-400">328</div>
  <div className="text-gray-400">设备总数</div>
</div>
```

#### 2. 表格组件
```tsx
<Table>
  <TableHeader>
    <TableRow>
      <TableHead className="text-gray-400">设备名称</TableHead>
      <TableHead className="text-gray-400">状态</TableHead>
    </TableRow>
  </TableHeader>
  <TableBody>
    {/* 数据行 */}
  </TableBody>
</Table>
```

---

## 📝 十、开发规范

### 10.1 代码规范

#### Java后端
- 遵循阿里巴巴Java开发规范
- 使用Lombok减少样板代码
- 统一异常处理
- API统一返回格式

#### 前端
- 遵循Airbnb JavaScript规范
- 使用TypeScript强类型
- 组件化开发
- 统一API调用

### 10.2 Git工作流

```bash
# 功能分支
feature/xxx
bugfix/xxx

# 提交规范
feat: 新功能
fix: 修复bug
docs: 文档
style: 格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 10.3 版本管理

采用语义化版本号：
- MAJOR.MINOR.PATCH
- MAJOR: 不兼容的API修改
- MINOR: 向下兼容的功能性新增
- PATCH: 向下兼容的问题修正

---

## 📚 十一、附录

### 11.1 系统端口说明

| 端口 | 服务 | 说明 |
|-----|------|------|
| 80 | HTTP | Nginx HTTP |
| 443 | HTTPS | Nginx HTTPS |
| 5000 | 前端 | Next.js前端服务 |
| 8080 | 后端 | Spring Boot后端API |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |
| 9090 | Prometheus | 监控 |
| 3000 | Grafana | 监控面板 |

### 11.2 关键配置文件

| 文件路径 | 说明 |
|---------|------|
| `java-backend/src/main/resources/application.yml` | 后端配置 |
| `java-backend/Dockerfile` | 后端Docker镜像 |
| `.env.local` | 前端环境变量 |
| `nginx/conf.d/default.conf` | Nginx配置 |

### 11.3 相关文档

- `ARCHITECTURE.md` - 系统架构设计
- `PERFORMANCE_OPTIMIZATION.md` - 性能优化方案
- `java-backend/docs/api-standard.md` - API接口规范
- `java-backend/docs/extensibility-design.md` - 扩展性设计
- `java-backend/docs/distributed-collection-architecture.md` - 分布式采集架构
- `java-backend/docs/multi-datacenter-architecture.md` - 多数据中心架构

---

**技术方案完成！** 本文档提供了完整的技术架构和实现方案，可作为开发和运维的参考指南。
