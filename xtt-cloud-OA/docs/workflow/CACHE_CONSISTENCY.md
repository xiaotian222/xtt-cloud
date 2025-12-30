# 缓存一致性保证方案

## 概述

本文档说明 Workflow 模块中缓存一致性保证的实现方案和最佳实践。

## 缓存一致性策略

### 1. Cache Aside（旁路缓存）模式

**实现方式：**
- **读操作**：先查缓存，命中则返回；未命中则查数据库/远程服务，写入缓存后返回
- **写操作**：更新数据库/远程服务，删除相关缓存（让下次读取时重新加载）

**优点：**
- 实现简单
- 缓存和数据库解耦
- 适合读多写少的场景

**缺点：**
- 可能出现短暂的不一致（删除缓存后、下次读取前）
- 需要处理缓存穿透、缓存击穿、缓存雪崩等问题

### 2. 事件驱动更新

**实现方式：**
- Platform 服务数据变更时，通过事件通知 Workflow 服务
- Workflow 服务监听事件，主动更新或删除缓存

**优点：**
- 解耦，支持多服务缓存同步
- 实时性好

**缺点：**
- 需要消息中间件
- 可能存在延迟

## 缓存一致性保证机制

### 1. 流程实例缓存

**更新时机：**
- 流程实例创建后：更新缓存
- 流程实例状态变更后：更新缓存（审批、拒绝、回退、暂停、恢复等）
- 流程变量更新后：更新缓存

**失效时机：**
- 流程实例删除时：删除缓存
- 流程实例状态变更时：更新缓存（先删除旧缓存，再写入新缓存）

**实现位置：**
- `FlowApplicationService`：在保存流程实例后调用 `CacheUpdateService.updateFlowInstanceCache()`

### 2. 用户/角色/部门缓存

**更新时机：**
- 从 Platform 服务获取数据后：自动写入缓存
- 缓存未命中时：从远程服务获取并缓存

**失效时机：**
- Platform 服务数据变更时：通过事件通知失效缓存
- 用户角色/部门关系变更时：失效相关缓存

**实现位置：**
- `PlatformUserServiceAdapter`：在获取数据后自动缓存
- `CacheInvalidationEventHandler`：监听事件并失效缓存

## 缓存失效事件

### 事件类型

```java
public enum CacheType {
    USER,           // 用户缓存
    ROLE,           // 角色缓存
    DEPARTMENT,     // 部门缓存
    FLOW_INSTANCE   // 流程实例缓存
}

public enum OperationType {
    UPDATE,         // 更新
    DELETE,         // 删除
    CREATE          // 创建
}
```

### 事件处理

`CacheInvalidationEventHandler` 监听 `CacheInvalidationEvent` 事件，并根据事件类型执行相应的缓存失效操作。

## 最佳实践

### 1. 写操作后更新缓存

```java
@Transactional
public void approve(ApproveCommand command) {
    // 1. 业务逻辑
    FlowInstance flowInstance = ...;
    flowInstance.approve(...);
    
    // 2. 保存到数据库
    flowInstanceRepository.save(flowInstance);
    
    // 3. 更新缓存
    FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
    cacheUpdateService.updateFlowInstanceCache(dto);
}
```

### 2. 读操作优先查缓存

```java
public List<Approver> convertToApprovers(List<Long> userIds) {
    // 1. 先从缓存获取
    List<UserInfoDto> cachedUsers = userCacheService.getUsersByIds(userIds);
    
    // 2. 找出缺失的用户
    List<Long> missingUserIds = ...;
    
    // 3. 从远程服务获取缺失的用户
    if (!missingUserIds.isEmpty()) {
        List<UserInfoDto> remoteUsers = platformFeignClient.getUsersByIds(missingUserIds);
        // 4. 写入缓存
        userCacheService.cacheUsers(remoteUsers);
    }
}
```

### 3. 异常容错

所有缓存操作都应该有异常处理，确保缓存失败不影响业务逻辑：

```java
try {
    cacheUpdateService.updateFlowInstanceCache(dto);
} catch (Exception e) {
    log.error("更新缓存失败", e);
    // 不抛出异常，继续执行
}
```

### 4. 缓存过期时间

- **流程实例缓存**：30分钟（频繁变更）
- **用户/角色/部门缓存**：1小时（相对稳定）

可通过配置文件调整：

```yaml
workflow:
  cache:
    user:
      expiration: 3600
    role:
      expiration: 3600
    department:
      expiration: 3600
    instance:
      expiration: 1800
```

## 潜在问题和解决方案

### 1. 缓存穿透

**问题**：查询不存在的数据，每次都查数据库

**解决方案**：
- 缓存空值（设置较短的过期时间）
- 使用布隆过滤器

### 2. 缓存击穿

**问题**：热点数据过期，大量请求同时查数据库

**解决方案**：
- 使用分布式锁
- 设置热点数据永不过期，异步更新

### 3. 缓存雪崩

**问题**：大量缓存同时过期，导致数据库压力激增

**解决方案**：
- 设置随机过期时间
- 使用多级缓存
- 限流降级

### 4. 数据不一致

**问题**：缓存和数据库数据不一致

**解决方案**：
- 写操作后立即删除缓存（Cache Aside）
- 使用事件驱动更新
- 设置合理的过期时间

## 监控和告警

建议监控以下指标：
- 缓存命中率
- 缓存更新失败次数
- 缓存过期时间分布
- Redis 连接数和内存使用

## 总结

当前实现采用 **Cache Aside + 事件驱动更新** 的组合方案：
- 读操作：优先查缓存，未命中则查数据库并缓存
- 写操作：更新数据库后删除/更新缓存
- 跨服务：通过事件通知其他服务更新缓存

这种方案在保证缓存一致性的同时，兼顾了性能和实现复杂度。


