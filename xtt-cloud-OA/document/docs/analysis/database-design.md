# 公文系统数据库设计

## 概述
公文系统的数据库设计遵循RBAC权限模型，扩展了公文管理、流程审批、待办已办等核心功能。

## 核心表结构

### 1. 公文相关表

#### 公文表 (doc_document)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR(255) | 公文标题 |
| doc_number | VARCHAR(64) | 公文编号 |
| doc_type_id | BIGINT | 公文类型ID |
| secret_level | TINYINT | 密级 |
| urgency_level | TINYINT | 紧急程度 |
| content | LONGTEXT | 公文内容 |
| attachment | VARCHAR(512) | 附件路径 |
| status | TINYINT | 状态 |
| creator_id | BIGINT | 创建人ID |
| dept_id | BIGINT | 所属部门ID |
| publish_time | TIMESTAMP | 发布时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 公文类型表 (doc_type)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(64) | 类型名称 |
| code | VARCHAR(32) | 类型编码 |
| description | VARCHAR(255) | 描述 |
| enabled | TINYINT | 是否启用 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 2. 流程相关表

#### 流程定义表 (doc_flow_definition)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(128) | 流程名称 |
| code | VARCHAR(64) | 流程编码 |
| doc_type_id | BIGINT | 适用公文类型ID |
| description | VARCHAR(255) | 流程描述 |
| version | INT | 版本号 |
| status | TINYINT | 状态 |
| creator_id | BIGINT | 创建人ID |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 流程节点定义表 (doc_flow_node)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| flow_def_id | BIGINT | 流程定义ID |
| node_name | VARCHAR(64) | 节点名称 |
| node_type | TINYINT | 节点类型 |
| approver_type | TINYINT | 审批人类型 |
| approver_value | VARCHAR(255) | 审批人值 |
| order_num | INT | 节点顺序 |
| skip_condition | VARCHAR(255) | 跳过条件 |
| required | TINYINT | 是否必须 |
| parallel_mode | TINYINT | 并行模式(0:串行,1:并行) |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 流程实例表 (doc_flow_instance)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| document_id | BIGINT | 公文ID |
| flow_def_id | BIGINT | 流程定义ID |
| flow_type | TINYINT | 流程类型(1:发文,2:收文)
| status | TINYINT | 流程状态 |
| current_node_id | BIGINT | 当前节点ID |
| start_time | TIMESTAMP | 开始时间 |
| end_time | TIMESTAMP | 结束时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 节点实例表 (doc_flow_node_instance)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| flow_instance_id | BIGINT | 流程实例ID |
| node_id | BIGINT | 节点定义ID |
| approver_id | BIGINT | 审批人ID |
| approver_dept_id | BIGINT | 审批人部门ID |
| status | TINYINT | 节点状态 |
| comments | VARCHAR(512) | 审批意见 |
| handled_at | TIMESTAMP | 处理时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

### 3. 待办已办相关表

#### 待办事项表 (doc_todo_item)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| document_id | BIGINT | 公文ID |
| flow_instance_id | BIGINT | 流程实例ID |
| node_instance_id | BIGINT | 节点实例ID |
| assignee_id | BIGINT | 处理人ID |
| title | VARCHAR(255) | 待办标题 |
| content | TEXT | 待办内容 |
| priority | TINYINT | 优先级 |
| status | TINYINT | 状态 |
| due_date | TIMESTAMP | 截止时间 |
| handled_at | TIMESTAMP | 处理时间 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 已办事项表 (doc_done_item)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| document_id | BIGINT | 公文ID |
| flow_instance_id | BIGINT | 流程实例ID |
| node_instance_id | BIGINT | 节点实例ID |
| handler_id | BIGINT | 处理人ID |
| title | VARCHAR(255) | 已办标题 |
| action | VARCHAR(32) | 操作类型 |
| comments | TEXT | 处理意见 |
| handled_at | TIMESTAMP | 处理时间 |
| created_at | TIMESTAMP | 创建时间 |

### 4. 流程扩展信息表

#### 发文流程扩展表 (doc_issuance_info)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| flow_instance_id | BIGINT | 流程实例ID |
| draft_user_id | BIGINT | 拟稿人ID |
| draft_dept_id | BIGINT | 拟稿部门ID |
| issuing_unit | VARCHAR(128) | 发文单位 |
| document_category | VARCHAR(64) | 公文种类 |
| urgency_level | TINYINT | 紧急程度 |
| secret_level | TINYINT | 密级 |
| word_count | INT | 字数 |
| printing_copies | INT | 印发份数 |
| main_recipients | TEXT | 主送单位 |
| cc_recipients | TEXT | 抄送单位 |
| keywords | VARCHAR(255) | 主题词 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 收文流程扩展表 (doc_receipt_info)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| flow_instance_id | BIGINT | 流程实例ID |
| receive_date | TIMESTAMP | 收文日期 |
| sender_unit | VARCHAR(128) | 来文单位 |
| document_number | VARCHAR(64) | 来文编号 |
| receive_method | TINYINT | 收文方式(1:纸质,2:电子,3:其他) |
| attachments | TEXT | 附件信息 |
| keywords | VARCHAR(255) | 主题词 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 外单位签收登记表 (doc_external_sign_receipt)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| flow_instance_id | BIGINT | 流程实例ID |
| external_unit_id | BIGINT | 外单位ID |
| receiver_name | VARCHAR(64) | 接收人姓名 |
| receipt_time | TIMESTAMP | 签收时间 |
| receipt_status | TINYINT | 签收状态(0:未签收,1:已签收,2:拒签) |
| remarks | VARCHAR(255) | 备注 |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### 承办记录表 (doc_handling)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键 |
| flow_instance_id | BIGINT | 流程实例ID |
| node_instance_id | BIGINT | 节点实例ID |
| handler_id | BIGINT | 承办人ID |
| handler_dept_id | BIGINT | 承办部门ID |
| handling_content | TEXT | 承办内容 |
| handling_result | TEXT | 承办结果 |
| start_time | TIMESTAMP | 开始时间 |
| end_time | TIMESTAMP | 结束时间 |
| status | TINYINT | 状态(0:进行中,1:已完成,2:已退回) |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

## 索引设计

### 公文表索引
```sql
-- 公文表索引
CREATE INDEX idx_doc_creator ON doc_document(creator_id);
CREATE INDEX idx_doc_dept ON doc_document(dept_id);
CREATE INDEX idx_doc_status ON doc_document(status);
CREATE INDEX idx_doc_created ON doc_document(created_at);
```

### 流程表索引
```sql
-- 流程定义表索引
CREATE INDEX idx_flow_def_doc_type ON doc_flow_definition(doc_type_id);
CREATE INDEX idx_flow_def_status ON doc_flow_definition(status);

-- 流程节点表索引
CREATE INDEX idx_flow_node_flow_def ON doc_flow_node(flow_def_id);

-- 流程实例表索引
CREATE INDEX idx_flow_inst_document ON doc_flow_instance(document_id);
CREATE INDEX idx_flow_inst_status ON doc_flow_instance(status);
CREATE INDEX idx_flow_inst_type ON doc_flow_instance(flow_type);

-- 节点实例表索引
CREATE INDEX idx_flow_node_inst_flow ON doc_flow_node_instance(flow_instance_id);
CREATE INDEX idx_flow_node_inst_approver ON doc_flow_node_instance(approver_id);
```

### 待办已办表索引
```sql
-- 待办事项表索引
CREATE INDEX idx_todo_assignee ON doc_todo_item(assignee_id);
CREATE INDEX idx_todo_status ON doc_todo_item(status);
CREATE INDEX idx_todo_created ON doc_todo_item(created_at);

-- 已办事项表索引
CREATE INDEX idx_done_handler ON doc_done_item(handler_id);
CREATE INDEX idx_done_handled ON doc_done_item(handled_at);
```

### 流程扩展信息表索引
```sql
-- 发文流程扩展表索引
CREATE INDEX idx_issuance_flow_inst ON doc_issuance_info(flow_instance_id);

-- 收文流程扩展表索引
CREATE INDEX idx_receipt_flow_inst ON doc_receipt_info(flow_instance_id);

-- 外单位签收登记表索引
CREATE INDEX idx_external_sign_receipt ON doc_external_sign_receipt(flow_instance_id);
CREATE INDEX idx_external_unit ON doc_external_sign_receipt(external_unit_id);

-- 承办记录表索引
CREATE INDEX idx_handling_flow_inst ON doc_handling(flow_instance_id);
CREATE INDEX idx_handling_node_inst ON doc_handling(node_instance_id);
CREATE INDEX idx_handling_handler ON doc_handling(handler_id);
```