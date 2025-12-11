-- =====================================================
-- Document 项目数据库初始化脚本
-- 版本: 2023.0.3.3
-- 数据库: MySQL 5.7+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `document_db` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `document_db`;

-- =====================================================
-- 1. 文档相关表
-- =====================================================

-- 1.1 文档表
DROP TABLE IF EXISTS `document`;
CREATE TABLE `document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '文档标题',
    `doc_number` VARCHAR(100) DEFAULT NULL COMMENT '文档编号',
    `doc_type_id` BIGINT DEFAULT NULL COMMENT '文档类型ID（1:发文,2:收文）',
    `secret_level` INT DEFAULT NULL COMMENT '密级（0:普通,1:秘密,2:机密,3:绝密）',
    `urgency_level` INT DEFAULT NULL COMMENT '紧急程度（0:普通,1:急,2:特急）',
    `content` TEXT COMMENT '文档内容',
    `attachment` VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态（0:草稿,1:审核中,2:已发布,3:已归档）',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_doc_type_id` (`doc_type_id`),
    KEY `idx_status` (`status`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档表-文档抽象基表，存储所有类型文档的通用信息';

-- 1.2 发文信息表
DROP TABLE IF EXISTS `issuance_info`;
CREATE TABLE `issuance_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID（外键关联document.id）',
    `draft_user_id` BIGINT DEFAULT NULL COMMENT '拟稿人ID',
    `draft_dept_id` BIGINT DEFAULT NULL COMMENT '拟稿部门ID',
    `issuing_unit` VARCHAR(200) DEFAULT NULL COMMENT '发文单位',
    `document_category` VARCHAR(100) DEFAULT NULL COMMENT '文种',
    `urgency_level` INT DEFAULT NULL COMMENT '紧急程度',
    `secret_level` INT DEFAULT NULL COMMENT '密级',
    `word_count` INT DEFAULT NULL COMMENT '字数',
    `printing_copies` INT DEFAULT NULL COMMENT '印制份数',
    `main_recipients` VARCHAR(500) DEFAULT NULL COMMENT '主送单位',
    `cc_recipients` VARCHAR(500) DEFAULT NULL COMMENT '抄送单位',
    `keywords` VARCHAR(200) DEFAULT NULL COMMENT '关键词',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_document_id` (`document_id`),
    KEY `idx_draft_user_id` (`draft_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发文信息表-发文文档的扩展信息表';

-- 1.3 收文信息表
DROP TABLE IF EXISTS `receipt_info`;
CREATE TABLE `receipt_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID（外键关联document.id）',
    `receive_date` DATETIME DEFAULT NULL COMMENT '收文日期',
    `sender_unit` VARCHAR(200) DEFAULT NULL COMMENT '来文单位',
    `document_number` VARCHAR(100) DEFAULT NULL COMMENT '来文字号',
    `receive_method` INT DEFAULT NULL COMMENT '收文方式（1:纸质,2:电子,3:其他）',
    `attachments` VARCHAR(500) DEFAULT NULL COMMENT '附件',
    `keywords` VARCHAR(200) DEFAULT NULL COMMENT '关键词',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_document_id` (`document_id`),
    KEY `idx_receive_date` (`receive_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收文信息表-收文文档的扩展信息表';

-- =====================================================
-- 2. 流程定义相关表
-- =====================================================

-- 2.1 流程定义表
DROP TABLE IF EXISTS `flow_definition`;
CREATE TABLE `flow_definition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '流程名称',
    `code` VARCHAR(100) NOT NULL COMMENT '流程编码（唯一）',
    `doc_type_id` BIGINT DEFAULT NULL COMMENT '适用公文类型ID',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
    `version` INT DEFAULT NULL COMMENT '版本号',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态（0:停用,1:启用）',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_doc_type_id` (`doc_type_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程定义表-存储流程模板定义';

-- 2.2 流程节点定义表
DROP TABLE IF EXISTS `flow_node`;
CREATE TABLE `flow_node` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `flow_def_id` BIGINT NOT NULL COMMENT '流程定义ID（外键）',
    `node_name` VARCHAR(200) NOT NULL COMMENT '节点名称',
    `node_type` INT NOT NULL COMMENT '节点类型（1:审批,2:抄送,3:条件,4:自动,5:自由流）',
    `approver_type` INT DEFAULT NULL COMMENT '审批人类型（1:指定人员,2:指定角色,3:部门负责人,4:发起人指定）',
    `approver_value` VARCHAR(500) DEFAULT NULL COMMENT '审批人值（JSON或逗号分隔）',
    `order_num` INT NOT NULL COMMENT '节点顺序',
    `skip_condition` VARCHAR(500) DEFAULT NULL COMMENT '跳过条件（SpEL表达式）',
    `required` INT DEFAULT NULL COMMENT '是否必须（0:可跳过,1:必须）',
    `parallel_mode` INT DEFAULT NULL COMMENT '并行模式（0:串行,1:会签,2:或签）',
    `is_free_flow` INT DEFAULT NULL COMMENT '是否为自由流节点（0:否,1:是）',
    `allow_free_flow` INT DEFAULT NULL COMMENT '是否允许在此节点使用自由流（0:不允许,1:允许）',
    `is_last_node` INT DEFAULT NULL COMMENT '是否为最后一个节点（0:否,1:是）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_flow_def_id` (`flow_def_id`),
    KEY `idx_order_num` (`flow_def_id`, `order_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程节点定义表-存储流程中的节点定义';

-- =====================================================
-- 3. 流程实例相关表
-- =====================================================

-- 3.1 流程实例表
DROP TABLE IF EXISTS `flow_instance`;
CREATE TABLE `flow_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID（外键关联document.id）',
    `flow_def_id` BIGINT DEFAULT NULL COMMENT '流程定义ID（外键关联flow_definition.id）',
    `flow_type` INT DEFAULT NULL COMMENT '流程类型（1:发文,2:收文）',
    `status` INT NOT NULL DEFAULT 0 COMMENT '流程状态（0:进行中,1:已完成,2:已终止）',
    `current_node_id` BIGINT DEFAULT NULL COMMENT '当前节点ID',
    `parent_flow_instance_id` BIGINT DEFAULT NULL COMMENT '父流程实例ID（用于子流程）',
    `flow_mode` INT DEFAULT NULL COMMENT '流程模式（1:固定流,2:自由流,3:混合流）',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_flow_def_id` (`flow_def_id`),
    KEY `idx_status` (`status`),
    KEY `idx_parent_flow_instance_id` (`parent_flow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程实例表-存储流程运行时的实例信息';

-- 3.2 节点实例表
DROP TABLE IF EXISTS `flow_node_instance`;
CREATE TABLE `flow_node_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `flow_instance_id` BIGINT NOT NULL COMMENT '流程实例ID（外键）',
    `node_id` BIGINT NOT NULL COMMENT '节点定义ID（外键）',
    `approver_id` BIGINT NOT NULL COMMENT '审批人ID',
    `approver_dept_id` BIGINT DEFAULT NULL COMMENT '审批人部门ID',
    `status` INT NOT NULL DEFAULT 0 COMMENT '节点状态（0:待处理,1:处理中,2:已完成,3:已拒绝,4:已跳过）',
    `comments` VARCHAR(1000) DEFAULT NULL COMMENT '审批意见',
    `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_flow_instance_id` (`flow_instance_id`),
    KEY `idx_node_id` (`node_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节点实例表-存储流程运行时的节点实例信息';

-- =====================================================
-- 4. 自由流相关表
-- =====================================================

-- 4.1 发送动作表
DROP TABLE IF EXISTS `flow_action`;
CREATE TABLE `flow_action` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `action_code` VARCHAR(100) NOT NULL COMMENT '动作编码（唯一）',
    `action_name` VARCHAR(200) NOT NULL COMMENT '动作名称',
    `action_type` INT NOT NULL COMMENT '动作类型（1:单位内办理,2:核稿,3:转外单位办理,4:返回）',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `enabled` INT NOT NULL DEFAULT 1 COMMENT '是否启用（0:停用,1:启用）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_action_code` (`action_code`),
    KEY `idx_action_type` (`action_type`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发送动作表-存储自由流中可用的发送动作定义';

-- 4.2 动作规则表
DROP TABLE IF EXISTS `flow_action_rule`;
CREATE TABLE `flow_action_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `action_id` BIGINT NOT NULL COMMENT '动作ID（外键）',
    `document_status` INT NOT NULL COMMENT '文件状态（0:草稿,1:审核中,2:已发布,3:已归档）',
    `user_role` VARCHAR(200) DEFAULT NULL COMMENT '用户角色（支持多个，逗号分隔，*表示所有角色）',
    `dept_id` BIGINT DEFAULT NULL COMMENT '部门ID（可选，限制特定部门）',
    `priority` INT DEFAULT NULL COMMENT '优先级（数字越大优先级越高）',
    `enabled` INT NOT NULL DEFAULT 1 COMMENT '是否启用（0:停用,1:启用）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_action_id` (`action_id`),
    KEY `idx_document_status` (`document_status`),
    KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动作规则表-定义在什么条件下可以使用某个发送动作';

-- 4.3 审批人选择范围表
DROP TABLE IF EXISTS `approver_scope`;
CREATE TABLE `approver_scope` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `action_id` BIGINT NOT NULL COMMENT '动作ID（外键）',
    `scope_type` INT NOT NULL COMMENT '范围类型（1:部门,2:人员,3:部门+人员）',
    `dept_ids` VARCHAR(1000) DEFAULT NULL COMMENT '可选部门ID列表（JSON数组字符串）',
    `user_ids` VARCHAR(1000) DEFAULT NULL COMMENT '可选人员ID列表（JSON数组字符串）',
    `role_codes` VARCHAR(500) DEFAULT NULL COMMENT '可选角色编码（逗号分隔）',
    `allow_custom` INT DEFAULT NULL COMMENT '是否允许自定义选择（0:不允许,1:允许）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_action_id` (`action_id`),
    KEY `idx_scope_type` (`scope_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批人选择范围表-定义每个发送动作对应的审批人选择范围';

-- 4.4 自由流节点实例扩展表
DROP TABLE IF EXISTS `free_flow_node_instance`;
CREATE TABLE `free_flow_node_instance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `node_instance_id` BIGINT NOT NULL COMMENT '节点实例ID（外键，唯一）',
    `action_id` BIGINT NOT NULL COMMENT '发送动作ID（外键）',
    `action_name` VARCHAR(200) DEFAULT NULL COMMENT '动作名称',
    `selected_dept_ids` VARCHAR(1000) DEFAULT NULL COMMENT '选择的部门ID列表（JSON数组字符串）',
    `selected_user_ids` VARCHAR(1000) DEFAULT NULL COMMENT '选择的人员ID列表（JSON数组字符串）',
    `custom_comment` VARCHAR(1000) DEFAULT NULL COMMENT '自定义备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_node_instance_id` (`node_instance_id`),
    KEY `idx_action_id` (`action_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自由流节点实例扩展表-记录自由流节点的额外信息';

-- =====================================================
-- 5. 任务相关表
-- =====================================================

-- 5.1 待办任务表
DROP TABLE IF EXISTS `todo_task`;
CREATE TABLE `todo_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '公文ID（外键）',
    `flow_instance_id` BIGINT NOT NULL COMMENT '流程实例ID（外键）',
    `node_instance_id` BIGINT NOT NULL COMMENT '节点实例ID（外键）',
    `assignee_id` BIGINT NOT NULL COMMENT '处理人ID',
    `title` VARCHAR(500) DEFAULT NULL COMMENT '待办标题',
    `content` TEXT COMMENT '待办内容',
    `task_type` INT NOT NULL DEFAULT 1 COMMENT '任务类型（1:用户任务,2:JAVA任务,3:其他任务）',
    `priority` INT DEFAULT NULL COMMENT '优先级（0:普通,1:紧急,2:特急）',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态（0:待处理,1:已处理,2:已取消）',
    `due_date` DATETIME DEFAULT NULL COMMENT '截止时间',
    `handled_at` DATETIME DEFAULT NULL COMMENT '处理时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_flow_instance_id` (`flow_instance_id`),
    KEY `idx_node_instance_id` (`node_instance_id`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_status` (`status`),
    KEY `idx_task_type` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='待办任务表-存储待办任务信息';

-- 5.2 已办任务表
DROP TABLE IF EXISTS `done_task`;
CREATE TABLE `done_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id` BIGINT NOT NULL COMMENT '公文ID（外键）',
    `flow_instance_id` BIGINT NOT NULL COMMENT '流程实例ID（外键）',
    `node_instance_id` BIGINT NOT NULL COMMENT '节点实例ID（外键）',
    `handler_id` BIGINT NOT NULL COMMENT '处理人ID',
    `title` VARCHAR(500) DEFAULT NULL COMMENT '已办标题',
    `task_type` INT NOT NULL DEFAULT 1 COMMENT '任务类型（1:用户任务,2:JAVA任务,3:其他任务）',
    `action` VARCHAR(50) DEFAULT NULL COMMENT '操作类型（approve,reject,forward,return,delegate）',
    `comments` VARCHAR(1000) DEFAULT NULL COMMENT '处理意见',
    `handled_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '处理时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`),
    KEY `idx_flow_instance_id` (`flow_instance_id`),
    KEY `idx_node_instance_id` (`node_instance_id`),
    KEY `idx_handler_id` (`handler_id`),
    KEY `idx_task_type` (`task_type`),
    KEY `idx_handled_at` (`handled_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='已办任务表-存储已办任务信息';

-- =====================================================
-- 6. 扩展功能表
-- =====================================================

-- 6.1 承办记录表
DROP TABLE IF EXISTS `handling`;
CREATE TABLE `handling` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `flow_instance_id` BIGINT NOT NULL COMMENT '流程实例ID（外键）',
    `node_instance_id` BIGINT DEFAULT NULL COMMENT '节点实例ID（外键）',
    `handler_id` BIGINT NOT NULL COMMENT '承办人ID',
    `handler_dept_id` BIGINT DEFAULT NULL COMMENT '承办人部门ID',
    `handling_content` VARCHAR(2000) DEFAULT NULL COMMENT '承办内容',
    `handling_result` VARCHAR(1000) DEFAULT NULL COMMENT '承办结果',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `status` INT NOT NULL DEFAULT 0 COMMENT '状态（0:进行中,1:已完成,2:已退回）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_flow_instance_id` (`flow_instance_id`),
    KEY `idx_node_instance_id` (`node_instance_id`),
    KEY `idx_handler_id` (`handler_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='承办记录表-存储流程中的承办记录信息';

-- 6.2 外单位签收登记表
DROP TABLE IF EXISTS `external_sign_receipt`;
CREATE TABLE `external_sign_receipt` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `flow_instance_id` BIGINT NOT NULL COMMENT '流程实例ID（外键）',
    `external_unit_id` BIGINT NOT NULL COMMENT '外单位ID',
    `receiver_name` VARCHAR(100) DEFAULT NULL COMMENT '接收人姓名',
    `receipt_time` DATETIME DEFAULT NULL COMMENT '签收时间',
    `receipt_status` INT NOT NULL DEFAULT 0 COMMENT '签收状态（0:未签收,1:已签收,2:拒签）',
    `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_flow_instance_id` (`flow_instance_id`),
    KEY `idx_external_unit_id` (`external_unit_id`),
    KEY `idx_receipt_status` (`receipt_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外单位签收登记表-存储外单位签收登记信息';

-- =====================================================
-- 初始化数据（可选）
-- =====================================================

-- 初始化发送动作数据
INSERT INTO `flow_action` (`action_code`, `action_name`, `action_type`, `description`, `enabled`) VALUES
('UNIT_HANDLE', '单位内办理', 1, '在单位内部办理', 1),
('REVIEW', '核稿', 2, '核稿处理', 1),
('EXTERNAL', '转外单位办理', 3, '转外单位办理', 1),
('RETURN', '返回', 4, '返回操作', 1);

-- =====================================================
-- 脚本执行完成
-- =====================================================

