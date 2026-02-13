package com.literature.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * <p>
 * 使用示例：
 * <pre>
 * {@code @DistributedLock(key = "'order:' + #orderId", waitTime = 5, leaseTime = 30)}
 * public void processOrder(String orderId) {
 *     // 业务逻辑
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 锁的 key，支持 SpEL 表达式
     * <p>
     * 例如：
     * <ul>
     *   <li>"order:create" - 固定 key</li>
     *   <li>"'order:' + #orderId" - 动态 key</li>
     *   <li>"'user:' + #user.id" - 对象属性</li>
     * </ul>
     */
    String key();

    /**
     * 锁类型
     */
    LockType type() default LockType.REENTRANT;

    /**
     * 等待获取锁的最大时间
     * <p>
     * 设为 0 表示不等待，立即返回
     * 设为 -1 表示一直等待直到获取锁
     */
    long waitTime() default 10;

    /**
     * 锁的租约时间（自动释放时间）
     * <p>
     * 设为 -1 表示启用看门狗自动续期
     */
    long leaseTime() default 30;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取锁失败时的处理策略
     */
    FailureStrategy onFailure() default FailureStrategy.THROW_EXCEPTION;

    /**
     * 失败策略枚举
     */
    enum FailureStrategy {
        /**
         * 抛出异常
         */
        THROW_EXCEPTION,

        /**
         * 快速失败（返回 null 或默认值）
         */
        FAIL_FAST,

        /**
         * 继续执行（不加锁）
         */
        CONTINUE
    }
}
