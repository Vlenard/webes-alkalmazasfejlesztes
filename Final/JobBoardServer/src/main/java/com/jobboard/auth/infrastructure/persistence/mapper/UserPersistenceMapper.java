package com.jobboard.auth.infrastructure.persistence.mapper;

import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Maps between User domain model and UserJpaEntity.
 */
@Component
public class UserPersistenceMapper {

    public User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getCompanyName(),
                entity.getCreatedAt(),
                entity.getSkills()
        );
    }

    public UserJpaEntity toJpaEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserJpaEntity(
                domain.getId(),
                domain.getName(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.getRole(),
                domain.getCompanyName(),
                domain.getCreatedAt(),
                domain.getSkills()
        );
    }
}
