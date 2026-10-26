package com.jobboard.applications.infrastructure.web.dto;

/**
 * Response DTO matching frontend expectation: { success: boolean, message: string }
 */
public record ApplicationSubmissionResponseDto(
        boolean success,
        String message,
        Long applicationId,
        String submittedAt
) {
    public static ApplicationSubmissionResponseDto success(Long applicationId, String submittedAt) {
        return new ApplicationSubmissionResponseDto(
                true,
                "A jelentkezést sikeresen rögzítettük!",
                applicationId,
                submittedAt
        );
    }
}
