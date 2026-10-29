package com.jobboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.applications.infrastructure.web.dto.UpdateApplicationStatusRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobStatusRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SeededUsersAndFeaturesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Seeder Teszt: varadi1@gmail.com és Start123 bejelentkezés, és jelentkezések állapota (ACCEPTED, REJECTED, PENDING)")
    void testVaradi1ApplicantWorkflow() throws Exception {
        // 1. Bejelentkezés mint varadi1@gmail.com Start123 jelszóval
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "varadi1@gmail.com",
                                "password", "Start123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("varadi1@gmail.com"))
                .andExpect(jsonPath("$.user.role").value("APPLICANT"))
                .andReturn();

        String token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).get("token").asText();

        // 2. /api/applications/my lekérése
        MvcResult myAppsResult = mockMvc.perform(get("/api/applications/my")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode apps = objectMapper.readTree(myAppsResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(apps.isArray()).isTrue();
        assertThat(apps.size()).isGreaterThanOrEqualTo(3);

        boolean hasAccepted = false;
        boolean hasRejected = false;
        boolean hasPending = false;

        for (JsonNode app : apps) {
            String appStatus = app.get("status").asText();
            String statusMsg = app.get("statusMessage").asText();
            if ("ACCEPTED".equals(appStatus)) {
                hasAccepted = true;
                assertThat(statusMsg).isEqualTo("A cég értesíti Önt a továbbiakról.");
            } else if ("REJECTED".equals(appStatus)) {
                hasRejected = true;
                assertThat(statusMsg).isEqualTo("A jelentkezését elutasították.");
            } else if ("PENDING".equals(appStatus)) {
                hasPending = true;
                assertThat(statusMsg).isEqualTo("Jelentkezése elbírálás alatt áll.");
            }
        }

        assertThat(hasAccepted).as("Lennie kell ACCEPTED státuszú jelentkezésnek").isTrue();
        assertThat(hasRejected).as("Lennie kell REJECTED státuszú jelentkezésnek").isTrue();
        assertThat(hasPending).as("Lennie kell PENDING státuszú jelentkezésnek").isTrue();
    }

    @Test
    @DisplayName("Seeder Teszt: varadi2@gmail.com és Start123 bejelentkezés, saját hirdetések, jelentkezők kezelése és CV letöltés")
    void testVaradi2EmployerWorkflow() throws Exception {
        // 1. Bejelentkezés mint varadi2@gmail.com Start123 jelszóval
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "varadi2@gmail.com",
                                "password", "Start123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("varadi2@gmail.com"))
                .andExpect(jsonPath("$.user.role").value("COMPANY_MANAGER"))
                .andExpect(jsonPath("$.user.companyName").value("TechCorp Hungary"))
                .andReturn();

        String token = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).get("token").asText();

        // 2. Saját hirdetések lekérése (GET /api/jobs/my)
        MvcResult myJobsResult = mockMvc.perform(get("/api/jobs/my")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode myJobs = objectMapper.readTree(myJobsResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(myJobs.isArray()).isTrue();
        assertThat(myJobs.size()).isEqualTo(7); // 5 aktív + 2 lezárt TechCorp állás

        // Ellenőrizzük, hogy mindegyik TechCorp Hungary céghez tartozik
        Long targetJobIdWithCandidates = null;
        Long closedJobId = null;

        for (JsonNode job : myJobs) {
            assertThat(job.get("company").asText()).isEqualTo("TechCorp Hungary");
            if ("Senior Backend Fejlesztő (Java / Spring Boot)".equals(job.get("title").asText())) {
                targetJobIdWithCandidates = job.get("id").asLong();
            }
            if ("CLOSED".equals(job.get("status").asText())) {
                closedJobId = job.get("id").asLong();
            }
        }

        assertThat(targetJobIdWithCandidates).isNotNull();
        assertThat(closedJobId).isNotNull();

        // 3. Jelentkezők lekérése az álláshoz (GET /api/jobs/{id}/applications)
        MvcResult appsResult = mockMvc.perform(get("/api/jobs/" + targetJobIdWithCandidates + "/applications")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode applications = objectMapper.readTree(appsResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(applications.size()).isGreaterThanOrEqualTo(3);

        Long pendingAppId = null;
        for (JsonNode app : applications) {
            if ("PENDING".equals(app.get("status").asText())) {
                pendingAppId = app.get("id").asLong();
            }
        }
        assertThat(pendingAppId).isNotNull();

        // 4. Önéletrajz letöltése (GET /api/applications/{id}/resume)
        mockMvc.perform(get("/api/applications/" + pendingAppId + "/resume")
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().contentType("application/pdf"));

        // 5. Jelentkezés elbírálása (PATCH /api/applications/{id}/status -> ACCEPTED)
        mockMvc.perform(patch("/api/applications/" + pendingAppId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateApplicationStatusRequestDto("ACCEPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 6. Álláshirdetés újranyitása (CLOSED -> ACTIVE)
        mockMvc.perform(patch("/api/jobs/" + closedJobId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateJobStatusRequestDto("ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Seeder Teszt: További jelentkezők mind be tudnak lépni Start123 jelszóval")
    void testAdditionalSampleApplicants() throws Exception {
        List<String> emails = List.of(
                "kovacs.anna@gmail.com",
                "nagy.peter@gmail.com",
                "toth.eszter@gmail.com",
                "szabo.david@gmail.com",
                "kiss.gabor@gmail.com"
        );

        for (String email : emails) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "email", email,
                                    "password", "Start123"
                            ))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user.email").value(email))
                    .andExpect(jsonPath("$.user.role").value("APPLICANT"));
        }
    }

    @Test
    @DisplayName("Seeder Teszt: varadi1 skillekkel rendelkezik, az álláslista illeszkedés szerint rendezett és varadi2 skilleket tud szerkeszteni")
    void testRecommendationAndSkillsWorkflow() throws Exception {
        // 1. Bejelentkezés mint varadi1 (APPLICANT)
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "varadi1@gmail.com",
                                "password", "Start123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.skills").isArray())
                .andExpect(jsonPath("$.user.skills[0]").value("React ismeret"))
                .andReturn();

        String applicantToken = "Bearer " + objectMapper.readTree(loginResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).get("token").asText();

        // 2. GET /api/jobs bejelentkezett APPLICANT-ként: a lista rendezett matchScore szerint csökkenő sorrendben
        MvcResult jobsResult = mockMvc.perform(get("/api/jobs")
                        .header(HttpHeaders.AUTHORIZATION, applicantToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jobsArray = objectMapper.readTree(jobsResult.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(jobsArray.isArray()).isTrue();
        assertThat(jobsArray.size()).isGreaterThanOrEqualTo(8); // aktív állások

        // Az első munka a legmagasabb pontszámú (React Frontend munka)
        JsonNode firstJob = jobsArray.get(0);
        assertThat(firstJob.get("matchScore").asInt()).isGreaterThanOrEqualTo(50);
        assertThat(firstJob.get("recommended").asBoolean()).isTrue();
        assertThat(firstJob.get("matchedSkills").size()).isGreaterThan(0);

        // Az utolsó munka pontszáma kisebb vagy egyenlő
        JsonNode lastJob = jobsArray.get(jobsArray.size() - 1);
        assertThat(lastJob.get("matchScore").asInt()).isLessThanOrEqualTo(firstJob.get("matchScore").asInt());

        // 3. GET /api/jobs/{id} APPLICANT-ként
        Long firstJobId = firstJob.get("id").asLong();
        mockMvc.perform(get("/api/jobs/" + firstJobId)
                        .header(HttpHeaders.AUTHORIZATION, applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstJobId))
                .andExpect(jsonPath("$.matchScore").isNumber())
                .andExpect(jsonPath("$.matchedSkills").isArray())
                .andExpect(jsonPath("$.missingSkills").isArray())
                .andExpect(jsonPath("$.recommended").isBoolean());

        // 4. Bejelentkezés mint varadi2 (COMPANY_MANAGER)
        MvcResult managerLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "varadi2@gmail.com",
                                "password", "Start123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        String managerToken = "Bearer " + objectMapper.readTree(managerLogin.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).get("token").asText();

        // 5. PATCH /api/jobs/{id}/skills céges kezelőként
        mockMvc.perform(patch("/api/jobs/" + firstJobId + "/skills")
                        .header(HttpHeaders.AUTHORIZATION, managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("skills", List.of("React", "TypeScript", "Tailwind CSS", "Next.js")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(firstJobId))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills[3]").value("Next.js"));
    }
}

