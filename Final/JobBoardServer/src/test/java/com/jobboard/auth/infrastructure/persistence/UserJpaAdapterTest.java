package com.jobboard.auth.infrastructure.persistence;

import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;
import com.jobboard.auth.infrastructure.persistence.adapter.UserJpaAdapter;
import com.jobboard.auth.infrastructure.persistence.mapper.UserPersistenceMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserJpaAdapter.class, UserPersistenceMapper.class})
class UserJpaAdapterTest {

    @Autowired
    private UserJpaAdapter userJpaAdapter;

    @Test
    @DisplayName("Felhasználó mentése és lekérdezése e-mail alapján")
    void shouldSaveAndFindUserByEmail() {
        User user = User.createNew(
                "Minta Károly",
                "karoly@example.com",
                "hashedpassword",
                UserRole.COMPANY_MANAGER,
                "Minta Kft."
        );

        User saved = userJpaAdapter.save(user);
        assertThat(saved.getId()).isNotNull();

        Optional<User> found = userJpaAdapter.findByEmail("karoly@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Minta Károly");
        assertThat(found.get().getRole()).isEqualTo(UserRole.COMPANY_MANAGER);
        assertThat(found.get().getCompanyName()).isEqualTo("Minta Kft.");

        assertThat(userJpaAdapter.existsByEmail("karoly@example.com")).isTrue();
        assertThat(userJpaAdapter.existsByEmail("other@example.com")).isFalse();
    }

    @Test
    @DisplayName("Felhasználó lekérdezése ID alapján")
    void shouldFindUserById() {
        User user = User.createNew("Jelentkező Anna", "anna@example.com", "hash", UserRole.APPLICANT, null);
        User saved = userJpaAdapter.save(user);

        Optional<User> found = userJpaAdapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isApplicant()).isTrue();
        assertThat(found.get().getCompanyName()).isNull();
    }

    @Test
    @DisplayName("Felhasználó skillekkel történő mentése és lekérdezése")
    void shouldSaveAndFindUserWithSkills() {
        User user = User.createNew(
                "Skill Elek",
                "elek.skills@example.com",
                "hash",
                UserRole.APPLICANT,
                null,
                java.util.List.of("Java", "Spring Boot", "Docker")
        );
        User saved = userJpaAdapter.save(user);

        Optional<User> found = userJpaAdapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSkills()).containsExactly("Java", "Spring Boot", "Docker");
    }
}
