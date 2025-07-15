package com.sm.patientservice.exception;

public class BillingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BillingException(String message) {
        super(message);
    }

    public BillingException(String message, Throwable cause) {
        super(message, cause);
    }
    public BillingException(Throwable cause) {
        super(cause);
    }
    
}
