package com.jobboard.jobs.application.port.in;

import com.jobboard.jobs.domain.model.Job;
import java.util.List;

/**
 * Inbound port for updating required skills of a job listing.
 * Only the creator of the job may update its skills.
 */
public interface UpdateJobSkillsUseCase {

    Job updateJobSkills(Long jobId, List<String> skills, Long requestingUserId);
}
