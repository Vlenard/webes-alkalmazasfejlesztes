package com.jobboard.auth.application.port.out;

import com.jobboard.auth.domain.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for User persistence.
 */
public interface UserRepositoryPort {

    User save(User user);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    long count();
}
