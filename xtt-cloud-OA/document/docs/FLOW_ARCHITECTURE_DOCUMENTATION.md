# 流程引擎架构文档

## 📋 概述

本文档详细描述了 Document 项目的流程引擎架构设计，包括整体架构、核心组件、设计模式、技术选型等。

**版本**: 2023.0.3.3  
**最后更新**: 2024年

---

## 🏗️ 整体架构

### 架构分层

```
┌─────────────────────────────────────────────────────────┐
│                    REST API Layer                         │
│  (FlowController, FreeFlowController)                    │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  Application Layer                       │
│  FlowService (统一对外接口)                              │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   Core Service Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │FlowEngine    │  │FreeFlowEngine│  │FlowApproval  │ │
│  │Service       │  │Service       │  │Service       │ │
│  └──────────────┘  └──────────────┘  └──────────────┘ │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │TaskService   │  │DocumentService│                   │
│  └──────────────┘  └──────────────┘                    │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
│  (Entity, Mapper)                                       │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                    │
│  (Database, MyBatis Plus)                               │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 核心组件

### 1. FlowService（流程服务 - 统一对外接口）

**职责**: 流程引擎的唯一对外接口，封装所有流程相关的业务操作。

**设计原则**:
- 所有流程操作必须通过 FlowService 进行
- 隐藏流程引擎的内部实现细节
- 统一异常处理和日志记录
- 保证事务一致性

**核心方法**:
```java
// 流程启动
FlowInstance startFlow(Long documentId, Long flowDefId)

// 审批处理
void approve(Long nodeInstanceId, String comments, Long approverId)
void reject(Long nodeInstanceId, String comments, Long approverId)
void forward(Long nodeInstanceId, String comments, Long approverId)
void returnBack(Long nodeInstanceId, String comments, Long approverId)

// 固定流和自由流整合
FlowNodeInstance executeFreeFlowInFixedNode(...)
FlowInstance startFixedSubFlowInFreeFlow(...)
void checkAndContinueParentFlow(Long subFlowInstanceId)
```

**依赖关系**:
- FlowEngineService（固定流引擎）
- FreeFlowEngineService（自由流引擎）
- FlowApprovalService（审批处理）
- TaskService（任务管理）
- DocumentService（文档服务）

---

### 2. FlowEngineService（固定流引擎服务）

**职责**: 处理固定流程的核心逻辑，包括流程启动、节点流转、流程结束等。

**核心功能**:
1. **流程启动**
   - 创建流程实例
   - 加载流程定义和节点列表
   - 创建第一个节点实例
   - 分配审批人
   - 生成待办事项

2. **节点流转**
   - 串行节点流转
   - 并行节点流转（会签/或签）
   - 条件节点流转
   - 自动节点处理

3. **流程结束**
   - 判断流程是否完成
   - 更新流程状态
   - 更新文档状态

**核心方法**:
```java
// 启动流程
FlowInstance startFlow(Long documentId, Long flowDefId)

// 处理节点审批
void processNodeApproval(Long nodeInstanceId, String action, String comments, Long approverId)

// 移动到下一个节点（私有）
private void moveToNextNode(Long flowInstanceId, Long currentNodeInstanceId)
```

**并行模式支持**:
- **会签（PARALLEL_ALL）**: 所有并行节点都完成后才能流转
- **或签（PARALLEL_ANY）**: 任一并行节点完成后即可流转

---

### 3. FreeFlowEngineService（自由流引擎服务）

**职责**: 处理自由流程的核心逻辑，支持动态流转。

**核心功能**:
1. **获取可用动作**
   - 根据文档状态和用户角色动态计算可用动作
   - 匹配动作规则

2. **执行发送动作**
   - 验证动作可用性
   - 验证审批人范围
   - 创建新的节点实例
   - 分配审批人

3. **审批人范围管理**
   - 获取动作对应的审批人选择范围
   - 验证选择的审批人是否在允许范围内

**核心方法**:
```java
// 获取可用动作
List<FlowAction> getAvailableActions(Long documentId, Long userId)

// 执行发送动作
FlowNodeInstance executeAction(Long currentNodeInstanceId, Long actionId, ...)

// 获取审批人选择范围
ApproverScope getApproverScope(Long actionId)
```

**动作类型**:
- `TYPE_UNIT_HANDLE = 1`: 单位内办理
- `TYPE_REVIEW = 2`: 核稿
- `TYPE_EXTERNAL = 3`: 转外单位办理
- `TYPE_RETURN = 4`: 返回

---

### 4. FlowApprovalService（审批处理服务）

**职责**: 提供审批相关的业务逻辑封装。

**核心方法**:
```java
// 审批同意
void approve(Long nodeInstanceId, String comments, Long approverId)

// 审批拒绝
void reject(Long nodeInstanceId, String comments, Long approverId)

// 审批转发
void forward(Long nodeInstanceId, String comments, Long approverId)

// 审批退回
void returnBack(Long nodeInstanceId, String comments, Long approverId)
```

**实现方式**: 所有方法都委托给 `FlowEngineService.processNodeApproval()` 处理。

---

### 5. TaskService（任务服务）

**职责**: 负责待办任务和已办任务的管理。

**核心功能**:
1. **待办任务管理**
   - 创建待办任务
   - 标记待办任务为已处理
   - 取消待办任务
   - 查询待办任务列表

2. **已办任务管理**
   - 创建已办任务
   - 查询已办任务列表

3. **任务类型支持**
   - 用户任务（TASK_TYPE_USER）
   - JAVA任务（TASK_TYPE_JAVA）
   - 其他任务（TASK_TYPE_OTHER）

**核心方法**:
```java
// 创建待办任务
void createTodoTask(FlowNodeInstance nodeInstance, Long approverId, ...)

// 创建已办任务
void createDoneTask(FlowNodeInstance nodeInstance, String action, ...)

// 标记待办任务为已处理
void markAsHandled(Long nodeInstanceId, Long approverId)

// 查询待办任务（分页）
IPage<TodoTask> getTodoTasksByAssignee(Long assigneeId, int pageNum, int pageSize)

// 查询已办任务（分页）
IPage<DoneTask> getDoneTasksByHandler(Long handlerId, int pageNum, int pageSize)
```

---

### 6. DocumentService（文档服务）

**职责**: 提供文档相关的业务逻辑。

**核心功能**:
- 创建文档
- 更新文档
- 删除文档（逻辑删除）
- 查询文档详情
- 分页查询文档列表

---

## 🔄 流程执行流程

### 固定流程执行流程

```
1. 启动流程
   ↓
2. 创建流程实例
   ↓
3. 加载流程定义和节点列表
   ↓
4. 创建第一个节点实例
   ↓
5. 分配审批人
   ↓
6. 生成待办事项
   ↓
7. 审批人处理待办
   ↓
8. 更新节点实例状态
   ↓
9. 创建已办任务
   ↓
10. 判断是否流转到下一个节点
    ↓
11. 是 → 创建下一个节点实例，重复步骤5-10
    否 → 流程结束
```

### 自由流程执行流程

```
1. 获取可用动作
   ↓
2. 用户选择动作和审批人
   ↓
3. 执行发送动作
   ↓
4. 验证动作可用性
   ↓
5. 验证审批人范围
   ↓
6. 创建新的节点实例
   ↓
7. 分配审批人
   ↓
8. 生成待办事项
   ↓
9. 审批人处理待办
   ↓
10. 重复步骤1-9（直到流程结束）
```

### 混合流程执行流程

```
固定流程节点
   ↓
判断节点是否允许自由流
   ↓
是 → 执行自由流转 → 继续固定流程
否 → 继续固定流程
```

---

## 🔀 节点流转算法

### 串行流转

```
当前节点完成
   ↓
查找下一个节点（orderNum + 1）
   ↓
创建节点实例
   ↓
分配审批人
   ↓
生成待办事项
```

### 并行流转 - 会签

```
当前节点完成
   ↓
识别并行节点组（相同 orderNum）
   ↓
为每个审批人创建节点实例
   ↓
为每个节点实例生成待办事项
   ↓
等待所有并行节点完成
   ↓
所有节点都完成 → 汇聚到下一个节点
```

### 并行流转 - 或签

```
当前节点完成
   ↓
识别并行节点组（相同 orderNum）
   ↓
为每个审批人创建节点实例
   ↓
为每个节点实例生成待办事项
   ↓
任一节点完成 → 汇聚到下一个节点
```

### 条件流转

```
当前节点完成
   ↓
评估条件表达式（SpEL）
   ↓
根据条件结果选择下一个节点
   ↓
创建节点实例
   ↓
分配审批人
   ↓
生成待办事项
```

---

## 🔗 固定流和自由流整合

### 整合架构

```
FlowService (协调层)
    ├── FlowEngineService (固定流引擎)
    │       └── executeFreeFlowInFixedNode() (在固定节点中执行自由流)
    └── FreeFlowEngineService (自由流引擎)
            └── startFixedSubFlow() (在自由流中启动固定子流程)
```

### 整合场景

#### 场景1: 固定流程中加入自由流转节点

**实现方式**:
- 在 `FlowNode` 中设置 `allowFreeFlow=1`
- 当节点允许自由流时，不自动流转到下一个节点
- 用户可以选择执行自由流动作
- 调用 `FlowService.executeFreeFlowInFixedNode()` 执行自由流转

**流程**:
```
固定流程节点（allowFreeFlow=1）
   ↓
用户选择自由流动作
   ↓
执行自由流转
   ↓
创建自由流节点实例
   ↓
继续固定流程的后续节点
```

#### 场景2: 自由流程中加入固定流程

**实现方式**:
- 在 `FlowInstance` 中通过 `parentFlowInstanceId` 建立父子关系
- 调用 `FlowService.startFixedSubFlowInFreeFlow()` 启动固定子流程
- 子流程完成后，调用 `FlowService.checkAndContinueParentFlow()` 通知父流程

**流程**:
```
自由流程节点
   ↓
用户选择启动固定子流程
   ↓
启动固定子流程
   ↓
固定子流程按照预定义流程执行
   ↓
子流程完成
   ↓
通知父流程继续
   ↓
父流程继续执行后续节点
```

---

## 🎨 设计模式

### 1. 门面模式（Facade Pattern）

**应用**: `FlowService` 作为流程引擎的门面，统一对外接口。

**优势**:
- 简化客户端调用
- 隐藏内部复杂性
- 降低耦合度

### 2. 策略模式（Strategy Pattern）

**应用**: 不同的节点类型使用不同的处理策略。

**实现**:
- 串行节点策略
- 并行节点策略（会签/或签）
- 条件节点策略
- 自动节点策略

### 3. 状态模式（State Pattern）

**应用**: 流程实例和节点实例的状态管理。

**状态转换**:
```
流程实例: 创建 → 进行中 → 已完成/已终止
节点实例: 创建 → 待处理 → 处理中 → 已完成/已拒绝/已跳过
```

### 4. 模板方法模式（Template Method Pattern）

**应用**: 流程执行的标准流程，具体步骤由子类实现。

---

## 🛠️ 技术选型

### 后端框架
- **Spring Boot**: 应用框架
- **MyBatis Plus**: ORM框架
- **Spring Transaction**: 事务管理

### 数据库
- **MySQL**: 关系型数据库
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci

### 服务发现与配置
- **Nacos**: 服务发现和配置中心

### 日志框架
- **SLF4J + Logback**: 日志记录

---

## 📦 包结构

```
xtt.cloud.oa.document
├── application                    # 应用层
│   ├── flow                       # 流程相关
│   │   ├── core                   # 核心服务
│   │   │   ├── FlowEngineService.java
│   │   │   ├── FreeFlowEngineService.java
│   │   │   ├── FlowApprovalService.java
│   │   │   ├── TaskService.java
│   │   │   └── DocumentService.java
│   │   └── FlowService.java       # 统一对外接口
│   └── gw                         # 公文相关
├── domain                         # 领域层
│   ├── entity                     # 实体类
│   │   ├── flow                   # 流程实体
│   │   │   ├── task               # 任务实体
│   │   │   └── ...
│   │   └── gw                     # 公文实体
│   └── mapper                     # Mapper接口
│       ├── flow                   # 流程Mapper
│       └── gw                     # 公文Mapper
├── interfaces                     # 接口层
│   └── rest                       # REST API
│       ├── flow                   # 流程Controller
│       └── dto                    # 数据传输对象
└── config                         # 配置类
```

---

## 🔒 事务管理

### 事务边界

所有流程操作都在事务中执行，确保数据一致性：

```java
@Transactional
public FlowInstance startFlow(Long documentId, Long flowDefId) {
    // 流程启动逻辑
}

@Transactional
public void processNodeApproval(...) {
    // 审批处理逻辑
}

@Transactional
public FlowNodeInstance executeAction(...) {
    // 执行动作逻辑
}
```

### 事务传播

- 默认使用 `REQUIRED` 传播级别
- 确保所有数据库操作在同一事务中

---

## 🚨 异常处理

### 业务异常

使用 `BusinessException` 处理业务异常：

```java
if (document == null) {
    throw new BusinessException("公文不存在");
}
```

### 异常处理策略

- 所有异常都在 `FlowService` 层统一捕获和处理
- 记录详细的错误日志
- 返回友好的错误信息给客户端

---

## 📊 性能优化

### 1. 数据库优化

- 为常用查询字段建立索引
- 使用分页查询减少数据量
- 避免N+1查询问题

### 2. 缓存策略

- 流程定义可以缓存（未来实现）
- 审批人分配结果可以缓存（未来实现）

### 3. 批量操作

- 使用批量插入减少数据库访问
- 使用批量更新提高性能

---

## 🔐 安全考虑

### 1. 权限验证

- 验证审批人是否有权限操作节点
- 验证节点状态是否正确
- 验证动作是否可用

### 2. 数据安全

- 使用参数化查询防止SQL注入
- 验证输入数据的合法性
- 记录操作日志

---

## 📈 扩展性设计

### 1. 节点类型扩展

通过 `nodeType` 字段支持新的节点类型：
- 审批节点
- 抄送节点
- 条件节点
- 自动节点
- 自由流节点

### 2. 审批人类型扩展

通过 `approverType` 字段支持新的审批人类型：
- 指定人员
- 指定角色
- 部门负责人
- 发起人指定

### 3. 动作类型扩展

通过 `actionType` 字段支持新的动作类型：
- 单位内办理
- 核稿
- 转外单位办理
- 返回

---

## 🧪 测试策略

### 单元测试

- 使用 JUnit 5 和 Mockito 进行单元测试
- 覆盖所有核心服务的方法
- 测试正常流程、边界情况和异常情况

### 集成测试

- 测试完整的流程执行流程
- 测试固定流和自由流的整合
- 测试并发场景

---

## 📝 最佳实践

### 1. 代码规范

- 使用有意义的变量名和方法名
- 添加详细的注释和文档
- 遵循单一职责原则

### 2. 日志记录

- 记录关键操作日志
- 使用适当的日志级别
- 包含足够的上下文信息

### 3. 错误处理

- 提供清晰的错误信息
- 记录详细的错误日志
- 优雅地处理异常情况

---

## 🚀 未来规划

### 1. 功能增强

- [ ] 流程撤回功能
- [ ] 流程回退功能
- [ ] 流程超时处理
- [ ] 流程统计和分析

### 2. 性能优化

- [ ] 流程定义缓存
- [ ] 审批人分配结果缓存
- [ ] 异步处理待办事项生成
- [ ] 消息队列解耦

### 3. 扩展功能

- [ ] 流程可视化
- [ ] 流程监控和告警
- [ ] 流程性能分析
- [ ] 流程模板市场

---

**文档版本**: 1.0  
**最后更新**: 2024年  
**维护人员**: XTT Cloud Team

