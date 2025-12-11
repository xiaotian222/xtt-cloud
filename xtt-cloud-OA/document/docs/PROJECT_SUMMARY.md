# Document 项目总结文档

## 📋 项目概述

Document 项目是一个基于 Spring Boot 的公文管理系统，集成了完整的流程引擎功能，支持固定流程、自由流程以及两者的混合使用。

**项目版本**: 2023.0.3.3  
**技术栈**: Spring Boot + MyBatis Plus + MySQL + Nacos  
**架构模式**: DDD（领域驱动设计）分层架构

---

## 🎯 核心功能

### 1. 文档管理

- **文档抽象**: Document 作为文档抽象基类
- **文档类型**: 支持发文（IssuanceInfo）和收文（ReceiptInfo）两种类型
- **文档状态**: 草稿、审核中、已发布、已归档
- **文档属性**: 标题、编号、内容、附件、密级、紧急程度等

### 2. 流程引擎

#### 2.1 固定流程（Fixed Flow）

- **流程定义**: 支持预定义的流程模板
- **节点类型**: 审批节点、抄送节点、条件节点、自动节点
- **并行模式**: 支持串行、会签（所有节点完成）、或签（任一节点完成）
- **审批人分配**: 支持指定人员、指定角色、部门负责人、发起人指定
- **条件流转**: 支持基于 SpEL 表达式的条件分支

#### 2.2 自由流程（Free Flow）

- **动态流转**: 根据文档状态和用户角色动态计算可用动作
- **发送动作**: 单位内办理、核稿、转外单位办理、返回
- **动作规则**: 基于文档状态、用户角色、部门的动作可用性规则
- **审批人范围**: 支持部门、人员、部门+人员三种范围类型

#### 2.3 混合流程（Mixed Flow）

- **固定流中自由流**: 在固定流程节点中允许执行自由流转
- **自由流中固定流**: 在自由流程中启动固定的子流程
- **流程模式**: 自动识别流程模式（固定流、自由流、混合流）

### 3. 任务管理

- **待办任务**: 支持用户任务、JAVA任务、其他任务三种类型
- **已办任务**: 记录所有已处理的任务
- **任务查询**: 支持按审批人、流程实例、任务类型查询
- **分页查询**: 支持分页查询待办和已办任务

### 4. 扩展功能

- **承办记录**: 记录流程中的承办信息
- **外单位签收**: 记录外单位的签收信息

---

## 🏗️ 架构设计

### 分层架构

```
REST API Layer (接口层)
    ↓
Application Layer (应用层)
    ├── FlowService (统一对外接口)
    └── Core Services (核心服务)
        ├── FlowEngineService (固定流引擎)
        ├── FreeFlowEngineService (自由流引擎)
        ├── FlowApprovalService (审批处理)
        ├── TaskService (任务管理)
        └── DocumentService (文档服务)
    ↓
Domain Layer (领域层)
    ├── Entity (实体类)
    └── Mapper (数据访问)
    ↓
Infrastructure Layer (基础设施层)
    └── Database (数据库)
```

### 核心服务职责

1. **FlowService**: 流程引擎的统一对外接口，封装所有流程操作
2. **FlowEngineService**: 处理固定流程的核心逻辑
3. **FreeFlowEngineService**: 处理自由流程的核心逻辑
4. **FlowApprovalService**: 提供审批相关的业务逻辑封装
5. **TaskService**: 负责待办任务和已办任务的管理
6. **DocumentService**: 提供文档相关的业务逻辑

### 设计模式

- **门面模式**: FlowService 作为流程引擎的门面
- **策略模式**: 不同的节点类型使用不同的处理策略
- **状态模式**: 流程实例和节点实例的状态管理
- **模板方法模式**: 流程执行的标准流程

---

## 📊 数据库设计

### 核心表结构

1. **文档相关表**
   - `document`: 文档表（抽象基表）
   - `issuance_info`: 发文信息表
   - `receipt_info`: 收文信息表

2. **流程定义相关表**
   - `flow_definition`: 流程定义表
   - `flow_node`: 流程节点定义表

3. **流程实例相关表**
   - `flow_instance`: 流程实例表
   - `flow_node_instance`: 节点实例表

4. **自由流相关表**
   - `flow_action`: 发送动作表
   - `flow_action_rule`: 动作规则表
   - `approver_scope`: 审批人选择范围表
   - `free_flow_node_instance`: 自由流节点实例扩展表

5. **任务相关表**
   - `todo_task`: 待办任务表
   - `done_task`: 已办任务表

6. **扩展功能表**
   - `handling`: 承办记录表
   - `external_sign_receipt`: 外单位签收登记表

### 表关系

- Document 1:1 IssuanceInfo/ReceiptInfo
- Document 1:N FlowInstance
- FlowDefinition 1:N FlowNode
- FlowDefinition 1:N FlowInstance
- FlowInstance 1:N FlowNodeInstance
- FlowNodeInstance 1:N TodoTask/DoneTask
- FlowAction 1:N FlowActionRule
- FlowAction 1:1 ApproverScope

详细数据库设计请参考 [DATABASE_DOCUMENTATION.md](./DATABASE_DOCUMENTATION.md)

---

## 🔄 流程执行流程

### 固定流程执行流程

```
启动流程 → 创建流程实例 → 创建第一个节点实例 → 
分配审批人 → 生成待办事项 → 审批处理 → 
节点流转 → 流程结束
```

### 自由流程执行流程

```
获取可用动作 → 选择动作和审批人 → 执行发送动作 → 
创建节点实例 → 分配审批人 → 生成待办事项 → 
审批处理 → 重复执行
```

### 混合流程执行流程

```
固定流程节点 → 判断是否允许自由流 → 
是：执行自由流转 → 继续固定流程
否：继续固定流程
```

详细流程架构请参考 [FLOW_ARCHITECTURE_DOCUMENTATION.md](./FLOW_ARCHITECTURE_DOCUMENTATION.md)

---

## 🛠️ 技术实现

### 后端技术栈

- **Spring Boot**: 应用框架
- **MyBatis Plus**: ORM框架，提供便捷的数据库操作
- **MySQL**: 关系型数据库
- **Nacos**: 服务发现和配置中心
- **SLF4J + Logback**: 日志框架

### 核心特性

1. **事务管理**: 所有流程操作都在事务中执行
2. **异常处理**: 统一的异常处理和错误信息返回
3. **日志记录**: 详细的操作日志记录
4. **单元测试**: 完整的单元测试覆盖

---

## 📁 项目结构

```
document/
├── docs/                          # 文档目录
│   ├── DATABASE_DOCUMENTATION.md  # 数据库文档
│   ├── FLOW_ARCHITECTURE_DOCUMENTATION.md  # 流程架构文档
│   ├── flow-engine-design.md     # 流程引擎设计
│   ├── FLOW_INTEGRATION_DESIGN.md # 固定流和自由流整合设计
│   └── ...
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── xtt/cloud/oa/document/
│   │   │       ├── application/   # 应用层
│   │   │       │   └── flow/      # 流程相关服务
│   │   │       ├── domain/        # 领域层
│   │   │       │   ├── entity/   # 实体类
│   │   │       │   └── mapper/   # Mapper接口
│   │   │       ├── interfaces/    # 接口层
│   │   │       │   └── rest/     # REST API
│   │   │       └── config/        # 配置类
│   │   └── resources/
│   │       └── application.yaml  # 配置文件
│   └── test/                      # 测试代码
│       └── java/                  # 单元测试
└── pom.xml                        # Maven配置
```

---

## 🔑 关键设计决策

### 1. 文档抽象设计

- **决策**: Document 作为抽象基类，IssuanceInfo 和 ReceiptInfo 作为子类型
- **原因**: 提高代码复用性，便于扩展新的文档类型
- **实现**: 通过 `documentId` 关联，而不是继承关系

### 2. 流程引擎分离

- **决策**: 将固定流引擎和自由流引擎分离为独立服务
- **原因**: 降低耦合度，提高可维护性
- **实现**: FlowService 作为协调层，统一对外接口

### 3. 循环依赖解决

- **决策**: 将整合逻辑提取到 FlowService，避免核心服务之间的循环依赖
- **原因**: 保持架构清晰，避免循环依赖问题
- **实现**: FlowEngineService 和 FreeFlowEngineService 不再直接依赖对方

### 4. 任务管理独立

- **决策**: 将任务管理从 FlowService 中分离到 TaskService
- **原因**: 职责分离，提高代码可维护性
- **实现**: TaskService 独立管理待办和已办任务

---

## 📈 项目亮点

### 1. 灵活的流程模式

- 支持固定流程、自由流程、混合流程三种模式
- 可以在固定流程中使用自由流转
- 可以在自由流程中启动固定子流程

### 2. 强大的并行支持

- 支持会签（所有节点完成）和或签（任一节点完成）两种并行模式
- 灵活的审批人分配策略

### 3. 完善的扩展性

- 支持多种节点类型
- 支持多种审批人类型
- 支持多种动作类型
- 易于扩展新的功能

### 4. 清晰的架构设计

- DDD 分层架构
- 职责分离明确
- 代码结构清晰
- 易于维护和扩展

---

## 🧪 测试覆盖

### 单元测试

- **FlowServiceTest**: FlowService 的单元测试
- **FlowEngineServiceTest**: FlowEngineService 的单元测试
- **FreeFlowEngineServiceTest**: FreeFlowEngineService 的单元测试
- **FlowApprovalServiceTest**: FlowApprovalService 的单元测试
- **TaskServiceTest**: TaskService 的单元测试

### 测试框架

- **JUnit 5**: 测试框架
- **Mockito**: Mock框架

详细测试说明请参考 [src/test/README.md](../src/test/README.md)

---

## 📚 相关文档

1. **数据库文档**: [DATABASE_DOCUMENTATION.md](./DATABASE_DOCUMENTATION.md)
   - 详细的表结构设计
   - 字段说明和索引
   - 表关系图

2. **流程架构文档**: [FLOW_ARCHITECTURE_DOCUMENTATION.md](./FLOW_ARCHITECTURE_DOCUMENTATION.md)
   - 整体架构设计
   - 核心组件说明
   - 流程执行流程

3. **流程引擎设计**: [flow-engine-design.md](./flow-engine-design.md)
   - 流程引擎核心逻辑
   - 节点流转算法
   - 审批处理流程

4. **固定流和自由流整合**: [FLOW_INTEGRATION_DESIGN.md](./FLOW_INTEGRATION_DESIGN.md)
   - 整合场景说明
   - 实现方式
   - 使用示例

5. **并行模式设计**: [PARALLEL_MODE_DESIGN.md](./PARALLEL_MODE_DESIGN.md)
   - 会签和或签的设计
   - 实现细节

---

## 🚀 未来规划

### 功能增强

- [ ] 流程撤回功能
- [ ] 流程回退功能
- [ ] 流程超时处理
- [ ] 流程统计和分析
- [ ] 流程可视化

### 性能优化

- [ ] 流程定义缓存
- [ ] 审批人分配结果缓存
- [ ] 异步处理待办事项生成
- [ ] 消息队列解耦

### 扩展功能

- [ ] 流程监控和告警
- [ ] 流程性能分析
- [ ] 流程模板市场
- [ ] 移动端支持

---

## 👥 团队信息

**项目名称**: Document Service  
**版本**: 2023.0.3.3  
**维护团队**: XTT Cloud Team  
**最后更新**: 2024年

---

## 📝 更新日志

### 2023.0.3.3

- ✅ 实现流程引擎核心功能
- ✅ 实现固定流程和自由流程
- ✅ 实现固定流和自由流的整合
- ✅ 实现并行模式（会签/或签）
- ✅ 实现任务管理（待办/已办）
- ✅ 实现单元测试
- ✅ 生成数据库文档和架构文档

---

**文档版本**: 1.0  
**最后更新**: 2024年

