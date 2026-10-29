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
            Long createdByUserId,
            List<String> skills
    ) {
        /**
         * Backward-compatible 8-parameter constructor without createdByUserId and skills.
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
            this(title, company, type, location, badge, salary, description, requirements, null, List.of());
        }

        /**
         * Backward-compatible 9-parameter constructor without skills.
         */
        public CreateJobCommand(
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
            this(title, company, type, location, badge, salary, description, requirements, createdByUserId, List.of());
        }

        public CreateJobCommand {
            requirements = requirements != null ? List.copyOf(requirements) : List.of();
            skills = skills != null ? List.copyOf(skills) : List.of();
        }
    }
}
