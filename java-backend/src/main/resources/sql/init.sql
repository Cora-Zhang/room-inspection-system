-- ============================================
-- 机房巡检系统 - MySQL5.7 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `room_inspection` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `room_inspection`;

-- ============================================
-- 用户和权限模块
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` varchar(36) NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `department_id` varchar(36) DEFAULT NULL COMMENT '部门ID',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `employee_id` varchar(50) DEFAULT NULL COMMENT '员工工号',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态',
  `source` varchar(20) NOT NULL DEFAULT 'LOCAL' COMMENT '用户来源',
  `external_id` varchar(100) DEFAULT NULL COMMENT '外部系统ID',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `failed_login_count` int NOT NULL DEFAULT '0' COMMENT '失败登录次数',
  `locked_until` datetime DEFAULT NULL COMMENT '锁定时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_employee_id` (`employee_id`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_status` (`status`),
  KEY `idx_source` (`source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 部门表
CREATE TABLE IF NOT EXISTS `departments` (
  `id` varchar(36) NOT NULL COMMENT '部门ID',
  `code` varchar(50) NOT NULL COMMENT '部门编码',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  `parent_id` varchar(36) DEFAULT NULL COMMENT '父部门ID',
  `level` int NOT NULL DEFAULT '0' COMMENT '层级',
  `path` varchar(500) DEFAULT NULL COMMENT '层级路径',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `external_id` varchar(100) DEFAULT NULL COMMENT '外部系统ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 角色表
CREATE TABLE IF NOT EXISTS `roles` (
  `id` varchar(36) NOT NULL COMMENT '角色ID',
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `name` varchar(100) NOT NULL COMMENT '角色名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `department_id` varchar(36) DEFAULT NULL COMMENT '部门ID',
  `is_system` tinyint(1) NOT NULL DEFAULT '0' COMMENT '系统内置角色',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permissions` (
  `id` varchar(36) NOT NULL COMMENT '权限ID',
  `code` varchar(100) NOT NULL COMMENT '权限编码',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `type` varchar(20) NOT NULL COMMENT '权限类型',
  `resource` varchar(50) DEFAULT NULL COMMENT '资源',
  `action` varchar(50) DEFAULT NULL COMMENT '操作',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_type` (`type`),
  KEY `idx_resource` (`resource`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_roles` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `user_id` varchar(36) NOT NULL COMMENT '用户ID',
  `role_id` varchar(36) NOT NULL COMMENT '角色ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permissions` (
  `id` varchar(36) NOT NULL COMMENT 'ID',
  `role_id` varchar(36) NOT NULL COMMENT '角色ID',
  `permission_id` varchar(36) NOT NULL COMMENT '权限ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================
-- 机房和设备模块
-- ============================================

-- 机房表
CREATE TABLE IF NOT EXISTS `rooms` (
  `id` varchar(36) NOT NULL COMMENT '机房ID',
  `code` varchar(50) NOT NULL COMMENT '机房编号',
  `name` varchar(100) NOT NULL COMMENT '机房名称',
  `type` varchar(20) NOT NULL COMMENT '机房类型',
  `location` varchar(200) DEFAULT NULL COMMENT '位置',
  `floor` varchar(50) DEFAULT NULL COMMENT '楼层',
  `area` double DEFAULT NULL COMMENT '面积（平方米）',
  `capacity` int DEFAULT NULL COMMENT '容量（机柜数）',
  `manager_id` varchar(36) DEFAULT NULL COMMENT '负责人ID',
  `admin_id` varchar(36) DEFAULT NULL COMMENT '管理员ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '机房状态',
  `temperature_threshold` double DEFAULT NULL COMMENT '温度阈值（℃）',
  `humidity_threshold` double DEFAULT NULL COMMENT '湿度阈值（%）',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_manager_id` (`manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机房表';

-- 设备表
CREATE TABLE IF NOT EXISTS `devices` (
  `id` varchar(36) NOT NULL COMMENT '设备ID',
  `code` varchar(50) NOT NULL COMMENT '设备编号',
  `name` varchar(100) NOT NULL COMMENT '设备名称',
  `type` varchar(50) NOT NULL COMMENT '设备类型',
  `brand` varchar(50) DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `serial_number` varchar(100) DEFAULT NULL COMMENT '序列号',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `rack_code` varchar(50) DEFAULT NULL COMMENT '机柜编号',
  `u_position` int DEFAULT NULL COMMENT 'U位',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `mac_address` varchar(50) DEFAULT NULL COMMENT 'MAC地址',
  `protocol` varchar(20) DEFAULT NULL COMMENT '监控协议',
  `monitor_params` text DEFAULT NULL COMMENT '监控参数',
  `access_control_device_id` varchar(100) DEFAULT NULL COMMENT '门禁设备ID',
  `status` varchar(20) NOT NULL DEFAULT 'ONLINE' COMMENT '设备状态',
  `manager_id` varchar(36) DEFAULT NULL COMMENT '负责人ID',
  `purchase_date` datetime DEFAULT NULL COMMENT '采购日期',
  `warranty_expire_date` datetime DEFAULT NULL COMMENT '保修到期日期',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_protocol` (`protocol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备表';

-- ============================================
-- 巡检模块
-- ============================================

-- 巡检记录表
CREATE TABLE IF NOT EXISTS `inspections` (
  `id` varchar(36) NOT NULL COMMENT '巡检ID',
  `code` varchar(50) NOT NULL COMMENT '巡检编号',
  `room_id` varchar(36) NOT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `inspector_id` varchar(36) NOT NULL COMMENT '巡检人ID',
  `inspector_name` varchar(50) DEFAULT NULL COMMENT '巡检人姓名',
  `shift` varchar(20) NOT NULL COMMENT '班次：DAY-白班，NIGHT-夜班',
  `inspection_date` datetime NOT NULL COMMENT '巡检日期',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `temperature` double DEFAULT NULL COMMENT '温度（℃）',
  `humidity` double DEFAULT NULL COMMENT '湿度（%）',
  `result` varchar(20) DEFAULT NULL COMMENT '巡检结果',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '巡检状态',
  `issue_count` int NOT NULL DEFAULT '0' COMMENT '发现问题数量',
  `issues` text DEFAULT NULL COMMENT '问题描述',
  `suggestions` text DEFAULT NULL COMMENT '处理建议',
  `photos` text DEFAULT NULL COMMENT '照片URL列表',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_inspector_id` (`inspector_id`),
  KEY `idx_shift` (`shift`),
  KEY `idx_inspection_date` (`inspection_date`),
  KEY `idx_status` (`status`),
  KEY `idx_result` (`result`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='巡检记录表';

-- ============================================
-- 值班管理模块
-- ============================================

-- 值班人员表
CREATE TABLE IF NOT EXISTS `staffs` (
  `id` varchar(36) NOT NULL COMMENT '值班人员ID',
  `employee_id` varchar(50) NOT NULL COMMENT '员工工号',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `position` varchar(50) DEFAULT NULL COMMENT '职位',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '人员状态',
  `joined_date` datetime DEFAULT NULL COMMENT '入职日期',
  `skills` varchar(500) DEFAULT NULL COMMENT '技能',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_id` (`employee_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='值班人员表';

-- 值班排班表
CREATE TABLE IF NOT EXISTS `shift_schedules` (
  `id` varchar(36) NOT NULL COMMENT '排班ID',
  `schedule_date` date NOT NULL COMMENT '值班日期',
  `shift` varchar(20) NOT NULL COMMENT '班次：DAY-白班，NIGHT-夜班',
  `staff_id` varchar(36) NOT NULL COMMENT '值班人员ID',
  `staff_name` varchar(50) DEFAULT NULL COMMENT '值班人员姓名',
  `room_id` varchar(36) DEFAULT NULL COMMENT '责任机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '责任机房名称',
  `status` varchar(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT '排班状态',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `data_source` int NOT NULL DEFAULT '1' COMMENT '数据来源（1-手动创建 2-Excel导入 3-钉钉同步）',
  `import_batch` varchar(50) DEFAULT NULL COMMENT '导入批次号',
  `sync_dingtalk_status` int DEFAULT '0' COMMENT '钉钉同步状态（0-未同步 1-已同步 2-同步失败）',
  `dingtalk_task_id` varchar(100) DEFAULT NULL COMMENT '钉钉任务ID',
  `schedule_type` int DEFAULT NULL COMMENT '周期性排班类型（null-非周期性 1-每周轮换 2-每月轮换 3-季度轮换）',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule` (`schedule_date`,`shift`,`room_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_status` (`status`),
  KEY `idx_data_source` (`data_source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='值班排班表';

-- 值班交接记录表
CREATE TABLE IF NOT EXISTS `shift_handovers` (
  `id` varchar(36) NOT NULL COMMENT '交接记录ID',
  `code` varchar(50) NOT NULL COMMENT '交接编号',
  `handover_date` date NOT NULL COMMENT '交接日期',
  `shift` varchar(20) NOT NULL COMMENT '班次',
  `room_id` varchar(36) DEFAULT NULL COMMENT '责任机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '责任机房名称',
  `outgoing_staff_id` varchar(36) NOT NULL COMMENT '交班人ID',
  `outgoing_staff_name` varchar(50) DEFAULT NULL COMMENT '交班人姓名',
  `incoming_staff_id` varchar(36) NOT NULL COMMENT '接班人ID',
  `incoming_staff_name` varchar(50) DEFAULT NULL COMMENT '接班人姓名',
  `outgoing_time` datetime DEFAULT NULL COMMENT '交班时间',
  `incoming_time` datetime DEFAULT NULL COMMENT '接班时间',
  `tasks` text DEFAULT NULL COMMENT '完成任务列表',
  `issues` text DEFAULT NULL COMMENT '待处理问题列表',
  `equipment_status` text DEFAULT NULL COMMENT '设备状态',
  `handover_content` text DEFAULT NULL COMMENT '交接内容',
  `incoming_confirmed` tinyint(1) DEFAULT '0' COMMENT '接班确认',
  `access_records` text DEFAULT NULL COMMENT '门禁刷卡记录',
  `is_abnormal` tinyint(1) DEFAULT '0' COMMENT '是否异常',
  `abnormal_reason` varchar(500) DEFAULT NULL COMMENT '异常原因',
  `is_reminded` tinyint(1) DEFAULT '0' COMMENT '是否已发送交接提醒',
  `remind_time` datetime DEFAULT NULL COMMENT '提醒发送时间',
  `remark` text DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_handover_date` (`handover_date`),
  KEY `idx_shift` (`shift`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_outgoing_staff_id` (`outgoing_staff_id`),
  KEY `idx_incoming_staff_id` (`incoming_staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='值班交接记录表';

-- 门禁权限表
CREATE TABLE IF NOT EXISTS `door_access_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `staff_id` bigint NOT NULL COMMENT '值班人员ID',
  `staff_name` varchar(50) DEFAULT NULL COMMENT '值班人员姓名',
  `staff_no` varchar(50) DEFAULT NULL COMMENT '值班人员工号',
  `room_id` bigint DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `device_id` varchar(100) DEFAULT NULL COMMENT '门禁设备ID',
  `card_no` varchar(50) DEFAULT NULL COMMENT '门禁卡号',
  `permission_type` int NOT NULL DEFAULT '1' COMMENT '权限类型（1-值班权限 2-临时权限）',
  `effective_start_time` datetime NOT NULL COMMENT '生效开始时间',
  `effective_end_time` datetime NOT NULL COMMENT '生效结束时间',
  `status` int NOT NULL DEFAULT '0' COMMENT '权限状态（0-未生效 1-生效中 2-已过期 3-已回收）',
  `sync_status` int NOT NULL DEFAULT '0' COMMENT '下发状态（0-未下发 1-已下发 2-下发失败）',
  `sync_time` datetime DEFAULT NULL COMMENT '下发时间',
  `door_system_type` int DEFAULT NULL COMMENT '门禁系统类型（1-海康 2-大华）',
  `revoke_time` datetime DEFAULT NULL COMMENT '回收时间',
  `revoke_reason` varchar(500) DEFAULT NULL COMMENT '回收原因',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_permission_type` (`permission_type`),
  KEY `idx_status` (`status`),
  KEY `idx_effective_time` (`effective_start_time`, `effective_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁权限表';

-- 临时门禁权限申请表
CREATE TABLE IF NOT EXISTS `temp_access_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `applicant_no` varchar(50) NOT NULL COMMENT '申请人工号',
  `applicant_name` varchar(50) NOT NULL COMMENT '申请人姓名',
  `department` varchar(100) DEFAULT NULL COMMENT '申请人部门',
  `phone` varchar(20) DEFAULT NULL COMMENT '申请人联系电话',
  `room_id` bigint DEFAULT NULL COMMENT '访问机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '访问机房名称',
  `reason` text DEFAULT NULL COMMENT '访问事由',
  `access_start_time` datetime NOT NULL COMMENT '访问开始时间',
  `access_end_time` datetime NOT NULL COMMENT '访问结束时间',
  `approval_status` int NOT NULL DEFAULT '0' COMMENT '审批状态（0-待审批 1-审批通过 2-审批拒绝 3-已撤销）',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) DEFAULT NULL COMMENT '审批人姓名',
  `approval_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approval_comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `sync_status` int NOT NULL DEFAULT '0' COMMENT '权限下发状态（0-未下发 1-已下发 2-下发失败）',
  `effective_status` int NOT NULL DEFAULT '0' COMMENT '权限生效状态（0-未生效 1-生效中 2-已过期 3-已回收）',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除标志',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_applicant_no` (`applicant_no`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_access_time` (`access_start_time`, `access_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临时门禁权限申请表';

-- ============================================
-- 系统配置模块
-- ============================================

-- SSO配置表
CREATE TABLE IF NOT EXISTS `sso_config` (
  `id` varchar(36) NOT NULL COMMENT '配置ID',
  `provider_code` varchar(50) NOT NULL COMMENT 'SSO提供商编码',
  `provider_name` varchar(100) NOT NULL COMMENT 'SSO提供商名称',
  `client_id` varchar(200) NOT NULL COMMENT '客户端ID',
  `client_secret` varchar(200) NOT NULL COMMENT '客户端密钥',
  `authorization_uri` varchar(500) NOT NULL COMMENT '授权URI',
  `token_uri` varchar(500) NOT NULL COMMENT '令牌URI',
  `user_info_uri` varchar(500) NOT NULL COMMENT '用户信息URI',
  `jwk_set_uri` varchar(500) DEFAULT NULL COMMENT 'JWK集合URI',
  `redirect_uri` varchar(500) NOT NULL COMMENT '回调URI',
  `scopes` varchar(500) DEFAULT NULL COMMENT '授权范围',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `data_sync_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用数据同步',
  `sync_api_url` varchar(500) DEFAULT NULL COMMENT '数据同步API地址',
  `sync_api_key` varchar(200) DEFAULT NULL COMMENT '数据同步API密钥',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_code` (`provider_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SSO配置表';

-- ============================================
-- 监控和告警模块
-- ============================================

-- 告警记录表
CREATE TABLE IF NOT EXISTS `alerts` (
  `id` varchar(36) NOT NULL COMMENT '告警ID',
  `code` varchar(50) NOT NULL COMMENT '告警编号',
  `type` varchar(50) NOT NULL COMMENT '告警类型',
  `level` varchar(20) NOT NULL COMMENT '告警级别',
  `source` varchar(50) DEFAULT NULL COMMENT '告警来源',
  `device_id` varchar(36) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `title` varchar(200) NOT NULL COMMENT '告警标题',
  `content` text NOT NULL COMMENT '告警内容',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '告警状态',
  `alert_time` datetime NOT NULL COMMENT '告警时间',
  `ack_time` datetime DEFAULT NULL COMMENT '确认时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handler_id` varchar(36) DEFAULT NULL COMMENT '处理人ID',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '处理人姓名',
  `handle_result` text DEFAULT NULL COMMENT '处理结果',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_type` (`type`),
  KEY `idx_level` (`level`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_status` (`status`),
  KEY `idx_alert_time` (`alert_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

-- ============================================
-- 插入初始数据
-- ============================================

-- 插入默认管理员用户
INSERT INTO `users` (`id`, `username`, `password`, `real_name`, `status`, `source`) VALUES
('admin-id', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ACTIVE', 'LOCAL');

-- 插入默认角色
INSERT INTO `roles` (`id`, `code`, `name`, `description`, `is_system`, `status`) VALUES
('role-admin', 'ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1, 'ACTIVE'),
('role-manager', 'MANAGER', '管理员', '管理员，拥有大部分管理权限', 1, 'ACTIVE'),
('role-inspector', 'INSPECTOR', '巡检员', '巡检员，负责机房巡检工作', 1, 'ACTIVE'),
('role-operator', 'OPERATOR', '值班员', '值班员，负责日常值班工作', 1, 'ACTIVE');

-- 为管理员分配角色
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`) VALUES
('user-role-admin', 'admin-id', 'role-admin');

-- 插入示例部门
INSERT INTO `departments` (`id`, `code`, `name`, `parent_id`, `level`, `path`, `status`) VALUES
('dept-root', 'ROOT', '总部', NULL, 0, '/ROOT', 'ACTIVE'),
('dept-ops', 'OPS', '运维部', 'dept-root', 1, '/ROOT/dept-ops', 'ACTIVE'),
('dept-net', 'NET', '网络部', 'dept-root', 1, '/ROOT/dept-net', 'ACTIVE');

-- 插入示例机房
INSERT INTO `rooms` (`id`, `code`, `name`, `type`, `location`, `floor`, `area`, `capacity`, `status`) VALUES
('room-001', 'ROOM-A-101', '机房A-101', 'IDC', '中心机房', '3楼', 500.0, 200, 'NORMAL'),
('room-002', 'ROOM-B-203', '机房B-203', 'IDC', '中心机房', '2楼', 300.0, 100, 'NORMAL'),
('room-003', 'ROOM-C-305', '机房C-305', 'EDGE', '边缘机房', '5楼', 200.0, 50, 'NORMAL');

-- 插入示例值班人员
INSERT INTO `staffs` (`id`, `employee_id`, `name`, `department`, `position`, `phone`, `email`, `status`, `joined_date`) VALUES
('staff-001', 'STF-001', '张三', '运维一部', '值班工程师', '13800138001', 'zhangsan@example.com', 'ACTIVE', '2024-01-15'),
('staff-002', 'STF-002', '李四', '运维一部', '值班工程师', '13800138002', 'lisi@example.com', 'ACTIVE', '2024-02-20'),
('staff-003', 'STF-003', '王五', '运维二部', '值班工程师', '13800138003', 'wangwu@example.com', 'ACTIVE', '2024-03-10');

-- 插入示例排班（白班和夜班）
INSERT INTO `shift_schedules` (`id`, `schedule_date`, `shift`, `staff_id`, `staff_name`, `status`) VALUES
('schedule-001', CURDATE(), 'DAY', 'staff-001', '张三', 'SCHEDULED'),
('schedule-002', CURDATE(), 'NIGHT', 'staff-002', '李四', 'SCHEDULED');

-- ============================================
-- 接口配置模块
-- ============================================

-- 接口配置表
CREATE TABLE IF NOT EXISTS `api_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_type` varchar(50) NOT NULL COMMENT '配置类型：DINGTALK-钉钉, SMS-短信, EMAIL-邮件, HIKVISION-海康门禁, DAHUA-大华门禁, SNMP-SNMP监控, MODBUS-Modbus监控, BMS-BMS接口, SENSOR-传感器网络, FIRE-消防主机, OTHER-其他',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text DEFAULT NULL COMMENT '配置值（敏感信息需加密存储）',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `is_sensitive` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否敏感配置（0-否 1-是）',
  `config_group` varchar(50) DEFAULT NULL COMMENT '配置分组',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（0-禁用 1-启用）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序序号',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` text DEFAULT NULL COMMENT '备注',
  `extra_config` json DEFAULT NULL COMMENT '扩展JSON字段（存储额外的配置信息）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_type_key` (`config_type`,`config_key`),
  KEY `idx_config_type` (`config_type`),
  KEY `idx_config_group` (`config_group`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口配置表';

-- 插入示例接口配置
-- 钉钉API配置
INSERT INTO `api_config` (`config_type`, `config_name`, `config_key`, `config_value`, `description`, `is_sensitive`, `config_group`, `status`, `sort_order`) VALUES
('DINGTALK', '钉钉AppKey', 'dingtalk.app.key', '', '钉钉开放平台AppKey', 1, '钉钉配置', 1, 1),
('DINGTALK', '钉钉AppSecret', 'dingtalk.app.secret', '', '钉钉开放平台AppSecret', 1, '钉钉配置', 1, 2),
('DINGTALK', '钉钉AgentId', 'dingtalk.agent.id', '', '钉钉微应用AgentId', 0, '钉钉配置', 1, 3),
('DINGTALK', '钉钉API地址', 'dingtalk.api.url', 'https://oapi.dingtalk.com', '钉钉API服务地址', 0, '钉钉配置', 1, 4);

-- 短信API配置
INSERT INTO `api_config` (`config_type`, `config_name`, `config_key`, `config_value`, `description`, `is_sensitive`, `config_group`, `status`, `sort_order`) VALUES
('SMS', '阿里云AccessKeyId', 'sms.access.key.id', '', '阿里云短信服务AccessKeyId', 1, '短信配置', 1, 1),
('SMS', '阿里云AccessKeySecret', 'sms.access.key.secret', '', '阿里云短信服务AccessKeySecret', 1, '短信配置', 1, 2),
('SMS', '短信签名', 'sms.sign.name', '', '短信签名名称', 0, '短信配置', 1, 3),
('SMS', '值班提醒模板ID', 'sms.template.shift.remind', '', '值班提醒短信模板ID', 0, '短信配置', 1, 4),
('SMS', '交接提醒模板ID', 'sms.template.handover.remind', '', '交接提醒短信模板ID', 0, '短信配置', 1, 5);

-- 门禁API配置（海康）
INSERT INTO `api_config` (`config_type`, `config_name`, `config_key`, `config_value`, `description`, `is_sensitive`, `config_group`, `status`, `sort_order`) VALUES
('HIKVISION', '海康API地址', 'hikvision.api.url', 'http://localhost:8080/artemis', '海康门禁API地址', 0, '海康门禁', 1, 1),
('HIKVISION', '海康AppKey', 'hikvision.app.key', '', '海康开放平台AppKey', 1, '海康门禁', 1, 2),
('HIKVISION', '海康AppSecret', 'hikvision.app.secret', '', '海康开放平台AppSecret', 1, '海康门禁', 1, 3);

-- 门禁API配置（大华）
INSERT INTO `api_config` (`config_type`, `config_name`, `config_key`, `config_value`, `description`, `is_sensitive`, `config_group`, `status`, `sort_order`) VALUES
('DAHUA', '大华API地址', 'dahua.api.url', 'http://localhost:8080', '大华门禁API地址', 0, '大华门禁', 1, 1),
('DAHUA', '大华用户名', 'dahua.username', 'admin', '大华门禁系统用户名', 1, '大华门禁', 1, 2),
('DAHUA', '大华密码', 'dahua.password', '', '大华门禁系统密码', 1, '大华门禁', 1, 3);

-- 监控协议配置（SNMP）
INSERT INTO `api_config` (`config_type`, `config_name`, `config_key`, `config_value`, `description`, `is_sensitive`, `config_group`, `status`, `sort_order`) VALUES
('SNMP', 'SNMP版本', 'snmp.version', 'v2c', 'SNMP协议版本', 0, 'SNMP配置', 1, 1),
('SNMP', 'SNMP Community', 'snmp.community', 'public', 'SNMP Community字符串', 1, 'SNMP配置', 1, 2),
('SNMP', 'SNMP超时时间', 'snmp.timeout', '5000', 'SNMP超时时间（毫秒）', 0, 'SNMP配置', 1, 3),
('SNMP', 'SNMP重试次数', 'snmp.retry', '3', 'SNMP重试次数', 0, 'SNMP配置', 1, 4);

-- 监控协议配置（Modbus）
INSERT INTO `api_config` (`config_type`, `config_name`, `config_key`, `config_value`, `description`, `is_sensitive`, `config_group`, `status`, `sort_order`) VALUES
('MODBUS', 'Modbus超时时间', 'modbus.timeout', '3000', 'Modbus超时时间（毫秒）', 0, 'Modbus配置', 1, 1),
('MODBUS', 'Modbus重试次数', 'modbus.retry', '2', 'Modbus重试次数', 0, 'Modbus配置', 1, 2);

-- ============================================
-- 设备巡检模块
-- ============================================

-- 更新设备表结构
ALTER TABLE `devices`
ADD COLUMN `sub_type` varchar(50) DEFAULT NULL COMMENT '设备子类型' AFTER `type`,
ADD COLUMN `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称' AFTER `room_id`,
ADD COLUMN `snmp_version` varchar(10) DEFAULT 'v2c' COMMENT 'SNMP版本' AFTER `protocol`,
ADD COLUMN `snmp_community` varchar(100) DEFAULT 'public' COMMENT 'SNMP Community字符串' AFTER `snmp_version`,
ADD COLUMN `snmp_port` int DEFAULT 161 COMMENT 'SNMP端口号' AFTER `snmp_community`,
ADD COLUMN `is_key_device` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关键设备（需要拍照巡检）' AFTER `access_control_device_id`,
ADD COLUMN `inspection_cycle` varchar(20) DEFAULT 'DAILY' COMMENT '巡检周期' AFTER `is_key_device`,
ADD COLUMN `custom_inspection_interval` int DEFAULT NULL COMMENT '自定义巡检间隔（分钟）' AFTER `inspection_cycle`,
ADD COLUMN `cpu_threshold` double DEFAULT 80.0 COMMENT 'CPU使用率告警阈值（%）' AFTER `custom_inspection_interval`,
ADD COLUMN `memory_threshold` double DEFAULT 85.0 COMMENT '内存使用率告警阈值（%）' AFTER `cpu_threshold`,
ADD COLUMN `disk_threshold` double DEFAULT 90.0 COMMENT '磁盘使用率告警阈值（%）' AFTER `memory_threshold`,
ADD COLUMN `traffic_threshold` double DEFAULT 200.0 COMMENT '流量异常波动告警阈值（%）' AFTER `disk_threshold`,
ADD COLUMN `port_down_alert_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '端口异常告警' AFTER `traffic_threshold`,
ADD COLUMN `device_labels` text DEFAULT NULL COMMENT '设备标签信息（JSON格式，用于OCR识别）' AFTER `port_down_alert_enabled`,
ADD COLUMN `latest_photo_url` varchar(500) DEFAULT NULL COMMENT '最新巡检照片URL' AFTER `device_labels`,
ADD COLUMN `last_inspection_time` datetime DEFAULT NULL COMMENT '上次巡检时间' AFTER `latest_photo_url`,
ADD COLUMN `next_inspection_time` datetime DEFAULT NULL COMMENT '下次巡检时间' AFTER `last_inspection_time`,
ADD COLUMN `current_cpu_usage` double DEFAULT NULL COMMENT '当前CPU使用率（%）' AFTER `status`,
ADD COLUMN `current_memory_usage` double DEFAULT NULL COMMENT '当前内存使用率（%）' AFTER `current_cpu_usage`,
ADD COLUMN `current_disk_usage` double DEFAULT NULL COMMENT '当前磁盘使用率（%）' AFTER `current_memory_usage`,
ADD COLUMN `last_status_update_time` datetime DEFAULT NULL COMMENT '最近状态更新时间' AFTER `current_disk_usage`,
ADD COLUMN `manager_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名' AFTER `manager_id`,
ADD COLUMN `created_by` bigint DEFAULT NULL COMMENT '创建人ID' AFTER `description`,
ADD COLUMN `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名' AFTER `created_by`,
ADD COLUMN `updated_by` bigint DEFAULT NULL COMMENT '更新人ID' AFTER `created_at`,
ADD COLUMN `updated_by_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名' AFTER `updated_by`;

-- 设备巡检记录表
CREATE TABLE IF NOT EXISTS `device_inspection_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) NOT NULL COMMENT '巡检编号',
  `device_id` varchar(36) NOT NULL COMMENT '设备ID',
  `device_code` varchar(50) DEFAULT NULL COMMENT '设备编号',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `inspection_type` varchar(20) NOT NULL DEFAULT 'AUTO' COMMENT '巡检类型（AUTO-自动巡检，MANUAL-手动巡检）',
  `inspection_method` varchar(20) NOT NULL COMMENT '巡检方式（SNMP-SNMP采集，SCRIPT-脚本执行，PHOTO-拍照巡检，MANUAL-手动录入）',
  `start_time` datetime NOT NULL COMMENT '巡检开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '巡检结束时间',
  `inspector_id` varchar(36) DEFAULT NULL COMMENT '巡检人ID',
  `inspector_name` varchar(50) DEFAULT NULL COMMENT '巡检人姓名',
  `result` varchar(20) DEFAULT NULL COMMENT '巡检结果（NORMAL-正常，WARNING-警告，ERROR-错误）',
  `cpu_usage` double DEFAULT NULL COMMENT 'CPU使用率（%）',
  `memory_usage` double DEFAULT NULL COMMENT '内存使用率（%）',
  `disk_usage` double DEFAULT NULL COMMENT '磁盘使用率（%）',
  `inspection_data` text DEFAULT NULL COMMENT '巡检数据（JSON格式）',
  `issues` text DEFAULT NULL COMMENT '异常信息',
  `suggestions` text DEFAULT NULL COMMENT '处理建议',
  `photo_urls` text DEFAULT NULL COMMENT '照片URL列表（逗号分隔）',
  `ocr_recognized` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已OCR识别',
  `ocr_result` text DEFAULT NULL COMMENT 'OCR识别结果（JSON格式）',
  `has_watermark` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否包含水印',
  `watermark_info` text DEFAULT NULL COMMENT '水印信息（JSON格式）',
  `script_result` text DEFAULT NULL COMMENT '脚本执行结果',
  `is_alerted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否告警',
  `alert_ids` varchar(500) DEFAULT NULL COMMENT '告警ID列表（逗号分隔）',
  `remark` text DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_inspection_type` (`inspection_type`),
  KEY `idx_result` (`result`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备巡检记录表';

-- 设备监控指标表
CREATE TABLE IF NOT EXISTS `device_metric` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(36) NOT NULL COMMENT '设备ID',
  `device_code` varchar(50) DEFAULT NULL COMMENT '设备编号',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `metric_type` varchar(50) NOT NULL COMMENT '指标类型',
  `metric_name` varchar(100) DEFAULT NULL COMMENT '指标名称',
  `metric_value` double DEFAULT NULL COMMENT '指标值',
  `unit` varchar(20) DEFAULT NULL COMMENT '指标单位',
  `status` varchar(20) DEFAULT 'NORMAL' COMMENT '指标状态（NORMAL-正常，WARNING-警告，CRITICAL-严重）',
  `exceeded_threshold` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否超出阈值',
  `port_name` varchar(100) DEFAULT NULL COMMENT '端口名称',
  `port_index` int DEFAULT NULL COMMENT '端口索引',
  `port_status` varchar(20) DEFAULT NULL COMMENT '端口状态（UP, DOWN, TESTING, UNKNOWN）',
  `disk_name` varchar(100) DEFAULT NULL COMMENT '磁盘名称',
  `disk_path` varchar(200) DEFAULT NULL COMMENT '磁盘路径',
  `disk_total` double DEFAULT NULL COMMENT '磁盘总量（GB）',
  `disk_used` double DEFAULT NULL COMMENT '磁盘已用量（GB）',
  `disk_free` double DEFAULT NULL COMMENT '磁盘可用量（GB）',
  `threshold_upper` double DEFAULT NULL COMMENT '阈值上限',
  `threshold_lower` double DEFAULT NULL COMMENT '阈值下限',
  `custom_key` varchar(200) DEFAULT NULL COMMENT '自定义指标键',
  `custom_value` text DEFAULT NULL COMMENT '自定义指标值（JSON格式）',
  `collection_method` varchar(20) DEFAULT NULL COMMENT '采集方式',
  `collection_time` datetime NOT NULL COMMENT '采集时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_metric_type` (`metric_type`),
  KEY `idx_collection_time` (`collection_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备监控指标表';

-- 告警规则表
CREATE TABLE IF NOT EXISTS `alert_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_code` varchar(100) NOT NULL COMMENT '规则编码',
  `device_id` varchar(36) DEFAULT NULL COMMENT '设备ID',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `device_sub_type` varchar(50) DEFAULT NULL COMMENT '设备子类型',
  `room_id` varchar(36) DEFAULT NULL COMMENT '机房ID',
  `alert_type` varchar(50) NOT NULL COMMENT '告警类型',
  `alert_level` varchar(20) NOT NULL COMMENT '告警级别（INFO-信息，WARNING-警告，CRITICAL-严重，EMERGENCY-紧急）',
  `condition` varchar(20) NOT NULL COMMENT '告警条件（GT-大于，LT-小于，EQ-等于等）',
  `threshold_upper` double DEFAULT NULL COMMENT '阈值上限',
  `threshold_lower` double DEFAULT NULL COMMENT '阈值下限',
  `duration` int DEFAULT NULL COMMENT '持续时间（秒）',
  `message_template` varchar(500) DEFAULT NULL COMMENT '告警消息模板',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `notify_method` varchar(20) DEFAULT 'ALL' COMMENT '通知方式（EMAIL-邮件，SMS-短信，DINGTALK-钉钉，ALL-全部）',
  `notify_user_ids` varchar(500) DEFAULT NULL COMMENT '通知人员ID列表（逗号分隔）',
  `is_silenced` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否静默',
  `silence_start_time` datetime DEFAULT NULL COMMENT '静默开始时间',
  `silence_end_time` datetime DEFAULT NULL COMMENT '静默结束时间',
  `recovery_notify` tinyint(1) NOT NULL DEFAULT 1 COMMENT '告警恢复通知',
  `priority` int NOT NULL DEFAULT 0 COMMENT '规则优先级',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_by_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `updated_by_name` varchar(50) DEFAULT NULL COMMENT '更新人姓名',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_alert_type` (`alert_type`),
  KEY `idx_alert_level` (`alert_level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- 插入示例告警规则
INSERT INTO `alert_rule` (`rule_name`, `rule_code`, `device_type`, `alert_type`, `alert_level`, `condition`, `threshold_upper`, `duration`, `message_template`, `status`, `notify_method`) VALUES
('CPU使用率过高告警', 'CPU_HIGH_USAGE', 'SERVER', 'CPU_USAGE', 'WARNING', 'GT', 80.0, 300, '设备{deviceName}的CPU使用率达到{metricValue}%，超过阈值80%', 1, 'ALL'),
('内存使用率过高告警', 'MEMORY_HIGH_USAGE', 'SERVER', 'MEMORY_USAGE', 'WARNING', 'GT', 85.0, 300, '设备{deviceName}的内存使用率达到{metricValue}%，超过阈值85%', 1, 'ALL'),
('磁盘使用率过高告警', 'DISK_HIGH_USAGE', 'SERVER', 'DISK_USAGE', 'CRITICAL', 'GT', 90.0, 300, '设备{deviceName}的磁盘{diskName}使用率达到{metricValue}%，超过阈值90%', 1, 'ALL'),
('端口Down告警', 'PORT_DOWN', 'SWITCH', 'PORT_DOWN', 'CRITICAL', 'EQ', 0.0, 0, '设备{deviceName}的端口{portName}状态为DOWN', 1, 'ALL'),
('流量异常波动告警', 'TRAFFIC_ABNORMAL', 'SWITCH', 'TRAFFIC_ABNORMAL', 'WARNING', 'GT', 200.0, 60, '设备{deviceName}的流量异常波动，变化率达到{metricValue}%', 1, 'ALL');

-- ============================================
-- 巡检核验体系模块
-- ============================================

-- 门禁日志表
CREATE TABLE IF NOT EXISTS `door_access_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `system_type` varchar(20) DEFAULT NULL COMMENT '门禁系统类型（hikvision-海康、dahua-大华、custom-自定义）',
  `device_id` varchar(100) DEFAULT NULL COMMENT '门禁设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '门禁设备名称',
  `room_id` bigint DEFAULT NULL COMMENT '机房ID',
  `staff_id` bigint DEFAULT NULL COMMENT '人员ID',
  `staff_name` varchar(50) DEFAULT NULL COMMENT '人员姓名',
  `staff_code` varchar(50) DEFAULT NULL COMMENT '人员工号/卡号',
  `direction` varchar(10) DEFAULT NULL COMMENT '进出方向（in-进入、out-离开）',
  `access_time` datetime NOT NULL COMMENT '进出时间',
  `access_method` varchar(20) DEFAULT NULL COMMENT '访问方式（card-刷卡、face-人脸、fingerprint-指纹、password-密码）',
  `status` varchar(20) DEFAULT NULL COMMENT '访问状态（success-成功、failed-失败、denied-拒绝）',
  `reject_reason` varchar(500) DEFAULT NULL COMMENT '拒绝原因',
  `inspection_task_id` bigint DEFAULT NULL COMMENT '关联巡检任务ID',
  `verified` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已核验',
  `verification_result` varchar(20) DEFAULT NULL COMMENT '核验结果（match-匹配、mismatch-不匹配、pending-待核验）',
  `photo_url` varchar(500) DEFAULT NULL COMMENT '门禁照片URL',
  `extra_data` text DEFAULT NULL COMMENT '扩展字段（JSON格式，存储门禁系统返回的原始数据）',
  `data_source` varchar(20) DEFAULT 'sync' COMMENT '数据来源（sync-同步对接、manual-手动录入）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_system_type` (`system_type`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_access_time` (`access_time`),
  KEY `idx_inspection_task_id` (`inspection_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁日志表';

-- 照片核验表
CREATE TABLE IF NOT EXISTS `photo_verification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inspection_task_id` bigint DEFAULT NULL COMMENT '关联巡检任务ID',
  `device_id` bigint DEFAULT NULL COMMENT '关联设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(50) DEFAULT NULL COMMENT '设备类型',
  `photo_path` varchar(500) DEFAULT NULL COMMENT '照片OSS存储路径',
  `photo_url` varchar(500) DEFAULT NULL COMMENT '照片URL',
  `uploader_id` bigint DEFAULT NULL COMMENT '上传人员ID',
  `uploader_name` varchar(50) DEFAULT NULL COMMENT '上传人员姓名',
  `photo_time` datetime DEFAULT NULL COMMENT '拍照时间',
  `photo_location` varchar(200) DEFAULT NULL COMMENT '拍摄位置',
  `resolution` varchar(20) DEFAULT NULL COMMENT '照片分辨率（如：1920x1080）',
  `file_size` bigint DEFAULT NULL COMMENT '照片大小（字节）',
  `clarity_score` int DEFAULT NULL COMMENT '清晰度评分（0-100分）',
  `brightness_score` int DEFAULT NULL COMMENT '亮度评分（0-100分）',
  `blur_status` varchar(20) DEFAULT NULL COMMENT '模糊度检测（normal-正常、blurry-模糊、very_blurry-严重模糊）',
  `ocr_result` text DEFAULT NULL COMMENT 'OCR识别结果（JSON格式）',
  `ocr_confidence` double DEFAULT NULL COMMENT 'OCR识别置信度（0-1）',
  `label_recognizable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '设备标签是否可识别',
  `time_consistent` tinyint(1) DEFAULT NULL COMMENT '时间一致性核验',
  `time_deviation` int DEFAULT NULL COMMENT '时间偏差（秒）',
  `location_consistent` tinyint(1) DEFAULT NULL COMMENT '位置一致性核验',
  `watermark` text DEFAULT NULL COMMENT '照片水印内容（JSON格式）',
  `verification_status` varchar(20) DEFAULT 'pending' COMMENT '核验状态（pending-待核验、passed-通过、failed-未通过、manual-人工复核）',
  `verification_summary` varchar(500) DEFAULT NULL COMMENT '核验结果摘要',
  `verifier_id` bigint DEFAULT NULL COMMENT '核验人员ID',
  `verification_time` datetime DEFAULT NULL COMMENT '核验时间',
  `abnormal_type` varchar(50) DEFAULT NULL COMMENT '异常标记（multiple-重复照片、dark-光线过暗、blur-模糊、no_label-无标签）',
  `extra_data` text DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_inspection_task_id` (`inspection_task_id`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_verification_status` (`verification_status`),
  KEY `idx_photo_time` (`photo_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片核验表';

-- 巡检核验表
CREATE TABLE IF NOT EXISTS `inspection_verification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inspection_task_id` bigint DEFAULT NULL COMMENT '巡检任务ID',
  `task_no` varchar(50) DEFAULT NULL COMMENT '任务编号',
  `room_id` bigint DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `inspector_id` bigint DEFAULT NULL COMMENT '巡检人员ID',
  `inspector_name` varchar(50) DEFAULT NULL COMMENT '巡检人员姓名',
  `planned_start_time` datetime DEFAULT NULL COMMENT '计划开始时间',
  `planned_end_time` datetime DEFAULT NULL COMMENT '计划结束时间',
  `actual_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  `planned_duration` int DEFAULT NULL COMMENT '计划时长（分钟）',
  `actual_duration` int DEFAULT NULL COMMENT '实际时长（分钟）',
  `stay_duration` int DEFAULT NULL COMMENT '停留时长（分钟）',
  `planned_device_count` int DEFAULT NULL COMMENT '计划巡检设备数',
  `actual_device_count` int DEFAULT NULL COMMENT '实际巡检设备数',
  `planned_route` text DEFAULT NULL COMMENT '计划巡检路线（JSON格式）',
  `actual_route` text DEFAULT NULL COMMENT '实际巡检路线（JSON格式）',
  `route_consistent` tinyint(1) DEFAULT NULL COMMENT '路线一致性',
  `access_verified` tinyint(1) DEFAULT 0 COMMENT '进出核验',
  `enter_log_id` bigint DEFAULT NULL COMMENT '进门禁记录ID',
  `exit_log_id` bigint DEFAULT NULL COMMENT '离门禁记录ID',
  `on_time_entry` tinyint(1) DEFAULT NULL COMMENT '是否按时进入',
  `on_time_completion` tinyint(1) DEFAULT NULL COMMENT '是否按时完成',
  `entry_delay` int DEFAULT NULL COMMENT '进入延迟时间（分钟）',
  `completion_delay` int DEFAULT NULL COMMENT '完成延迟时间（分钟）',
  `photo_passed_count` int DEFAULT 0 COMMENT '照片核验通过数',
  `photo_failed_count` int DEFAULT 0 COMMENT '照片核验失败数',
  `photo_pass_rate` decimal(5,2) DEFAULT NULL COMMENT '照片核验率（0-100）',
  `abnormal_behaviors` text DEFAULT NULL COMMENT '异常行为识别（JSON格式）',
  `quality_score` int DEFAULT NULL COMMENT '巡检质量评分（0-100分）',
  `grade_level` varchar(20) DEFAULT NULL COMMENT '评分等级（excellent-优秀、good-良好、average-合格、poor-较差、fail-不合格）',
  `verification_status` varchar(20) DEFAULT 'pending' COMMENT '核验状态（pending-待核验、processing-核验中、completed-已完成）',
  `verification_report` text DEFAULT NULL COMMENT '核验报告（Markdown格式）',
  `suggestions` text DEFAULT NULL COMMENT '核验建议（JSON格式）',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人员ID',
  `review_comment` text DEFAULT NULL COMMENT '审核意见',
  `extra_data` text DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_inspection_task_id` (`inspection_task_id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_inspector_id` (`inspector_id`),
  KEY `idx_verification_status` (`verification_status`),
  KEY `idx_quality_score` (`quality_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='巡检核验表';

-- ============================================
-- 电力系统监控与巡检模块
-- ============================================

-- 电力系统设备表
CREATE TABLE IF NOT EXISTS `power_system` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_code` varchar(50) NOT NULL COMMENT '设备编号',
  `device_name` varchar(100) NOT NULL COMMENT '设备名称',
  `device_type` varchar(20) NOT NULL COMMENT '设备类型（main_power-市电、ups-不间断电源、pdu-配电单元、generator-发电机）',
  `brand` varchar(50) DEFAULT NULL COMMENT '设备品牌',
  `model` varchar(100) DEFAULT NULL COMMENT '设备型号',
  `serial_number` varchar(100) DEFAULT NULL COMMENT '序列号',
  `room_id` bigint DEFAULT NULL COMMENT '机房ID',
  `room_name` varchar(100) DEFAULT NULL COMMENT '机房名称',
  `location` varchar(200) DEFAULT NULL COMMENT '位置描述',
  `rated_capacity` decimal(10,2) DEFAULT NULL COMMENT '额定容量（kVA或kW）',
  `protocol` varchar(20) DEFAULT NULL COMMENT '监控协议（snmp、modbus、bms、custom）',
  `monitor_address` varchar(100) DEFAULT NULL COMMENT '监控地址（IP地址或串口地址）',
  `monitor_port` int DEFAULT NULL COMMENT '监控端口',
  `auth_credential` varchar(200) DEFAULT NULL COMMENT 'SNMP Community或认证凭证',
  `oid_config` text DEFAULT NULL COMMENT 'OID配置（JSON格式）',
  `modbus_config` text DEFAULT NULL COMMENT 'Modbus寄存器配置（JSON格式）',
  `collect_interval` int DEFAULT 300 COMMENT '采集间隔（秒）',
  `last_collect_time` datetime DEFAULT NULL COMMENT '上次采集时间',
  `status` varchar(20) DEFAULT 'unknown' COMMENT '设备状态（online-在线、offline-离线、unknown-未知）',
  `last_alert_time` datetime DEFAULT NULL COMMENT '最后告警时间',
  `related_devices` text DEFAULT NULL COMMENT '关联设备列表（JSON格式）',
  `remark` text DEFAULT NULL COMMENT '备注说明',
  `extra_data` text DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_code` (`device_code`),
  KEY `idx_device_type` (`device_type`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_protocol` (`protocol`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电力系统设备表';

-- 电力指标表
CREATE TABLE IF NOT EXISTS `power_metric` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `power_system_id` bigint DEFAULT NULL COMMENT '电力设备ID',
  `device_code` varchar(50) DEFAULT NULL COMMENT '设备编号',
  `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型',
  `collect_time` datetime NOT NULL COMMENT '采集时间',
  `input_voltage_a` decimal(10,2) DEFAULT NULL COMMENT '输入电压A相（V）',
  `input_voltage_b` decimal(10,2) DEFAULT NULL COMMENT '输入电压B相（V）',
  `input_voltage_c` decimal(10,2) DEFAULT NULL COMMENT '输入电压C相（V）',
  `input_current_a` decimal(10,2) DEFAULT NULL COMMENT '输入电流A相（A）',
  `input_current_b` decimal(10,2) DEFAULT NULL COMMENT '输入电流B相（A）',
  `input_current_c` decimal(10,2) DEFAULT NULL COMMENT '输入电流C相（A）',
  `input_frequency` decimal(6,2) DEFAULT NULL COMMENT '输入频率（Hz）',
  `active_power` decimal(10,2) DEFAULT NULL COMMENT '有功功率（kW）',
  `reactive_power` decimal(10,2) DEFAULT NULL COMMENT '无功功率（kVar）',
  `power_factor` decimal(4,3) DEFAULT NULL COMMENT '功率因数',
  `ups_load_percent` decimal(5,2) DEFAULT NULL COMMENT 'UPS负载百分比（%）',
  `ups_backup_time` int DEFAULT NULL COMMENT 'UPS后备时间（分钟）',
  `output_voltage_a` decimal(10,2) DEFAULT NULL COMMENT 'UPS输出电压A相（V）',
  `output_voltage_b` decimal(10,2) DEFAULT NULL COMMENT 'UPS输出电压B相（V）',
  `output_voltage_c` decimal(10,2) DEFAULT NULL COMMENT 'UPS输出电压C相（V）',
  `battery_temperature` decimal(6,2) DEFAULT NULL COMMENT '电池温度（℃）',
  `battery_capacity` decimal(5,2) DEFAULT NULL COMMENT '电池容量百分比（%）',
  `battery_status` varchar(20) DEFAULT NULL COMMENT '电池状态（charging-充电、discharging-放电、idle-空闲）',
  `pdu_total_current` decimal(10,2) DEFAULT NULL COMMENT 'PDU总电流（A）',
  `pdu_load_status` varchar(20) DEFAULT NULL COMMENT 'PDU负载状态（normal-正常、overload-过载、warning-预警）',
  `pdu_output_voltage` decimal(10,2) DEFAULT NULL COMMENT 'PDU输出电压（V）',
  `total_load_percent` decimal(5,2) DEFAULT NULL COMMENT '总负载百分比（%）',
  `total_active_power` decimal(10,2) DEFAULT NULL COMMENT '总有功功率（kW）',
  `ambient_temperature` decimal(6,2) DEFAULT NULL COMMENT '环境温度（℃）',
  `ambient_humidity` decimal(5,2) DEFAULT NULL COMMENT '环境湿度（%）',
  `data_source` varchar(20) DEFAULT NULL COMMENT '数据来源（snmp、modbus、manual）',
  `is_alert` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否告警',
  `alert_info` text DEFAULT NULL COMMENT '告警信息（JSON格式）',
  `extra_data` text DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_power_system_id` (`power_system_id`),
  KEY `idx_device_code` (`device_code`),
  KEY `idx_collect_time` (`collect_time`),
  KEY `idx_is_alert` (`is_alert`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电力指标表';

-- 电力趋势表
CREATE TABLE IF NOT EXISTS `power_trend` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `power_system_id` bigint DEFAULT NULL COMMENT '电力设备ID',
  `device_code` varchar(50) DEFAULT NULL COMMENT '设备编号',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `device_type` varchar(20) DEFAULT NULL COMMENT '设备类型',
  `statistic_date` date NOT NULL COMMENT '统计日期',
  `statistic_period` varchar(20) NOT NULL COMMENT '统计周期（hourly-小时、daily-日、weekly-周、monthly-月）',
  `sample_count` int DEFAULT NULL COMMENT '采集样本数',
  `avg_load_percent` decimal(5,2) DEFAULT NULL COMMENT '平均负载百分比（%）',
  `max_load_percent` decimal(5,2) DEFAULT NULL COMMENT '最大负载百分比（%）',
  `min_load_percent` decimal(5,2) DEFAULT NULL COMMENT '最小负载百分比（%）',
  `peak_load_time` datetime DEFAULT NULL COMMENT '峰值负载时间',
  `load_std_dev` decimal(8,4) DEFAULT NULL COMMENT '负载标准差',
  `avg_active_power` decimal(10,2) DEFAULT NULL COMMENT '平均有功功率（kW）',
  `max_active_power` decimal(10,2) DEFAULT NULL COMMENT '最大有功功率（kW）',
  `total_energy_consumption` decimal(12,2) DEFAULT NULL COMMENT '累计耗电量（kWh）',
  `load_trend` varchar(20) DEFAULT NULL COMMENT '负载变化趋势（up-上升、down-下降、stable-稳定）',
  `consecutive_up_days` int DEFAULT 0 COMMENT '连续上升天数',
  `consecutive_down_days` int DEFAULT 0 COMMENT '连续下降天数',
  `seven_day_growth_rate` decimal(8,2) DEFAULT NULL COMMENT '7日负载增长率（%）',
  `thirty_day_growth_rate` decimal(8,2) DEFAULT NULL COMMENT '30日负载增长率（%）',
  `need_capacity_alert` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要容量预警',
  `alert_level` varchar(20) DEFAULT NULL COMMENT '预警等级（info-信息、warning-警告、critical-严重）',
  `alert_threshold` decimal(5,2) DEFAULT NULL COMMENT '预警阈值（%）',
  `days_to_threshold` int DEFAULT NULL COMMENT '预计达到阈值天数',
  `alert_report` text DEFAULT NULL COMMENT '预警报告（Markdown格式）',
  `suggestions` text DEFAULT NULL COMMENT '建议措施（JSON格式）',
  `capacity_utilization` decimal(5,2) DEFAULT NULL COMMENT '当前容量利用率（%）',
  `remaining_capacity` decimal(10,2) DEFAULT NULL COMMENT '剩余容量（kW）',
  `estimated_days_to_full_load` int DEFAULT NULL COMMENT '预计满载天数',
  `expansion_suggestion` text DEFAULT NULL COMMENT '扩容建议（JSON格式）',
  `extra_data` text DEFAULT NULL COMMENT '扩展字段（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标志',
  PRIMARY KEY (`id`),
  KEY `idx_power_system_id` (`power_system_id`),
  KEY `idx_device_code` (`device_code`),
  KEY `idx_statistic_date` (`statistic_date`),
  KEY `idx_statistic_period` (`statistic_period`),
  KEY `idx_need_capacity_alert` (`need_capacity_alert`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='电力趋势表';

-- 插入示例电力设备
INSERT INTO `power_system` (`device_code`, `device_name`, `device_type`, `brand`, `model`, `room_id`, `room_name`, `location`, `rated_capacity`, `protocol`, `monitor_address`, `monitor_port`, `collect_interval`, `status`) VALUES
('PWR-UPS-001', '机房A UPS主设备', 'ups', 'APC', 'Smart-UPS 3000VA', 1, '机房A-101', 'A区配电间', 3.00, 'snmp', '192.168.1.100', 161, 60, 'online'),
('PWR-PDU-001', '机房A PDU-01', 'pdu', 'Schneider', 'AP8868', 1, '机房A-101', 'A区第一排', 22.00, 'modbus', '192.168.1.101', 502, 60, 'online'),
('PWR-PDU-002', '机房A PDU-02', 'pdu', 'Schneider', 'AP8868', 1, '机房A-101', 'A区第二排', 22.00, 'modbus', '192.168.1.102', 502, 60, 'online'),
('PWR-MAIN-001', '市电输入总开关', 'main_power', 'Schneider', 'MasterPact NT', 1, '机房A-101', '配电间', 630.00, 'modbus', '192.168.1.103', 502, 60, 'online');
