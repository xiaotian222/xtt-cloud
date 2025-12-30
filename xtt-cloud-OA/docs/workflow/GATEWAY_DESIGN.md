# 网关设计文档

## 📋 概述

网关（Gateway）用于控制流程的分支和汇聚，替代原来的 `parallelMode` 字段控制方式。

## 🎯 网关类型

### 1. 并行网关（Parallel Gateway）

并行网关用于将流程分成多个并行执行的分支，并在所有分支完成后汇聚。

#### 1.1 并行网关 Split（分支开始）

- **类型**：`PARALLEL_SPLIT`
- **作用**：将流程分成多个并行分支
- **特点**：所有分支都会被执行

#### 1.2 并行网关 Join（分支汇聚）

- **类型**：`PARALLEL_JOIN`
- **作用**：等待所有并行分支完成后汇聚
- **模式**：
  - **会签模式（PARALLEL_ALL）**：所有分支都完成才汇聚
  - **或签模式（PARALLEL_ANY）**：任一分支完成即可汇聚

### 2. 条件网关（Condition Gateway）

条件网关用于根据条件表达式和流程变量，选择性地执行某些分支。

#### 2.1 条件网关 Split（条件分支开始）

- **类型**：`CONDITION_SPLIT`
- **作用**：根据条件表达式选择执行的分支
- **条件表达式**：使用 SpEL（Spring Expression Language）格式
- **示例**：
  ```spel
  secretLevel > 1 && urgencyLevel == 2
  amount > 10000
  deptId == 1 || deptId == 2
  ```

#### 2.2 条件网关 Join（条件分支汇聚）

- **类型**：`CONDITION_JOIN`
- **作用**：等待所有激活的分支完成后汇聚
- **特点**：只等待实际执行的分支，未激活的分支不等待

## 📊 数据模型

### FlowNode 新增字段

```java
private Integer gatewayType;        // 网关类型
private Integer gatewayMode;        // 并行网关模式（仅用于并行网关）
private Long gatewayId;             // 网关ID（用于关联Split和Join）
private String conditionExpression; // 条件表达式（仅用于条件网关）
```

### Gateway 实体

```java
public class Gateway {
    private Long id;
    private Long flowDefId;              // 流程定义ID
    private String gatewayName;          // 网关名称
    private GatewayType gatewayType;     // 网关类型
    private GatewayMode gatewayMode;     // 并行网关模式
    private Long splitNodeId;            // Split 节点ID
    private Long joinNodeId;             // Join 节点ID
    private String conditionExpression;  // 条件表达式
}
```

## 🔄 使用场景

### 场景1：并行会签

```
节点A → [并行Split] → 节点B ┐
                          ├→ [并行Join] → 节点C
节点A → [并行Split] → 节点D ┘
```

- Split 节点：`gatewayType = PARALLEL_SPLIT`, `gatewayMode = PARALLEL_ALL`
- Join 节点：`gatewayType = PARALLEL_JOIN`, `gatewayMode = PARALLEL_ALL`
- 执行逻辑：节点B和节点D都完成后，才能流转到节点C

### 场景2：并行或签

```
节点A → [并行Split] → 节点B ┐
                          ├→ [并行Join] → 节点C
节点A → [并行Split] → 节点D ┘
```

- Split 节点：`gatewayType = PARALLEL_SPLIT`, `gatewayMode = PARALLEL_ANY`
- Join 节点：`gatewayType = PARALLEL_JOIN`, `gatewayMode = PARALLEL_ANY`
- 执行逻辑：节点B或节点D任一完成后，即可流转到节点C

### 场景3：条件分支

```
节点A → [条件Split] → 节点B（条件：amount > 10000）┐
                    → 节点C（条件：amount <= 10000）┘
                                                    ↓
                                              [条件Join] → 节点D
```

- Split 节点：`gatewayType = CONDITION_SPLIT`, `conditionExpression = "amount > 10000"`
- Join 节点：`gatewayType = CONDITION_JOIN`
- 执行逻辑：根据 amount 的值，选择执行节点B或节点C，然后汇聚到节点D

## 🔧 实现要点

### 1. Split 分支处理

- **并行Split**：创建所有分支的节点实例
- **条件Split**：根据条件表达式评估，只创建满足条件的分支节点实例

### 2. Join 汇聚处理

- **并行Join（会签）**：检查所有分支的节点实例是否都完成
- **并行Join（或签）**：检查是否有任一分支的节点实例完成
- **条件Join**：检查所有激活分支的节点实例是否都完成

### 3. 条件表达式评估

使用 `ConditionEvaluationService` 评估 SpEL 表达式：

```java
boolean result = conditionEvaluationService.evaluate(
    conditionExpression, 
    processVariables
);
```

## 📝 迁移说明

### 从 parallelMode 迁移到网关模式

1. **旧代码**（已废弃但保留兼容）：
   ```java
   node.setParallelMode(PARALLEL_MODE_PARALLEL_ALL);
   ```

2. **新代码**（使用网关）：
   ```java
   // 创建并行网关Split
   Gateway splitGateway = Gateway.createParallelSplit(
       flowDefId, 
       "并行审批", 
       GatewayMode.PARALLEL_ALL
   );
   
   // 创建并行网关Join
   Gateway joinGateway = Gateway.createParallelJoin(
       flowDefId, 
       "汇聚节点", 
       GatewayMode.PARALLEL_ALL,
       splitGateway.getId()
   );
   
   // 设置节点为网关节点
   splitNode.setGatewayType(GatewayType.PARALLEL_SPLIT);
   splitNode.setGatewayMode(GatewayMode.PARALLEL_ALL);
   splitNode.setGatewayId(splitGateway.getId());
   ```

## 🎨 流程图示例

```
开始 → 节点A → [并行Split-会签] → 节点B ┐
                                      ├→ [并行Join] → 节点C
                    [并行Split-会签] → 节点D ┘
                                      ↓
                    [条件Split] → 节点E（条件：x > 10）┐
                                      → 节点F（条件：x <= 10）┘
                                                          ↓
                                                    [条件Join] → 结束
```

## ✅ 优势

1. **更灵活**：支持复杂的并行和条件分支逻辑
2. **更清晰**：网关模式更符合 BPMN 标准
3. **更易维护**：Split 和 Join 成对出现，逻辑清晰
4. **更强大**：条件网关支持复杂的 SpEL 表达式

## 🔗 相关文档

- [DDD_PROJECT_STRUCTURE.md](./DDD_PROJECT_STRUCTURE.md) - DDD 项目结构
- [NodeRoutingService](../src/main/java/xtt/cloud/oa/workflow/domain/flow/service/NodeRoutingService.java) - 节点路由服务

