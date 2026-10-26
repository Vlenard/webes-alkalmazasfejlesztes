package com.jobboard.jobs.infrastructure.persistence.adapter;

import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.infrastructure.persistence.entity.JobJpaEntity;
import com.jobboard.jobs.infrastructure.persistence.mapper.JobPersistenceMapper;
import com.jobboard.jobs.infrastructure.persistence.repository.SpringDataJobRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JobJpaAdapter implements JobRepositoryPort {

    private final SpringDataJobRepository springDataJobRepository;
    private final JobPersistenceMapper jobPersistenceMapper;

    public JobJpaAdapter(SpringDataJobRepository springDataJobRepository,
                         JobPersistenceMapper jobPersistenceMapper) {
        this.springDataJobRepository = springDataJobRepository;
        this.jobPersistenceMapper = jobPersistenceMapper;
    }

    @Override
    public List<Job> findAll() {
        return springDataJobRepository.findAll().stream()
                .map(jobPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Job> searchJobs(String search, String badge) {
        return springDataJobRepository.searchActiveJobs(search, badge).stream()
                .map(jobPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Job> findByCreatedByUserId(Long createdByUserId) {
        return springDataJobRepository.findByCreatedByUserId(createdByUserId).stream()
                .map(jobPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Job> findByCreatedByUserIdOrCompanyName(Long createdByUserId, String companyName) {
        return springDataJobRepository.findByCreatorOrCompanyName(createdByUserId, companyName).stream()
                .map(jobPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Job> findById(Long id) {
        return springDataJobRepository.findById(id)
                .map(jobPersistenceMapper::toDomain);
    }

    @Override
    public Job save(Job job) {
        JobJpaEntity entityToSave = jobPersistenceMapper.toJpaEntity(job);
        JobJpaEntity savedEntity = springDataJobRepository.save(entityToSave);
        return jobPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataJobRepository.existsById(id);
    }

    @Override
    public long count() {
        return springDataJobRepository.count();
    }
}
