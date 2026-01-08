# 机房巡检系统部署指南

## 目录结构

```
java-backend/
├── docs/
│   ├── deployment-architecture.md    # 部署与架构文档（详细说明）
│   ├── deployment-guide.md           # 本文档（部署指南）
│   └── compatibility.md              # 兼容性文档
├── k8s/
│   └── deployment.yaml               # Kubernetes部署配置
├── docker/
│   └── healthcheck.sh                # Docker健康检查脚本
├── network/
│   ├── firewall-rules.sh             # 防火墙规则脚本
│   └── ipsec-vpn.conf               # IPsec VPN配置
├── nginx/
│   ├── nginx.conf                   # Nginx主配置
│   └── conf.d/
│       └── default.conf              # Nginx虚拟主机配置
├── prometheus/
│   ├── prometheus.yml                # Prometheus监控配置
│   └── alerts/
│       └── backend.yml               # 后端告警规则
├── grafana/
│   └── provisioning/
│       └── dashboards/
│           └── datasource.yml        # Grafana数据源配置
├── scripts/
│   └── deploy.sh                     # 一键部署脚本
├── docker-compose.yml               # Docker Compose配置
├── Dockerfile                       # Docker镜像构建文件
└── src/main/resources/
    ├── application-dev.yml          # 开发环境配置
    ├── application-test.yml          # 测试环境配置
    └── application-prod.yml          # 生产环境配置
```

## 快速开始

### 1. Docker Compose部署（开发环境）

```bash
# 克隆代码
git clone https://github.com/your-org/room-inspection.git
cd room-inspection/java-backend

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

### 2. Kubernetes部署（生产环境）

```bash
# 部署到Kubernetes
kubectl apply -f k8s/deployment.yaml

# 查看部署状态
kubectl get pods -n room-inspection

# 查看服务
kubectl get svc -n room-inspection

# 查看日志
kubectl logs -f -n room-inspection deployment/room-inspection-backend

# 扩容
kubectl scale deployment/room-inspection-backend --replicas=5 -n room-inspection
```

### 3. 手动部署（传统方式）

```bash
# 执行一键部署脚本
cd java-backend/scripts
chmod +x deploy.sh
./deploy.sh

# 按照提示选择环境（开发/测试/生产）
```

## 详细部署步骤

### 前置要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 8u311+ | 后端运行环境 |
| Maven | 3.6+ | 构建工具 |
| MySQL | 5.7+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Nginx | 1.20+ | 反向代理 |
| Docker | 20.10+ | 容器化部署（可选） |
| Kubernetes | 1.20+ | 容器编排（可选） |

### 1. 环境配置

#### 1.1 开发环境

使用 `application-dev.yml` 配置，特点：
- 单机部署
- 详细日志输出
- SQL语句打印
- 使用本地数据库

#### 1.2 测试环境

使用 `application-test.yml` 配置，特点：
- 双节点部署
- 数据库读写分离
- 基础日志输出
- 真实数据脱敏

#### 1.3 生产环境

使用 `application-prod.yml` 配置，特点：
- 高可用集群
- 数据库读写分离 + 主从复制
- Redis集群
- 优化的JVM参数
- 限制的日志输出

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p << EOF
CREATE DATABASE room_inspection DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

# 导入初始化脚本
mysql -u root -p room_inspection < sql/init.sql
```

### 3. 后端部署

#### 3.1 构建应用

```bash
cd java-backend
mvn clean package -DskipTests
```

#### 3.2 部署JAR包

```bash
# 创建部署目录
sudo mkdir -p /opt/room-inspection
sudo mkdir -p /var/log/room-inspection

# 复制JAR包
sudo cp target/room-inspection-*.jar /opt/room-inspection/

# 设置权限
sudo chown -R room-inspection:room-inspection /opt/room-inspection
sudo chown -R room-inspection:room-inspection /var/log/room-inspection
```

#### 3.3 配置systemd服务

```bash
# 创建服务文件
sudo tee /etc/systemd/system/room-inspection.service > /dev/null << 'EOF'
[Unit]
Description=Room Inspection System Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=room-inspection
Group=room-inspection
WorkingDirectory=/opt/room-inspection
Environment="SPRING_PROFILES_ACTIVE=prod"
ExecStart=/usr/bin/java -jar /opt/room-inspection/room-inspection.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable room-inspection
sudo systemctl start room-inspection
```

### 4. 前端部署

```bash
cd vue-frontend

# 安装依赖
npm install

# 构建
npm run build

# 部署到Nginx
sudo cp -r dist/* /var/www/room-inspection/
sudo chown -R nginx:nginx /var/www/room-inspection

# 配置Nginx
sudo cp ../java-backend/nginx/*.conf /etc/nginx/
sudo cp ../java-backend/nginx/conf.d/*.conf /etc/nginx/conf.d/

# 测试并重启
sudo nginx -t
sudo systemctl restart nginx
```

### 5. 负载均衡配置

#### 5.1 Nginx负载均衡

参考 `nginx/conf.d/default.conf` 配置文件。

#### 5.2 高可用配置

使用Keepalived实现Nginx高可用：

```bash
# 安装Keepalived
sudo apt-get install keepalived

# 配置Keepalived
sudo tee /etc/keepalived/keepalived.conf > /dev/null << 'EOF'
vrrp_script chk_nginx {
    script "/usr/bin/killall -0 nginx"
    interval 2
    weight -2
}

vrrp_instance VI_1 {
    state MASTER  # BACKUP for slave
    interface eth0
    virtual_router_id 51
    priority 100  # 90 for slave
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1234
    }
    virtual_ipaddress {
        10.0.1.100
    }
    track_script {
        chk_nginx
    }
}
EOF

# 启动Keepalived
sudo systemctl start keepalived
sudo systemctl enable keepalived
```

### 6. 监控部署

#### 6.1 Prometheus部署

```bash
# Docker Compose已包含Prometheus配置
docker-compose up -d prometheus

# 访问Prometheus UI
# http://localhost:9090
```

#### 6.2 Grafana部署

```bash
# Docker Compose已包含Grafana配置
docker-compose up -d grafana

# 访问Grafana UI
# http://localhost:3000
# 默认用户名/密码: admin/admin
```

#### 6.3 告警配置

告警规则位于 `prometheus/alerts/backend.yml`。

需要配置Alertmanager实现告警通知。

### 7. 网络安全配置

#### 7.1 防火墙配置

```bash
# 执行防火墙规则脚本
sudo chmod +x network/firewall-rules.sh
sudo ./network/firewall-rules.sh
```

#### 7.2 IPsec VPN配置

```bash
# 复制VPN配置
sudo cp network/ipsec-vpn.conf /etc/ipsec.conf

# 重启IPsec服务
sudo systemctl restart strongswan-starter
sudo systemctl enable strongswan-starter
```

### 8. 数据备份配置

#### 8.1 MySQL备份

```bash
# 创建备份脚本
sudo tee /usr/local/bin/mysql-backup.sh > /dev/null << 'EOF'
#!/bin/bash
BACKUP_DIR="/backup/mysql"
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u backup_user -p$BACKUP_PASSWORD room_inspection | gzip > $BACKUP_DIR/room_inspection_$DATE.sql.gz
find $BACKUP_DIR -name "room_inspection_*.sql.gz" -mtime +30 -delete
EOF

# 配置定时任务
chmod +x /usr/local/bin/mysql-backup.sh
(crontab -l 2>/dev/null; echo "0 2 * * * /usr/local/bin/mysql-backup.sh") | crontab -
```

## 运维指南

### 查看服务状态

```bash
# 后端服务
sudo systemctl status room-inspection

# Nginx
sudo systemctl status nginx

# MySQL
sudo systemctl status mysql

# Redis
sudo systemctl status redis
```

### 查看日志

```bash
# 后端日志
sudo tail -f /var/log/room-inspection/application.log

# Nginx日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# Docker日志
docker-compose logs -f backend

# Kubernetes日志
kubectl logs -f deployment/room-inspection-backend -n room-inspection
```

### 常见问题排查

#### 1. 服务无法启动

```bash
# 检查端口占用
sudo netstat -tulpn | grep 8080

# 检查日志
sudo journalctl -u room-inspection -n 50

# 检查配置文件
java -jar /opt/room-inspection/room-inspection.jar --debug
```

#### 2. 数据库连接失败

```bash
# 检查数据库服务
sudo systemctl status mysql

# 测试连接
mysql -h localhost -u prod_user -p

# 检查防火墙
sudo iptables -L -n | grep 3306
```

#### 3. Redis连接超时

```bash
# 检查Redis服务
sudo systemctl status redis

# 测试连接
redis-cli ping

# 检查防火墙
sudo iptables -L -n | grep 6379
```

### 性能优化

#### JVM参数优化

```bash
# 编辑systemd服务文件
sudo vim /etc/systemd/system/room-inspection.service

# 添加JVM参数
ExecStart=/usr/bin/java -Xms4g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar ...

# 重启服务
sudo systemctl daemon-reload
sudo systemctl restart room-inspection
```

#### 数据库优化

```sql
-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query%';
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- 查看连接数
SHOW STATUS LIKE 'Threads_connected';
SHOW VARIABLES LIKE 'max_connections';
```

## 更新部署

### 滚动更新（Kubernetes）

```bash
# 更新镜像
kubectl set image deployment/room-inspection-backend \
  backend=room-inspection/backend:1.1.0 \
  -n room-inspection

# 查看更新状态
kubectl rollout status deployment/room-inspection-backend -n room-inspection

# 回滚
kubectl rollout undo deployment/room-inspection-backend -n room-inspection
```

### 手动更新（传统方式）

```bash
# 停止服务
sudo systemctl stop room-inspection

# 备份当前版本
cp /opt/room-inspection/room-inspection.jar /opt/room-inspection/room-inspection.jar.bak

# 部署新版本
cp target/room-inspection-1.1.0.jar /opt/room-inspection/room-inspection.jar

# 启动服务
sudo systemctl start room-inspection

# 验证更新
curl http://localhost:8080/actuator/health
```

## 安全加固

### 1. SSL证书配置

```bash
# 生成自签名证书（测试环境）
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/nginx/ssl/roominspection.com.key \
  -out /etc/nginx/ssl/roominspection.com.crt

# 或使用Let's Encrypt（生产环境）
sudo certbot --nginx -d www.roominspection.com
```

### 2. 敏感信息加密

```bash
# 使用环境变量存储敏感信息
export DB_PASSWORD="your_password"
export REDIS_PASSWORD="your_password"
export JWT_SECRET="your_secret"

# 或使用Vault等密钥管理工具
```

### 3. 审计日志

启用操作审计日志，记录所有关键操作。

## 支持与联系

- 技术文档: `docs/deployment-architecture.md`
- 技术支持: support@roominspection.com
- 问题反馈: https://github.com/your-org/room-inspection/issues
