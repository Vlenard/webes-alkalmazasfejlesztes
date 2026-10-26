package com.jobboard.applications.infrastructure.web.dto;

/**
 * DTO representing an applicant's view of their application.
 */
public record ApplicantApplicationResponseDto(
        Long id,
        Long jobId,
        String jobTitle,
        String company,
        String location,
        String status,
        String statusMessage,
        String resumeFileName,
        String submittedAt
) {}
