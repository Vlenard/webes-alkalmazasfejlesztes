package com.jobboard.auth.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenAdapterTest {

    private JwtTokenAdapter jwtTokenAdapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jwtTokenAdapter = new JwtTokenAdapter(
                objectMapper,
                "TestSuperSecretSigningKeyForJobBoard2026AtLeast256BitsLong!",
                24
        );
    }

    @Test
    @DisplayName("Sikeres token generálás és visszafejtés")
    void shouldGenerateAndExtractTokenSuccessfully() {
        User user = new User(
                10L,
                "Kovács Béla",
                "kovacs.bela@example.com",
                "hash",
                UserRole.COMPANY_MANAGER,
                "MegaCorp",
                Instant.now()
        );

        String token = jwtTokenAdapter.generateToken(user);
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);

        AuthenticateTokenUseCase.AuthenticatedUser authenticatedUser = jwtTokenAdapter.verifyAndExtract(token);

        assertThat(authenticatedUser.id()).isEqualTo(10L);
        assertThat(authenticatedUser.name()).isEqualTo("Kovács Béla");
        assertThat(authenticatedUser.email()).isEqualTo("kovacs.bela@example.com");
        assertThat(authenticatedUser.role()).isEqualTo(UserRole.COMPANY_MANAGER);
        assertThat(authenticatedUser.companyName()).isEqualTo("MegaCorp");
        assertThat(authenticatedUser.isCompanyManager()).isTrue();
        assertThat(authenticatedUser.isApplicant()).isFalse();
    }

    @Test
    @DisplayName("Manipulált aláírás esetén UnauthorizedAccessException dobódik")
    void shouldThrowExceptionWhenSignatureIsTampered() {
        User user = User.createNew("János", "janos@test.com", "hash", UserRole.APPLICANT, null);
        String token = jwtTokenAdapter.generateToken(user);

        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".invalidSignaturePayload";

        assertThatThrownBy(() -> jwtTokenAdapter.verifyAndExtract(tamperedToken))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Érvénytelen JWT token aláírás");
    }

    @Test
    @DisplayName("Formátumhibás token esetén UnauthorizedAccessException dobódik")
    void shouldThrowExceptionForInvalidFormat() {
        assertThatThrownBy(() -> jwtTokenAdapter.verifyAndExtract("not.a.valid.jwt.token"))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Érvénytelen JWT token formátum");

        assertThatThrownBy(() -> jwtTokenAdapter.verifyAndExtract(null))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("Lejárt token esetén UnauthorizedAccessException dobódik")
    void shouldThrowExceptionWhenTokenIsExpired() {
        JwtTokenAdapter expiredAdapter = new JwtTokenAdapter(
                objectMapper,
                "TestSuperSecretSigningKeyForJobBoard2026AtLeast256BitsLong!",
                -1 // Expired 1 hour ago
        );

        User user = User.createNew("Lejárt", "expired@test.com", "hash", UserRole.APPLICANT, null);
        String token = expiredAdapter.generateToken(user);

        assertThatThrownBy(() -> jwtTokenAdapter.verifyAndExtract(token))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("A JWT token lejárt");
    }
}
