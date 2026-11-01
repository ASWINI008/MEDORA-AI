package com.medora.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class EventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);

    @Autowired
    private Optional<KafkaEventProducer> kafkaEventProducer;

    @Async
    public void publishEvent(String topic, Map<String, Object> payload) {
        String timestamp = LocalDateTime.now().toString();
        
        if (kafkaEventProducer.isPresent()) {
            kafkaEventProducer.get().sendEvent(topic, payload);
            logger.info("[KAFKA EVENT] Topic: '{}' | Timestamp: {} | Payload: {}", topic, timestamp, payload);
        } else {
            // Fallback for monolith without Kafka
            logger.info("[LOCAL EVENT] Topic: '{}' | Timestamp: {} | Payload: {}", topic, timestamp, payload);
        }
    }
}
