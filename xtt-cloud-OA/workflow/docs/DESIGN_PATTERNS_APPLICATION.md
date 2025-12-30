# Workflow 项目设计模式应用指南

## 📋 概述

本文档分析 workflow 项目中可以应用设计模式的地方，提供具体的实现建议和代码示例。

## 🎯 设计模式应用场景

### 1. 策略模式（Strategy Pattern）⭐ 高优先级

#### 应用场景 1：审批人分配策略

**当前问题**：`ApproverAssignmentServiceImpl` 中使用 `switch-case` 处理不同的审批人类型

**当前代码**：
```java
switch (approverType) {
    case FlowNode.APPROVER_TYPE_USER:
        approvers = assignByUserIds(approverValue);
        break;
    case FlowNode.APPROVER_TYPE_ROLE:
        approvers = assignByRoleIds(approverValue);
        break;
    case FlowNode.APPROVER_TYPE_DEPT_LEADER:
        approvers = assignByDeptIds(approverValue);
        break;
    case FlowNode.APPROVER_TYPE_INITIATOR:
        approvers = assignByInitiator(flowInstanceId, processVariables);
        break;
}
```

**改进方案**：使用策略模式

```java
// 1. 定义策略接口
public interface ApproverAssignmentStrategy {
    List<Approver> assign(String approverValue, Long flowInstanceId, 
                         Map<String, Object> processVariables);
    boolean supports(Integer approverType);
}

// 2. 实现具体策略
@Component
public class UserApproverStrategy implements ApproverAssignmentStrategy {
    private final ApproverProvider approverProvider;
    
    @Override
    public List<Approver> assign(String approverValue, Long flowInstanceId, 
                                Map<String, Object> processVariables) {
        List<Long> userIds = parseIdList(approverValue);
        return approverProvider.convertToApprovers(userIds);
    }
    
    @Override
    public boolean supports(Integer approverType) {
        return FlowNode.APPROVER_TYPE_USER.equals(approverType);
    }
}

@Component
public class RoleApproverStrategy implements ApproverAssignmentStrategy {
    // ... 实现
}

// 3. 策略上下文
@Service
public class ApproverAssignmentServiceImpl implements ApproverAssignmentService {
    private final List<ApproverAssignmentStrategy> strategies;
    
    @Override
    public List<Approver> assignApprovers(...) {
        ApproverAssignmentStrategy strategy = strategies.stream()
            .filter(s -> s.supports(approverType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("不支持的审批人类型: " + approverType));
        
        return strategy.assign(approverValue, flowInstanceId, processVariables);
    }
}
```

**优势**：
- ✅ 消除 switch-case，符合开闭原则
- ✅ 易于扩展新的审批人类型
- ✅ 每个策略独立测试

---

#### 应用场景 2：网关路由策略

**当前问题**：`NodeRoutingServiceImpl` 中处理不同网关类型的逻辑混杂在一起

**改进方案**：使用策略模式处理不同网关类型

```java
// 1. 网关路由策略接口
public interface GatewayRoutingStrategy {
    List<Long> getNextNodes(Long gatewayNodeId, Long flowInstanceId, 
                           Map<String, Object> processVariables);
    boolean canConverge(Long joinNodeId, Long flowInstanceId);
    boolean supports(GatewayType gatewayType);
}

// 2. 并行网关策略
@Component
public class ParallelGatewayStrategy implements GatewayRoutingStrategy {
    @Override
    public List<Long> getNextNodes(...) {
        // 并行网关：获取所有分支节点
    }
    
    @Override
    public boolean canConverge(...) {
        // 根据网关模式（会签/或签）判断是否可以汇聚
    }
    
    @Override
    public boolean supports(GatewayType gatewayType) {
        return gatewayType == GatewayType.PARALLEL_SPLIT 
            || gatewayType == GatewayType.PARALLEL_JOIN;
    }
}

// 3. 条件网关策略
@Component
public class ConditionGatewayStrategy implements GatewayRoutingStrategy {
    private final ConditionEvaluationService conditionEvaluationService;
    
    @Override
    public List<Long> getNextNodes(...) {
        // 条件网关：评估条件表达式，返回满足条件的分支
    }
    
    @Override
    public boolean canConverge(...) {
        // 条件网关：至少有一个分支完成即可汇聚
    }
    
    @Override
    public boolean supports(GatewayType gatewayType) {
        return gatewayType == GatewayType.CONDITION_SPLIT 
            || gatewayType == GatewayType.CONDITION_JOIN;
    }
}
```

---

### 2. 状态模式（State Pattern）⭐ 高优先级

#### 应用场景：流程状态转换

**当前问题**：`FlowStatus` 是值对象，状态转换逻辑分散在 `FlowInstance` 中

**改进方案**：使用状态模式封装状态转换逻辑

```java
// 1. 状态接口
public interface FlowState {
    void start(FlowInstance context);
    void complete(FlowInstance context);
    void terminate(FlowInstance context);
    void suspend(FlowInstance context);
    void resume(FlowInstance context);
    void cancel(FlowInstance context);
    boolean canProceed();
    FlowStatus getStatus();
}

// 2. 具体状态实现
public class ProcessingState implements FlowState {
    @Override
    public void complete(FlowInstance context) {
        context.transitionTo(new CompletedState());
        context.addDomainEvent(new FlowCompletedEvent(...));
    }
    
    @Override
    public void suspend(FlowInstance context) {
        context.transitionTo(new SuspendedState());
    }
    
    @Override
    public boolean canProceed() {
        return true;
    }
    
    @Override
    public FlowStatus getStatus() {
        return FlowStatus.PROCESSING;
    }
}

public class CompletedState implements FlowState {
    @Override
    public void complete(FlowInstance context) {
        throw new IllegalStateException("已完成的状态不能再次完成");
    }
    
    @Override
    public boolean canProceed() {
        return false;
    }
}

// 3. 在 FlowInstance 中使用
public class FlowInstance {
    private FlowState currentState;
    
    public void complete() {
        currentState.complete(this);
    }
    
    public void transitionTo(FlowState newState) {
        this.currentState = newState;
    }
}
```

**优势**：
- ✅ 状态转换逻辑集中管理
- ✅ 防止非法状态转换
- ✅ 易于添加新状态

---

### 3. 责任链模式（Chain of Responsibility）⭐ 中优先级

#### 应用场景 1：审批权限验证链

**当前问题**：权限验证逻辑集中在一个方法中

**改进方案**：使用责任链模式

```java
// 1. 处理器接口
public interface ApprovalPermissionHandler {
    boolean handle(Long userId, Long nodeId, Long flowInstanceId, 
                   ApprovalPermissionContext context);
    void setNext(ApprovalPermissionHandler next);
}

// 2. 具体处理器
@Component
public class ApproverValidationHandler implements ApprovalPermissionHandler {
    private ApprovalPermissionHandler next;
    
    @Override
    public boolean handle(...) {
        // 验证用户是否是审批人
        if (!isApprover(userId, nodeId, flowInstanceId)) {
            return false;
        }
        return next != null ? next.handle(...) : true;
    }
}

@Component
public class NodeStatusValidationHandler implements ApprovalPermissionHandler {
    // 验证节点状态是否允许审批
}

@Component
public class FlowStatusValidationHandler implements ApprovalPermissionHandler {
    // 验证流程状态是否允许审批
}

// 3. 构建责任链
@Service
public class ApprovalPermissionChain {
    private final ApprovalPermissionHandler chain;
    
    public boolean validate(Long userId, Long nodeId, Long flowInstanceId) {
        ApprovalPermissionContext context = new ApprovalPermissionContext();
        return chain.handle(userId, nodeId, flowInstanceId, context);
    }
}
```

---

#### 应用场景 2：节点路由处理链

**当前问题**：节点路由逻辑复杂，包含多个步骤

**改进方案**：使用责任链模式处理节点路由的各个步骤

```java
// 1. 路由处理器接口
public interface NodeRoutingHandler {
    void handle(NodeRoutingContext context);
    void setNext(NodeRoutingHandler next);
}

// 2. 具体处理器
@Component
public class GatewayCheckHandler implements NodeRoutingHandler {
    // 检查是否为网关节点
}

@Component
public class ConvergenceCheckHandler implements NodeRoutingHandler {
    // 检查汇聚条件
}

@Component
public class SkipConditionHandler implements NodeRoutingHandler {
    // 检查跳过条件
}

@Component
public class NodeInstanceCreationHandler implements NodeRoutingHandler {
    // 创建节点实例
}
```

---

### 4. 模板方法模式（Template Method Pattern）⭐ 中优先级

#### 应用场景：流程操作通用流程

**当前问题**：审批、拒绝、回退等操作有相似的流程

**改进方案**：使用模板方法模式

```java
// 1. 抽象模板类
public abstract class FlowOperationTemplate {
    
    // 模板方法
    public final void execute(FlowOperationContext context) {
        // 1. 验证流程状态
        validateFlowStatus(context);
        
        // 2. 验证权限
        validatePermission(context);
        
        // 3. 执行具体操作（由子类实现）
        doExecute(context);
        
        // 4. 更新节点状态
        updateNodeStatus(context);
        
        // 5. 流转到下一个节点
        routeToNextNode(context);
        
        // 6. 发布领域事件
        publishEvents(context);
        
        // 7. 更新缓存
        updateCache(context);
    }
    
    // 钩子方法
    protected abstract void doExecute(FlowOperationContext context);
    
    protected void validateFlowStatus(FlowOperationContext context) {
        // 通用验证逻辑
    }
    
    protected void validatePermission(FlowOperationContext context) {
        // 通用权限验证
    }
}

// 2. 具体实现
@Component
public class ApproveOperation extends FlowOperationTemplate {
    @Override
    protected void doExecute(FlowOperationContext context) {
        // 审批的具体逻辑
        context.getNodeInstance().complete(context.getComments());
    }
}

@Component
public class RejectOperation extends FlowOperationTemplate {
    @Override
    protected void doExecute(FlowOperationContext context) {
        // 拒绝的具体逻辑
        context.getNodeInstance().reject(context.getComments());
    }
}
```

---

### 5. 建造者模式（Builder Pattern）⭐ 低优先级

#### 应用场景：复杂对象构建

**当前问题**：`FlowInstance` 和 `FlowDefinition` 的创建参数较多

**改进方案**：使用建造者模式（可选，当前工厂方法已足够）

```java
// FlowInstanceBuilder.java
public class FlowInstanceBuilder {
    private Long documentId;
    private Long flowDefId;
    private FlowType flowType;
    private FlowMode flowMode;
    private ProcessVariables processVariables;
    
    public FlowInstanceBuilder documentId(Long documentId) {
        this.documentId = documentId;
        return this;
    }
    
    public FlowInstanceBuilder flowDefId(Long flowDefId) {
        this.flowDefId = flowDefId;
        return this;
    }
    
    public FlowInstance build() {
        return FlowInstance.create(documentId, flowDefId, flowType, flowMode, processVariables);
    }
}
```

---

### 6. 装饰器模式（Decorator Pattern）⭐ 中优先级

#### 应用场景：缓存装饰器

**当前问题**：缓存逻辑与业务逻辑耦合

**改进方案**：使用装饰器模式

```java
// 1. 组件接口
public interface FlowInstanceRepository {
    Optional<FlowInstance> findById(Long id);
    FlowInstance save(FlowInstance instance);
}

// 2. 具体组件
public class FlowInstanceRepositoryImpl implements FlowInstanceRepository {
    // 直接访问数据库
}

// 3. 装饰器
public class CachedFlowInstanceRepository implements FlowInstanceRepository {
    private final FlowInstanceRepository delegate;
    private final FlowInstanceCacheService cacheService;
    
    @Override
    public Optional<FlowInstance> findById(Long id) {
        // 先查缓存
        FlowInstance cached = cacheService.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        // 缓存未命中，查询数据库
        Optional<FlowInstance> instance = delegate.findById(id);
        instance.ifPresent(cacheService::put);
        return instance;
    }
}
```

---

### 7. 观察者模式（Observer Pattern）✅ 已应用

**当前实现**：领域事件机制就是观察者模式的应用

```java
// 主题：FlowInstance（发布事件）
public class FlowInstance {
    private List<Object> domainEvents;
    
    public void addDomainEvent(Object event) {
        this.domainEvents.add(event);
    }
}

// 观察者：EventHandler（监听事件）
@Component
public class TaskEventHandler {
    @EventListener
    @Async
    public void handleNodeInstanceCreated(NodeInstanceCreatedEvent event) {
        // 处理事件
    }
}
```

**优势**：已实现，符合 DDD 设计原则

---

### 8. 适配器模式（Adapter Pattern）✅ 已应用

**当前实现**：`PlatformUserServiceAdapter` 就是适配器模式

```java
// 适配器：将外部服务适配为领域接口
@Component
public class PlatformUserServiceAdapter implements ApproverProvider {
    private final PlatformFeignClient platformFeignClient;
    
    // 适配外部服务调用
}
```

**优势**：已实现，隔离外部依赖

---

### 9. 命令模式（Command Pattern）✅ 已应用

**当前实现**：Command 对象就是命令模式

```java
// 命令对象
public class ApproveCommand {
    private Long flowInstanceId;
    private Long nodeInstanceId;
    private Long approverId;
    private String comments;
}

// 命令执行者
@Service
public class FlowApplicationService {
    public void approve(ApproveCommand command) {
        // 执行命令
    }
}
```

**优势**：已实现，支持撤销、重做等扩展

---

### 10. 工厂模式（Factory Pattern）✅ 已应用

**当前实现**：`FlowInstanceFactory`、`FlowDefinitionFactory` 就是工厂模式

**优势**：已实现，封装复杂对象创建

---

## 📊 优先级总结

| 设计模式 | 优先级 | 应用场景 | 当前状态 |
|---------|--------|---------|---------|
| **策略模式** | ⭐⭐⭐⭐⭐ | 审批人分配、网关路由 | ❌ 需要改进 |
| **状态模式** | ⭐⭐⭐⭐⭐ | 流程状态转换 | ❌ 需要改进 |
| **责任链模式** | ⭐⭐⭐⭐ | 权限验证、节点路由 | ❌ 需要改进 |
| **模板方法模式** | ⭐⭐⭐⭐ | 流程操作通用流程 | ❌ 需要改进 |
| **装饰器模式** | ⭐⭐⭐ | 缓存装饰 | ❌ 需要改进 |
| **观察者模式** | ✅ | 领域事件 | ✅ 已实现 |
| **适配器模式** | ✅ | 外部服务适配 | ✅ 已实现 |
| **命令模式** | ✅ | Command 对象 | ✅ 已实现 |
| **工厂模式** | ✅ | 对象创建 | ✅ 已实现 |
| **建造者模式** | ⭐⭐ | 复杂对象构建 | ⚠️ 可选 |

---

## 🎯 实施建议

### 第一阶段：高优先级（立即实施）

1. **策略模式 - 审批人分配**
   - 消除 `switch-case`
   - 提高可扩展性

2. **状态模式 - 流程状态转换**
   - 集中管理状态转换逻辑
   - 防止非法状态转换

### 第二阶段：中优先级（逐步实施）

3. **责任链模式 - 权限验证**
   - 解耦验证逻辑
   - 易于添加新的验证规则

4. **模板方法模式 - 流程操作**
   - 提取公共流程
   - 减少代码重复

5. **装饰器模式 - 缓存**
   - 解耦缓存逻辑
   - 支持多层装饰

### 第三阶段：低优先级（按需实施）

6. **建造者模式 - 复杂对象构建**
   - 可选，当前工厂方法已足够

---

## 📝 已实现的代码示例

### 1. 策略模式 - 审批人分配

**文件位置**：
- `domain/flow/service/strategy/ApproverAssignmentStrategy.java` - 策略接口
- `domain/flow/service/strategy/impl/UserApproverStrategy.java` - 用户策略
- `domain/flow/service/strategy/impl/RoleApproverStrategy.java` - 角色策略
- `domain/flow/service/strategy/impl/DeptLeaderApproverStrategy.java` - 部门负责人策略
- `domain/flow/service/strategy/impl/InitiatorApproverStrategy.java` - 发起人指定策略
- `domain/flow/service/impl/ApproverAssignmentServiceImplV2.java` - 使用策略的服务实现

**使用方式**：
```java
// Spring 会自动注入所有策略实现
@Service
public class ApproverAssignmentServiceImplV2 {
    private final List<ApproverAssignmentStrategy> strategies;
    
    public List<Approver> assignApprovers(...) {
        // 根据审批人类型选择策略
        ApproverAssignmentStrategy strategy = strategies.stream()
            .filter(s -> s.supports(approverType))
            .findFirst()
            .orElseThrow(...);
        
        return strategy.assign(approverValue, flowInstanceId, processVariables);
    }
}
```

### 2. 策略模式 - 网关路由

**文件位置**：
- `domain/flow/service/strategy/gateway/GatewayRoutingStrategy.java` - 策略接口
- `domain/flow/service/strategy/gateway/impl/ParallelGatewayStrategy.java` - 并行网关策略
- `domain/flow/service/strategy/gateway/impl/ConditionGatewayStrategy.java` - 条件网关策略

### 3. 状态模式 - 流程状态转换

**文件位置**：
- `domain/flow/service/state/FlowState.java` - 状态接口
- `domain/flow/service/state/impl/ProcessingState.java` - 进行中状态

---

## 🎯 其他可应用的设计模式

### 11. 访问者模式（Visitor Pattern）

**应用场景**：流程定义验证、节点遍历

```java
// 访问者接口
public interface FlowNodeVisitor {
    void visit(FlowNode node);
    void visit(Gateway gateway);
}

// 具体访问者：流程定义验证器
public class FlowDefinitionValidator implements FlowNodeVisitor {
    // 验证节点连接、网关配对等
}
```

### 12. 中介者模式（Mediator Pattern）

**应用场景**：流程引擎协调多个服务

```java
// 流程引擎作为中介者
public class FlowEngineMediator {
    private NodeRoutingService routingService;
    private ApproverAssignmentService approverService;
    private TaskService taskService;
    
    // 协调各个服务完成流程操作
}
```

### 13. 备忘录模式（Memento Pattern）

**应用场景**：流程回退、快照保存

```java
// 流程快照
public class FlowInstanceMemento {
    private FlowStatus status;
    private Long currentNodeId;
    private List<FlowNodeInstance> nodeInstances;
    // 保存流程状态快照，用于回退
}
```

### 14. 代理模式（Proxy Pattern）

**应用场景**：缓存代理、权限代理

```java
// 缓存代理
public class CachedFlowInstanceRepository implements FlowInstanceRepository {
    private FlowInstanceRepository target;
    private CacheService cache;
    
    @Override
    public Optional<FlowInstance> findById(Long id) {
        // 先查缓存，再查数据库
    }
}
```

---

## 📊 设计模式应用优先级矩阵

| 设计模式 | 优先级 | 复杂度 | 收益 | 实施难度 |
|---------|--------|--------|------|----------|
| **策略模式（审批人分配）** | ⭐⭐⭐⭐⭐ | 低 | 高 | 低 |
| **策略模式（网关路由）** | ⭐⭐⭐⭐ | 中 | 高 | 中 |
| **状态模式** | ⭐⭐⭐⭐⭐ | 中 | 高 | 中 |
| **责任链模式** | ⭐⭐⭐⭐ | 中 | 中 | 中 |
| **模板方法模式** | ⭐⭐⭐⭐ | 低 | 中 | 低 |
| **装饰器模式** | ⭐⭐⭐ | 低 | 中 | 低 |
| **访问者模式** | ⭐⭐ | 高 | 低 | 高 |
| **中介者模式** | ⭐⭐ | 中 | 低 | 中 |
| **备忘录模式** | ⭐⭐ | 中 | 低 | 中 |
| **代理模式** | ⭐⭐⭐ | 低 | 中 | 低 |

---

## 🔗 相关文档

- [DDD 重构指南](./DDD_REFACTORING_GUIDE.md)
- [工厂模式指南](./FACTORY_PATTERN_GUIDE.md)
- [聚合根业务方法指南](./AGGREGATE_BUSINESS_METHODS_GUIDE.md)
- [网关设计](./GATEWAY_ENTITY_DESIGN.md)

