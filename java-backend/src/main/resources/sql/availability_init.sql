-- ========================================
-- 可用性与可靠性功能数据库初始化脚本
-- ========================================

-- 1. 健康检查记录表
CREATE TABLE IF NOT EXISTS `health_check` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_name` varchar(100) NOT NULL COMMENT '服务名称',
  `instance_id` varchar(100) NOT NULL COMMENT '服务实例ID',
  `service_address` varchar(100) NOT NULL COMMENT '服务地址',
  `check_type` varchar(50) NOT NULL COMMENT '检查类型（HEARTBEAT/RESOURCE/DATABASE/REDIS/MQ）',
  `status` varchar(20) NOT NULL COMMENT '检查状态（UP/DOWN/UNKNOWN）',
  `response_time` bigint(20) DEFAULT NULL COMMENT '响应时间（毫秒）',
  `error_message` text COMMENT '错误信息',
  `details` text COMMENT '检查详情（JSON格式）',
  `check_time` datetime NOT NULL COMMENT '检查时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_service_name` (`service_name`),
  KEY `idx_check_time` (`check_time`),
  KEY `idx_instance_id` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康检查记录表';

-- 2. 服务实例表
CREATE TABLE IF NOT EXISTS `service_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_name` varchar(100) NOT NULL COMMENT '服务名称',
  `instance_id` varchar(100) NOT NULL COMMENT '服务实例ID',
  `service_address` varchar(100) NOT NULL COMMENT '服务地址（IP:PORT）',
  `status` varchar(20) NOT NULL COMMENT '服务状态（UP/DOWN/STARTING/STOPPING）',
  `health_status` varchar(20) NOT NULL COMMENT '健康状态（HEALTHY/UNHEALTHY/DEGRADED）',
  `weight` int(11) NOT NULL DEFAULT 100 COMMENT '实例权重（负载均衡使用）',
  `is_primary` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为主节点',
  `last_heartbeat` datetime NOT NULL COMMENT '最后心跳时间',
  `start_time` datetime NOT NULL COMMENT '启动时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_instance_id` (`instance_id`),
  KEY `idx_service_name` (`service_name`),
  KEY `idx_status` (`status`),
  KEY `idx_health_status` (`health_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务实例表';

-- 3. 数据库备份记录表
CREATE TABLE IF NOT EXISTS `database_backup` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `backup_name` varchar(255) NOT NULL COMMENT '备份名称',
  `backup_path` varchar(500) NOT NULL COMMENT '备份文件路径',
  `file_size` bigint(20) DEFAULT NULL COMMENT '备份文件大小（字节）',
  `backup_type` varchar(50) NOT NULL COMMENT '备份类型（FULL/INCREMENTAL/DIFFERENTIAL）',
  `status` varchar(20) NOT NULL COMMENT '备份状态（SUCCESS/FAILED/IN_PROGRESS/CANCELLED）',
  `start_time` datetime NOT NULL COMMENT '备份开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '备份结束时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '备份耗时（秒）',
  `error_message` text COMMENT '错误信息',
  `description` varchar(500) DEFAULT NULL COMMENT '备份描述',
  `created_by` varchar(100) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_auto_backup` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动备份',
  PRIMARY KEY (`id`),
  KEY `idx_backup_name` (`backup_name`),
  KEY `idx_backup_type` (`backup_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库备份记录表';

-- 4. 本地缓存记录表
CREATE TABLE IF NOT EXISTS `local_cache_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cache_type` varchar(50) NOT NULL COMMENT '缓存类型（MONITOR_DATA/ALARM/CONFIG）',
  `data_source_type` varchar(50) NOT NULL COMMENT '数据源类型（DEVICE/SENSOR/ALARM）',
  `data_source_id` bigint(20) NOT NULL COMMENT '数据源ID',
  `data_content` longtext NOT NULL COMMENT '数据内容（JSON格式）',
  `data_timestamp` bigint(20) NOT NULL COMMENT '数据时间戳',
  `status` varchar(20) NOT NULL COMMENT '缓存状态（PENDING/SYNCED/FAILED）',
  `sync_fail_count` int(11) NOT NULL DEFAULT 0 COMMENT '同步失败次数',
  `last_sync_time` datetime DEFAULT NULL COMMENT '最后同步时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_cache_type` (`cache_type`),
  KEY `idx_data_source` (`data_source_type`, `data_source_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地缓存记录表';

-- 5. 系统监控指标表
CREATE TABLE IF NOT EXISTS `system_metrics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `service_name` varchar(100) NOT NULL COMMENT '服务名称',
  `instance_id` varchar(100) NOT NULL COMMENT '服务实例ID',
  `cpu_usage` double DEFAULT NULL COMMENT 'CPU使用率（%）',
  `memory_usage` double DEFAULT NULL COMMENT '内存使用率（%）',
  `disk_usage` double DEFAULT NULL COMMENT '磁盘使用率（%）',
  `jvm_heap_usage` double DEFAULT NULL COMMENT 'JVM堆内存使用率（%）',
  `thread_count` int(11) DEFAULT NULL COMMENT '线程数',
  `request_count` bigint(20) DEFAULT NULL COMMENT '请求总数',
  `error_count` bigint(20) DEFAULT NULL COMMENT '错误数',
  `avg_response_time` double DEFAULT NULL COMMENT '平均响应时间（毫秒）',
  `metrics_time` datetime NOT NULL COMMENT '指标采集时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_service_name` (`service_name`),
  KEY `idx_instance_id` (`instance_id`),
  KEY `idx_metrics_time` (`metrics_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统监控指标表';

-- 创建索引以提高查询性能
CREATE INDEX idx_health_check_composite ON health_check(service_name, check_type, check_time);
CREATE INDEX idx_system_metrics_composite ON system_metrics(service_name, instance_id, metrics_time);
CREATE INDEX idx_local_cache_composite ON local_cache_record(cache_type, status, create_time);

-- 插入默认服务实例记录
INSERT INTO service_instance (service_name, instance_id, service_address, status, health_status, weight, is_primary, last_heartbeat, start_time)
VALUES ('room-inspection-backend', 'default-instance', 'localhost:8080', 'UP', 'HEALTHY', 100, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE service_name=service_name;
