package com.jobboard.jobs.application.service;

import com.jobboard.jobs.application.port.in.CreateJobUseCase;
import com.jobboard.jobs.application.port.in.GetJobDetailUseCase;
import com.jobboard.jobs.application.port.in.GetJobsUseCase;
import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.jobs.domain.model.Job;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class JobService implements GetJobsUseCase, GetJobDetailUseCase, CreateJobUseCase {

    private final JobRepositoryPort jobRepositoryPort;

    public JobService(JobRepositoryPort jobRepositoryPort) {
        this.jobRepositoryPort = jobRepositoryPort;
    }

    @Override
    public List<Job> getJobs(String search, String badge) {
        return jobRepositoryPort.searchJobs(search, badge);
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
                command.requirements()
        );
        return jobRepositoryPort.save(newJob);
    }
}
