# 部署文档

## 环境要求

### 前端
- Node.js 18+
- pnpm 8+

### 后端
- Node.js 20+
- PostgreSQL 14+
- Redis (可选)
- pnpm 8+

## 开发环境部署

### 1. 克隆项目

```bash
git clone <repository-url>
cd room-inspection-system
```

### 2. 安装依赖

#### 前端
```bash
pnpm install
```

#### 后端
```bash
cd backend
pnpm install
```

### 3. 配置环境变量

#### 前端配置

复制 `.env.local.example` 为 `.env.local`:

```bash
cp .env.local.example .env.local
```

编辑 `.env.local`:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:3000
NEXT_PUBLIC_SSO_ENABLED=false
NEXT_PUBLIC_SSO_TYPE=local
```

#### 后端配置

复制 `.env.example` 为 `.env`:

```bash
cd backend
cp .env.example .env
```

编辑 `.env`:

```bash
# 数据库
DATABASE_URL=postgresql://postgres:password@localhost:5432/inspection

# JWT密钥 (必须修改)
JWT_SECRET=your-production-secret-key

# SSO配置 (按需配置)
SSO_TYPE=local
```

### 4. 初始化数据库

```bash
cd backend

# 生成Prisma客户端
npx prisma generate

# 运行数据库迁移
npx prisma migrate dev --name init
```

### 5. 启动服务

#### 启动前端

```bash
# 在项目根目录
pnpm dev
# 或
bash .cozeproj/scripts/dev_run.sh
```

前端将运行在 `http://localhost:5000`

#### 启动后端

```bash
cd backend
pnpm dev
```

后端将运行在 `http://localhost:3000`

### 6. 初始化数据

创建管理员账号:

```bash
cd backend
node scripts/init-admin.js
```

或通过API创建:

```bash
curl -X POST http://localhost:3000/api/v1/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "username": "admin",
    "password": "Admin@123",
    "email": "admin@example.com",
    "realName": "系统管理员",
    "roleId": "admin-role-id"
  }'
```

## 生产环境部署

### 方案1: Docker部署

#### 创建Docker Compose文件

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: inspection-db
    environment:
      POSTGRES_USER: inspection
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: inspection
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: inspection-redis
    ports:
      - "6379:6379"
    restart: unless-stopped

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: inspection-backend
    environment:
      NODE_ENV: production
      DATABASE_URL: postgresql://inspection:${DB_PASSWORD}@postgres:5432/inspection
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - postgres
      - redis
    ports:
      - "3000:3000"
    restart: unless-stopped

  frontend:
    build:
      context: .
      dockerfile: Dockerfile.frontend
    container_name: inspection-frontend
    ports:
      - "80:3000"
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  postgres-data:
```

#### 创建后端Dockerfile

```dockerfile
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN pnpm install --prod

COPY . .
RUN pnpm run build

EXPOSE 3000

CMD ["pnpm", "start"]
```

#### 创建前端Dockerfile

```dockerfile
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN pnpm install

COPY . .
RUN pnpm run build

FROM node:20-alpine

WORKDIR /app

COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package*.json ./
RUN pnpm install --prod

EXPOSE 3000

CMD ["pnpm", "start"]
```

#### 部署

```bash
# 创建环境变量文件
cat > .env.prod <<EOF
DB_PASSWORD=your-strong-password
JWT_SECRET=your-jwt-secret-key
EOF

# 启动所有服务
docker-compose up -d
```

### 方案2: Kubernetes部署

#### 后端部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inspection-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: inspection-backend
  template:
    metadata:
      labels:
        app: inspection-backend
    spec:
      containers:
      - name: backend
        image: your-registry/inspection-backend:latest
        ports:
        - containerPort: 3000
        env:
        - name: NODE_ENV
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: inspection-secrets
              key: database-url
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: inspection-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /health
            port: 3000
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 3000
          initialDelaySeconds: 5
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: inspection-backend-service
spec:
  selector:
    app: inspection-backend
  ports:
  - protocol: TCP
    port: 3000
    targetPort: 3000
  type: ClusterIP
```

#### 前端部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: inspection-frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: inspection-frontend
  template:
    metadata:
      labels:
        app: inspection-frontend
    spec:
      containers:
      - name: frontend
        image: your-registry/inspection-frontend:latest
        ports:
        - containerPort: 3000
        env:
        - name: NEXT_PUBLIC_API_BASE_URL
          value: "http://inspection-backend-service:3000"
        resources:
          requests:
            memory: "128Mi"
            cpu: "125m"
          limits:
            memory: "256Mi"
            cpu: "250m"
---
apiVersion: v1
kind: Service
metadata:
  name: inspection-frontend-service
spec:
  selector:
    app: inspection-frontend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 3000
  type: LoadBalancer
```

### 方案3: 传统服务器部署

#### 后端

使用PM2管理进程:

```bash
# 安装PM2
npm install -g pm2

# 启动服务
cd backend
pm2 start dist/app.js --name inspection-backend

# 保存进程列表
pm2 save

# 设置开机自启
pm2 startup
```

#### 前端

构建并使用Nginx代理:

```bash
# 构建
pnpm run build

# Nginx配置
cat > /etc/nginx/sites-available/inspection <<EOF
server {
    listen 80;
    server_name inspection.example.com;

    # 前端静态文件
    location / {
        root /var/www/inspection;
        try_files \$uri \$uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_cache_bypass \$http_upgrade;
    }
}
EOF

# 启用站点
ln -s /etc/nginx/sites-available/inspection /etc/nginx/sites-enabled/

# 重启Nginx
nginx -t && systemctl restart nginx
```

## 安全加固

### 1. HTTPS配置

使用Let's Encrypt:

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d inspection.example.com
```

### 2. 防火墙配置

```bash
# UFW
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

### 3. 数据库安全

- 修改默认端口
- 启用SSL连接
- 定期备份
- 限制远程访问

### 4. 环境变量管理

使用Vault或AWS Secrets Manager管理敏感信息。

## 监控与日志

### 后端日志

日志文件位置: `backend/logs/`

- `app.log`: 应用日志
- `error.log`: 错误日志
- `audit.log`: 审计日志

### 日志聚合

使用ELK Stack或Loki进行日志聚合。

### 性能监控

- APM: New Relic / Datadog
- 指标: Prometheus + Grafana
- 日志: ELK / Loki

## 备份策略

### 数据库备份

```bash
# 每日备份
0 2 * * * pg_dump -U inspection inspection | gzip > /backup/inspection_$(date +\%Y\%m\%d).sql.gz

# 保留7天备份
find /backup -name "inspection_*.sql.gz" -mtime +7 -delete
```

### 文件备份

备份对象存储中的文件。

## 回滚策略

1. 保持至少3个版本的Docker镜像
2. 数据库迁移前自动备份
3. 使用蓝绿部署或金丝雀发布

## 故障排查

### 常见问题

#### 1. 数据库连接失败

检查 `DATABASE_URL` 配置和数据库服务状态。

#### 2. JWT验证失败

检查 `JWT_SECRET` 配置前后端是否一致。

#### 3. CORS错误

检查后端 `CORS_ORIGIN` 配置。

#### 4. 文件上传失败

检查对象存储配置和权限。

## 性能优化

### 数据库

- 创建索引
- 查询优化
- 连接池配置

### 缓存

- Redis缓存热点数据
- API响应缓存

### 前端

- 代码分割
- 图片优化
- CDN加速
