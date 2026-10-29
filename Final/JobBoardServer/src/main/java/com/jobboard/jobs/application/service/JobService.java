package com.jobboard.jobs.application.service;

import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.jobs.application.port.in.CreateJobUseCase;
import com.jobboard.jobs.application.port.in.GetJobDetailUseCase;
import com.jobboard.jobs.application.port.in.GetJobsUseCase;
import com.jobboard.jobs.application.port.in.UpdateJobSkillsUseCase;
import com.jobboard.jobs.application.port.in.UpdateJobStatusUseCase;
import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class JobService implements GetJobsUseCase, GetJobDetailUseCase, CreateJobUseCase, UpdateJobStatusUseCase, UpdateJobSkillsUseCase {

    private final JobRepositoryPort jobRepositoryPort;

    public JobService(JobRepositoryPort jobRepositoryPort) {
        this.jobRepositoryPort = jobRepositoryPort;
    }

    @Override
    public List<Job> getJobs(String search, String badge) {
        return jobRepositoryPort.searchJobs(search, badge).stream()
                .filter(Job::isActive)
                .toList();
    }

    @Override
    public List<Job> getJobsByCreator(Long creatorUserId) {
        return getJobsByCreator(creatorUserId, null);
    }

    @Override
    public List<Job> getJobsByCreator(Long creatorUserId, String companyName) {
        if (companyName != null && !companyName.isBlank()) {
            return jobRepositoryPort.findByCreatedByUserIdOrCompanyName(creatorUserId, companyName);
        }
        return jobRepositoryPort.findByCreatedByUserId(creatorUserId);
    }

    @Override
    public Job getJobById(Long id) {
        return jobRepositoryPort.findById(id)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    @Override
    @Transactional
    public Job createJob(CreateJobCommand command) {
        Job newJob = Job.createNew(
                command.title(),
                command.company(),
                command.type(),
                command.location(),
                command.badge(),
                command.salary(),
                command.description(),
                command.requirements(),
                command.createdByUserId(),
                command.skills()
        );
        return jobRepositoryPort.save(newJob);
    }

    @Override
    @Transactional
    public Job updateJobStatus(Long jobId, JobStatus newStatus, Long requestingUserId) {
        Job existing = getJobById(jobId);
        if (existing.getCreatedByUserId() != null && !existing.getCreatedByUserId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("Csak a hirdetés létrehozója módosíthatja vagy nyithatja újra a hirdetést.");
        }
        Job updated = existing.withStatusChangedBy(newStatus, requestingUserId);
        if (updated.getCreatedByUserId() == null && requestingUserId != null) {
            updated = updated.withCreatedByUserId(requestingUserId);
        }
        return jobRepositoryPort.save(updated);
    }

    @Override
    @Transactional
    public Job updateJobSkills(Long jobId, List<String> skills, Long requestingUserId) {
        Job existing = getJobById(jobId);
        if (existing.getCreatedByUserId() != null && !existing.getCreatedByUserId().equals(requestingUserId)) {
            throw new UnauthorizedAccessException("Csak a hirdetés létrehozója módosíthatja a szükséges skilleket.");
        }
        Job updated = existing.withSkillsChangedBy(skills, requestingUserId);
        if (updated.getCreatedByUserId() == null && requestingUserId != null) {
            updated = updated.withCreatedByUserId(requestingUserId);
        }
        return jobRepositoryPort.save(updated);
    }
}

