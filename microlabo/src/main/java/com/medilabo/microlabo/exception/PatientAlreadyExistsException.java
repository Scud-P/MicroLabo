package com.medilabo.microlabo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a patient that already exists in the system.
 * This exception is annotated with {@link ResponseStatus} to indicate that
 * an HTTP 409 Conflict status should be returned when this exception is thrown.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class PatientAlreadyExistsException extends RuntimeException {
    /**
     * Constructs a new PatientAlreadyExistsException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public PatientAlreadyExistsException(String message) {
        super(message);
    }
}
