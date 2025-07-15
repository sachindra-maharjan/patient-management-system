package com.sm.patientservice.grpc;

import org.springframework.stereotype.Service;

import com.sm.patientservice.config.BillingServiceConfig;
import com.sm.patientservice.model.Patient;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import billing.BillingServiceGrpc.BillingServiceBlockingStub;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BillingServiceGrpcClient {

    private final BillingServiceBlockingStub billingServiceStub;
 
    public BillingServiceGrpcClient(BillingServiceConfig billingConfig) {
        log.info("BillingServiceGrpcClient initialized with host: {} and port: {}", 
            billingConfig.getBillingServiceHost(), billingConfig.getBillingServicePort());
        
        // Create a blocking stub for the BillingService
        this.billingServiceStub = BillingServiceGrpc.newBlockingStub(
            io.grpc.ManagedChannelBuilder.forAddress(billingConfig.getBillingServiceHost(), billingConfig.getBillingServicePort())
                .usePlaintext()
                .build()
        );
        
    }

    public BillingResponse createBillingAccount(Patient patient) {
        var billingRequest  = BillingRequest.newBuilder()
            .setName(patient.getFirstName() + " " + patient.getLastName())
            .setEmail(patient.getEmail())
            .setPhone(patient.getPhoneNumber())
            .setAddress(patient.getAddress().toString())
            .setId(patient.getId().toString())
            .build();

        log.info("Sending billing request for patient: {}", patient.getId());
        BillingResponse billingResponse = billingServiceStub.createBillingAccount(billingRequest);
        log.info("Received billing response for patient: {}, status: {}", patient.getId(), billingResponse.getStatus());
        return billingResponse;
    }

}
