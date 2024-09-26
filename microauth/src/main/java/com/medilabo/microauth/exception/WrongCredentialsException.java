package com.medilabo.microauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user authentication fails due to incorrect credentials.
 * <p>
 * This exception is annotated with {@link ResponseStatus} to indicate that
 * an HTTP 401 Unauthorized status should be returned when this exception is thrown.
 * </p>
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
