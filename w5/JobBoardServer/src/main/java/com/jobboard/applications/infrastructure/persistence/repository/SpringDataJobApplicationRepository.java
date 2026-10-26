package com.jobboard.applications.infrastructure.persistence.repository;

import com.jobboard.applications.infrastructure.persistence.entity.JobApplicationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataJobApplicationRepository extends JpaRepository<JobApplicationJpaEntity, Long> {
    List<JobApplicationJpaEntity> findByJobIdOrderBySubmittedAtDesc(Long jobId);
}
