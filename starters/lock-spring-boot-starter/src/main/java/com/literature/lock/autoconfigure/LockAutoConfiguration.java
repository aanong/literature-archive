package com.literature.lock.autoconfigure;

import com.literature.lock.aspect.DistributedLockAspect;
import com.literature.lock.core.DistributedLockTemplate;
import com.literature.lock.core.LockExecutor;
import com.literature.lock.core.RedissonLockExecutor;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 分布式锁自动配置
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(prefix = "literature.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LockProperties.class)
public class LockAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LockAutoConfiguration.class);

    /**
     * 配置 Redisson 锁执行器
     */
    @Bean
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean(LockExecutor.class)
    public LockExecutor redissonLockExecutor(RedissonClient redissonClient, LockProperties lockProperties) {
        log.info("Configuring RedissonLockExecutor with prefix: {}", lockProperties.getRedis().getKeyPrefix());
        return new RedissonLockExecutor(redissonClient, lockProperties);
    }

    /**
     * 配置分布式锁模板
     */
    @Bean
    @ConditionalOnBean(LockExecutor.class)
    @ConditionalOnMissingBean(DistributedLockTemplate.class)
    public DistributedLockTemplate distributedLockTemplate(LockExecutor lockExecutor, LockProperties lockProperties) {
        log.info("Configuring DistributedLockTemplate");
        return new DistributedLockTemplate(lockExecutor, lockProperties);
    }

    /**
     * 配置分布式锁切面
     */
    @Bean
    @ConditionalOnBean(LockExecutor.class)
    @ConditionalOnMissingBean(DistributedLockAspect.class)
    public DistributedLockAspect distributedLockAspect(LockExecutor lockExecutor) {
        log.info("Configuring DistributedLockAspect");
        return new DistributedLockAspect(lockExecutor);
    }
}
