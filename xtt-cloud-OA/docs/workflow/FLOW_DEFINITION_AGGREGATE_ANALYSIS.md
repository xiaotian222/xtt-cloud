# FlowDefinition 聚合根设计分析

## 📋 问题

**FlowDefinition（流程定义）和 FlowNode（流程节点）是否应该成为一个聚合根？**

## 🔍 当前设计

### 当前结构

```
FlowDefinition (实体)
  └── 独立的 Repository
  
FlowNode (实体)
  └── 独立的 Repository
  └── 包含 flowDefId 字段（外键关系）
```

### 当前问题

1. **缺乏业务逻辑封装**
   - FlowDefinition 和 FlowNode 都是简单的 PO 包装器
   - 没有封装业务规则（如：节点顺序验证、节点完整性检查）

2. **一致性边界不明确**
   - FlowNode 属于 FlowDefinition，但没有明确的聚合边界
   - 可以单独操作 FlowNode，可能导致数据不一致

3. **业务规则分散**
   - 节点验证逻辑分散在应用层
   - 没有在领域层统一管理

---

## ✅ 支持成为聚合根的理由

### 1. **强一致性要求**

**业务规则：**
- 一个 FlowNode 必须属于一个 FlowDefinition
- 不能存在"孤儿节点"（没有流程定义的节点）
- 删除 FlowDefinition 时，应该级联删除所有 FlowNode

**当前问题：**
```java
// ❌ 当前设计：可以单独删除节点，可能导致数据不一致
flowNodeRepository.delete(nodeId);  // 节点可能变成"孤儿"

// ❌ 当前设计：可以单独创建节点，没有验证
FlowNode node = new FlowNode(...);
flowNodeRepository.save(node);  // 没有验证 flowDefId 是否存在
```

**聚合根设计：**
```java
// ✅ 聚合根设计：通过聚合根管理，保证一致性
FlowDefinition flowDef = flowDefinitionRepository.findById(defId);
flowDef.addNode(node);  // 聚合根验证并添加节点
flowDefinitionRepository.save(flowDef);  // 一起保存
```

### 2. **业务完整性**

**业务规则：**
- 流程定义必须至少有一个节点
- 节点顺序必须连续（不能有间隙）
- 节点之间的连接关系必须有效（nextNodeId 必须存在）

**聚合根可以封装这些规则：**
```java
public class FlowDefinition {
    private List<FlowNode> nodes;
    
    /**
     * 添加节点（封装业务规则）
     */
    public void addNode(FlowNode node) {
        // 1. 验证节点是否属于当前流程定义
        if (!node.getFlowDefId().equals(this.id)) {
            throw new IllegalArgumentException("节点不属于当前流程定义");
        }
        
        // 2. 验证节点顺序
        validateNodeOrder(node);
        
        // 3. 验证节点连接关系
        validateNodeConnections(node);
        
        // 4. 添加到集合
        this.nodes.add(node);
    }
    
    /**
     * 删除节点（封装业务规则）
     */
    public void removeNode(Long nodeId) {
        FlowNode node = findNodeById(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("节点不存在");
        }
        
        // 1. 验证是否可以删除（不能是最后一个节点）
        if (nodes.size() == 1) {
            throw new IllegalStateException("不能删除最后一个节点");
        }
        
        // 2. 验证是否有其他节点依赖此节点
        if (hasDependentNodes(node)) {
            throw new IllegalStateException("有其他节点依赖此节点，不能删除");
        }
        
        // 3. 删除节点
        nodes.remove(node);
    }
}
```

### 3. **事务边界清晰**

**聚合根设计：**
- 所有对 FlowDefinition 和 FlowNode 的修改都在同一个事务中
- 保证数据一致性

**当前设计：**
- 需要手动管理事务，容易出错

### 4. **业务操作的自然表达**

**业务场景：**
- "创建一个流程定义，包含多个节点"
- "修改流程定义，调整节点顺序"
- "删除流程定义及其所有节点"

**聚合根设计更自然：**
```java
// ✅ 聚合根设计：业务操作更自然
FlowDefinition flowDef = FlowDefinition.create(name, code, docTypeId);
flowDef.addNode(node1);
flowDef.addNode(node2);
flowDef.addNode(node3);
flowDefinitionRepository.save(flowDef);

// ❌ 当前设计：需要多次操作，容易出错
FlowDefinition flowDef = new FlowDefinition(...);
flowDefinitionRepository.save(flowDef);
flowNodeRepository.save(node1);
flowNodeRepository.save(node2);
flowNodeRepository.save(node3);
// 如果中间失败，数据可能不一致
```

---

## ❌ 不支持成为聚合根的理由

### 1. **性能问题**

**问题：**
- 一个流程定义可能包含几十个甚至上百个节点
- 如果每次加载 FlowDefinition 都要加载所有节点，性能会很差

**场景：**
```java
// ❌ 如果 FlowDefinition 是聚合根，每次都要加载所有节点
FlowDefinition flowDef = flowDefinitionRepository.findById(defId);
// 即使只需要查询流程定义的基本信息，也要加载所有节点

// ✅ 当前设计：可以按需加载
FlowDefinition flowDef = flowDefinitionRepository.findById(defId);  // 只加载定义
List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(defId);   // 按需加载节点
```

### 2. **查询场景多样**

**常见查询场景：**
- 查询流程定义列表（不需要节点）
- 查询某个节点的详细信息（不需要整个流程定义）
- 查询流程定义及其第一个节点（只需要部分节点）

**聚合根设计的问题：**
- 如果聚合根包含所有节点，无法支持这些灵活的查询场景

### 3. **并发修改问题**

**场景：**
- 用户 A 修改流程定义的名称
- 用户 B 同时添加一个新节点

**聚合根设计的问题：**
- 如果 FlowDefinition 是聚合根，这两个操作会冲突
- 但实际上，修改名称和添加节点不应该冲突

**当前设计：**
- 可以分别操作，并发性能更好


## 🎯 推荐方案：**有界上下文分离 + 懒加载**

### 方案一：FlowDefinition 作为聚合根（推荐用于写操作）

**适用场景：**
- 创建、修改、删除流程定义
- 需要保证数据一致性

**设计：**
```java
/**
 * 流程定义聚合根
 * 
 * 管理流程定义及其节点的创建、修改、删除
 */
public class FlowDefinition {
    private FlowDefinitionId id;
    private String name;
    private String code;
    private Long docTypeId;
    private FlowDefinitionStatus status;
    
    // 节点集合（懒加载）
    private List<FlowNode> nodes;  // 可以是懒加载的集合
    
    /**
     * 添加节点
     */
    public void addNode(FlowNode node) {
        // 业务规则验证
        validateNode(node);
        this.nodes.add(node);
    }
    
    /**
     * 删除节点
     */
    public void removeNode(FlowNodeId nodeId) {
        // 业务规则验证
        FlowNode node = findNodeById(nodeId);
        validateCanRemove(node);
        this.nodes.remove(node);
    }
    
    /**
     * 获取节点（懒加载）
     */
    public List<FlowNode> getNodes() {
        if (nodes == null) {
            // 懒加载：从 Repository 加载
            nodes = flowNodeRepository.findByFlowDefId(this.id);
        }
        return nodes;
    }
}
```

**仓储设计：**
```java
public interface FlowDefinitionRepository {
    /**
     * 保存流程定义（包含节点）
     */
    void save(FlowDefinition flowDefinition);
    
    /**
     * 查找流程定义（不加载节点）
     */
    Optional<FlowDefinition> findById(FlowDefinitionId id);
    
    /**
     * 查找流程定义（包含节点）
     */
    Optional<FlowDefinition> findByIdWithNodes(FlowDefinitionId id);
}
```

### 方案二：分离聚合（推荐用于读操作）

**设计：**
```java
/**
 * 流程定义聚合根（轻量级）
 * 只管理流程定义本身
 */
public class FlowDefinition {
    private FlowDefinitionId id;
    private String name;
    private String code;
    // ... 其他属性
    
    // 不包含节点集合
}

/**
 * 流程节点实体
 * 通过 flowDefId 关联到 FlowDefinition
 * 但不是聚合的一部分
 */
public class FlowNode {
    private FlowNodeId id;
    private FlowDefinitionId flowDefId;  // 关联到流程定义
    // ... 其他属性
}
```

**使用场景：**
- **写操作**：通过 FlowDefinition 聚合根管理节点
- **读操作**：直接查询 FlowNode，不需要加载整个聚合

---

## 🏆 最终推荐：**混合方案**

### 推荐设计

**采用"聚合根 + 懒加载 + 分离查询"的混合方案：**

```java
/**
 * 流程定义聚合根
 * 
 * 特点：
 * 1. 管理流程定义及其节点的创建、修改、删除
 * 2. 节点集合懒加载（按需加载）
 * 3. 封装业务规则
 */
public class FlowDefinition {
    private FlowDefinitionId id;
    private String name;
    private String code;
    private Long docTypeId;
    private FlowDefinitionStatus status;
    
    // 节点集合（懒加载，不是直接字段）
    // 通过 Repository 按需加载
    
    /**
     * 添加节点（业务方法）
     */
    public void addNode(FlowNode node) {
        // 1. 验证节点属于当前流程定义
        if (!node.getFlowDefId().equals(this.id)) {
            throw new IllegalArgumentException("节点不属于当前流程定义");
        }
        
        // 2. 验证业务规则
        validateNode(node);
        
        // 3. 标记为需要保存（通过领域事件或变更追踪）
        this.addDomainEvent(new NodeAddedEvent(this.id, node.getId()));
    }
    
    /**
     * 删除节点（业务方法）
     */
    public void removeNode(FlowNodeId nodeId) {
        // 1. 验证是否可以删除
        validateCanRemoveNode(nodeId);
        
        // 2. 标记为需要删除
        this.addDomainEvent(new NodeRemovedEvent(this.id, nodeId));
    }
    
    /**
     * 获取节点列表（委托给 Repository）
     * 注意：这不是聚合的一部分，只是查询方法
     */
    public List<FlowNode> getNodes() {
        // 通过 Repository 查询，不存储在聚合中
        return flowNodeRepository.findByFlowDefId(this.id);
    }
}
```

**仓储设计：**
```java
public interface FlowDefinitionRepository {
    /**
     * 保存流程定义（写操作）
     * 同时处理节点的保存（通过领域事件）
     */
    void save(FlowDefinition flowDefinition);
    
    /**
     * 查找流程定义（读操作，不加载节点）
     */
    Optional<FlowDefinition> findById(FlowDefinitionId id);
}

public interface FlowNodeRepository {
    /**
     * 根据流程定义ID查找节点（读操作）
     */
    List<FlowNode> findByFlowDefId(FlowDefinitionId flowDefId);
    
    /**
     * 保存节点（由 FlowDefinition 聚合根调用）
     */
    void save(FlowNode node);
    
    /**
     * 删除节点（由 FlowDefinition 聚合根调用）
     */
    void delete(FlowNodeId nodeId);
}
```

**应用服务：**
```java
@Service
public class FlowDefinitionApplicationService {
    
    /**
     * 创建流程定义（写操作）
     */
    @Transactional
    public FlowDefinitionId createFlowDefinition(CreateFlowDefinitionCommand command) {
        // 1. 创建聚合根
        FlowDefinition flowDef = FlowDefinition.create(
            command.getName(),
            command.getCode(),
            command.getDocTypeId()
        );
        
        // 2. 添加节点（通过聚合根）
        for (CreateNodeCommand nodeCmd : command.getNodes()) {
            FlowNode node = FlowNode.create(
                flowDef.getId(),
                nodeCmd.getNodeName(),
                nodeCmd.getNodeType()
            );
            flowDef.addNode(node);  // 聚合根验证并添加
        }
        
        // 3. 保存聚合根（仓储实现会处理节点的保存）
        flowDefinitionRepository.save(flowDef);
        
        return flowDef.getId();
    }
    
    /**
     * 查询流程定义（读操作）
     */
    @Transactional(readOnly = true)
    public FlowDefinitionDTO getFlowDefinition(FlowDefinitionId id) {
        FlowDefinition flowDef = flowDefinitionRepository.findById(id)
            .orElseThrow(() -> new FlowDefinitionNotFoundException(id));
        
        // 按需加载节点
        List<FlowNode> nodes = flowNodeRepository.findByFlowDefId(id);
        
        return FlowDefinitionAssembler.toDTO(flowDef, nodes);
    }
}
```

---

## 📊 方案对比

| 特性 | 当前设计 | 聚合根设计（全加载） | 聚合根设计（懒加载） |
|------|---------|-------------------|-------------------|
| **数据一致性** | ⚠️ 需要手动保证 | ✅ 自动保证 | ✅ 自动保证 |
| **业务规则封装** | ❌ 分散在应用层 | ✅ 封装在聚合根 | ✅ 封装在聚合根 |
| **写操作性能** | ⚠️ 需要多次操作 | ⚠️ 可能加载大量数据 | ✅ 按需加载 |
| **读操作性能** | ✅ 灵活查询 | ❌ 必须加载全部 | ✅ 按需查询 |
| **并发性能** | ✅ 可以分别操作 | ⚠️ 可能冲突 | ✅ 可以分别操作 |
| **代码复杂度** | ⚠️ 需要手动管理 | ✅ 聚合根管理 | ⚠️ 需要懒加载逻辑 |

---

## 🎯 最终建议

### 推荐：**FlowDefinition 作为聚合根（懒加载方案）**

**理由：**
1. ✅ **保证数据一致性**：通过聚合根管理节点，避免"孤儿节点"
2. ✅ **封装业务规则**：节点验证、顺序检查等逻辑封装在聚合根中
3. ✅ **性能可接受**：通过懒加载，读操作不受影响
4. ✅ **写操作更安全**：创建、修改、删除流程定义时，保证数据一致性

**实现要点：**
1. FlowDefinition 作为聚合根，管理节点的创建、修改、删除
2. 节点集合不直接存储在聚合中，通过 Repository 按需加载
3. 写操作通过聚合根，读操作可以直接查询节点
4. 使用领域事件或变更追踪机制，在保存聚合根时同步保存节点

**不推荐：**
- ❌ 将节点直接存储在聚合中（性能问题）
- ❌ 完全分离聚合（一致性难以保证）

---

## 📝 实施步骤

### 第一步：重构 FlowDefinition 为聚合根

```java
public class FlowDefinition {
    private FlowDefinitionId id;
    private String name;
    private String code;
    // ... 其他属性
    
    // 节点变更追踪（不直接存储节点）
    private List<FlowNode> nodesToAdd = new ArrayList<>();
    private List<FlowNodeId> nodesToRemove = new ArrayList<>();
    
    /**
     * 添加节点
     */
    public void addNode(FlowNode node) {
        validateNode(node);
        nodesToAdd.add(node);
    }
    
    /**
     * 删除节点
     */
    public void removeNode(FlowNodeId nodeId) {
        validateCanRemove(nodeId);
        nodesToRemove.add(nodeId);
    }
    
    /**
     * 获取待保存的节点
     */
    public List<FlowNode> getNodesToAdd() {
        return new ArrayList<>(nodesToAdd);
    }
    
    /**
     * 获取待删除的节点ID
     */
    public List<FlowNodeId> getNodesToRemove() {
        return new ArrayList<>(nodesToRemove);
    }
}
```

### 第二步：更新仓储实现

```java
@Repository
public class FlowDefinitionRepositoryImpl implements FlowDefinitionRepository {
    
    @Override
    public void save(FlowDefinition flowDefinition) {
        // 1. 保存流程定义
        FlowDefinitionPO po = converter.toPO(flowDefinition);
        if (po.getId() == null) {
            flowDefinitionMapper.insert(po);
            flowDefinition.setId(FlowDefinitionId.of(po.getId()));
        } else {
            flowDefinitionMapper.updateById(po);
        }
        
        // 2. 处理节点变更
        for (FlowNode node : flowDefinition.getNodesToAdd()) {
            node.setFlowDefId(flowDefinition.getId().getValue());
            flowNodeRepository.save(node);
        }
        
        for (FlowNodeId nodeId : flowDefinition.getNodesToRemove()) {
            flowNodeRepository.delete(nodeId);
        }
        
        // 3. 清空变更追踪
        flowDefinition.clearChanges();
    }
}
```

### 第三步：更新应用服务

```java
@Service
public class FlowDefinitionApplicationService {
    
    @Transactional
    public FlowDefinitionId createFlowDefinition(CreateFlowDefinitionCommand command) {
        FlowDefinition flowDef = FlowDefinition.create(...);
        
        // 通过聚合根添加节点
        for (CreateNodeCommand nodeCmd : command.getNodes()) {
            FlowNode node = FlowNode.create(...);
            flowDef.addNode(node);  // 聚合根验证
        }
        
        // 保存聚合根（会自动保存节点）
        flowDefinitionRepository.save(flowDef);
        
        return flowDef.getId();
    }
}
```

---

## 🔗 相关文档

- [DDD_PROJECT_STRUCTURE.md](./DDD_PROJECT_STRUCTURE.md) - DDD 项目结构说明
- [DOMAIN_SERVICE_GUIDE.md](./DOMAIN_SERVICE_GUIDE.md) - Domain Service 指南

