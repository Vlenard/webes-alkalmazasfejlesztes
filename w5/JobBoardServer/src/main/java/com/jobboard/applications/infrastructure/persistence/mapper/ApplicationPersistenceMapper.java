package com.jobboard.applications.infrastructure.persistence.mapper;

import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.persistence.entity.JobApplicationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ApplicationPersistenceMapper {

    public JobApplication toDomain(JobApplicationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new JobApplication(
                entity.getId(),
                entity.getJobId(),
                entity.getApplicantName(),
                entity.getApplicantEmail(),
                entity.getResumeFileName(),
                entity.getSubmittedAt()
        );
    }

    public JobApplicationJpaEntity toJpaEntity(JobApplication domain) {
        if (domain == null) {
            return null;
        }
        return new JobApplicationJpaEntity(
                domain.getId(),
                domain.getJobId(),
                domain.getApplicantName(),
                domain.getApplicantEmail(),
                domain.getResumeFileName(),
                domain.getSubmittedAt()
        );
    }
}
