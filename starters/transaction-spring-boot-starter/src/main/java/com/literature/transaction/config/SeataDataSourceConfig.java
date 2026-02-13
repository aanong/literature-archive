package com.literature.transaction.config;

import io.seata.rm.datasource.DataSourceProxy;
import io.seata.rm.datasource.xa.DataSourceProxyXA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Seata 数据源代理配置
 * <p>
 * 根据配置的代理模式（AT/XA）自动代理数据源
 */
@Configuration
@ConditionalOnClass({DataSourceProxy.class, DataSource.class})
@ConditionalOnProperty(prefix = "literature.transaction.seata", name = "enable-auto-data-source-proxy", havingValue = "true", matchIfMissing = true)
public class SeataDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(SeataDataSourceConfig.class);

    /**
     * AT 模式数据源代理
     */
    @Bean
    @Primary
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(DataSourceProxy.class)
    @ConditionalOnProperty(prefix = "literature.transaction.seata", name = "data-source-proxy-mode", havingValue = "AT", matchIfMissing = true)
    public DataSourceProxy dataSourceProxy(DataSource dataSource) {
        log.info("Configuring Seata AT mode DataSourceProxy");
        return new DataSourceProxy(dataSource);
    }

    /**
     * XA 模式数据源代理
     */
    @Bean
    @Primary
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean(DataSourceProxyXA.class)
    @ConditionalOnProperty(prefix = "literature.transaction.seata", name = "data-source-proxy-mode", havingValue = "XA")
    public DataSourceProxyXA dataSourceProxyXA(DataSource dataSource) {
        log.info("Configuring Seata XA mode DataSourceProxyXA");
        return new DataSourceProxyXA(dataSource);
    }
}
