# 数据库设计文档

## 📋 概述

本文档详细描述了 Document 项目的数据库表结构设计，包括所有实体表、字段定义、索引、外键关系等。

**版本**: 2023.0.3.3  
**最后更新**: 2024年

---

## 📊 数据库表结构

### 1. 文档相关表

#### 1.1 document（文档表）

文档抽象基表，存储所有类型文档的通用信息。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| title | VARCHAR | 255 | YES | NO | 文档标题 |
| doc_number | VARCHAR | 100 | YES | NO | 文档编号 |
| doc_type_id | BIGINT | - | YES | NO | 文档类型ID（1:发文,2:收文） |
| secret_level | INT | - | YES | NO | 密级（0:普通,1:秘密,2:机密,3:绝密） |
| urgency_level | INT | - | YES | NO | 紧急程度（0:普通,1:急,2:特急） |
| content | TEXT | - | YES | NO | 文档内容 |
| attachment | VARCHAR | 500 | YES | NO | 附件路径 |
| status | INT | - | NO | NO | 状态（0:草稿,1:审核中,2:已发布,3:已归档） |
| creator_id | BIGINT | - | YES | NO | 创建人ID |
| dept_id | BIGINT | - | YES | NO | 部门ID |
| publish_time | DATETIME | - | YES | NO | 发布时间 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_doc_type_id` (`doc_type_id`)
- INDEX `idx_status` (`status`)
- INDEX `idx_creator_id` (`creator_id`)
- INDEX `idx_created_at` (`created_at`)

**状态常量**:
- `STATUS_DRAFT = 0`: 草稿
- `STATUS_REVIEWING = 1`: 审核中
- `STATUS_PUBLISHED = 2`: 已发布
- `STATUS_ARCHIVED = 3`: 已归档

---

#### 1.2 issuance_info（发文信息表）

发文文档的扩展信息表，关联到 document 表。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| document_id | BIGINT | - | NO | NO | 文档ID（外键关联document.id） |
| draft_user_id | BIGINT | - | YES | NO | 拟稿人ID |
| draft_dept_id | BIGINT | - | YES | NO | 拟稿部门ID |
| issuing_unit | VARCHAR | 200 | YES | NO | 发文单位 |
| document_category | VARCHAR | 100 | YES | NO | 文种 |
| urgency_level | INT | - | YES | NO | 紧急程度 |
| secret_level | INT | - | YES | NO | 密级 |
| word_count | INT | - | YES | NO | 字数 |
| printing_copies | INT | - | YES | NO | 印制份数 |
| main_recipients | VARCHAR | 500 | YES | NO | 主送单位 |
| cc_recipients | VARCHAR | 500 | YES | NO | 抄送单位 |
| keywords | VARCHAR | 200 | YES | NO | 关键词 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_document_id` (`document_id`)
- INDEX `idx_draft_user_id` (`draft_user_id`)

**关系**:
- `document_id` → `document.id` (1:1)

---

#### 1.3 receipt_info（收文信息表）

收文文档的扩展信息表，关联到 document 表。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| document_id | BIGINT | - | NO | NO | 文档ID（外键关联document.id） |
| receive_date | DATETIME | - | YES | NO | 收文日期 |
| sender_unit | VARCHAR | 200 | YES | NO | 来文单位 |
| document_number | VARCHAR | 100 | YES | NO | 来文字号 |
| receive_method | INT | - | YES | NO | 收文方式（1:纸质,2:电子,3:其他） |
| attachments | VARCHAR | 500 | YES | NO | 附件 |
| keywords | VARCHAR | 200 | YES | NO | 关键词 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_document_id` (`document_id`)
- INDEX `idx_receive_date` (`receive_date`)

**关系**:
- `document_id` → `document.id` (1:1)

**收文方式常量**:
- `METHOD_PAPER = 1`: 纸质
- `METHOD_ELECTRONIC = 2`: 电子
- `METHOD_OTHER = 3`: 其他

---

### 2. 流程定义相关表

#### 2.1 flow_definition（流程定义表）

存储流程模板定义。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| name | VARCHAR | 200 | NO | NO | 流程名称 |
| code | VARCHAR | 100 | NO | NO | 流程编码（唯一） |
| doc_type_id | BIGINT | - | YES | NO | 适用公文类型ID |
| description | VARCHAR | 500 | YES | NO | 流程描述 |
| version | INT | - | YES | NO | 版本号 |
| status | INT | - | NO | NO | 状态（0:停用,1:启用） |
| creator_id | BIGINT | - | YES | NO | 创建人ID |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_code` (`code`)
- INDEX `idx_doc_type_id` (`doc_type_id`)
- INDEX `idx_status` (`status`)

**状态常量**:
- `STATUS_DISABLED = 0`: 停用
- `STATUS_ENABLED = 1`: 启用

---

#### 2.2 flow_node（流程节点定义表）

存储流程中的节点定义。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| flow_def_id | BIGINT | - | NO | NO | 流程定义ID（外键） |
| node_name | VARCHAR | 200 | NO | NO | 节点名称 |
| node_type | INT | - | NO | NO | 节点类型（1:审批,2:抄送,3:条件,4:自动,5:自由流） |
| approver_type | INT | - | YES | NO | 审批人类型（1:指定人员,2:指定角色,3:部门负责人,4:发起人指定） |
| approver_value | VARCHAR | 500 | YES | NO | 审批人值（JSON或逗号分隔） |
| order_num | INT | - | NO | NO | 节点顺序 |
| skip_condition | VARCHAR | 500 | YES | NO | 跳过条件（SpEL表达式） |
| required | INT | - | YES | NO | 是否必须（0:可跳过,1:必须） |
| parallel_mode | INT | - | YES | NO | 并行模式（0:串行,1:会签,2:或签） |
| is_free_flow | INT | - | YES | NO | 是否为自由流节点（0:否,1:是） |
| allow_free_flow | INT | - | YES | NO | 是否允许在此节点使用自由流（0:不允许,1:允许） |
| is_last_node | INT | - | YES | NO | 是否为最后一个节点（0:否,1:是） |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_flow_def_id` (`flow_def_id`)
- INDEX `idx_order_num` (`flow_def_id`, `order_num`)

**关系**:
- `flow_def_id` → `flow_definition.id` (N:1)

**节点类型常量**:
- `NODE_TYPE_APPROVAL = 1`: 审批节点
- `NODE_TYPE_NOTIFY = 2`: 抄送节点
- `NODE_TYPE_CONDITION = 3`: 条件节点
- `NODE_TYPE_AUTO = 4`: 自动节点
- `NODE_TYPE_FREE_FLOW = 5`: 自由流节点

**审批人类型常量**:
- `APPROVER_TYPE_USER = 1`: 指定人员
- `APPROVER_TYPE_ROLE = 2`: 指定角色
- `APPROVER_TYPE_DEPT_LEADER = 3`: 指定部门负责人
- `APPROVER_TYPE_INITIATOR = 4`: 发起人指定

**并行模式常量**:
- `PARALLEL_MODE_SERIAL = 0`: 串行
- `PARALLEL_MODE_PARALLEL_ALL = 1`: 并行-会签（所有节点都完成）
- `PARALLEL_MODE_PARALLEL_ANY = 2`: 并行-或签（任一节点完成）

---

### 3. 流程实例相关表

#### 3.1 flow_instance（流程实例表）

存储流程运行时的实例信息。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| document_id | BIGINT | - | NO | NO | 文档ID（外键关联document.id） |
| flow_def_id | BIGINT | - | YES | NO | 流程定义ID（外键关联flow_definition.id） |
| flow_type | INT | - | YES | NO | 流程类型（1:发文,2:收文） |
| status | INT | - | NO | NO | 流程状态（0:进行中,1:已完成,2:已终止） |
| current_node_id | BIGINT | - | YES | NO | 当前节点ID |
| parent_flow_instance_id | BIGINT | - | YES | NO | 父流程实例ID（用于子流程） |
| flow_mode | INT | - | YES | NO | 流程模式（1:固定流,2:自由流,3:混合流） |
| start_time | DATETIME | - | YES | NO | 开始时间 |
| end_time | DATETIME | - | YES | NO | 结束时间 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_document_id` (`document_id`)
- INDEX `idx_flow_def_id` (`flow_def_id`)
- INDEX `idx_status` (`status`)
- INDEX `idx_parent_flow_instance_id` (`parent_flow_instance_id`)

**关系**:
- `document_id` → `document.id` (N:1)
- `flow_def_id` → `flow_definition.id` (N:1)
- `parent_flow_instance_id` → `flow_instance.id` (N:1, 自关联)

**流程类型常量**:
- `TYPE_ISSUANCE = 1`: 发文
- `TYPE_RECEIPT = 2`: 收文

**流程模式常量**:
- `FLOW_MODE_FIXED = 1`: 固定流
- `FLOW_MODE_FREE = 2`: 自由流
- `FLOW_MODE_MIXED = 3`: 混合流

**流程状态常量**:
- `STATUS_PROCESSING = 0`: 进行中
- `STATUS_COMPLETED = 1`: 已完成
- `STATUS_TERMINATED = 2`: 已终止

---

#### 3.2 flow_node_instance（节点实例表）

存储流程运行时的节点实例信息。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| flow_instance_id | BIGINT | - | NO | NO | 流程实例ID（外键） |
| node_id | BIGINT | - | NO | NO | 节点定义ID（外键） |
| approver_id | BIGINT | - | NO | NO | 审批人ID |
| approver_dept_id | BIGINT | - | YES | NO | 审批人部门ID |
| status | INT | - | NO | NO | 节点状态（0:待处理,1:处理中,2:已完成,3:已拒绝,4:已跳过） |
| comments | VARCHAR | 1000 | YES | NO | 审批意见 |
| handled_at | DATETIME | - | YES | NO | 处理时间 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_flow_instance_id` (`flow_instance_id`)
- INDEX `idx_node_id` (`node_id`)
- INDEX `idx_approver_id` (`approver_id`)
- INDEX `idx_status` (`status`)

**关系**:
- `flow_instance_id` → `flow_instance.id` (N:1)
- `node_id` → `flow_node.id` (N:1)

**节点状态常量**:
- `STATUS_PENDING = 0`: 待处理
- `STATUS_PROCESSING = 1`: 处理中
- `STATUS_COMPLETED = 2`: 已完成
- `STATUS_REJECTED = 3`: 已拒绝
- `STATUS_SKIPPED = 4`: 已跳过

---

### 4. 自由流相关表

#### 4.1 flow_action（发送动作表）

存储自由流中可用的发送动作定义。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| action_code | VARCHAR | 100 | NO | NO | 动作编码（唯一） |
| action_name | VARCHAR | 200 | NO | NO | 动作名称 |
| action_type | INT | - | NO | NO | 动作类型（1:单位内办理,2:核稿,3:转外单位办理,4:返回） |
| description | VARCHAR | 500 | YES | NO | 描述 |
| icon | VARCHAR | 100 | YES | NO | 图标 |
| enabled | INT | - | NO | NO | 是否启用（0:停用,1:启用） |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_action_code` (`action_code`)
- INDEX `idx_action_type` (`action_type`)
- INDEX `idx_enabled` (`enabled`)

**动作类型常量**:
- `TYPE_UNIT_HANDLE = 1`: 单位内办理
- `TYPE_REVIEW = 2`: 核稿
- `TYPE_EXTERNAL = 3`: 转外单位办理
- `TYPE_RETURN = 4`: 返回

---

#### 4.2 flow_action_rule（动作规则表）

定义在什么条件下可以使用某个发送动作。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| action_id | BIGINT | - | NO | NO | 动作ID（外键） |
| document_status | INT | - | NO | NO | 文件状态（0:草稿,1:审核中,2:已发布,3:已归档） |
| user_role | VARCHAR | 200 | YES | NO | 用户角色（支持多个，逗号分隔，*表示所有角色） |
| dept_id | BIGINT | - | YES | NO | 部门ID（可选，限制特定部门） |
| priority | INT | - | YES | NO | 优先级（数字越大优先级越高） |
| enabled | INT | - | NO | NO | 是否启用（0:停用,1:启用） |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_action_id` (`action_id`)
- INDEX `idx_document_status` (`document_status`)
- INDEX `idx_enabled` (`enabled`)

**关系**:
- `action_id` → `flow_action.id` (N:1)

---

#### 4.3 approver_scope（审批人选择范围表）

定义每个发送动作对应的审批人选择范围。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| action_id | BIGINT | - | NO | NO | 动作ID（外键） |
| scope_type | INT | - | NO | NO | 范围类型（1:部门,2:人员,3:部门+人员） |
| dept_ids | VARCHAR | 1000 | YES | NO | 可选部门ID列表（JSON数组字符串） |
| user_ids | VARCHAR | 1000 | YES | NO | 可选人员ID列表（JSON数组字符串） |
| role_codes | VARCHAR | 500 | YES | NO | 可选角色编码（逗号分隔） |
| allow_custom | INT | - | YES | NO | 是否允许自定义选择（0:不允许,1:允许） |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_action_id` (`action_id`)
- INDEX `idx_scope_type` (`scope_type`)

**关系**:
- `action_id` → `flow_action.id` (N:1)

**范围类型常量**:
- `SCOPE_TYPE_DEPT = 1`: 部门
- `SCOPE_TYPE_USER = 2`: 人员
- `SCOPE_TYPE_DEPT_USER = 3`: 部门+人员

---

#### 4.4 free_flow_node_instance（自由流节点实例扩展表）

记录自由流节点的额外信息（发送动作、选择的审批人等）。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| node_instance_id | BIGINT | - | NO | NO | 节点实例ID（外键，唯一） |
| action_id | BIGINT | - | NO | NO | 发送动作ID（外键） |
| action_name | VARCHAR | 200 | YES | NO | 动作名称 |
| selected_dept_ids | VARCHAR | 1000 | YES | NO | 选择的部门ID列表（JSON数组字符串） |
| selected_user_ids | VARCHAR | 1000 | YES | NO | 选择的人员ID列表（JSON数组字符串） |
| custom_comment | VARCHAR | 1000 | YES | NO | 自定义备注 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_node_instance_id` (`node_instance_id`)
- INDEX `idx_action_id` (`action_id`)

**关系**:
- `node_instance_id` → `flow_node_instance.id` (1:1)
- `action_id` → `flow_action.id` (N:1)

---

### 5. 任务相关表

#### 5.1 todo_task（待办任务表）

存储待办任务信息。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| document_id | BIGINT | - | NO | NO | 公文ID（外键） |
| flow_instance_id | BIGINT | - | NO | NO | 流程实例ID（外键） |
| node_instance_id | BIGINT | - | NO | NO | 节点实例ID（外键） |
| assignee_id | BIGINT | - | NO | NO | 处理人ID |
| title | VARCHAR | 500 | YES | NO | 待办标题 |
| content | TEXT | - | YES | NO | 待办内容 |
| task_type | INT | - | NO | NO | 任务类型（1:用户任务,2:JAVA任务,3:其他任务） |
| priority | INT | - | YES | NO | 优先级（0:普通,1:紧急,2:特急） |
| status | INT | - | NO | NO | 状态（0:待处理,1:已处理,2:已取消） |
| due_date | DATETIME | - | YES | NO | 截止时间 |
| handled_at | DATETIME | - | YES | NO | 处理时间 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_document_id` (`document_id`)
- INDEX `idx_flow_instance_id` (`flow_instance_id`)
- INDEX `idx_node_instance_id` (`node_instance_id`)
- INDEX `idx_assignee_id` (`assignee_id`)
- INDEX `idx_status` (`status`)
- INDEX `idx_task_type` (`task_type`)

**关系**:
- `document_id` → `document.id` (N:1)
- `flow_instance_id` → `flow_instance.id` (N:1)
- `node_instance_id` → `flow_node_instance.id` (N:1)

**任务类型常量**:
- `TASK_TYPE_USER = 1`: 用户任务
- `TASK_TYPE_JAVA = 2`: JAVA任务
- `TASK_TYPE_OTHER = 3`: 其他任务

**优先级常量**:
- `PRIORITY_NORMAL = 0`: 普通
- `PRIORITY_URGENT = 1`: 紧急
- `PRIORITY_CRITICAL = 2`: 特急

**状态常量**:
- `STATUS_PENDING = 0`: 待处理
- `STATUS_HANDLED = 1`: 已处理
- `STATUS_CANCELLED = 2`: 已取消

---

#### 5.2 done_task（已办任务表）

存储已办任务信息。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| document_id | BIGINT | - | NO | NO | 公文ID（外键） |
| flow_instance_id | BIGINT | - | NO | NO | 流程实例ID（外键） |
| node_instance_id | BIGINT | - | NO | NO | 节点实例ID（外键） |
| handler_id | BIGINT | - | NO | NO | 处理人ID |
| title | VARCHAR | 500 | YES | NO | 已办标题 |
| task_type | INT | - | NO | NO | 任务类型（1:用户任务,2:JAVA任务,3:其他任务） |
| action | VARCHAR | 50 | YES | NO | 操作类型（approve,reject,forward,return,delegate） |
| comments | VARCHAR | 1000 | YES | NO | 处理意见 |
| handled_at | DATETIME | - | NO | NO | 处理时间 |
| created_at | DATETIME | - | NO | NO | 创建时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_document_id` (`document_id`)
- INDEX `idx_flow_instance_id` (`flow_instance_id`)
- INDEX `idx_node_instance_id` (`node_instance_id`)
- INDEX `idx_handler_id` (`handler_id`)
- INDEX `idx_task_type` (`task_type`)
- INDEX `idx_handled_at` (`handled_at`)

**关系**:
- `document_id` → `document.id` (N:1)
- `flow_instance_id` → `flow_instance.id` (N:1)
- `node_instance_id` → `flow_node_instance.id` (N:1)

**任务类型常量**:
- `TASK_TYPE_USER = 1`: 用户任务
- `TASK_TYPE_JAVA = 2`: JAVA任务
- `TASK_TYPE_OTHER = 3`: 其他任务

**操作类型常量**:
- `ACTION_APPROVE = "approve"`: 同意
- `ACTION_REJECT = "reject"`: 拒绝
- `ACTION_FORWARD = "forward"`: 转发
- `ACTION_RETURN = "return"`: 退回
- `ACTION_DELEGATE = "delegate"`: 委派

---

### 6. 扩展功能表

#### 6.1 handling（承办记录表）

存储流程中的承办记录信息。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| flow_instance_id | BIGINT | - | NO | NO | 流程实例ID（外键） |
| node_instance_id | BIGINT | - | YES | NO | 节点实例ID（外键） |
| handler_id | BIGINT | - | NO | NO | 承办人ID |
| handler_dept_id | BIGINT | - | YES | NO | 承办人部门ID |
| handling_content | VARCHAR | 2000 | YES | NO | 承办内容 |
| handling_result | VARCHAR | 1000 | YES | NO | 承办结果 |
| start_time | DATETIME | - | YES | NO | 开始时间 |
| end_time | DATETIME | - | YES | NO | 结束时间 |
| status | INT | - | NO | NO | 状态（0:进行中,1:已完成,2:已退回） |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_flow_instance_id` (`flow_instance_id`)
- INDEX `idx_node_instance_id` (`node_instance_id`)
- INDEX `idx_handler_id` (`handler_id`)
- INDEX `idx_status` (`status`)

**关系**:
- `flow_instance_id` → `flow_instance.id` (N:1)
- `node_instance_id` → `flow_node_instance.id` (N:1)

**承办状态常量**:
- `STATUS_PROCESSING = 0`: 进行中
- `STATUS_COMPLETED = 1`: 已完成
- `STATUS_REJECTED = 2`: 已退回

---

#### 6.2 external_sign_receipt（外单位签收登记表）

存储外单位签收登记信息。

| 字段名 | 类型 | 长度 | 是否为空 | 主键 | 说明 |
|--------|------|------|----------|------|------|
| id | BIGINT | - | NO | YES | 主键ID |
| flow_instance_id | BIGINT | - | NO | NO | 流程实例ID（外键） |
| external_unit_id | BIGINT | - | NO | NO | 外单位ID |
| receiver_name | VARCHAR | 100 | YES | NO | 接收人姓名 |
| receipt_time | DATETIME | - | YES | NO | 签收时间 |
| receipt_status | INT | - | NO | NO | 签收状态（0:未签收,1:已签收,2:拒签） |
| remarks | VARCHAR | 500 | YES | NO | 备注 |
| created_at | DATETIME | - | NO | NO | 创建时间 |
| updated_at | DATETIME | - | NO | NO | 更新时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_flow_instance_id` (`flow_instance_id`)
- INDEX `idx_external_unit_id` (`external_unit_id`)
- INDEX `idx_receipt_status` (`receipt_status`)

**关系**:
- `flow_instance_id` → `flow_instance.id` (N:1)

**签收状态常量**:
- `STATUS_UNRECEIVED = 0`: 未签收
- `STATUS_RECEIVED = 1`: 已签收
- `STATUS_REJECTED = 2`: 拒签

---

## 📊 表关系图

```
document (文档表)
    ├── issuance_info (发文信息) [1:1]
    ├── receipt_info (收文信息) [1:1]
    └── flow_instance (流程实例) [1:N]
            ├── flow_node_instance (节点实例) [1:N]
            │       ├── todo_task (待办任务) [1:N]
            │       ├── done_task (已办任务) [1:N]
            │       ├── free_flow_node_instance (自由流节点扩展) [1:1]
            │       └── handling (承办记录) [1:N]
            └── external_sign_receipt (外单位签收) [1:N]

flow_definition (流程定义)
    ├── flow_node (流程节点) [1:N]
    └── flow_instance (流程实例) [1:N]

flow_action (发送动作)
    ├── flow_action_rule (动作规则) [1:N]
    ├── approver_scope (审批人范围) [1:1]
    └── free_flow_node_instance (自由流节点扩展) [1:N]
```

---

## 🔑 外键约束说明

### 主要外键关系

1. **document → issuance_info / receipt_info**
   - 关系: 1:1
   - 说明: 一个文档只能有一个发文信息或收文信息

2. **document → flow_instance**
   - 关系: 1:N
   - 说明: 一个文档可以有多个流程实例（历史记录）

3. **flow_definition → flow_node**
   - 关系: 1:N
   - 说明: 一个流程定义包含多个节点

4. **flow_definition → flow_instance**
   - 关系: 1:N
   - 说明: 一个流程定义可以启动多个流程实例

5. **flow_instance → flow_node_instance**
   - 关系: 1:N
   - 说明: 一个流程实例包含多个节点实例

6. **flow_node_instance → todo_task / done_task**
   - 关系: 1:N
   - 说明: 一个节点实例可以生成多个待办/已办任务

7. **flow_action → flow_action_rule**
   - 关系: 1:N
   - 说明: 一个动作可以有多个规则

8. **flow_action → approver_scope**
   - 关系: 1:1
   - 说明: 一个动作对应一个审批人范围

---

## 📝 数据库设计原则

1. **统一时间字段**: 所有表都包含 `created_at` 和 `updated_at` 字段
2. **软删除策略**: 通过状态字段实现逻辑删除，不物理删除数据
3. **索引优化**: 为常用查询字段建立索引，提高查询性能
4. **外键约束**: 使用逻辑外键（通过代码保证），不强制数据库外键约束
5. **状态管理**: 使用常量定义状态值，便于维护和扩展
6. **JSON存储**: 对于列表类型数据（如部门ID列表、用户ID列表），使用JSON字符串存储

---

## 🚀 数据库初始化建议

1. **创建数据库**:
   ```sql
   CREATE DATABASE document_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **创建表结构**: 按照上述表结构创建所有表

3. **初始化数据**:
   - 初始化流程定义数据
   - 初始化发送动作数据
   - 初始化动作规则数据

4. **创建索引**: 确保所有索引都已创建

5. **数据备份**: 定期备份数据库，建议使用定时任务

---

**文档版本**: 1.0  
**最后更新**: 2024年  
**维护人员**: XTT Cloud Team

