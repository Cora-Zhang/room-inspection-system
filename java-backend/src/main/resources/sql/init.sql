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
