package com.medilabo.microfront.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when the provided credentials are incorrect during authentication.
 * This exception is annotated with {@link ResponseStatus} to indicate that
 * an HTTP 401 Unauthorized status should be returned when this exception is thrown.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class WrongCredentialsException extends RuntimeException {
    /**
     * Constructs a new WrongCredentialsException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public WrongCredentialsException(String message) {
        super(message);
    }
}
