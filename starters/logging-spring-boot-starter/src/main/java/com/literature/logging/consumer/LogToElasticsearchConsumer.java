package com.literature.logging.consumer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.literature.logging.autoconfigure.LoggingProperties;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class LogToElasticsearchConsumer {
  private static final Logger logger = LoggerFactory.getLogger(LogToElasticsearchConsumer.class);

  private final ElasticsearchClient esClient;
  private final LoggingProperties properties;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>();
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH");
  private final Object lock = new Object();
  private volatile boolean running = true;

  public LogToElasticsearchConsumer(ElasticsearchClient esClient, LoggingProperties properties) {
    this.esClient = esClient;
    this.properties = properties;
  }

  @KafkaListener(topics = "#{loggingProperties.kafka.topic}")
  public void consumeLog(String logMessage) {
    try {
      Map<String, Object> payload = objectMapper.readValue(logMessage, Map.class);
      queue.offer(payload);
    } catch (Exception ex) {
      logger.warn("Failed to parse log message", ex);
    }
  }

  @PostConstruct
  public void startBatchWorker() {
    Thread worker = new Thread(this::flushLoop, "logging-es-batch");
    worker.setDaemon(true);
    worker.start();
  }

  @PreDestroy
  public void stop() {
    running = false;
  }

  private void flushLoop() {
    LoggingProperties.ElasticsearchProperties.BatchProperties batch = properties.getElasticsearch().getBatch();
    while (running) {
      try {
        List<Map<String, Object>> drained = drainQueue(batch.getSize(), batch.getFlushInterval());
        if (!drained.isEmpty()) {
          sendBatch(drained);
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private List<Map<String, Object>> drainQueue(int batchSize, java.time.Duration flushInterval) throws InterruptedException {
    List<Map<String, Object>> drained = new ArrayList<>();
    Map<String, Object> first = queue.poll(flushInterval.toMillis(), TimeUnit.MILLISECONDS);
    if (first != null) {
      drained.add(first);
      queue.drainTo(drained, batchSize - 1);
    }
    return drained;
  }

  private void sendBatch(List<Map<String, Object>> payloads) {
    String indexName = generateIndexName();
    List<BulkOperation> operations = new ArrayList<>();
    for (Map<String, Object> payload : payloads) {
      operations.add(BulkOperation.of(op -> op
          .index(idx -> idx
              .index(indexName)
              .document(payload))));
    }
    if (operations.isEmpty()) {
      return;
    }
    try {
      BulkRequest request = BulkRequest.of(builder -> builder.operations(operations));
      esClient.bulk(request);
    } catch (Exception ex) {
      logger.error("Failed to send log batch to Elasticsearch", ex);
    }
  }

  private String generateIndexName() {
    String pattern = properties.getElasticsearch().getIndexPattern();
    String timestamp = LocalDateTime.now(ZoneId.systemDefault()).format(formatter);
    if (pattern.contains("%{yyyy.MM.dd.HH}")) {
      return pattern.replace("%{yyyy.MM.dd.HH}", timestamp);
    }
    return pattern + "-" + timestamp;
  }
}
