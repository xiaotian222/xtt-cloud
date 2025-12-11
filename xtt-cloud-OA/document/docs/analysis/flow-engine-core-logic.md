# 流程引擎核心逻辑详细设计

## 📋 核心问题讨论

### 1. 节点流转的核心挑战

#### 问题1：如何确定下一个节点？

**方案A：基于顺序号（orderNum）**
- 优点：简单直观，易于理解
- 缺点：不支持复杂的分支逻辑

**方案B：显式指定下一个节点（nextNodeId）**
- 优点：灵活，支持任意分支
- 缺点：需要维护节点关系

**推荐方案：混合方案**
- 串行节点：使用 orderNum + 1
- 并行节点：使用 nextNodeIds 列表
- 条件节点：根据条件选择 nextNodeId

#### 问题2：并行节点如何汇聚？

**方案A：等待所有节点完成**
- 所有并行节点都完成后，才流转到下一个节点
- 适用于"会签"场景

**方案B：任一节点完成即可**
- 任一并行节点完成后，就流转到下一个节点
- 适用于"或签"场景

**推荐方案：可配置**
- 在流程定义中添加 `parallelMode` 字段
- `0`: 串行
- `1`: 并行（全部完成）
- `2`: 或签（任一完成）

#### 问题3：节点跳过条件如何实现？

**方案A：表达式引擎**
- 使用 SpEL（Spring Expression Language）
- 支持复杂的条件表达式

**方案B：简单规则引擎**
- 预定义规则类型
- 配置规则参数

**推荐方案：SpEL 表达式**
- 灵活强大
- Spring 原生支持
- 示例：`secretLevel > 1 && urgencyLevel == 2`

---

## 🔄 节点流转详细算法

### 算法1：串行节点流转

```java
/**
 * 串行节点流转算法
 */
private void moveToNextSerialNode(Long flowInstanceId, Long currentNodeInstanceId) {
    // 1. 获取当前节点实例和节点定义
    FlowNodeInstance currentNode = nodeInstanceMapper.selectById(currentNodeInstanceId);
    FlowNode currentNodeDef = flowNodeMapper.selectById(currentNode.getNodeId());
    
    // 2. 查找下一个节点（orderNum + 1）
    FlowNode nextNode = flowNodeMapper.selectOne(
        new LambdaQueryWrapper<FlowNode>()
            .eq(FlowNode::getFlowDefId, flowInstance.getFlowDefId())
            .eq(FlowNode::getOrderNum, currentNodeDef.getOrderNum() + 1)
    );
    
    // 3. 检查跳过条件
    if (nextNode != null && shouldSkipNode(nextNode, flowInstance)) {
        // 跳过当前节点，继续查找下一个
        moveToNextSerialNode(flowInstanceId, currentNodeInstanceId);
        return;
    }
    
    // 4. 创建下一个节点实例
    if (nextNode != null) {
        createNodeInstance(flowInstance, nextNode);
    } else {
        // 没有下一个节点，流程结束
        completeFlow(flowInstanceId);
    }
}
```

### 算法2：并行节点流转

```java
/**
 * 并行节点流转算法
 */
private void moveToNextParallelNodes(Long flowInstanceId, Long currentNodeInstanceId) {
    FlowInstance flowInstance = flowInstanceMapper.selectById(flowInstanceId);
    FlowNodeInstance currentNode = nodeInstanceMapper.selectById(currentNodeInstanceId);
    FlowNode currentNodeDef = flowNodeMapper.selectById(currentNode.getNodeId());
    
    // 1. 查找并行节点组（相同 orderNum）
    List<FlowNode> parallelNodes = flowNodeMapper.selectList(
        new LambdaQueryWrapper<FlowNode>()
            .eq(FlowNode::getFlowDefId, flowInstance.getFlowDefId())
            .eq(FlowNode::getOrderNum, currentNodeDef.getOrderNum() + 1)
            .eq(FlowNode::getParallelMode, PARALLEL_MODE_PARALLEL)
    );
    
    // 2. 为每个并行节点创建节点实例
    for (FlowNode node : parallelNodes) {
        List<Long> approverIds = assignApprovers(node, flowInstance);
        for (Long approverId : approverIds) {
            FlowNodeInstance nodeInstance = createNodeInstance(flowInstance, node, approverId);
            createTodoItem(nodeInstance, approverId);
        }
    }
    
    // 3. 更新流程实例当前节点（指向并行节点组）
    flowInstance.setCurrentNodeId(parallelNodes.get(0).getId());
    flowInstanceMapper.updateById(flowInstance);
}
```

### 算法3：并行节点汇聚判断

```java
/**
 * 检查并行节点是否全部完成
 */
private boolean allParallelNodesCompleted(Long flowInstanceId, Long nodeId) {
    // 1. 获取所有并行节点实例
    List<FlowNodeInstance> nodeInstances = nodeInstanceMapper.selectList(
        new LambdaQueryWrapper<FlowNodeInstance>()
            .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
            .eq(FlowNodeInstance::getNodeId, nodeId)
    );
    
    // 2. 检查是否全部完成
    return nodeInstances.stream()
        .allMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
}

/**
 * 检查并行节点是否任一完成（或签）
 */
private boolean anyParallelNodeCompleted(Long flowInstanceId, Long nodeId) {
    List<FlowNodeInstance> nodeInstances = nodeInstanceMapper.selectList(
        new LambdaQueryWrapper<FlowNodeInstance>()
            .eq(FlowNodeInstance::getFlowInstanceId, flowInstanceId)
            .eq(FlowNodeInstance::getNodeId, nodeId)
    );
    
    return nodeInstances.stream()
        .anyMatch(ni -> ni.getStatus() == FlowNodeInstance.STATUS_COMPLETED);
}
```

### 算法4：条件分支判断

```java
/**
 * 条件节点分支判断
 */
private FlowNode evaluateConditionNode(FlowNode conditionNode, FlowInstance flowInstance) {
    // 1. 获取公文信息
    Document document = documentMapper.selectById(flowInstance.getDocumentId());
    
    // 2. 创建表达式上下文
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariable("document", document);
    context.setVariable("flowInstance", flowInstance);
    
    // 3. 解析并执行条件表达式
    ExpressionParser parser = new SpelExpressionParser();
    Expression expression = parser.parseExpression(conditionNode.getSkipCondition());
    Boolean result = expression.getValue(context, Boolean.class);
    
    // 4. 根据结果选择下一个节点
    if (result != null && result) {
        // 条件为真，返回 true 分支节点
        return flowNodeMapper.selectById(conditionNode.getTrueNextNodeId());
    } else {
        // 条件为假，返回 false 分支节点
        return flowNodeMapper.selectById(conditionNode.getFalseNextNodeId());
    }
}
```

---

## ✅ 审批处理详细逻辑

### 审批操作处理

```java
/**
 * 处理审批操作
 */
public void processApproval(Long nodeInstanceId, String action, String comments, Long approverId) {
    // 1. 验证权限和状态
    FlowNodeInstance nodeInstance = validateApprovalPermission(nodeInstanceId, approverId);
    
    // 2. 根据操作类型处理
    switch (action) {
        case ACTION_APPROVE:
            handleApprove(nodeInstance, comments);
            break;
        case ACTION_REJECT:
            handleReject(nodeInstance, comments);
            break;
        case ACTION_FORWARD:
            handleForward(nodeInstance, comments);
            break;
        case ACTION_RETURN:
            handleReturn(nodeInstance, comments);
            break;
        default:
            throw new BusinessException("不支持的操作类型: " + action);
    }
}

/**
 * 处理同意操作
 */
private void handleApprove(FlowNodeInstance nodeInstance, String comments) {
    // 1. 更新节点实例状态
    nodeInstance.setStatus(FlowNodeInstance.STATUS_COMPLETED);
    nodeInstance.setComments(comments);
    nodeInstance.setHandledAt(LocalDateTime.now());
    nodeInstanceMapper.updateById(nodeInstance);
    
    // 2. 创建已办事项
    createDoneItem(nodeInstance, ACTION_APPROVE, comments);
    
    // 3. 更新待办事项状态
    todoService.markAsHandled(nodeInstance.getId(), nodeInstance.getApproverId());
    
    // 4. 判断是否需要等待并行节点
    FlowNode nodeDef = flowNodeMapper.selectById(nodeInstance.getNodeId());
    if (nodeDef.getParallelMode() == PARALLEL_MODE_PARALLEL) {
        // 并行节点：检查是否所有节点都完成
        if (allParallelNodesCompleted(nodeInstance.getFlowInstanceId(), nodeDef.getId())) {
            moveToNextNode(nodeInstance.getFlowInstanceId(), nodeInstance.getId());
        }
    } else {
        // 串行节点：直接流转
        moveToNextNode(nodeInstance.getFlowInstanceId(), nodeInstance.getId());
    }
}

/**
 * 处理拒绝操作
 */
private void handleReject(FlowNodeInstance nodeInstance, String comments) {
    // 1. 更新节点实例状态
    nodeInstance.setStatus(FlowNodeInstance.STATUS_REJECTED);
    nodeInstance.setComments(comments);
    nodeInstance.setHandledAt(LocalDateTime.now());
    nodeInstanceMapper.updateById(nodeInstance);
    
    // 2. 创建已办事项
    createDoneItem(nodeInstance, ACTION_REJECT, comments);
    
    // 3. 更新待办事项状态
    todoService.markAsHandled(nodeInstance.getId(), nodeInstance.getApproverId());
    
    // 4. 终止流程或退回
    FlowInstance flowInstance = flowInstanceMapper.selectById(nodeInstance.getFlowInstanceId());
    FlowNode nodeDef = flowNodeMapper.selectById(nodeInstance.getNodeId());
    
    if (nodeDef.getRequired() == 1) {
        // 必须节点被拒绝，流程终止
        terminateFlow(flowInstance.getId(), "节点被拒绝: " + comments);
    } else {
        // 非必须节点被拒绝，可以跳过或退回
        // 根据业务规则处理
    }
}
```

---

## 🔍 关键设计决策

### 决策1：节点实例 vs 节点定义

**节点定义（FlowNode）**：流程模板，定义节点规则
**节点实例（FlowNodeInstance）**：流程执行时的具体实例

**为什么需要节点实例？**
- 并行节点需要为每个审批人创建独立的实例
- 可以记录每个实例的处理状态和意见
- 支持节点重试和转发

### 决策2：当前节点如何表示？

**方案A：使用 currentNodeId（节点定义ID）**
- 优点：简单
- 缺点：无法区分并行节点的不同实例

**方案B：使用 currentNodeInstanceId（节点实例ID）**
- 优点：精确
- 缺点：并行节点时无法表示

**推荐方案：混合方案**
- `currentNodeId`: 指向节点定义（用于并行节点组）
- 并行节点时，检查该节点组的所有实例

### 决策3：流程结束判断

**方案A：检查最后一个节点是否完成**
- 简单，但不够灵活

**方案B：检查所有节点是否完成**
- 准确，但性能可能有问题

**推荐方案：标记最后一个节点**
- 在节点定义中添加 `isLastNode` 标志
- 最后一个节点完成时，流程结束

---

## 🎯 实现步骤建议

### 第一步：创建实体类和 Mapper

1. 创建 FlowDefinition 实体和 Mapper
2. 创建 FlowNode 实体和 Mapper
3. 创建 FlowNodeInstance 实体和 Mapper
4. 创建 TodoItem 实体和 Mapper
5. 创建 DoneItem 实体和 Mapper

### 第二步：实现基础服务

1. FlowDefinitionService - 流程定义管理
2. FlowNodeService - 节点定义管理
3. FlowNodeInstanceService - 节点实例管理

### 第三步：实现流程引擎核心

1. FlowEngineService - 流程启动
2. FlowEngineService - 节点流转（串行）
3. FlowEngineService - 审批处理

### 第四步：实现高级功能

1. 并行流程处理
2. 条件分支处理
3. 审批人自动分配

### 第五步：实现待办已办

1. TodoService - 待办事项管理
2. DoneService - 已办事项管理

---

## 💡 设计模式建议

### 1. 策略模式 - 审批人分配

```java
public interface ApproverAssignStrategy {
    List<Long> assignApprovers(FlowNode node, FlowInstance flowInstance);
}

public class UserApproverStrategy implements ApproverAssignStrategy {
    // 指定人员分配
}

public class RoleApproverStrategy implements ApproverAssignStrategy {
    // 角色分配
}

public class DeptLeaderApproverStrategy implements ApproverAssignStrategy {
    // 部门负责人分配
}
```

### 2. 状态模式 - 节点状态流转

```java
public interface NodeState {
    void handle(FlowNodeInstance nodeInstance, String action);
}

public class PendingState implements NodeState {
    // 待处理状态
}

public class ProcessingState implements NodeState {
    // 处理中状态
}

public class CompletedState implements NodeState {
    // 已完成状态
}
```

### 3. 责任链模式 - 节点流转

```java
public abstract class NodeHandler {
    protected NodeHandler nextHandler;
    
    public abstract void handle(FlowNodeInstance nodeInstance);
    
    public void setNext(NodeHandler handler) {
        this.nextHandler = handler;
    }
}

public class SerialNodeHandler extends NodeHandler {
    // 串行节点处理
}

public class ParallelNodeHandler extends NodeHandler {
    // 并行节点处理
}

public class ConditionNodeHandler extends NodeHandler {
    // 条件节点处理
}
```

---

## 🚨 边界情况处理

### 1. 并行节点部分完成

**场景**：3个并行节点，2个完成，1个拒绝

**处理方案**：
- 方案A：全部完成才流转（会签模式）
- 方案B：任一完成即流转（或签模式）
- 方案C：可配置规则（推荐）

### 2. 节点跳过条件

**场景**：节点设置了跳过条件，条件满足时跳过

**处理方案**：
- 创建节点实例，但状态设为"已跳过"
- 不生成待办事项
- 直接流转到下一个节点

### 3. 流程撤回

**场景**：发起人想要撤回已提交的流程

**处理方案**：
- 检查当前节点是否允许撤回
- 取消所有待办事项
- 更新流程状态为"已撤回"

### 4. 审批超时

**场景**：审批人在规定时间内未处理

**处理方案**：
- 定时任务检查超时的待办事项
- 发送提醒通知
- 可配置自动处理规则（自动同意/自动退回）

---

## 📊 性能考虑

### 1. 批量操作

- 并行节点创建时，使用批量插入
- 待办事项生成时，使用批量插入

### 2. 缓存策略

- 流程定义和节点定义可以缓存
- 审批人分配结果可以缓存

### 3. 异步处理

- 待办事项生成可以异步化
- 通知发送可以异步化
- 使用消息队列解耦

---

## 🔐 安全考虑

### 1. 权限验证

- 审批前验证审批人权限
- 防止越权操作

### 2. 数据一致性

- 使用事务保证数据一致性
- 使用乐观锁防止并发问题

### 3. 审计日志

- 记录所有审批操作
- 记录流程状态变更

---

## 📝 下一步行动

1. **讨论确认设计方案**
   - 节点流转算法
   - 并行处理机制
   - 条件分支实现

2. **创建实体类**
   - 根据设计创建所有实体类

3. **实现核心算法**
   - 先实现串行流程
   - 再实现并行流程
   - 最后实现条件分支

4. **测试验证**
   - 单元测试
   - 集成测试
   - 流程测试

---

**讨论时间**: 2023.0.3.3  
**参与人**: XTT Cloud Team

