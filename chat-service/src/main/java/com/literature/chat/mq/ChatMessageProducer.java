package com.literature.chat.mq;

import com.literature.chat.dto.ChatMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatMessageProducer {

    @Autowired
    private KafkaTemplate<String, ChatMessageDTO> kafkaTemplate;

    public void sendToServer(String targetServer, ChatMessageDTO message) {
        // Topic: chat-messages
        // Key: targetServer (ensures messages for the same server go to the same
        // partition if configured)
        // Or simply use targetServer as a property to filter on consumer side if
        // needed,
        // but typically we broadcast or use specific topics.
        // For simplicity in this phase, we might broadcast or use a topic per server?
        // Better: Use a single topic and filter? No, that's inefficient.
        // Standard approach:
        // Option A: Topic per server (dynamic topics, can be messy).
        // Option B: Single topic, consumer group per server? No, all servers need to
        // hear?
        // Wait, if users are on specific servers, we want to send ONLY to that server.
        // Kafka partitioning by 'targetServer' means consumers must be assigned those
        // partitions.
        // This effectively implies static scaling or rebalancing.
        // A simpler approach for dynamic scaling is "Broadcast" to all servers, and
        // servers discard if user not local.
        // OR: specific topic name like "chat-server-{ip}-{port}".

        // Let's go with the Plan: "sendToServer".
        // Broadcast to all servers, let them filter by local session
        // In a real production system with massive scale, we would use specific topics
        // or partitions.
        kafkaTemplate.send("chat-messages", message);
        log.info("Published message to chat-messages: {}", message);
    }
}
