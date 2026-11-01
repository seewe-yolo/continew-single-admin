-- liquibase formatted sql

-- changeset seewis:plugin_approval_1
-- comment: 初始化顺序审批插件表、菜单和角色权限
CREATE TABLE `approval_definition` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `code` VARCHAR(50) NOT NULL COMMENT '审批编码',
    `name` VARCHAR(100) NOT NULL COMMENT '审批名称',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '审批分类',
    `flow_config` JSON NOT NULL COMMENT '流程配置',
    `form_config` JSON DEFAULT NULL COMMENT '申请表单配置',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（1启用 0禁用）',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_user` BIGINT DEFAULT NULL COMMENT '修改人',
    `update_time` DATETIME DEFAULT NULL COMMENT '修改时间',
    `deleted` BIGINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_approval_definition_code_deleted` (`code`, `deleted`),
    KEY `idx_approval_definition_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批定义表';

CREATE TABLE `approval_instance` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `definition_code` VARCHAR(50) NOT NULL COMMENT '审批定义编码',
    `business_key` VARCHAR(100) NOT NULL COMMENT '业务主键',
    `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型',
    `title` VARCHAR(200) NOT NULL COMMENT '审批标题',
    `current_node_id` VARCHAR(50) DEFAULT NULL COMMENT '当前节点ID',
    `status` VARCHAR(20) NOT NULL COMMENT '状态',
    `result` VARCHAR(20) DEFAULT NULL COMMENT '审批结果',
    `form_data` JSON DEFAULT NULL COMMENT '表单数据',
    `form_config` JSON DEFAULT NULL COMMENT '申请表单配置快照',
    `flow_config` TEXT NOT NULL COMMENT '流程配置快照',
    `start_user` BIGINT NOT NULL COMMENT '发起人',
    `start_time` DATETIME NOT NULL COMMENT '发起时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`id`),
    KEY `idx_approval_instance_business` (`business_key`, `business_type`),
    KEY `idx_approval_instance_status` (`status`),
    KEY `idx_approval_instance_start_user` (`start_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

CREATE TABLE `approval_task` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `instance_id` BIGINT NOT NULL COMMENT '审批实例ID',
    `node_id` VARCHAR(50) NOT NULL COMMENT '节点ID',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `assignee` BIGINT NOT NULL COMMENT '审批人',
    `status` VARCHAR(20) NOT NULL COMMENT '状态',
    `opinion` VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_approval_task_instance` (`instance_id`),
    KEY `idx_approval_task_assignee_status` (`assignee`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

CREATE TABLE `approval_history` (
    `id` BIGINT NOT NULL COMMENT 'ID',
    `instance_id` BIGINT NOT NULL COMMENT '审批实例ID',
    `task_id` BIGINT DEFAULT NULL COMMENT '任务ID',
    `node_id` VARCHAR(50) DEFAULT NULL COMMENT '节点ID',
    `node_name` VARCHAR(100) DEFAULT NULL COMMENT '节点名称',
    `operator` BIGINT NOT NULL COMMENT '操作人',
    `action` VARCHAR(20) NOT NULL COMMENT '操作',
    `opinion` VARCHAR(500) DEFAULT NULL COMMENT '意见',
    `create_time` DATETIME NOT NULL COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_approval_history_instance` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批历史表';

INSERT INTO `sys_menu`
(`id`, `title`, `parent_id`, `type`, `path`, `name`, `component`, `redirect`, `icon`, `is_external`, `is_cache`, `is_hidden`, `permission`, `sort`, `status`, `create_user`, `create_time`)
VALUES
(10000, '审批中心', 0, 1, '/approval', 'Approval', 'Layout', '/approval/application', 'mdi:clipboard-check-outline', b'0', b'0', b'0', NULL, 10, 1, 1, NOW()),
(10010, '审批定义', 10000, 2, '/approval/definition', 'ApprovalDefinition', 'approval/definition/index', NULL, 'mdi:file-tree-outline', b'0', b'0', b'0', NULL, 1, 1, 1, NOW()),
(10011, '列表', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:list', 1, 1, 1, NOW()),
(10012, '详情', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:get', 2, 1, 1, NOW()),
(10013, '新增', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:create', 3, 1, 1, NOW()),
(10014, '修改', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:update', 4, 1, 1, NOW()),
(10015, '删除', 10010, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:definition:delete', 5, 1, 1, NOW()),
(10030, '我的申请', 10000, 2, '/approval/application', 'ApprovalApplication', 'approval/application/index', NULL, 'mdi:file-document-outline', b'0', b'0', b'0', NULL, 2, 1, 1, NOW()),
(10031, '列表', 10030, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:list', 1, 1, 1, NOW()),
(10032, '详情', 10030, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:get', 2, 1, 1, NOW()),
(10033, '撤销', 10030, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:cancel', 3, 1, 1, NOW()),
(10034, '发起', 10030, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:instance:start', 4, 1, 1, NOW()),
(10040, '我的待办', 10000, 2, '/approval/task', 'ApprovalTask', 'approval/task/index', NULL, 'mdi:clipboard-check-outline', b'0', b'0', b'0', NULL, 3, 1, 1, NOW()),
(10041, '列表', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:task:list', 1, 1, 1, NOW()),
(10042, '同意', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:task:approve', 2, 1, 1, NOW()),
(10043, '拒绝', 10040, 3, NULL, NULL, NULL, NULL, NULL, b'0', b'0', b'0', 'approval:task:reject', 3, 1, 1, NOW());

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id` FROM `sys_role` r CROSS JOIN `sys_menu` m
WHERE r.`code` IN ('super_admin', 'sys_admin') AND r.`deleted` = 0
AND m.`id` IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043)
AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.`id`, m.`id` FROM `sys_role` r CROSS JOIN `sys_menu` m
WHERE r.`code` = 'general' AND r.`deleted` = 0
AND m.`id` IN (10000, 10011, 10012, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043)
AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = r.`id` AND rm.`menu_id` = m.`id`);

-- rollback DELETE FROM `sys_role_menu` WHERE `menu_id` IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043);
-- rollback DELETE FROM `sys_menu` WHERE `id` IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043);
