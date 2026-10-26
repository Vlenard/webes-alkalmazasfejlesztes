package com.jobboard.auth.infrastructure.persistence.adapter;

import com.jobboard.auth.application.port.out.UserRepositoryPort;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.jobboard.auth.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.jobboard.auth.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistence adapter implementing UserRepositoryPort using Spring Data JPA.
 */
@Component
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserPersistenceMapper mapper;

    public UserJpaAdapter(SpringDataUserRepository springDataUserRepository, UserPersistenceMapper mapper) {
        this.springDataUserRepository = springDataUserRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapper.toJpaEntity(user);
        UserJpaEntity saved = springDataUserRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public java.util.List<User> findAll() {
        return springDataUserRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return springDataUserRepository.findByEmail(email.trim().toLowerCase())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return springDataUserRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return springDataUserRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public long count() {
        return springDataUserRepository.count();
    }
}
