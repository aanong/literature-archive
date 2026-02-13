package com.literature.lock.core;

import com.literature.lock.annotation.LockType;
import com.literature.lock.autoconfigure.LockProperties;
import com.literature.lock.exception.LockAcquireException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁模板，提供编程式 API
 * <p>
 * 使用示例：
 * <pre>
 * // 带返回值
 * String result = lockTemplate.executeWithLock("order:123", () -> {
 *     return processOrder();
 * });
 * 
 * // 无返回值
 * lockTemplate.executeWithLock("order:123", () -> {
 *     processOrder();
 * });
 * 
 * // 自定义参数
 * lockTemplate.executeWithLock("order:123", LockType.FAIR, 5, 30, TimeUnit.SECONDS, () -> {
 *     return processOrder();
 * });
 * </pre>
 */
public class DistributedLockTemplate {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockTemplate.class);

    private final LockExecutor lockExecutor;
    private final LockProperties lockProperties;

    public DistributedLockTemplate(LockExecutor lockExecutor, LockProperties lockProperties) {
        this.lockExecutor = lockExecutor;
        this.lockProperties = lockProperties;
    }

    /**
     * 在分布式锁保护下执行操作（使用默认配置）
     *
     * @param key      锁的 key
     * @param supplier 要执行的操作
     * @param <T>      返回值类型
     * @return 操作返回值
     * @throws LockAcquireException 如果获取锁失败
     */
    public <T> T executeWithLock(String key, Supplier<T> supplier) {
        return executeWithLock(
            key,
            LockType.REENTRANT,
            lockProperties.getRedis().getWaitTime().getSeconds(),
            lockProperties.getRedis().getLeaseTime().getSeconds(),
            TimeUnit.SECONDS,
            supplier
        );
    }

    /**
     * 在分布式锁保护下执行操作（无返回值，使用默认配置）
     *
     * @param key      锁的 key
     * @param runnable 要执行的操作
     * @throws LockAcquireException 如果获取锁失败
     */
    public void executeWithLock(String key, Runnable runnable) {
        executeWithLock(key, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 在分布式锁保护下执行操作（完整参数）
     *
     * @param key       锁的 key
     * @param type      锁类型
     * @param waitTime  等待时间
     * @param leaseTime 租约时间（-1 表示启用看门狗）
     * @param timeUnit  时间单位
     * @param supplier  要执行的操作
     * @param <T>       返回值类型
     * @return 操作返回值
     * @throws LockAcquireException 如果获取锁失败
     */
    public <T> T executeWithLock(String key, LockType type, long waitTime, long leaseTime,
                                  TimeUnit timeUnit, Supplier<T> supplier) {
        LockExecutor.LockInfo lockInfo = null;
        try {
            lockInfo = lockExecutor.tryLock(key, type, waitTime, leaseTime, timeUnit);
            if (lockInfo == null) {
                throw new LockAcquireException(key);
            }
            return supplier.get();
        } finally {
            if (lockInfo != null) {
                lockExecutor.unlock(lockInfo);
            }
        }
    }

    /**
     * 在分布式锁保护下执行操作（完整参数，无返回值）
     */
    public void executeWithLock(String key, LockType type, long waitTime, long leaseTime,
                                 TimeUnit timeUnit, Runnable runnable) {
        executeWithLock(key, type, waitTime, leaseTime, timeUnit, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 尝试在分布式锁保护下执行操作，获取失败时返回默认值
     *
     * @param key          锁的 key
     * @param supplier     要执行的操作
     * @param defaultValue 获取锁失败时的默认值
     * @param <T>          返回值类型
     * @return 操作返回值或默认值
     */
    public <T> T tryExecuteWithLock(String key, Supplier<T> supplier, T defaultValue) {
        return tryExecuteWithLock(
            key,
            LockType.REENTRANT,
            lockProperties.getRedis().getWaitTime().getSeconds(),
            lockProperties.getRedis().getLeaseTime().getSeconds(),
            TimeUnit.SECONDS,
            supplier,
            defaultValue
        );
    }

    /**
     * 尝试在分布式锁保护下执行操作，获取失败时返回默认值（完整参数）
     */
    public <T> T tryExecuteWithLock(String key, LockType type, long waitTime, long leaseTime,
                                     TimeUnit timeUnit, Supplier<T> supplier, T defaultValue) {
        LockExecutor.LockInfo lockInfo = null;
        try {
            lockInfo = lockExecutor.tryLock(key, type, waitTime, leaseTime, timeUnit);
            if (lockInfo == null) {
                log.debug("Failed to acquire lock, returning default value: {}", key);
                return defaultValue;
            }
            return supplier.get();
        } finally {
            if (lockInfo != null) {
                lockExecutor.unlock(lockInfo);
            }
        }
    }

    /**
     * 尝试在分布式锁保护下执行操作，获取失败时不执行
     *
     * @param key      锁的 key
     * @param runnable 要执行的操作
     * @return 是否成功获取锁并执行
     */
    public boolean tryExecuteWithLock(String key, Runnable runnable) {
        return tryExecuteWithLock(
            key,
            LockType.REENTRANT,
            lockProperties.getRedis().getWaitTime().getSeconds(),
            lockProperties.getRedis().getLeaseTime().getSeconds(),
            TimeUnit.SECONDS,
            runnable
        );
    }

    /**
     * 尝试在分布式锁保护下执行操作，获取失败时不执行（完整参数）
     */
    public boolean tryExecuteWithLock(String key, LockType type, long waitTime, long leaseTime,
                                       TimeUnit timeUnit, Runnable runnable) {
        LockExecutor.LockInfo lockInfo = null;
        try {
            lockInfo = lockExecutor.tryLock(key, type, waitTime, leaseTime, timeUnit);
            if (lockInfo == null) {
                log.debug("Failed to acquire lock, skipping execution: {}", key);
                return false;
            }
            runnable.run();
            return true;
        } finally {
            if (lockInfo != null) {
                lockExecutor.unlock(lockInfo);
            }
        }
    }
}
