# 机房巡检系统 - 完整部署方案

## 📋 文档说明

本文档提供机房巡检系统的完整部署方案，包括开发环境、生产环境的详细部署步骤，以及数据库初始化、前后端配置、系统监控等内容。

**版本**: v1.0.0  
**最后更新**: 2024-01-20  
**技术栈**: Java8 + Spring Boot + Vue3 + Next.js + MySQL5.7 + Redis

---

## 🏗️ 系统架构概览

### 1. 技术栈组成

| 组件 | 技术选型 | 版本 | 说明 |
|-----|---------|------|------|
| **前端** | Next.js | 16.0.10 | React 19 + App Router |
| **前端样式** | Tailwind CSS | 4.0 | 科幻风格UI |
| **后端** | Spring Boot | 2.7.18 | Java 8 |
| **数据库** | MySQL | 5.7 | 主数据库 |
| **缓存** | Redis | 6.0+ | 缓存与会话存储 |
| **反向代理** | Nginx | 1.20+ | 负载均衡 |
| **容器化** | Docker | 20.10+ | 应用容器化 |
| **编排** | Kubernetes | 1.24+ | 生产环境编排 |
| **监控** | Prometheus + Grafana | - | 系统监控 |

### 2. 端口分配

| 服务 | 端口 | 说明 |
|-----|------|------|
| Next.js 前端 | 5000 | Web前端服务 |
| Java后端 | 8080 | 后端API服务 |
| MySQL | 3306 | 数据库服务 |
| Redis | 6379 | 缓存服务 |
| Nginx | 80/443 | 反向代理 |

---

## 🚀 一、环境准备

### 1.1 硬件要求

#### 最低配置（开发/测试环境）
- CPU: 4核
- 内存: 8GB
- 硬盘: 100GB SSD

#### 推荐配置（生产环境）
- CPU: 8核+
- 内存: 16GB+
- 硬盘: 500GB+ SSD

### 1.2 软件依赖

#### 基础环境
```bash
# 操作系统
- Linux (Ubuntu 20.04+ / CentOS 7+ / Debian 10+)

# 必需软件
- Java 8 (OpenJDK 1.8.0_xxx)
- Node.js 18.x 或 20.x
- pnpm 8.x
- MySQL 5.7
- Redis 6.0+
- Nginx 1.20+
- Docker 20.10+
- Docker Compose 2.0+
```

#### 安装命令示例

```bash
# Ubuntu/Debian 安装 Java 8
sudo apt update
sudo apt install -y openjdk-8-jdk

# 安装 Node.js 和 pnpm
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
npm install -g pnpm@8

# 安装 MySQL 5.7
sudo apt install -y mysql-server-5.7

# 安装 Redis
sudo apt install -y redis-server

# 安装 Docker 和 Docker Compose
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
sudo apt install -y docker-compose
```

---

## 📦 二、数据库部署与初始化

### 2.1 MySQL 安装配置

#### 安装 MySQL 5.7

```bash
# Ubuntu/Debian
sudo apt install -y mysql-server-5.7 mysql-client-5.7

# 启动 MySQL 服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

#### 创建数据库和用户

```sql
-- 连接到 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE IF NOT EXISTS room_inspection 
DEFAULT CHARACTER SET utf8mb4 
DEFAULT COLLATE utf8mb4_unicode_ci;

-- 创建数据库用户
CREATE USER 'inspection_user'@'%' IDENTIFIED BY 'YourSecurePassword123!';

-- 授权
GRANT ALL PRIVILEGES ON room_inspection.* TO 'inspection_user'@'%';
FLUSH PRIVILEGES;

-- 验证
SHOW DATABASES;
SHOW GRANTS FOR 'inspection_user'@'%';
```

### 2.2 数据库表结构初始化

```bash
# 导入数据库初始化脚本
cd java-backend
mysql -u inspection_user -p room_inspection < src/main/resources/sql/init.sql
```

### 2.3 Redis 安装配置

```bash
# 安装 Redis
sudo apt install -y redis-server

# 配置 Redis
sudo nano /etc/redis/redis.conf

# 修改以下配置项（生产环境必须修改）
# bind 127.0.0.1 -> bind 0.0.0.0 (如果需要远程访问)
# requirepass your-redis-password (设置密码)

# 重启 Redis
sudo systemctl restart redis
sudo systemctl enable redis

# 验证
redis-cli ping
# 应返回: PONG
```

---

## 🔧 三、后端部署（Java Spring Boot）

### 3.1 构建后端应用

```bash
cd java-backend

# 清理并编译
mvn clean package -DskipTests

# 生成的 JAR 文件位置: target/room-inspection-backend-1.0.0.jar
```

### 3.2 配置文件

#### application.yml（生产环境）

```bash
# 创建生产配置文件
cp src/main/resources/application.yml src/main/resources/application-prod.yml

# 修改关键配置
nano src/main/resources/application-prod.yml
```

**关键配置项**:
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/room_inspection?useSSL=true&serverTimezone=GMT%2B8
    username: inspection_user
    password: YourSecurePassword123!
  
  redis:
    host: localhost
    port: 6379
    password: your-redis-password

# JWT 密钥（生产环境必须修改）
jwt:
  secret: your-production-secret-key-at-least-256-bits-long-for-hs256-algorithm
  expiration: 86400000

# 文件上传路径
file:
  upload-path: /var/lib/room-inspection/uploads
```

### 3.3 创建必要的目录

```bash
# 创建上传目录
sudo mkdir -p /var/lib/room-inspection/uploads
sudo chown -R $USER:$USER /var/lib/room-inspection

# 创建日志目录
sudo mkdir -p /var/log/room-inspection
sudo chown -R $USER:$USER /var/log/room-inspection
```

### 3.4 启动后端服务

#### 方式1: 直接运行 JAR

```bash
cd java-backend

# 启动服务
java -jar target/room-inspection-backend-1.0.0.jar \
  --spring.profiles.active=prod \
  > /var/log/room-inspection/application.log 2>&1 &

# 查看日志
tail -f /var/log/room-inspection/application.log
```

#### 方式2: 使用 systemd 服务（推荐生产环境）

```bash
# 创建 systemd 服务文件
sudo nano /etc/systemd/system/room-inspection.service
```

**服务文件内容**:
```ini
[Unit]
Description=Room Inspection System Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=your-username
WorkingDirectory=/opt/room-inspection/backend
ExecStart=/usr/bin/java -jar /opt/room-inspection/backend/room-inspection-backend-1.0.0.jar \
  --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=append:/var/log/room-inspection/application.log
StandardError=append:/var/log/room-inspection/error.log

[Install]
WantedBy=multi-user.target
```

```bash
# 复制 JAR 文件到部署目录
sudo mkdir -p /opt/room-inspection/backend
sudo cp target/room-inspection-backend-1.0.0.jar /opt/room-inspection/backend/
sudo chown -R your-username:your-username /opt/room-inspection

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable room-inspection
sudo systemctl start room-inspection

# 查看状态
sudo systemctl status room-inspection

# 查看日志
sudo journalctl -u room-inspection -f
```

### 3.5 验证后端服务

```bash
# 检查端口监听
sudo lsof -i :8080

# 测试 API
curl -X GET http://localhost:8080/api/health

# 访问 Swagger 文档
http://your-server-ip:8080/api/swagger-ui.html
```

---

## 🎨 四、前端部署（Next.js）

### 4.1 安装依赖

```bash
# 在项目根目录
pnpm install
```

### 4.2 配置环境变量

```bash
# 创建环境变量文件
cat > .env.production << EOF
# API配置
NEXT_PUBLIC_API_BASE_URL=http://your-backend-ip:8080/api

# SSO配置（如果启用）
NEXT_PUBLIC_SSO_ENABLED=false
NEXT_PUBLIC_SSO_TYPE=local

# 应用配置
NEXT_PUBLIC_APP_TITLE=机房巡检系统
NEXT_PUBLIC_APP_VERSION=1.0.0
EOF
```

### 4.3 构建前端应用

```bash
# 构建生产版本
pnpm build

# 生成的文件在 .next 目录
```

### 4.4 部署前端服务

#### 方式1: 使用 Node.js 运行

```bash
# 启动生产服务器
pnpm start

# 或使用 PM2（推荐）
npm install -g pm2

# 创建 PM2 配置文件
cat > ecosystem.config.js << EOF
module.exports = {
  apps: [{
    name: 'room-inspection-frontend',
    script: 'node_modules/next/dist/bin/next',
    args: 'start -p 5000',
    cwd: '/opt/room-inspection/frontend',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '1G',
    env: {
      NODE_ENV: 'production',
      PORT: 5000
    }
  }]
};
EOF

# 复制文件到部署目录
sudo mkdir -p /opt/room-inspection/frontend
sudo cp -r .next node_modules package.json public /opt/room-inspection/frontend/
sudo chown -R your-username:your-username /opt/room-inspection

# 启动 PM2
pm2 start ecosystem.config.js
pm2 save
pm2 startup
```

#### 方式2: 使用 Docker（推荐生产环境）

```bash
# 构建 Docker 镜像
docker build -t room-inspection-frontend:latest .

# 运行容器
docker run -d \
  --name room-inspection-frontend \
  -p 5000:5000 \
  --env-file .env.production \
  --restart unless-stopped \
  room-inspection-frontend:latest

# 查看日志
docker logs -f room-inspection-frontend
```

### 4.5 验证前端服务

```bash
# 检查端口
sudo lsof -i :5000

# 访问前端
http://your-server-ip:5000
```

---

## 🌐 五、Nginx 反向代理配置

### 5.1 安装 Nginx

```bash
sudo apt install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

### 5.2 配置反向代理

```bash
# 创建站点配置
sudo nano /etc/nginx/sites-available/room-inspection
```

**配置文件内容**:
```nginx
# HTTP 重定向到 HTTPS（可选，生产环境推荐）
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS 配置（需要 SSL 证书）
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    # SSL 证书配置（使用 Let's Encrypt 或自有证书）
    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 日志
    access_log /var/log/nginx/room-inspection-access.log;
    error_log /var/log/nginx/room-inspection-error.log;

    # 客户端最大请求体大小
    client_max_body_size 10M;

    # WebSocket 支持
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";

    # 前端代理
    location / {
        proxy_pass http://localhost:5000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # WebSocket 支持
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 文件上传
    location /uploads {
        alias /var/lib/room-inspection/uploads;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # 健康检查
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
```

### 5.3 启用配置

```bash
# 创建符号链接
sudo ln -s /etc/nginx/sites-available/room-inspection /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

---

## 🐳 六、Docker Compose 部署（推荐用于开发/测试环境）

### 6.1 创建 docker-compose.yml

```bash
cd java-backend
nano docker-compose.yml
```

**内容**:
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:5.7
    container_name: room-inspection-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: room_inspection
      MYSQL_USER: inspection_user
      MYSQL_PASSWORD: inspection123456
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./src/main/resources/sql:/docker-entrypoint-initdb.d
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:6-alpine
    container_name: room-inspection-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: room-inspection-backend
    restart: always
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/room_inspection?useSSL=false&serverTimezone=GMT%2B8
      SPRING_DATASOURCE_USERNAME: inspection_user
      SPRING_DATASOURCE_PASSWORD: inspection123456
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
    depends_on:
      - mysql
      - redis
    volumes:
      - /var/lib/room-inspection/uploads:/app/uploads
      - ./logs:/app/logs

  frontend:
    build:
      context: .
      dockerfile: Dockerfile.frontend
    container_name: room-inspection-frontend
    restart: always
    ports:
      - "5000:5000"
    environment:
      NEXT_PUBLIC_API_BASE_URL: http://localhost:8080/api
    depends_on:
      - backend

volumes:
  mysql-data:
  redis-data:
```

### 6.2 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

---

## ☸️ 七、Kubernetes 部署（生产环境）

### 7.1 创建命名空间

```bash
kubectl create namespace room-inspection
```

### 7.2 部署 MySQL

```bash
kubectl apply -f java-backend/k8s/mysql-deployment.yaml
```

### 7.3 部署 Redis

```bash
kubectl apply -f java-backend/k8s/redis-deployment.yaml
```

### 7.4 部署后端服务

```bash
kubectl apply -f java-backend/k8s/backend-deployment.yaml
```

### 7.5 部署前端服务

```bash
kubectl apply -f java-backend/k8s/frontend-deployment.yaml
```

### 7.6 配置 Ingress

```bash
kubectl apply -f java-backend/k8s/ingress.yaml
```

---

## 📊 八、监控与日志

### 8.1 Prometheus + Grafana

#### 安装 Prometheus

```bash
# 使用 Docker 安装 Prometheus
docker run -d \
  --name prometheus \
  -p 9090:9090 \
  -v java-backend/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

#### 安装 Grafana

```bash
docker run -d \
  --name grafana \
  -p 3000:3000 \
  -v grafana-data:/var/lib/grafana \
  grafana/grafana
```

#### 配置数据源和仪表盘

```bash
# 访问 Grafana
http://your-server:3000

# 默认账号: admin/admin

# 添加 Prometheus 数据源
# 导入预置仪表盘
```

### 8.2 日志管理

#### 配置日志轮转

```bash
sudo nano /etc/logrotate.d/room-inspection
```

**内容**:
```
/var/log/room-inspection/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 0644 your-username your-username
    sharedscripts
    postrotate
        systemctl reload room-inspection
    endscript
}
```

---

## 🔒 九、安全配置

### 9.1 防火墙配置

```bash
# UFW 配置（Ubuntu）
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

### 9.2 SSL/TLS 证书

#### 使用 Let's Encrypt

```bash
# 安装 Certbot
sudo apt install -y certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

### 9.3 数据库安全

```bash
# MySQL 安全
- 禁用远程 root 登录
- 修改默认端口
- 定期备份数据
- 使用强密码

# Redis 安全
- 设置密码
- 禁用危险命令
- 限制访问 IP
```

---

## 🔄 十、系统初始化与验证

### 10.1 创建默认管理员账户

```bash
# 调用后端 API 创建管理员
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123",
    "email": "admin@example.com",
    "role": "ADMIN"
  }'
```

### 10.2 系统健康检查

```bash
# 检查后端
curl http://localhost:8080/api/health

# 检查前端
curl http://localhost:5000/health

# 检查数据库
mysql -u inspection_user -p -e "SELECT 1"

# 检查 Redis
redis-cli ping
```

### 10.3 访问系统

```bash
# 前端访问
http://your-domain.com

# 默认管理员登录
用户名: admin
密码: Admin@123

# 后端 API 文档
http://your-domain.com/api/swagger-ui.html
```

---

## 📝 十一、常见问题排查

### 11.1 后端无法启动

```bash
# 检查日志
tail -f /var/log/room-inspection/application.log

# 检查端口占用
sudo lsof -i :8080

# 检查数据库连接
mysql -h localhost -u inspection_user -p room_inspection
```

### 11.2 前端无法访问

```bash
# 检查端口
sudo lsof -i :5000

# 检查 Nginx 配置
sudo nginx -t

# 查看 Nginx 日志
tail -f /var/log/nginx/room-inspection-error.log
```

### 11.3 数据库连接失败

```bash
# 检查 MySQL 服务
sudo systemctl status mysql

# 检查连接
telnet localhost 3306

# 查看数据库日志
tail -f /var/log/mysql/error.log
```

---

## 📚 十二、维护与备份

### 12.1 数据库备份

```bash
# 创建备份脚本
cat > /opt/scripts/backup-mysql.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR
mysqldump -u inspection_user -p'YourPassword' room_inspection | gzip > $BACKUP_DIR/room_inspection_$DATE.sql.gz
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
EOF

chmod +x /opt/scripts/backup-mysql.sh

# 添加到 crontab（每天凌晨2点备份）
crontab -e
0 2 * * * /opt/scripts/backup-mysql.sh
```

### 12.2 应用更新

```bash
# 后端更新
cd java-backend
git pull
mvn clean package -DskipTests
sudo systemctl restart room-inspection

# 前端更新
pnpm install
pnpm build
pm2 restart room-inspection-frontend
```

---

## 📞 十三、技术支持

### 联系方式
- **技术文档**: 详见 `docs/` 目录
- **架构设计**: 详见 `ARCHITECTURE.md`
- **性能优化**: 详见 `PERFORMANCE_OPTIMIZATION.md`

---

**部署完成！** 现在您可以通过浏览器访问机房巡检系统了。
