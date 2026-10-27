package com.jobboard.applications.application.service;

import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.applications.application.port.in.DownloadResumeUseCase;
import com.jobboard.applications.application.port.in.GetApplicationsUseCase;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase;
import com.jobboard.applications.application.port.in.UpdateApplicationStatusUseCase;
import com.jobboard.applications.application.port.out.ApplicationRepositoryPort;
import com.jobboard.applications.domain.exception.InvalidApplicationException;
import com.jobboard.applications.domain.model.ApplicationStatus;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.jobs.domain.model.Job;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ApplicationService implements
        SubmitApplicationUseCase,
        GetApplicationsUseCase,
        UpdateApplicationStatusUseCase,
        DownloadResumeUseCase {

    private final ApplicationRepositoryPort applicationRepositoryPort;
    private final JobRepositoryPort jobRepositoryPort;

    public ApplicationService(ApplicationRepositoryPort applicationRepositoryPort,
                              JobRepositoryPort jobRepositoryPort) {
        this.applicationRepositoryPort = applicationRepositoryPort;
        this.jobRepositoryPort = jobRepositoryPort;
    }

    @Override
    @Transactional
    public JobApplication submitApplication(SubmitApplicationCommand command) {
        Job job = jobRepositoryPort.findById(command.jobId())
                .orElseThrow(() -> new JobNotFoundException(command.jobId()));

        if (!job.isActive()) {
            throw new InvalidApplicationException("Lezárt álláshirdetésre nem lehet jelentkezni.");
        }

        if (applicationRepositoryPort.existsByJobIdAndApplicant(command.jobId(), command.applicantUserId(), command.applicantEmail())) {
            throw new InvalidApplicationException("Már jelentkeztél erre az állásra.");
        }

        JobApplication newApplication = JobApplication.createNew(
                command.jobId(),
                command.applicantName(),
                command.applicantEmail(),
                command.resumeFileName(),
                command.applicantUserId(),
                command.resumeContentType(),
                command.resumeData()
        );

        return applicationRepositoryPort.save(newApplication);
    }

    @Override
    public List<JobApplication> getApplicationsByJobId(Long jobId) {
        if (!jobRepositoryPort.existsById(jobId)) {
            throw new JobNotFoundException(jobId);
        }
        return applicationRepositoryPort.findByJobId(jobId);
    }

    @Override
    public List<GetAllApplicationsResult> getAllApplications() {
        return applicationRepositoryPort.findAll().stream()
                .map(app -> new GetAllApplicationsResult(
                        app.getId(),
                        app.getJobId(),
                        app.getApplicantName(),
                        app.getApplicantEmail(),
                        app.getResumeFileName(),
                        app.getSubmittedAt().toString()
                ))
                .toList();
    }

    @Override
    public List<ApplicantApplicationView> getApplicationsForApplicant(Long applicantUserId, String applicantEmail) {
        List<JobApplication> apps = applicationRepositoryPort.findByApplicant(applicantUserId, applicantEmail);
        return apps.stream().map(app -> {
            var jobOpt = jobRepositoryPort.findById(app.getJobId());
            String jobTitle = jobOpt.map(Job::getTitle).orElse("Ismeretlen pozíció");
            String company = jobOpt.map(Job::getCompany).orElse("Ismeretlen cég");
            String location = jobOpt.map(Job::getLocation).orElse("-");
            String statusStr = app.getStatus() != null ? app.getStatus().name() : "PENDING";
            String statusMsg = ApplicantApplicationView.resolveStatusMessage(statusStr);
            return new ApplicantApplicationView(
                    app.getId(),
                    app.getJobId(),
                    jobTitle,
                    company,
                    location,
                    statusStr,
                    statusMsg,
                    app.getResumeFileName(),
                    app.getSubmittedAt()
            );
        }).toList();
    }

    @Override
    @Transactional
    public JobApplication updateStatus(Long applicationId, ApplicationStatus newStatus, Long employerUserId) {
        JobApplication app = applicationRepositoryPort.findById(applicationId)
                .orElseThrow(() -> new InvalidApplicationException("A jelentkezés nem található (ID: " + applicationId + ")"));

        Job job = jobRepositoryPort.findById(app.getJobId())
                .orElseThrow(() -> new JobNotFoundException(app.getJobId()));

        if (job.getCreatedByUserId() != null && employerUserId != null && !job.getCreatedByUserId().equals(employerUserId)) {
            throw new UnauthorizedAccessException("Csak az álláshirdetés létrehozója módosíthatja a jelentkezés státuszát.");
        }

        JobApplication updated = app.withStatus(newStatus);
        return applicationRepositoryPort.save(updated);
    }

    @Override
    public ResumeFile getResume(Long applicationId, Long employerUserId) {
        JobApplication app = applicationRepositoryPort.findById(applicationId)
                .orElseThrow(() -> new InvalidApplicationException("A jelentkezés nem található (ID: " + applicationId + ")"));

        Job job = jobRepositoryPort.findById(app.getJobId())
                .orElseThrow(() -> new JobNotFoundException(app.getJobId()));

        if (job.getCreatedByUserId() != null && employerUserId != null && !job.getCreatedByUserId().equals(employerUserId)) {
            throw new UnauthorizedAccessException("Csak az álláshirdetés létrehozója töltheti le az önéletrajzot.");
        }

        if (app.getResumeData() == null || app.getResumeData().length == 0) {
            throw new InvalidApplicationException("Ehhez a jelentkezéshez nem tartozik feltöltött önéletrajz fájl.");
        }

        String contentType = app.getResumeContentType();
        if (contentType == null || contentType.isBlank()) {
            if (app.getResumeFileName() != null && app.getResumeFileName().toLowerCase().endsWith(".pdf")) {
                contentType = "application/pdf";
            } else {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
        }

        return new ResumeFile(app.getResumeFileName(), contentType, app.getResumeData());
    }
}
