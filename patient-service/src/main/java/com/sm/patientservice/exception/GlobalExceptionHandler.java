package com.sm.patientservice.exception;

import java.util.function.Consumer;

import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.sm.patientservice.constant.ResponseConstant;
import com.sm.patientservice.model.dto.ValidationErrorResponse;
import com.sm.patientservice.model.dto.ValidationErrorResponseError;
import com.sm.patientservice.model.dto.ValidationErrorResponseErrorFieldErrorsInner;
import com.sm.patientservice.utils.AppUtils;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    private ResponseEntity<ValidationErrorResponse> buildErrorResponse(Consumer<ValidationErrorResponseError> errorCustomizer, int status) {
        ValidationErrorResponse response = new ValidationErrorResponse();
        ValidationErrorResponseError error = new ValidationErrorResponseError();
        errorCustomizer.accept(error);
        error.setTimestamp(AppUtils.getCurrentTimestamp());
        response.setStatus(ResponseConstant.ERROR_STATUS);
        response.setError(error);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Handles MethodArgumentNotValidException which is thrown when validation on an argument annotated with
     * @Valid fails.
     *
     * @param ex the MethodArgumentNotValidException
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        return buildErrorResponse(error -> {
            ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
                ValidationErrorResponseErrorFieldErrorsInner fieldErrorDto = new ValidationErrorResponseErrorFieldErrorsInner();
                fieldErrorDto.setField(fieldError.getField());
                fieldErrorDto.setMessage(fieldError.getDefaultMessage());
                error.addFieldErrorsItem(fieldErrorDto);
            });
            error.setMessage("Validation failed for one or more fields.");
            error.setCode("VALIDATION_ERROR");
        }, 400);
    }

    /**
     * Handles EmailAlreadyExistException which is thrown when a patient with the same email already exists.
     *
     * @param ex the EmailAlreadyExistException
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the error
     */
    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ValidationErrorResponse> handleEmailAlreadyExistException(EmailAlreadyExistException ex) {
        return buildErrorResponse(error -> {
            ValidationErrorResponseErrorFieldErrorsInner fieldErrorDto = new ValidationErrorResponseErrorFieldErrorsInner();
            fieldErrorDto.setField("email");
            fieldErrorDto.setMessage(ex.getMessage());
            error.addFieldErrorsItem(fieldErrorDto);
            error.setMessage("Email already exists.");
            error.setCode("EMAIL_ALREADY_EXISTS");
        }, 409);
    }
    
    /**
     * Handles PatientNotExistException which is thrown when a patient with the specified ID does not exist.
     *
     * @param ex the PatientNotExistException
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the error
     */
    @ExceptionHandler(PatientNotExistException.class)
    public ResponseEntity<ValidationErrorResponse> handlePatientNotExistException(PatientNotExistException ex) {
        return buildErrorResponse(error -> {
            error.setMessage(ex.getMessage());
            error.setCode("PATIENT_NOT_FOUND");
        }, 404);
    }

    /**
     * Handles ObjectOptimisticLockingFailureException which is thrown when optimistic locking fails.
     *
     * @param ex the ObjectOptimisticLockingFailureException
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the error
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ValidationErrorResponse> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException ex) {
        return buildErrorResponse(error -> {
            error.setMessage("The patient data has been modified by another operation. Please refresh and try again.");
            error.setCode("OPTIMISTIC_LOCKING_FAILURE");
        }, 409);
    }

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<ValidationErrorResponse> handleBillingException(BillingException ex) {
        return buildErrorResponse(error -> {
            error.setMessage(ex.getMessage());
            error.setCode("BILLING_ERROR");
        }, 500);
    }

    /**
     * Handles any other exceptions that are not specifically handled.
     *
     * @param ex the Exception
     * @return a ResponseEntity containing a ValidationErrorResponse with details of the error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleGenericException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return buildErrorResponse(error -> {
            error.setMessage("An unexpected error occurred. Please try again later.");
            error.setCode("INTERNAL_SERVER_ERROR");
        }, 500);
    }
}       
