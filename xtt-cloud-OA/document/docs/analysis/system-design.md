# 公文系统设计文档

## 系统概述

公文管理系统是OA系统的重要组成部分，提供公文起草、审批、归档等全流程管理功能。系统采用微服务架构，基于Spring Boot和MyBatis Plus开发。

## 功能模块

### 1. 公文管理模块
- 公文创建、编辑、删除
- 公文版本管理
- 公文分类和标签

### 2. 流程管理模块
- 审批流程定义
- 流程实例执行
- 节点状态跟踪

### 3. 待办管理模块
- 待办事项生成
- 待办事项处理
- 已办事项记录

### 4. 权限管理模块
- 数据权限控制
- 功能权限分配
- 部门级别权限

### 5. 公文流程模块
- 统一流程管理（发文、收文等）
- 流程扩展信息管理
- 外单位协作处理
- 承办任务管理

## 技术架构

### 后端技术栈
- Spring Boot 2.7.x
- MyBatis Plus 3.5.x
- MySQL 5.7+
- Nacos (服务发现和配置中心)
- Redis (缓存)
- RocketMQ (消息队列)

### 前端技术栈
- Vue 3 + Element Plus
- Axios
- Pinia状态管理

## 数据库设计

### 核心表结构

#### 公文表 (doc_document)
```sql
CREATE TABLE doc_document (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  title         VARCHAR(255) NOT NULL COMMENT '公文标题',
  doc_number    VARCHAR(64) NOT NULL UNIQUE COMMENT '公文编号',
  doc_type_id   BIGINT NOT NULL COMMENT '公文类型ID',
  secret_level  TINYINT DEFAULT 0 COMMENT '密级(0:普通,1:秘密,2:机密,3:绝密)',
  urgency_level TINYINT DEFAULT 0 COMMENT '紧急程度(0:普通,1:急,2:特急)',
  content       LONGTEXT COMMENT '公文内容',
  attachment    VARCHAR(512) COMMENT '附件路径',
  status        TINYINT DEFAULT 0 COMMENT '状态(0:草稿,1:审核中,2:已发布,3:已归档)',
  creator_id    BIGINT NOT NULL COMMENT '创建人ID',
  dept_id       BIGINT NOT NULL COMMENT '所属部门ID',
  publish_time  TIMESTAMP NULL COMMENT '发布时间',
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 公文类型表 (doc_type)
```sql
CREATE TABLE doc_type (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(64) NOT NULL COMMENT '类型名称',
  code        VARCHAR(32) NOT NULL UNIQUE COMMENT '类型编码',
  description VARCHAR(255) COMMENT '描述',
  enabled     TINYINT DEFAULT 1 COMMENT '是否启用',
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 流程定义表 (doc_flow_definition)
```sql
CREATE TABLE doc_flow_definition (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(128) NOT NULL COMMENT '流程名称',
  code            VARCHAR(64) NOT NULL UNIQUE COMMENT '流程编码',
  doc_type_id     BIGINT NOT NULL COMMENT '适用公文类型ID',
  description     VARCHAR(255) COMMENT '流程描述',
  version         INT DEFAULT 1 COMMENT '版本号',
  status          TINYINT DEFAULT 1 COMMENT '状态(0:停用,1:启用)',
  creator_id      BIGINT NOT NULL COMMENT '创建人ID',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 流程实例表 (doc_flow_instance)
```sql
CREATE TABLE doc_flow_instance (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  document_id     BIGINT NOT NULL COMMENT '公文ID',
  flow_def_id     BIGINT NOT NULL COMMENT '流程定义ID',
  flow_type       TINYINT NOT NULL COMMENT '流程类型(1:发文,2:收文)',
  status          TINYINT DEFAULT 0 COMMENT '流程状态(0:进行中,1:已完成,2:已终止)',
  current_node_id BIGINT COMMENT '当前节点ID',
  start_time      TIMESTAMP NULL COMMENT '开始时间',
  end_time        TIMESTAMP NULL COMMENT '结束时间',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 流程节点定义表 (doc_flow_node)
```sql
CREATE TABLE doc_flow_node (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_def_id     BIGINT NOT NULL COMMENT '流程定义ID',
  node_name       VARCHAR(64) NOT NULL COMMENT '节点名称',
  node_type       TINYINT NOT NULL COMMENT '节点类型(1:审批节点,2:抄送节点,3:条件节点)',
  approver_type   TINYINT NOT NULL COMMENT '审批人类型(1:指定人员,2:指定角色,3:指定部门负责人,4:发起人指定)',
  approver_value  VARCHAR(255) COMMENT '审批人值(根据类型存储不同值)',
  order_num       INT NOT NULL COMMENT '节点顺序',
  skip_condition  VARCHAR(255) COMMENT '跳过条件',
  required        TINYINT DEFAULT 1 COMMENT '是否必须(0:可跳过,1:必须)',
  parallel_mode   TINYINT DEFAULT 0 COMMENT '并行模式(0:串行,1:并行)',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 节点实例表 (doc_flow_node_instance)
```sql
CREATE TABLE doc_flow_node_instance (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  node_id         BIGINT NOT NULL COMMENT '节点定义ID',
  approver_id     BIGINT COMMENT '审批人ID',
  approver_dept_id BIGINT COMMENT '审批人部门ID',
  status          TINYINT DEFAULT 0 COMMENT '节点状态(0:待处理,1:处理中,2:已完成,3:已拒绝)',
  comments        VARCHAR(512) COMMENT '审批意见',
  handled_at      TIMESTAMP NULL COMMENT '处理时间',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 待办事项表 (doc_todo_item)
```sql
CREATE TABLE doc_todo_item (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  document_id     BIGINT NOT NULL COMMENT '公文ID',
  flow_instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  node_instance_id BIGINT NOT NULL COMMENT '节点实例ID',
  assignee_id     BIGINT NOT NULL COMMENT '处理人ID',
  title           VARCHAR(255) NOT NULL COMMENT '待办标题',
  content         TEXT COMMENT '待办内容',
  priority        TINYINT DEFAULT 0 COMMENT '优先级(0:普通,1:紧急,2:特急)',
  status          TINYINT DEFAULT 0 COMMENT '状态(0:待处理,1:已处理,2:已取消)',
  due_date        TIMESTAMP NULL COMMENT '截止时间',
  handled_at      TIMESTAMP NULL COMMENT '处理时间',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 已办事项表 (doc_done_item)
```sql
CREATE TABLE doc_done_item (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  document_id     BIGINT NOT NULL COMMENT '公文ID',
  flow_instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  node_instance_id BIGINT NOT NULL COMMENT '节点实例ID',
  handler_id      BIGINT NOT NULL COMMENT '处理人ID',
  title           VARCHAR(255) NOT NULL COMMENT '已办标题',
  action          VARCHAR(32) NOT NULL COMMENT '操作类型(approve:同意,reject:拒绝,forward:转发)',
  comments        TEXT COMMENT '处理意见',
  handled_at      TIMESTAMP NOT NULL COMMENT '处理时间',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 发文流程扩展表 (doc_issuance_info)
```sql
CREATE TABLE doc_issuance_info (
  id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_instance_id    BIGINT NOT NULL COMMENT '流程实例ID',
  draft_user_id       BIGINT COMMENT '拟稿人ID',
  draft_dept_id       BIGINT COMMENT '拟稿部门ID',
  issuing_unit        VARCHAR(128) COMMENT '发文单位',
  document_category   VARCHAR(64) COMMENT '公文种类',
  urgency_level       TINYINT DEFAULT 0 COMMENT '紧急程度',
  secret_level        TINYINT DEFAULT 0 COMMENT '密级',
  word_count          INT COMMENT '字数',
  printing_copies     INT COMMENT '印发份数',
  main_recipients     TEXT COMMENT '主送单位',
  cc_recipients       TEXT COMMENT '抄送单位',
  keywords            VARCHAR(255) COMMENT '主题词',
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 收文流程扩展表 (doc_receipt_info)
```sql
CREATE TABLE doc_receipt_info (
  id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_instance_id    BIGINT NOT NULL COMMENT '流程实例ID',
  receive_date        TIMESTAMP NULL COMMENT '收文日期',
  sender_unit         VARCHAR(128) COMMENT '来文单位',
  document_number     VARCHAR(64) COMMENT '来文编号',
  receive_method      TINYINT DEFAULT 1 COMMENT '收文方式(1:纸质,2:电子,3:其他)',
  attachments         TEXT COMMENT '附件信息',
  keywords            VARCHAR(255) COMMENT '主题词',
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 外单位签收登记表 (doc_external_sign_receipt)
```sql
CREATE TABLE doc_external_sign_receipt (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  external_unit_id BIGINT NOT NULL COMMENT '外单位ID',
  receiver_name   VARCHAR(64) COMMENT '接收人姓名',
  receipt_time    TIMESTAMP NULL COMMENT '签收时间',
  receipt_status  TINYINT DEFAULT 0 COMMENT '签收状态(0:未签收,1:已签收,2:拒签)',
  remarks         VARCHAR(255) COMMENT '备注',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 承办记录表 (doc_handling)
```sql
CREATE TABLE doc_handling (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_instance_id BIGINT NOT NULL COMMENT '流程实例ID',
  node_instance_id BIGINT NOT NULL COMMENT '节点实例ID',
  handler_id      BIGINT NOT NULL COMMENT '承办人ID',
  handler_dept_id BIGINT NOT NULL COMMENT '承办部门ID',
  handling_content TEXT COMMENT '承办内容',
  handling_result TEXT COMMENT '承办结果',
  start_time      TIMESTAMP NULL COMMENT '开始时间',
  end_time        TIMESTAMP NULL COMMENT '结束时间',
  status          TINYINT DEFAULT 0 COMMENT '状态(0:进行中,1:已完成,2:已退回)',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```