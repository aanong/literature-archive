package com.literature.sharding.autoconfigure;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "literature.sharding")
public class ShardingProperties {
  private boolean enabled = true;
  private Map<String, DataSourceProperties> datasources = new LinkedHashMap<>();
  private ShardingRuleProperties rules = new ShardingRuleProperties();
  private ReadWriteSplittingProperties readWriteSplitting = new ReadWriteSplittingProperties();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, DataSourceProperties> getDatasources() {
    return datasources;
  }

  public void setDatasources(Map<String, DataSourceProperties> datasources) {
    this.datasources = datasources;
  }

  public ShardingRuleProperties getRules() {
    return rules;
  }

  public void setRules(ShardingRuleProperties rules) {
    this.rules = rules;
  }

  public ReadWriteSplittingProperties getReadWriteSplitting() {
    return readWriteSplitting;
  }

  public void setReadWriteSplitting(ReadWriteSplittingProperties readWriteSplitting) {
    this.readWriteSplitting = readWriteSplitting;
  }

  public static class DataSourceProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName = "com.mysql.cj.jdbc.Driver";

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getDriverClassName() {
      return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
      this.driverClassName = driverClassName;
    }
  }

  public static class ShardingRuleProperties {
    private Map<String, TableRuleProperties> tables = new LinkedHashMap<>();
    private String[] bindingTables = new String[0];
    private String[] broadcastTables = new String[0];
    private Map<String, AlgorithmProperties> shardingAlgorithms = new LinkedHashMap<>();
    private Map<String, KeyGeneratorProperties> keyGenerators = new LinkedHashMap<>();

    public Map<String, TableRuleProperties> getTables() {
      return tables;
    }

    public void setTables(Map<String, TableRuleProperties> tables) {
      this.tables = tables;
    }

    public String[] getBindingTables() {
      return bindingTables;
    }

    public void setBindingTables(String[] bindingTables) {
      this.bindingTables = bindingTables;
    }

    public String[] getBroadcastTables() {
      return broadcastTables;
    }

    public void setBroadcastTables(String[] broadcastTables) {
      this.broadcastTables = broadcastTables;
    }

    public Map<String, AlgorithmProperties> getShardingAlgorithms() {
      return shardingAlgorithms;
    }

    public void setShardingAlgorithms(Map<String, AlgorithmProperties> shardingAlgorithms) {
      this.shardingAlgorithms = shardingAlgorithms;
    }

    public Map<String, KeyGeneratorProperties> getKeyGenerators() {
      return keyGenerators;
    }

    public void setKeyGenerators(Map<String, KeyGeneratorProperties> keyGenerators) {
      this.keyGenerators = keyGenerators;
    }
  }

  public static class TableRuleProperties {
    private String actualDataNodes;
    private TableStrategyProperties tableStrategy = new TableStrategyProperties();
    private KeyGenerateStrategyProperties keyGenerateStrategy = new KeyGenerateStrategyProperties();

    public String getActualDataNodes() {
      return actualDataNodes;
    }

    public void setActualDataNodes(String actualDataNodes) {
      this.actualDataNodes = actualDataNodes;
    }

    public TableStrategyProperties getTableStrategy() {
      return tableStrategy;
    }

    public void setTableStrategy(TableStrategyProperties tableStrategy) {
      this.tableStrategy = tableStrategy;
    }

    public KeyGenerateStrategyProperties getKeyGenerateStrategy() {
      return keyGenerateStrategy;
    }

    public void setKeyGenerateStrategy(KeyGenerateStrategyProperties keyGenerateStrategy) {
      this.keyGenerateStrategy = keyGenerateStrategy;
    }
  }

  public static class TableStrategyProperties {
    private StandardStrategyProperties standard = new StandardStrategyProperties();

    public StandardStrategyProperties getStandard() {
      return standard;
    }

    public void setStandard(StandardStrategyProperties standard) {
      this.standard = standard;
    }
  }

  public static class StandardStrategyProperties {
    private String shardingColumn;
    private String shardingAlgorithmName;

    public String getShardingColumn() {
      return shardingColumn;
    }

    public void setShardingColumn(String shardingColumn) {
      this.shardingColumn = shardingColumn;
    }

    public String getShardingAlgorithmName() {
      return shardingAlgorithmName;
    }

    public void setShardingAlgorithmName(String shardingAlgorithmName) {
      this.shardingAlgorithmName = shardingAlgorithmName;
    }
  }

  public static class KeyGenerateStrategyProperties {
    private String column;
    private String keyGeneratorName;

    public String getColumn() {
      return column;
    }

    public void setColumn(String column) {
      this.column = column;
    }

    public String getKeyGeneratorName() {
      return keyGeneratorName;
    }

    public void setKeyGeneratorName(String keyGeneratorName) {
      this.keyGeneratorName = keyGeneratorName;
    }
  }

  public static class AlgorithmProperties {
    private String type;
    private Map<String, String> props = new LinkedHashMap<>();

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public Map<String, String> getProps() {
      return props;
    }

    public void setProps(Map<String, String> props) {
      this.props = props;
    }
  }

  public static class KeyGeneratorProperties {
    private String type;
    private Map<String, String> props = new LinkedHashMap<>();

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public Map<String, String> getProps() {
      return props;
    }

    public void setProps(Map<String, String> props) {
      this.props = props;
    }
  }

  public static class ReadWriteSplittingProperties {
    private boolean enabled;
    private String primary;
    private String[] replicas = new String[0];
    private Duration loadBalanceTimeout = Duration.ofSeconds(3);

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getPrimary() {
      return primary;
    }

    public void setPrimary(String primary) {
      this.primary = primary;
    }

    public String[] getReplicas() {
      return replicas;
    }

    public void setReplicas(String[] replicas) {
      this.replicas = replicas;
    }

    public Duration getLoadBalanceTimeout() {
      return loadBalanceTimeout;
    }

    public void setLoadBalanceTimeout(Duration loadBalanceTimeout) {
      this.loadBalanceTimeout = loadBalanceTimeout;
    }
  }
}
