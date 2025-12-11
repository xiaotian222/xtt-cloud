# 文档架构设计

## 📋 概述

本文档描述了流程引擎中的文档架构设计，明确了 Document 作为文件抽象类型，以及 IssuanceInfo 和 ReceiptInfo 作为其子类型的设计理念。

---

## 🏗️ 架构设计

### 核心概念

```
Document (文档抽象类型)
    ├── IssuanceInfo (发文 - Document 的子类型)
    └── ReceiptInfo (收文 - Document 的子类型)
```

### 设计原则

1. **Document 是抽象类型**
   - Document 是流程引擎中的通用文档抽象
   - 包含所有文档的通用属性（标题、编号、内容、状态等）
   - 不区分具体的文档类型（发文/收文）

2. **子类型扩展**
   - IssuanceInfo（发文）是 Document 的子类型
   - ReceiptInfo（收文）是 Document 的子类型
   - 子类型包含特定类型的扩展属性

3. **关联关系**
   - IssuanceInfo 和 ReceiptInfo 通过 `documentId` 关联到 Document
   - 一个 Document 只能有一个 IssuanceInfo 或 ReceiptInfo（取决于文档类型）

---

## 📊 实体关系

### Document（文档）

```java
public class Document {
    private Long id;              // 文档ID
    private String title;         // 标题
    private String docNumber;     // 文档编号
    private Long docTypeId;       // 文档类型ID（1:发文,2:收文）
    private Integer secretLevel;  // 密级
    private Integer urgencyLevel; // 紧急程度
    private String content;       // 内容
    private String attachment;    // 附件
    private Integer status;       // 状态
    private Long creatorId;       // 创建人ID
    private Long deptId;          // 部门ID
    private LocalDateTime publishTime; // 发布时间
}
```

**特点：**
- 包含所有文档的通用属性
- 通过 `docTypeId` 区分文档类型
- 不包含特定类型的业务属性

### IssuanceInfo（发文信息）

```java
public class IssuanceInfo {
    private Long id;
    private Long documentId;      // 关联到 Document（而不是 FlowInstance）
    private Long draftUserId;      // 拟稿人ID
    private Long draftDeptId;      // 拟稿部门ID
    private String issuingUnit;    // 发文单位
    private String documentCategory; // 公文种类
    private String mainRecipients; // 主送单位
    private String ccRecipients;  // 抄送单位
    private Integer wordCount;     // 字数
    private Integer printingCopies; // 印发份数
    private String keywords;       // 主题词
}
```

**特点：**
- 通过 `documentId` 关联到 Document
- 包含发文特有的业务属性
- 是 Document 的子类型

### ReceiptInfo（收文信息）

```java
public class ReceiptInfo {
    private Long id;
    private Long documentId;       // 关联到 Document（而不是 FlowInstance）
    private LocalDateTime receiveDate; // 收文日期
    private String senderUnit;     // 来文单位
    private String documentNumber; // 来文编号
    private Integer receiveMethod; // 收文方式
    private String attachments;    // 附件信息
    private String keywords;       // 主题词
}
```

**特点：**
- 通过 `documentId` 关联到 Document
- 包含收文特有的业务属性
- 是 Document 的子类型

---

## 🔄 数据流转

### 创建流程

```
1. 创建 Document（通用文档）
   ↓
2. 根据 docTypeId 判断文档类型
   ↓
3. 如果是发文类型（docTypeId = 1）
   → 创建 IssuanceInfo，关联到 Document
4. 如果是收文类型（docTypeId = 2）
   → 创建 ReceiptInfo，关联到 Document
   ↓
5. 创建 FlowInstance，关联到 Document
   ↓
6. 启动流程
```

### 查询流程

```
1. 通过 Document ID 查询文档基本信息
   ↓
2. 根据 docTypeId 判断文档类型
   ↓
3. 如果是发文类型
   → 通过 documentId 查询 IssuanceInfo
4. 如果是收文类型
   → 通过 documentId 查询 ReceiptInfo
   ↓
5. 通过 documentId 查询 FlowInstance
```

---

## 🎯 设计优势

### 1. 清晰的层次结构

- Document 作为抽象层，包含通用属性
- 子类型包含特定业务属性
- 职责清晰，易于维护

### 2. 易于扩展

- 可以轻松添加新的文档类型
- 只需创建新的子类型实体
- 不需要修改 Document 结构

### 3. 数据一致性

- 所有文档共享相同的基础属性
- 子类型属性独立管理
- 通过 documentId 建立关联

### 4. 流程引擎解耦

- 流程引擎只关心 Document
- 不关心具体的文档类型
- 子类型信息通过 Document 获取

---

## 📝 使用示例

### 创建发文

```java
// 1. 创建 Document
Document document = new Document();
document.setTitle("关于XXX的通知");
document.setDocTypeId(1); // 发文类型
document.setStatus(Document.STATUS_DRAFT);
documentService.createDocument(document);

// 2. 创建 IssuanceInfo
IssuanceInfo issuanceInfo = new IssuanceInfo();
issuanceInfo.setDocumentId(document.getId());
issuanceInfo.setIssuingUnit("XX部门");
issuanceInfo.setMainRecipients("XX单位");
flowService.createIssuanceInfo(issuanceInfo);

// 3. 创建并启动流程
FlowInstance flowInstance = new FlowInstance();
flowInstance.setDocumentId(document.getId());
flowInstance.setFlowType(FlowInstance.TYPE_ISSUANCE);
flowService.createFlowInstance(flowInstance);
flowService.startFlow(document.getId(), flowDefId);
```

### 查询发文

```java
// 1. 查询 Document
Document document = documentService.getDocument(documentId);

// 2. 查询 IssuanceInfo
IssuanceInfo issuanceInfo = flowService.getIssuanceInfoByDocumentId(documentId);

// 3. 查询流程实例
FlowInstance flowInstance = flowService.getFlowInstanceByDocumentId(documentId);
```

---

## 🔧 数据库设计

### doc_document 表

```sql
CREATE TABLE doc_document (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  title           VARCHAR(255) NOT NULL COMMENT '标题',
  doc_number      VARCHAR(64) COMMENT '文档编号',
  doc_type_id     BIGINT NOT NULL COMMENT '文档类型ID（1:发文,2:收文）',
  secret_level    TINYINT DEFAULT 0 COMMENT '密级',
  urgency_level   TINYINT DEFAULT 0 COMMENT '紧急程度',
  content         TEXT COMMENT '内容',
  attachment      VARCHAR(512) COMMENT '附件',
  status          TINYINT DEFAULT 0 COMMENT '状态',
  creator_id      BIGINT NOT NULL COMMENT '创建人ID',
  dept_id         BIGINT COMMENT '部门ID',
  publish_time    TIMESTAMP NULL COMMENT '发布时间',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### doc_issuance_info 表

```sql
CREATE TABLE doc_issuance_info (
  id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
  document_id         BIGINT NOT NULL COMMENT '文档ID（关联到 doc_document）',
  draft_user_id       BIGINT COMMENT '拟稿人ID',
  draft_dept_id       BIGINT COMMENT '拟稿部门ID',
  issuing_unit        VARCHAR(128) COMMENT '发文单位',
  document_category   VARCHAR(64) COMMENT '公文种类',
  main_recipients     TEXT COMMENT '主送单位',
  cc_recipients       TEXT COMMENT '抄送单位',
  word_count          INT COMMENT '字数',
  printing_copies     INT COMMENT '印发份数',
  keywords            VARCHAR(255) COMMENT '主题词',
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_document_id (document_id)
);
```

### doc_receipt_info 表

```sql
CREATE TABLE doc_receipt_info (
  id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
  document_id         BIGINT NOT NULL COMMENT '文档ID（关联到 doc_document）',
  receive_date        TIMESTAMP NULL COMMENT '收文日期',
  sender_unit         VARCHAR(128) COMMENT '来文单位',
  document_number     VARCHAR(64) COMMENT '来文编号',
  receive_method      TINYINT DEFAULT 1 COMMENT '收文方式',
  attachments         TEXT COMMENT '附件信息',
  keywords            VARCHAR(255) COMMENT '主题词',
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_document_id (document_id)
);
```

---

## 🚀 迁移说明

### 从旧设计迁移

**旧设计：**
- IssuanceInfo 和 ReceiptInfo 通过 `flowInstanceId` 关联到 FlowInstance

**新设计：**
- IssuanceInfo 和 ReceiptInfo 通过 `documentId` 关联到 Document

**迁移步骤：**
1. 修改数据库表结构，将 `flow_instance_id` 改为 `document_id`
2. 更新实体类，修改字段名和关联关系
3. 更新服务层代码，修改查询逻辑
4. 更新 Controller，保持 API 兼容性（通过 FlowInstance 查询时，先获取 documentId）

---

**设计时间**: 2023.0.3.3  
**设计人**: XTT Cloud Team

