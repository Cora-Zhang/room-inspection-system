# 机房巡检系统部署与架构文档

## 目录
- [1. 部署环境](#1-部署环境)
- [2. 高可用架构](#2-高可用架构)
- [3. 网络架构](#3-网络架构)
- [4. 部署步骤](#4-部署步骤)
- [5. 监控与运维](#5-监控与运维)
- [6. 安全加固](#6-安全加固)

---

## 1. 部署环境

### 1.1 环境规划

建议分开发、测试、生产三套环境，逐步部署验证。

#### 环境对比表

| 环境 | 用途 | 配置要求 | 数据来源 | 访问方式 |
|------|------|---------|---------|---------|
| **开发环境** | 日常开发调试 | 单机部署，1核2G | 模拟数据 | 内网访问 |
| **测试环境** | 功能测试、集成测试 | 双节点部署，2核4G | 真实数据脱敏 | 内网访问 |
| **生产环境** | 正式运行 | 高可用集群，4核8G+ | 真实生产数据 | 通过防火墙访问 |

### 1.2 环境配置

#### 1.2.1 开发环境配置

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/room_inspection_dev?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: dev_password
  redis:
    host: localhost
    port: 6379
    database: 0

server:
  port: 8080

logging:
  level:
    root: DEBUG
    com.roominspection: DEBUG
```

#### 1.2.2 测试环境配置

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:mysql://test-db.internal:3306/room_inspection_test?useSSL=true&serverTimezone=Asia/Shanghai
    username: test_user
    password: ${DB_PASSWORD}
  redis:
    host: test-redis.internal
    port: 6379
    database: 1
    password: ${REDIS_PASSWORD}

server:
  port: 8080

logging:
  level:
    root: INFO
    com.roominspection: DEBUG
```

#### 1.2.3 生产环境配置

```yaml
# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-db-master.internal:3306/room_inspection?useSSL=true&serverTimezone=Asia/Shanghai
    username: prod_user
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  redis:
    host: prod-redis-cluster.internal
    port: 6379
    database: 0
    password: ${REDIS_PASSWORD}
    cluster:
      nodes:
        - prod-redis-node1.internal:6379
        - prod-redis-node2.internal:6379
        - prod-redis-node3.internal:6379

server:
  port: 8080
  tomcat:
    max-threads: 200
    min-spare-threads: 20

logging:
  level:
    root: WARN
    com.roominspection: INFO
  file:
    name: /var/log/room-inspection/application.log
  logback:
    rollingpolicy:
      max-file-size: 100MB
      max-history: 30
```

### 1.3 环境切换策略

```bash
# 开发环境启动
java -jar room-inspection.jar --spring.profiles.active=dev

# 测试环境启动
java -jar room-inspection.jar --spring.profiles.active=test

# 生产环境启动
java -jar room-inspection.jar --spring.profiles.active=prod
```

---

## 2. 高可用架构

### 2.1 整体架构图

```
                    ┌─────────────┐
                    │  负载均衡器  │
                    │ (Nginx/HAProxy)│
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
     ┌────▼────┐      ┌───▼───┐      ┌───▼───┐
     │后端实例1 │      │后端实例2│      │后端实例3│
     │(Instance)│      │(Instance)│      │(Instance)│
     └────┬────┘      └───┬───┘      └───┬───┘
          │                │                │
          └────────────────┼────────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
     ┌────▼────┐      ┌───▼───┐      ┌───▼───┐
     │ MySQL主库│◄────►│MySQL从库│◄────►│MySQL从库│
     │ (Master) │  复制 │(Slave-1)│  复制 │(Slave-2)│
     └────┬────┘      └────────┘      └────────┘
          │
          ▼
     ┌────────┐
     │Redis集群│
     │(Sentinel)│
     └────────┘
```

### 2.2 后端服务高可用

#### 2.2.1 部署要求

- **最小配置**：部署2个实例
- **推荐配置**：部署3-5个实例
- **负载均衡**：使用Nginx或云负载均衡
- **健康检查**：每5秒检查一次健康状态

#### 2.2.2 Nginx负载均衡配置

```nginx
upstream room_inspection_backend {
    least_conn;  # 最少连接数算法
    server backend1.internal:8080 max_fails=3 fail_timeout=30s;
    server backend2.internal:8080 max_fails=3 fail_timeout=30s;
    server backend3.internal:8080 max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name api.roominspection.com;

    location / {
        proxy_pass http://room_inspection_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

#### 2.2.3 实例监控与自动伸缩

```yaml
# Kubernetes Deployment配置示例
apiVersion: apps/v1
kind: Deployment
metadata:
  name: room-inspection-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: room-inspection
  template:
    metadata:
      labels:
        app: room-inspection
    spec:
      containers:
      - name: backend
        image: room-inspection:v1.0.0
        ports:
        - containerPort: 8080
        resources:
          requests:
            cpu: "2"
            memory: "4Gi"
          limits:
            cpu: "4"
            memory: "8Gi"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 2.3 数据库高可用

#### 2.3.1 MySQL主从复制配置

```ini
# 主库配置 (my.cnf)
[mysqld]
server-id = 1
log-bin = mysql-bin
binlog-format = ROW
binlog-do-db = room_inspection
max_binlog_size = 100M
expire_logs_days = 7

# 创建复制用户
CREATE USER 'replicator'@'%' IDENTIFIED BY 'replication_password';
GRANT REPLICATION SLAVE ON *.* TO 'replicator'@'%';
FLUSH PRIVILEGES;

# 查看主库状态
SHOW MASTER STATUS;
```

```ini
# 从库配置 (my.cnf)
[mysqld]
server-id = 2
relay-log = mysql-relay-bin
read-only = 1

# 配置主从复制
CHANGE MASTER TO
  MASTER_HOST='master-db.internal',
  MASTER_USER='replicator',
  MASTER_PASSWORD='replication_password',
  MASTER_LOG_FILE='mysql-bin.000001',
  MASTER_LOG_POS=154;

START SLAVE;
SHOW SLAVE STATUS\G
```

#### 2.3.2 读写分离配置

```java
// application.yml 配置多数据源
spring:
  shardingsphere:
    datasource:
      names: master,slave1,slave2
      master:
        type: com.zaxxer.hikari.HikariDataSource
        jdbc-url: jdbc:mysql://master-db.internal:3306/room_inspection
        username: prod_user
        password: ${DB_PASSWORD}
      slave1:
        type: com.zaxxer.hikari.HikariDataSource
        jdbc-url: jdbc:mysql://slave-db-1.internal:3306/room_inspection
        username: readonly_user
        password: ${READONLY_PASSWORD}
      slave2:
        type: com.zaxxer.hikari.HikariDataSource
        jdbc-url: jdbc:mysql://slave-db-2.internal:3306/room_inspection
        username: readonly_user
        password: ${READONLY_PASSWORD}
    rules:
      readwrite-splitting:
        data-sources:
          readwrite_ds:
            write-data-source-name: master
            read-data-source-names: slave1,slave2
            load-balancer-name: round_robin
        load-balancers:
          round_robin:
            type: ROUND_ROBIN
```

#### 2.3.3 数据备份策略

```bash
# 全量备份脚本 (每天凌晨2点执行)
#!/bin/bash
BACKUP_DIR="/backup/mysql/full"
DATE=$(date +%Y%m%d)
mysqldump -u backup_user -p$BACKUP_PASSWORD \
  --single-transaction \
  --master-data=2 \
  --flush-logs \
  --triggers \
  --routines \
  --events \
  room_inspection | gzip > $BACKUP_DIR/room_inspection_$DATE.sql.gz

# 保留30天备份
find $BACKUP_DIR -name "room_inspection_*.sql.gz" -mtime +30 -delete

# 增量备份 (每小时执行一次)
#!/bin/bash
BACKUP_DIR="/backup/mysql/incremental"
DATE=$(date +%Y%m%d)
HOUR=$(date +%H)
mysqladmin -u backup_user -p$BACKUP_PASSWORD flush-logs
cp /var/lib/mysql/mysql-bin.* $BACKUP_DIR/
```

### 2.4 Redis缓存高可用

#### 2.4.1 Redis Sentinel架构

```conf
# sentinel.conf
port 26379
sentinel monitor redis-master redis-master.internal 6379 2
sentinel down-after-milliseconds redis-master 5000
sentinel failover-timeout redis-master 180000
sentinel parallel-syncs redis-master 1
```

```yaml
# Spring Boot配置
spring:
  redis:
    sentinel:
      master: redis-master
      nodes:
        - sentinel1.internal:26379
        - sentinel2.internal:26379
        - sentinel3.internal:26379
      password: ${REDIS_PASSWORD}
```

#### 2.4.2 Redis Cluster架构

```conf
# redis-cluster.conf
port 7000
cluster-enabled yes
cluster-config-file nodes-7000.conf
cluster-node-timeout 5000
appendonly yes
```

```bash
# 创建Redis集群
redis-cli --cluster create \
  redis-node1.internal:7000 \
  redis-node2.internal:7000 \
  redis-node3.internal:7000 \
  redis-node4.internal:7000 \
  redis-node5.internal:7000 \
  redis-node6.internal:7000 \
  --cluster-replicas 1
```

### 2.5 监控采集服务分布式部署

```yaml
# 监控采集任务分布式执行配置
spring:
  cloud:
    zookeeper:
      connect-string: zk1.internal:2181,zk2.internal:2181,zk3.internal:2181

# 使用分布式锁避免重复执行
@Scheduled(cron = "0 */5 * * * ?")
public void collectDeviceMetrics() {
    String lockKey = "lock:device:metrics:collect";
    boolean locked = redisTemplate.opsForValue().setIfAbsent(
        lockKey, "locked", 10, TimeUnit.SECONDS
    );

    if (locked) {
        try {
            deviceMetricService.collectMetrics();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}
```

---

## 3. 网络架构

### 3.1 网络架构图

```
┌─────────────────────────────────────────────────────────────┐
│                       外部网络 (Internet)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                    ┌──────▼──────┐
                    │  防火墙FW   │
                    │ (WAF/DDOS)  │
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────▼──────┐   ┌─────▼──────┐   ┌─────▼──────┐
    │  DMZ区域   │   │  业务网络  │   │ 监控网络  │
    │            │   │            │   │            │
    │ Nginx LB   │   │ 后端服务群 │   │ 监控采集器 │
    │ API Gateway│   │ 数据库集群 │   │ 传感器网络 │
    │ 前端服务器 │   │ Redis集群  │   │ 门禁控制器 │
    └────────────┘   └────────────┘   └────────────┘
                           │
                    ┌──────▼──────┐
                    │  内网核心   │
                    │   交换机    │
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
    ┌─────▼──────┐   ┌─────▼──────┐   ┌─────▼──────┐
    │ 机房A区域  │   │ 机房B区域  │   │ 机房C区域  │
    │            │   │            │   │            │
    │ IT设备     │   │ IT设备     │   │ IT设备     │
    │ 传感器     │   │ 传感器     │   │ 传感器     │
    │ 门禁设备   │   │ 门禁设备   │   │ 门禁设备   │
    └────────────┘   └────────────┘   └────────────┘
```

### 3.2 网络分区策略

#### 3.2.1 DMZ区域

**用途**：对外服务区域，部署面向Internet的服务

**部署服务**：
- Nginx负载均衡器
- API Gateway
- 前端静态资源服务器

**安全策略**：
- 只开放80（HTTP）、443（HTTPS）端口
- 禁止SSH直接访问
- 配置WAF防护SQL注入、XSS等攻击

```bash
# 防火墙规则 (DMZ)
iptables -A INPUT -p tcp --dport 80 -j ACCEPT
iptables -A INPUT -p tcp --dport 443 -j ACCEPT
iptables -A INPUT -p tcp --dport 22 -s 10.0.0.0/8 -j ACCEPT  # 仅内网SSH
iptables -A INPUT -j DROP
```

#### 3.2.2 业务网络

**用途**：内部业务服务区域，部署核心业务系统

**部署服务**：
- 后端服务集群（Spring Boot）
- 数据库集群（MySQL）
- Redis缓存集群

**安全策略**：
- 只允许DMZ区域访问8080端口
- 数据库只允许后端服务访问
- 禁止外网直接访问

```bash
# 防火墙规则 (业务网络)
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 8080 -j ACCEPT  # DMZ访问
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 3306 -j ACCEPT  # 后端访问DB
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 6379 -j ACCEPT  # 后端访问Redis
iptables -A INPUT -j DROP
```

#### 3.2.3 监控网络

**用途**：监控数据采集区域，连接各类监控设备

**部署服务**：
- 监控采集服务
- SNMP Agent
- Modbus Gateway
- 传感器网络网关
- 门禁系统对接服务

**安全策略**：
- 监控网络与业务网络分离
- 只允许特定协议通信（SNMP、Modbus、MQTT等）
- 门禁对接需通过防火墙策略控制

```bash
# 防火墙规则 (监控网络)
# SNMP (161/162)
iptables -A INPUT -p udp --dport 161 -j ACCEPT
iptables -A INPUT -p udp --dport 162 -j ACCEPT

# Modbus TCP (502)
iptables -A INPUT -s 10.0.3.0/24 -p tcp --dport 502 -j ACCEPT

# MQTT (1883)
iptables -A INPUT -p tcp --dport 1883 -j ACCEPT

# 门禁API (自定义端口)
iptables -A INPUT -s 192.168.100.0/24 -p tcp --dport 8443 -j ACCEPT

# 禁止其他访问
iptables -A INPUT -j DROP
```

### 3.3 跨机房监控数据汇聚

#### 3.3.1 VPN隧道配置

```bash
# IPsec VPN配置 (StrongSwan)
config setup
    charondebug="ike 2, knl 2, cfg 2, net 2, esp 2, dmn 2, mgr 2"

conn room_inspection_vpn
    left=10.0.1.1
    leftsubnet=10.0.1.0/24
    leftrsasigkey=...  # 机房A公钥
    right=10.0.2.1
    rightsubnet=10.0.2.0/24
    rightrsasigkey=...  # 机房B公钥
    authby=rsasig
    auto=start
    dpdaction=clear
    dpddelay=30s
    dpdtimeout=120s
```

#### 3.3.2 数据汇聚架构

```yaml
# Spring Boot配置多数据源汇聚
spring:
  shardingsphere:
    datasource:
      names: master_a, master_b, master_c
      master_a:
        jdbc-url: jdbc:mysql://room-a-db.internal:3306/room_inspection
      master_b:
        jdbc-url: jdbc:mysql://room-b-db.internal:3306/room_inspection
      master_c:
        jdbc-url: jdbc:mysql://room-c-db.internal:3306/room_inspection
```

### 3.4 门禁对接网络安全

#### 3.4.1 防火墙策略

```bash
# 只允许特定IP访问门禁API
iptables -A INPUT -s 192.168.100.10 -p tcp --dport 8443 -j ACCEPT
iptables -A INPUT -s 192.168.100.20 -p tcp --dport 8443 -j ACCEPT

# 限制连接频率
iptables -A INPUT -p tcp --dport 8443 -m limit --limit 10/second --limit-burst 20 -j ACCEPT

# 记录访问日志
iptables -A INPUT -p tcp --dport 8443 -j LOG --log-prefix "DoorAccess: "
```

#### 3.4.2 双向SSL认证

```java
// 门禁API双向SSL配置
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-alias: room-inspection
    trust-store: classpath:truststore.jks
    trust-store-password: ${TRUSTSTORE_PASSWORD}
    client-auth: need
```

#### 3.4.3 API访问控制

```java
// IP白名单过滤器
@Component
public class DoorAccessWhitelistFilter implements Filter {

    @Value("${door.access.whitelist}")
    private String whitelist;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = getClientIp(httpRequest);

        if (!isWhitelisted(clientIp)) {
            ((HttpServletResponse) response).sendError(403, "Access Denied");
            return;
        }

        chain.doFilter(request, response);
    }
}
```

---

## 4. 部署步骤

### 4.1 前置准备

#### 4.1.1 软件依赖

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 8u311+ | 后端运行环境 |
| Node.js | 18+ | 前端构建 |
| MySQL | 5.7+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Nginx | 1.20+ | 负载均衡 |
| Docker | 20.10+ | 容器化部署（可选） |

#### 4.1.2 系统要求

**后端服务节点**：
- CPU: 2核+
- 内存: 4GB+
- 磁盘: 50GB+

**数据库节点**：
- CPU: 4核+
- 内存: 8GB+
- 磁盘: 500GB+ SSD

### 4.2 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS room_inspection DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE room_inspection;

-- 创建表（按顺序执行建表SQL）
-- 1. 用户权限表
-- 2. 机房设备表
-- 3. 巡检任务表
-- 4. 监控数据表
-- 5. 告警记录表
-- ...

-- 初始化数据
INSERT INTO users (id, username, password, real_name, status, source) VALUES
('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ACTIVE', 'LOCAL');
```

### 4.3 后端部署

#### 4.3.1 构建应用

```bash
# 克隆代码
git clone https://github.com/your-org/room-inspection.git
cd room-inspection/java-backend

# 构建
mvn clean package -DskipTests

# 生成JAR包
# target/room-inspection-backend-1.0.0.jar
```

#### 4.3.2 配置应用

```bash
# 复制配置文件
cp application-prod.yml /opt/room-inspection/application.yml

# 编辑配置
vim /opt/room-inspection/application.yml
```

#### 4.3.3 创建systemd服务

```ini
# /etc/systemd/system/room-inspection.service
[Unit]
Description=Room Inspection System Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=room-inspection
WorkingDirectory=/opt/room-inspection
ExecStart=/usr/bin/java -jar -Xms2g -Xmx4g -XX:+UseG1GC /opt/room-inspection/room-inspection.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 启用并启动服务
systemctl daemon-reload
systemctl enable room-inspection
systemctl start room-inspection
systemctl status room-inspection
```

### 4.4 前端部署

#### 4.4.1 构建前端

```bash
cd room-inspection/vue-frontend

# 安装依赖
npm install

# 构建
npm run build

# 生成dist目录
```

#### 4.4.2 Nginx配置

```nginx
server {
    listen 80;
    server_name www.roominspection.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name www.roominspection.com;

    ssl_certificate /etc/nginx/ssl/roominspection.com.crt;
    ssl_certificate_key /etc/nginx/ssl/roominspection.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 前端静态资源
    location / {
        root /var/www/room-inspection/dist;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://room_inspection_backend/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # SSO回调
    location /api/sso/callback {
        proxy_pass http://room_inspection_backend/api/sso/callback;
        proxy_set_header Host $host;
    }

    # WebSocket
    location /ws {
        proxy_pass http://room_inspection_backend/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
}
```

#### 4.4.3 部署静态资源

```bash
# 创建目录
mkdir -p /var/www/room-inspection

# 复制构建产物
cp -r dist/* /var/www/room-inspection/

# 设置权限
chown -R nginx:nginx /var/www/room-inspection
chmod -R 755 /var/www/room-inspection

# 测试Nginx配置
nginx -t

# 重启Nginx
systemctl restart nginx
```

### 4.5 负载均衡配置

```nginx
# 安装Keepalived实现高可用
# /etc/keepalived/keepalived.conf
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
```

### 4.6 部署验证

```bash
# 1. 检查后端服务
curl http://localhost:8080/actuator/health

# 2. 检查前端页面
curl -I https://www.roominspection.com

# 3. 检查数据库连接
mysql -h localhost -u prod_user -p -e "SELECT 1"

# 4. 检查Redis连接
redis-cli -h localhost -a $REDIS_PASSWORD ping

# 5. 检查负载均衡
curl http://10.0.1.100/actuator/health

# 6. 检查监控采集
curl http://localhost:8080/api/monitor/health
```

---

## 5. 监控与运维

### 5.1 监控体系

#### 5.1.1 监控指标

| 监控对象 | 指标 | 告警阈值 | 级别 |
|---------|------|---------|------|
| **后端服务** | CPU使用率 | >80% | 警告 |
| | 内存使用率 | >85% | 警告 |
| | 响应时间 | >2s | 警告 |
| | 错误率 | >1% | 严重 |
| | JVM堆内存 | >90% | 严重 |
| **数据库** | QPS | >5000 | 警告 |
| | 慢查询数 | >10/分钟 | 警告 |
| | 主从延迟 | >5秒 | 严重 |
| | 连接数 | >80% | 警告 |
| **Redis** | 内存使用率 | >85% | 警告 |
| | 命中率 | <90% | 警告 |
| | 连接数 | >1000 | 警告 |
| **监控采集** | 设备离线数 | >5 | 严重 |
| | 数据采集延迟 | >30秒 | 警告 |
| | 采集失败率 | >5% | 警告 |

#### 5.1.2 Prometheus配置

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
            - localhost:9093

rule_files:
    - "alerts/*.yml"

scrape_configs:
  - job_name: 'room-inspection-backend'
    static_configs:
      - targets:
          - 'backend1:8080'
          - 'backend2:8080'
          - 'backend3:8080'
    metrics_path: '/actuator/prometheus'

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']

  - job_name: 'node'
    static_configs:
      - targets:
          - 'node1:9100'
          - 'node2:9100'
          - 'node3:9100'
```

#### 5.1.3 告警规则

```yaml
# alerts/backend.yml
groups:
  - name: backend
    rules:
      - alert: HighCPUUsage
        expr: rate(process_cpu_seconds_total{job="room-inspection-backend"}[5m]) > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CPU使用率过高"
          description: "实例 {{ $labels.instance }} CPU使用率超过80%"

      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{job="room-inspection-backend",area="heap"} / jvm_memory_max_bytes{job="room-inspection-backend",area="heap"} > 0.9
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "堆内存使用率过高"
          description: "实例 {{ $labels.instance }} 堆内存使用率超过90%"

      - alert: HighResponseTime
        expr: rate(http_server_requests_seconds_sum{job="room-inspection-backend"}[5m]) / rate(http_server_requests_seconds_count{job="room-inspection-backend"}[5m]) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "响应时间过长"
          description: "实例 {{ $labels.instance }} 平均响应时间超过2秒"
```

### 5.2 日志管理

#### 5.2.1 日志收集（ELK Stack）

```yaml
# filebeat.yml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /var/log/room-inspection/application.log
    fields:
      app: room-inspection
      env: prod
    fields_under_root: true

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  indices:
    - index: "room-inspection-%{+yyyy.MM.dd}"

setup.kibana:
  host: "kibana:5601"
```

#### 5.2.2 日志保留策略

- 应用日志：保留30天
- 访问日志：保留7天
- 审计日志：保留180天
- 监控数据：保留90天

### 5.3 备份策略

#### 5.3.1 备份计划

| 备份类型 | 频率 | 保留周期 | 存储位置 |
|---------|------|---------|---------|
| 数据库全量 | 每天 | 30天 | 本地+异地 |
| 数据库增量 | 每小时 | 7天 | 本地 |
| 配置文件 | 每次 | 90天 | Git仓库 |
| 代码仓库 | 实时 | 永久 | Git |
| 监控数据 | 每天 | 90天 | 本地 |

#### 5.3.2 恢复演练

- 每月进行一次数据库恢复演练
- 每季度进行一次全系统灾难恢复演练
- 演练结果需记录并存档

---

## 6. 安全加固

### 6.1 系统安全

#### 6.1.1 操作系统加固

```bash
# 禁用不必要的服务
systemctl disable postfix
systemctl stop postfix

# 配置防火墙
firewall-cmd --set-default-zone=public
firewall-cmd --permanent --add-service=ssh
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
firewall-cmd --reload

# 禁用root远程登录
sed -i 's/#PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config
systemctl restart sshd

# 配置SSH密钥认证
mkdir -p ~/.ssh
chmod 700 ~/.ssh
echo "ssh-rsa AAAAB3..." >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

#### 6.1.2 应用安全

```bash
# 配置文件权限
chmod 600 /opt/room-inspection/application.yml
chown room-inspection:room-inspection /opt/room-inspection/application.yml

# 限制文件描述符
echo "room-inspection soft nofile 65536" >> /etc/security/limits.conf
echo "room-inspection hard nofile 65536" >> /etc/security/limits.conf

# 禁止JMX远程访问
JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote=false"
```

### 6.2 数据安全

#### 6.2.1 敏感数据加密

```java
// AES-GCM加密敏感配置
@Value("${database.password}")
private String dbPassword;

@PostConstruct
public void init() {
    String encryptedPassword = encryptionService.decrypt(dbPassword);
    // 使用解密后的密码
}
```

#### 6.2.2 数据传输加密

```yaml
# 强制HTTPS
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-alias: room-inspection
```

#### 6.2.3 数据库访问控制

```sql
-- 创建只读用户
CREATE USER 'readonly_user'@'%' IDENTIFIED BY 'readonly_password';
GRANT SELECT ON room_inspection.* TO 'readonly_user'@'%';

-- 创建备份用户
CREATE USER 'backup_user'@'localhost' IDENTIFIED BY 'backup_password';
GRANT SELECT, RELOAD, SHOW DATABASES, LOCK TABLES, REPLICATION CLIENT ON *.* TO 'backup_user'@'localhost';
```

### 6.3 审计与合规

#### 6.3.1 操作审计

```java
// 审计日志注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String action();
    String module();
}

// 审计切面
@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @Around("@annotation(auditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String action = auditLog.action();
        String module = auditLog.module();

        try {
            Object result = joinPoint.proceed();
            auditLogService.logSuccess(username, action, module);
            return result;
        } catch (Exception e) {
            auditLogService.logFailure(username, action, module, e.getMessage());
            throw e;
        }
    }
}
```

#### 6.3.2 合规检查

- 定期进行安全漏洞扫描
- 定期进行渗透测试
- 定期进行代码安全审计
- 遵守等保2.0三级要求

---

## 附录

### A. 快速部署脚本

```bash
#!/bin/bash
# 一键部署脚本

set -e

echo "开始部署机房巡检系统..."

# 1. 检查环境
echo "检查环境..."
java -version
mysql --version
redis-cli --version

# 2. 初始化数据库
echo "初始化数据库..."
mysql -u root -p < sql/init.sql

# 3. 部署后端
echo "部署后端..."
cp target/room-inspection.jar /opt/room-inspection/
systemctl start room-inspection

# 4. 部署前端
echo "部署前端..."
cp -r dist/* /var/www/room-inspection/
systemctl restart nginx

# 5. 验证部署
echo "验证部署..."
curl http://localhost:8080/actuator/health

echo "部署完成！"
```

### B. 故障排查指南

| 故障现象 | 可能原因 | 排查方法 |
|---------|---------|---------|
| 服务无法启动 | 端口被占用 | netstat -tulpn \| grep 8080 |
| 数据库连接失败 | 网络不通 | ping db-host |
| Redis连接超时 | 密码错误 | redis-cli -h host -a password ping |
| 监控数据不更新 | 采集任务卡死 | 查看应用日志 |
| 负载均衡失效 | 后端服务挂掉 | systemctl status room-inspection |

### C. 联系方式

- 技术支持：support@roominspection.com
- 紧急热线：400-XXX-XXXX
- 文档地址：docs.roominspection.com
