package com.jobboard.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.domain.model.UserRole;
import com.jobboard.auth.infrastructure.web.dto.AuthResponseDto;
import com.jobboard.auth.infrastructure.web.dto.LoginRequestDto;
import com.jobboard.auth.infrastructure.web.dto.RegisterRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Teljes end-to-end auth életciklus: regisztráció -> belépés -> /me profil lekérés")
    void fullAuthLifecycleTest() throws Exception {
        // 1. Regisztráció mint COMPANY_MANAGER
        RegisterRequestDto registerRequest = new RegisterRequestDto(
                "Nagy Zoltán",
                "zoltan@techcorp.hu",
                "securePass123",
                UserRole.COMPANY_MANAGER,
                "TechCorp Hungary"
        );

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.name").value("Nagy Zoltán"))
                .andExpect(jsonPath("$.user.email").value("zoltan@techcorp.hu"))
                .andExpect(jsonPath("$.user.role").value("COMPANY_MANAGER"))
                .andExpect(jsonPath("$.user.companyName").value("TechCorp Hungary"))
                .andReturn();

        AuthResponseDto registerAuth = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                AuthResponseDto.class
        );
        assertThat(registerAuth.token()).isNotBlank();

        // 2. Bejelentkezés a regisztrált adatokkal
        LoginRequestDto loginRequest = new LoginRequestDto("zoltan@techcorp.hu", "securePass123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("zoltan@techcorp.hu"))
                .andReturn();

        AuthResponseDto loginAuth = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                AuthResponseDto.class
        );
        assertThat(loginAuth.token()).isNotBlank();

        // 3. /me lekérdezése a kapott JWT Bearer tokennel
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginAuth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loginAuth.user().id()))
                .andExpect(jsonPath("$.name").value("Nagy Zoltán"))
                .andExpect(jsonPath("$.role").value("COMPANY_MANAGER"))
                .andExpect(jsonPath("$.companyName").value("TechCorp Hungary"));

        // 4. Újraregisztráció ugyanazzal az e-maillel -> 409 Conflict
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // 5. Bejelentkezés rossz jelszóval -> 401 Unauthorized
        LoginRequestDto badPasswordRequest = new LoginRequestDto("zoltan@techcorp.hu", "wrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badPasswordRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
