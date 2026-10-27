package com.jobboard.applications.infrastructure.persistence.adapter;

import com.jobboard.applications.application.port.out.ApplicationRepositoryPort;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.persistence.entity.JobApplicationJpaEntity;
import com.jobboard.applications.infrastructure.persistence.mapper.ApplicationPersistenceMapper;
import com.jobboard.applications.infrastructure.persistence.repository.SpringDataJobApplicationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ApplicationJpaAdapter implements ApplicationRepositoryPort {

    private final SpringDataJobApplicationRepository repository;
    private final ApplicationPersistenceMapper mapper;

    public ApplicationJpaAdapter(SpringDataJobApplicationRepository repository,
                                 ApplicationPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public JobApplication save(JobApplication application) {
        JobApplicationJpaEntity entityToSave = mapper.toJpaEntity(application);
        JobApplicationJpaEntity saved = repository.save(entityToSave);
        return mapper.toDomain(saved);
    }

    @Override
    public List<JobApplication> findByJobId(Long jobId) {
        return repository.findByJobIdOrderBySubmittedAtDesc(jobId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<JobApplication> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<JobApplication> findByApplicant(Long applicantUserId, String applicantEmail) {
        return repository.findByApplicant(applicantUserId, applicantEmail).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByJobIdAndApplicant(Long jobId, Long applicantUserId, String applicantEmail) {
        if (applicantUserId != null && repository.existsByJobIdAndApplicantUserId(jobId, applicantUserId)) {
            return true;
        }
        if (applicantEmail != null && !applicantEmail.isBlank()
                && repository.existsByJobIdAndApplicantEmailIgnoreCase(jobId, applicantEmail.trim())) {
            return true;
        }
        return false;
    }

    @Override
    public Optional<JobApplication> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public long count() {
        return repository.count();
    }
}
