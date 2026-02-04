package com.literature.logging.autoconfigure;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "literature.logging")
public class LoggingProperties {
  private boolean enabled = true;
  private TraceProperties trace = new TraceProperties();
  private KafkaProperties kafka = new KafkaProperties();
  private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();
  private SkywalkingProperties skywalking = new SkywalkingProperties();
  private String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{traceId}] [%thread] %-5level %logger{36} - %msg%n";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public TraceProperties getTrace() {
    return trace;
  }

  public void setTrace(TraceProperties trace) {
    this.trace = trace;
  }

  public KafkaProperties getKafka() {
    return kafka;
  }

  public void setKafka(KafkaProperties kafka) {
    this.kafka = kafka;
  }

  public ElasticsearchProperties getElasticsearch() {
    return elasticsearch;
  }

  public void setElasticsearch(ElasticsearchProperties elasticsearch) {
    this.elasticsearch = elasticsearch;
  }

  public SkywalkingProperties getSkywalking() {
    return skywalking;
  }

  public void setSkywalking(SkywalkingProperties skywalking) {
    this.skywalking = skywalking;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public static class TraceProperties {
    private String headerName = "X-Trace-Id";
    private String mdcKey = "traceId";
    private boolean autoGenerate = true;

    public String getHeaderName() {
      return headerName;
    }

    public void setHeaderName(String headerName) {
      this.headerName = headerName;
    }

    public String getMdcKey() {
      return mdcKey;
    }

    public void setMdcKey(String mdcKey) {
      this.mdcKey = mdcKey;
    }

    public boolean isAutoGenerate() {
      return autoGenerate;
    }

    public void setAutoGenerate(boolean autoGenerate) {
      this.autoGenerate = autoGenerate;
    }
  }

  public static class KafkaProperties {
    private boolean enabled = true;
    private String bootstrapServers = "localhost:9092";
    private String topic = "literature-logs";
    private SecurityProperties security = new SecurityProperties();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getBootstrapServers() {
      return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
      this.bootstrapServers = bootstrapServers;
    }

    public String getTopic() {
      return topic;
    }

    public void setTopic(String topic) {
      this.topic = topic;
    }

    public SecurityProperties getSecurity() {
      return security;
    }

    public void setSecurity(SecurityProperties security) {
      this.security = security;
    }

    public static class SecurityProperties {
      private String protocol = "SASL_PLAINTEXT";
      private String saslMechanism = "PLAIN";
      private String username;
      private String password;

      public String getProtocol() {
        return protocol;
      }

      public void setProtocol(String protocol) {
        this.protocol = protocol;
      }

      public String getSaslMechanism() {
        return saslMechanism;
      }

      public void setSaslMechanism(String saslMechanism) {
        this.saslMechanism = saslMechanism;
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
    }
  }

  public static class ElasticsearchProperties {
    private boolean enabled = true;
    private List<String> hosts = new ArrayList<>(List.of("http://localhost:9200"));
    private String indexPattern = "literature-logs-%{yyyy.MM.dd.HH}";
    private String username = "elastic";
    private String password;
    private BatchProperties batch = new BatchProperties();

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public List<String> getHosts() {
      return hosts;
    }

    public void setHosts(List<String> hosts) {
      this.hosts = hosts;
    }

    public String getIndexPattern() {
      return indexPattern;
    }

    public void setIndexPattern(String indexPattern) {
      this.indexPattern = indexPattern;
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

    public BatchProperties getBatch() {
      return batch;
    }

    public void setBatch(BatchProperties batch) {
      this.batch = batch;
    }

    public static class BatchProperties {
      private int size = 500;
      private Duration flushInterval = Duration.ofSeconds(3);

      public int getSize() {
        return size;
      }

      public void setSize(int size) {
        this.size = size;
      }

      public Duration getFlushInterval() {
        return flushInterval;
      }

      public void setFlushInterval(Duration flushInterval) {
        this.flushInterval = flushInterval;
      }
    }
  }

  public static class SkywalkingProperties {
    private boolean enabled = true;
    private String serviceName;
    private String backendService = "skywalking-oap:11800";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getServiceName() {
      return serviceName;
    }

    public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
    }

    public String getBackendService() {
      return backendService;
    }

    public void setBackendService(String backendService) {
      this.backendService = backendService;
    }
  }
}
