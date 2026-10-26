package com.jobboard.applications.application.port.in;

/**
 * Inbound port for downloading the uploaded resume file of an applicant.
 */
public interface DownloadResumeUseCase {

    ResumeFile getResume(Long applicationId, Long employerUserId);

    record ResumeFile(String fileName, String contentType, byte[] data) {}
}
