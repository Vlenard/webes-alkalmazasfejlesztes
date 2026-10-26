package com.jobboard.applications.infrastructure.web.mapper;

import com.jobboard.applications.application.port.in.GetApplicationsUseCase.ApplicantApplicationView;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase.SubmitApplicationCommand;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.web.dto.ApplicantApplicationResponseDto;
import com.jobboard.applications.infrastructure.web.dto.ApplicationSubmissionResponseDto;
import com.jobboard.applications.infrastructure.web.dto.JobApplicationDetailDto;
import com.jobboard.applications.infrastructure.web.dto.SubmitApplicationRequestDto;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class ApplicationWebMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public SubmitApplicationCommand toCommand(SubmitApplicationRequestDto request) {
        return toCommand(request, null, null, null);
    }

    public SubmitApplicationCommand toCommand(SubmitApplicationRequestDto request, Long applicantUserId, String resumeContentType, byte[] resumeData) {
        if (request == null) {
            return null;
        }
        return new SubmitApplicationCommand(
                request.jobId(),
                request.applicantName(),
                request.applicantEmail(),
                request.resumeFileName(),
                applicantUserId,
                resumeContentType,
                resumeData
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
                application.getStatus() != null ? application.getStatus().name() : "PENDING",
                application.hasResume(),
                application.getSubmittedAt() != null ? ISO_FORMATTER.format(application.getSubmittedAt()) : null
        );
    }

    public ApplicantApplicationResponseDto toApplicantViewDto(ApplicantApplicationView view) {
        if (view == null) {
            return null;
        }
        return new ApplicantApplicationResponseDto(
                view.id(),
                view.jobId(),
                view.jobTitle(),
                view.company(),
                view.location(),
                view.status(),
                view.statusMessage(),
                view.resumeFileName(),
                view.submittedAt() != null ? ISO_FORMATTER.format(view.submittedAt()) : null
        );
    }
}
