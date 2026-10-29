package com.jobboard.auth.application.service;

import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.in.LoginUserUseCase;
import com.jobboard.auth.application.port.in.RegisterUserUseCase;
import com.jobboard.auth.application.port.in.UpdateUserProfileUseCase;
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

    @Test
    @DisplayName("Sikeres profilmódosítás új névvel és cégnévvel, változatlan szerepkörrel")
    void shouldUpdateProfileSuccessfully() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenServicePort.generateToken(any(User.class))).thenReturn("refreshed.jwt.token");

        UpdateUserProfileUseCase.UpdateProfileCommand command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Módosított Név",
                "janos@example.com",
                "Új Cég Kft.",
                null,
                null
        );

        RegisterUserUseCase.AuthResult result = authService.updateProfile(1L, command);

        assertThat(result.token()).isEqualTo("refreshed.jwt.token");
        assertThat(result.user().getName()).isEqualTo("Módosított Név");
        assertThat(result.user().getEmail()).isEqualTo("janos@example.com");
        assertThat(result.user().getCompanyName()).isEqualTo("Új Cég Kft.");
        assertThat(result.user().getRole()).isEqualTo(sampleUser.getRole()); // Preserved!
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("Sikeres profilmódosítás új skillekkel")
    void shouldUpdateProfileWithSkillsSuccessfully() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenServicePort.generateToken(any(User.class))).thenReturn("refreshed.jwt.token");

        UpdateUserProfileUseCase.UpdateProfileCommand command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Módosított Név",
                "janos@example.com",
                null,
                null,
                null,
                java.util.List.of("React", "TypeScript", "Tailwind CSS")
        );

        RegisterUserUseCase.AuthResult result = authService.updateProfile(1L, command);

        assertThat(result.user().getSkills()).containsExactly("React", "TypeScript", "Tailwind CSS");
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile: skillek normalizálása és deduplikálása profilmódosítás során")
    void shouldNormalizeAndDeduplicateSkillsDuringUpdateProfile() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenServicePort.generateToken(any(User.class))).thenReturn("refreshed.jwt.token");

        UpdateUserProfileUseCase.UpdateProfileCommand command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Teszt Elek",
                "elek@example.com",
                null,
                null,
                null,
                java.util.List.of("  Java  ", "java", "  ", "SPRING Boot", "spring boot", "Docker")
        );

        RegisterUserUseCase.AuthResult result = authService.updateProfile(1L, command);

        assertThat(result.user().getSkills()).containsExactly("Java", "SPRING Boot", "Docker");
    }

    @Test
    @DisplayName("updateProfile: null skillek megőrzik a meglévő skilleket, üres lista törli őket")
    void shouldPreserveOrClearSkillsOnUpdateProfile() {
        User userWithSkills = User.createNew("Teszt Elek", "elek@example.com", "hash", UserRole.APPLICANT, null, java.util.List.of("Java", "Kotlin"));
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(userWithSkills));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenServicePort.generateToken(any(User.class))).thenReturn("refreshed.jwt.token");

        // 1. null skillek esetén meglévő skillek megmaradnak
        UpdateUserProfileUseCase.UpdateProfileCommand commandKeep = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Teszt Elek",
                "elek@example.com",
                null,
                null,
                null,
                null
        );
        RegisterUserUseCase.AuthResult resultKeep = authService.updateProfile(1L, commandKeep);
        assertThat(resultKeep.user().getSkills()).containsExactly("Java", "Kotlin");

        // 2. üres lista esetén kiürül a skillek listája
        UpdateUserProfileUseCase.UpdateProfileCommand commandClear = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Teszt Elek",
                "elek@example.com",
                null,
                null,
                null,
                java.util.List.of()
        );
        RegisterUserUseCase.AuthResult resultClear = authService.updateProfile(1L, commandClear);
        assertThat(resultClear.user().getSkills()).isEmpty();
    }

    @Test
    @DisplayName("Sikeres jelszócsere helyes jelenlegi jelszóval")
    void shouldChangePasswordSuccessfully() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoderPort.matches("currentPass123", sampleUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoderPort.encode("newSecretPass")).thenReturn("newHashedPassword");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenServicePort.generateToken(any(User.class))).thenReturn("new.token");

        UpdateUserProfileUseCase.UpdateProfileCommand command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Kiss János",
                "janos@example.com",
                null,
                "currentPass123",
                "newSecretPass"
        );

        RegisterUserUseCase.AuthResult result = authService.updateProfile(1L, command);

        assertThat(result.user().getPasswordHash()).isEqualTo("newHashedPassword");
        assertThat(result.user().getRole()).isEqualTo(sampleUser.getRole());
    }

    @Test
    @DisplayName("Jelszócsere hiba esetén, ha a jelenlegi jelszó hibás")
    void shouldThrowExceptionWhenCurrentPasswordIsInvalid() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoderPort.matches("wrongPass", sampleUser.getPasswordHash())).thenReturn(false);

        UpdateUserProfileUseCase.UpdateProfileCommand command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Kiss János",
                "janos@example.com",
                null,
                "wrongPass",
                "newSecretPass"
        );

        assertThatThrownBy(() -> authService.updateProfile(1L, command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("A jelenlegi jelszó helytelen.");
    }

    @Test
    @DisplayName("Jelszócsere hiba, ha az új jelszó túl rövid")
    void shouldThrowExceptionWhenNewPasswordIsTooShort() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoderPort.matches("currentPass123", sampleUser.getPasswordHash())).thenReturn(true);

        UpdateUserProfileUseCase.UpdateProfileCommand command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Kiss János",
                "janos@example.com",
                null,
                "currentPass123",
                "12345"
        );

        assertThatThrownBy(() -> authService.updateProfile(1L, command))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("legalább 6 karakter");
    }

    @Test
    @DisplayName("Profilmódosítás hiba, ha a megadott e-mail címmel már másik felhasználó rendelkezik")
    void shouldThrowExceptionWhenEmailAlreadyExistsForAnotherUser() {
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));
        User otherUser = new User(2L, "Másik User", "masik@example.com", "hash", UserRole.APPLICANT, null, Instant.now());
        when(userRepositoryPort.findByEmail("masik@example.com")).thenReturn(Optional.of(otherUser));

        UpdateUserProfileUseCase.UpdateProfileCommand command = new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Kiss János",
                "masik@example.com",
                null,
                null,
                null
        );

        assertThatThrownBy(() -> authService.updateProfile(1L, command))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("már regisztráltak");
    }
}
