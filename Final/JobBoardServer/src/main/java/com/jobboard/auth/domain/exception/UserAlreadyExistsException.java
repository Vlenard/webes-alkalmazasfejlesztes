package com.jobboard.auth.domain.exception;

/**
 * Thrown when trying to register with an email that is already in use.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
