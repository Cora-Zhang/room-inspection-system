# 机房巡检系统 - MacBook Air M1本地部署指南

## 📋 前提条件

- ✅ MacBook Air M1（Apple Silicon）
- ✅ macOS 12.0+ (Monterey或更新版本)
- ✅ 至少8GB内存（推荐16GB+）
- ✅ 至少50GB可用空间
- ✅ 管理员权限（用于安装软件）

---

## 🚀 快速开始（推荐方式）

### 方案一：Docker一键部署（最简单）

**优势**：
- ✅ 无需手动安装Java、MySQL、Redis
- ✅ 隔离环境，不污染系统
- ✅ 一键启动/停止
- ✅ 跨平台一致性

### 方案二：手动部署（适合开发调试）

**优势**：
- ✅ 更好的开发体验（热更新、断点调试）
- ✅ 更轻量的资源占用
- ✅ 更灵活的配置

---

## 🐳 方案一：Docker一键部署

### 步骤1：安装Docker Desktop for Mac

#### 1.1 下载Docker Desktop

访问 [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)  
下载 **Docker Desktop for Mac with Apple Silicon** 版本

#### 1.2 安装

```bash
# 双击下载的 .dmg 文件
# 将 Docker 拖拽到 Applications 文件夹
```

#### 1.3 启动Docker

```bash
# 在 Launchpad 中找到 Docker
# 启动后，顶部菜单栏会出现鲸鱼图标
```

#### 1.4 验证安装

```bash
docker --version
docker-compose --version
```

应该显示：
```
Docker version 20.10.x, build xxxxx
Docker Compose version v2.x.x
```

---

### 步骤2：克隆项目

```bash
# 使用HTTPS克隆
git clone https://github.com/your-username/room-inspection-system.git
cd room-inspection-system

# 或使用SSH克隆（如果已配置SSH密钥）
git clone git@github.com:your-username/room-inspection-system.git
cd room-inspection-system
```

---

### 步骤3：配置环境变量

```bash
# 复制环境变量模板
cp .env.production.example .env.production

# 编辑环境变量
nano .env.production
# 或使用 VS Code
code .env.production
```

**必须修改的配置**：
```bash
# JWT密钥（生产环境必须修改）
JWT_SECRET=your-256-bit-secret-key-for-jwt-token-generation-please-change-this-in-production

# 数据加密密钥
ENCRYPTION_SECRET_KEY=your-256-bit-encryption-key-change-me-in-production

# 数据库密码（生产环境）
MYSQL_PASSWORD=YourSecurePassword123!
MYSQL_ROOT_PASSWORD=YourRootPassword123!

# Redis密码
REDIS_PASSWORD=YourRedisPassword123!
```

**可选配置**：
```bash
# SSO配置（如果需要）
SSO_ENABLED=false

# 告警配置
DINGTALK_WEBHOOK=
DINGTALK_SECRET=

# 邮件配置
EMAIL_HOST=smtp.example.com
EMAIL_PORT=465
EMAIL_USERNAME=your-email@example.com
EMAIL_PASSWORD=your-email-password
EMAIL_FROM=noreply@example.com

# 短信配置（阿里云）
SMS_ACCESS_KEY=
SMS_SECRET_KEY=
SMS_SIGN_NAME=机房巡检
SMS_TEMPLATE_CODE=
```

---

### 步骤4：启动服务

```bash
# 启动所有服务
docker-compose -f docker-compose.full.yml up -d

# 查看服务状态
docker-compose -f docker-compose.full.yml ps
```

**启动的服务**：
- ✅ MySQL 5.7（端口3306）
- ✅ Redis 6（端口6379）
- ✅ Java后端（端口8080）
- ✅ Next.js前端（端口5000）
- ✅ Nginx（端口80/443）
- ✅ Prometheus（端口9090）
- ✅ Grafana（端口3000）

---

### 步骤5：等待服务就绪

```bash
# 查看日志
docker-compose -f docker-compose.full.yml logs -f

# 或等待一段时间后检查健康状态
docker-compose -f docker-compose.full.yml ps
```

所有服务状态应该显示 `Up` 或 `healthy`。

---

### 步骤6：验证部署

#### 6.1 检查前端

打开浏览器访问：`http://localhost:5000`

应该看到机房巡检系统登录页面。

#### 6.2 检查后端

```bash
curl http://localhost:8080/api/health
```

应该返回健康状态JSON。

#### 6.3 检查Grafana

打开浏览器访问：`http://localhost:3000`

- 用户名：`admin`
- 密码：`admin123456`

---

### 步骤7：登录系统

- **访问地址**: http://localhost:5000
- **默认账号**: `admin`
- **默认密码**: `Admin@123`

⚠️ **首次登录后请立即修改密码！**

---

## 💻 方案二：手动部署（开发调试）

### 步骤1：安装基础软件

#### 1.1 安装Homebrew

Homebrew是macOS的包管理器，推荐使用。

```bash
# 安装Homebrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装完成后，按照提示添加到PATH
# 通常需要执行：
(echo; echo 'eval "$(/opt/homebrew/bin/brew shellenv)"') >> /Users/$USER/.zprofile
eval "$(/opt/homebrew/bin/brew shellenv)"
```

#### 1.2 安装Java 8（Zulu JDK ARM64）

**⚠️ 重要**：M1芯片需要使用ARM64版本的Java。

```bash
# 使用Homebrew安装Zulu JDK 8（ARM64版本）
brew install --cask zulu8

# 验证安装
java -version
```

应该显示：
```
openjdk version "1.8.0_xxx"
OpenJDK Runtime Environment (Zulu 8.xx+xx) [macOS ARM64]
OpenJDK 64-Bit Server VM (Zulu 8.xx+xx) [macOS ARM64]
```

**其他选项**：
- Oracle JDK 8（需要Rosetta转译，性能较差）
- Amazon Corretto 8（ARM64版本）

#### 1.3 安装Maven

```bash
# 使用Homebrew安装Maven
brew install maven

# 验证安装
mvn -version
```

#### 1.4 安装Node.js

```bash
# 使用Homebrew安装Node.js 18（LTS）
brew install node@18

# 验证安装
node --version
npm --version
```

#### 1.5 安装pnpm

```bash
# 使用npm安装pnpm
npm install -g pnpm@8

# 验证安装
pnpm --version
```

#### 1.6 安装MySQL 5.7

```bash
# 使用Homebrew安装MySQL 5.7
brew install mysql@5.7

# 启动MySQL
brew services start mysql@5.7

# 安全配置
mysql_secure_installation
```

#### 1.7 安装Redis

```bash
# 使用Homebrew安装Redis
brew install redis

# 启动Redis
brew services start redis
```

#### 1.8 安装Git（如果未安装）

```bash
# macOS通常自带Git，检查版本
git --version

# 如需更新，使用Homebrew
brew install git
```

---

### 步骤2：克隆项目

```bash
# 克隆项目
git clone https://github.com/your-username/room-inspection-system.git
cd room-inspection-system
```

---

### 步骤3：配置数据库

#### 3.1 创建数据库和用户

```bash
# 连接到MySQL
mysql -u root -p

# 执行以下SQL
CREATE DATABASE IF NOT EXISTS room_inspection 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER 'inspection_user'@'localhost' IDENTIFIED BY 'YourSecurePassword123!';
GRANT ALL PRIVILEGES ON room_inspection.* TO 'inspection_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 3.2 导入数据库脚本

```bash
# 导入初始化脚本
mysql -u inspection_user -p room_inspection < java-backend/src/main/resources/sql/init.sql
```

---

### 步骤4：配置后端

#### 4.1 修改配置文件

```bash
# 编辑后端配置
code java-backend/src/main/resources/application.yml
# 或
nano java-backend/src/main/resources/application.yml
```

**修改关键配置**：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/room_inspection?useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true
    username: inspection_user
    password: YourSecurePassword123!
  
  redis:
    host: localhost
    port: 6379
    password:  # 如果设置了Redis密码，填在这里

jwt:
  secret: your-256-bit-secret-key-for-jwt-token-generation-please-change-this-in-production
```

#### 4.2 构建后端

```bash
# 进入后端目录
cd java-backend

# 清理并编译（跳过测试）
mvn clean package -DskipTests

# 编译成功后，JAR文件位置：target/room-inspection-backend-1.0.0.jar
```

#### 4.3 启动后端

```bash
# 方式一：直接运行JAR
java -jar target/room-inspection-backend-1.0.0.jar

# 方式二：指定配置文件
java -jar target/room-inspection-backend-1.0.0.jar --spring.profiles.active=prod

# 方式三：后台运行（生产环境推荐）
nohup java -jar target/room-inspection-backend-1.0.0.jar > logs/backend.log 2>&1 &
```

**后端启动成功标志**：
- 控制台显示 `Started RoomInspectionBackendApplication in xx.xxx seconds`
- 访问 `http://localhost:8080/api/health` 返回健康状态

---

### 步骤5：配置前端

#### 5.1 创建环境变量

```bash
# 创建前端环境变量
cd ..
echo "NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api" > .env.local
echo "NEXT_PUBLIC_APP_TITLE=机房巡检系统" >> .env.local
echo "NEXT_PUBLIC_APP_VERSION=1.0.0" >> .env.local
echo "NEXT_PUBLIC_SSO_ENABLED=false" >> .env.local
echo "NEXT_PUBLIC_SSO_TYPE=local" >> .env.local
```

#### 5.2 安装依赖

```bash
# 使用pnpm安装依赖
pnpm install
```

⚠️ **注意**：
- 如果遇到 `sharp` 等原生模块编译问题，可能需要：
  ```bash
  # 设置架构为arm64
  npm config set target_arch arm64
  
  # 重新安装
  rm -rf node_modules
  pnpm install
  ```

#### 5.3 启动前端

```bash
# 开发模式（热更新）
pnpm dev

# 或使用自定义端口
pnpm dev --port 5000
```

**前端启动成功标志**：
- 控制台显示 `Ready in xxx ms`
- 访问 `http://localhost:5000` 显示登录页面

---

## 🧪 验证部署

### 1. 检查所有服务

```bash
# 后端健康检查
curl http://localhost:8080/api/health

# 前端页面
open http://localhost:5000

# Grafana监控
open http://localhost:3000
```

### 2. 登录测试

- **访问**: http://localhost:5000
- **账号**: `admin`
- **密码**: `Admin@123`

### 3. 功能测试

- ✅ 用户登录
- ✅ 查看仪表盘
- ✅ 创建设备
- ✅ 创建巡检计划
- ✅ 查看告警记录

---

## 🛠️ 常见问题解决

### 问题1：Java版本不兼容

**症状**：
```
Unsupported class file major version 52
```

**解决方案**：
```bash
# 确认Java版本
java -version

# 必须是Java 8（1.8.0_xxx）
# 如果不是，安装Zulu JDK 8 ARM64版本
brew install --cask zulu8

# 设置环境变量
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home
```

---

### 问题2：Node.js原生模块编译失败

**症状**：
```
Error: The module was compiled against a different Node.js version
```

**解决方案**：
```bash
# 清理并重新安装
rm -rf node_modules .next
pnpm install
pnpm dev
```

---

### 问题3：MySQL连接失败

**症状**：
```
Communications link failure
```

**解决方案**：
```bash
# 检查MySQL是否运行
brew services list | grep mysql

# 启动MySQL
brew services start mysql@5.7

# 检查MySQL状态
mysql -u root -p -e "SELECT 1"
```

---

### 问题4：Redis连接失败

**症状**：
```
Connection refused: /127.0.0.1:6379
```

**解决方案**：
```bash
# 检查Redis是否运行
brew services list | grep redis

# 启动Redis
brew services start redis

# 测试连接
redis-cli ping
# 应返回: PONG
```

---

### 问题5：端口被占用

**症状**：
```
Address already in use: 5000
```

**解决方案**：
```bash
# 查看占用端口的进程
lsof -i :5000

# 结束进程
kill -9 <PID>

# 或修改端口
pnpm dev --port 3000
```

---

### 问题6：Docker Desktop占用过多资源

**症状**：
- 系统卡顿
- 内存占用过高

**解决方案**：
```bash
# 打开Docker Desktop
# Settings → Resources
# 调整以下配置：
# - CPUs: 4
# - Memory: 8GB
# - Disk: 100GB

# 或停止不必要的服务
docker-compose -f docker-compose.full.yml stop prometheus grafana
```

---

## 📝 开发技巧

### 1. 使用VS Code调试

#### 前端调试

安装VS Code扩展：
- `ESLint`
- `Prettier`
- `Tailwind CSS IntelliSense`

配置 `.vscode/launch.json`：
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "node",
      "request": "launch",
      "name": "Next.js: debug server-side",
      "runtimeExecutable": "pnpm",
      "runtimeArgs": ["dev"],
      "console": "integratedTerminal",
      "restart": true,
      "cwd": "${workspaceFolder}"
    }
  ]
}
```

#### 后端调试

配置 `.vscode/launch.json`：
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Spring Boot App",
      "request": "launch",
      "mainClass": "com.roominspection.backend.RoomInspectionBackendApplication",
      "projectName": "room-inspection-backend",
      "args": "--spring.profiles.active=dev"
    }
  ]
}
```

---

### 2. 日志查看

#### 前端日志

```bash
# 终端查看
pnpm dev

# 浏览器控制台
# 按 F12 打开开发者工具 → Console
```

#### 后端日志

```bash
# 如果使用nohup运行
tail -f java-backend/logs/backend.log

# Docker日志
docker-compose -f docker-compose.full.yml logs -f backend
```

---

### 3. 数据库管理

#### 使用MySQL Workbench

```bash
# 下载安装MySQL Workbench for macOS
brew install --cask mysqlworkbench
```

#### 使用命令行

```bash
# 连接数据库
mysql -u inspection_user -p room_inspection

# 查看表
SHOW TABLES;

# 查询数据
SELECT * FROM sys_user LIMIT 10;
```

---

## 🔄 更新项目

### 从GitHub拉取最新代码

```bash
# 拉取最新代码
git pull origin main

# 更新依赖
cd java-backend && mvn clean package -DskipTests && cd ..
pnpm install

# 重启服务
# Docker方式
docker-compose -f docker-compose.full.yml restart

# 手动方式
# 后端
cd java-backend
mvn spring-boot:run

# 前端（新终端）
cd ..
pnpm dev
```

---

## 🎉 完成！

恭喜你已成功在MacBook Air M1上部署机房巡检系统！🎊

---

## 📚 相关文档

- [GitHub上传指南](./GITHUB_UPLOAD_GUIDE.md)
- [完整部署指南](./FINAL_DEPLOYMENT_GUIDE.md)
- [技术方案](./TECHNICAL_SOLUTION.md)
- [项目总结](./PROJECT_SUMMARY.md)

---

## 🆘 获取帮助

如遇到问题，请：
1. 查阅本文档的常见问题部分
2. 查看日志文件定位问题
3. 参考相关文档

---

**文档版本**: v1.0.0  
**最后更新**: 2024-01  
**适用平台**: macOS Apple Silicon (M1/M2/M3)
