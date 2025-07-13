package com.sm.patientservice.exception;

public class EmailAlreadyExistException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyExistException(String email) {
        super("Email already exists: " + email);
    }

    public EmailAlreadyExistException(String email, Throwable cause) {
        super("Email already exists: " + email, cause);
    }
    
}
