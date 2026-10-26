package com.jobboard.jobs.application.port.in;

import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;

/**
 * Inbound port for changing a job's status (ACTIVE <-> CLOSED).
 * Only the creator of the job may change its status.
 */
public interface UpdateJobStatusUseCase {

    Job updateJobStatus(Long jobId, JobStatus newStatus, Long requestingUserId);
}
