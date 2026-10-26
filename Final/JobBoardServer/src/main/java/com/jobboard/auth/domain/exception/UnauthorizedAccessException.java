package com.jobboard.auth.domain.exception;

/**
 * Thrown when an unauthenticated or unauthorized user attempts an operation.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
