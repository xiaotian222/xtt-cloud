package xtt.cloud.oa.workflow.infrastructure.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.UUID;

/**
 * 基于 Redis 的分布式锁服务实现
 * 
 * 使用 Redis SETNX 命令实现分布式锁，支持锁超时自动释放
 * 
 * @author xtt
 */
@Service
@ConditionalOnProperty(name = "workflow.lock.enabled", havingValue = "true", matchIfMissing = true)
public class RedisDistributedLockService implements DistributedLockService {
    
    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLockService.class);
    
    private static final String LOCK_PREFIX = "workflow:lock:";
    private static final String LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    private final ThreadLocal<String> lockValueHolder = new ThreadLocal<>();
    
    @Value("${workflow.lock.default-wait-time:3}")
    private long defaultWaitTime; // 默认等待时间（秒）
    
    @Value("${workflow.lock.default-lease-time:10}")
    private long defaultLeaseTime; // 默认锁持有时间（秒）

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        if (lockKey == null || lockKey.isEmpty()) {
            return false;
        }
        
        String fullKey = LOCK_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();
        long waitTimeMillis = timeUnit.toMillis(waitTime);
        long leaseTimeSeconds = timeUnit.toSeconds(leaseTime);
        long startTime = System.currentTimeMillis();
        
        try {
            while (System.currentTimeMillis() - startTime < waitTimeMillis) {
                // 尝试获取锁：SET key value NX EX timeout
                Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    fullKey, 
                    lockValue, 
                    leaseTimeSeconds, 
                    TimeUnit.SECONDS
                );
                
                if (Boolean.TRUE.equals(success)) {
                    // 成功获取锁，保存锁值到 ThreadLocal
                    lockValueHolder.set(lockValue);
                    log.debug("获取锁成功，锁键: {}", lockKey);
                    return true;
                }
                
                // 获取锁失败，等待一小段时间后重试
                try {
                    Thread.sleep(50); // 等待50ms后重试
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("获取锁被中断，锁键: {}", lockKey);
                    return false;
                }
            }
            
            log.debug("获取锁超时，锁键: {}", lockKey);
            return false;
        } catch (Exception e) {
            log.error("获取锁失败，锁键: {}", lockKey, e);
            return false;
        }
    }
    
    @Override
    public boolean tryLock(String lockKey, long leaseTime, TimeUnit timeUnit) {
        return tryLock(lockKey, defaultWaitTime, leaseTime, timeUnit);
    }
    
    @Override
    public void unlock(String lockKey) {
        if (lockKey == null || lockKey.isEmpty()) {
            return;
        }
        
        String fullKey = LOCK_PREFIX + lockKey;
        String lockValue = lockValueHolder.get();
        
        if (lockValue == null) {
            log.warn("当前线程未持有锁，锁键: {}", lockKey);
            return;
        }
        
        try {
            // 使用 Lua 脚本确保只释放自己持有的锁
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(LOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = redisTemplate.execute(script, Collections.singletonList(fullKey), lockValue);
            
            if (result != null && result > 0) {
                lockValueHolder.remove();
                log.debug("释放锁成功，锁键: {}", lockKey);
            } else {
                log.warn("释放锁失败，可能锁已过期或被其他线程释放，锁键: {}", lockKey);
            }
        } catch (Exception e) {
            log.error("释放锁失败，锁键: {}", lockKey, e);
        } finally {
            lockValueHolder.remove();
        }
    }
    
    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {
        if (!tryLock(lockKey, waitTime, leaseTime, timeUnit)) {
            log.debug("获取锁失败，锁键: {}", lockKey);
            return null;
        }
        
        try {
            return supplier.get();
        } finally {
            unlock(lockKey);
        }
    }
    
    @Override
    public <T> T executeWithLock(String lockKey, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {
        return executeWithLock(lockKey, defaultWaitTime, leaseTime, timeUnit, supplier);
    }
    
    @Override
    public <T> T executeWithLockOrThrow(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier) {
        if (!tryLock(lockKey, waitTime, leaseTime, timeUnit)) {
            throw new LockAcquisitionException("获取锁失败，锁键: " + lockKey);
        }
        
        try {
            return supplier.get();
        } finally {
            unlock(lockKey);
        }
    }
}
