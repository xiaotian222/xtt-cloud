# 自由流实现总结

## ✅ 已完成的工作

### 1. 实体类（Entity）

已创建以下实体类：

- ✅ `FlowAction` - 发送动作实体
- ✅ `FlowActionRule` - 动作规则实体
- ✅ `ApproverScope` - 审批人选择范围实体
- ✅ `FreeFlowNodeInstance` - 自由流节点实例扩展实体
- ✅ `FlowNode` - 流程节点定义实体（扩展）
- ✅ `FlowNodeInstance` - 节点实例实体

### 2. Mapper 接口

已创建以下 Mapper 接口：

- ✅ `FlowActionMapper`
- ✅ `FlowActionRuleMapper`
- ✅ `ApproverScopeMapper`
- ✅ `FreeFlowNodeInstanceMapper`
- ✅ `FlowNodeMapper`
- ✅ `FlowNodeInstanceMapper`

### 3. 核心服务

已实现 `FreeFlowService`，包含以下核心方法：

- ✅ `getAvailableActions()` - 获取可用发送动作
- ✅ `getApproverScope()` - 获取审批人选择范围
- ✅ `executeAction()` - 执行发送动作
- ✅ `matchActionRules()` - 匹配动作规则
- ✅ `validateApproverScope()` - 验证审批人范围
- ✅ `assignApprovers()` - 分配审批人
- ✅ `createFreeFlowNode()` - 创建自由流节点
- ✅ `handleReturnAction()` - 处理返回操作

### 4. REST API

已实现 `FreeFlowController`，包含以下接口：

- ✅ `GET /api/document/flows/{flowInstanceId}/available-actions` - 获取可用发送动作（通过流程实例ID）
- ✅ `GET /api/document/flows/documents/{documentId}/available-actions` - 获取可用发送动作（通过文档ID）
- ✅ `GET /api/document/flows/actions/{actionId}/approver-scope` - 获取审批人选择范围
- ✅ `POST /api/document/flows/node-instances/{nodeInstanceId}/execute-action` - 执行发送动作

### 5. DTO 类

已创建请求 DTO：

- ✅ `ExecuteActionRequest` - 执行发送动作请求DTO

---

## 🔧 核心功能说明

### 1. 获取可用发送动作

**逻辑流程：**
1. 获取文档信息（状态）
2. 获取用户角色
3. 匹配动作规则（根据文档状态和用户角色）
4. 返回可用的发送动作列表

**关键代码：**
```java
List<FlowAction> actions = freeFlowService.getAvailableActions(documentId, userId);
```

### 2. 获取审批人选择范围

**逻辑流程：**
1. 根据动作ID查询审批人范围配置
2. 如果没有配置，返回默认范围（允许所有部门和人员）
3. 返回范围类型、可选部门、可选人员等信息

**关键代码：**
```java
ApproverScope scope = freeFlowService.getApproverScope(actionId);
```

### 3. 执行发送动作

**逻辑流程：**
1. 验证当前节点权限和状态
2. 验证动作可用性
3. 验证审批人范围
4. 根据范围类型分配审批人
5. 创建自由流节点定义
6. 为每个审批人创建节点实例
7. 创建自由流节点扩展信息
8. 更新当前节点状态
9. 更新流程实例当前节点

**关键代码：**
```java
FlowNodeInstance nodeInstance = freeFlowService.executeAction(
    nodeInstanceId, actionId, selectedDeptIds, selectedUserIds, comment, operatorId
);
```

---

## 📝 待完成的工作

### 1. 用户服务集成

当前代码中有以下 TODO 需要完成：

- ⚠️ `getUserRoles()` - 获取用户角色（需要调用用户服务）
- ⚠️ `getDeptLeaderId()` - 获取部门负责人ID（需要调用用户服务）
- ⚠️ `getUserIdsByDeptId()` - 根据部门ID获取用户列表（需要调用用户服务）

**建议方案：**
- 创建 `PlatformClient` 调用 Platform 服务
- 或者使用 Feign 客户端调用用户服务接口

### 2. 待办事项生成

当前代码中创建了节点实例，但还没有生成待办事项。

**需要实现：**
- 创建 `TodoService` 服务
- 在 `executeAction()` 方法中调用 `TodoService.createTodoItem()`

### 3. 已办事项记录

当前代码中还没有创建已办事项记录。

**需要实现：**
- 创建 `DoneService` 服务
- 在节点完成时调用 `DoneService.createDoneItem()`

### 4. 流程引擎整合

需要创建 `FlowEngineService` 来整合固定流程和自由流。

**需要实现：**
- 流程模式检测
- 统一审批处理入口
- 固定流程和自由流的切换

### 5. 数据库表创建

需要创建以下数据库表：

- ⚠️ `doc_flow_action` - 发送动作定义表
- ⚠️ `doc_flow_action_rule` - 动作规则表
- ⚠️ `doc_flow_approver_scope` - 审批人选择范围表
- ⚠️ `doc_flow_free_node_instance` - 自由流节点实例扩展表
- ⚠️ `doc_flow_node` - 流程节点定义表（如果不存在）
- ⚠️ `doc_flow_node_instance` - 节点实例表（如果不存在）

---

## 🚀 使用示例

### 1. 获取可用发送动作

```bash
GET /api/document/flows/1/available-actions
Headers:
  X-User-Id: 123

Response:
{
  "code": 2001,
  "message": "接口调用成功",
  "data": [
    {
      "id": 1,
      "actionCode": "UNIT_HANDLE",
      "actionName": "单位内办理",
      "actionType": 1,
      "icon": "handle",
      "description": "发送给单位内部部门或人员办理"
    },
    {
      "id": 2,
      "actionCode": "REVIEW",
      "actionName": "核稿",
      "actionType": 2,
      "icon": "review",
      "description": "发送给核稿组或核稿人员"
    }
  ]
}
```

### 2. 获取审批人选择范围

```bash
GET /api/document/flows/actions/1/approver-scope

Response:
{
  "code": 2001,
  "message": "接口调用成功",
  "data": {
    "actionId": 1,
    "scopeType": 3,
    "deptIds": "[1,2,3]",
    "userIds": "[10,11,12]",
    "roleCodes": "MANAGER,REVIEWER",
    "allowCustom": 1
  }
}
```

### 3. 执行发送动作

```bash
POST /api/document/flows/node-instances/1/execute-action
Headers:
  X-User-Id: 123
Body:
{
  "actionId": 1,
  "selectedDeptIds": [1, 2],
  "selectedUserIds": [10, 11],
  "comment": "请相关部门办理"
}

Response:
{
  "code": 2001,
  "message": "接口调用成功",
  "data": {
    "nodeInstanceId": 123,
    "flowInstanceId": 1,
    "approverId": 10,
    "status": 0
  }
}
```

---

## 🔍 关键设计要点

### 1. 动作规则匹配

- 支持根据文档状态匹配
- 支持根据用户角色匹配（支持多个角色，逗号分隔）
- 支持通配符 `*` 表示所有角色
- 支持部门限制（可选）

### 2. 审批人范围验证

- 部门类型：验证部门是否在允许范围内
- 人员类型：验证人员是否在允许范围内
- 部门+人员类型：验证部门和人员组合
- 支持自定义选择（`allowCustom = 1`）

### 3. 动态节点创建

- 自由流节点是动态创建的，不存储在流程定义中
- 使用特殊顺序号（9999）标识自由流节点
- 每个自由流节点都有对应的扩展信息记录

### 4. 返回操作处理

- 查找上一个节点实例
- 如果没有上一个节点，退回给文档发起人
- 创建返回节点实例

---

## 📊 数据流转示例

### 场景：单位内办理

```
1. 用户A（部门经理）处理待办事项
   ↓
2. 调用 getAvailableActions() 获取可用动作
   → 返回：["单位内办理", "核稿", "返回"]
   ↓
3. 用户选择"单位内办理"
   ↓
4. 调用 getApproverScope() 获取审批人范围
   → 返回：部门+人员类型，可选部门[技术部, 市场部]
   ↓
5. 用户选择：技术部 + 张三、李四
   ↓
6. 调用 executeAction() 执行发送动作
   ↓
7. 系统创建节点实例：
   - 节点实例1：审批人=张三
   - 节点实例2：审批人=李四
   ↓
8. 生成待办事项（待实现）
   ↓
9. 张三、李四分别处理待办事项
```

---

## ⚠️ 注意事项

1. **用户服务集成**
   - 当前代码中用户服务相关方法都是 TODO
   - 需要集成 Platform 服务或创建 Feign 客户端

2. **待办事项生成**
   - 当前代码中创建了节点实例，但还没有生成待办事项
   - 需要实现 TodoService

3. **已办事项记录**
   - 当前代码中还没有创建已办事项记录
   - 需要实现 DoneService

4. **数据库表**
   - 需要创建相应的数据库表
   - 需要初始化基础数据（发送动作定义）

5. **错误处理**
   - 当前代码中有基本的异常处理
   - 建议增加更详细的错误信息

---

## 🎯 下一步计划

1. **集成用户服务**
   - 创建 PlatformClient 或使用 Feign
   - 实现 `getUserRoles()`, `getDeptLeaderId()`, `getUserIdsByDeptId()`

2. **实现待办已办服务**
   - 创建 TodoService 和 DoneService
   - 在节点创建和完成时调用

3. **创建数据库表**
   - 编写 SQL 脚本创建表
   - 初始化基础数据

4. **流程引擎整合**
   - 创建 FlowEngineService
   - 整合固定流程和自由流

5. **单元测试**
   - 编写单元测试验证核心逻辑
   - 编写集成测试验证 API

---

**实现时间**: 2023.0.3.3  
**实现人**: XTT Cloud Team

