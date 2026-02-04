package com.literature.logging.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaLogAppender extends AppenderBase<ILoggingEvent> {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private KafkaTemplate<String, String> kafkaTemplate;
  private String topic;
  private String logPattern;

  public void setKafkaTemplate(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public void setLogPattern(String logPattern) {
    this.logPattern = logPattern;
  }

  @Override
  protected void append(ILoggingEvent event) {
    if (kafkaTemplate == null || topic == null) {
      return;
    }
    try {
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("timestamp", Instant.ofEpochMilli(event.getTimeStamp()).toString());
      payload.put("level", event.getLevel().levelStr);
      payload.put("logger", event.getLoggerName());
      payload.put("thread", event.getThreadName());
      payload.put("message", event.getFormattedMessage());
      payload.put("mdc", event.getMDCPropertyMap());
      String json = objectMapper.writeValueAsString(payload);
      kafkaTemplate.send(topic, event.getLoggerName(), json);
    } catch (Exception ex) {
      addError("Failed to send log event to Kafka", ex);
    }
  }
}
