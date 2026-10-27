package com.jobboard.auth.domain.model;

import com.jobboard.auth.domain.exception.InvalidUserDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("Sikeres User példányosítás érvényes adatokkal")
    void shouldCreateUserSuccessfully() {
        User user = new User(
                1L,
                "Kiss János",
                "KISS.JANOS@EXAMPLE.COM",
                "hashedpassword",
                UserRole.APPLICANT,
                null,
                Instant.now()
        );

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("Kiss János");
        assertThat(user.getEmail()).isEqualTo("kiss.janos@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.APPLICANT);
        assertThat(user.isApplicant()).isTrue();
        assertThat(user.isCompanyManager()).isFalse();
        assertThat(user.getCompanyName()).isNull();
    }

    @Test
    @DisplayName("Cégvezető (COMPANY_MANAGER) cégnevének beállítása")
    void shouldSetCompanyNameForCompanyManager() {
        User user = User.createNew(
                "Nagy Éva",
                "eva@techcorp.hu",
                "secret_hash",
                UserRole.COMPANY_MANAGER,
                " TechCorp Hungary "
        );

        assertThat(user.getRole()).isEqualTo(UserRole.COMPANY_MANAGER);
        assertThat(user.isCompanyManager()).isTrue();
        assertThat(user.getCompanyName()).isEqualTo("TechCorp Hungary");
    }

    @Test
    @DisplayName("Üres név esetén InvalidUserDataException dobódik")
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> new User(null, "  ", "email@example.com", "hash", UserRole.APPLICANT, null, null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("A felhasználónév nem lehet üres.");
    }

    @Test
    @DisplayName("Érvénytelen vagy üres email esetén InvalidUserDataException dobódik")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThatThrownBy(() -> new User(null, "János", "  ", "hash", UserRole.APPLICANT, null, null))
                .isInstanceOf(InvalidUserDataException.class);

        assertThatThrownBy(() -> new User(null, "János", "no-at-sign", "hash", UserRole.APPLICANT, null, null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Érvényes e-mail cím");
    }

    @Test
    @DisplayName("Üres jelszó hash esetén InvalidUserDataException dobódik")
    void shouldThrowExceptionWhenPasswordHashIsBlank() {
        assertThatThrownBy(() -> new User(null, "János", "janos@example.com", "  ", UserRole.APPLICANT, null, null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("A jelszó hash nem lehet üres.");
    }

    @Test
    @DisplayName("Null szerepkör esetén InvalidUserDataException dobódik")
    void shouldThrowExceptionWhenRoleIsNull() {
        assertThatThrownBy(() -> new User(null, "János", "janos@example.com", "hash", null, null, null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("A szerepkör kiválasztása kötelező.");
    }

    @Test
    @DisplayName("withId új példányt hoz létre a megadott ID-val")
    void shouldCreateNewInstanceWithGivenId() {
        User user = User.createNew("Teszt Elek", "elek@example.com", "hash", UserRole.APPLICANT, null);
        assertThat(user.getId()).isNull();

        User userWithId = user.withId(42L);
        assertThat(userWithId.getId()).isEqualTo(42L);
        assertThat(userWithId.getName()).isEqualTo("Teszt Elek");
    }

    @Test
    @DisplayName("withUpdatedProfile sikeresen frissíti a felhasználó adatait, miközben a szerepkör változatlan marad")
    void shouldUpdateUserProfileSuccessfully() {
        User user = new User(10L, "Régi Név", "regi@example.com", "regi_hash", UserRole.APPLICANT, null, Instant.now());

        User updated = user.withUpdatedProfile("Új Név", "uj@example.com", "Új Cég Kft.", "uj_hash");

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getName()).isEqualTo("Új Név");
        assertThat(updated.getEmail()).isEqualTo("uj@example.com");
        assertThat(updated.getCompanyName()).isEqualTo("Új Cég Kft.");
        assertThat(updated.getRole()).isEqualTo(UserRole.APPLICANT); // Role remains APPLICANT!
        assertThat(updated.getPasswordHash()).isEqualTo("uj_hash");
    }

    @Test
    @DisplayName("withUpdatedProfile megtartja a meglévő értékeket, ha a paraméterek üresek vagy hiányoznak")
    void shouldRetainExistingValuesWhenParametersAreNullOrBlank() {
        User user = new User(10L, "Meglévő Név", "meglevo@example.com", "hash123", UserRole.APPLICANT, "Létező Cég", Instant.now());

        User updated = user.withUpdatedProfile(null, " ", null, null);

        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getName()).isEqualTo("Meglévő Név");
        assertThat(updated.getEmail()).isEqualTo("meglevo@example.com");
        assertThat(updated.getRole()).isEqualTo(UserRole.APPLICANT);
        assertThat(updated.getPasswordHash()).isEqualTo("hash123");
    }
}
