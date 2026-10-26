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
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(j.company) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:badge IS NULL OR :badge = '' OR LOWER(j.badge) = LOWER(:badge))
        ORDER BY j.postedAt DESC, j.id DESC
    """)
    List<JobJpaEntity> searchJobs(@Param("search") String search, @Param("badge") String badge);
}
