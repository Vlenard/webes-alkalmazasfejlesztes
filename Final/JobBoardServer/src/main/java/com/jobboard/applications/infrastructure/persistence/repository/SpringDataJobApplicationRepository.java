package com.jobboard.applications.infrastructure.persistence.repository;

import com.jobboard.applications.infrastructure.persistence.entity.JobApplicationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataJobApplicationRepository extends JpaRepository<JobApplicationJpaEntity, Long> {

    List<JobApplicationJpaEntity> findByJobIdOrderBySubmittedAtDesc(Long jobId);

    boolean existsByJobIdAndApplicantUserId(Long jobId, Long applicantUserId);

    boolean existsByJobIdAndApplicantEmailIgnoreCase(Long jobId, String applicantEmail);

    @Query("""
        SELECT a FROM JobApplicationJpaEntity a
        WHERE (:userId IS NOT NULL AND a.applicantUserId = :userId)
           OR (:email IS NOT NULL AND LOWER(a.applicantEmail) = LOWER(:email))
        ORDER BY a.submittedAt DESC
    """)
    List<JobApplicationJpaEntity> findByApplicant(@Param("userId") Long userId, @Param("email") String email);
}
