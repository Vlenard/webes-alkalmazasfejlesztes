package com.jobboard.applications.application.service;

import com.jobboard.applications.application.port.in.GetApplicationsUseCase;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase;
import com.jobboard.applications.application.port.out.ApplicationRepositoryPort;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ApplicationService implements SubmitApplicationUseCase, GetApplicationsUseCase {

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
        // Domain boundary check: verify target job existence
        if (!jobRepositoryPort.existsById(command.jobId())) {
            throw new JobNotFoundException(command.jobId());
        }

        JobApplication newApplication = JobApplication.createNew(
                command.jobId(),
                command.applicantName(),
                command.applicantEmail(),
                command.resumeFileName()
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
}
