package com.sm.patientservice.exception;

import java.time.OffsetDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.sm.patientservice.model.dto.ValidationErrorResponse;
import com.sm.patientservice.model.dto.ValidationErrorResponseError;
import com.sm.patientservice.model.dto.ValidationErrorResponseErrorFieldErrorsInner;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles MethodArgumentNotValidException which is thrown when validation on an argument annotated with
     * @Valid fails.
     *
     * @param ex the MethodArgumentNotValidException
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        ValidationErrorResponseError error = new ValidationErrorResponseError();
        
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            ValidationErrorResponseErrorFieldErrorsInner fieldErrorDto = new ValidationErrorResponseErrorFieldErrorsInner();
            fieldErrorDto.setField(fieldError.getField());
            fieldErrorDto.setMessage(fieldError.getDefaultMessage());
            error.addFieldErrorsItem(fieldErrorDto);
        });

        error.setMessage("Validation failed for one or more fields.");
        error.setCode("VALIDATION_ERROR");
        error.setTimestamp(OffsetDateTime.now());
        response.setStatus("error");
        response.setError(error);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles EmailAlreadyExistException which is thrown when a patient with the same email already exists.
     *
     * @param ex the EmailAlreadyExistException
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the error
     */
    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ValidationErrorResponse> handleEmailAlreadyExistException(EmailAlreadyExistException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        ValidationErrorResponseError error = new ValidationErrorResponseError();

        ValidationErrorResponseErrorFieldErrorsInner fieldErrorDto = new ValidationErrorResponseErrorFieldErrorsInner();
        fieldErrorDto.setField("email");
        fieldErrorDto.setMessage(ex.getMessage());
        error.addFieldErrorsItem(fieldErrorDto);
        
        error.setMessage("Email already exists.");
        error.setCode("EMAIL_ALREADY_EXISTS");
        error.setTimestamp(OffsetDateTime.now());
        response.setStatus("error");
        response.setError(error);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles ObjectOptimisticLockingFailureException which is thrown when optimistic locking fails.
     *
     * @param ex the ObjectOptimisticLockingFailureException
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the error
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ValidationErrorResponse> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        ValidationErrorResponseError error = new ValidationErrorResponseError();
        
        error.setMessage("The patient data has been modified by another operation. Please refresh and try again.");
        error.setCode("OPTIMISTIC_LOCKING_FAILURE");
        error.setTimestamp(OffsetDateTime.now());
        response.setStatus("error");
        response.setError(error);

        return ResponseEntity.status(409).body(response);
    }

    /**
     * Handles any other exceptions that are not specifically handled.
     *
     * @param ex the Exception
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleGenericException(Exception ex) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        ValidationErrorResponseError error = new ValidationErrorResponseError();
        
        error.setMessage("An unexpected error occurred. Please try again later.");
        error.setCode("INTERNAL_SERVER_ERROR");
        error.setTimestamp(OffsetDateTime.now());
        response.setStatus("error");
        response.setError(error);

        return ResponseEntity.status(500).body(response);
    }
}       
