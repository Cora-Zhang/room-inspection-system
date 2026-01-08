-- ============================================
-- 综合巡检看板和工单管理模块 - 数据库Schema
-- ============================================

USE `room_inspection`;

-- ============================================
-- 机房平面图模块
-- ============================================

-- 机房平面图表
CREATE TABLE IF NOT EXISTS `room_floor_plans` (
  `id` varchar(36) NOT NULL COMMENT '平面图ID',
  `room_id` varchar(36) NOT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `floor` varchar(50) DEFAULT NULL COMMENT '楼层',
  `name` varchar(100) NOT NULL COMMENT '平面图名称',
  `image_url` varchar(500) DEFAULT NULL COMMENT '平面图URL',
  `plan_data` text COMMENT '平面图数据（JSON格式）',
  `thumbnail_url` varchar(500) DEFAULT NULL COMMENT '缩略图URL',
  `is_main` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否主图',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `created_by` varchar(36) DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_is_main` (`is_main`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机房平面图表';

-- 设备位置表
CREATE TABLE IF NOT EXISTS `device_locations` (
  `id` varchar(36) NOT NULL COMMENT '位置ID',
  `device_id` varchar(36) NOT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `room_id` varchar(36) NOT NULL COMMENT '机房ID',
  `floor_plan_id` varchar(36) DEFAULT NULL COMMENT '平面图ID',
  `zone` varchar(50) DEFAULT NULL COMMENT '区域编号',
  `cabinet` varchar(50) DEFAULT NULL COMMENT '机柜编号',
  `u_position` varchar(50) DEFAULT NULL COMMENT 'U位',
  `x_coordinate` double DEFAULT NULL COMMENT 'X坐标',
  `y_coordinate` double DEFAULT NULL COMMENT 'Y坐标',
  `status` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '设备状态',
  `last_alarm_time` datetime DEFAULT NULL COMMENT '最后告警时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注信息',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_floor_plan_id` (`floor_plan_id`),
  KEY `idx_zone` (`zone`),
  KEY `idx_status` (`status`),
  KEY `idx_device_type` (`device_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备位置表';

-- ============================================
-- 告警管理模块
-- ============================================

-- 告警记录表
CREATE TABLE IF NOT EXISTS `alarm_records` (
  `id` varchar(36) NOT NULL COMMENT '告警ID',
  `alarm_code` varchar(50) NOT NULL COMMENT '告警编号',
  `level` varchar(20) NOT NULL COMMENT '告警级别',
  `type` varchar(20) NOT NULL COMMENT '告警类型',
  `device_id` varchar(36) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `title` varchar(200) NOT NULL COMMENT '告警标题',
  `content` text COMMENT '告警内容',
  `alarm_value` varchar(100) DEFAULT NULL COMMENT '告警值',
  `threshold` varchar(100) DEFAULT NULL COMMENT '阈值',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '告警状态',
  `source` varchar(20) NOT NULL DEFAULT 'SYSTEM' COMMENT '告警来源',
  `alarm_time` datetime NOT NULL COMMENT '发生时间',
  `acknowledged_by` varchar(36) DEFAULT NULL COMMENT '确认人ID',
  `acknowledged_at` datetime DEFAULT NULL COMMENT '确认时间',
  `handled_by` varchar(36) DEFAULT NULL COMMENT '处理人ID',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `work_order_id` varchar(36) DEFAULT NULL COMMENT '关联工单ID',
  `sms_sent` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否发送短信',
  `ding_talk_sent` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否发送钉钉',
  `email_sent` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否发送邮件',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '重试次数',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_alarm_code` (`alarm_code`),
  KEY `idx_level` (`level`),
  KEY `idx_type` (`type`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_status` (`status`),
  KEY `idx_alarm_time` (`alarm_time`),
  KEY `idx_work_order_id` (`work_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

-- ============================================
-- 工单管理模块
-- ============================================

-- 工单表
CREATE TABLE IF NOT EXISTS `work_orders` (
  `id` varchar(36) NOT NULL COMMENT '工单ID',
  `order_code` varchar(50) NOT NULL COMMENT '工单编号',
  `type` varchar(50) NOT NULL COMMENT '工单类型',
  `title` varchar(200) NOT NULL COMMENT '工单标题',
  `description` text COMMENT '工单描述',
  `priority` varchar(20) NOT NULL DEFAULT 'MEDIUM' COMMENT '优先级',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '工单状态',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `device_id` varchar(36) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `created_by` varchar(36) NOT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime NOT NULL COMMENT '创建时间',
  `assigned_to` varchar(36) DEFAULT NULL COMMENT '指派人ID',
  `assigned_to_name` varchar(50) DEFAULT NULL COMMENT '指派人姓名',
  `assigned_at` datetime DEFAULT NULL COMMENT '指派时间',
  `owner_id` varchar(36) DEFAULT NULL COMMENT '负责人ID',
  `owner_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名',
  `expected_start_time` datetime DEFAULT NULL COMMENT '预期开始时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `expected_end_time` datetime DEFAULT NULL COMMENT '预期完成时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际完成时间',
  `duration` double DEFAULT NULL COMMENT '工作时长（小时）',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `handle_description` text COMMENT '处理说明',
  `attachments` text COMMENT '附件信息（JSON格式）',
  `alarm_id` varchar(36) DEFAULT NULL COMMENT '关联告警ID',
  `is_overdue` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否超时',
  `overdue_minutes` int DEFAULT NULL COMMENT '超时时长（分钟）',
  `quality_score` int DEFAULT NULL COMMENT '质量评分',
  `quality_comment` varchar(500) DEFAULT NULL COMMENT '评分说明',
  `closed_by` varchar(36) DEFAULT NULL COMMENT '关闭人ID',
  `closed_by_name` varchar(50) DEFAULT NULL COMMENT '关闭人姓名',
  `closed_at` datetime DEFAULT NULL COMMENT '关闭时间',
  `close_reason` varchar(500) DEFAULT NULL COMMENT '关闭原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_code` (`order_code`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_assigned_to` (`assigned_to`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_is_overdue` (`is_overdue`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';

-- 工单流转记录表
CREATE TABLE IF NOT EXISTS `work_order_flows` (
  `id` varchar(36) NOT NULL COMMENT '流转ID',
  `work_order_id` varchar(36) NOT NULL COMMENT '工单ID',
  `order_code` varchar(50) NOT NULL COMMENT '工单编号',
  `action_type` varchar(50) NOT NULL COMMENT '操作类型',
  `from_status` varchar(20) DEFAULT NULL COMMENT '操作前状态',
  `to_status` varchar(20) DEFAULT NULL COMMENT '操作后状态',
  `operator_id` varchar(36) NOT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `content` varchar(500) DEFAULT NULL COMMENT '操作内容',
  `comment` text COMMENT '操作意见',
  `attachments` text COMMENT '关联附件（JSON格式）',
  `operated_at` datetime NOT NULL COMMENT '操作时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_work_order_id` (`work_order_id`),
  KEY `idx_action_type` (`action_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operated_at` (`operated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单流转记录表';

-- ============================================
-- 报表统计模块
-- ============================================

-- 巡检报表表
CREATE TABLE IF NOT EXISTS `inspection_reports` (
  `id` varchar(36) NOT NULL COMMENT '报表ID',
  `report_code` varchar(50) NOT NULL COMMENT '报表编号',
  `report_type` varchar(20) NOT NULL COMMENT '报表类型',
  `report_date` date NOT NULL COMMENT '统计日期',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `planned_inspections` int NOT NULL DEFAULT '0' COMMENT '计划巡检次数',
  `actual_inspections` int NOT NULL DEFAULT '0' COMMENT '实际巡检次数',
  `completion_rate` double DEFAULT NULL COMMENT '完成率（%）',
  `normal_inspections` int NOT NULL DEFAULT '0' COMMENT '正常巡检次数',
  `abnormal_inspections` int NOT NULL DEFAULT '0' COMMENT '异常巡检次数',
  `issue_count` int NOT NULL DEFAULT '0' COMMENT '发现问题数量',
  `resolved_issue_count` int NOT NULL DEFAULT '0' COMMENT '已处理问题数量',
  `issue_resolution_rate` double DEFAULT NULL COMMENT '问题处理率（%）',
  `work_order_count` int NOT NULL DEFAULT '0' COMMENT '生成工单数量',
  `completed_work_order_count` int NOT NULL DEFAULT '0' COMMENT '已完成工单数量',
  `work_order_completion_rate` double DEFAULT NULL COMMENT '工单完成率（%）',
  `avg_inspection_duration` double DEFAULT NULL COMMENT '平均巡检时长（分钟）',
  `min_inspection_duration` double DEFAULT NULL COMMENT '最短巡检时长（分钟）',
  `max_inspection_duration` double DEFAULT NULL COMMENT '最长巡检时长（分钟）',
  `report_data` text COMMENT '报表数据（JSON格式）',
  `generated_by` varchar(36) DEFAULT NULL COMMENT '报表人ID',
  `generated_by_name` varchar(50) DEFAULT NULL COMMENT '报表人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_code` (`report_code`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_report_date` (`report_date`),
  KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='巡检报表表';

-- 设备性能报表表
CREATE TABLE IF NOT EXISTS `device_performance_reports` (
  `id` varchar(36) NOT NULL COMMENT '报表ID',
  `report_code` varchar(50) NOT NULL COMMENT '报表编号',
  `report_type` varchar(20) NOT NULL COMMENT '报表类型',
  `report_date` date NOT NULL COMMENT '统计日期',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `device_id` varchar(36) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `total_devices` int NOT NULL DEFAULT '0' COMMENT '设备总数',
  `normal_devices` int NOT NULL DEFAULT '0' COMMENT '正常设备数量',
  `warning_devices` int NOT NULL DEFAULT '0' COMMENT '告警设备数量',
  `error_devices` int NOT NULL DEFAULT '0' COMMENT '故障设备数量',
  `offline_devices` int NOT NULL DEFAULT '0' COMMENT '离线设备数量',
  `availability_rate` double DEFAULT NULL COMMENT '设备可用率（%）',
  `running_hours` double NOT NULL DEFAULT '0' COMMENT '运行时长（小时）',
  `downtime_hours` double NOT NULL DEFAULT '0' COMMENT '故障时长（小时）',
  `alarm_count` int NOT NULL DEFAULT '0' COMMENT '告警次数',
  `critical_alarm_count` int NOT NULL DEFAULT '0' COMMENT '紧急告警次数',
  `major_alarm_count` int NOT NULL DEFAULT '0' COMMENT '重要告警次数',
  `minor_alarm_count` int NOT NULL DEFAULT '0' COMMENT '一般告警次数',
  `mtbf` double DEFAULT NULL COMMENT '平均无故障时间（MTBF，小时）',
  `mttr` double DEFAULT NULL COMMENT '平均修复时间（MTTR，小时）',
  `maintenance_count` int NOT NULL DEFAULT '0' COMMENT '维护次数',
  `preventive_maintenance_count` int NOT NULL DEFAULT '0' COMMENT '保养次数',
  `performance_metrics` text COMMENT '性能指标（JSON格式）',
  `report_data` text COMMENT '报表数据（JSON格式）',
  `generated_by` varchar(36) DEFAULT NULL COMMENT '报表人ID',
  `generated_by_name` varchar(50) DEFAULT NULL COMMENT '报表人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_report_code` (`report_code`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_report_date` (`report_date`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_device_type` (`device_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备性能报表表';

-- ============================================
-- 插入示例数据
-- ============================================

-- 示例机房平面图
INSERT INTO `room_floor_plans` (`id`, `room_id`, `room_name`, `floor`, `name`, `image_url`, `plan_data`, `is_main`, `sort_order`, `status`, `created_by`) VALUES
('FP001', 'R001', '数据中心机房A', '2楼', '机房A平面图', '/uploads/floor-plans/room-a-plan.png', '{"zones": [{"id": "Z001", "name": "服务器区", "x": 100, "y": 100, "width": 400, "height": 300}, {"id": "Z002", "name": "网络区", "x": 520, "y": 100, "width": 280, "height": 300}]}', 1, 1, 'ACTIVE', 'U001'),
('FP002', 'R001', '数据中心机房A', '2楼', '机房A详细平面图', '/uploads/floor-plans/room-a-detail.png', NULL, 0, 2, 'ACTIVE', 'U001');

-- 示例设备位置
INSERT INTO `device_locations` (`id`, `device_id`, `device_name`, `device_type`, `room_id`, `floor_plan_id`, `zone`, `cabinet`, `u_position`, `x_coordinate`, `y_coordinate`, `status`) VALUES
('DL001', 'D001', '服务器001', 'SERVER', 'R001', 'FP001', 'Z001', 'C01', 'U01-U04', 150.0, 150.0, 'NORMAL'),
('DL002', 'D002', '服务器002', 'SERVER', 'R001', 'FP001', 'Z001', 'C02', 'U01-U04', 200.0, 150.0, 'NORMAL'),
('DL003', 'D003', '交换机001', 'SWITCH', 'R001', 'FP001', 'Z002', 'S01', NULL, 560.0, 150.0, 'NORMAL'),
('DL004', 'D004', '空调001', 'AIR_CONDITIONER', 'R001', 'FP001', 'Z001', NULL, NULL, 300.0, 280.0, 'WARNING');

-- 示例告警记录
INSERT INTO `alarm_records` (`id`, `alarm_code`, `level`, `type`, `device_id`, `device_name`, `room_id`, `title`, `content`, `alarm_value`, `threshold`, `status`, `source`, `alarm_time`) VALUES
('ALM001', 'ALM-20250120-001', 'CRITICAL', 'DEVICE', 'D001', '服务器001', 'R001', '服务器CPU温度过高', '服务器001 CPU温度达到85℃，超过阈值75℃', '85', '75', 'ACTIVE', 'SYSTEM', '2025-01-20 10:30:00'),
('ALM002', 'ALM-20250120-002', 'MAJOR', 'ENVIRONMENT', 'D004', '空调001', 'R001', '空调回风温度异常', '空调001回风温度为26℃，设定温度22℃，温差4℃超过阈值', '26', '22', 'ACTIVE', 'SYSTEM', '2025-01-20 10:25:00'),
('ALM003', 'ALM-20250120-003', 'MINOR', 'DEVICE', 'D003', '交换机001', 'R001', '交换机端口流量异常', '交换机001 端口GigabitEthernet0/1流量达到95%，超过阈值90%', '95', '90', 'ACKNOWLEDGED', 'SYSTEM', '2025-01-20 09:15:00');

-- 示例工单
INSERT INTO `work_orders` (`id`, `order_code`, `type`, `title`, `description`, `priority`, `status`, `room_id`, `room_name`, `device_id`, `device_name`, `created_by`, `created_by_name`, `created_at`, `assigned_to`, `assigned_to_name`, `assigned_at`, `owner_id`, `owner_name`, `expected_start_time`, `expected_end_time`) VALUES
('WO001', 'WO-20250120-001', 'MAINTENANCE', '服务器温度告警处理', '服务器001 CPU温度过高，需要检查散热系统', 'URGENT', 'ASSIGNED', 'R001', '数据中心机房A', 'D001', '服务器001', 'U001', '张三', '2025-01-20 10:30:00', 'U002', '李四', '2025-01-20 10:35:00', 'U002', '李四', '2025-01-20 10:40:00', '2025-01-20 12:00:00'),
('WO002', 'WO-20250120-002', 'MAINTENANCE', '空调温差异常检查', '空调001回风温度异常，需要检查空调运行状态和设定温度', 'HIGH', 'IN_PROGRESS', 'R001', '数据中心机房A', 'D004', '空调001', 'U001', '张三', '2025-01-20 10:25:00', 'U003', '王五', '2025-01-20 10:30:00', 'U003', '王五', '2025-01-20 10:32:00', '2025-01-20 11:00:00'),
('WO003', 'WO-20250120-003', 'MAINTENANCE_PREVENTIVE', '网络设备例行巡检', '交换机001例行巡检任务', 'MEDIUM', 'PENDING', 'R001', '数据中心机房A', 'D003', '交换机001', 'U001', '张三', '2025-01-20 08:00:00', NULL, NULL, NULL, NULL, NULL, '2025-01-20 14:00:00', '2025-01-20 16:00:00');

-- 示例工单流转记录
INSERT INTO `work_order_flows` (`id`, `work_order_id`, `order_code`, `action_type`, `from_status`, `to_status`, `operator_id`, `operator_name`, `content`, `operated_at`) VALUES
('WOF001', 'WO001', 'WO-20250120-001', 'CREATE', NULL, 'PENDING', 'U001', '张三', '创建工单', '2025-01-20 10:30:00'),
('WOF002', 'WO001', 'WO-20250120-001', 'ASSIGN', 'PENDING', 'ASSIGNED', 'U001', '张三', '工单已指派给李四', '2025-01-20 10:35:00'),
('WOF003', 'WO002', 'WO-20250120-002', 'CREATE', NULL, 'PENDING', 'U001', '张三', '创建工单', '2025-01-20 10:25:00'),
('WOF004', 'WO002', 'WO-20250120-002', 'ASSIGN', 'PENDING', 'ASSIGNED', 'U001', '张三', '工单已指派给王五', '2025-01-20 10:30:00'),
('WOF005', 'WO002', 'WO-20250120-002', 'START', 'ASSIGNED', 'IN_PROGRESS', 'U003', '王五', '开始处理工单', '2025-01-20 10:32:00');

-- 示例巡检报表
INSERT INTO `inspection_reports` (`id`, `report_code`, `report_type`, `report_date`, `start_date`, `end_date`, `room_id`, `room_name`, `planned_inspections`, `actual_inspections`, `completion_rate`, `normal_inspections`, `abnormal_inspections`, `issue_count`, `resolved_issue_count`, `issue_resolution_rate`, `work_order_count`, `completed_work_order_count`, `work_order_completion_rate`, `avg_inspection_duration`, `generated_by`, `generated_by_name`) VALUES
('IR001', 'IR-D-20250119', 'DAILY', '2025-01-19', '2025-01-19', '2025-01-19', 'R001', '数据中心机房A', 4, 4, 100.0, 3, 1, 1, 1, 100.0, 1, 1, 100.0, 45.5, 'U001', '张三'),
('IR002', 'IR-W-20250119', 'WEEKLY', '2025-01-19', '2025-01-13', '2025-01-19', 'R001', '数据中心机房A', 28, 27, 96.4, 24, 3, 3, 3, 100.0, 3, 3, 100.0, 42.8, 'U001', '张三');

-- 示例设备性能报表
INSERT INTO `device_performance_reports` (`id`, `report_code`, `report_type`, `report_date`, `start_date`, `end_date`, `room_id`, `room_name`, `total_devices`, `normal_devices`, `warning_devices`, `error_devices`, `offline_devices`, `availability_rate`, `running_hours`, `downtime_hours`, `alarm_count`, `critical_alarm_count`, `major_alarm_count`, `minor_alarm_count`, `mtbf`, `mttr`, `maintenance_count`, `preventive_maintenance_count`, `generated_by`, `generated_by_name`) VALUES
('DPR001', 'DPR-D-20250119', 'DAILY', '2025-01-19', '2025-01-19', '2025-01-19', 'R001', '数据中心机房A', 50, 46, 3, 1, 0, 98.0, 1170.0, 24.0, 15, 1, 8, 6, 78.0, 2.4, 2, 1, 'U001', '张三'),
('DPR002', 'DPR-W-20250119', 'WEEKLY', '2025-01-19', '2025-01-13', '2025-01-19', 'R001', '数据中心机房A', 50, 48, 2, 0, 0, 99.0, 8190.0, 82.0, 42, 3, 18, 21, 195.0, 2.0, 5, 3, 'U001', '张三');
