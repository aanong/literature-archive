package com.literature.lock.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 分布式锁配置属性
 */
@ConfigurationProperties(prefix = "literature.lock")
public class LockProperties {

    /**
     * 是否启用分布式锁
     */
    private boolean enabled = true;

    /**
     * 锁实现类型
     */
    private LockType type = LockType.REDIS;

    /**
     * Redis 锁配置
     */
    private RedisLockProperties redis = new RedisLockProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LockType getType() {
        return type;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public RedisLockProperties getRedis() {
        return redis;
    }

    public void setRedis(RedisLockProperties redis) {
        this.redis = redis;
    }

    /**
     * 锁实现类型枚举
     */
    public enum LockType {
        REDIS
    }

    /**
     * Redis 锁配置
     */
    public static class RedisLockProperties {

        /**
         * 锁 key 前缀
         */
        private String keyPrefix = "lit:lock:";

        /**
         * 默认租约时长（锁自动释放时间）
         */
        private Duration leaseTime = Duration.ofSeconds(30);

        /**
         * 默认等待时长（获取锁的最大等待时间）
         */
        private Duration waitTime = Duration.ofSeconds(10);

        /**
         * 看门狗超时时间
         */
        private Duration watchdogTimeout = Duration.ofSeconds(30);

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public Duration getLeaseTime() {
            return leaseTime;
        }

        public void setLeaseTime(Duration leaseTime) {
            this.leaseTime = leaseTime;
        }

        public Duration getWaitTime() {
            return waitTime;
        }

        public void setWaitTime(Duration waitTime) {
            this.waitTime = waitTime;
        }

        public Duration getWatchdogTimeout() {
            return watchdogTimeout;
        }

        public void setWatchdogTimeout(Duration watchdogTimeout) {
            this.watchdogTimeout = watchdogTimeout;
        }
    }
}
