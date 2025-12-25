package xtt.cloud.oa.workflow.infrastructure.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务接口
 * 
 * 用于在分布式环境下实现互斥操作，防止缓存击穿、重复提交等问题
 * 
 * @author xtt
 */
public interface DistributedLockService {
    
    /**
     * 尝试获取锁
     * 
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit);
    
    /**
     * 尝试获取锁（默认等待时间）
     * 
     * @param lockKey 锁的键
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(String lockKey, long leaseTime, TimeUnit timeUnit);
    
    /**
     * 释放锁
     * 
     * @param lockKey 锁的键
     */
    void unlock(String lockKey);
    
    /**
     * 执行带锁的操作
     * 
     * 如果获取锁失败，返回 null
     * 
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @param supplier 要执行的操作
     * @return 操作结果，如果获取锁失败则返回 null
     */
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);
    
    /**
     * 执行带锁的操作（默认等待时间）
     * 
     * @param lockKey 锁的键
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @param supplier 要执行的操作
     * @return 操作结果，如果获取锁失败则返回 null
     */
    <T> T executeWithLock(String lockKey, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);
    
    /**
     * 执行带锁的操作（必须获取锁，否则抛出异常）
     * 
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param leaseTime 锁的持有时间
     * @param timeUnit 时间单位
     * @param supplier 要执行的操作
     * @return 操作结果
     * @throws LockAcquisitionException 如果获取锁失败
     */
    <T> T executeWithLockOrThrow(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> supplier);
}

