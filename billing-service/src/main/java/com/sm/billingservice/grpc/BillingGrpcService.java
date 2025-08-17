package com.sm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Slf4j
public class BillingGrpcService extends BillingServiceImplBase{

    @Override
    public void createBillingAccount(BillingRequest request, StreamObserver<BillingResponse> responseObserver) {
        // Here you would implement the logic to create a billing account
        // For now, we will just return a dummy response

        log.info("Received billing request: {}", request.toString());

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("SUCCESS")
                .setMessage("Billing account created successfully")
                .build();
        
        // Send the response back to the client
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        log.info("Billing account created successfully for patient with ID: {}", request.getId());
    }
    
}
