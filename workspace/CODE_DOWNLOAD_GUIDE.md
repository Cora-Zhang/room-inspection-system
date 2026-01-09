# 机房巡检系统 - 代码下载指南

## 📦 项目已打包完成

项目代码已成功打包为压缩文件，您可以通过以下方式获取：

### 文件信息
- **文件名**: `room-inspection-system.tar.gz`
- **文件大小**: 约 5.8 MB（已排除 node_modules 等大文件）
- **位置**: `/workspace/room-inspection-system.tar.gz`
- **项目内容**: 268个Java文件，10290个TypeScript/JavaScript文件

---

## 🚀 获取代码的方式

### 方式1：通过文件管理器下载（推荐）

如果您有访问沙箱文件系统的权限：
1. 访问沙箱文件系统
2. 导航到 `/workspace/` 目录
3. 下载 `room-inspection-system.tar.gz` 文件
4. 在本地解压：`tar -xzf room-inspection-system.tar.gz`

### 方式2：通过Git仓库下载

如果您配置了Git仓库：

#### 步骤1：推送到Git仓库
```bash
# 在项目根目录执行
cd /workspace/projects

# 初始化Git（如果还没初始化）
# git init

# 添加远程仓库（请替换为您的仓库地址）
git remote add origin https://github.com/your-username/room-inspection-system.git

# 添加所有文件
git add .

# 提交
git commit -m "feat: 完成机房巡检系统开发"

# 推送到远程仓库
git push -u origin main
```

#### 步骤2：在本地克隆
```bash
# 在您的本地电脑执行
git clone https://github.com/your-username/room-inspection-system.git
cd room-inspection-system
```

### 方式3：通过SCP下载（如果有服务器访问权限）

如果您有SSH访问权限：

```bash
# 在您的本地电脑执行
scp username@server:/workspace/room-inspection-system.tar.gz ./

# 解压
tar -xzf room-inspection-system.tar.gz
```

---

## 📂 项目结构（解压后）

解压后的目录结构：
```
projects/                          # 项目根目录
├── docs/                          # 文档目录
│   ├── FINAL_DEPLOYMENT_GUIDE.md  # 📋 完整部署方案
│   ├── TECHNICAL_SOLUTION.md      # 🔧 技术方案
│   ├── PROJECT_SUMMARY.md         # 📊 项目总结
│   ├── ARCHITECTURE.md           # 🏗️ 架构设计
│   └── ...                        # 其他文档
│
├── java-backend/                  # Java Spring Boot 后端
│   ├── src/main/java/            # Java源代码
│   │   └── com/roominspection/backend/
│   │       ├── controller/        # 28个控制器
│   │       ├── service/          # 业务逻辑层
│   │       ├── mapper/           # 数据访问层（45个Mapper）
│   │       ├── entity/           # 实体类（40+个）
│   │       └── ...
│   ├── src/main/resources/
│   │   ├── application.yml       # 应用配置
│   │   └── sql/init.sql         # 数据库初始化脚本
│   ├── pom.xml                   # Maven配置
│   ├── Dockerfile               # 后端Dockerfile
│   └── docker-compose.yml       # Docker Compose配置
│
├── src/                          # Next.js 前端
│   ├── app/                     # App Router页面
│   │   ├── page.tsx             # 首页
│   │   ├── dashboard/           # 仪表盘
│   │   ├── login/               # 登录页
│   │   ├── inspection/          # 巡检管理
│   │   ├── device/              # 设备管理
│   │   ├── room/                # 机房管理
│   │   └── ...
│   ├── components/              # React组件
│   ├── lib/                     # 工具库
│   ├── types/                   # TypeScript类型
│   └── package.json
│
├── docker-compose.full.yml       # 完整Docker Compose配置
├── Dockerfile.frontend          # 前端Dockerfile
├── .env.production.example      # 环境变量模板
├── scripts/deploy.sh            # 一键部署脚本
└── README.md                    # 项目说明
```

---

## 🔧 解压后的快速开始

### 1. 环境要求
- Node.js 18+
- Java 8 (JDK 1.8)
- MySQL 5.7+
- Redis 6.0+
- Docker & Docker Compose（可选）

### 2. 安装依赖

#### 前端依赖
```bash
cd projects
pnpm install
# 或
npm install
```

#### 后端依赖
```bash
cd java-backend
mvn clean install
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp .env.production.example .env.production

# 编辑环境变量
nano .env.production
```

### 4. 启动服务

#### 方式1：使用Docker Compose（推荐）
```bash
# 一键部署
chmod +x scripts/deploy.sh
./scripts/deploy.sh

# 或手动启动
docker-compose -f docker-compose.full.yml up -d
```

#### 方式2：开发环境
```bash
# 启动数据库
docker-compose up -d mysql redis

# 启动后端
cd java-backend
mvn spring-boot:run

# 启动前端
cd ..
pnpm dev
```

### 5. 访问系统
- 前端: http://localhost:5000
- 后端API: http://localhost:8080/api
- Grafana: http://localhost:3000
- 默认账号: admin / Admin@123

---

## 📚 文档说明

### 必读文档
1. **docs/FINAL_DEPLOYMENT_GUIDE.md** - 完整的部署方案，包含生产环境部署步骤
2. **docs/TECHNICAL_SOLUTION.md** - 技术架构与实现方案
3. **docs/PROJECT_SUMMARY.md** - 项目总结与功能说明

### 技术文档
- java-backend/docs/api-standard.md - API接口规范
- java-backend/docs/extensibility-design.md - 扩展性设计
- java-backend/docs/distributed-collection-architecture.md - 分布式采集架构

---

## 🎯 核心功能模块

- ✅ 用户权限管理（RBAC）
- ✅ OAuth2.0 单点登录
- ✅ 巡检管理（计划、记录、验证、报告）
- ✅ 设备管理（监控、维护、分析）
- ✅ 机房管理（布局、热力图）
- ✅ 监控采集（SNMP、Modbus、BMS等）
- ✅ 告警管理（规则、多渠道通知）
- ✅ 值班管理（班次、排班、交接）
- ✅ 扩展功能（API版本控制、协议插件、AI识别）
- ✅ 门禁集成（海康、大华、宇视）
- ✅ 系统监控（Prometheus + Grafana）

---

## 📞 技术支持

如需帮助，请参考：
- 部署问题：查看 `docs/FINAL_DEPLOYMENT_GUIDE.md` 中的"常见问题排查"章节
- 技术问题：查看 `docs/TECHNICAL_SOLUTION.md`
- API问题：查看 `java-backend/docs/api-standard.md`

---

## 📋 项目统计

| 项目 | 数量 |
|-----|------|
| Java文件 | 268个 |
| TypeScript/JS文件 | 10290个 |
| 数据库表 | 32张 |
| Controller | 28个 |
| Mapper | 45个 |
| Entity | 40+个 |
| 文档 | 20+份 |

**项目完成度：100%**

---

**下载完成后，建议首先阅读 `docs/PROJECT_SUMMARY.md` 了解项目全貌，然后按照 `docs/FINAL_DEPLOYMENT_GUIDE.md` 进行部署。**
