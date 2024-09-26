package com.medilabo.microfront.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested patient is not found in the system.
 * This exception is annotated with {@link ResponseStatus} to indicate that
 * an HTTP 404 Not Found status should be returned when this exception is thrown.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class PatientNotFoundException extends RuntimeException {
    /**
     * Constructs a new PatientNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public PatientNotFoundException(String message) {
        super(message);
    }
}
