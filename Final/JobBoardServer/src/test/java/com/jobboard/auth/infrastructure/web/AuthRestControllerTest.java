package com.jobboard.auth.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.in.LoginUserUseCase;
import com.jobboard.auth.application.port.in.RegisterUserUseCase;
import com.jobboard.auth.application.port.in.UpdateUserProfileUseCase;
import com.jobboard.auth.domain.exception.InvalidCredentialsException;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.exception.UserAlreadyExistsException;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;
import com.jobboard.auth.infrastructure.web.dto.LoginRequestDto;
import com.jobboard.auth.infrastructure.web.dto.RegisterRequestDto;
import com.jobboard.auth.infrastructure.web.dto.UpdateProfileRequestDto;
import com.jobboard.auth.infrastructure.web.mapper.AuthWebMapper;
import com.jobboard.shared.infrastructure.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthRestController.class)
@Import({AuthWebMapper.class, GlobalExceptionHandler.class})
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterUserUseCase registerUserUseCase;

    @MockBean
    private LoginUserUseCase loginUserUseCase;

    @MockBean
    private AuthenticateTokenUseCase authenticateTokenUseCase;

    @MockBean
    private UpdateUserProfileUseCase updateUserProfileUseCase;

    @Test
    @DisplayName("POST /api/auth/register sikeres regisztráció 201 CREATED")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "Kiss János",
                "kiss.janos@example.com",
                "password123",
                UserRole.APPLICANT,
                null
        );

        User createdUser = new User(1L, request.name(), request.email(), "hash", request.role(), null, Instant.now());
        RegisterUserUseCase.AuthResult authResult = new RegisterUserUseCase.AuthResult("fake.jwt.token", createdUser);

        when(registerUserUseCase.register(any())).thenReturn(authResult);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.name").value("Kiss János"))
                .andExpect(jsonPath("$.user.role").value("APPLICANT"));
    }

    @Test
    @DisplayName("POST /api/auth/register 400 Bad Request ha hiányzik a név vagy rövid a jelszó")
    void shouldReturn400WhenRegisterValidationFails() throws Exception {
        RegisterRequestDto invalid = new RegisterRequestDto(
                "",
                "invalid-email",
                "123",
                UserRole.APPLICANT,
                null
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register 409 Conflict ha az e-mail már létezik")
    void shouldReturn409WhenUserAlreadyExists() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "Kiss János",
                "duplicate@example.com",
                "password123",
                UserRole.APPLICANT,
                null
        );

        when(registerUserUseCase.register(any()))
                .thenThrow(new UserAlreadyExistsException("A megadott e-mail címmel már regisztráltak: duplicate@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A megadott e-mail címmel már regisztráltak: duplicate@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login sikeres bejelentkezés 200 OK")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDto request = new LoginRequestDto("kiss.janos@example.com", "password123");
        User user = new User(1L, "Kiss János", request.email(), "hash", UserRole.APPLICANT, null, Instant.now());
        RegisterUserUseCase.AuthResult authResult = new RegisterUserUseCase.AuthResult("login.jwt.token", user);

        when(loginUserUseCase.login(any())).thenReturn(authResult);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login.jwt.token"))
                .andExpect(jsonPath("$.user.name").value("Kiss János"));
    }

    @Test
    @DisplayName("POST /api/auth/login 401 Unauthorized hibás adatok esetén")
    void shouldReturn401WhenInvalidCredentials() throws Exception {
        LoginRequestDto request = new LoginRequestDto("wrong@example.com", "badpassword");

        when(loginUserUseCase.login(any())).thenThrow(new InvalidCredentialsException("Hibás e-mail cím vagy jelszó."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Hibás e-mail cím vagy jelszó."));
    }

    @Test
    @DisplayName("GET /api/auth/me visszaadja a bejelentkezett felhasználót 200 OK")
    void shouldReturnCurrentUser() throws Exception {
        AuthenticateTokenUseCase.AuthenticatedUser user = new AuthenticateTokenUseCase.AuthenticatedUser(
                42L,
                "Manager Béla",
                "bela@company.com",
                UserRole.COMPANY_MANAGER,
                "Béla Corp"
        );

        when(authenticateTokenUseCase.authenticate("Bearer valid.jwt.token")).thenReturn(user);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("Manager Béla"))
                .andExpect(jsonPath("$.role").value("COMPANY_MANAGER"))
                .andExpect(jsonPath("$.companyName").value("Béla Corp"));
    }

    @Test
    @DisplayName("GET /api/auth/me 403 Forbidden érvénytelen token esetén")
    void shouldReturn403WhenUnauthorized() throws Exception {
        when(authenticateTokenUseCase.authenticate(any()))
                .thenThrow(new UnauthorizedAccessException("Hiányzó vagy érvénytelen Authorization fejléc."));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Hiányzó vagy érvénytelen Authorization fejléc."));
    }

    @Test
    @DisplayName("PUT /api/auth/profile 200 OK sikeres profilfrissítés")
    void shouldUpdateProfileSuccessfully() throws Exception {
        AuthenticateTokenUseCase.AuthenticatedUser authUser =
                new AuthenticateTokenUseCase.AuthenticatedUser(42L, "Régi Név", "regi@example.com", UserRole.APPLICANT, null);
        when(authenticateTokenUseCase.authenticate("Bearer valid.token")).thenReturn(authUser);

        UpdateProfileRequestDto request = new UpdateProfileRequestDto(
                "Új Név",
                "uj@example.com",
                "Új Cég Kft.",
                "currentPass",
                "newSecretPass"
        );

        User updatedUser = new User(42L, "Új Név", "uj@example.com", "new_hash", UserRole.APPLICANT, "Új Cég Kft.", Instant.now());
        RegisterUserUseCase.AuthResult authResult = new RegisterUserUseCase.AuthResult("refreshed.token", updatedUser);
        when(updateUserProfileUseCase.updateProfile(42L, new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Új Név", "uj@example.com", "Új Cég Kft.", "currentPass", "newSecretPass"
        ))).thenReturn(authResult);

        mockMvc.perform(put("/api/auth/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("refreshed.token"))
                .andExpect(jsonPath("$.user.name").value("Új Név"))
                .andExpect(jsonPath("$.user.email").value("uj@example.com"))
                .andExpect(jsonPath("$.user.role").value("APPLICANT"))
                .andExpect(jsonPath("$.user.companyName").value("Új Cég Kft."));
    }

    @Test
    @DisplayName("PUT /api/auth/profile 400 Bad Request érvénytelen adatok esetén")
    void shouldReturn400WhenUpdateProfileRequestIsInvalid() throws Exception {
        UpdateProfileRequestDto invalidRequest = new UpdateProfileRequestDto(
                "",
                "invalid-email",
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/auth/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/auth/profile kezeli a skills listát a kérésben")
    void shouldUpdateProfileWithSkillsSuccessfully() throws Exception {
        AuthenticateTokenUseCase.AuthenticatedUser authUser =
                new AuthenticateTokenUseCase.AuthenticatedUser(42L, "Régi Név", "regi@example.com", UserRole.APPLICANT, null);
        when(authenticateTokenUseCase.authenticate("Bearer valid.token")).thenReturn(authUser);

        java.util.List<String> skills = java.util.List.of("React", "TypeScript", "Tailwind CSS");
        UpdateProfileRequestDto request = new UpdateProfileRequestDto(
                "Új Név",
                "uj@example.com",
                null,
                null,
                null,
                skills
        );

        User updatedUser = new User(42L, "Új Név", "uj@example.com", "hash", UserRole.APPLICANT, null, Instant.now(), skills);
        RegisterUserUseCase.AuthResult authResult = new RegisterUserUseCase.AuthResult("refreshed.token", updatedUser);
        when(updateUserProfileUseCase.updateProfile(42L, new UpdateUserProfileUseCase.UpdateProfileCommand(
                "Új Név", "uj@example.com", null, null, null, skills
        ))).thenReturn(authResult);

        mockMvc.perform(put("/api/auth/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.skills[0]").value("React"))
                .andExpect(jsonPath("$.user.skills[1]").value("TypeScript"))
                .andExpect(jsonPath("$.user.skills[2]").value("Tailwind CSS"));
    }

    @Test
    @DisplayName("GET /api/auth/me visszaadja a skilleket a tokenből")
    void shouldReturnCurrentUserWithSkills() throws Exception {
        AuthenticateTokenUseCase.AuthenticatedUser user = new AuthenticateTokenUseCase.AuthenticatedUser(
                42L,
                "Jelentkező Béla",
                "bela@applicant.com",
                UserRole.APPLICANT,
                null,
                java.util.List.of("Java", "Spring Boot")
        );

        when(authenticateTokenUseCase.authenticate("Bearer valid.jwt.token")).thenReturn(user);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer valid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.skills[0]").value("Java"))
                .andExpect(jsonPath("$.skills[1]").value("Spring Boot"));
    }
}
