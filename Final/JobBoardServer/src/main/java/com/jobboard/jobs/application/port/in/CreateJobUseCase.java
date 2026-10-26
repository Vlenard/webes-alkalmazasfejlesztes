package com.jobboard.jobs.application.port.in;

import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobType;
import java.util.List;

public interface CreateJobUseCase {
    Job createJob(CreateJobCommand command);

    record CreateJobCommand(
            String title,
            String company,
            JobType type,
            String location,
            String badge,
            String salary,
            String description,
            List<String> requirements,
            Long createdByUserId
    ) {
        /**
         * Backward-compatible constructor without createdByUserId (defaults to null).
         */
        public CreateJobCommand(
                String title,
                String company,
                JobType type,
                String location,
                String badge,
                String salary,
                String description,
                List<String> requirements
        ) {
            this(title, company, type, location, badge, salary, description, requirements, null);
        }
    }
}
