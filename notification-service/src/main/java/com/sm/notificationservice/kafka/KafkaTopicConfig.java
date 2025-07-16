package com.sm.notificationservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    @Bean
    public NewTopic patietEventTopic() {
        return TopicBuilder.name("patients")
                .partitions(3) // Number of partitions
                .replicas(1) // Number of replicas
                .build();
    }

}
