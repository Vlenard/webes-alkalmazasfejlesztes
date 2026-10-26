package com.jobboard.jobs.application.port.in;

import com.jobboard.jobs.domain.model.Job;

public interface GetJobDetailUseCase {
    Job getJobById(Long id);
}
