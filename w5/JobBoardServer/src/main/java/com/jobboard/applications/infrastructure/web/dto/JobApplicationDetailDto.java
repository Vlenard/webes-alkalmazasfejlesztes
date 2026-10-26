package com.jobboard.applications.infrastructure.web.dto;

public record JobApplicationDetailDto(
        Long id,
        Long jobId,
        String applicantName,
        String applicantEmail,
        String resumeFileName,
        String submittedAt
) {}
