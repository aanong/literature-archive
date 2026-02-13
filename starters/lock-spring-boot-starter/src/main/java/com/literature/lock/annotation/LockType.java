package com.literature.lock.annotation;

/**
 * 锁类型枚举
 */
public enum LockType {

    /**
     * 可重入锁（默认）
     */
    REENTRANT,

    /**
     * 公平锁
     */
    FAIR,

    /**
     * 读锁
     */
    READ,

    /**
     * 写锁
     */
    WRITE
}
