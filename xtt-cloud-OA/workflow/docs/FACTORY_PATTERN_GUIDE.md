# DDD 工厂模式指南

## 📋 问题

FlowInstance 和 FlowDefinition 这些充血对象的构建功能单独抽取出来的话，应该放在哪里？

## 🎯 DDD 中的工厂模式

在 DDD 中，对象创建有几种模式：

### 1. 聚合根内部静态工厂方法（Simple Factory）

**位置**：聚合根类内部

**适用场景**：
- ✅ 简单的创建逻辑
- ✅ 只需要聚合根自身的属性
- ✅ 不需要访问外部服务或仓储

**示例**：
```java
// FlowInstance.java
public static FlowInstance create(
        Long documentId,
        Long flowDefId,
        FlowType flowType,
        FlowMode flowMode,
        ProcessVariables initialVariables) {
    // 简单的创建逻辑
    FlowInstance instance = new FlowInstance();
    // ... 设置属性
    return instance;
}
```

**优点**：
- 简单直接
- 不需要额外的类
- 符合封装原则

**缺点**：
- 如果创建逻辑复杂，会让聚合根变得臃肿
- 无法访问外部服务

---

### 2. 领域层工厂（Domain Factory）⭐ **推荐用于复杂创建**

**位置**：`domain/flow/factory/`

**适用场景**：
- ✅ 复杂的聚合根创建逻辑
- ✅ 需要访问领域服务（如 ApproverAssignmentService）
- ✅ 需要访问仓储接口（如 FlowNodeRepository）
- ✅ 需要验证业务规则
- ✅ 创建逻辑可能被多个应用服务复用

**示例**：
```java
// domain/flow/factory/FlowInstanceFactory.java
@Component
public class FlowInstanceFactory {
    
    private final FlowNodeRepository flowNodeRepository;
    private final ApproverAssignmentService approverAssignmentService;
    
    /**
     * 创建并初始化流程实例
     * 
     * 包括：
     * 1. 创建聚合根
     * 2. 加载节点列表
     * 3. 创建第一个节点实例
     * 4. 分配审批人
     */
    public FlowInstance createAndInitialize(
            Long documentId,
            Long flowDefId,
            FlowType flowType,
            FlowMode flowMode,
            ProcessVariables initialVariables) {
        
        // 1. 创建聚合根
        FlowInstance instance = FlowInstance.create(
            documentId, flowDefId, flowType, flowMode, initialVariables);
        
        // 2. 加载节点列表
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(...);
        
        // 3. 创建第一个节点实例
        FlowNode firstNode = getFirstNode(nodes);
        createNodeInstances(instance, firstNode);
        
        return instance;
    }
}
```

**优点**：
- ✅ 职责清晰：专门负责聚合根的创建
- ✅ 可以访问领域服务和仓储
- ✅ 可以封装复杂的创建逻辑
- ✅ 可以被多个应用服务复用

**缺点**：
- ⚠️ 需要额外的类
- ⚠️ 需要依赖注入

---

### 3. 应用层工厂（Application Factory）

**位置**：`application/flow/factory/`

**适用场景**：
- ✅ 需要协调多个聚合根
- ✅ 需要访问多个仓储
- ✅ 需要处理事务边界
- ✅ 需要处理应用层的副作用（如发送通知）

**示例**：
```java
// application/flow/factory/FlowInstanceApplicationFactory.java
@Component
public class FlowInstanceApplicationFactory {
    
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeRepository flowNodeRepository;
    private final TaskApplicationService taskService;
    
    /**
     * 创建并组装流程实例（应用层）
     * 
     * 包括：
     * 1. 创建流程实例
     * 2. 保存到数据库
     * 3. 创建待办任务
     * 4. 发送通知
     */
    @Transactional
    public FlowInstance createAndAssemble(StartFlowCommand command) {
        // 1. 创建流程实例（可以调用领域工厂）
        FlowInstance instance = flowInstanceFactory.createAndInitialize(...);
        
        // 2. 保存
        instance = flowInstanceRepository.save(instance);
        
        // 3. 创建待办任务（应用层副作用）
        taskService.createTodoTask(...);
        
        return instance;
    }
}
```

**优点**：
- ✅ 可以处理应用层的副作用
- ✅ 可以协调多个聚合根
- ✅ 可以管理事务

**缺点**：
- ⚠️ 属于应用层，不应该包含核心业务逻辑

---

## 🎯 推荐方案

### 分层策略

```
┌─────────────────────────────────────────┐
│  Application Layer (应用层)              │
│  ┌───────────────────────────────────┐  │
│  │  FlowInstanceApplicationFactory    │  │ ← 协调多个聚合根，处理副作用
│  │  - createAndAssemble()             │  │
│  │  - 调用领域工厂                     │  │
│  │  - 创建待办任务、发送通知            │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                 ↓ 调用
┌─────────────────────────────────────────┐
│  Domain Layer (领域层)                   │
│  ┌───────────────────────────────────┐  │
│  │  FlowInstanceFactory               │  │ ← 复杂创建逻辑
│  │  - createAndInitialize()           │  │
│  │  - 访问领域服务和仓储                │  │
│  │  - 验证业务规则                      │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │  FlowInstance (聚合根)             │  │ ← 简单创建逻辑
│  │  - create()                        │  │
│  │  - reconstruct()                   │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### 具体建议

#### 1. **简单创建** → 聚合根内部静态方法

```java
// FlowInstance.java
public static FlowInstance create(...) {
    // 简单的属性设置
    // 不需要外部依赖
}
```

#### 2. **复杂创建** → 领域层工厂 ⭐

```java
// domain/flow/factory/FlowInstanceFactory.java
@Component
public class FlowInstanceFactory {
    // 复杂的创建逻辑
    // 可以访问领域服务和仓储
}
```

#### 3. **应用层组装** → 应用层工厂

```java
// application/flow/factory/FlowInstanceApplicationFactory.java
@Component
public class FlowInstanceApplicationFactory {
    // 协调多个聚合根
    // 处理应用层副作用
}
```

---

## 📝 实现建议

### 方案A：领域工厂 + 应用工厂（推荐）⭐

**领域工厂**：处理复杂的领域逻辑
**应用工厂**：处理应用层的协调和副作用

```java
// domain/flow/factory/FlowInstanceFactory.java
@Component
public class FlowInstanceFactory {
    
    private final FlowNodeRepository flowNodeRepository;
    private final ApproverAssignmentService approverAssignmentService;
    
    /**
     * 创建并初始化流程实例（领域逻辑）
     */
    public FlowInstance createAndInitialize(
            Long documentId,
            Long flowDefId,
            FlowType flowType,
            FlowMode flowMode,
            ProcessVariables initialVariables) {
        
        // 1. 创建聚合根
        FlowInstance instance = FlowInstance.create(...);
        
        // 2. 加载节点
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(...);
        
        // 3. 创建第一个节点实例
        FlowNode firstNode = getFirstNode(nodes);
        createNodeInstances(instance, firstNode);
        
        return instance;
    }
}

// application/flow/factory/FlowInstanceApplicationFactory.java
@Component
public class FlowInstanceApplicationFactory {
    
    private final FlowInstanceFactory flowInstanceFactory; // 领域工厂
    private final FlowInstanceRepository flowInstanceRepository;
    private final TaskApplicationService taskService;
    
    /**
     * 创建并组装流程实例（应用层）
     */
    @Transactional
    public FlowInstance createAndAssemble(StartFlowCommand command) {
        // 1. 调用领域工厂创建
        FlowInstance instance = flowInstanceFactory.createAndInitialize(...);
        
        // 2. 保存
        instance = flowInstanceRepository.save(instance);
        
        // 3. 创建待办任务（应用层副作用）
        taskService.createTodoTask(...);
        
        return instance;
    }
}
```

### 方案B：仅使用应用工厂（当前实现）

如果创建逻辑不太复杂，可以只在应用层使用工厂：

```java
// application/flow/factory/FlowInstanceApplicationFactory.java
@Component
public class FlowInstanceApplicationFactory {
    // 包含所有创建和组装逻辑
}
```

---

## ✅ 最终建议

### 对于 FlowInstance 和 FlowDefinition

1. **简单创建**：保留聚合根内部的 `create()` 方法
   - 用于基本的对象创建
   - 不需要外部依赖

2. **复杂创建**：创建领域层工厂
   - `domain/flow/factory/FlowInstanceFactory.java`
   - `domain/flow/factory/FlowDefinitionFactory.java`
   - 处理需要访问领域服务和仓储的复杂创建逻辑

3. **应用层组装**：保留或创建应用层工厂
   - `application/flow/factory/FlowInstanceApplicationFactory.java`
   - 处理应用层的协调和副作用

### 目录结构

```
domain/flow/
  ├── factory/                    ← 领域工厂（新增）
  │   ├── FlowInstanceFactory.java
  │   └── FlowDefinitionFactory.java
  ├── model/
  │   └── aggregate/
  │       ├── FlowInstance.java  ← 保留简单的 create()
  │       └── FlowDefinition.java ← 保留简单的 create()
  └── ...

application/flow/
  ├── factory/                    ← 应用工厂
  │   └── FlowInstanceApplicationFactory.java
  └── ...
```

---

## 📚 总结

| 创建复杂度 | 推荐位置 | 示例 |
|-----------|---------|------|
| **简单** | 聚合根内部静态方法 | `FlowInstance.create()` |
| **中等** | 领域层工厂 | `FlowInstanceFactory.createAndInitialize()` |
| **复杂（需要协调多个聚合根）** | 应用层工厂 | `FlowInstanceApplicationFactory.createAndAssemble()` |

**核心原则**：
- ✅ 领域逻辑放在领域层
- ✅ 应用协调放在应用层
- ✅ 简单逻辑不抽取，复杂逻辑抽取到工厂

