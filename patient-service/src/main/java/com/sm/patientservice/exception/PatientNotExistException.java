package com.sm.patientservice.exception;

public class PatientNotExistException extends RuntimeException{
    public PatientNotExistException(String message) {
        super(message);
    }
    public PatientNotExistException(String message, Throwable cause) {
        super(message, cause);
    }
    public PatientNotExistException(Throwable cause) {
        super(cause);
    }
}
