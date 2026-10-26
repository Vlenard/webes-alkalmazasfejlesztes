package com.jobboard.applications.infrastructure.web.mapper;

import com.jobboard.applications.application.port.in.SubmitApplicationUseCase.SubmitApplicationCommand;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.web.dto.ApplicationSubmissionResponseDto;
import com.jobboard.applications.infrastructure.web.dto.JobApplicationDetailDto;
import com.jobboard.applications.infrastructure.web.dto.SubmitApplicationRequestDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ApplicationWebMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public SubmitApplicationCommand toCommand(SubmitApplicationRequestDto request) {
        if (request == null) {
            return null;
        }
        return new SubmitApplicationCommand(
                request.jobId(),
                request.applicantName(),
                request.applicantEmail(),
                request.resumeFileName()
        );
    }

    public ApplicationSubmissionResponseDto toSubmissionResponse(JobApplication application) {
        if (application == null) {
            return null;
        }
        String formattedDate = application.getSubmittedAt() != null
                ? ISO_FORMATTER.format(application.getSubmittedAt())
                : null;
        return ApplicationSubmissionResponseDto.success(application.getId(), formattedDate);
    }

    public JobApplicationDetailDto toDetailDto(JobApplication application) {
        if (application == null) {
            return null;
        }
        return new JobApplicationDetailDto(
                application.getId(),
                application.getJobId(),
                application.getApplicantName(),
                application.getApplicantEmail(),
                application.getResumeFileName(),
                application.getSubmittedAt() != null ? ISO_FORMATTER.format(application.getSubmittedAt()) : null
        );
    }
}
