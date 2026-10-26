package com.jobboard.applications.application.port.in;

import com.jobboard.applications.domain.model.JobApplication;

public interface SubmitApplicationUseCase {
    JobApplication submitApplication(SubmitApplicationCommand command);

    record SubmitApplicationCommand(
            Long jobId,
            String applicantName,
            String applicantEmail,
            String resumeFileName,
            Long applicantUserId,
            String resumeContentType,
            byte[] resumeData
    ) {
        /**
         * Backward-compatible constructor for legacy submissions without binary attachment or user ID.
         */
        public SubmitApplicationCommand(Long jobId, String applicantName, String applicantEmail, String resumeFileName) {
            this(jobId, applicantName, applicantEmail, resumeFileName, null, null, null);
        }
    }
}
