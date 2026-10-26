package com.jobboard.shared.infrastructure.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return new ErrorResponseDto(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponseDto ofValidation(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ErrorResponseDto(Instant.now(), status, error, message, path, validationErrors);
    }
}
