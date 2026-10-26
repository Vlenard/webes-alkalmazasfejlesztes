package com.jobboard.applications.domain.model;

import com.jobboard.applications.domain.exception.InvalidApplicationException;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing an applicant's submission to a specific job listing.
 */
public class JobApplication {
    private final Long id;
    private final Long jobId;
    private final String applicantName;
    private final String applicantEmail;
    private final String resumeFileName;
    private final Instant submittedAt;

    public JobApplication(Long id, Long jobId, String applicantName, String applicantEmail,
                          String resumeFileName, Instant submittedAt) {
        validate(jobId, applicantName, applicantEmail);
        this.id = id;
        this.jobId = jobId;
        this.applicantName = applicantName.trim();
        this.applicantEmail = applicantEmail.trim().toLowerCase();
        this.resumeFileName = resumeFileName != null && !resumeFileName.isBlank() ? resumeFileName.trim() : null;
        this.submittedAt = submittedAt != null ? submittedAt : Instant.now();
    }

    public static JobApplication createNew(Long jobId, String applicantName, String applicantEmail, String resumeFileName) {
        return new JobApplication(null, jobId, applicantName, applicantEmail, resumeFileName, Instant.now());
    }

    private static void validate(Long jobId, String applicantName, String applicantEmail) {
        if (jobId == null || jobId <= 0) {
            throw new InvalidApplicationException("Érvénytelen állás azonosító (jobId).");
        }
        if (applicantName == null || applicantName.isBlank()) {
            throw new InvalidApplicationException("A jelentkező neve nem lehet üres.");
        }
        if (applicantEmail == null || applicantEmail.isBlank() || !applicantEmail.contains("@")) {
            throw new InvalidApplicationException("Érvényes email cím megadása kötelező.");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getJobId() {
        return jobId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobApplication that = (JobApplication) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
