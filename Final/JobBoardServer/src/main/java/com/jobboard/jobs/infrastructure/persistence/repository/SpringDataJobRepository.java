package com.jobboard.jobs.infrastructure.persistence.repository;

import com.jobboard.jobs.infrastructure.persistence.entity.JobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataJobRepository extends JpaRepository<JobJpaEntity, Long> {

    @Query("""
        SELECT j FROM JobJpaEntity j
        WHERE j.status = com.jobboard.jobs.domain.model.JobStatus.ACTIVE
          AND (:search IS NULL OR :search = ''
               OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(j.company) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:badge IS NULL OR :badge = '' OR LOWER(j.badge) = LOWER(:badge))
        ORDER BY j.postedAt DESC, j.id DESC
    """)
    List<JobJpaEntity> searchActiveJobs(@Param("search") String search, @Param("badge") String badge);

    @Query("""
        SELECT j FROM JobJpaEntity j
        WHERE j.createdByUserId = :createdByUserId
        ORDER BY j.postedAt DESC, j.id DESC
    """)
    List<JobJpaEntity> findByCreatedByUserId(@Param("createdByUserId") Long createdByUserId);

    @Query("""
        SELECT j FROM JobJpaEntity j
        WHERE j.createdByUserId = :createdByUserId
           OR (:companyName IS NOT NULL AND :companyName != '' AND LOWER(TRIM(j.company)) = LOWER(TRIM(:companyName)))
        ORDER BY j.postedAt DESC, j.id DESC
    """)
    List<JobJpaEntity> findByCreatorOrCompanyName(
            @Param("createdByUserId") Long createdByUserId,
            @Param("companyName") String companyName
    );
}
