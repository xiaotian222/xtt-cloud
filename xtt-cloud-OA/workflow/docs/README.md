# Workflow DDD 模块

## 📁 已创建的目录结构

### 1. 值对象（Value Objects）
位于 `domain/flow/model/valueobject/`

- ✅ **FlowStatus** - 流程状态（进行中、已完成、已终止、已暂停、已取消）
- ✅ **NodeStatus** - 节点状态（待处理、处理中、已完成、已拒绝、已跳过）
- ✅ **FlowType** - 流程类型（发文、收文）
- ✅ **FlowMode** - 流程模式（固定流、自由流、混合流）
- ✅ **ProcessVariables** - 流程变量（支持 JSON 序列化）
- ✅ **FlowInstanceId** - 流程实例ID（类型安全）
- ✅ **Approver** - 审批人（用户ID、部门ID、用户名、部门名）

### 2. 聚合根（Aggregate Root）
位于 `domain/flow/model/aggregate/`

- ✅ **FlowInstance** - 流程实例聚合根
  - 管理流程生命周期（启动、完成、终止、暂停、恢复、取消）
  - 管理流程变量
  - 管理节点实例集合
  - 发布领域事件

### 3. 实体（Entity）
位于 `domain/flow/model/entity/`

- ✅ **FlowNodeInstance** - 节点实例实体
  - 属于 FlowInstance 聚合
  - 管理节点状态转换（待处理、处理中、已完成、已拒绝、已跳过）

### 4. 领域事件（Domain Events）
位于 `domain/flow/event/`

- ✅ **FlowStartedEvent** - 流程启动事件
- ✅ **FlowCompletedEvent** - 流程完成事件
- ✅ **FlowTerminatedEvent** - 流程终止事件
- ✅ **NodeCompletedEvent** - 节点完成事件

### 5. 领域服务接口（Domain Services）
位于 `domain/flow/service/`

- ✅ **NodeRoutingService** - 节点路由服务
  - 计算下一个节点
  - 判断汇聚节点
  - 判断节点是否跳过

- ✅ **ConditionEvaluationService** - 条件评估服务
  - 评估条件表达式（SpEL）
  - 验证表达式有效性

- ✅ **ApproverAssignmentService** - 审批人分配服务
  - 分配审批人
  - 验证审批权限

### 6. 仓储接口（Repository Interfaces）
位于 `domain/flow/repository/`

- ✅ **FlowInstanceRepository** - 流程实例仓储接口
- ✅ **FlowNodeInstanceRepository** - 节点实例仓储接口

## ✅ 已完成的工作

### 1. 应用层（Application Layer）✅
- ✅ **命令对象（Command）**
  - `StartFlowCommand` - 启动流程命令
  - `ApproveCommand` - 审批通过命令
  - `RejectCommand` - 审批拒绝命令
  - `WithdrawCommand` - 撤回流程命令
  - `RollbackCommand` - 回退流程命令

- ✅ **查询对象（Query）**
  - `FlowInstanceQuery` - 流程实例查询
  - `TodoTaskQuery` - 待办任务查询

- ✅ **DTO**
  - `FlowInstanceDTO` - 流程实例DTO
  - `FlowNodeInstanceDTO` - 节点实例DTO

- ✅ **组装器（Assembler）**
  - `FlowInstanceAssembler` - 流程实例组装器
  - `FlowNodeInstanceAssembler` - 节点实例组装器

- ✅ **应用服务**
  - `FlowApplicationService` - 流程应用服务（包含启动、审批、撤回、回退、暂停、恢复等操作）

### 2. 基础设施层（Infrastructure Layer）✅
- ✅ **仓储实现**
  - `FlowInstanceRepositoryImpl` - 流程实例仓储实现（框架代码，待完善）
  - `FlowNodeInstanceRepositoryImpl` - 节点实例仓储实现（框架代码，待完善）

### 3. 接口层（Interfaces Layer）✅
- ✅ **REST 控制器**
  - `FlowController` - 流程 REST API 控制器

- ✅ **接口层 DTO**
  - `StartFlowRequest` - 启动流程请求
  - `ApproveRequest` - 审批通过请求
  - `RejectRequest` - 审批拒绝请求
  - `WithdrawRequest` - 撤回流程请求
  - `RollbackRequest` - 回退流程请求

## 🔄 待完善的工作

### 1. 领域服务实现（Domain Service Implementation）
- ⏳ `domain/flow/service/impl/NodeRoutingServiceImpl` - 节点路由服务实现
- ⏳ `domain/flow/service/impl/ConditionEvaluationServiceImpl` - 条件评估服务实现
- ⏳ `domain/flow/service/impl/ApproverAssignmentServiceImpl` - 审批人分配服务实现

### 2. 基础设施层完善
- ⏳ `infrastructure/persistence/mapper/` - MyBatis Mapper 接口和 XML
- ⏳ `infrastructure/persistence/converter/` - 数据转换器（Entity <-> PO）
- ⏳ `infrastructure/messaging/event/` - 事件发布实现
- ⏳ `infrastructure/messaging/handler/` - 事件处理器

### 3. 业务逻辑完善
- ⏳ 完善 `FlowApplicationService` 中的审批、回退等业务逻辑
- ⏳ 实现节点流转逻辑（串行、并行、条件流转）
- ⏳ 实现审批人分配逻辑
- ⏳ 实现条件表达式评估（SpEL）
- ⏳ 实现流程变量管理
- ⏳ 实现待办任务生成和管理
- ⏳ 实现流程历史记录

## 📚 设计原则

### 值对象（Value Objects）
- ✅ 不可变（Immutable）
- ✅ 通过值相等性比较
- ✅ 无唯一标识

### 聚合根（Aggregate Root）
- ✅ 管理聚合内的所有实体和值对象
- ✅ 保证业务不变式
- ✅ 通过ID进行唯一标识
- ✅ 控制聚合内对象的访问

### 实体（Entity）
- ✅ 有唯一标识（ID）
- ✅ 可变（Mutable）
- ✅ 通过ID相等性比较
- ✅ 属于某个聚合

### 领域服务（Domain Service）
- ✅ 无状态的业务逻辑
- ✅ 跨聚合的操作
- ✅ 复杂的业务规则

### 仓储（Repository）
- ✅ 定义在领域层
- ✅ 实现在基础设施层
- ✅ 封装数据访问逻辑

## 🔗 相关文档

- `../../docs/DDD_PROJECT_STRUCTURE.md` - 详细的项目结构说明
- `../../docs/DDD_REFACTORING_GUIDE.md` - DDD 重构指南

