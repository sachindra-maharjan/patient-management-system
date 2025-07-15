package com.sm.patientservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class BillingServiceConfig {

    @Value("${grpc.billing.service.host}") 
    private String billingServiceHost;

    @Value("${grpc.billing.service.port}")
    private int billingServicePort;
    
}
