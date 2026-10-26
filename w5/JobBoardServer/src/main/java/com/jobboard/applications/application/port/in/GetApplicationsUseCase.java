package com.jobboard.applications.application.port.in;

import com.jobboard.applications.domain.model.JobApplication;
import java.util.List;

public interface GetApplicationsUseCase {
    List<JobApplication> getApplicationsByJobId(Long jobId);

    List<GetAllApplicationsResult> getAllApplications();

    record GetAllApplicationsResult(
            Long id,
            Long jobId,
            String applicantName,
            String applicantEmail,
            String resumeFileName,
            String submittedAt
    ) {}
}
