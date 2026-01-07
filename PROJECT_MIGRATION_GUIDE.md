# 机房巡检系统 - 技术栈迁移完整指南

## 一、项目概述

本项目已从 **Next.js + Node.js + PostgreSQL** 迁移至 **Vue3 + Java8 + MySQL5.7** 架构，并新增了多项企业级功能。

## 二、技术栈详情

### 后端技术栈
- **Java 8** - 基础开发语言
- **Spring Boot 2.7.18** - 核心框架
- **Spring Security** - 安全框架
- **Spring Security OAuth2** - OAuth2.0单点登录支持
- **MyBatis Plus** - ORM框架
- **MySQL Connector 8.0.33** - 数据库驱动
- **Druid** - 数据库连接池
- **Redis** - 缓存支持
- **JWT** - Token认证
- **SNMP4J** - SNMP监控协议
- **Modbus4J** - Modbus监控协议
- **Apache POI** - Excel处理

### 前端技术栈
- **Vue 3.4** - 前端框架
- **Vite 5.1** - 构建工具
- **TypeScript 5.3** - 类型系统
- **Element Plus 2.6** - UI组件库
- **Vue Router 4.3** - 路由管理
- **Pinia 2.1** - 状态管理
- **Axios** - HTTP客户端
- **ECharts 5.5** - 数据可视化

### 数据库
- **MySQL 5.7** - 主数据库
- **Redis** - 缓存数据库

## 三、项目结构

### 后端结构（java-backend）
```
java-backend/
├── src/main/java/com/roominspection/backend/
│   ├── common/              # 公共类
│   │   ├── Result.java      # 统一响应结果
│   │   └── PageResult.java # 分页结果
│   ├── controller/          # 控制器层
│   │   ├── AuthController.java      # 认证控制器
│   │   ├── RoomController.java      # 机房管理
│   │   ├── DeviceController.java    # 设备管理
│   │   ├── InspectionController.java # 巡检管理
│   │   ├── ScheduleController.java   # 值班管理
│   │   └── SystemController.java    # 系统管理
│   ├── service/             # 服务层
│   ├── mapper/              # 数据访问层
│   ├── entity/              # 实体类
│   ├── dto/                 # 数据传输对象
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java      # 安全配置
│   │   ├── RedisConfig.java        # Redis配置
│   │   └── OAuth2Config.java       # OAuth2配置
│   ├── security/            # 安全相关
│   │   ├── JwtTokenProvider.java   # JWT工具类
│   │   └── CustomUserDetails.java # 用户详情
│   ├── monitor/             # 监控协议
│   │   ├── SnmpService.java       # SNMP服务
│   │   ├── ModbusService.java     # Modbus服务
│   │   └── BmsService.java        # BMS接口
│   ├── access/              # 门禁对接
│   │   ├── HikvisionService.java # 海康威视
│   │   └── DahuaService.java     # 大华
│   └── alert/               # 告警通知
│       ├── DingTalkService.java  # 钉钉
│       ├── EmailService.java     # 邮件
│       └── SmsService.java       # 短信
├── src/main/resources/
│   ├── application.yml      # 应用配置
│   ├── sql/
│   │   └── init.sql        # 数据库初始化脚本
│   └── mapper/             # MyBatis映射文件
└── pom.xml                  # Maven配置
```

### 前端结构（vue-frontend）
```
vue-frontend/
├── src/
│   ├── api/                 # API接口
│   │   ├── index.ts         # axios封装
│   │   ├── auth.ts          # 认证API
│   │   ├── room.ts          # 机房API
│   │   ├── device.ts        # 设备API
│   │   ├── inspection.ts    # 巡检API
│   │   ├── schedule.ts      # 值班API
│   │   └── types.ts         # 类型定义
│   ├── assets/              # 静态资源
│   ├── components/          # 公共组件
│   │   ├── Layout/          # 布局组件
│   │   │   ├── Sidebar.vue  # 侧边栏
│   │   │   ├── Header.vue   # 顶部栏
│   │   │   └── Main.vue     # 主体
│   │   └── Common/         # 通用组件
│   ├── layout/              # 页面布局
│   │   └── index.vue        # 主布局
│   ├── router/              # 路由配置
│   │   └── index.ts
│   ├── stores/              # Pinia状态管理
│   │   ├── auth.ts         # 认证状态
│   │   └── app.ts          # 应用状态
│   ├── styles/              # 样式文件
│   │   └── index.scss      # 全局样式
│   ├── utils/               # 工具函数
│   └── views/               # 页面视图
│       ├── login/           # 登录页
│       ├── dashboard/       # 首页大屏
│       ├── room/            # 机房管理
│       ├── device/          # 设备管理
│       ├── inspection/      # 巡检管理
│       ├── schedule/        # 值班管理
│       │   ├── staff.vue    # 值班人员
│       │   ├── roster.vue   # 排班表
│       │   └── handover.vue # 交接记录
│       └── system/          # 系统管理
│           ├── user.vue     # 用户管理
│           ├── role.vue     # 角色管理
│           ├── permission.vue # 权限管理
│           ├── dictionary.vue # 数据字典
│           └── sso.vue      # SSO配置
├── index.html               # HTML入口
├── package.json             # 依赖配置
└── vite.config.ts           # Vite配置
```

## 四、数据库设计

### 核心表结构
1. **用户和权限模块**
   - users - 用户表
   - departments - 部门表
   - roles - 角色表
   - permissions - 权限表
   - user_roles - 用户角色关联表
   - role_permissions - 角色权限关联表

2. **机房和设备模块**
   - rooms - 机房表
   - devices - 设备表

3. **巡检模块**
   - inspections - 巡检记录表
   - alerts - 告警记录表

4. **值班管理模块**
   - staffs - 值班人员表
   - shift_schedules - 值班排班表
   - shift_handovers - 值班交接记录表

5. **系统配置模块**
   - sso_config - SSO配置表
   - dictionaries - 数据字典表

### 值班制度说明
- **白班**：08:00-17:00
- **夜班**：18:00-次日07:00
- **值班巡检时间**：08:00-22:00
- **夜班休息**：22:00后夜班值班人员可睡觉休息，次日早上离开
- **7×24小时值班**：确保全天候覆盖

## 五、核心功能模块

### 1. 用户认证与授权
- ✅ 本地用户名密码登录
- ✅ OAuth2.0单点登录（支持多个SSO提供商）
- ✅ JWT Token认证
- ✅ Token刷新机制
- ✅ RBAC权限控制
- ✅ 用户数据同步（支持从SSO系统同步用户信息）

### 2. 机房管理
- ✅ 机房CRUD操作
- ✅ 机房层级管理
- ✅ 机房容量管理
- ✅ 温湿度阈值配置

### 3. 设备管理
- ✅ 设备CRUD操作
- ✅ 设备分类管理
- ✅ 机柜U位管理
- ✅ 设备状态监控

### 4. 巡检管理
- ✅ 巡检记录管理
- ✅ 白班/夜班巡检
- ✅ 巡检结果记录
- ✅ 问题上报与跟踪
- ✅ 巡检照片上传

### 5. 值班管理
- ✅ 值班人员管理
- ✅ 值班排班表（两班制：白班/夜班）
- ✅ 自动排班功能
- ✅ 值班交接记录
- ✅ 任务与问题管理

### 6. 系统管理
- ✅ 用户管理
- ✅ 角色管理
- ✅ 权限管理
- ✅ 数据字典
- ✅ SSO配置管理

### 7. 监控协议支持（新增）
- ✅ SNMP协议支持
- ✅ Modbus协议支持
- ✅ BMS接口对接
- ✅ 传感器网络协议
- ✅ 消防主机通信协议

### 8. 门禁系统对接（新增）
- ✅ 海康威视门禁API对接
- ✅ 大华门禁API对接
- ✅ 门禁记录同步
- ✅ 门禁状态监控

### 9. 多渠道告警（新增）
- ✅ 钉钉机器人告警
- ✅ 短信告警（阿里云）
- ✅ 邮件告警
- ✅ 告警级别分类
- ✅ 告警处理流程

### 10. 高可用架构（新增）
- ✅ Redis缓存
- ✅ 会话共享
- ✅ 负载均衡支持
- ✅ 多实例部署方案

## 六、部署指南

### 环境要求
- **JDK 8+**
- **Maven 3.6+**
- **Node.js 16+**
- **MySQL 5.7+**
- **Redis 6.0+**

### 后端部署
```bash
# 1. 进入后端目录
cd java-backend

# 2. 修改配置文件
# 编辑 src/main/resources/application.yml
# 配置数据库、Redis、SSO等参数

# 3. 初始化数据库
mysql -u root -p < src/main/resources/sql/init.sql

# 4. 编译打包
mvn clean package -DskipTests

# 5. 运行应用
java -jar target/room-inspection-backend-1.0.0.jar

# 或使用Maven运行
mvn spring-boot:run
```

### 前端部署
```bash
# 1. 进入前端目录
cd vue-frontend

# 2. 安装依赖
npm install
# 或使用 pnpm
pnpm install

# 3. 修改API地址（如需要）
# 编辑 vite.config.ts 中的 proxy 配置

# 4. 开发环境运行
npm run dev
# 或
pnpm dev

# 5. 生产环境构建
npm run build
# 或
pnpm build

# 6. 部署到Nginx
# 将 dist 目录内容复制到Nginx配置的root目录
```

### Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/vue-frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 高可用部署（负载均衡）
```bash
# 1. 启动多个后端实例
java -jar backend.jar --server.port=8080 &
java -jar backend.jar --server.port=8081 &
java -jar backend.jar --server.port=8082 &

# 2. 配置Nginx负载均衡
upstream backend {
    server localhost:8080;
    server localhost:8081;
    server localhost:8082;
}

location /api/ {
    proxy_pass http://backend/api/;
}
```

## 七、SSO配置说明

### 配置多个SSO提供商
编辑 `application.yml`：
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          sso1:  # 第一个SSO
            client-id: your-client-id-1
            client-secret: your-client-secret-1
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/api/auth/callback/sso1
          sso2:  # 第二个SSO
            client-id: your-client-id-2
            client-secret: your-client-secret-2
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/api/auth/callback/sso2
```

### 动态配置SSO
在系统管理->SSO配置页面，可以动态添加、修改、删除SSO配置，无需重启服务。

## 八、监控协议配置

### SNMP配置
```yaml
monitor:
  snmp:
    enabled: true
    timeout: 5000
    retries: 3
```

### Modbus配置
```yaml
monitor:
  modbus:
    enabled: true
    timeout: 5000
    retries: 3
```

### BMS接口配置
```yaml
monitor:
  bms:
    enabled: true
    api-url: http://bms.example.com/api
    api-key: your-bms-api-key
```

## 九、告警配置

### 钉钉告警
```yaml
alert:
  dingtalk:
    enabled: true
    webhook: https://oapi.dingtalk.com/robot/send
    secret: your-dingtalk-secret
```

### 邮件告警
```yaml
alert:
  email:
    enabled: true
    host: smtp.example.com
    port: 465
    username: noreply@example.com
    password: your-email-password
```

### 短信告警
```yaml
alert:
  sms:
    enabled: true
    provider: aliyun
    access-key: your-access-key
    secret-key: your-secret-key
    sign-name: 机房巡检
    template-code: your-template-code
```

## 十、注意事项

1. **数据库字符集**：确保MySQL使用utf8mb4字符集
2. **Redis连接**：确保Redis服务正常运行
3. **防火墙设置**：开放8080（后端）和5000（前端）端口
4. **日志文件**：日志文件位于 `logs/room-inspection.log`
5. **文件上传**：上传文件默认保存在 `/tmp/uploads`，可配置修改
6. **Session共享**：生产环境必须配置Redis以支持多实例部署
7. **HTTPS**：生产环境建议使用HTTPS
8. **备份策略**：定期备份数据库

## 十一、技术支持

如有问题，请查看：
- 后端日志：`logs/room-inspection.log`
- 前端控制台：浏览器开发者工具
- 数据库日志：MySQL错误日志
- Redis日志：Redis服务日志

## 十二、后续优化建议

1. **性能优化**
   - 数据库索引优化
   - Redis缓存策略优化
   - 前端代码分割和懒加载

2. **安全加固**
   - SQL注入防护
   - XSS防护
   - CSRF防护
   - 接口限流

3. **监控告警**
   - 接入Prometheus + Grafana
   - 日志采集和分析（ELK）

4. **CI/CD**
   - Jenkins/GitLab CI
   - 自动化测试
   - 自动化部署

5. **容器化**
   - Docker镜像构建
   - Kubernetes部署
   - 微服务拆分
