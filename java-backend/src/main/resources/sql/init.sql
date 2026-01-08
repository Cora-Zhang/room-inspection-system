-- ============================================
-- 机房巡检系统 - 数据库初始化脚本
-- 版本: v1.0.0
-- 最后更新: 2024-01-20
-- 数据库: MySQL 5.7+
-- ============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 用户与权限管理
-- ============================================

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `department_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `job_id` bigint(20) DEFAULT NULL COMMENT '职位ID',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_department` (`department_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 部门表
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `code` varchar(50) DEFAULT NULL COMMENT '部门编码',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '上级部门ID',
  `level` int(11) DEFAULT '1' COMMENT '部门层级',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 3. 职位表
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '职位ID',
  `name` varchar(50) NOT NULL COMMENT '职位名称',
  `code` varchar(50) DEFAULT NULL COMMENT '职位编码',
  `department_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_department` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 4. 角色表
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) DEFAULT NULL COMMENT '角色编码',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `data_scope` tinyint(1) DEFAULT '1' COMMENT '数据范围：1-全部，2-本部门，3-本部门及以下，4-仅本人',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 5. 用户角色关联表
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 6. 权限表
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(50) NOT NULL COMMENT '权限名称',
  `code` varchar(100) NOT NULL COMMENT '权限编码',
  `type` tinyint(1) DEFAULT '1' COMMENT '类型：1-菜单，2-按钮',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '上级权限ID',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `visible` tinyint(1) DEFAULT '1' COMMENT '是否显示：0-隐藏，1-显示',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 7. 角色权限关联表
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 机房与设备管理
-- ============================================

-- 8. 数据中心表
DROP TABLE IF EXISTS `data_center`;
CREATE TABLE `data_center` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据中心ID',
  `name` varchar(100) NOT NULL COMMENT '数据中心名称',
  `code` varchar(50) DEFAULT NULL COMMENT '数据中心编码',
  `location` varchar(200) DEFAULT NULL COMMENT '地理位置',
  `address` varchar(300) DEFAULT NULL COMMENT '详细地址',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `api_endpoint` varchar(200) DEFAULT NULL COMMENT 'API端点',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据中心表';

-- 9. 机房表
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '机房ID',
  `name` varchar(100) NOT NULL COMMENT '机房名称',
  `code` varchar(50) DEFAULT NULL COMMENT '机房编码',
  `data_center_id` bigint(20) DEFAULT NULL COMMENT '数据中心ID',
  `floor` int(11) DEFAULT NULL COMMENT '楼层',
  `location` varchar(200) DEFAULT NULL COMMENT '位置',
  `area` decimal(10,2) DEFAULT NULL COMMENT '面积（平方米）',
  `capacity` int(11) DEFAULT NULL COMMENT '设备容量',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_data_center` (`data_center_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机房表';

-- 10. 设备表
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `name` varchar(100) NOT NULL COMMENT '设备名称',
  `code` varchar(50) DEFAULT NULL COMMENT '设备编码',
  `type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `room_id` bigint(20) DEFAULT NULL COMMENT '机房ID',
  `location_id` bigint(20) DEFAULT NULL COMMENT '位置ID',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `mac` varchar(50) DEFAULT NULL COMMENT 'MAC地址',
  `manufacturer` varchar(100) DEFAULT NULL COMMENT '厂商',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `serial_number` varchar(100) DEFAULT NULL COMMENT '序列号',
  `status` varchar(20) DEFAULT 'NORMAL' COMMENT '状态：NORMAL-正常，WARNING-警告，ERROR-异常，OFFLINE-离线',
  `install_date` date DEFAULT NULL COMMENT '安装日期',
  `warranty_date` date DEFAULT NULL COMMENT '保修到期日',
  `next_maintenance_date` date DEFAULT NULL COMMENT '下次维护日期',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_room` (`room_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 11. 设备位置表
DROP TABLE IF EXISTS `device_location`;
CREATE TABLE `device_location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '位置ID',
  `room_id` bigint(20) NOT NULL COMMENT '机房ID',
  `name` varchar(100) DEFAULT NULL COMMENT '位置名称',
  `x_coordinate` decimal(10,2) DEFAULT NULL COMMENT 'X坐标',
  `y_coordinate` decimal(10,2) DEFAULT NULL COMMENT 'Y坐标',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备位置表';

-- ============================================
-- 巡检管理
-- ============================================

-- 12. 巡检模板表
DROP TABLE IF EXISTS `inspection_template`;
CREATE TABLE `inspection_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `name` varchar(100) NOT NULL COMMENT '模板名称',
  `code` varchar(50) DEFAULT NULL COMMENT '模板编码',
  `type` varchar(50) DEFAULT 'REGULAR' COMMENT '类型：REGULAR-日常，SPECIAL-专项，EMERGENCY-应急',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检模板表';

-- 13. 巡检模板项表
DROP TABLE IF EXISTS `inspection_template_item`;
CREATE TABLE `inspection_template_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板项ID',
  `template_id` bigint(20) NOT NULL COMMENT '模板ID',
  `item_name` varchar(100) NOT NULL COMMENT '项目名称',
  `item_type` varchar(20) NOT NULL COMMENT '项目类型：NUMERIC-数值，STATUS-状态，TEXT-文本，IMAGE-图片',
  `default_value` varchar(200) DEFAULT NULL COMMENT '默认值',
  `required` tinyint(1) DEFAULT '0' COMMENT '是否必填：0-否，1-是',
  `validation_rule` varchar(200) DEFAULT NULL COMMENT '验证规则',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_template` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检模板项表';

-- 14. 巡检记录表
DROP TABLE IF EXISTS `inspection`;
CREATE TABLE `inspection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '巡检ID',
  `name` varchar(100) NOT NULL COMMENT '巡检名称',
  `template_id` bigint(20) DEFAULT NULL COMMENT '模板ID',
  `room_id` bigint(20) DEFAULT NULL COMMENT '机房ID',
  `inspector_id` bigint(20) DEFAULT NULL COMMENT '巡检人ID',
  `planned_time` datetime DEFAULT NULL COMMENT '计划时间',
  `actual_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `status` varchar(20) DEFAULT 'PLANNED' COMMENT '状态：PLANNED-计划中，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消',
  `result` varchar(20) DEFAULT NULL COMMENT '结果：NORMAL-正常，WARNING-警告，ERROR-异常',
  `summary` text COMMENT '总结',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_template` (`template_id`),
  KEY `idx_room` (`room_id`),
  KEY `idx_inspector` (`inspector_id`),
  KEY `idx_status` (`status`),
  KEY `idx_planned_time` (`planned_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检记录表';

-- 15. 设备巡检记录表
DROP TABLE IF EXISTS `device_inspection_record`;
CREATE TABLE `device_inspection_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `inspection_id` bigint(20) NOT NULL COMMENT '巡检ID',
  `device_id` bigint(20) NOT NULL COMMENT '设备ID',
  `item_name` varchar(100) NOT NULL COMMENT '检查项名称',
  `item_type` varchar(20) NOT NULL COMMENT '检查项类型',
  `value` varchar(500) DEFAULT NULL COMMENT '检查值',
  `status` varchar(20) DEFAULT 'NORMAL' COMMENT '状态',
  `remarks` varchar(500) DEFAULT NULL COMMENT '备注',
  `photo_url` varchar(500) DEFAULT NULL COMMENT '照片URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_inspection` (`inspection_id`),
  KEY `idx_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备巡检记录表';

-- 16. 巡检验证表
DROP TABLE IF EXISTS `inspection_verification`;
CREATE TABLE `inspection_verification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '验证ID',
  `inspection_id` bigint(20) NOT NULL COMMENT '巡检ID',
  `type` varchar(20) NOT NULL COMMENT '验证类型：PHOTO-拍照，LOCATION-定位，SIGNATURE-签名',
  `data` text COMMENT '验证数据',
  `verified` tinyint(1) DEFAULT '0' COMMENT '是否已验证',
  `verify_time` datetime DEFAULT NULL COMMENT '验证时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_inspection` (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检验证表';

-- 17. 巡检报告表
DROP TABLE IF EXISTS `inspection_report`;
CREATE TABLE `inspection_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `inspection_id` bigint(20) NOT NULL COMMENT '巡检ID',
  `report_date` date NOT NULL COMMENT '报告日期',
  `room_count` int(11) DEFAULT '0' COMMENT '机房数量',
  `device_count` int(11) DEFAULT '0' COMMENT '设备数量',
  `normal_count` int(11) DEFAULT '0' COMMENT '正常数量',
  `warning_count` int(11) DEFAULT '0' COMMENT '警告数量',
  `error_count` int(11) DEFAULT '0' COMMENT '异常数量',
  `summary` text COMMENT '总结',
  `recommendations` text COMMENT '建议',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inspection` (`inspection_id`),
  KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='巡检报告表';

-- ============================================
-- 告警管理
-- ============================================

-- 18. 告警规则表
DROP TABLE IF EXISTS `alert_rule`;
CREATE TABLE `alert_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `name` varchar(100) NOT NULL COMMENT '规则名称',
  `metric_name` varchar(100) NOT NULL COMMENT '指标名称',
  `operator` varchar(10) NOT NULL COMMENT '操作符：>,<,=,>=,<=,!=',
  `threshold` decimal(10,2) NOT NULL COMMENT '阈值',
  `level` varchar(20) DEFAULT 'INFO' COMMENT '告警级别：INFO,WARNING,CRITICAL',
  `duration` int(11) DEFAULT '0' COMMENT '持续时间（秒）',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `channels` varchar(200) DEFAULT NULL COMMENT '通知渠道',
  `recipients` varchar(500) DEFAULT NULL COMMENT '接收人',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_metric` (`metric_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警规则表';

-- 19. 告警记录表
DROP TABLE IF EXISTS `alarm_record`;
CREATE TABLE `alarm_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '告警ID',
  `rule_id` bigint(20) DEFAULT NULL COMMENT '规则ID',
  `device_id` bigint(20) DEFAULT NULL COMMENT '设备ID',
  `metric_name` varchar(100) DEFAULT NULL COMMENT '指标名称',
  `level` varchar(20) DEFAULT 'INFO' COMMENT '告警级别',
  `content` varchar(500) DEFAULT NULL COMMENT '告警内容',
  `value` varchar(100) DEFAULT NULL COMMENT '当前值',
  `threshold` varchar(100) DEFAULT NULL COMMENT '阈值',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，PROCESSING-处理中，RESOLVED-已解决，CLOSED-已关闭',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_device` (`device_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警记录表';

-- ============================================
-- 监控采集
-- ============================================

-- 20. 监控配置表
DROP TABLE IF EXISTS `monitor_config`;
CREATE TABLE `monitor_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `device_id` bigint(20) NOT NULL COMMENT '设备ID',
  `protocol` varchar(50) NOT NULL COMMENT '协议：SNMP,Modbus,BMS等',
  `config_json` text COMMENT '配置JSON',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控配置表';

-- 21. 监控任务表
DROP TABLE IF EXISTS `monitor_task`;
CREATE TABLE `monitor_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `device_id` bigint(20) NOT NULL COMMENT '设备ID',
  `task_name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `interval` int(11) DEFAULT '60' COMMENT '采集间隔（秒）',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `last_execute_time` datetime DEFAULT NULL COMMENT '最后执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `status` varchar(20) DEFAULT 'IDLE' COMMENT '状态：IDLE-空闲，RUNNING-运行中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_device` (`device_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控任务表';

-- 22. 设备指标表
DROP TABLE IF EXISTS `device_metric`;
CREATE TABLE `device_metric` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `device_id` bigint(20) NOT NULL COMMENT '设备ID',
  `metric_name` varchar(100) NOT NULL COMMENT '指标名称',
  `metric_value` decimal(10,2) DEFAULT NULL COMMENT '指标值',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `collect_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_metric` (`device_id`, `metric_name`),
  KEY `idx_collect_time` (`collect_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备指标表';

-- 23. 采集节点表
DROP TABLE IF EXISTS `collector_node`;
CREATE TABLE `collector_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  `node_id` varchar(50) NOT NULL COMMENT '节点ID',
  `node_name` varchar(100) DEFAULT NULL COMMENT '节点名称',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `port` int(11) DEFAULT NULL COMMENT '端口',
  `status` varchar(20) DEFAULT 'ONLINE' COMMENT '状态：ONLINE,OFFLINE',
  `device_count` int(11) DEFAULT '0' COMMENT '设备数量',
  `heartbeat_time` datetime DEFAULT NULL COMMENT '心跳时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_id` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采集节点表';

-- 24. 采集任务表
DROP TABLE IF EXISTS `collection_task`;
CREATE TABLE `collection_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(100) DEFAULT NULL COMMENT '任务名称',
  `node_id` varchar(50) DEFAULT NULL COMMENT '节点ID',
  `device_ids` text COMMENT '设备ID列表',
  `interval` int(11) DEFAULT '60' COMMENT '采集间隔（秒）',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE,PAUSED,STOPPED',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_node` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采集任务表';

-- ============================================
-- 值班管理
-- ============================================

-- 25. 班次计划表
DROP TABLE IF EXISTS `shift_schedule`;
CREATE TABLE `shift_schedule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '计划ID',
  `name` varchar(100) DEFAULT NULL COMMENT '计划名称',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `shift_type` varchar(20) DEFAULT NULL COMMENT '班次类型：DAY, NIGHT, MORNING, AFTERNOON',
  `staff_ids` text COMMENT '员工ID列表',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_date` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次计划表';

-- ============================================
-- SSO与数据同步
-- ============================================

-- 26. OAuth2令牌表
DROP TABLE IF EXISTS `oauth2_token`;
CREATE TABLE `oauth2_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
  `provider` varchar(50) NOT NULL COMMENT '提供商',
  `access_token` varchar(500) DEFAULT NULL COMMENT '访问令牌',
  `refresh_token` varchar(500) DEFAULT NULL COMMENT '刷新令牌',
  `expires_in` int(11) DEFAULT NULL COMMENT '过期时间（秒）',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2令牌表';

-- 27. IAM用户表
DROP TABLE IF EXISTS `iam_user`;
CREATE TABLE `iam_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'IAM用户ID',
  `iam_user_id` varchar(100) NOT NULL COMMENT 'IAM系统用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `organization_id` varchar(100) DEFAULT NULL COMMENT '组织ID',
  `job_id` varchar(100) DEFAULT NULL COMMENT '职位ID',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_iam_user_id` (`iam_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IAM用户表';

-- 28. 组织表
DROP TABLE IF EXISTS `organization`;
CREATE TABLE `organization` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '组织ID',
  `org_id` varchar(100) NOT NULL COMMENT 'IAM组织ID',
  `name` varchar(100) NOT NULL COMMENT '组织名称',
  `code` varchar(50) DEFAULT NULL COMMENT '组织编码',
  `parent_id` varchar(100) DEFAULT NULL COMMENT '上级组织ID',
  `level` int(11) DEFAULT '1' COMMENT '层级',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_org_id` (`org_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织表';

-- ============================================
-- 审计与日志
-- ============================================

-- 29. 操作日志表
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `module` varchar(50) DEFAULT NULL COMMENT '模块',
  `operation` varchar(50) DEFAULT NULL COMMENT '操作',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `response_data` text COMMENT '响应数据',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `execute_time` int(11) DEFAULT NULL COMMENT '执行时间（毫秒）',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-失败，1-成功',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 扩展功能
-- ============================================

-- 30. 协议插件表
DROP TABLE IF EXISTS `protocol_plugin`;
CREATE TABLE `protocol_plugin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '插件ID',
  `name` varchar(100) NOT NULL COMMENT '插件名称',
  `code` varchar(50) NOT NULL COMMENT '插件编码',
  `protocol` varchar(50) NOT NULL COMMENT '支持的协议',
  `version` varchar(20) DEFAULT NULL COMMENT '版本',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `jar_path` varchar(200) DEFAULT NULL COMMENT 'JAR文件路径',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议插件表';

-- 31. API配置表
DROP TABLE IF EXISTS `api_config`;
CREATE TABLE `api_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `api_name` varchar(100) NOT NULL COMMENT 'API名称',
  `api_path` varchar(200) NOT NULL COMMENT 'API路径',
  `version` varchar(20) DEFAULT 'v1' COMMENT '版本',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `rate_limit` int(11) DEFAULT '100' COMMENT '限流（次/分钟）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api` (`api_path`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API配置表';

-- 32. 用户数据中心权限表
DROP TABLE IF EXISTS `user_data_center_permission`;
CREATE TABLE `user_data_center_permission` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `data_center_id` bigint(20) NOT NULL COMMENT '数据中心ID',
  `permission_type` varchar(20) NOT NULL COMMENT '权限类型：READ,WRITE,ADMIN',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_dc` (`user_id`, `data_center_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户数据中心权限表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员账户（密码：Admin@123，BCrypt加密）
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `status`, `create_by`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@example.com', 1, 'SYSTEM');

-- 插入默认角色
INSERT INTO `role` (`name`, `code`, `description`, `data_scope`) VALUES
('超级管理员', 'ADMIN', '拥有系统所有权限', 1),
('巡检员', 'INSPECTOR', '巡检设备，提交巡检记录', 4),
('值班员', 'DUTY', '值班管理，交接班记录', 3),
('查看员', 'VIEWER', '查看数据，无编辑权限', 2);

-- 给管理员分配角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 插入默认权限（部分核心权限）
INSERT INTO `permission` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`) VALUES
('系统管理', 'system', 1, 0, '/system', 'Setting', 100),
('用户管理', 'user', 1, (SELECT id FROM permission WHERE code='system'), '/system/user', 'User', 1),
('角色管理', 'role', 1, (SELECT id FROM permission WHERE code='system'), '/system/role', 'Shield', 2),
('巡检管理', 'inspection', 1, 0, '/inspection', 'Clipboard', 200),
('巡检计划', 'inspection-plan', 1, (SELECT id FROM permission WHERE code='inspection'), '/inspection/plan', 'Calendar', 1),
('巡检记录', 'inspection-record', 1, (SELECT id FROM permission WHERE code='inspection'), '/inspection/list', 'List', 2),
('设备管理', 'device', 1, 0, '/device', 'Server', 300),
('设备列表', 'device-list', 1, (SELECT id FROM permission WHERE code='device'), '/device/list', 'Monitor', 1),
('机房管理', 'room', 1, 0, '/room', 'Home', 400),
('机房列表', 'room-list', 1, (SELECT id FROM permission WHERE code='room'), '/room/list', 'Building', 1);

-- 插入默认部门
INSERT INTO `department` (`name`, `code`, `parent_id`, `level`, `sort`) VALUES
('总公司', 'HEADQUARTERS', 0, 1, 1),
('信息技术部', 'IT_DEPT', (SELECT id FROM department WHERE code='HEADQUARTERS'), 2, 1),
('运维部', 'OPS_DEPT', (SELECT id FROM department WHERE code='IT_DEPT'), 3, 1),
('监控部', 'MONITOR_DEPT', (SELECT id FROM department WHERE code='IT_DEPT'), 3, 2);

-- 插入默认数据中心
INSERT INTO `data_center` (`name`, `code`, `location`, `contact_person`, `contact_phone`) VALUES
('主数据中心', 'DC_MAIN', '北京市朝阳区', '张经理', '13800138000'),
('备数据中心', 'DC_BACKUP', '上海市浦东新区', '李经理', '13900139000');

-- 插入默认机房
INSERT INTO `room` (`name`, `code`, `data_center_id`, `floor`, `location`, `area`) VALUES
('A区机房', 'ROOM_A', (SELECT id FROM data_center WHERE code='DC_MAIN'), 3, '东侧', 500.00),
('B区机房', 'ROOM_B', (SELECT id FROM data_center WHERE code='DC_MAIN'), 3, '西侧', 450.00);

-- 插入默认巡检模板
INSERT INTO `inspection_template` (`name`, `code`, `type`, `description`) VALUES
('日常巡检模板', 'DAILY', 'REGULAR', '日常设备巡检标准模板'),
('专项巡检模板', 'SPECIAL', 'SPECIAL', '专项检查模板');

-- 插入默认巡检模板项
INSERT INTO `inspection_template_item` (`template_id`, `item_name`, `item_type`, `required`, `sort`) VALUES
((SELECT id FROM inspection_template WHERE code='DAILY'), '设备外观检查', 'STATUS', 1, 1),
((SELECT id FROM inspection_template WHERE code='DAILY'), '温度检查', 'NUMERIC', 1, 2),
((SELECT id FROM inspection_template WHERE code='DAILY'), '湿度检查', 'NUMERIC', 1, 3),
((SELECT id FROM inspection_template WHERE code='DAILY'), '指示灯状态', 'STATUS', 1, 4),
((SELECT id FROM inspection_template WHERE code='DAILY'), '噪音检查', 'NUMERIC', 0, 5),
((SELECT id FROM inspection_template WHERE code='DAILY'), '拍照记录', 'IMAGE', 1, 6);

-- 插入默认采集节点
INSERT INTO `collector_node` (`node_id`, `node_name`, `ip`, `port`, `status`, `device_count`) VALUES
('NODE_001', '采集节点1', '192.168.1.100', 8081, 'ONLINE', 50),
('NODE_002', '采集节点2', '192.168.1.101', 8081, 'ONLINE', 50);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成
-- ============================================
SELECT '数据库初始化完成！' AS message;
SELECT '默认管理员：admin / Admin@123' AS admin_info;
