package com.sm.analyticsservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.google.protobuf.InvalidProtocolBufferException;

import lombok.extern.slf4j.Slf4j;
import patient.events.PatientEvent;

@Service
@Slf4j
public class KafkaConsumer {

    // This class will handle the consumption of messages from Kafka topics.
    // You can implement methods to listen to specific topics and process the messages accordingly.

    @KafkaListener(topics = "patients", groupId = "analytics-group")
    public void consumeEvent(byte[] event) {
        
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            log.info("Received PatientEvent: {}", patientEvent);
            // Here you can add logic to process the patientEvent, such as saving it to a database or triggering other actions.

        } catch (InvalidProtocolBufferException e) {
            log.error("Error parsing PatientEvent from Kafka message: {}", e.getMessage());
        }

    }
    
}
