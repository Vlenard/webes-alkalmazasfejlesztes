package com.jobboard.jobs.application.port.in;

import com.jobboard.jobs.domain.model.Job;
import java.util.List;

public interface GetJobsUseCase {
    List<Job> getJobs(String search, String badge);
}
