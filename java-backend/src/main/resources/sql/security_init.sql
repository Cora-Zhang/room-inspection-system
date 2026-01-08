-- =====================================
-- 机房巡检系统安全功能数据库初始化脚本
-- =====================================

-- 1. 操作日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '操作人ID',
    `username` VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作人IP地址',
    `operation_type` VARCHAR(20) DEFAULT NULL COMMENT '操作类型',
    `module` VARCHAR(20) DEFAULT NULL COMMENT '操作模块',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `params` TEXT DEFAULT NULL COMMENT '请求参数（加密存储）',
    `result` TEXT DEFAULT NULL COMMENT '响应结果（加密存储）',
    `status` VARCHAR(20) DEFAULT NULL COMMENT '操作状态',
    `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
    `execution_time` BIGINT(20) DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
    `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_module` (`module`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '角色描述',
    `level` INT(11) DEFAULT NULL COMMENT '角色级别',
    `data_scope` VARCHAR(20) DEFAULT NULL COMMENT '数据权限范围',
    `data_scope_dept_ids` VARCHAR(500) DEFAULT NULL COMMENT '自定义数据权限部门ID',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '角色状态',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父权限ID',
    `type` VARCHAR(20) DEFAULT NULL COMMENT '权限类型',
    `permission_code` VARCHAR(100) DEFAULT NULL COMMENT '权限编码',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
    `visible` TINYINT(1) DEFAULT 1 COMMENT '是否可见',
    `keep_alive` TINYINT(1) DEFAULT 0 COMMENT '是否缓存',
    `is_frame` TINYINT(1) DEFAULT 0 COMMENT '是否外链',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '权限状态',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updated_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人ID',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT(20) NOT NULL COMMENT '权限ID',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
    `created_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =====================================
-- 初始化角色数据
-- =====================================

-- 超级管理员
INSERT INTO `role` (`role_code`, `role_name`, `description`, `level`, `data_scope`, `status`, `sort_order`, `create_time`)
VALUES ('ADMIN', '超级管理员', '拥有系统所有权限', 1, 'ALL', 'ACTIVE', 1, NOW())
ON DUPLICATE KEY UPDATE `role_name`='超级管理员';

-- 管理员
INSERT INTO `role` (`role_code`, `role_name`, `description`, `level`, `data_scope`, `status`, `sort_order`, `create_time`)
VALUES ('MANAGER', '管理员', '拥有大部分管理权限', 2, 'DEPT', 'ACTIVE', 2, NOW())
ON DUPLICATE KEY UPDATE `role_name`='管理员';

-- 主管
INSERT INTO `role` (`role_code`, `role_name`, `description`, `level`, `data_scope`, `status`, `sort_order`, `create_time`)
VALUES ('SUPERVISOR', '主管', '拥有本部门及下属部门权限', 3, 'DEPT', 'ACTIVE', 3, NOW())
ON DUPLICATE KEY UPDATE `role_name`='主管';

-- 普通用户
INSERT INTO `role` (`role_code`, `role_name`, `description`, `level`, `data_scope`, `status`, `sort_order`, `create_time`)
VALUES ('USER', '普通用户', '拥有基本操作权限', 4, 'SELF', 'ACTIVE', 4, NOW())
ON DUPLICATE KEY UPDATE `role_name`='普通用户';

-- 巡检员
INSERT INTO `role` (`role_code`, `role_name`, `description`, `level`, `data_scope`, `status`, `sort_order`, `create_time`)
VALUES ('INSPECTOR', '巡检员', '负责设备巡检', 4, 'SELF', 'ACTIVE', 5, NOW())
ON DUPLICATE KEY UPDATE `role_name`='巡检员';

-- 维修员
INSERT INTO `role` (`role_code`, `role_name`, `description`, `level`, `data_scope`, `status`, `sort_order`, `create_time`)
VALUES ('MAINTAINER', '维修员', '负责设备维修', 4, 'SELF', 'ACTIVE', 6, NOW())
ON DUPLICATE KEY UPDATE `role_name`='维修员';

-- 查看员
INSERT INTO `role` (`role_code`, `role_name`, `description`, `level`, `data_scope`, `status`, `sort_order`, `create_time`)
VALUES ('VIEWER', '查看员', '只能查看数据', 5, 'ALL', 'ACTIVE', 7, NOW())
ON DUPLICATE KEY UPDATE `role_name`='查看员';

-- =====================================
-- 初始化权限数据
-- =====================================

-- 根菜单
INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'dashboard', '首页', '/dashboard', 'dashboard', 1, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='首页';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'inspection', '巡检管理', '/inspection', 'inspection', 2, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='巡检管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'room', '机房管理', '/room', 'room', 3, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='机房管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'device', '设备管理', '/device', 'device', 4, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='设备管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'shift', '值班管理', '/shift', 'shift', 5, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='值班管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'workorder', '工单管理', '/workorder', 'workorder', 6, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='工单管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'monitor', '监控管理', '/monitor', 'monitor', 7, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='监控管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'alert', '告警管理', '/alert', 'alert', 8, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='告警管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'dooraccess', '门禁管理', '/dooraccess', 'dooraccess', 9, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='门禁管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'fireprotection', '消防管理', '/fireprotection', 'fireprotection', 10, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='消防管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'energy', '能效管理', '/energy', 'energy', 11, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='能效管理';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES (0, 'MENU', 'system', '系统管理', '/system', 'system', 12, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='系统管理';

-- =====================================
-- 初始化系统管理子菜单
-- =====================================

-- 用户管理
INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system'), 'MENU', 'system:user', '用户管理', '/system/user', 'user', 1, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='用户管理';

-- 角色管理
INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system'), 'MENU', 'system:role', '角色管理', '/system/role', 'role', 2, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='角色管理';

-- 权限管理
INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system'), 'MENU', 'system:permission', '权限管理', '/system/permission', 'permission', 3, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='权限管理';

-- 操作日志
INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `path`, `icon`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system'), 'MENU', 'system:auditlog', '操作日志', '/system/auditlog', 'log', 4, 1, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='操作日志';

-- =====================================
-- 初始化按钮权限
-- =====================================

-- 用户管理按钮权限
INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:user'), 'BUTTON', 'user:create', '新增用户', 1, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='新增用户';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:user'), 'BUTTON', 'user:update', '编辑用户', 2, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='编辑用户';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:user'), 'BUTTON', 'user:delete', '删除用户', 3, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='删除用户';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:user'), 'BUTTON', 'user:query', '查询用户', 4, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='查询用户';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:user'), 'BUTTON', 'user:assignRole', '分配角色', 5, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='分配角色';

-- 角色管理按钮权限
INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:role'), 'BUTTON', 'role:create', '新增角色', 1, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='新增角色';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:role'), 'BUTTON', 'role:update', '编辑角色', 2, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='编辑角色';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:role'), 'BUTTON', 'role:delete', '删除角色', 3, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='删除角色';

INSERT INTO `permission` (`parent_id`, `type`, `permission_code`, `permission_name`, `sort_order`, `visible`, `status`, `create_time`)
VALUES ((SELECT id FROM permission WHERE permission_code='system:role'), 'BUTTON', 'role:assignPermission', '分配权限', 4, 0, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE `permission_name`='分配权限';

-- =====================================
-- 为超级管理员分配所有权限
-- =====================================
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 1, id, NOW() FROM `permission` WHERE `status`='ACTIVE';
