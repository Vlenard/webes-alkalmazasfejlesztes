package com.jobboard.jobs.application.port.in;

import com.jobboard.jobs.domain.model.Job;
import java.util.List;

/**
 * Inbound port for querying job listings.
 */
public interface GetJobsUseCase {

    /**
     * Retrieves all active jobs matching optional search query and badge filter.
     * Closed jobs are excluded.
     */
    List<Job> getJobs(String search, String badge);

    /**
     * Retrieves all jobs (both ACTIVE and CLOSED) created by the specified user.
     */
    List<Job> getJobsByCreator(Long creatorUserId);

    /**
     * Retrieves all jobs (both ACTIVE and CLOSED) created by the specified user or matching company name.
     */
    default List<Job> getJobsByCreator(Long creatorUserId, String companyName) {
        return getJobsByCreator(creatorUserId);
    }
}
