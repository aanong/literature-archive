package com.literature.transaction.autoconfigure;

import com.literature.transaction.config.SeataDataSourceConfig;
import com.literature.transaction.handler.GlobalTransactionExceptionHandler;
import com.literature.transaction.interceptor.SeataFeignInterceptor;
import feign.RequestInterceptor;
import io.seata.spring.annotation.GlobalTransactionScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 分布式事务自动配置
 */
@AutoConfiguration
@ConditionalOnClass(GlobalTransactionScanner.class)
@ConditionalOnProperty(prefix = "literature.transaction", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TransactionProperties.class)
@Import(SeataDataSourceConfig.class)
public class TransactionAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TransactionAutoConfiguration.class);

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    /**
     * 配置全局事务扫描器
     */
    @Bean
    @ConditionalOnMissingBean(GlobalTransactionScanner.class)
    public GlobalTransactionScanner globalTransactionScanner(TransactionProperties properties) {
        String appId = properties.getSeata().getApplicationId();
        if (appId == null || appId.isEmpty()) {
            appId = applicationName;
        }
        String txGroup = properties.getSeata().getTxServiceGroup();
        
        log.info("Configuring GlobalTransactionScanner, appId: {}, txGroup: {}", appId, txGroup);
        return new GlobalTransactionScanner(appId, txGroup);
    }

    /**
     * 配置 Feign XID 传播拦截器
     */
    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    @ConditionalOnProperty(prefix = "literature.transaction.seata", name = "enable-feign-xid-propagation", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(SeataFeignInterceptor.class)
    public SeataFeignInterceptor seataFeignInterceptor() {
        log.info("Configuring SeataFeignInterceptor for XID propagation");
        return new SeataFeignInterceptor();
    }

    /**
     * 配置全局事务异常处理器
     */
    @Bean
    @ConditionalOnMissingBean(GlobalTransactionExceptionHandler.class)
    public GlobalTransactionExceptionHandler globalTransactionExceptionHandler() {
        log.info("Configuring GlobalTransactionExceptionHandler");
        return new GlobalTransactionExceptionHandler();
    }
}
