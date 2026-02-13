package com.literature.lock.aspect;

import com.literature.lock.annotation.DistributedLock;
import com.literature.lock.core.LockExecutor;
import com.literature.lock.core.LockExecutor.LockInfo;
import com.literature.lock.exception.LockAcquireException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * 分布式锁 AOP 切面
 * <p>
 * 处理 @DistributedLock 注解，自动完成锁的获取和释放
 */
@Aspect
public class DistributedLockAspect {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockAspect.class);

    private final LockExecutor lockExecutor;
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public DistributedLockAspect(LockExecutor lockExecutor) {
        this.lockExecutor = lockExecutor;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        // 解析锁的 key（支持 SpEL）
        String lockKey = parseLockKey(joinPoint, distributedLock);
        
        log.debug("Attempting to acquire lock: {}, type: {}", lockKey, distributedLock.type());

        LockInfo lockInfo = null;
        try {
            // 尝试获取锁
            lockInfo = lockExecutor.tryLock(
                lockKey,
                distributedLock.type(),
                distributedLock.waitTime(),
                distributedLock.leaseTime(),
                distributedLock.timeUnit()
            );

            // 处理获取锁失败的情况
            if (lockInfo == null) {
                return handleLockFailure(joinPoint, distributedLock, lockKey);
            }

            log.debug("Lock acquired successfully: {}", lockKey);
            
            // 执行目标方法
            return joinPoint.proceed();
            
        } finally {
            // 释放锁
            if (lockInfo != null) {
                lockExecutor.unlock(lockInfo);
                log.debug("Lock released: {}", lockKey);
            }
        }
    }

    /**
     * 解析锁的 key，支持 SpEL 表达式
     */
    private String parseLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        String keyExpression = distributedLock.key();
        
        // 如果不是 SpEL 表达式，直接返回
        if (!keyExpression.contains("#") && !keyExpression.contains("'")) {
            return keyExpression;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();

        EvaluationContext context = new MethodBasedEvaluationContext(
            target,
            method,
            args,
            parameterNameDiscoverer
        );

        try {
            Object result = expressionParser.parseExpression(keyExpression).getValue(context);
            return result != null ? result.toString() : keyExpression;
        } catch (Exception e) {
            log.warn("Failed to parse SpEL expression: {}, using raw key", keyExpression, e);
            return keyExpression;
        }
    }

    /**
     * 处理获取锁失败的情况
     */
    private Object handleLockFailure(ProceedingJoinPoint joinPoint, DistributedLock distributedLock, 
                                      String lockKey) throws Throwable {
        log.warn("Failed to acquire lock: {}, strategy: {}", lockKey, distributedLock.onFailure());

        return switch (distributedLock.onFailure()) {
            case THROW_EXCEPTION -> throw new LockAcquireException(lockKey);
            case FAIL_FAST -> getDefaultReturnValue(joinPoint);
            case CONTINUE -> {
                log.warn("Continuing without lock: {}", lockKey);
                yield joinPoint.proceed();
            }
        };
    }

    /**
     * 获取方法的默认返回值
     */
    private Object getDefaultReturnValue(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();

        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) return false;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == double.class) return 0.0;
            if (returnType == float.class) return 0.0f;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == short.class) return (short) 0;
            if (returnType == char.class) return '\0';
        }
        
        return null;
    }
}
