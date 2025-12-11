# DocumentService 和 DocumentController 实现文档

## 📋 概述

已完整实现 `DocumentService` 和 `DocumentController`，提供公文管理的完整 CRUD 功能，包括创建、查询、更新、删除、发布、归档等操作。

---

## ✅ 已实现功能

### 1. DocumentService（公文服务）

#### 核心方法

| 方法 | 功能 | 状态 |
|------|------|------|
| `createDocument()` | 创建公文 | ✅ |
| `updateDocument()` | 更新公文 | ✅ |
| `deleteDocument()` | 删除公文 | ✅ |
| `getDocument()` | 获取公文详情 | ✅ |
| `listDocuments()` | 分页查询公文列表 | ✅ |
| `publishDocument()` | 发布公文 | ✅ |
| `archiveDocument()` | 归档公文 | ✅ |
| `generateDocNumber()` | 生成公文编号 | ✅ |
| `validateDocument()` | 验证公文参数 | ✅ |

#### 功能特性

1. **公文编号自动生成**
   - 格式：`DOC-YYYYMMDD-序号`（如：`DOC-20231201-001`）
   - 基于日期和同类型公文数量自动生成
   - 保证唯一性

2. **状态管理**
   - 草稿（STATUS_DRAFT = 0）
   - 审核中（STATUS_REVIEWING = 1）
   - 已发布（STATUS_PUBLISHED = 2）
   - 已归档（STATUS_ARCHIVED = 3）

3. **业务规则验证**
   - 只有草稿状态的公文可以删除
   - 只有审核中的公文可以发布
   - 只有已发布的公文可以归档
   - 公文编号创建后不允许修改

4. **分页查询**
   - 支持多条件组合查询
   - 支持标题模糊搜索
   - 支持时间范围查询
   - 使用 MyBatis Plus 分页插件

5. **日志记录**
   - 所有关键操作都有日志记录
   - 使用 INFO、DEBUG、WARN、ERROR 不同级别
   - 记录操作参数和结果

---

### 2. DocumentController（公文控制器）

#### REST API 接口

| 方法 | 路径 | 功能 | 状态 |
|------|------|------|------|
| POST | `/api/document/documents` | 创建公文 | ✅ |
| GET | `/api/document/documents/{id}` | 获取公文详情 | ✅ |
| PUT | `/api/document/documents/{id}` | 更新公文 | ✅ |
| DELETE | `/api/document/documents/{id}` | 删除公文 | ✅ |
| GET | `/api/document/documents` | 查询公文列表（分页） | ✅ |
| POST | `/api/document/documents/{id}/publish` | 发布公文 | ✅ |
| POST | `/api/document/documents/{id}/archive` | 归档公文 | ✅ |

#### 查询参数

- `page`: 页码（默认 1）
- `size`: 每页大小（默认 10）
- `title`: 公文标题（模糊搜索）
- `docTypeId`: 公文类型ID
- `status`: 公文状态
- `creatorId`: 创建人ID
- `deptId`: 所属部门ID
- `startTime`: 创建开始时间（ISO 8601 格式）
- `endTime`: 创建结束时间（ISO 8601 格式）

---

## 🔧 技术实现

### 1. 使用 MyBatis Plus

- 继承 `BaseMapper<Document>`，自动获得 CRUD 方法
- 使用 `LambdaQueryWrapper` 构建类型安全的查询条件
- 使用 `Page<T>` 实现分页查询
- 配置了分页插件（`MyBatisPlusConfig`）

### 2. 事务管理

- 所有写操作使用 `@Transactional` 保证数据一致性
- 创建、更新、删除、发布、归档都在事务中执行

### 3. 异常处理

- 使用 `BusinessException` 抛出业务异常
- 参数验证失败时抛出明确的异常信息
- 异常会被全局异常处理器统一处理

### 4. 日志记录

- 使用 SLF4J 日志框架
- 关键操作记录 INFO 级别日志
- 查询操作记录 DEBUG 级别日志
- 异常情况记录 ERROR 级别日志

---

## 📝 代码示例

### 创建公文

```java
POST /api/document/documents
Content-Type: application/json

{
  "title": "关于召开年度总结会议的通知",
  "docTypeId": 1,
  "secretLevel": 0,
  "urgencyLevel": 1,
  "content": "会议内容...",
  "attachment": "file_path",
  "creatorId": 1,
  "deptId": 1
}

响应:
{
  "code": 2001,
  "message": "接口调用成功",
  "data": {
    "id": 1,
    "title": "关于召开年度总结会议的通知",
    "docNumber": "DOC-20231201-001",
    "docTypeId": 1,
    "status": 0,
    ...
  }
}
```

### 查询公文列表

```java
GET /api/document/documents?page=1&size=10&title=会议&status=0

响应:
{
  "code": 2001,
  "message": "接口调用成功",
  "data": {
    "items": [...],
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10
  }
}
```

### 发布公文

```java
POST /api/document/documents/1/publish

响应:
{
  "code": 2001,
  "message": "接口调用成功",
  "data": "发布成功"
}
```

---

## 🔍 业务规则

### 公文状态流转

```
草稿 (0) → 审核中 (1) → 已发布 (2) → 已归档 (3)
   ↑                                    ↓
   └────────────────────────────────────┘
```

### 操作权限

- **创建**: 任何用户都可以创建公文（草稿状态）
- **更新**: 只有草稿状态的公文可以更新
- **删除**: 只有草稿状态的公文可以删除
- **发布**: 只有审核中的公文可以发布
- **归档**: 只有已发布的公文可以归档

### 公文编号规则

- 格式：`DOC-YYYYMMDD-序号`
- 示例：`DOC-20231201-001`
- 基于日期和同类型公文数量生成
- 创建后不允许修改

---

## ⚙️ 配置说明

### MyBatis Plus 分页插件

已创建 `MyBatisPlusConfig` 配置类，配置了分页插件：

```java
@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
```

---

## 🎯 常量定义

### 公文状态

```java
public static final int STATUS_DRAFT = 0;        // 草稿
public static final int STATUS_REVIEWING = 1;    // 审核中
public static final int STATUS_PUBLISHED = 2;    // 已发布
public static final int STATUS_ARCHIVED = 3;     // 已归档
```

### 密级

```java
public static final int SECRET_LEVEL_NORMAL = 0;      // 普通
public static final int SECRET_LEVEL_SECRET = 1;      // 秘密
public static final int SECRET_LEVEL_CONFIDENTIAL = 2; // 机密
public static final int SECRET_LEVEL_TOP_SECRET = 3;   // 绝密
```

### 紧急程度

```java
public static final int URGENCY_LEVEL_NORMAL = 0;     // 普通
public static final int URGENCY_LEVEL_URGENT = 1;     // 急
public static final int URGENCY_LEVEL_VERY_URGENT = 2; // 特急
```

---

## 📊 数据验证

### 必填字段验证

- `title`: 公文标题（不能为空）
- `docTypeId`: 公文类型ID（不能为空）
- `creatorId`: 创建人ID（不能为空）
- `deptId`: 所属部门ID（不能为空）

### 默认值设置

- `status`: 默认为草稿（STATUS_DRAFT = 0）
- `secretLevel`: 默认为普通（SECRET_LEVEL_NORMAL = 0）
- `urgencyLevel`: 默认为普通（URGENCY_LEVEL_NORMAL = 0）

---

## 🚀 使用示例

### 1. 创建公文

```bash
curl -X POST http://localhost:8086/api/document/documents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "关于召开年度总结会议的通知",
    "docTypeId": 1,
    "secretLevel": 0,
    "urgencyLevel": 1,
    "content": "会议内容...",
    "creatorId": 1,
    "deptId": 1
  }'
```

### 2. 查询公文列表

```bash
curl -X GET "http://localhost:8086/api/document/documents?page=1&size=10&title=会议" \
  -H "Authorization: Bearer <token>"
```

### 3. 更新公文

```bash
curl -X PUT http://localhost:8086/api/document/documents/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "关于召开年度总结会议的通知（修订版）",
    "content": "修订后的会议内容..."
  }'
```

### 4. 发布公文

```bash
curl -X POST http://localhost:8086/api/document/documents/1/publish \
  -H "Authorization: Bearer <token>"
```

---

## 🔄 与流程模块的集成

公文创建后，可以通过流程模块启动审批流程：

```java
// 1. 创建公文
Document document = documentService.createDocument(documentDto);

// 2. 创建流程实例
FlowInstance flowInstance = new FlowInstance();
flowInstance.setDocumentId(document.getId());
flowInstance.setFlowType(FlowInstance.TYPE_ISSUANCE);
flowService.createFlow(flowInstance);

// 3. 启动流程
flowService.startIssuanceFlow(flowInstance.getId());
```

---

## 📈 性能优化建议

1. **索引优化**
   - 确保数据库表有适当的索引（title、docTypeId、status、createdAt 等）
   - 参考设计文档中的索引设计

2. **缓存策略**
   - 可以考虑缓存公文类型信息
   - 频繁查询的公文可以考虑缓存

3. **分页优化**
   - 默认每页大小设置为 10，避免一次查询过多数据
   - 最大分页限制设置为 1000

---

## 🐛 错误处理

### 常见错误

1. **公文不存在**
   - 错误信息：`公文不存在`
   - HTTP 状态码：200（业务异常）
   - 响应格式：`Result.error("公文不存在")`

2. **状态不允许操作**
   - 错误信息：`只有草稿状态的公文可以删除`
   - HTTP 状态码：200（业务异常）

3. **参数验证失败**
   - 错误信息：`公文标题不能为空`
   - HTTP 状态码：200（业务异常）

所有异常都会被全局异常处理器统一处理，返回标准格式的响应。

---

## ✅ 完成状态

- [x] DocumentService 完整实现
- [x] DocumentController 完整实现
- [x] 公文 CRUD 功能
- [x] 分页查询功能
- [x] 公文发布功能
- [x] 公文归档功能
- [x] 公文编号自动生成
- [x] 业务规则验证
- [x] 日志记录
- [x] 异常处理
- [x] MyBatis Plus 分页插件配置

---

**实现时间**: 2023.0.3.3  
**实现人**: XTT Cloud Team

