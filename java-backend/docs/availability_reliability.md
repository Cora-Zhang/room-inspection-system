# 机房巡检系统 - 可用性与可靠性功能

## 功能概述

本系统实现了完整的可用性与可靠性功能，确保系统7×24小时不间断运行，关键服务支持冗余部署与自动故障切换，数据存储支持备份与恢复机制，监控服务故障时支持本地缓存与断点续传。

## 核心功能

### 1. 健康检查机制

**功能说明：**
- 自动定时健康检查（每30秒）
- 支持多种检查类型：数据库、Redis、JVM、磁盘空间
- 记录健康检查历史和响应时间
- 支持健康状态统计和趋势分析

**实现类：**
- `HealthCheckConfig.java` - 健康检查配置类
- `HealthCheckService.java` - 健康检查服务
- `HealthCheck.java` - 健康检查记录实体
- `ServiceInstance.java` - 服务实例实体

**API接口：**
- `GET /api/availability/health-checks` - 获取健康检查记录
- `GET /api/availability/health-stats` - 获取健康状态统计
- `GET /api/availability/healthy-instances` - 获取健康服务实例
- `GET /api/availability/health` - 健康检查端点（负载均衡探活）
- `GET /api/availability/ready` - 就绪检查端点（K8s容器编排）

### 2. 系统监控指标

**功能说明：**
- 实时采集系统监控指标（CPU、内存、磁盘、JVM）
- 支持监控指标趋势分析
- 自动清理过期监控数据
- 系统健康状态判断

**实现类：**
- `SystemMetricsService.java` - 系统监控指标服务
- `SystemMetrics.java` - 系统监控指标实体
- `HealthCheckService.java` - 包含指标采集逻辑

**API接口：**
- `GET /api/availability/system-health` - 获取系统健康状态
- `GET /api/availability/metrics-summary` - 获取监控指标统计
- `GET /api/availability/cpu-trend` - 获取CPU使用率趋势
- `GET /api/availability/memory-trend` - 获取内存使用率趋势

### 3. 数据库备份与恢复

**功能说明：**
- 支持全量、增量、差异备份
- 自动定时备份（每天凌晨2点）
- 支持手动触发备份
- 备份记录管理和查询
- 数据库恢复功能

**实现类：**
- `DatabaseBackupService.java` - 数据库备份服务
- `DatabaseBackup.java` - 数据库备份记录实体

**API接口：**
- `POST /api/availability/backup` - 手动执行数据库备份
- `GET /api/availability/backups` - 获取备份记录列表
- `POST /api/availability/restore` - 恢复数据库
- `DELETE /api/availability/backup/{backupId}` - 删除备份

**配置参数：**
```yaml
backup:
  path: /tmp/backups                          # 备份文件存储路径
  auto-backup: true                           # 是否启用自动备份
  cron: 0 0 2 * * ?                          # 自动备份时间（Cron表达式）
  retain-days: 30                             # 备份保留天数
  default-type: FULL                         # 默认备份类型
```

### 4. 本地缓存与断点续传

**功能说明：**
- 监控服务故障时本地缓存数据
- 服务恢复后自动同步到Redis
- 支持重试机制（最多5次）
- 自动清理过期缓存数据

**实现类：**
- `LocalCacheService.java` - 本地缓存服务
- `LocalCacheRecord.java` - 本地缓存记录实体

**API接口：**
- `GET /api/availability/cache-stats` - 获取缓存统计信息
- `POST /api/availability/cache/sync` - 手动触发缓存同步
- `DELETE /api/availability/cache/{cacheType}` - 清除指定类型的缓存

**配置参数：**
```yaml
local-cache:
  enabled: true                               # 是否启用本地缓存
  retain-days: 30                            # 缓存数据保留天数
  sync-interval: 60                           # 同步间隔（秒）
  max-retry-count: 5                          # 最大重试次数
  retry-interval: 600                         # 同步失败重试间隔（秒）
```

### 5. 高可用配置

**功能说明：**
- 支持多实例部署
- 分布式锁机制（Redis实现）
- 负载均衡策略（轮询、加权轮询、随机）
- 线程池配置（异步任务、监控采集、工单处理、备份任务）
- 自动故障切换

**实现类：**
- `HighAvailabilityConfig.java` - 高可用配置类
- 包含分布式锁实现、负载均衡策略

**配置参数：**
```yaml
ha:
  instance:
    weight: 100                               # 实例权重
    primary: false                            # 是否为主节点

  load-balance:
    strategy: ROUND_ROBIN                     # 负载均衡策略
    health-check-interval: 30                 # 健康检查间隔（秒）
    failover-threshold: 3                     # 故障切换阈值

  failover:
    enabled: true                             # 是否启用自动故障切换
    check-interval: 10                        # 故障检测间隔（秒）
    timeout: 30                               # 故障切换超时（秒）
```

## 线程池配置

系统配置了多个专用线程池，确保不同类型任务的并发处理：

```yaml
# 异步任务线程池
async-task:
  thread-pool:
    core-size: 10
    max-size: 20
    queue-capacity: 200

# 监控数据采集线程池（支持200+设备并发）
monitor-task:
  thread-pool:
    core-size: 20
    max-size: 50
    queue-capacity: 500

# 工单处理线程池（响应时间≤3秒）
workorder-task:
  thread-pool:
    core-size: 10
    max-size: 20
    queue-capacity: 300

# 备份任务线程池
backup-task:
  thread-pool:
    core-size: 2
    max-size: 5
    queue-capacity: 10

# 健康检查线程池
healthcheck-task:
  thread-pool:
    core-size: 5
    max-size: 10
    queue-capacity: 50
```

## Spring Boot Actuator集成

系统集成了Spring Boot Actuator，提供完整的监控端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: '*'                          # 暴露所有监控端点
  endpoint:
    health:
      show-details: always                    # 显示详细健康信息
```

**常用监控端点：**
- `/actuator/health` - 健康检查
- `/actuator/metrics` - 监控指标
- `/actuator/info` - 应用信息
- `/actuator/env` - 环境信息
- `/actuator/loggers` - 日志管理

## 定时任务

系统通过Spring Scheduling实现了多个定时任务：

| 任务名称 | 执行频率 | 功能说明 |
|---------|---------|---------|
| 健康检查 | 每30秒 | 执行数据库、Redis、JVM、磁盘健康检查 |
| 系统指标采集 | 每60秒 | 采集CPU、内存、磁盘、JVM指标 |
| 自动备份 | 每天凌晨2点 | 自动执行数据库备份 |
| 缓存同步 | 每60秒 | 同步本地缓存到Redis |
| 失败重试 | 每10分钟 | 重试失败的同步记录 |
| 清理过期数据 | 每天凌晨3点 | 清理过期监控指标和缓存记录 |

## 数据库表结构

### health_check（健康检查记录表）
- 记录所有健康检查历史
- 支持按服务、检查类型、时间范围查询
- 包含响应时间和错误信息

### service_instance（服务实例表）
- 记录所有服务实例信息
- 支持负载均衡权重配置
- 记录实例状态和健康状态

### database_backup（数据库备份记录表）
- 记录所有备份操作
- 支持备份类型、状态查询
- 包含备份文件路径和大小

### local_cache_record（本地缓存记录表）
- 记录本地缓存数据
- 支持待同步、同步失败查询
- 记录同步失败次数

### system_metrics（系统监控指标表）
- 记录系统监控指标
- 支持按服务、实例、时间范围查询
- 包含CPU、内存、磁盘、JVM指标

## 使用示例

### 1. 手动执行数据库备份

```bash
curl -X POST "http://localhost:8080/api/availability/backup?backupType=FULL"
```

### 2. 查询系统健康状态

```bash
curl "http://localhost:8080/api/availability/system-health"
```

### 3. 获取健康检查记录

```bash
curl "http://localhost:8080/api/availability/health-checks?serviceName=room-inspection-backend&hours=24"
```

### 4. 触发缓存同步

```bash
curl -X POST "http://localhost:8080/api/availability/cache/sync"
```

### 5. 查看系统可用性概览

```bash
curl "http://localhost:8080/api/availability/overview"
```

## 性能指标

根据需求，系统实现了以下性能指标：

- **监控数据采集频率**：可配置（1-86400秒），支持秒级更新
- **并发监控设备数**：200+设备（使用专用线程池）
- **工单处理响应时间**：≤3秒（异步处理+线程池）
- **页面响应时间**：≤2秒（前端虚拟滚动、路由懒加载）
- **告警推送延迟**：<100ms（WebSocket实时推送）
- **WebSocket实时推送**：告警与监控数据

## 部署建议

### 单实例部署

适用于小型机房，无需配置负载均衡：
- 启用自动健康检查
- 启用自动数据库备份
- 启用本地缓存机制

### 多实例部署（高可用）

适用于中型机房，建议配置负载均衡：
- 配置Nginx负载均衡器
- 启用自动故障切换
- 配置Redis集群
- 配置MySQL主从复制

### 集群部署（企业级）

适用于大型机房或数据中心：
- 配置Nginx/HAProxy负载均衡器
- 配置Redis Sentinel/Cluster
- 配置MySQL Galera Cluster
- 配置Kubernetes容器编排
- 配置Prometheus+Grafana监控

## 故障恢复流程

### 1. 数据库故障
- 本地缓存继续记录监控数据
- 数据库恢复后自动同步
- 最多重试5次，失败后标记为FAILED

### 2. Redis故障
- 数据保存到数据库
- Redis恢复后自动同步
- 不影响核心业务功能

### 3. 服务实例故障
- 负载均衡器自动切换到健康实例
- 故障实例恢复后自动加入
- 健康检查确保实例可用性

### 4. 网络分区
- 本地缓存记录数据
- 网络恢复后自动同步
- 分布式锁防止数据冲突

## 监控告警

系统支持多种监控告警方式：

1. **系统健康告警**：CPU/内存/磁盘/JVM使用率超过阈值
2. **服务状态告警**：服务实例DOWN/UNHEALTHY
3. **备份失败告警**：数据库备份失败
4. **同步失败告警**：本地缓存同步失败

告警通知渠道：
- 钉钉机器人
- 短信通知
- 邮件通知
- WebSocket实时推送

## 最佳实践

1. **定期备份数据**：建议每天自动备份，保留30天
2. **监控关键指标**：重点关注CPU、内存、磁盘使用率
3. **及时处理告警**：监控告警应及时响应处理
4. **定期清理数据**：自动清理过期监控数据，避免占用过多空间
5. **负载测试**：定期进行压力测试，验证系统性能
6. **灾难恢复演练**：定期演练数据恢复流程，确保备份可用性

## 常见问题

### Q1: 如何调整健康检查频率？

修改`application.yml`中的配置：
```yaml
health-check:
  interval: 30  # 改为其他值（秒）
```

### Q2: 如何增加并发监控设备数？

修改`monitor-task`线程池配置：
```yaml
monitor-task:
  thread-pool:
    core-size: 20  # 增加核心线程数
    max-size: 50   # 增加最大线程数
```

### Q3: 如何修改自动备份时间？

修改Cron表达式：
```yaml
backup:
  cron: 0 0 2 * * ?  # 改为其他时间
```

### Q4: 如何配置负载均衡？

使用Nginx配置示例：
```nginx
upstream room_inspection {
    server 192.168.1.10:8080 weight=100;
    server 192.168.1.11:8080 weight=100;
    server 192.168.1.12:8080 weight=100 backup;
}

server {
    listen 80;
    location / {
        proxy_pass http://room_inspection;
        proxy_set_header Host $host;
        health_check interval=30 fails=3;
    }
}
```

### Q5: 如何清理过期数据？

系统会自动清理，也可以手动执行：
```sql
-- 清理30天前的健康检查记录
DELETE FROM health_check WHERE create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 清理7天前的监控指标
DELETE FROM system_metrics WHERE create_time < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

## 总结

本系统的可用性与可靠性功能通过以下机制确保系统稳定运行：

1. **7×24小时不间断运行**：自动健康检查、故障切换、负载均衡
2. **关键服务冗余部署**：多实例支持、自动故障切换、负载均衡
3. **数据备份与恢复**：自动备份、手动备份、数据恢复功能
4. **本地缓存与断点续传**：故障时本地缓存、恢复后自动同步

通过这些机制，系统能够在高并发、故障场景下保持稳定运行，确保机房巡检工作的连续性和可靠性。
