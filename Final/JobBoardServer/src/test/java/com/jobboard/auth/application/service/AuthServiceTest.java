package com.jobboard.auth.application.service;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.in.LoginUserUseCase;
import com.jobboard.auth.application.port.in.RegisterUserUseCase;
import com.jobboard.auth.application.port.out.PasswordEncoderPort;
import com.jobboard.auth.application.port.out.TokenServicePort;
import com.jobboard.auth.application.port.out.UserRepositoryPort;
import com.jobboard.auth.domain.exception.InvalidCredentialsException;
import com.jobboard.auth.domain.exception.InvalidUserDataException;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.exception.UserAlreadyExistsException;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private TokenServicePort tokenServicePort;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(
                1L,
                "Teszt Elek",
                "elek@example.com",
                "encoded_hash",
                UserRole.APPLICANT,
                null,
                Instant.now()
        );
    }

    @Test
    @DisplayName("Sikeres regisztráció folyamat")
    void shouldRegisterUserSuccessfully() {
        RegisterUserUseCase.RegisterCommand command = new RegisterUserUseCase.RegisterCommand(
                "Teszt Elek",
                "elek@example.com",
                "secret123",
                UserRole.APPLICANT,
                null
        );

        when(userRepositoryPort.existsByEmail("elek@example.com")).thenReturn(false);
        when(passwordEncoderPort.encode("secret123")).thenReturn("encoded_hash");
        when(userRepositoryPort.save(any(User.class))).thenReturn(sampleUser);
        when(tokenServicePort.generateToken(sampleUser)).thenReturn("sample.jwt.token");

        RegisterUserUseCase.AuthResult result = authService.register(command);

        assertThat(result.token()).isEqualTo("sample.jwt.token");
        assertThat(result.user().getName()).isEqualTo("Teszt Elek");
        assertThat(result.user().getEmail()).isEqualTo("elek@example.com");
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Foglalt e-mail esetén UserAlreadyExistsException dobódik")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterUserUseCase.RegisterCommand command = new RegisterUserUseCase.RegisterCommand(
                "Teszt Elek",
                "elek@example.com",
                "secret123",
                UserRole.APPLICANT,
                null
        );

        when(userRepositoryPort.existsByEmail("elek@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("már regisztráltak");
    }

    @Test
    @DisplayName("Rövid jelszó esetén InvalidUserDataException dobódik")
    void shouldThrowExceptionWhenPasswordIsTooShort() {
        RegisterUserUseCase.RegisterCommand command = new RegisterUserUseCase.RegisterCommand(
                "Teszt Elek",
                "elek@example.com",
                "12345",
                UserRole.APPLICANT,
                null
        );

        assertThatThrownBy(() -> authService.register(command))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("legalább 6 karakter");
    }

    @Test
    @DisplayName("Sikeres bejelentkezés")
    void shouldLoginSuccessfully() {
        LoginUserUseCase.LoginCommand command = new LoginUserUseCase.LoginCommand("elek@example.com", "secret123");

        when(userRepositoryPort.findByEmail("elek@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoderPort.matches("secret123", "encoded_hash")).thenReturn(true);
        when(tokenServicePort.generateToken(sampleUser)).thenReturn("sample.jwt.token");

        RegisterUserUseCase.AuthResult result = authService.login(command);

        assertThat(result.token()).isEqualTo("sample.jwt.token");
        assertThat(result.user().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Hibás jelszó esetén InvalidCredentialsException dobódik")
    void shouldThrowExceptionWhenPasswordDoesNotMatch() {
        LoginUserUseCase.LoginCommand command = new LoginUserUseCase.LoginCommand("elek@example.com", "wrongpassword");

        when(userRepositoryPort.findByEmail("elek@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoderPort.matches("wrongpassword", "encoded_hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Hibás e-mail cím vagy jelszó.");
    }

    @Test
    @DisplayName("Nem létező felhasználó esetén InvalidCredentialsException dobódik")
    void shouldThrowExceptionWhenUserNotFound() {
        LoginUserUseCase.LoginCommand command = new LoginUserUseCase.LoginCommand("nonexistent@example.com", "secret123");

        when(userRepositoryPort.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Sikeres kötelező token autentikáció")
    void shouldAuthenticateValidBearerToken() {
        AuthenticateTokenUseCase.AuthenticatedUser user = new AuthenticateTokenUseCase.AuthenticatedUser(
                1L, "Teszt Elek", "elek@example.com", UserRole.APPLICANT, null
        );
        when(tokenServicePort.verifyAndExtract("valid.token")).thenReturn(user);

        AuthenticateTokenUseCase.AuthenticatedUser result = authService.authenticate("Bearer valid.token");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.isApplicant()).isTrue();
    }

    @Test
    @DisplayName("Hiányzó vagy érvénytelen header esetén UnauthorizedAccessException dobódik")
    void shouldThrowExceptionWhenHeaderIsInvalid() {
        assertThatThrownBy(() -> authService.authenticate(null))
                .isInstanceOf(UnauthorizedAccessException.class);

        assertThatThrownBy(() -> authService.authenticate("Basic 12345"))
                .isInstanceOf(UnauthorizedAccessException.class);

        assertThatThrownBy(() -> authService.authenticate("Bearer   "))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("Opcionális autentikáció helyes viselkedése hibás vagy hiányzó fejléc esetén")
    void shouldReturnNullForOptionalAuthenticationWhenHeaderMissingOrInvalid() {
        assertThat(authService.authenticateOptional(null)).isNull();
        assertThat(authService.authenticateOptional("NotBearer token")).isNull();

        when(tokenServicePort.verifyAndExtract(anyString())).thenThrow(new UnauthorizedAccessException("Lejárt"));
        assertThat(authService.authenticateOptional("Bearer expired.token")).isNull();
    }
}
