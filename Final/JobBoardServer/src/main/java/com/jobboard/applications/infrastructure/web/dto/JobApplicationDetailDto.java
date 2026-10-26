package com.jobboard.applications.infrastructure.web.dto;

public record JobApplicationDetailDto(
        Long id,
        Long jobId,
        String applicantName,
        String applicantEmail,
        String resumeFileName,
        String status,
        boolean hasResume,
        String submittedAt
) {
    /**
     * Backward-compatible 6-parameter constructor.
     */
    public JobApplicationDetailDto(Long id, Long jobId, String applicantName, String applicantEmail, String resumeFileName, String submittedAt) {
        this(id, jobId, applicantName, applicantEmail, resumeFileName, "PENDING", resumeFileName != null && !resumeFileName.isBlank(), submittedAt);
    }
}
