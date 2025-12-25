# DDD 重构指南

## 📋 当前代码问题分析

### 1. 贫血模型（Anemic Domain Model）
**问题**：
- 实体类只是简单的 POJO，只有 getter/setter
- 所有业务逻辑都在 Service 层
- 实体类没有行为，只是数据容器

**示例**：
```java
// 当前代码：FlowInstance 只是数据容器
public class FlowInstance {
    private Long id;
    private Integer status;
    // 只有 getter/setter，没有业务方法
}

// 业务逻辑在 Service 层
public class FlowEngineService {
    public void moveToNextNode(FlowInstance instance, FlowNodeInstance node) {
        // 所有业务逻辑都在这里
        if (instance.getStatus() == FlowInstance.STATUS_PROCESSING) {
            // ...
        }
    }
}
```

### 2. 业务逻辑分散
**问题**：
- 流程状态判断逻辑分散在多个 Service 中
- 节点流转逻辑在 FlowEngineService
- 审批逻辑在 FlowApprovalService
- 节点创建逻辑在 FlowNodeService
- 缺乏统一的业务规则管理

### 3. 缺乏领域概念
**问题**：
- 没有值对象（Value Object），用基本类型表示概念
- 没有聚合根（Aggregate Root）的概念
- 实体之间的关系不清晰
- 没有领域事件（Domain Event）

### 4. 职责不清
**问题**：
- Service 层既负责业务逻辑，又负责数据持久化
- 实体类没有封装业务规则
- 缺乏领域服务（Domain Service）的概念

---

## 🎯 DDD 重构原因

### 1. 提高代码可维护性
- **业务逻辑集中**：业务规则集中在领域层，易于理解和修改
- **减少重复代码**：通过领域模型复用业务逻辑
- **清晰的边界**：领域层、应用层、基础设施层职责清晰

### 2. 提高业务表达能力
- **领域语言**：代码更接近业务语言，易于与业务人员沟通
- **业务规则显式化**：业务规则在领域模型中明确表达
- **领域概念清晰**：值对象、实体、聚合根等概念清晰

### 3. 提高代码质量
- **单一职责**：每个类职责单一，符合 SOLID 原则
- **封装性**：业务规则封装在领域模型中，外部不能随意修改
- **可测试性**：领域模型易于单元测试

### 4. 支持复杂业务场景
- **聚合根管理**：通过聚合根管理实体生命周期
- **领域事件**：支持事件驱动架构
- **业务规则扩展**：易于扩展新的业务规则

---

## 🏗️ DDD 重构思路

### 第一步：识别领域模型

#### 1. 聚合根（Aggregate Root）
- **FlowInstance**：流程实例聚合根
  - 管理流程生命周期
  - 管理节点实例
  - 管理流程状态

#### 2. 实体（Entity）
- **FlowNodeInstance**：节点实例实体
- **FlowDefinition**：流程定义实体（可能是另一个聚合根）
- **FlowNode**：节点定义实体（属于 FlowDefinition 聚合）

#### 3. 值对象（Value Object）
- **FlowStatus**：流程状态值对象
- **NodeStatus**：节点状态值对象
- **Approver**：审批人值对象
- **ProcessVariables**：流程变量值对象
- **FlowType**：流程类型值对象

#### 4. 领域服务（Domain Service）
- **NodeRoutingService**：节点路由领域服务
- **ApproverAssignmentService**：审批人分配领域服务
- **ConditionEvaluationService**：条件评估领域服务

#### 5. 领域事件（Domain Event）
- **FlowStartedEvent**：流程启动事件
- **NodeCompletedEvent**：节点完成事件
- **FlowCompletedEvent**：流程完成事件
- **FlowTerminatedEvent**：流程终止事件

---

### 第二步：重构聚合根

#### FlowInstance 聚合根重构

**当前问题**：
```java
// 贫血模型
public class FlowInstance {
    private Long id;
    private Integer status;  // 用基本类型表示状态
    // 没有业务方法
}
```

**重构后**：
```java
/**
 * 流程实例聚合根
 * 负责管理流程的生命周期和状态转换
 */
public class FlowInstance {
    private FlowInstanceId id;
    private DocumentId documentId;
    private FlowDefinitionId flowDefId;
    private FlowStatus status;  // 值对象
    private FlowType flowType;  // 值对象
    private FlowMode flowMode;  // 值对象
    private NodeId currentNodeId;
    private ProcessVariables variables;  // 值对象
    private List<FlowNodeInstance> nodeInstances;  // 实体集合
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // ========== 业务方法 ==========
    
    /**
     * 启动流程
     * 领域方法，封装启动流程的业务规则
     */
    public void start(FlowDefinition flowDef, Document document) {
        // 业务规则验证
        if (!flowDef.isEnabled()) {
            throw new FlowDefinitionDisabledException();
        }
        
        if (flowDef.getNodes().isEmpty()) {
            throw new FlowDefinitionInvalidException("流程定义没有配置节点");
        }
        
        // 状态转换
        this.status = FlowStatus.PROCESSING;
        this.flowDefId = flowDef.getId();
        this.documentId = document.getId();
        this.startTime = LocalDateTime.now();
        
        // 创建第一个节点实例
        FlowNode firstNode = flowDef.getFirstNode();
        FlowNodeInstance firstNodeInstance = createFirstNodeInstance(firstNode, document);
        this.nodeInstances.add(firstNodeInstance);
        this.currentNodeId = firstNode.getId();
        
        // 发布领域事件
        DomainEventPublisher.publish(new FlowStartedEvent(this.id, document.getId()));
    }
    
    /**
     * 完成节点
     * 封装节点完成的业务逻辑
     */
    public void completeNode(NodeInstanceId nodeInstanceId, ApproverId approverId, String comments) {
        FlowNodeInstance nodeInstance = findNodeInstance(nodeInstanceId);
        
        // 业务规则验证
        if (!nodeInstance.canBeCompletedBy(approverId)) {
            throw new NodeCompletionException("无权完成此节点");
        }
        
        // 完成节点
        nodeInstance.complete(approverId, comments);
        
        // 判断是否可以流转
        if (canMoveToNextNode(nodeInstance)) {
            moveToNextNode(nodeInstance);
        }
        
        // 判断流程是否完成
        if (isCompleted()) {
            complete();
        }
    }
    
    /**
     * 判断是否可以流转到下一个节点
     */
    private boolean canMoveToNextNode(FlowNodeInstance currentNode) {
        FlowNode nodeDef = getNodeDefinition(currentNode.getNodeId());
        
        if (nodeDef.isParallelMode()) {
            if (nodeDef.isParallelAllMode()) {
                return allParallelNodesCompleted(nodeDef);
            } else if (nodeDef.isParallelAnyMode()) {
                return anyParallelNodeCompleted(nodeDef);
            }
        }
        
        return true;
    }
    
    /**
     * 流转到下一个节点
     */
    private void moveToNextNode(FlowNodeInstance currentNode) {
        FlowNode nodeDef = getNodeDefinition(currentNode.getNodeId());
        List<FlowNode> nextNodes = nodeDef.getNextNodes();
        
        if (nextNodes.isEmpty()) {
            return;  // 没有下一个节点，流程将在后续判断中完成
        }
        
        for (FlowNode nextNode : nextNodes) {
            // 检查跳过条件
            if (shouldSkipNode(nextNode)) {
                FlowNodeInstance skippedNode = createSkippedNodeInstance(nextNode);
                this.nodeInstances.add(skippedNode);
                moveToNextNode(skippedNode);  // 递归处理
                continue;
            }
            
            // 创建节点实例
            FlowNodeInstance newNodeInstance = createNodeInstance(nextNode);
            this.nodeInstances.add(newNodeInstance);
        }
        
        // 更新当前节点
        this.currentNodeId = nextNodes.get(0).getId();
    }
    
    /**
     * 完成流程
     */
    public void complete() {
        if (this.status != FlowStatus.PROCESSING) {
            throw new IllegalStateException("流程状态不正确，无法完成");
        }
        
        this.status = FlowStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        
        // 发布领域事件
        DomainEventPublisher.publish(new FlowCompletedEvent(this.id, this.documentId));
    }
    
    /**
     * 终止流程
     */
    public void terminate(String reason) {
        if (this.status != FlowStatus.PROCESSING) {
            throw new IllegalStateException("流程状态不正确，无法终止");
        }
        
        this.status = FlowStatus.TERMINATED;
        this.endTime = LocalDateTime.now();
        
        // 取消所有待办任务
        cancelAllPendingTasks();
        
        // 发布领域事件
        DomainEventPublisher.publish(new FlowTerminatedEvent(this.id, reason));
    }
    
    /**
     * 撤回流程
     */
    public void withdraw(UserId initiatorId, String reason) {
        // 业务规则验证
        if (this.status != FlowStatus.PROCESSING) {
            throw new FlowWithdrawException("只能撤回进行中的流程");
        }
        
        if (!canBeWithdrawnBy(initiatorId)) {
            throw new FlowWithdrawException("无权撤回此流程");
        }
        
        // 状态转换
        this.status = FlowStatus.WITHDRAWN;
        this.endTime = LocalDateTime.now();
        
        // 取消所有待办任务
        cancelAllPendingTasks();
        
        // 发布领域事件
        DomainEventPublisher.publish(new FlowWithdrawnEvent(this.id, initiatorId, reason));
    }
    
    /**
     * 回退到指定节点
     */
    public void rollbackToNode(NodeId targetNodeId, UserId operatorId, String reason) {
        // 业务规则验证
        if (!canRollbackTo(targetNodeId, operatorId)) {
            throw new FlowRollbackException("无法回退到指定节点");
        }
        
        // 取消当前节点及之后的所有待办任务
        cancelTasksAfterNode(targetNodeId);
        
        // 更新当前节点
        this.currentNodeId = targetNodeId;
        
        // 创建目标节点的节点实例
        FlowNode targetNode = getNodeDefinition(targetNodeId);
        FlowNodeInstance newNodeInstance = createNodeInstance(targetNode);
        this.nodeInstances.add(newNodeInstance);
        
        // 发布领域事件
        DomainEventPublisher.publish(new FlowRollbackEvent(this.id, targetNodeId, operatorId, reason));
    }
    
    // ========== 查询方法 ==========
    
    public boolean isCompleted() {
        return this.status == FlowStatus.COMPLETED;
    }
    
    public boolean isProcessing() {
        return this.status == FlowStatus.PROCESSING;
    }
    
    public boolean canBeWithdrawnBy(UserId userId) {
        // 业务规则：只有发起人可以撤回
        // TODO: 需要从 Document 获取发起人ID
        return true;  // 简化实现
    }
    
    // ========== 私有辅助方法 ==========
    
    private FlowNodeInstance findNodeInstance(NodeInstanceId nodeInstanceId) {
        return this.nodeInstances.stream()
            .filter(ni -> ni.getId().equals(nodeInstanceId))
            .findFirst()
            .orElseThrow(() -> new NodeInstanceNotFoundException());
    }
    
    private FlowNode getNodeDefinition(NodeId nodeId) {
        // 通过领域服务获取节点定义
        return nodeDefinitionService.getNodeDefinition(this.flowDefId, nodeId);
    }
    
    private boolean allParallelNodesCompleted(FlowNode nodeDef) {
        List<FlowNodeInstance> nodeInstances = getNodeInstancesByNodeId(nodeDef.getId());
        return nodeInstances.stream()
            .allMatch(ni -> ni.isCompleted());
    }
    
    private boolean anyParallelNodeCompleted(FlowNode nodeDef) {
        List<FlowNodeInstance> nodeInstances = getNodeInstancesByNodeId(nodeDef.getId());
        return nodeInstances.stream()
            .anyMatch(ni -> ni.isCompleted());
    }
    
    private boolean shouldSkipNode(FlowNode node) {
        if (!node.hasSkipCondition()) {
            return false;
        }
        
        // 使用领域服务评估条件
        return conditionEvaluationService.evaluate(node.getSkipCondition(), this.variables);
    }
    
    private FlowNodeInstance createNodeInstance(FlowNode node) {
        // 分配审批人
        List<Approver> approvers = approverAssignmentService.assignApprovers(
            node, this, getDocument()
        );
        
        // 创建节点实例
        FlowNodeInstance nodeInstance = FlowNodeInstance.create(
            this.id,
            node.getId(),
            approvers
        );
        
        return nodeInstance;
    }
    
    private void cancelAllPendingTasks() {
        this.nodeInstances.stream()
            .filter(ni -> ni.isPending())
            .forEach(ni -> ni.cancel());
    }
}
```

---

### 第三步：创建值对象

#### FlowStatus 值对象
```java
/**
 * 流程状态值对象
 * 不可变对象，封装状态相关的业务规则
 */
public class FlowStatus {
    public static final FlowStatus PROCESSING = new FlowStatus(0, "进行中");
    public static final FlowStatus COMPLETED = new FlowStatus(1, "已完成");
    public static final FlowStatus TERMINATED = new FlowStatus(2, "已终止");
    public static final FlowStatus WITHDRAWN = new FlowStatus(3, "已撤回");
    public static final FlowStatus SUSPENDED = new FlowStatus(4, "已挂起");
    
    private final int value;
    private final String description;
    
    private FlowStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canTransitionTo(FlowStatus target) {
        // 状态转换规则
        if (this == PROCESSING) {
            return target == COMPLETED || target == TERMINATED || target == WITHDRAWN || target == SUSPENDED;
        }
        if (this == SUSPENDED) {
            return target == PROCESSING || target == TERMINATED;
        }
        return false;  // 其他状态不能转换
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowStatus that = (FlowStatus) o;
        return value == that.value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
```

#### NodeStatus 值对象
```java
/**
 * 节点状态值对象
 */
public class NodeStatus {
    public static final NodeStatus PENDING = new NodeStatus(0, "待处理");
    public static final NodeStatus PROCESSING = new NodeStatus(1, "处理中");
    public static final NodeStatus COMPLETED = new NodeStatus(2, "已完成");
    public static final NodeStatus REJECTED = new NodeStatus(3, "已拒绝");
    public static final NodeStatus SKIPPED = new NodeStatus(4, "已跳过");
    
    private final int value;
    private final String description;
    
    // 类似 FlowStatus 的实现
}
```

#### Approver 值对象
```java
/**
 * 审批人值对象
 */
public class Approver {
    private final UserId userId;
    private final String userName;
    private final DeptId deptId;
    private final String deptName;
    
    public Approver(UserId userId, String userName, DeptId deptId, String deptName) {
        this.userId = userId;
        this.userName = userName;
        this.deptId = deptId;
        this.deptName = deptName;
    }
    
    public boolean isSameUser(UserId userId) {
        return this.userId.equals(userId);
    }
    
    // getters...
}
```

---

### 第四步：重构实体

#### FlowNodeInstance 实体重构

**重构后**：
```java
/**
 * 节点实例实体
 * 属于 FlowInstance 聚合
 */
public class FlowNodeInstance {
    private NodeInstanceId id;
    private FlowInstanceId flowInstanceId;
    private NodeId nodeId;
    private Approver approver;  // 值对象
    private NodeStatus status;  // 值对象
    private String comments;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;
    
    // ========== 业务方法 ==========
    
    /**
     * 创建节点实例
     * 工厂方法
     */
    public static FlowNodeInstance create(
            FlowInstanceId flowInstanceId,
            NodeId nodeId,
            List<Approver> approvers) {
        // 为每个审批人创建节点实例
        // 这里简化处理，实际应该返回多个实例
        return new FlowNodeInstance(flowInstanceId, nodeId, approvers.get(0));
    }
    
    /**
     * 完成节点
     */
    public void complete(ApproverId approverId, String comments) {
        // 业务规则验证
        if (!this.approver.isSameUser(approverId)) {
            throw new NodeCompletionException("无权完成此节点");
        }
        
        if (this.status != NodeStatus.PENDING) {
            throw new NodeCompletionException("节点状态不正确");
        }
        
        // 状态转换
        this.status = NodeStatus.COMPLETED;
        this.comments = comments;
        this.handledAt = LocalDateTime.now();
        
        // 发布领域事件
        DomainEventPublisher.publish(new NodeCompletedEvent(
            this.id,
            this.flowInstanceId,
            this.nodeId,
            approverId
        ));
    }
    
    /**
     * 拒绝节点
     */
    public void reject(ApproverId approverId, String comments) {
        // 业务规则验证
        if (!this.approver.isSameUser(approverId)) {
            throw new NodeRejectionException("无权拒绝此节点");
        }
        
        // 状态转换
        this.status = NodeStatus.REJECTED;
        this.comments = comments;
        this.handledAt = LocalDateTime.now();
        
        // 发布领域事件
        DomainEventPublisher.publish(new NodeRejectedEvent(
            this.id,
            this.flowInstanceId,
            this.nodeId,
            approverId,
            comments
        ));
    }
    
    /**
     * 跳过节点
     */
    public void skip(String reason) {
        this.status = NodeStatus.SKIPPED;
        this.comments = reason;
        this.handledAt = LocalDateTime.now();
    }
    
    /**
     * 取消节点
     */
    public void cancel() {
        if (this.status == NodeStatus.PENDING) {
            this.status = NodeStatus.CANCELLED;
        }
    }
    
    // ========== 查询方法 ==========
    
    public boolean canBeCompletedBy(ApproverId approverId) {
        return this.status == NodeStatus.PENDING 
            && this.approver.isSameUser(approverId);
    }
    
    public boolean isPending() {
        return this.status == NodeStatus.PENDING;
    }
    
    public boolean isCompleted() {
        return this.status == NodeStatus.COMPLETED;
    }
}
```

---

### 第五步：创建领域服务

#### NodeRoutingService 领域服务
```java
/**
 * 节点路由领域服务
 * 处理跨聚合的节点路由逻辑
 */
@Service
public class NodeRoutingService {
    
    private final FlowNodeRepository flowNodeRepository;
    private final ConditionEvaluationService conditionEvaluationService;
    
    /**
     * 获取下一个节点列表
     */
    public List<FlowNode> getNextNodes(FlowDefinitionId flowDefId, FlowNode currentNode) {
        // 1. 优先使用 nextNodeIds
        if (currentNode.hasNextNodeIds()) {
            return getNodesByIds(flowDefId, currentNode.getNextNodeIds());
        }
        
        // 2. 使用 nextNodeId
        if (currentNode.hasNextNodeId()) {
            FlowNode nextNode = flowNodeRepository.findById(currentNode.getNextNodeId());
            return Collections.singletonList(nextNode);
        }
        
        // 3. 使用 orderNum
        return flowNodeRepository.findByFlowDefIdAndOrderNum(
            flowDefId, currentNode.getOrderNum() + 1);
    }
    
    /**
     * 判断是否为汇聚节点
     */
    public boolean isConvergenceNode(FlowDefinitionId flowDefId, FlowNode node) {
        // 查找所有指向该节点的节点
        List<FlowNode> pointingNodes = flowNodeRepository.findNodesPointingTo(
            flowDefId, node.getId());
        return pointingNodes.size() > 1;
    }
}
```

#### ConditionEvaluationService 领域服务
```java
/**
 * 条件评估领域服务
 * 使用 SpEL 评估条件表达式
 */
@Service
public class ConditionEvaluationService {
    
    private final SpelExpressionParser parser = new SpelExpressionParser();
    
    /**
     * 评估条件表达式
     */
    public boolean evaluate(String conditionExpression, ProcessVariables variables) {
        if (StringUtils.isEmpty(conditionExpression)) {
            return false;
        }
        
        try {
            Expression expression = parser.parseExpression(conditionExpression);
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(variables.toMap());
            return expression.getValue(context, Boolean.class);
        } catch (Exception e) {
            throw new ConditionEvaluationException("条件表达式评估失败: " + conditionExpression, e);
        }
    }
}
```

---

### 第六步：重构应用服务

#### FlowEngineService 重构为应用服务

**重构后**：
```java
/**
 * 流程引擎应用服务
 * 协调领域对象完成业务流程
 * 不包含业务逻辑，只负责协调
 */
@Service
public class FlowEngineApplicationService {
    
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowDefinitionRepository flowDefinitionRepository;
    private final DocumentRepository documentRepository;
    private final TaskApplicationService taskApplicationService;
    private final FlowHistoryApplicationService flowHistoryApplicationService;
    
    /**
     * 启动流程
     * 应用服务方法，协调领域对象
     */
    @Transactional
    public FlowInstanceId startFlow(DocumentId documentId, FlowDefinitionId flowDefId) {
        // 1. 加载聚合根
        FlowDefinition flowDef = flowDefinitionRepository.findById(flowDefId);
        Document document = documentRepository.findById(documentId);
        
        // 2. 创建流程实例聚合根
        FlowInstance flowInstance = FlowInstance.create(documentId, flowDefId);
        
        // 3. 调用领域方法
        flowInstance.start(flowDef, document);
        
        // 4. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 5. 处理副作用（创建待办任务、记录历史等）
        // 可以通过领域事件处理，或者在这里处理
        handleFlowStarted(flowInstance);
        
        return flowInstance.getId();
    }
    
    /**
     * 完成节点
     */
    @Transactional
    public void completeNode(NodeInstanceId nodeInstanceId, ApproverId approverId, String comments) {
        // 1. 加载聚合根
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(nodeInstanceId);
        FlowInstance flowInstance = flowInstanceRepository.findById(nodeInstance.getFlowInstanceId());
        
        // 2. 调用领域方法
        flowInstance.completeNode(nodeInstanceId, approverId, comments);
        
        // 3. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 4. 处理副作用
        handleNodeCompleted(nodeInstance, flowInstance);
    }
    
    /**
     * 撤回流程
     */
    @Transactional
    public void withdrawFlow(FlowInstanceId flowInstanceId, UserId initiatorId, String reason) {
        // 1. 加载聚合根
        FlowInstance flowInstance = flowInstanceRepository.findById(flowInstanceId);
        
        // 2. 调用领域方法
        flowInstance.withdraw(initiatorId, reason);
        
        // 3. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 4. 处理副作用
        handleFlowWithdrawn(flowInstance);
    }
    
    // ========== 私有辅助方法 ==========
    
    private void handleFlowStarted(FlowInstance flowInstance) {
        // 创建待办任务
        // 记录历史
        // 发送通知
    }
    
    private void handleNodeCompleted(FlowNodeInstance nodeInstance, FlowInstance flowInstance) {
        // 创建已办任务
        // 更新待办任务状态
        // 记录历史
    }
    
    private void handleFlowWithdrawn(FlowInstance flowInstance) {
        // 取消待办任务
        // 记录历史
        // 发送通知
    }
}
```

---

## 📊 重构前后对比

### 重构前（贫血模型）
```java
// 实体：只有数据
public class FlowInstance {
    private Integer status;
    // 只有 getter/setter
}

// 业务逻辑在 Service
public class FlowEngineService {
    public void moveToNextNode(FlowInstance instance, FlowNodeInstance node) {
        if (instance.getStatus() == FlowInstance.STATUS_PROCESSING) {
            // 业务逻辑...
        }
    }
}
```

### 重构后（充血模型）
```java
// 实体：包含业务逻辑
public class FlowInstance {
    private FlowStatus status;  // 值对象
    
    public void moveToNextNode(FlowNodeInstance node) {
        if (!this.status.canTransitionTo(FlowStatus.PROCESSING)) {
            throw new IllegalStateException();
        }
        // 业务逻辑...
    }
}

// 应用服务：只负责协调
public class FlowEngineApplicationService {
    public void moveToNextNode(FlowInstanceId id, NodeInstanceId nodeId) {
        FlowInstance instance = repository.findById(id);
        instance.moveToNextNode(nodeId);
        repository.save(instance);
    }
}
```

---

## 🎯 重构步骤

### 阶段一：基础重构（1-2周）
1. ✅ 创建值对象（FlowStatus, NodeStatus, Approver 等）
2. ✅ 重构 FlowInstance 为聚合根
3. ✅ 将业务逻辑移到 FlowInstance
4. ✅ 重构 FlowNodeInstance 实体

### 阶段二：领域服务（1周）
1. ✅ 创建 NodeRoutingService
2. ✅ 创建 ConditionEvaluationService
3. ✅ 创建 ApproverAssignmentService

### 阶段三：应用服务重构（1周）
1. ✅ 重构 FlowEngineService 为应用服务
2. ✅ 重构 FlowApprovalService 为应用服务
3. ✅ 简化应用服务逻辑

### 阶段四：领域事件（1周）
1. ✅ 定义领域事件
2. ✅ 实现事件发布机制
3. ✅ 实现事件处理器

### 阶段五：测试和优化（1周）
1. ✅ 单元测试
2. ✅ 集成测试
3. ✅ 性能优化

---

## ⚠️ 注意事项

### 1. 不要过度设计
- 值对象不要创建过多，只在有意义的地方使用
- 领域服务不要创建过多，只在跨聚合时使用

### 2. 保持向后兼容
- 重构时保持 API 兼容
- 逐步迁移，不要一次性重构所有代码

### 3. 性能考虑
- 聚合根不要太大，避免加载过多数据
- 使用懒加载或按需加载

### 4. 事务边界
- 一个事务只修改一个聚合根
- 跨聚合的操作使用领域事件

---

## 📚 参考资源

1. 《领域驱动设计》- Eric Evans
2. 《实现领域驱动设计》- Vaughn Vernon
3. DDD 社区最佳实践



