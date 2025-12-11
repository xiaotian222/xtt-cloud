# 流程引擎核心逻辑实现文档

## 📋 概述

已完整实现流程引擎的核心逻辑，包括节点流转、审批处理、待办已办管理等核心功能。

---

## ✅ 已实现功能

### 1. 实体类

#### FlowDefinition（流程定义）
- 流程名称、编码、描述
- 适用公文类型
- 版本号和状态管理

#### FlowNode（流程节点定义）
- 节点名称、类型
- 审批人类型和值
- 节点顺序、并行模式
- 跳过条件、是否必须
- 是否最后一个节点

#### FlowNodeInstance（节点实例）
- 关联流程实例和节点定义
- 审批人信息
- 节点状态（待处理、处理中、已完成、已拒绝、已跳过）
- 审批意见和处理时间

#### TodoItem（待办事项）
- 关联公文、流程实例、节点实例
- 处理人、标题、内容
- 优先级、状态、截止时间

#### DoneItem（已办事项）
- 关联公文、流程实例、节点实例
- 处理人、操作类型、处理意见
- 处理时间

---

### 2. Mapper 接口

- ✅ FlowDefinitionMapper
- ✅ FlowNodeMapper
- ✅ FlowNodeInstanceMapper
- ✅ TodoItemMapper
- ✅ DoneItemMapper

---

### 3. 核心服务

#### FlowEngineService（流程引擎核心服务）

**主要功能：**

1. **流程启动** (`startFlow`)
   - 创建流程实例
   - 加载流程定义和节点列表
   - 创建第一个节点实例
   - 分配审批人
   - 生成待办事项

2. **节点流转** (`moveToNextNode`)
   - 串行节点流转
   - 并行节点流转和汇聚
   - 条件分支判断（预留接口）
   - 节点跳过逻辑

3. **审批处理** (`processNodeApproval`)
   - 同意操作
   - 拒绝操作
   - 转发操作（预留）
   - 退回操作（预留）

4. **流程结束** (`completeFlow`)
   - 判断流程是否结束
   - 更新流程实例状态
   - 更新公文状态

**核心算法：**

- **串行节点流转**：基于 `orderNum` 查找下一个节点
- **并行节点汇聚**：检查所有并行节点实例是否完成
- **审批人分配**：支持指定人员、角色、部门负责人、发起人指定

#### FlowApprovalService（审批处理服务）

提供审批操作的封装接口：
- `approve()` - 审批同意
- `reject()` - 审批拒绝
- `forward()` - 审批转发
- `returnBack()` - 审批退回

#### TodoService（待办已办服务）

**待办事项管理：**
- `createTodoItem()` - 创建待办事项
- `markAsHandled()` - 标记为已处理
- `cancelTodoItem()` - 取消待办事项
- `getTodoItemsByAssignee()` - 根据审批人查询待办列表
- `getTodoItemsByFlowInstance()` - 根据流程实例查询待办列表

**已办事项管理：**
- `createDoneItem()` - 创建已办事项
- `getDoneItemsByHandler()` - 根据处理人查询已办列表
- `getDoneItemsByFlowInstance()` - 根据流程实例查询已办列表

---

### 4. REST API 接口

#### FlowController

**流程管理：**
- `POST /api/document/flows/start` - 启动流程
  - 参数：`documentId`, `flowDefId`

**审批操作：**
- `POST /api/document/flows/approve` - 审批同意
  - 参数：`nodeInstanceId`, `comments`, `approverId`
- `POST /api/document/flows/reject` - 审批拒绝
  - 参数：`nodeInstanceId`, `comments`, `approverId`
- `POST /api/document/flows/forward` - 审批转发
  - 参数：`nodeInstanceId`, `comments`, `approverId`
- `POST /api/document/flows/return` - 审批退回
  - 参数：`nodeInstanceId`, `comments`, `approverId`

**待办已办：**
- `GET /api/document/flows/todos` - 查询待办事项列表
  - 参数：`assigneeId`, `pageNum`, `pageSize`
- `GET /api/document/flows/dones` - 查询已办事项列表
  - 参数：`handlerId`, `pageNum`, `pageSize`

---

## 🔄 核心流程

### 1. 流程启动流程

```
1. 验证公文存在
   ↓
2. 加载流程定义（检查是否启用）
   ↓
3. 加载节点列表（按顺序排序）
   ↓
4. 创建流程实例
   ↓
5. 创建第一个节点实例
   ↓
6. 分配审批人
   ↓
7. 生成待办事项
   ↓
8. 更新流程实例当前节点
```

### 2. 节点流转流程

```
当前节点完成
   ↓
判断节点类型和并行模式
   ↓
串行节点 → 查找下一个节点（orderNum + 1）
并行节点 → 检查所有并行节点是否完成
   ↓
检查跳过条件
   ↓
创建下一个节点实例
   ↓
分配审批人
   ↓
生成待办事项
   ↓
更新流程实例当前节点
```

### 3. 审批处理流程

```
审批人处理待办事项
   ↓
验证审批权限
   ↓
更新节点实例状态
   ↓
创建已办事项记录
   ↓
更新待办事项状态
   ↓
判断操作类型
   ↓
同意 → 流转到下一个节点
拒绝 → 流程终止或退回
转发 → 创建新的节点实例（预留）
退回 → 流转到上一个节点（预留）
```

---

## 🎯 核心特性

### 1. 串行流程支持
- 基于节点顺序号（orderNum）自动流转
- 支持节点跳过条件判断

### 2. 并行流程支持
- 支持并行节点创建
- 支持并行节点汇聚（等待所有节点完成）
- 为每个审批人创建独立的节点实例

### 3. 审批人分配策略
- **指定人员**：直接使用用户ID列表
- **指定角色**：查询角色下的所有用户（需集成用户服务）
- **部门负责人**：查询部门负责人（需集成用户服务）
- **发起人指定**：从流程实例中获取（需存储发起人指定的审批人）

### 4. 待办已办管理
- 自动生成待办事项
- 自动创建已办记录
- 支持分页查询
- 支持按审批人、流程实例查询

### 5. 流程状态管理
- 流程实例状态：进行中、已完成、已终止
- 节点实例状态：待处理、处理中、已完成、已拒绝、已跳过

---

## ⚠️ 待完善功能

### 1. 用户服务集成
以下方法需要集成用户服务：
- `getUserIdsByRole()` - 根据角色获取用户ID列表
- `getDeptLeaderIds()` - 获取部门负责人ID列表
- `getApproversFromFlowInstance()` - 获取发起人指定的审批人

### 2. 条件分支处理
- `shouldSkipNode()` - 节点跳过条件判断
- 需要实现 SpEL 表达式解析

### 3. 转发和退回功能
- `handleForward()` - 转发逻辑
- `handleReturn()` - 退回逻辑

### 4. 超时处理
- 待办事项超时提醒
- 超时自动处理规则

---

## 📊 数据库表结构

### 流程定义表 (doc_flow_definition)
- id, name, code, doc_type_id, description, version, status, creator_id

### 流程节点定义表 (doc_flow_node)
- id, flow_def_id, node_name, node_type, approver_type, approver_value
- order_num, skip_condition, required, parallel_mode, is_last_node

### 流程实例表 (doc_flow_instance)
- id, document_id, flow_def_id, flow_type, status, current_node_id
- start_time, end_time

### 节点实例表 (doc_flow_node_instance)
- id, flow_instance_id, node_id, approver_id, approver_dept_id
- status, comments, handled_at

### 待办事项表 (doc_todo_item)
- id, document_id, flow_instance_id, node_instance_id, assignee_id
- title, content, priority, status, due_date, handled_at

### 已办事项表 (doc_done_item)
- id, document_id, flow_instance_id, node_instance_id, handler_id
- title, action, comments, handled_at

---

## 🔧 使用示例

### 1. 启动流程

```java
// 启动流程
FlowInstance flowInstance = flowEngineService.startFlow(documentId, flowDefId);
```

### 2. 审批同意

```java
// 审批同意
flowApprovalService.approve(nodeInstanceId, "同意", approverId);
```

### 3. 查询待办事项

```java
// 查询待办事项
IPage<TodoItem> todos = todoService.getTodoItemsByAssignee(approverId, 1, 10);
```

---

## 📝 注意事项

1. **事务管理**：所有流程操作都在事务中执行，保证数据一致性
2. **权限验证**：审批前会验证审批人权限
3. **状态检查**：节点流转前会检查节点状态
4. **日志记录**：所有关键操作都有日志记录

---

## 🚀 下一步计划

1. **集成用户服务**
   - 实现用户角色查询
   - 实现部门负责人查询

2. **实现条件分支**
   - 集成 SpEL 表达式引擎
   - 实现条件表达式解析和评估

3. **完善转发和退回**
   - 实现转发逻辑
   - 实现退回逻辑

4. **性能优化**
   - 流程定义缓存
   - 批量操作优化

5. **测试验证**
   - 单元测试
   - 集成测试
   - 流程测试

---

**实现时间**: 2023.0.3.3  
**实现人**: XTT Cloud Team

