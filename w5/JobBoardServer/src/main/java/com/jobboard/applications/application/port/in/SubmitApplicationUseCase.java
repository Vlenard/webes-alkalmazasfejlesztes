package com.jobboard.applications.application.port.in;

import com.jobboard.applications.domain.model.JobApplication;

public interface SubmitApplicationUseCase {
    JobApplication submitApplication(SubmitApplicationCommand command);

    record SubmitApplicationCommand(
            Long jobId,
            String applicantName,
            String applicantEmail,
            String resumeFileName
    ) {}
}
