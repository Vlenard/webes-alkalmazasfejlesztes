package com.jobboard.applications.domain.model;

import com.jobboard.applications.domain.exception.InvalidApplicationException;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing an applicant's submission to a specific job listing.
 * Encapsulates application lifecycle, validation rules, and resume metadata.
 */
public class JobApplication {
    private final Long id;
    private final Long jobId;
    private final String applicantName;
    private final String applicantEmail;
    private final String resumeFileName;
    private final Instant submittedAt;
    private final Long applicantUserId;
    private final ApplicationStatus status;
    private final String resumeContentType;
    private final byte[] resumeData;

    /**
     * Backward-compatible 6-parameter constructor for existing tests and JSON endpoints.
     */
    public JobApplication(Long id, Long jobId, String applicantName, String applicantEmail,
                          String resumeFileName, Instant submittedAt) {
        this(id, jobId, applicantName, applicantEmail, resumeFileName, submittedAt, null, ApplicationStatus.PENDING, null, null);
    }

    /**
     * Full domain constructor with resume binary data and applicant user reference.
     */
    public JobApplication(Long id, Long jobId, String applicantName, String applicantEmail,
                          String resumeFileName, Instant submittedAt, Long applicantUserId,
                          ApplicationStatus status, String resumeContentType, byte[] resumeData) {
        validate(jobId, applicantName, applicantEmail, resumeFileName);
        this.id = id;
        this.jobId = jobId;
        this.applicantName = applicantName.trim();
        this.applicantEmail = applicantEmail.trim().toLowerCase();
        this.resumeFileName = resumeFileName != null && !resumeFileName.isBlank() ? resumeFileName.trim() : null;
        this.submittedAt = submittedAt != null ? submittedAt : Instant.now();
        this.applicantUserId = applicantUserId;
        this.status = status != null ? status : ApplicationStatus.PENDING;
        this.resumeContentType = resumeContentType;
        this.resumeData = resumeData != null ? resumeData.clone() : null;
    }

    public static JobApplication createNew(Long jobId, String applicantName, String applicantEmail, String resumeFileName) {
        return createNew(jobId, applicantName, applicantEmail, resumeFileName, null, null, null);
    }

    public static JobApplication createNew(Long jobId, String applicantName, String applicantEmail,
                                          String resumeFileName, Long applicantUserId,
                                          String resumeContentType, byte[] resumeData) {
        return new JobApplication(null, jobId, applicantName, applicantEmail, resumeFileName, Instant.now(),
                applicantUserId, ApplicationStatus.PENDING, resumeContentType, resumeData);
    }

    private static void validate(Long jobId, String applicantName, String applicantEmail, String resumeFileName) {
        if (jobId == null || jobId <= 0) {
            throw new InvalidApplicationException("Érvénytelen állás azonosító (jobId).");
        }
        if (applicantName == null || applicantName.isBlank()) {
            throw new InvalidApplicationException("A jelentkező neve nem lehet üres.");
        }
        if (applicantEmail == null || applicantEmail.isBlank() || !applicantEmail.contains("@")) {
            throw new InvalidApplicationException("Érvényes email cím megadása kötelező.");
        }
        if (resumeFileName != null && !resumeFileName.isBlank()) {
            String lower = resumeFileName.trim().toLowerCase();
            if (!lower.endsWith(".pdf") && !lower.endsWith(".docx")) {
                throw new InvalidApplicationException("Csak PDF vagy DOCX formátumú önéletrajz tölthető fel.");
            }
        }
    }

    public JobApplication withStatus(ApplicationStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidApplicationException("A státusz megadása kötelező.");
        }
        return new JobApplication(this.id, this.jobId, this.applicantName, this.applicantEmail,
                this.resumeFileName, this.submittedAt, this.applicantUserId, newStatus,
                this.resumeContentType, this.resumeData);
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

    public Long getApplicantUserId() {
        return applicantUserId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getResumeContentType() {
        return resumeContentType;
    }

    public byte[] getResumeData() {
        return resumeData != null ? resumeData.clone() : null;
    }

    public boolean hasResume() {
        return resumeData != null && resumeData.length > 0;
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
