package com.literature.lock.core;

import com.literature.lock.annotation.LockType;
import com.literature.lock.autoconfigure.LockProperties;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的分布式锁执行器实现
 */
public class RedissonLockExecutor implements LockExecutor {

    private static final Logger log = LoggerFactory.getLogger(RedissonLockExecutor.class);

    private final RedissonClient redissonClient;
    private final LockProperties lockProperties;

    public RedissonLockExecutor(RedissonClient redissonClient, LockProperties lockProperties) {
        this.redissonClient = redissonClient;
        this.lockProperties = lockProperties;
    }

    @Override
    public LockInfo tryLock(String key, LockType type, long waitTime, long leaseTime, TimeUnit timeUnit) {
        String fullKey = lockProperties.getRedis().getKeyPrefix() + key;
        RLock lock = getLock(fullKey, type);
        
        try {
            boolean acquired;
            if (leaseTime == -1) {
                // 启用看门狗自动续期
                acquired = lock.tryLock(waitTime, timeUnit);
            } else {
                acquired = lock.tryLock(waitTime, leaseTime, timeUnit);
            }
            
            if (acquired) {
                log.debug("Successfully acquired lock: {}, type: {}", fullKey, type);
                return new LockInfo(fullKey, type, lock, Thread.currentThread());
            } else {
                log.debug("Failed to acquire lock: {}, type: {}", fullKey, type);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted: {}", fullKey, e);
            return null;
        }
    }

    @Override
    public void unlock(LockInfo lockInfo) {
        if (lockInfo == null || lockInfo.lock() == null) {
            return;
        }
        
        RLock lock = (RLock) lockInfo.lock();
        
        // 确保只有持有锁的线程才能释放
        if (lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
                log.debug("Successfully released lock: {}", lockInfo.key());
            } catch (IllegalMonitorStateException e) {
                log.warn("Failed to release lock (not held by current thread): {}", lockInfo.key());
            }
        } else {
            log.warn("Current thread does not hold the lock: {}", lockInfo.key());
        }
    }

    /**
     * 根据锁类型获取对应的 RLock 实例
     */
    private RLock getLock(String key, LockType type) {
        return switch (type) {
            case REENTRANT -> redissonClient.getLock(key);
            case FAIR -> redissonClient.getFairLock(key);
            case READ -> {
                RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
                yield rwLock.readLock();
            }
            case WRITE -> {
                RReadWriteLock rwLock = redissonClient.getReadWriteLock(key);
                yield rwLock.writeLock();
            }
        };
    }
}
