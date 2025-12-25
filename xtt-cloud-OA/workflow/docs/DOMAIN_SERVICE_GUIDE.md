# Domain Service 领域服务指南

## 📋 Domain Service 的作用

Domain Service（领域服务）是 DDD 中的一个重要概念，用于处理**不属于任何单个实体或值对象的业务逻辑**。

### 1. 什么时候需要 Domain Service？

Domain Service 适用于以下场景：

#### ✅ 场景一：跨聚合的业务逻辑
当业务逻辑涉及多个聚合根时，应该放在 Domain Service 中。

**示例**：节点路由服务
```java
/**
 * 节点路由领域服务
 * 需要查询多个节点定义，计算下一个节点
 */
public interface NodeRoutingService {
    List<Long> getNextNodeIds(Long currentNodeId, Long flowDefId, 
                              Map<String, Object> processVariables);
}
```

#### ✅ 场景二：复杂的业务规则
当业务规则过于复杂，放在实体中会让实体变得臃肿时。

**示例**：条件评估服务
```java
/**
 * 条件评估领域服务
 * 使用 SpEL 表达式评估复杂的业务条件
 */
public interface ConditionEvaluationService {
    boolean evaluate(String conditionExpression, Map<String, Object> processVariables);
}
```

#### ✅ 场景三：需要外部依赖的业务逻辑
当业务逻辑需要调用外部服务（如用户服务、角色服务）时。

**示例**：审批人分配服务
```java
/**
 * 审批人分配领域服务
 * 需要从外部系统获取用户、角色、部门信息
 */
public interface ApproverAssignmentService {
    List<Approver> assignApprovers(Long nodeId, Long flowDefId, 
                                    Long flowInstanceId,
                                    Map<String, Object> processVariables);
}
```

### 2. Domain Service 的特点

- ✅ **包含业务逻辑**：Domain Service 包含核心业务逻辑
- ✅ **无状态**：Domain Service 应该是无状态的（Stateless）
- ✅ **依赖抽象**：Domain Service 只依赖接口，不依赖具体实现
- ✅ **属于领域层**：Domain Service 接口和实现都在 `domain` 包下

---

## ❌ Domain Service 不能做什么？

### 1. **不能直接调用 Infrastructure 层**

❌ **错误示例**：
```java
@Service
public class ApproverAssignmentServiceImpl implements ApproverAssignmentService {
    
    // ❌ 错误：直接依赖 Infrastructure 层的具体实现
    private final PlatformFeignClient platformFeignClient;  // Infrastructure 层
    
    public List<Approver> assignApprovers(...) {
        // ❌ 错误：直接调用 Infrastructure 层
        UserInfoDto user = platformFeignClient.getUserById(userId);
        // ...
    }
}
```

### 2. **不能依赖 Application 层**

❌ **错误示例**：
```java
@Service
public class NodeRoutingServiceImpl implements NodeRoutingService {
    
    // ❌ 错误：Domain Service 不能依赖 Application 层
    private final FlowApplicationService flowApplicationService;
}
```

### 3. **不能包含技术细节**

❌ **错误示例**：
```java
@Service
public class ConditionEvaluationServiceImpl implements ConditionEvaluationService {
    
    // ❌ 错误：不应该直接依赖 MyBatis Mapper
    private final FlowNodeMapper flowNodeMapper;  // Infrastructure 层
    
    // ❌ 错误：不应该直接操作数据库
    public boolean evaluate(...) {
        FlowNodePO po = flowNodeMapper.selectById(nodeId);  // 直接操作数据库
    }
}
```

---

## ✅ 正确的做法：依赖倒置原则（DIP）

Domain Service 应该**依赖抽象（接口）**，而不是具体实现。具体实现由 Infrastructure 层提供。

### 架构图

```
┌─────────────────────────────────────────┐
│         Domain Layer (领域层)            │
│  ┌───────────────────────────────────┐  │
│  │  ApproverAssignmentService        │  │
│  │  (Domain Service Interface)       │  │
│  └──────────────┬────────────────────┘  │
│                 │ 依赖                    │
│  ┌──────────────▼────────────────────┐  │
│  │  ApproverProvider                 │  │
│  │  (接口，定义在 Domain 层)          │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                 ▲
                 │ 实现
┌─────────────────────────────────────────┐
│      Infrastructure Layer (基础设施层)     │
│  ┌───────────────────────────────────┐  │
│  │  PlatformUserServiceAdapter       │  │
│  │  (实现 ApproverProvider 接口)      │  │
│  │  - 调用 PlatformFeignClient        │  │
│  │  - 使用缓存服务                    │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### ✅ 正确示例

#### 1. 在 Domain 层定义接口

```java
// domain/flow/service/ApproverProvider.java
package xtt.cloud.oa.workflow.domain.flow.service;

import xtt.cloud.oa.workflow.domain.flow.model.valueobject.Approver;
import java.util.List;

/**
 * 审批人提供者接口
 * 
 * 领域服务接口，用于从外部系统获取审批人信息
 * 实现类在基础设施层，通过依赖倒置实现
 */
public interface ApproverProvider {
    
    /**
     * 根据用户ID列表转换为审批人列表
     */
    List<Approver> convertToApprovers(List<Long> userIds);
    
    /**
     * 根据角色ID列表获取该角色下的所有用户（去重）
     */
    List<Approver> getUsersByRoleIds(List<Long> roleIds);
    
    /**
     * 根据部门ID列表获取所有部门负责人（去重）
     */
    List<Approver> getDeptLeadersByDeptIds(List<Long> deptIds);
}
```

#### 2. Domain Service 依赖接口

```java
// domain/flow/service/impl/ApproverAssignmentServiceImpl.java
@Service
public class ApproverAssignmentServiceImpl implements ApproverAssignmentService {
    
    // ✅ 正确：依赖 Domain 层定义的接口
    private final ApproverProvider approverProvider;  // 接口，不是具体实现
    
    // ✅ 正确：依赖 Domain 层的 Repository 接口
    private final FlowNodeRepository flowNodeRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    public ApproverAssignmentServiceImpl(
            FlowNodeRepository flowNodeRepository,
            FlowNodeInstanceRepository flowNodeInstanceRepository,
            ApproverProvider approverProvider) {  // 依赖注入接口
        this.flowNodeRepository = flowNodeRepository;
        this.flowNodeInstanceRepository = flowNodeInstanceRepository;
        this.approverProvider = approverProvider;
    }
    
    @Override
    public List<Approver> assignApprovers(...) {
        // ✅ 正确：调用接口方法，不关心具体实现
        return approverProvider.convertToApprovers(userIds);
    }
}
```

#### 3. Infrastructure 层实现接口

```java
// infrastructure/external/platform/PlatformUserServiceAdapter.java
@Component
public class PlatformUserServiceAdapter implements ApproverProvider {
    
    // ✅ 正确：Infrastructure 层可以依赖其他 Infrastructure 组件
    private final PlatformFeignClient platformFeignClient;
    private final UserCacheService userCacheService;
    private final RoleCacheService roleCacheService;
    private final DepartmentCacheService departmentCacheService;
    
    @Override
    public List<Approver> convertToApprovers(List<Long> userIds) {
        // ✅ 正确：在 Infrastructure 层实现具体的技术细节
        List<UserInfoDto> users = userCacheService.getUsersByIds(userIds);
        return users.stream()
                .map(this::convertToApprover)
                .collect(Collectors.toList());
    }
}
```

---

## 🔄 依赖关系总结

### 正确的依赖关系

```
Domain Service (领域服务)
    ↓ 依赖
Domain Repository Interface (领域仓储接口)
    ↑ 实现
Infrastructure Repository Implementation (基础设施仓储实现)

Domain Service (领域服务)
    ↓ 依赖
Domain Service Interface (领域服务接口，如 ApproverProvider)
    ↑ 实现
Infrastructure Adapter (基础设施适配器)
```

### 依赖规则

| 层级 | 可以依赖 | 不能依赖 |
|------|---------|---------|
| **Domain Service** | ✅ Domain 层的接口（Repository、Service 接口）<br>✅ Domain 层的实体和值对象 | ❌ Infrastructure 层的具体实现<br>❌ Application 层<br>❌ Interfaces 层 |

---

## 📝 当前代码检查

### ✅ 正确的实现

1. **NodeRoutingServiceImpl**
   - ✅ 依赖 `FlowNodeRepository` 接口（Domain 层）
   - ✅ 依赖 `FlowNodeInstanceRepository` 接口（Domain 层）
   - ✅ 依赖 `ConditionEvaluationService` 接口（Domain 层）

2. **ApproverAssignmentServiceImpl**
   - ✅ 依赖 `ApproverProvider` 接口（Domain 层定义）
   - ✅ 依赖 `FlowNodeRepository` 接口（Domain 层）
   - ✅ 依赖 `FlowNodeInstanceRepository` 接口（Domain 层）

3. **ConditionEvaluationServiceImpl**
   - ✅ 只使用 Spring SpEL（框架工具，不违反原则）
   - ✅ 无外部依赖

### ⚠️ 需要注意的地方

1. **ObjectMapper 的使用**
   ```java
   // NodeRoutingServiceImpl 中直接 new ObjectMapper()
   private final ObjectMapper objectMapper;
   
   // ✅ 可以接受：ObjectMapper 是工具类，不违反原则
   // 或者通过构造函数注入，由 Spring 管理
   ```

2. **Spring 注解的使用**
   ```java
   @Service  // ✅ 可以接受：这是 Spring 的标记注解，不违反依赖原则
   public class NodeRoutingServiceImpl implements NodeRoutingService {
   ```

---

## 🎯 最佳实践

### 1. 接口定义在 Domain 层

所有 Domain Service 需要的接口都应该定义在 Domain 层：

```
domain/
  └── flow/
      └── service/
          ├── NodeRoutingService.java          (接口)
          ├── ConditionEvaluationService.java  (接口)
          ├── ApproverAssignmentService.java  (接口)
          ├── ApproverProvider.java            (接口，供 Domain Service 使用)
          └── impl/
              ├── NodeRoutingServiceImpl.java
              ├── ConditionEvaluationServiceImpl.java
              └── ApproverAssignmentServiceImpl.java
```

### 2. 实现类在 Infrastructure 层

所有需要调用外部系统或使用技术框架的实现都在 Infrastructure 层：

```
infrastructure/
  └── external/
      └── platform/
          └── PlatformUserServiceAdapter.java  (实现 ApproverProvider)
```

### 3. 通过依赖注入连接

使用 Spring 的依赖注入机制，让 Infrastructure 层的实现自动注入到 Domain Service：

```java
// Spring 会自动将 PlatformUserServiceAdapter 注入到 ApproverAssignmentServiceImpl
@Service
public class ApproverAssignmentServiceImpl implements ApproverAssignmentService {
    public ApproverAssignmentServiceImpl(ApproverProvider approverProvider) {
        // Spring 会注入 PlatformUserServiceAdapter 实例
    }
}
```

---

## 📚 总结

### Domain Service 的作用

1. ✅ 处理跨聚合的业务逻辑
2. ✅ 封装复杂的业务规则
3. ✅ 协调多个领域对象完成业务操作

### Domain Service 的依赖规则

1. ✅ **可以依赖**：
   - Domain 层的接口（Repository、Service 接口）
   - Domain 层的实体和值对象
   - 框架工具类（如 Spring SpEL、Jackson ObjectMapper）

2. ❌ **不能依赖**：
   - Infrastructure 层的具体实现类
   - Application 层
   - Interfaces 层

3. ✅ **正确做法**：
   - 在 Domain 层定义需要的接口
   - 在 Infrastructure 层实现这些接口
   - 通过依赖注入连接

### 核心原则

> **依赖倒置原则（DIP）**：Domain Service 依赖抽象（接口），而不是具体实现。具体实现由 Infrastructure 层提供，通过依赖注入机制连接。

---

## 🔗 相关文档

- [DDD_PROJECT_STRUCTURE.md](./DDD_PROJECT_STRUCTURE.md) - DDD 项目结构说明
- [DDD_REFACTORING_GUIDE.md](./DDD_REFACTORING_GUIDE.md) - DDD 重构指南


