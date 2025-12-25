package xtt.cloud.oa.workflow.infrastructure.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.workflow.infrastructure.lock.DistributedLockService;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 带分布式锁的缓存服务辅助类
 * 
 * 用于在缓存操作中使用分布式锁，防止缓存击穿
 * 
 * @author xtt
 */
@Component
public class CacheServiceWithLock {
    
    private static final Logger log = LoggerFactory.getLogger(CacheServiceWithLock.class);
    
    private final DistributedLockService distributedLockService;
    
    @org.springframework.beans.factory.annotation.Value("${workflow.cache.lock.wait-time:3}")
    private long defaultWaitTime; // 默认等待时间（秒）
    
    @org.springframework.beans.factory.annotation.Value("${workflow.cache.lock.lease-time:10}")
    private long defaultLeaseTime; // 默认锁持有时间（秒）
    
    public CacheServiceWithLock(DistributedLockService distributedLockService) {
        this.distributedLockService = distributedLockService;
    }
    
    /**
     * 带锁的缓存获取操作
     * 
     * 流程：
     * 1. 先查缓存
     * 2. 缓存未命中，获取分布式锁
     * 3. 双重检查缓存（可能在等待锁期间，其他线程已经更新了缓存）
     * 4. 如果缓存仍为空，执行数据加载并更新缓存
     * 
     * @param cacheKey 缓存键
     * @param cacheGetter 缓存获取函数
     * @param dataLoader 数据加载函数（当缓存未命中时调用）
     * @param cacheUpdater 缓存更新函数
     * @return 数据
     */
    public <T> Optional<T> getWithLock(
            String cacheKey,
            Supplier<Optional<T>> cacheGetter,
            Supplier<T> dataLoader,
            java.util.function.Consumer<T> cacheUpdater) {
        
        // 1. 先查缓存
        Optional<T> cached = cacheGetter.get();
        if (cached.isPresent()) {
            return cached;
        }
        
        // 2. 缓存未命中，使用分布式锁
        String lockKey = "cache:" + cacheKey;
        T result = distributedLockService.executeWithLock(
            lockKey,
            defaultWaitTime,
            defaultLeaseTime,
            TimeUnit.SECONDS,
            () -> {
                // 3. 双重检查缓存
                Optional<T> cachedAgain = cacheGetter.get();
                if (cachedAgain.isPresent()) {
                    return cachedAgain.get();
                }
                
                // 4. 加载数据并更新缓存
                T data = dataLoader.get();
                if (data != null) {
                    cacheUpdater.accept(data);
                }
                return data;
            }
        );
        
        // 如果获取锁失败，直接加载数据（降级策略）
        if (result == null) {
            log.warn("获取分布式锁失败，直接加载数据，缓存键: {}", cacheKey);
            T data = dataLoader.get();
            if (data != null) {
                cacheUpdater.accept(data);
            }
            return Optional.ofNullable(data);
        }
        
        return Optional.ofNullable(result);
    }
}

