package com.literature.sharding.autoconfigure;

import com.literature.sharding.config.ShardingRuleConfig;
import javax.sql.DataSource;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@EnableConfigurationProperties(ShardingProperties.class)
@ConditionalOnProperty(prefix = "literature.sharding", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(ShardingSphereDataSource.class)
public class ShardingAutoConfiguration {

  @Bean
  @Primary
  public DataSource shardingDataSource(ShardingProperties properties) throws Exception {
    return new ShardingRuleConfig().buildDataSource(properties);
  }
}
