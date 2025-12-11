# 并行模式设计文档

## 📋 概述

本文档详细说明并行节点的两种模式：会签和或签的设计与实现。

---

## 🎯 并行模式类型

### 1. 会签模式（PARALLEL_MODE_PARALLEL_ALL）

**定义**：所有并行节点都完成后，才能流转到下一个节点。

**使用场景**：
- 需要多人同时审批，所有人都同意才能通过
- 重要决策需要所有相关人员确认
- 财务审批需要多个部门负责人签字

**流转条件**：
- 所有并行节点实例的状态都为 `STATUS_COMPLETED`
- 只要有一个节点未完成，就继续等待

**示例**：
```
节点A（会签模式）
├── 节点实例A1（审批人：张三）→ 待处理
├── 节点实例A2（审批人：李四）→ 待处理
└── 节点实例A3（审批人：王五）→ 待处理

只有当张三、李四、王五都审批完成后，才能流转到下一个节点。
```

---

### 2. 或签模式（PARALLEL_MODE_PARALLEL_ANY）

**定义**：任一并行节点完成后，即可流转到下一个节点。

**使用场景**：
- 多个审批人任意一人审批即可
- 紧急情况下需要快速审批
- 多选一的审批场景

**流转条件**：
- 至少有一个并行节点实例的状态为 `STATUS_COMPLETED`
- 一旦有节点完成，立即流转，不再等待其他节点

**示例**：
```
节点A（或签模式）
├── 节点实例A1（审批人：张三）→ 待处理
├── 节点实例A2（审批人：李四）→ 待处理
└── 节点实例A3（审批人：王五）→ 待处理

只要张三、李四、王五中任意一人审批完成，就可以流转到下一个节点。
```

---

## 📊 数据模型

### FlowNode 实体

```java
public class FlowNode {
    private Integer parallelMode; // 并行模式
    
    // 并行模式常量
    public static final int PARALLEL_MODE_SERIAL = 0;      // 串行
    public static final int PARALLEL_MODE_PARALLEL_ALL = 1; // 并行-会签
    public static final int PARALLEL_MODE_PARALLEL_ANY = 2; // 并行-或签
    
    // 辅助方法
    public boolean isParallelMode() {
        return parallelMode != null && 
               (parallelMode == PARALLEL_MODE_PARALLEL_ALL || 
                parallelMode == PARALLEL_MODE_PARALLEL_ANY);
    }
    
    public boolean isParallelAllMode() {
        return parallelMode != null && parallelMode == PARALLEL_MODE_PARALLEL_ALL;
    }
    
    public boolean isParallelAnyMode() {
        return parallelMode != null && parallelMode == PARALLEL_MODE_PARALLEL_ANY;
    }
}
```

---

## 🔄 核心算法

### 1. 会签模式检查

```java
/**
 * 检查并行节点是否全部完成（会签模式）
 */
private boolean allParallelNodesCompleted(Long flowInstanceId, Long nodeId) {
    List<FlowNodeInstance> nodeInstances = flowNodeInstanceMapper.selectList(
        new LambdaQueryWrapper<FlowNodeInstance>()
            .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
            .eq(FlowNodeInstance::getNodeId, nodeId)
    );
    
    if (nodeInstances.isEmpty()) {
        return false;
    }
    
    // 检查是否所有节点实例都已完成
    return nodeInstances.stream()
        .allMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
}
```

### 2. 或签模式检查

```java
/**
 * 检查并行节点是否任一完成（或签模式）
 */
private boolean anyParallelNodeCompleted(Long flowInstanceId, Long nodeId) {
    List<FlowNodeInstance> nodeInstances = flowNodeInstanceMapper.selectList(
        new LambdaQueryWrapper<FlowNodeInstance>()
            .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
            .eq(FlowNodeInstance::getNodeId, nodeId)
    );
    
    if (nodeInstances.isEmpty()) {
        return false;
    }
    
    // 检查是否至少有一个节点实例已完成
    return nodeInstances.stream()
        .anyMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
}
```

### 3. 节点流转逻辑

```java
private void moveToNextNode(Long flowInstanceId, Long currentNodeInstanceId) {
    FlowNode currentNodeDef = flowNodeMapper.selectById(currentNode.getNodeId());
    
    if (currentNodeDef.isParallelMode()) {
        // 并行节点：根据模式检查是否满足流转条件
        boolean canMoveNext = false;
        
        if (currentNodeDef.isParallelAllMode()) {
            // 会签模式：所有节点都完成
            canMoveNext = allParallelNodesCompleted(flowInstanceId, currentNodeDef.getId());
        } else if (currentNodeDef.isParallelAnyMode()) {
            // 或签模式：任一节点完成
            canMoveNext = anyParallelNodeCompleted(flowInstanceId, currentNodeDef.getId());
        }
        
        if (canMoveNext) {
            // 流转到下一个节点
            // ...
        }
    } else {
        // 串行节点：直接流转
        // ...
    }
}
```

---

## 🎯 审批处理逻辑

### 审批同意后的处理

```java
private void handleApprove(FlowNodeInstance nodeInstance, String comments) {
    // 1. 更新节点实例状态
    nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED);
    // ...
    
    // 2. 判断并行模式
    FlowNode nodeDef = flowNodeMapper.selectById(nodeInstance.getNodeId());
    if (nodeDef.isParallelMode()) {
        boolean canMoveNext = false;
        
        if (nodeDef.isParallelAllMode()) {
            // 会签：检查是否所有节点都完成
            canMoveNext = allParallelNodesCompleted(
                nodeInstance.getFlowInstanceId(), nodeDef.getId());
        } else if (nodeDef.isParallelAnyMode()) {
            // 或签：检查是否任一节点完成
            canMoveNext = anyParallelNodeCompleted(
                nodeInstance.getFlowInstanceId(), nodeDef.getId());
        }
        
        if (canMoveNext) {
            moveToNextNode(nodeInstance.getFlowInstanceId(), nodeInstance.getId());
        }
    } else {
        // 串行节点：直接流转
        moveToNextNode(nodeInstance.getFlowInstanceId(), nodeInstance.getId());
    }
}
```

---

## 📝 数据库设计

### doc_flow_node 表

```sql
CREATE TABLE doc_flow_node (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  flow_def_id     BIGINT NOT NULL COMMENT '流程定义ID',
  node_name       VARCHAR(64) NOT NULL COMMENT '节点名称',
  node_type       TINYINT NOT NULL COMMENT '节点类型',
  approver_type   TINYINT NOT NULL COMMENT '审批人类型',
  approver_value  VARCHAR(255) COMMENT '审批人值',
  order_num       INT NOT NULL COMMENT '节点顺序',
  parallel_mode   TINYINT DEFAULT 0 COMMENT '并行模式(0:串行,1:并行-会签,2:并行-或签)',
  -- 其他字段...
);
```

**parallel_mode 字段说明**：
- `0`: 串行模式
- `1`: 并行-会签模式（所有节点都完成）
- `2`: 并行-或签模式（任一节点完成）

---

## 🔍 使用示例

### 示例1：会签模式

```java
// 创建会签节点
FlowNode node = new FlowNode();
node.setNodeName("部门负责人审批");
node.setParallelMode(FlowNode.PARALLEL_MODE_PARALLEL_ALL); // 会签模式
node.setApproverType(FlowNode.APPROVER_TYPE_DEPT_LEADER);
node.setApproverValue("1,2,3"); // 部门1、2、3的负责人

// 流程执行
// 1. 创建3个节点实例（分别分配给3个部门负责人）
// 2. 生成3个待办事项
// 3. 等待所有3个部门负责人都审批完成
// 4. 所有完成后，流转到下一个节点
```

### 示例2：或签模式

```java
// 创建或签节点
FlowNode node = new FlowNode();
node.setNodeName("紧急审批");
node.setParallelMode(FlowNode.PARALLEL_MODE_PARALLEL_ANY); // 或签模式
node.setApproverType(FlowNode.APPROVER_TYPE_ROLE);
node.setApproverValue("MANAGER"); // 任意一个经理

// 流程执行
// 1. 创建多个节点实例（分配给所有经理）
// 2. 生成多个待办事项
// 3. 任意一个经理审批完成即可
// 4. 一旦有节点完成，立即流转到下一个节点
```

---

## ⚠️ 注意事项

### 1. 拒绝处理

**会签模式**：
- 如果任一节点被拒绝，根据业务规则处理
- 如果节点是必须的（required=1），流程终止
- 如果节点不是必须的（required=0），可以跳过或继续等待其他节点

**或签模式**：
- 如果已完成节点被拒绝，需要重新处理
- 如果所有节点都被拒绝，流程终止

### 2. 节点状态

并行节点的每个实例都有独立的状态：
- `STATUS_PENDING`: 待处理
- `STATUS_PROCESSING`: 处理中
- `STATUS_COMPLETED`: 已完成
- `STATUS_REJECTED`: 已拒绝

### 3. 待办事项

- 会签模式：所有审批人都有待办事项，必须全部处理
- 或签模式：所有审批人都有待办事项，但只需一人处理即可

---

## 🚀 实现状态

- ✅ FlowNode 实体：添加并行模式常量和辅助方法
- ✅ FlowEngineService：实现会签和或签检查逻辑
- ✅ 节点流转：支持两种并行模式的流转判断
- ✅ 审批处理：支持两种并行模式的审批后处理

---

**设计时间**: 2023.0.3.3  
**设计人**: XTT Cloud Team

