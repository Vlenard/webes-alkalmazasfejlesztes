package com.jobboard.jobs.domain.exception;

public class InvalidJobDataException extends RuntimeException {
    public InvalidJobDataException(String message) {
        super(message);
    }
}
