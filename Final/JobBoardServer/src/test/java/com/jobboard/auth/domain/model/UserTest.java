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

    @Test
    @DisplayName("Skillek normalizálása: null kezelése, trimmelés, üres szűrés és case-insensitive deduplikáció")
    void shouldNormalizeTrimAndDeduplicateSkillsCaseInsensitive() {
        java.util.List<String> rawSkills = java.util.List.of("  Java  ", "java", "  ", "SPRING Boot", "spring boot", "Docker");
        User user = User.createNew("János", "janos@example.com", "hash", UserRole.APPLICANT, null, rawSkills);

        assertThat(user.getSkills()).containsExactly("Java", "SPRING Boot", "Docker");
    }

    @Test
    @DisplayName("7-paraméteres konstruktor és createNew üres skills listát inicializál")
    void shouldDefaultSkillsToEmptyListInBackwardCompatibleConstructors() {
        User user7 = new User(1L, "János", "janos@example.com", "hash", UserRole.APPLICANT, null, Instant.now());
        assertThat(user7.getSkills()).isEmpty();

        User userCreate = User.createNew("János", "janos@example.com", "hash", UserRole.APPLICANT, null);
        assertThat(userCreate.getSkills()).isEmpty();
    }

    @Test
    @DisplayName("withId megőrzi a meglévő skilleket")
    void shouldPreserveSkillsWithId() {
        User user = User.createNew("János", "janos@example.com", "hash", UserRole.APPLICANT, null, java.util.List.of("Java", "Spring"));
        User userWithId = user.withId(99L);

        assertThat(userWithId.getId()).isEqualTo(99L);
        assertThat(userWithId.getSkills()).containsExactly("Java", "Spring");
    }

    @Test
    @DisplayName("withUpdatedProfile skillekkel és skillek nélkül helyesen frissít vagy megőrzi")
    void shouldHandleSkillsInWithUpdatedProfile() {
        User user = User.createNew("János", "janos@example.com", "hash", UserRole.APPLICANT, null, java.util.List.of("Java", "React"));

        // 4-paraméteres változat: megtartja a skilleket
        User updated4 = user.withUpdatedProfile("Új Név", "uj@example.com", null, null);
        assertThat(updated4.getSkills()).containsExactly("Java", "React");

        // 5-paraméteres változat null skillekkel: megtartja a skilleket
        User updated5Null = user.withUpdatedProfile("Új Név", "uj@example.com", null, null, null);
        assertThat(updated5Null.getSkills()).containsExactly("Java", "React");

        // 5-paraméteres változat új skillekkel: lecseréli és normalizálja
        User updated5New = user.withUpdatedProfile("Új Név", "uj@example.com", null, null, java.util.List.of(" TypeScript ", "TYPESCRIPT", "NestJS"));
        assertThat(updated5New.getSkills()).containsExactly("TypeScript", "NestJS");
    }

    @Test
    @DisplayName("withUpdatedProfile: skillek normalizálása, szóközök trimmelése, üres elemek kiszűrése és deduplikáció az első előfordulás megőrzésével")
    void shouldNormalizeAndDeduplicateSkillsInWithUpdatedProfile() {
        User user = User.createNew("János", "janos@example.com", "hash", UserRole.APPLICANT, null, java.util.List.of("Java"));

        java.util.List<String> rawSkills = java.util.List.of(
                "  React.js  ",
                "react.js",
                "REACT.JS",
                "   ",
                "",
                "\tSpring Boot\n",
                "spring boot",
                "SPRING BOOT",
                "Docker"
        );

        User updated = user.withUpdatedProfile(null, null, null, null, rawSkills);

        assertThat(updated.getSkills())
                .containsExactly("React.js", "Spring Boot", "Docker");
        assertThatThrownBy(() -> updated.getSkills().add("Rust"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("withUpdatedProfile: üres vagy csupa szóköz skillek esetén üres lista tárolódik el")
    void shouldHandleEmptyOrBlankSkillsInWithUpdatedProfile() {
        User user = User.createNew("János", "janos@example.com", "hash", UserRole.APPLICANT, null, java.util.List.of("Java", "React"));

        // Üres lista átadása: kiüríti a meglévő skilleket
        User emptyUpdated = user.withUpdatedProfile(null, null, null, null, java.util.List.of());
        assertThat(emptyUpdated.getSkills()).isEmpty();

        // Csupa szóköz lista átadása: kiüríti a skilleket
        User blankUpdated = user.withUpdatedProfile(null, null, null, null, java.util.List.of("   ", "", " \t "));
        assertThat(blankUpdated.getSkills()).isEmpty();
    }
}
