package com.sm.billing_service.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingGrpcServiceTest {

    private BillingGrpcService billingGrpcService;
    private StreamObserver<BillingResponse> responseObserver;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        billingGrpcService = new BillingGrpcService();
        responseObserver = Mockito.mock(StreamObserver.class);
    }

    @Test
    void testCreateBillingAccount() {
        BillingRequest request = BillingRequest.newBuilder()
                .setName("John Doe")
                .setEmail("john.doe@gmail.com")
                .setAddress("123 Main St, Springfield, USA")
                .setPhone("+1234567890")
                .build();

        billingGrpcService.createBillingAccount(request, responseObserver);

        ArgumentCaptor<BillingResponse> responseCaptor = ArgumentCaptor.forClass(BillingResponse.class);
        verify(responseObserver, times(1)).onNext(responseCaptor.capture());
        verify(responseObserver, times(1)).onCompleted();

        BillingResponse response = responseCaptor.getValue();
        assertEquals("12345", response.getAccountId());
        assertEquals("Active", response.getStatus());
        assertEquals("Billing account created successfully", response.getMessage());
    }
} 