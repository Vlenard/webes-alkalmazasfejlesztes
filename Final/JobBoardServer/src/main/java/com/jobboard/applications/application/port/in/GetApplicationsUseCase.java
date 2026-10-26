package com.jobboard.applications.application.port.in;

import com.jobboard.applications.domain.model.JobApplication;
import java.time.Instant;
import java.util.List;

public interface GetApplicationsUseCase {
    List<JobApplication> getApplicationsByJobId(Long jobId);

    List<GetAllApplicationsResult> getAllApplications();

    List<ApplicantApplicationView> getApplicationsForApplicant(Long applicantUserId, String applicantEmail);

    record GetAllApplicationsResult(
            Long id,
            Long jobId,
            String applicantName,
            String applicantEmail,
            String resumeFileName,
            String submittedAt
    ) {}

    record ApplicantApplicationView(
            Long id,
            Long jobId,
            String jobTitle,
            String company,
            String location,
            String status,
            String statusMessage,
            String resumeFileName,
            Instant submittedAt
    ) {
        public static String resolveStatusMessage(String status) {
            if ("ACCEPTED".equalsIgnoreCase(status)) {
                return "A cég értesíti Önt a továbbiakról.";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                return "A jelentkezését elutasították.";
            } else {
                return "Jelentkezése elbírálás alatt áll.";
            }
        }
    }
}
