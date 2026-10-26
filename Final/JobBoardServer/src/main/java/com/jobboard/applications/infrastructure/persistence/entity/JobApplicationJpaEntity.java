package com.jobboard.applications.infrastructure.persistence.entity;

import com.jobboard.applications.domain.model.ApplicationStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_applications")
public class JobApplicationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    @Column(name = "resume_file_name")
    private String resumeFileName;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "applicant_user_id")
    private Long applicantUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'PENDING'")
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "resume_content_type")
    private String resumeContentType;

    @Column(name = "resume_data")
    private byte[] resumeData;

    public JobApplicationJpaEntity() {}

    public JobApplicationJpaEntity(Long id, Long jobId, String applicantName, String applicantEmail,
                                   String resumeFileName, Instant submittedAt) {
        this(id, jobId, applicantName, applicantEmail, resumeFileName, submittedAt, null,
                ApplicationStatus.PENDING, null, null);
    }

    public JobApplicationJpaEntity(Long id, Long jobId, String applicantName, String applicantEmail,
                                   String resumeFileName, Instant submittedAt, Long applicantUserId,
                                   ApplicationStatus status, String resumeContentType, byte[] resumeData) {
        this.id = id;
        this.jobId = jobId;
        this.applicantName = applicantName;
        this.applicantEmail = applicantEmail;
        this.resumeFileName = resumeFileName;
        this.submittedAt = submittedAt;
        this.applicantUserId = applicantUserId;
        this.status = status != null ? status : ApplicationStatus.PENDING;
        this.resumeContentType = resumeContentType;
        this.resumeData = resumeData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Long getApplicantUserId() {
        return applicantUserId;
    }

    public void setApplicantUserId(Long applicantUserId) {
        this.applicantUserId = applicantUserId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status != null ? status : ApplicationStatus.PENDING;
    }

    public String getResumeContentType() {
        return resumeContentType;
    }

    public void setResumeContentType(String resumeContentType) {
        this.resumeContentType = resumeContentType;
    }

    public byte[] getResumeData() {
        return resumeData;
    }

    public void setResumeData(byte[] resumeData) {
        this.resumeData = resumeData;
    }
}
