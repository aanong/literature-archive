package com.literature.sharding.config;

import com.literature.sharding.autoconfigure.ShardingProperties;
import com.literature.sharding.autoconfigure.ShardingProperties.AlgorithmProperties;
import com.literature.sharding.autoconfigure.ShardingProperties.DataSourceProperties;
import com.literature.sharding.autoconfigure.ShardingProperties.KeyGeneratorProperties;
import com.literature.sharding.autoconfigure.ShardingProperties.TableRuleProperties;
import com.zaxxer.hikari.HikariDataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;

public class ShardingRuleConfig {
  public DataSource buildDataSource(ShardingProperties properties) throws Exception {
    Map<String, DataSource> dataSources = createDataSources(properties.getDatasources());
    ShardingRuleConfiguration shardingRuleConfiguration = createShardingRuleConfiguration(properties);
    ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration = createReadWriteSplittingConfiguration(properties);

    List<RuleConfiguration> ruleConfigurations = new ArrayList<>();
    ruleConfigurations.add(shardingRuleConfiguration);
    if (readwriteSplittingRuleConfiguration != null) {
      ruleConfigurations.add(readwriteSplittingRuleConfiguration);
    }

    Properties runtimeProps = new Properties();
    runtimeProps.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.toString(true));

    return ShardingSphereDataSourceFactory.createDataSource(dataSources, ruleConfigurations, runtimeProps);
  }

  private Map<String, DataSource> createDataSources(Map<String, DataSourceProperties> datasourceProperties) {
    Map<String, DataSource> dataSources = new LinkedHashMap<>();
    for (Map.Entry<String, DataSourceProperties> entry : datasourceProperties.entrySet()) {
      HikariDataSource dataSource = new HikariDataSource();
      DataSourceProperties properties = entry.getValue();
      dataSource.setJdbcUrl(properties.getUrl());
      dataSource.setUsername(properties.getUsername());
      dataSource.setPassword(properties.getPassword());
      dataSource.setDriverClassName(properties.getDriverClassName());
      dataSources.put(entry.getKey(), dataSource);
    }
    return dataSources;
  }

  private ShardingRuleConfiguration createShardingRuleConfiguration(ShardingProperties properties) {
    ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();

    Map<String, TableRuleProperties> tables = properties.getRules().getTables();
    for (Map.Entry<String, TableRuleProperties> entry : tables.entrySet()) {
      ShardingTableRuleConfiguration tableRule = new ShardingTableRuleConfiguration(entry.getKey(),
          entry.getValue().getActualDataNodes());
      if (entry.getValue().getTableStrategy() != null) {
        String shardingColumn = entry.getValue().getTableStrategy().getStandard().getShardingColumn();
        String algorithmName = entry.getValue().getTableStrategy().getStandard().getShardingAlgorithmName();
        if (shardingColumn != null && algorithmName != null) {
          tableRule.setTableShardingStrategy(new StandardShardingStrategyConfiguration(shardingColumn, algorithmName));
        }
      }
      if (entry.getValue().getKeyGenerateStrategy() != null) {
        String column = entry.getValue().getKeyGenerateStrategy().getColumn();
        String keyGeneratorName = entry.getValue().getKeyGenerateStrategy().getKeyGeneratorName();
        if (column != null && keyGeneratorName != null) {
          tableRule.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration(column, keyGeneratorName));
        }
      }
      shardingRuleConfiguration.getTables().add(tableRule);
    }

    for (String bindingGroup : properties.getRules().getBindingTables()) {
      shardingRuleConfiguration.getBindingTableGroups().add(bindingGroup);
    }

    for (String table : properties.getRules().getBroadcastTables()) {
      shardingRuleConfiguration.getBroadcastTables().add(table);
    }

    for (Map.Entry<String, AlgorithmProperties> entry : properties.getRules().getShardingAlgorithms().entrySet()) {
      Properties algoProps = new Properties();
      algoProps.putAll(entry.getValue().getProps());
      shardingRuleConfiguration.getShardingAlgorithms().put(entry.getKey(),
          new AlgorithmConfiguration(entry.getValue().getType(), algoProps));
    }

    for (Map.Entry<String, KeyGeneratorProperties> entry : properties.getRules().getKeyGenerators().entrySet()) {
      Properties keyProps = new Properties();
      keyProps.putAll(entry.getValue().getProps());
      shardingRuleConfiguration.getKeyGenerators().put(entry.getKey(),
          new AlgorithmConfiguration(entry.getValue().getType(), keyProps));
    }

    return shardingRuleConfiguration;
  }

  private ReadwriteSplittingRuleConfiguration createReadWriteSplittingConfiguration(ShardingProperties properties) {
    ShardingProperties.ReadWriteSplittingProperties rwProperties = properties.getReadWriteSplitting();
    if (!rwProperties.isEnabled()) {
      return null;
    }

    String writeDataSourceName = rwProperties.getPrimary();
    String[] readDataSourceNames = rwProperties.getReplicas();

    StaticReadwriteSplittingStrategyConfiguration strategy =
        new StaticReadwriteSplittingStrategyConfiguration(writeDataSourceName, List.of(readDataSourceNames));
    ReadwriteSplittingDataSourceRuleConfiguration dataSourceRule =
        new ReadwriteSplittingDataSourceRuleConfiguration("rw", strategy, null, "ROUND_ROBIN");
    return new ReadwriteSplittingRuleConfiguration(List.of(dataSourceRule), new HashMap<>());
  }
}
