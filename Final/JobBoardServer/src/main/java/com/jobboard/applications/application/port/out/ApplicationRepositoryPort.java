package com.jobboard.applications.application.port.out;

import com.jobboard.applications.domain.model.JobApplication;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepositoryPort {
    JobApplication save(JobApplication application);

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findAll();

    List<JobApplication> findByApplicant(Long applicantUserId, String applicantEmail);

    Optional<JobApplication> findById(Long id);

    long count();
}
