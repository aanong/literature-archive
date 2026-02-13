package com.literature.lock.core;

import com.literature.lock.annotation.LockType;

import java.util.concurrent.TimeUnit;

/**
 * 锁执行器接口
 */
public interface LockExecutor {

    /**
     * 尝试获取锁
     *
     * @param key       锁的 key
     * @param type      锁类型
     * @param waitTime  等待时间
     * @param leaseTime 租约时间（-1 表示启用看门狗）
     * @param timeUnit  时间单位
     * @return 锁信息，获取失败返回 null
     */
    LockInfo tryLock(String key, LockType type, long waitTime, long leaseTime, TimeUnit timeUnit);

    /**
     * 释放锁
     *
     * @param lockInfo 锁信息
     */
    void unlock(LockInfo lockInfo);

    /**
     * 锁信息
     */
    record LockInfo(
        String key,
        LockType type,
        Object lock,
        Thread ownerThread
    ) {}
}
