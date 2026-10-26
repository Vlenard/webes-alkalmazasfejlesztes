package com.jobboard.jobs.application.port.out;

import com.jobboard.jobs.domain.model.Job;
import java.util.List;
import java.util.Optional;

public interface JobRepositoryPort {
    List<Job> findAll();

    List<Job> searchJobs(String search, String badge);

    Optional<Job> findById(Long id);

    Job save(Job job);

    boolean existsById(Long id);

    long count();
}
