package com.literature.lock.exception;

/**
 * 分布式锁获取异常
 */
public class LockAcquireException extends RuntimeException {

    private final String lockKey;

    public LockAcquireException(String lockKey) {
        super(String.format("Failed to acquire distributed lock: %s", lockKey));
        this.lockKey = lockKey;
    }

    public LockAcquireException(String lockKey, String message) {
        super(message);
        this.lockKey = lockKey;
    }

    public LockAcquireException(String lockKey, Throwable cause) {
        super(String.format("Failed to acquire distributed lock: %s", lockKey), cause);
        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }
}
