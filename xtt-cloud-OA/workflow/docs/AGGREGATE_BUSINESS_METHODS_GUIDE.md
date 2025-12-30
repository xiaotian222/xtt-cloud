# 聚合根业务方法作用指南

## 📋 问题

聚合根中的这些业务方法有什么作用？

## 🎯 核心作用

聚合根中的业务方法是 **DDD 充血模型的核心**，它们的作用包括：

### 1. **封装业务规则和不变量（Business Invariants）** ⭐ 最重要

业务方法确保聚合内的数据始终符合业务规则，防止非法状态。

#### 示例：FlowInstance 的状态转换

```java
/**
 * 完成流程
 * 
 * 业务规则：
 * 1. 只有正在处理中的流程才能完成
 * 2. 完成时必须设置结束时间
 * 3. 完成时发布领域事件
 */
public void complete() {
    // ✅ 业务规则验证：只有可继续的状态才能完成
    if (!status.canProceed()) {
        throw new IllegalStateException("Flow instance cannot be completed. Current status: " + status);
    }
    
    // ✅ 状态转换：保证状态一致性
    this.status = FlowStatus.COMPLETED;
    this.endTime = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    
    // ✅ 发布领域事件：通知其他聚合
    addDomainEvent(new FlowCompletedEvent(...));
}
```

**作用**：
- ✅ 防止非法状态转换（如已完成的状态再次完成）
- ✅ 保证数据一致性（完成时必须设置结束时间）
- ✅ 封装业务规则（只有可继续的状态才能完成）

---

### 2. **控制状态转换（State Transitions）**

业务方法控制聚合根的状态转换，确保状态转换符合业务规则。

#### FlowInstance 的状态转换方法

| 方法 | 作用 | 业务规则 |
|------|------|----------|
| `start()` | 启动流程 | 只有 PROCESSING 状态才能启动 |
| `complete()` | 完成流程 | 只有可继续的状态才能完成 |
| `terminate()` | 终止流程 | 只有可继续的状态才能终止 |
| `suspend()` | 暂停流程 | 只有可继续的状态才能暂停 |
| `resume()` | 恢复流程 | 只有 SUSPENDED 状态才能恢复 |
| `cancel()` | 取消流程 | 只有可继续的状态才能取消 |

```java
/**
 * 暂停流程
 * 
 * 业务规则：
 * 1. 只有正在处理中的流程才能暂停
 * 2. 暂停后状态变为 SUSPENDED
 */
public void suspend() {
    // ✅ 验证当前状态是否允许暂停
    if (!status.canProceed()) {
        throw new IllegalStateException("Flow instance cannot be suspended. Current status: " + status);
    }
    
    // ✅ 执行状态转换
    this.status = FlowStatus.SUSPENDED;
    this.updatedAt = LocalDateTime.now();
}
```

**作用**：
- ✅ 确保状态转换的合法性
- ✅ 防止状态混乱（如已完成的状态再次启动）
- ✅ 保证状态转换的原子性

---

### 3. **管理聚合内的实体（Entity Management）**

业务方法管理聚合内的实体，确保实体之间的关系符合业务规则。

#### FlowInstance 管理 FlowNodeInstance

```java
/**
 * 添加节点实例
 * 
 * 业务规则：
 * 1. 节点实例不能为空
 * 2. 节点实例必须属于当前流程实例
 */
public void addNodeInstance(FlowNodeInstance nodeInstance) {
    if (nodeInstance == null) {
        throw new IllegalArgumentException("Node instance cannot be null");
    }
    
    // ✅ 业务规则：节点实例必须属于当前流程实例
    if (this.id != null && nodeInstance.getFlowInstanceId() != null) {
        if (!this.id.getValue().equals(nodeInstance.getFlowInstanceId())) {
            throw new IllegalArgumentException("Node instance does not belong to this flow instance");
        }
    }
    
    // ✅ 添加到聚合内
    this.nodeInstances.add(nodeInstance);
}
```

**作用**：
- ✅ 保证实体归属关系正确
- ✅ 防止跨聚合的非法操作
- ✅ 维护聚合的一致性边界

---

### 4. **发布领域事件（Domain Events）**

业务方法在发生重要业务事件时发布领域事件，通知其他聚合或应用层。

```java
/**
 * 完成流程
 */
public void complete() {
    // ... 状态转换 ...
    
    // ✅ 发布领域事件：通知其他聚合或应用层
    addDomainEvent(new FlowCompletedEvent(
            id != null ? id.getValue() : null,
            documentId,
            startTime,
            endTime));
}

/**
 * 创建流程实例时发布事件
 */
public static FlowInstance create(...) {
    FlowInstance instance = new FlowInstance();
    // ... 初始化 ...
    
    // ✅ 发布领域事件
    instance.addDomainEvent(new FlowStartedEvent(...));
    
    return instance;
}
```

**作用**：
- ✅ 解耦聚合之间的依赖
- ✅ 支持最终一致性
- ✅ 触发应用层的副作用（如发送通知、创建待办任务）

---

### 5. **保证数据一致性（Data Consistency）**

业务方法确保聚合内的数据始终保持一致。

#### FlowDefinition 管理节点变更

```java
/**
 * 添加节点（业务方法）
 * 
 * 封装业务规则：
 * 1. 验证节点属于当前流程定义
 * 2. 验证节点顺序
 * 3. 验证节点连接关系
 */
public void addNode(FlowNode node) {
    if (node == null) {
        throw new IllegalArgumentException("节点不能为空");
    }
    
    // ✅ 业务规则：节点必须属于当前流程定义
    if (this.id != null && node.getFlowDefId() != null) {
        if (!node.getFlowDefId().equals(this.id.getValue())) {
            throw new IllegalArgumentException("节点不属于当前流程定义");
        }
    }
    
    // ✅ 添加到变更追踪列表（延迟保存）
    this.nodesToAdd.add(node);
    this.updatedAt = LocalDateTime.now();
}
```

**作用**：
- ✅ 保证节点归属关系正确
- ✅ 维护变更追踪的一致性
- ✅ 支持事务边界内的批量操作

---

### 6. **封装复杂业务逻辑（Complex Business Logic）**

业务方法封装复杂的业务逻辑，避免在应用层重复实现。

#### FlowDefinition 的启用/停用逻辑

```java
/**
 * 启用流程定义
 * 
 * 业务规则：
 * 1. 已经启用的不能再次启用
 * 2. 启用时更新状态和时间戳
 */
public void enable() {
    // ✅ 业务规则验证
    if (this.status.isEnabled()) {
        throw new IllegalStateException("流程定义已经启用");
    }
    
    // ✅ 状态转换（通过值对象的方法）
    this.status = this.status.enable();
    this.updatedAt = LocalDateTime.now();
}
```

**作用**：
- ✅ 封装业务规则，避免在应用层重复实现
- ✅ 提高代码复用性
- ✅ 降低应用层的复杂度

---

## 📊 业务方法分类

### FlowInstance 的业务方法

| 类别 | 方法 | 作用 |
|------|------|------|
| **生命周期管理** | `start()` | 启动流程 |
| | `complete()` | 完成流程 |
| | `terminate()` | 终止流程 |
| | `suspend()` | 暂停流程 |
| | `resume()` | 恢复流程 |
| | `cancel()` | 取消流程 |
| **节点管理** | `moveToNode()` | 移动到下一个节点 |
| | `addNodeInstance()` | 添加节点实例 |
| **流程变量** | `setProcessVariable()` | 设置流程变量 |
| | `getProcessVariable()` | 获取流程变量 |
| **领域事件** | `addDomainEvent()` | 添加领域事件 |
| | `getAndClearDomainEvents()` | 获取并清空领域事件 |

### FlowDefinition 的业务方法

| 类别 | 方法 | 作用 |
|------|------|------|
| **生命周期管理** | `enable()` | 启用流程定义 |
| | `disable()` | 停用流程定义 |
| **节点管理** | `addNode()` | 添加节点 |
| | `removeNode()` | 删除节点 |
| | `updateNode()` | 更新节点 |
| **信息管理** | `updateBasicInfo()` | 更新基本信息 |
| **变更追踪** | `getNodesToAdd()` | 获取待添加的节点 |
| | `getNodesToRemove()` | 获取待删除的节点 |
| | `getNodesToUpdate()` | 获取待更新的节点 |
| | `clearChanges()` | 清空变更追踪 |
| | `hasChanges()` | 判断是否有变更 |

---

## 🔄 业务方法 vs 普通 Setter

### ❌ 错误做法：直接使用 Setter

```java
// ❌ 应用层直接操作状态
flowInstance.setStatus(FlowStatus.COMPLETED);
flowInstance.setEndTime(LocalDateTime.now());

// 问题：
// 1. 没有验证业务规则
// 2. 可能产生非法状态
// 3. 没有发布领域事件
// 4. 没有更新时间戳
```

### ✅ 正确做法：使用业务方法

```java
// ✅ 使用业务方法
flowInstance.complete();

// 优点：
// 1. 自动验证业务规则
// 2. 保证状态一致性
// 3. 自动发布领域事件
// 4. 自动更新时间戳
```

---

## 🎯 设计原则

### 1. **封装业务规则**

业务方法应该封装所有相关的业务规则，而不是让调用者自己验证。

```java
// ✅ 好的设计：业务方法内部验证
public void complete() {
    if (!status.canProceed()) {
        throw new IllegalStateException("...");
    }
    // ... 执行完成逻辑
}

// ❌ 不好的设计：调用者需要自己验证
public void complete() {
    // 没有验证，调用者需要自己检查状态
    this.status = FlowStatus.COMPLETED;
}
```

### 2. **保证不变量**

业务方法应该保证聚合内的不变量始终成立。

```java
// ✅ 保证不变量：完成时必须设置结束时间
public void complete() {
    this.status = FlowStatus.COMPLETED;
    this.endTime = LocalDateTime.now(); // ✅ 保证不变量
    this.updatedAt = LocalDateTime.now();
}
```

### 3. **发布领域事件**

业务方法应该在发生重要业务事件时发布领域事件。

```java
// ✅ 发布领域事件
public void complete() {
    // ... 状态转换 ...
    addDomainEvent(new FlowCompletedEvent(...)); // ✅ 发布事件
}
```

### 4. **控制访问**

业务方法应该控制对聚合内实体的访问，防止外部直接操作。

```java
// ✅ 通过业务方法控制访问
public void addNodeInstance(FlowNodeInstance nodeInstance) {
    // 验证业务规则
    // 添加到聚合内
    this.nodeInstances.add(nodeInstance);
}

// ❌ 不应该直接暴露集合
public List<FlowNodeInstance> getNodeInstances() {
    return new ArrayList<>(nodeInstances); // ✅ 返回副本，防止外部修改
}
```

---

## 📝 总结

聚合根中的业务方法的作用：

1. ✅ **封装业务规则和不变量** - 确保数据始终符合业务规则
2. ✅ **控制状态转换** - 确保状态转换的合法性
3. ✅ **管理聚合内的实体** - 保证实体关系正确
4. ✅ **发布领域事件** - 通知其他聚合或应用层
5. ✅ **保证数据一致性** - 维护聚合的一致性边界
6. ✅ **封装复杂业务逻辑** - 提高代码复用性

**核心思想**：业务方法让聚合根成为"充血模型"，而不是"贫血模型"。它们不仅仅是数据的容器，更是业务逻辑的载体。

