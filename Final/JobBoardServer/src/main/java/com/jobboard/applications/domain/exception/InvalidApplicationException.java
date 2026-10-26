package com.jobboard.applications.domain.exception;

public class InvalidApplicationException extends RuntimeException {
    public InvalidApplicationException(String message) {
        super(message);
    }
}
