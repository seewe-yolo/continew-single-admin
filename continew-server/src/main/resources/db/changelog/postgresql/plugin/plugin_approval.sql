-- liquibase formatted sql

-- changeset seewis:plugin_approval_1
-- comment 初始化顺序审批插件表、菜单和角色权限
CREATE TABLE "approval_definition" (
    "id" BIGINT NOT NULL,
    "code" VARCHAR(50) NOT NULL,
    "name" VARCHAR(100) NOT NULL,
    "category" VARCHAR(50),
    "flow_config" JSONB NOT NULL,
    "form_config" JSONB,
    "description" VARCHAR(500),
    "status" SMALLINT NOT NULL DEFAULT 1,
    "create_user" BIGINT,
    "create_time" TIMESTAMP,
    "update_user" BIGINT,
    "update_time" TIMESTAMP,
    "deleted" BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_approval_definition_code_deleted" UNIQUE ("code", "deleted")
);
CREATE INDEX "idx_approval_definition_status" ON "approval_definition" ("status");
COMMENT ON TABLE "approval_definition" IS '审批定义表';
COMMENT ON COLUMN "approval_definition"."flow_config" IS '流程配置';
COMMENT ON COLUMN "approval_definition"."form_config" IS '申请表单配置';

CREATE TABLE "approval_instance" (
    "id" BIGINT NOT NULL,
    "definition_code" VARCHAR(50) NOT NULL,
    "business_key" VARCHAR(100) NOT NULL,
    "business_type" VARCHAR(50) NOT NULL,
    "title" VARCHAR(200) NOT NULL,
    "current_node_id" VARCHAR(50),
    "status" VARCHAR(20) NOT NULL,
    "result" VARCHAR(20),
    "form_data" JSONB,
    "form_config" JSONB,
    "flow_config" TEXT NOT NULL,
    "start_user" BIGINT NOT NULL,
    "start_time" TIMESTAMP NOT NULL,
    "end_time" TIMESTAMP,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_approval_instance_business" ON "approval_instance" ("business_key", "business_type");
CREATE INDEX "idx_approval_instance_status" ON "approval_instance" ("status");
CREATE INDEX "idx_approval_instance_start_user" ON "approval_instance" ("start_user");
COMMENT ON TABLE "approval_instance" IS '审批实例表';
COMMENT ON COLUMN "approval_instance"."form_config" IS '申请表单配置快照';
COMMENT ON COLUMN "approval_instance"."flow_config" IS '流程配置快照';

CREATE TABLE "approval_task" (
    "id" BIGINT NOT NULL,
    "instance_id" BIGINT NOT NULL,
    "node_id" VARCHAR(50) NOT NULL,
    "node_name" VARCHAR(100) NOT NULL,
    "assignee" BIGINT NOT NULL,
    "status" VARCHAR(20) NOT NULL,
    "opinion" VARCHAR(500),
    "complete_time" TIMESTAMP,
    "create_time" TIMESTAMP,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_approval_task_instance" ON "approval_task" ("instance_id");
CREATE INDEX "idx_approval_task_assignee_status" ON "approval_task" ("assignee", "status");
COMMENT ON TABLE "approval_task" IS '审批任务表';

CREATE TABLE "approval_history" (
    "id" BIGINT NOT NULL,
    "instance_id" BIGINT NOT NULL,
    "task_id" BIGINT,
    "node_id" VARCHAR(50),
    "node_name" VARCHAR(100),
    "operator" BIGINT NOT NULL,
    "action" VARCHAR(20) NOT NULL,
    "opinion" VARCHAR(500),
    "create_time" TIMESTAMP NOT NULL,
    PRIMARY KEY ("id")
);
CREATE INDEX "idx_approval_history_instance" ON "approval_history" ("instance_id");
COMMENT ON TABLE "approval_history" IS '审批历史表';

INSERT INTO "sys_menu"
("id", "title", "parent_id", "type", "path", "name", "component", "redirect", "icon", "is_external", "is_cache", "is_hidden", "permission", "sort", "status", "create_user", "create_time")
VALUES
(10000, '审批中心', 0, 1, '/approval', 'Approval', 'Layout', '/approval/application', 'mdi:clipboard-check-outline', false, false, false, NULL, 10, 1, 1, NOW()),
(10010, '审批定义', 10000, 2, '/approval/definition', 'ApprovalDefinition', 'approval/definition/index', NULL, 'mdi:file-tree-outline', false, false, false, NULL, 1, 1, 1, NOW()),
(10011, '列表', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:list', 1, 1, 1, NOW()),
(10012, '详情', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:get', 2, 1, 1, NOW()),
(10013, '新增', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:create', 3, 1, 1, NOW()),
(10014, '修改', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:update', 4, 1, 1, NOW()),
(10015, '删除', 10010, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:definition:delete', 5, 1, 1, NOW()),
(10030, '我的申请', 10000, 2, '/approval/application', 'ApprovalApplication', 'approval/application/index', NULL, 'mdi:file-document-outline', false, false, false, NULL, 2, 1, 1, NOW()),
(10031, '列表', 10030, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:list', 1, 1, 1, NOW()),
(10032, '详情', 10030, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:get', 2, 1, 1, NOW()),
(10033, '撤销', 10030, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:cancel', 3, 1, 1, NOW()),
(10034, '发起', 10030, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:instance:start', 4, 1, 1, NOW()),
(10040, '我的待办', 10000, 2, '/approval/task', 'ApprovalTask', 'approval/task/index', NULL, 'mdi:clipboard-check-outline', false, false, false, NULL, 3, 1, 1, NOW()),
(10041, '列表', 10040, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:task:list', 1, 1, 1, NOW()),
(10042, '同意', 10040, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:task:approve', 2, 1, 1, NOW()),
(10043, '拒绝', 10040, 3, NULL, NULL, NULL, NULL, NULL, false, false, false, 'approval:task:reject', 3, 1, 1, NOW());

INSERT INTO "sys_role_menu" ("role_id", "menu_id")
SELECT r."id", m."id" FROM "sys_role" r CROSS JOIN "sys_menu" m
WHERE r."code" IN ('super_admin', 'sys_admin') AND r."deleted" = 0
AND m."id" IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043)
AND NOT EXISTS (SELECT 1 FROM "sys_role_menu" rm WHERE rm."role_id" = r."id" AND rm."menu_id" = m."id");

INSERT INTO "sys_role_menu" ("role_id", "menu_id")
SELECT r."id", m."id" FROM "sys_role" r CROSS JOIN "sys_menu" m
WHERE r."code" = 'general' AND r."deleted" = 0
AND m."id" IN (10000, 10011, 10012, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043)
AND NOT EXISTS (SELECT 1 FROM "sys_role_menu" rm WHERE rm."role_id" = r."id" AND rm."menu_id" = m."id");

-- rollback DELETE FROM "sys_role_menu" WHERE "menu_id" IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043);
-- rollback DELETE FROM "sys_menu" WHERE "id" IN (10000, 10010, 10011, 10012, 10013, 10014, 10015, 10030, 10031, 10032, 10033, 10034, 10040, 10041, 10042, 10043);
