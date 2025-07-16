package com.sm.patientservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC = "patients";

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Sends an event to the Kafka topic.
     *
     * @param key   The key for the Kafka message.
     * @param event The event data to be sent, as a byte array.
     */
    public void sendEvent(String key, byte[] event) {
        kafkaTemplate.send(TOPIC, key, event);
        log.info("Sent event to Kafka topic {}: key={}, event={}", TOPIC, key, event);
    }

}
