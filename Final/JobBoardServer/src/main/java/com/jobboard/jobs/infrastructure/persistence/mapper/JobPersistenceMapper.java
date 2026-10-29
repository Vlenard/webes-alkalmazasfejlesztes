package com.jobboard.jobs.infrastructure.persistence.mapper;

import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import com.jobboard.jobs.domain.model.JobType;
import com.jobboard.jobs.infrastructure.persistence.entity.JobJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class JobPersistenceMapper {

    public Job toDomain(JobJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Job(
                entity.getId(),
                entity.getTitle(),
                entity.getCompany(),
                JobType.fromString(entity.getType()),
                entity.getLocation(),
                entity.getBadge(),
                entity.getSalary(),
                entity.getDescription(),
                entity.getRequirements(),
                entity.getPostedAt(),
                entity.getStatus() != null ? entity.getStatus() : JobStatus.ACTIVE,
                entity.getCreatedByUserId(),
                entity.getSkills()
        );
    }

    public JobJpaEntity toJpaEntity(Job domain) {
        if (domain == null) {
            return null;
        }
        return new JobJpaEntity(
                domain.getId(),
                domain.getTitle(),
                domain.getCompany(),
                domain.getType() != null ? domain.getType().getDisplayName() : null,
                domain.getLocation(),
                domain.getBadge(),
                domain.getSalary(),
                domain.getDescription(),
                domain.getRequirements(),
                domain.getPostedAt(),
                domain.getStatus() != null ? domain.getStatus() : JobStatus.ACTIVE,
                domain.getCreatedByUserId(),
                domain.getSkills()
        );
    }
}
