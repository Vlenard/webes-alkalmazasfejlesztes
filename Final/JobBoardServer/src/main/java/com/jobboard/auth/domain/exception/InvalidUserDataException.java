package com.jobboard.auth.domain.exception;

/**
 * Thrown when domain validation fails for user data.
 */
public class InvalidUserDataException extends RuntimeException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}
