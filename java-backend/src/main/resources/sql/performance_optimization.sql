-- ============================================================
-- 机房巡检系统 - 性能优化相关表结构
-- ============================================================

-- 创建性能指标表
CREATE TABLE IF NOT EXISTS `performance_metric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `metric_name` varchar(100) NOT NULL COMMENT '指标名称',
  `metric_type` varchar(50) NOT NULL COMMENT '指标类型：API_RESPONSE/DB_QUERY/MONITOR_COLLECT/WORK_ORDER_PROCESS/CACHE_HIT/MEMORY_USAGE/CPU_USAGE',
  `metric_value` double DEFAULT NULL COMMENT '指标值',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `object_id` varchar(200) DEFAULT NULL COMMENT '关联对象ID',
  `object_type` varchar(50) DEFAULT NULL COMMENT '关联对象类型',
  `threshold_upper` double DEFAULT NULL COMMENT '阈值上限',
  `exceeded_threshold` tinyint(1) DEFAULT '0' COMMENT '是否超出阈值（0-否 1-是）',
  `extra_info` text COMMENT '额外信息（JSON格式）',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_metric_type_record_time` (`metric_type`, `record_time`),
  KEY `idx_object_type_object_id` (`object_type`, `object_id`),
  KEY `idx_exceeded_threshold` (`exceeded_threshold`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='性能指标表';

-- 创建监控配置表
CREATE TABLE IF NOT EXISTS `monitor_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_name` varchar(100) DEFAULT NULL COMMENT '配置名称',
  `config_code` varchar(50) NOT NULL COMMENT '配置编码（唯一）',
  `device_type` varchar(50) NOT NULL COMMENT '设备类型：SERVER/SWITCH/ROUTER/FIREWALL/UPS/PDU/AIR_CONDITIONER/SENSOR/ALL',
  `collection_interval` int(11) NOT NULL DEFAULT '60' COMMENT '采集频率（秒），支持1-86400秒',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0-禁用 1-启用）',
  `concurrency_limit` int(11) DEFAULT '50' COMMENT '并发数限制',
  `timeout` int(11) DEFAULT '5000' COMMENT '超时时间（毫秒）',
  `retries` int(11) DEFAULT '3' COMMENT '重试次数',
  `protocol` varchar(20) DEFAULT 'SNMP' COMMENT '采集协议：SNMP/MODBUS/HTTP/MQTT/SCRIPT/CUSTOM',
  `protocol_config` text COMMENT '协议配置（JSON格式）',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_by` bigint(20) DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint(20) DEFAULT NULL COMMENT '更新人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_code` (`config_code`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控配置表';

-- 创建监控任务表
CREATE TABLE IF NOT EXISTS `monitor_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` varchar(50) NOT NULL COMMENT '任务ID（唯一标识）',
  `device_id` varchar(50) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `room_id` varchar(50) DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `task_type` varchar(20) NOT NULL DEFAULT 'COLLECTION' COMMENT '任务类型：COLLECTION/DISCOVERY/CHECK',
  `task_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/RUNNING/SUCCESS/FAILED/TIMEOUT',
  `start_time` datetime DEFAULT NULL COMMENT '采集开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '采集结束时间',
  `duration` bigint(20) DEFAULT NULL COMMENT '采集耗时（毫秒）',
  `data_count` int(11) DEFAULT '0' COMMENT '采集的数据条数',
  `error_message` text COMMENT '错误信息',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控任务表';

-- 优化设备监控指标表（添加索引）
-- 假设device_metric表已存在，这里只添加索引
-- ALTER TABLE `device_metric`
-- ADD INDEX `idx_device_id_collection_time` (`device_id`, `collection_time`),
-- ADD INDEX `idx_status_collection_time` (`status`, `collection_time`),
-- ADD INDEX `idx_metric_type_collection_time` (`metric_type`, `collection_time`);

-- 如果表不存在，创建完整的device_metric表
CREATE TABLE IF NOT EXISTS `device_metric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `device_code` varchar(50) DEFAULT NULL COMMENT '设备编号',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `metric_type` varchar(50) NOT NULL COMMENT '指标类型：CPU_USAGE/MEMORY_USAGE/DISK_USAGE/NETWORK_IN/NETWORK_OUT/PORT_STATUS/TEMPERATURE/HUMIDITY/POWER_USAGE/CUSTOM',
  `metric_name` varchar(100) DEFAULT NULL COMMENT '指标名称',
  `metric_value` double DEFAULT NULL COMMENT '指标值（数值型）',
  `unit` varchar(20) DEFAULT NULL COMMENT '指标单位',
  `status` varchar(20) DEFAULT 'NORMAL' COMMENT '指标状态：NORMAL/WARNING/CRITICAL',
  `exceeded_threshold` tinyint(1) DEFAULT '0' COMMENT '是否超出阈值（0-否 1-是）',
  `port_name` varchar(50) DEFAULT NULL COMMENT '端口名称',
  `port_index` int(11) DEFAULT NULL COMMENT '端口索引',
  `port_status` varchar(20) DEFAULT NULL COMMENT '端口状态：UP/DOWN/TESTING/UNKNOWN',
  `disk_name` varchar(50) DEFAULT NULL COMMENT '磁盘名称',
  `disk_path` varchar(200) DEFAULT NULL COMMENT '磁盘路径',
  `disk_total` double DEFAULT NULL COMMENT '磁盘总量（GB）',
  `disk_used` double DEFAULT NULL COMMENT '磁盘已用量（GB）',
  `disk_free` double DEFAULT NULL COMMENT '磁盘可用量（GB）',
  `threshold_upper` double DEFAULT NULL COMMENT '阈值上限',
  `threshold_lower` double DEFAULT NULL COMMENT '阈值下限',
  `custom_key` varchar(100) DEFAULT NULL COMMENT '自定义指标键',
  `custom_value` text COMMENT '自定义指标值（JSON格式）',
  `collection_method` varchar(20) DEFAULT 'SNMP' COMMENT '采集方式：SNMP/MODBUS/SCRIPT/MANUAL',
  `collection_time` datetime NOT NULL COMMENT '采集时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id_collection_time` (`device_id`, `collection_time`),
  KEY `idx_status_collection_time` (`status`, `collection_time`),
  KEY `idx_metric_type_collection_time` (`metric_type`, `collection_time`),
  KEY `idx_device_type` (`metric_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备监控指标表';

-- ============================================================
-- 插入示例监控配置数据
-- ============================================================

-- 服务器监控配置（高频采集，10秒）
INSERT INTO `monitor_config` (`config_name`, `config_code`, `device_type`, `collection_interval`, `status`, `concurrency_limit`, `timeout`, `retries`, `protocol`, `description`, `created_by`, `created_by_name`) VALUES
('服务器监控配置', 'SERVER_CONFIG', 'SERVER', 10, 1, 50, 5000, 3, 'SNMP', '服务器设备监控配置，采集频率10秒', 1, '系统管理员'),
('交换机监控配置', 'SWITCH_CONFIG', 'SWITCH', 30, 1, 50, 5000, 3, 'SNMP', '交换机设备监控配置，采集频率30秒', 1, '系统管理员'),
('路由器监控配置', 'ROUTER_CONFIG', 'ROUTER', 60, 1, 50, 5000, 3, 'SNMP', '路由器设备监控配置，采集频率60秒', 1, '系统管理员'),
('防火墙监控配置', 'FIREWALL_CONFIG', 'FIREWALL', 30, 1, 20, 5000, 3, 'SNMP', '防火墙设备监控配置，采集频率30秒', 1, '系统管理员'),
('UPS电源监控配置', 'UPS_CONFIG', 'UPS', 60, 1, 30, 5000, 3, 'SNMP', 'UPS电源监控配置，采集频率60秒', 1, '系统管理员'),
('PDU配电监控配置', 'PDU_CONFIG', 'PDU', 60, 1, 50, 5000, 3, 'SNMP', 'PDU配电监控配置，采集频率60秒', 1, '系统管理员'),
('精密空调监控配置', 'AIR_CONDITIONER_CONFIG', 'AIR_CONDITIONER', 60, 1, 20, 5000, 3, 'SNMP', '精密空调监控配置，采集频率60秒', 1, '系统管理员'),
('传感器监控配置', 'SENSOR_CONFIG', 'SENSOR', 5, 1, 100, 5000, 3, 'MODBUS', '传感器监控配置，采集频率5秒', 1, '系统管理员');
