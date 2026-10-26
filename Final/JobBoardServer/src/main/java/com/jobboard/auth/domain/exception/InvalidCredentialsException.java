package com.jobboard.auth.domain.exception;

/**
 * Thrown when login fails due to incorrect email or password.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
