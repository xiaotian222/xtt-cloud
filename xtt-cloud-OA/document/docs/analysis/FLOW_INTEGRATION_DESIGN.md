# 固定流和自由流整合设计文档

## 1. 概述

本文档描述了固定流（Fixed Flow）和自由流（Free Flow）的整合方案，实现了两种流程模式的混合使用。

## 2. 整合场景

### 2.1 固定流程中加入自由流转节点

**场景描述**：在固定流程的某个节点，允许使用自由流转方式进行审批。

**实现方式**：
- 在 `FlowNode` 实体中，通过 `allowFreeFlow` 字段标识节点是否允许自由流转
- 当 `allowFreeFlow=1` 时，该节点可以使用自由流引擎进行流转
- 在 `FlowEngineService` 中，当节点允许自由流时，不自动流转到下一个节点，而是等待用户主动执行自由流动作

**使用流程**：
1. 固定流程流转到允许自由流的节点
2. 用户在该节点可以选择执行自由流动作
3. 调用 `executeFreeFlowInFixedNode` 方法执行自由流转
4. 自由流转完成后，继续固定流程的后续节点

### 2.2 自由流程中加入固定流程

**场景描述**：在自由流程中，可以启动一个固定的子流程进行审批。

**实现方式**：
- 在 `FlowInstance` 实体中，通过 `parentFlowInstanceId` 字段建立父子流程关系
- 在 `FreeFlowEngineService` 中，提供 `startFixedSubFlow` 方法启动固定子流程
- 子流程完成后，通过 `checkAndContinueParentFlow` 方法通知父流程继续

**使用流程**：
1. 在自由流程的某个节点，用户选择启动固定子流程
2. 调用 `startFixedSubFlowInFreeFlow` 方法启动固定子流程
3. 固定子流程按照预定义的流程定义执行
4. 子流程完成后，调用 `checkAndContinueParentFlow` 方法通知父流程
5. 父流程继续执行后续节点

## 3. 数据模型

### 3.1 FlowInstance 扩展

```java
public class FlowInstance {
    private Long id;
    private Long documentId;
    private Long flowDefId;
    private Integer flowType;
    private Integer status;
    private Long currentNodeId;
    private Long parentFlowInstanceId;  // 新增：父流程实例ID
    private Integer flowMode;            // 新增：流程模式（1-固定流,2-自由流,3-混合流）
    // ... 其他字段
}
```

**流程模式说明**：
- `FLOW_MODE_FIXED = 1`：固定流，完全按照流程定义执行
- `FLOW_MODE_FREE = 2`：自由流，完全动态流转
- `FLOW_MODE_MIXED = 3`：混合流，固定流中包含允许自由流的节点

### 3.2 FlowNode 扩展

```java
public class FlowNode {
    private Integer isFreeFlow;      // 是否为自由流节点（0:否,1:是）
    private Integer allowFreeFlow;   // 是否允许在此节点使用自由流（0:不允许,1:允许）
    // ... 其他字段
}
```

## 4. 核心接口

### 4.1 FlowEngineService

#### executeFreeFlowInFixedNode

在固定流程节点中执行自由流转。

```java
@Transactional
public FlowNodeInstance executeFreeFlowInFixedNode(
        Long nodeInstanceId,
        Long actionId,
        List<Long> selectedDeptIds,
        List<Long> selectedUserIds,
        String comment,
        Long operatorId)
```

**参数说明**：
- `nodeInstanceId`：节点实例ID
- `actionId`：自由流动作ID
- `selectedDeptIds`：选择的部门ID列表
- `selectedUserIds`：选择的人员ID列表
- `comment`：备注
- `operatorId`：操作人ID

**返回值**：新创建的节点实例

### 4.2 FreeFlowEngineService

#### startFixedSubFlow

在自由流中启动固定子流程。

```java
@Transactional
public FlowInstance startFixedSubFlow(
        Long parentFlowInstanceId,
        Long documentId,
        Long flowDefId)
```

**参数说明**：
- `parentFlowInstanceId`：父流程实例ID（当前自由流实例）
- `documentId`：公文ID
- `flowDefId`：要启动的固定流程定义ID

**返回值**：子流程实例

#### checkAndContinueParentFlow

检查子流程是否完成并继续父流程。

```java
@Transactional
public void checkAndContinueParentFlow(Long subFlowInstanceId)
```

**参数说明**：
- `subFlowInstanceId`：子流程实例ID

## 5. REST API

### 5.1 在固定流程节点中执行自由流转

**接口**：`POST /api/document/flows/fixed-node/free-flow`

**请求参数**：
- `nodeInstanceId`：节点实例ID（必填）
- `actionId`：自由流动作ID（必填）
- `selectedDeptIds`：选择的部门ID列表（可选）
- `selectedUserIds`：选择的人员ID列表（可选）
- `comment`：备注（可选）
- `operatorId`：操作人ID（必填）

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "flowInstanceId": 456,
    "nodeId": 789,
    "approverId": 101,
    "status": 0
  }
}
```

### 5.2 在自由流中启动固定子流程

**接口**：`POST /api/document/flows/free-flow/fixed-sub-flow`

**请求参数**：
- `parentFlowInstanceId`：父流程实例ID（必填）
- `documentId`：公文ID（必填）
- `flowDefId`：流程定义ID（必填）

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "documentId": 456,
    "flowDefId": 789,
    "parentFlowInstanceId": 101,
    "flowMode": 1,
    "status": 0
  }
}
```

### 5.3 检查子流程并继续父流程

**接口**：`POST /api/document/flows/sub-flow/continue-parent`

**请求参数**：
- `subFlowInstanceId`：子流程实例ID（必填）

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": "操作成功"
}
```

## 6. 使用示例

### 6.1 固定流程中使用自由流转

```java
// 1. 固定流程流转到允许自由流的节点
FlowInstance flowInstance = flowService.startFlow(documentId, flowDefId);

// 2. 获取节点实例
FlowNodeInstance nodeInstance = getNodeInstance(flowInstanceId, nodeId);

// 3. 检查节点是否允许自由流
FlowNode node = flowNodeMapper.selectById(nodeInstance.getNodeId());
if (node.getAllowFreeFlow() == 1) {
    // 4. 获取可用的自由流动作
    List<FlowAction> actions = freeFlowEngineService.getAvailableActions(documentId, userId);
    
    // 5. 执行自由流转
    FlowNodeInstance newNodeInstance = flowService.executeFreeFlowInFixedNode(
        nodeInstance.getId(),
        actionId,
        selectedDeptIds,
        selectedUserIds,
        "自由流转备注",
        userId
    );
}
```

### 6.2 自由流中使用固定子流程

```java
// 1. 在自由流中启动固定子流程
FlowInstance subFlowInstance = flowService.startFixedSubFlowInFreeFlow(
    parentFlowInstanceId,
    documentId,
    fixedFlowDefId
);

// 2. 固定子流程按照预定义流程执行
// ... 子流程审批过程 ...

// 3. 子流程完成后，通知父流程继续
if (subFlowInstance.getStatus() == FlowInstance.STATUS_COMPLETED) {
    flowService.checkAndContinueParentFlow(subFlowInstance.getId());
}
```

## 7. 注意事项

1. **流程模式自动识别**：当固定流程定义中包含 `allowFreeFlow=1` 的节点时，流程实例会自动标记为 `FLOW_MODE_MIXED`。

2. **节点状态管理**：在固定流程节点中执行自由流转时，原节点实例会被标记为已完成，并创建新的自由流节点实例。

3. **父子流程关系**：子流程通过 `parentFlowInstanceId` 字段关联到父流程，支持多级嵌套。

4. **流程完成检查**：子流程完成后需要主动调用 `checkAndContinueParentFlow` 方法，系统不会自动检查。

5. **事务一致性**：所有整合操作都在事务中执行，确保数据一致性。

## 8. 未来扩展

1. **自动检查子流程完成**：可以添加定时任务或事件监听机制，自动检查子流程完成状态。

2. **流程回退支持**：支持在混合流程中进行回退操作。

3. **流程可视化**：在流程图中展示固定流和自由流的混合使用情况。

4. **流程统计**：统计混合流程的使用情况和性能指标。


