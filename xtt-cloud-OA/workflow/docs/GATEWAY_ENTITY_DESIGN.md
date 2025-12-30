# 网关实体设计分析

## 📋 问题

网关和节点是否要分成不同的实体类？是否可以做数据冗余？

## 🔍 两种设计方案对比

### 方案A：独立实体（Gateway 独立表）

```
┌─────────────────┐         ┌─────────────────┐
│   FlowNode      │         │    Gateway       │
│                 │         │                 │
│ - id            │◄──┐     │ - id            │
│ - gatewayId     │───┘     │ - gatewayType   │
│ - nodeName      │         │ - gatewayMode   │
│ - nodeType      │         │ - conditionExpr │
│ - ...           │         │ - splitNodeId   │
└─────────────────┘         │ - joinNodeId    │
                            └─────────────────┘
```

**优点**：
- ✅ 职责清晰：网关逻辑独立
- ✅ 符合单一职责原则
- ✅ 网关信息集中管理

**缺点**：
- ❌ 需要额外的表（Gateway）
- ❌ 查询节点时需要 JOIN Gateway 表
- ❌ 性能开销：每次查询节点都要关联查询
- ❌ 复杂度增加：需要维护两个实体

### 方案B：数据冗余（网关信息冗余在节点中）⭐ **推荐**

```
┌─────────────────────────────────────┐
│           FlowNode                  │
│                                     │
│ - id                                │
│ - gatewayType      (冗余)           │
│ - gatewayMode      (冗余)           │
│ - gatewayId        (关联Split/Join) │
│ - conditionExpression (冗余)        │
│ - nodeName                          │
│ - nodeType                          │
│ - ...                               │
└─────────────────────────────────────┘
```

**优点**：
- ✅ **查询性能好**：一次查询即可获取所有信息
- ✅ **简单直接**：不需要 JOIN 操作
- ✅ **减少表数量**：不需要独立的 Gateway 表
- ✅ **符合实际使用场景**：查询节点时通常需要网关信息

**缺点**：
- ⚠️ 数据冗余（但这是可以接受的）
- ⚠️ 需要保证 Split 和 Join 节点的 gatewayId 一致

## 🎯 推荐方案：数据冗余

### 理由

1. **网关信息相对固定**
   - 网关类型、模式、条件表达式在节点创建时确定
   - 不会频繁变更
   - 适合冗余存储

2. **查询场景**
   - 99% 的查询都是：根据节点ID查询节点信息（包括网关信息）
   - 很少需要单独查询网关信息
   - 冗余可以避免 JOIN 操作

3. **Split 和 Join 关联**
   - 通过 `gatewayId` 字段关联即可
   - Split 节点和 Join 节点存储相同的 `gatewayId`
   - 不需要额外的关联表

4. **性能考虑**
   - 节点查询是高频操作
   - 减少 JOIN 可以提升性能
   - 数据冗余换取性能是值得的

### 数据模型设计

```java
// FlowNode PO
public class FlowNode {
    private Long id;
    private Long flowDefId;
    private String nodeName;
    private Integer nodeType;
    
    // 网关相关字段（冗余在节点中）
    private Integer gatewayType;        // 网关类型
    private Integer gatewayMode;       // 并行网关模式
    private Long gatewayId;             // 网关ID（用于关联Split和Join）
    private String conditionExpression; // 条件表达式（仅条件网关）
    
    // ... 其他字段
}
```

### Split 和 Join 关联方式

```
节点A (Split)                   节点B (Join)
├─ gatewayId: 100              ├─ gatewayId: 100  (相同)
├─ gatewayType: PARALLEL_SPLIT ├─ gatewayType: PARALLEL_JOIN
└─ gatewayMode: PARALLEL_ALL   └─ gatewayMode: PARALLEL_ALL (相同)
```

**关联逻辑**：
- Split 和 Join 节点通过相同的 `gatewayId` 关联
- 查询 Join 节点时，可以通过 `gatewayId` 找到对应的 Split 节点
- 查询 Split 节点时，可以通过 `gatewayId` 找到对应的 Join 节点

### 查询示例

```java
// 查询 Split 节点对应的 Join 节点
FlowNode splitNode = flowNodeRepository.findById(splitNodeId);
Long gatewayId = splitNode.getGatewayId();

// 通过 gatewayId 查找 Join 节点
FlowNode joinNode = flowNodeRepository.findByGatewayIdAndGatewayType(
    gatewayId, 
    GatewayType.PARALLEL_JOIN
);
```

## 📊 对比总结

| 维度 | 独立实体 | 数据冗余 ⭐ |
|------|---------|-----------|
| **查询性能** | 需要 JOIN | 单表查询，性能好 |
| **代码复杂度** | 需要维护两个实体 | 只需维护一个实体 |
| **数据一致性** | 需要保证关联关系 | 通过 gatewayId 保证 |
| **扩展性** | 网关信息独立扩展 | 网关信息随节点扩展 |
| **适用场景** | 网关信息复杂且独立 | 网关信息简单且与节点强相关 ⭐ |

## ✅ 最终建议

**采用数据冗余方案**，原因：

1. **网关信息与节点强相关**：网关信息是节点的属性，不是独立的概念
2. **查询性能优先**：节点查询是高频操作，冗余可以避免 JOIN
3. **实现简单**：不需要额外的 Gateway 表和维护逻辑
4. **符合实际使用**：99% 的场景都是查询节点时同时需要网关信息

### 实现要点

1. **在 FlowNode 中冗余网关信息**
   - `gatewayType` - 网关类型
   - `gatewayMode` - 并行网关模式
   - `gatewayId` - 网关ID（用于关联）
   - `conditionExpression` - 条件表达式

2. **通过 gatewayId 关联 Split 和 Join**
   - Split 节点和 Join 节点存储相同的 `gatewayId`
   - 通过 `gatewayId` 查找对应的节点

3. **可以删除独立的 Gateway 实体**
   - 网关信息直接存储在 FlowNode 中
   - 不需要独立的 Gateway 表和实体

## 🔄 迁移建议

如果已经创建了 Gateway 实体，可以：

1. **保留 Gateway 实体**（可选）
   - 作为领域概念存在
   - 但不持久化到数据库
   - 仅用于业务逻辑处理

2. **或者完全删除 Gateway 实体**
   - 网关信息完全冗余在 FlowNode 中
   - 通过 FlowNode 的方法访问网关信息

## 📝 示例代码

```java
// FlowNode 领域实体
public class FlowNode {
    
    public GatewayType getGatewayType() {
        if (po == null || po.getGatewayType() == null) {
            return GatewayType.NONE;
        }
        return GatewayType.fromValue(po.getGatewayType());
    }
    
    public boolean isGateway() {
        return getGatewayType().isGateway();
    }
    
    public Long getGatewayId() {
        return po != null ? po.getGatewayId() : null;
    }
    
    // 查找对应的 Join 节点
    public Optional<FlowNode> findCorrespondingJoin(FlowNodeRepository repository) {
        if (!isSplitGateway()) {
            return Optional.empty();
        }
        Long gatewayId = getGatewayId();
        GatewayType joinType = getGatewayType().getCorrespondingJoin();
        return repository.findByGatewayIdAndGatewayType(gatewayId, joinType);
    }
}
```

---

**结论**：推荐使用数据冗余方案，网关信息直接存储在 FlowNode 中，通过 `gatewayId` 关联 Split 和 Join 节点。

