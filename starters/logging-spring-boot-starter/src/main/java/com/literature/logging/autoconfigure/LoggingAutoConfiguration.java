package com.literature.logging.autoconfigure;

import com.literature.logging.appender.KafkaLogAppender;
import com.literature.logging.consumer.LogToElasticsearchConsumer;
import com.literature.logging.filter.TraceIdMdcFilter;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@AutoConfiguration
@EnableKafka
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "literature.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAutoConfiguration {

  @Bean
  public TraceIdMdcFilter traceIdMdcFilter(LoggingProperties properties) {
    return new TraceIdMdcFilter(properties);
  }

  @Bean
  @ConditionalOnProperty(prefix = "literature.logging.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
  public ProducerFactory<String, String> logProducerFactory(LoggingProperties properties) {
    Map<String, Object> config = new HashMap<>();
    LoggingProperties.KafkaProperties kafka = properties.getKafka();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    LoggingProperties.KafkaProperties.SecurityProperties security = kafka.getSecurity();
    if (security.getUsername() != null && security.getPassword() != null) {
      config.put("security.protocol", security.getProtocol());
      config.put("sasl.mechanism", security.getSaslMechanism());
      String jaasConfig = String.format(
          "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
          security.getUsername(), security.getPassword());
      config.put("sasl.jaas.config", jaasConfig);
    }
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  @ConditionalOnProperty(prefix = "literature.logging.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
  public KafkaTemplate<String, String> logKafkaTemplate(ProducerFactory<String, String> logProducerFactory) {
    return new KafkaTemplate<>(logProducerFactory);
  }

  @Bean
  @ConditionalOnProperty(prefix = "literature.logging.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
  public KafkaLogAppender kafkaLogAppender(LoggingProperties properties, KafkaTemplate<String, String> logKafkaTemplate) {
    KafkaLogAppender appender = new KafkaLogAppender();
    appender.setTopic(properties.getKafka().getTopic());
    appender.setKafkaTemplate(logKafkaTemplate);
    appender.setLogPattern(properties.getPattern());
    return appender;
  }

  @Bean
  @ConditionalOnProperty(prefix = "literature.logging.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
  public ElasticsearchClient elasticsearchClient(LoggingProperties properties) {
    LoggingProperties.ElasticsearchProperties es = properties.getElasticsearch();
    HttpHost[] hosts = es.getHosts().stream().map(HttpHost::create).toArray(HttpHost[]::new);
    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    if (es.getUsername() != null && es.getPassword() != null) {
      credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(es.getUsername(), es.getPassword()));
    }

    RestClient restClient = RestClient.builder(hosts)
        .setHttpClientConfigCallback((HttpAsyncClientBuilder httpClientBuilder) ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
        .build();

    ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    return new ElasticsearchClient(transport);
  }

  @Bean
  @ConditionalOnProperty(prefix = "literature.logging.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
  public LogToElasticsearchConsumer logToElasticsearchConsumer(ElasticsearchClient client, LoggingProperties properties) {
    return new LogToElasticsearchConsumer(client, properties);
  }
}
