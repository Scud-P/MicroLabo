package com.medilabo.microfront.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested note is not found.
 * This exception is annotated with {@link ResponseStatus} to indicate that
 * an HTTP 404 Not Found status should be returned when this exception is thrown.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoteNotFoundException extends RuntimeException {

    /**
     * Constructs a new NoteNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public NoteNotFoundException(String message) {
        super(message);
    }
}
