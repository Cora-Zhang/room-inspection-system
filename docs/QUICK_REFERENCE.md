# 机房巡检系统 - 快速参考卡片

## 🚀 GitHub上传速查表

### 初始化并推送

```bash
# 1. 配置Git（首次）
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 2. 初始化仓库
git init

# 3. 添加所有文件
git add .

# 4. 提交
git commit -m "feat: 初始化机房巡检系统"

# 5. 关联远程仓库
git remote add origin https://github.com/your-username/room-inspection-system.git

# 6. 推送
git push -u origin main
```

### 日常更新

```bash
# 查看状态
git status

# 添加修改
git add .

# 提交修改
git commit -m "fix: 修复某问题"

# 推送到GitHub
git push
```

---

## 💻 MacBook M1部署速查表

### Docker一键部署（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/your-username/room-inspection-system.git
cd room-inspection-system

# 2. 配置环境变量
cp .env.production.example .env.production
nano .env.production  # 修改密钥和密码

# 3. 启动服务
docker-compose -f docker-compose.full.yml up -d

# 4. 查看状态
docker-compose -f docker-compose.full.yml ps

# 5. 访问系统
open http://localhost:5000
```

### 手动部署（开发调试）

```bash
# 1. 安装基础软件（Homebrew）
brew install --cask zulu8 maven mysql@5.7 redis node@18
npm install -g pnpm@8

# 2. 配置数据库
mysql -u root -p
CREATE DATABASE room_inspection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'inspection_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON room_inspection.* TO 'inspection_user'@'localhost';
FLUSH PRIVILEGES;

# 3. 导入数据库
mysql -u inspection_user -p room_inspection < java-backend/src/main/resources/sql/init.sql

# 4. 启动后端
cd java-backend
mvn clean package -DskipTests
java -jar target/room-inspection-backend-1.0.0.jar

# 5. 启动前端（新终端）
cd ..
echo "NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api" > .env.local
pnpm install
pnpm dev
```

---

## 🔧 服务端口说明

| 服务 | 端口 | 说明 |
|-----|------|------|
| 前端 | 5000 | Next.js Web界面 |
| 后端 | 8080 | Spring Boot API |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nginx | 80/443 | 反向代理 |
| Grafana | 3000 | 监控面板 |
| Prometheus | 9090 | 数据采集 |

---

## 🔑 默认账号密码

| 系统 | 用户名 | 密码 |
|-----|--------|------|
| 系统登录 | admin | Admin@123 |
| MySQL | root | root123456 |
| MySQL | inspection_user | inspection123456 |
| Redis | - | redis123456 |
| Grafana | admin | admin123456 |

⚠️ **生产环境必须修改所有默认密码！**

---

## 🛠️ 常用命令

### Docker相关

```bash
# 启动所有服务
docker-compose -f docker-compose.full.yml up -d

# 停止所有服务
docker-compose -f docker-compose.full.yml down

# 重启服务
docker-compose -f docker-compose.full.yml restart

# 查看日志
docker-compose -f docker-compose.full.yml logs -f

# 查看特定服务日志
docker-compose -f docker-compose.full.yml logs -f backend
```

### MySQL相关

```bash
# 启动MySQL
brew services start mysql@5.7

# 停止MySQL
brew services stop mysql@5.7

# 连接MySQL
mysql -u root -p

# 导入数据
mysql -u inspection_user -p room_inspection < init.sql
```

### Redis相关

```bash
# 启动Redis
brew services start redis

# 停止Redis
brew services stop redis

# 测试连接
redis-cli ping
```

### 前端相关

```bash
# 安装依赖
pnpm install

# 开发模式
pnpm dev

# 构建
pnpm build

# 启动生产服务
pnpm start
```

### 后端相关

```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/room-inspection-backend-1.0.0.jar

# 后台运行
nohup java -jar target/room-inspection-backend-1.0.0.jar > backend.log 2>&1 &
```

---

## 🐛 快速排错

### 端口被占用

```bash
# 查看占用端口的进程
lsof -i :5000

# 结束进程
kill -9 <PID>
```

### Java版本不对

```bash
# 查看Java版本
java -version

# 安装Zulu JDK 8 ARM64
brew install --cask zulu8
```

### 数据库连接失败

```bash
# 检查MySQL状态
brew services list | grep mysql

# 启动MySQL
brew services start mysql@5.7

# 测试连接
mysql -u inspection_user -p
```

### Docker启动失败

```bash
# 查看错误日志
docker-compose -f docker-compose.full.yml logs

# 重建容器
docker-compose -f docker-compose.full.yml down
docker-compose -f docker-compose.full.yml build --no-cache
docker-compose -f docker-compose.full.yml up -d
```

---

## 📖 完整文档

- [GitHub上传指南](./GITHUB_UPLOAD_GUIDE.md)
- [MacBook M1部署指南](./MACBOOK_M1_DEPLOYMENT.md)
- [完整部署指南](./FINAL_DEPLOYMENT_GUIDE.md)
- [技术方案](./TECHNICAL_SOLUTION.md)
- [项目总结](./PROJECT_SUMMARY.md)

---

## 🆘 获取帮助

遇到问题时：

1. **查看日志**：`docker-compose logs` 或查看 `/logs/` 目录
2. **检查配置**：确认 `.env` 文件配置正确
3. **查阅文档**：参考上述完整文档
4. **重启服务**：尝试重启相关服务

---

**文档版本**: v1.0.0  
**最后更新**: 2024-01
