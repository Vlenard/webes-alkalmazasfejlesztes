package com.jobboard.jobs.domain.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(Long id) {
        super("Az álláshirdetés nem található (ID: " + id + ")");
    }
}
