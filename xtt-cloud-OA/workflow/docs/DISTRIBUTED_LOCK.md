# 分布式锁实现文档

## 概述

本文档说明 Workflow 模块中分布式锁的实现和使用方式。分布式锁主要用于防止缓存击穿、重复提交等问题。

## 实现方案

### 基于 Redis 的分布式锁

使用 Redis `SETNX` 命令实现分布式锁，具有以下特性：

1. **原子性**：使用 Redis 的原子操作保证锁的获取和释放
2. **超时自动释放**：锁设置过期时间，防止死锁
3. **安全释放**：使用 Lua 脚本确保只释放自己持有的锁
4. **可重试**：支持等待时间，在等待期间重试获取锁

## 核心组件

### 1. DistributedLockService 接口

定义了分布式锁的基本操作：

```java
public interface DistributedLockService {
    // 尝试获取锁
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit);
    
    // 释放锁
    void unlock(String lockKey);
    
    // 执行带锁的操作
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);
    
    // 执行带锁的操作（必须获取锁，否则抛出异常）
    <T> T executeWithLockOrThrow(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);
}
```

### 2. RedisDistributedLockService 实现

基于 Redis 的分布式锁实现：

- **锁键格式**：`workflow:lock:{lockKey}`
- **锁值**：使用 UUID 确保唯一性
- **锁释放**：使用 Lua 脚本确保只释放自己持有的锁

## 使用场景

### 1. 防止缓存击穿

**场景**：当缓存过期时，大量请求同时查询数据库

**解决方案**：使用分布式锁，只有一个线程查询数据库并更新缓存

**示例**：

```java
@Transactional(readOnly = true)
public FlowInstanceDTO getFlowInstance(Long flowInstanceId) {
    // 1. 先从缓存获取
    Optional<FlowInstanceDTO> cached = flowInstanceCacheService.getFlowInstanceById(flowInstanceId);
    if (cached.isPresent()) {
        return cached.get();
    }
    
    // 2. 缓存未命中，使用分布式锁
    String lockKey = "flow_instance:" + flowInstanceId;
    FlowInstanceDTO result = distributedLockService.executeWithLock(
        lockKey,
        3, // 等待3秒
        10, // 锁持有10秒
        TimeUnit.SECONDS,
        () -> {
            // 双重检查：再次检查缓存
            Optional<FlowInstanceDTO> cachedAgain = flowInstanceCacheService.getFlowInstanceById(flowInstanceId);
            if (cachedAgain.isPresent()) {
                return cachedAgain.get();
            }
            
            // 从数据库加载
            FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
            FlowInstanceDTO dto = FlowInstanceAssembler.toDTO(flowInstance);
            
            // 更新缓存
            cacheUpdateService.updateFlowInstanceCache(dto);
            
            return dto;
        }
    );
    
    // 如果获取锁失败，直接查数据库（降级策略）
    if (result == null) {
        FlowInstance flowInstance = flowInstanceFactory.loadFlowInstance(flowInstanceId);
        return FlowInstanceAssembler.toDTO(flowInstance);
    }
    
    return result;
}
```

### 2. 防止重复提交

**场景**：防止用户重复提交相同的操作

**解决方案**：使用分布式锁，在操作执行期间加锁

**示例**：

```java
@Transactional
public void approve(ApproveCommand command) {
    String lockKey = "approve:" + command.getFlowInstanceId() + ":" + command.getNodeInstanceId();
    
    distributedLockService.executeWithLockOrThrow(
        lockKey,
        3,
        30, // 审批操作可能需要较长时间
        TimeUnit.SECONDS,
        () -> {
            // 执行审批逻辑
            // ...
            return null;
        }
    );
}
```

### 3. 并发控制

**场景**：限制某些操作的并发数

**解决方案**：使用分布式锁控制并发

## 配置

### application.yml

```yaml
workflow:
  lock:
    enabled: true  # 是否启用分布式锁（默认 true）
    default-wait-time: 3  # 默认等待时间（秒）
    default-lease-time: 10  # 默认锁持有时间（秒）
  cache:
    lock:
      wait-time: 3  # 缓存锁等待时间（秒）
      lease-time: 10  # 缓存锁持有时间（秒）
```

## 最佳实践

### 1. 锁的粒度

- **细粒度锁**：针对具体资源加锁（如：`flow_instance:123`）
- **避免粗粒度锁**：不要对整个服务加锁

### 2. 锁的超时时间

- **合理设置**：根据操作耗时设置锁的超时时间
- **避免过长**：锁持有时间过长会影响并发性能
- **避免过短**：锁持有时间过短可能导致操作未完成就被释放

### 3. 降级策略

- **获取锁失败时**：应该有降级策略，不要直接失败
- **示例**：获取锁失败时，直接查询数据库（如上面的 `getFlowInstance` 方法）

### 4. 异常处理

- **确保释放锁**：使用 `try-finally` 确保锁被释放
- **使用 `executeWithLock`**：自动处理锁的释放

### 5. 双重检查

- **缓存场景**：在获取锁后，再次检查缓存（双重检查模式）
- **避免重复加载**：防止多个线程同时加载相同数据

## 注意事项

### 1. 死锁风险

- **自动释放**：锁设置了过期时间，会自动释放，避免死锁
- **合理超时**：确保锁的超时时间大于操作耗时

### 2. 锁的释放

- **安全释放**：使用 Lua 脚本确保只释放自己持有的锁
- **避免误释放**：不要释放其他线程持有的锁

### 3. 性能影响

- **锁竞争**：高并发场景下，锁竞争可能影响性能
- **优化建议**：尽量减少锁的持有时间，使用细粒度锁

### 4. Redis 可用性

- **单点故障**：Redis 故障会导致分布式锁失效
- **建议**：使用 Redis 集群或哨兵模式提高可用性

## 监控和告警

建议监控以下指标：

1. **锁获取成功率**：监控锁获取失败的情况
2. **锁持有时间**：监控锁的持有时间分布
3. **锁等待时间**：监控获取锁的等待时间
4. **锁竞争情况**：监控同一锁的竞争情况

## 总结

分布式锁是保证缓存一致性和防止并发问题的重要工具。在使用时需要注意：

1. 合理设置锁的超时时间
2. 使用细粒度锁
3. 实现降级策略
4. 确保锁的正确释放
5. 监控锁的使用情况

通过合理使用分布式锁，可以有效防止缓存击穿、重复提交等问题，提高系统的稳定性和性能。

