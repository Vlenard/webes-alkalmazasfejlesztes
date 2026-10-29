package com.jobboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.domain.model.UserRole;
import com.jobboard.auth.infrastructure.web.dto.LoginRequestDto;
import com.jobboard.auth.infrastructure.web.dto.RegisterRequestDto;
import com.jobboard.auth.infrastructure.web.dto.UpdateProfileRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobSkillsRequestDto;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End integracios teszt osztaly a komplex allasajanlo rendszer ellenorzesere.
 * Lefedi:
 * 1. Jelentkezoi profil skillek beallitasa (PUT /api/auth/profile) es lekerdezese (GET /api/auth/me)
 * 2. Ceges kezelo hirdetesletrehozasa skillekkel (POST /api/jobs) es utolagos modositasa (PATCH /api/jobs/{id}/skills),
 *    valamint a jogosulatlan felhasznalok elutasitasa (403 Forbidden)
 * 3. Allasok lekérése jelentkezoi token alapjan (GET /api/jobs): pontszam szerinti rendezes, illeszkedo allasok elol,
 *    nem passzolo allasok hatrasorolasa
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RecommendationSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 1. Kovetelmeny:
     * Teszteld, hogy a jelentkezo (APPLICANT) a PUT /api/auth/profile vegponton meg tudja adni a skilljeit
     * (pl. ['React ismeret', 'TypeScript']), es a GET /api/auth/me visszaadja oket.
     */
    @Test
    @DisplayName("E2E: Jelentkezo (APPLICANT) a PUT /api/auth/profile-on skilleket allit be, amit a GET /api/auth/me visszad")
    void testApplicantCanUpdateSkillsAndRetrieveViaMeEndpoint() throws Exception {
        // 1. Jelentkezo bejelentkezese (seeded felhasznalo: varadi1@gmail.com / Start123)
        String applicantToken = loginAndGetToken("varadi1@gmail.com", "Start123");

        // 2. Skillek frissitese a PUT /api/auth/profile vegponton
        List<String> updatedSkills = List.of("React ismeret", "TypeScript");
        UpdateProfileRequestDto updateProfileDto = new UpdateProfileRequestDto(
                "Varadi 1 (Jelentkezo)",
                "varadi1@gmail.com",
                null,
                null,
                null,
                updatedSkills
        );

        MvcResult updateResult = mockMvc.perform(put("/api/auth/profile")
                        .header(HttpHeaders.AUTHORIZATION, applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("varadi1@gmail.com"))
                .andExpect(jsonPath("$.user.skills", hasSize(2)))
                .andExpect(jsonPath("$.user.skills", containsInAnyOrder("React ismeret", "TypeScript")))
                .andReturn();

        // Frissitett token kiolvasasa a profilvalaszbol
        JsonNode updateJson = objectMapper.readTree(updateResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        String refreshedToken = "Bearer " + updateJson.get("token").asText();

        // 3. GET /api/auth/me vegpont ellenorzese: a frissitett skillek visszaternek
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, refreshedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("varadi1@gmail.com"))
                .andExpect(jsonPath("$.role").value("APPLICANT"))
                .andExpect(jsonPath("$.skills", hasSize(2)))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("React ismeret", "TypeScript")));

        // Eredeti tokennel is a DB-bol feloldott frissitett skillek ternek vissza
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.skills", hasSize(2)))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("React ismeret", "TypeScript")));
    }

    /**
     * 2. Kovetelmeny:
     * Teszteld, hogy a kezelo (COMPANY_MANAGER) uj hirdetes letrehozasakor (POST /api/jobs) ES
     * meglevo hirdetesnel utolag (PATCH /api/jobs/{id}/skills) meg tudja adni a munkahoz szukseges skilleket,
     * mig jogosulatlan felhasznalo 403-at kap.
     */
    @Test
    @DisplayName("E2E: Ceges kezelo letrehoz es modosit skilleket, jogosulatlan felhasznalo 403-at kap")
    void testCompanyManagerCanManageSkillsWhileUnauthorizedGetsForbidden() throws Exception {
        // Céges kezelő belépése (seeded varadi2@gmail.com / Start123)
        String managerToken = loginAndGetToken("varadi2@gmail.com", "Start123");

        // Jelentkező belépése jogosultság-ellenőrzéshez (seeded varadi1@gmail.com / Start123)
        String applicantToken = loginAndGetToken("varadi1@gmail.com", "Start123");

        // A. Jogosulatlan felhasznalo (APPLICANT) nem hozhat letre uj allast -> 403 FORBIDDEN
        CreateJobRequestDto applicantCreateJobRequest = new CreateJobRequestDto(
                "Tiltott hirdetes",
                "Illegal Corp",
                "Full-time",
                "Budapest",
                "Java",
                "1 000 000 HUF",
                "Nem szabadna letrejonnie",
                List.of("Legalabb 1 ev tapasztalat"),
                List.of("Java", "Spring Boot")
        );

        mockMvc.perform(post("/api/jobs")
                        .header(HttpHeaders.AUTHORIZATION, applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicantCreateJobRequest)))
                .andExpect(status().isForbidden());

        // B. Céges kezelő sikeresen létrehoz új állást skillekkel (POST /api/jobs) -> 201 CREATED
        CreateJobRequestDto managerCreateJobRequest = new CreateJobRequestDto(
                "Lead Cloud Architect",
                "TechCorp Hungary",
                "Full-time",
                "Budapest / Hibrid",
                "Kubernetes",
                "2 000 000 - 2 500 000 HUF",
                "Komplex felho infrastruktura tervezese es iranyitasa.",
                List.of("5+ ev felho tapasztalat", "Kubernetes cluster tervezes"),
                List.of("Kubernetes", "Docker", "AWS", "Terraform")
        );

        MvcResult createJobResult = mockMvc.perform(post("/api/jobs")
                        .header(HttpHeaders.AUTHORIZATION, managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerCreateJobRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Lead Cloud Architect"))
                .andExpect(jsonPath("$.badge").value("Kubernetes"))
                .andExpect(jsonPath("$.skills", hasSize(4)))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("Kubernetes", "Docker", "AWS", "Terraform")))
                .andReturn();

        JsonNode createdJobJson = objectMapper.readTree(createJobResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        long createdJobId = createdJobJson.get("id").asLong();

        // C. Jogosulatlan felhasznalo (APPLICANT) nem modositgathatja az allas skilljeit -> 403 FORBIDDEN
        UpdateJobSkillsRequestDto unauthorizedUpdateDto = new UpdateJobSkillsRequestDto(List.of("Python", "Django"));
        mockMvc.perform(patch("/api/jobs/" + createdJobId + "/skills")
                        .header(HttpHeaders.AUTHORIZATION, applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unauthorizedUpdateDto)))
                .andExpect(status().isForbidden());

        // D. Bejelentkezes nelkul (hianyzo Authorization fejlec) torteno probalkozas -> 403 FORBIDDEN
        mockMvc.perform(patch("/api/jobs/" + createdJobId + "/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unauthorizedUpdateDto)))
                .andExpect(status().isForbidden());

        // E. Céges kezelő sikeresen módosítja a munkához szükséges skilleket utólag (PATCH /api/jobs/{id}/skills) -> 200 OK
        UpdateJobSkillsRequestDto validPatchDto = new UpdateJobSkillsRequestDto(
                List.of("Kubernetes", "Docker", "AWS", "Terraform", "Linux", "CI/CD")
        );

        mockMvc.perform(patch("/api/jobs/" + createdJobId + "/skills")
                        .header(HttpHeaders.AUTHORIZATION, managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdJobId))
                .andExpect(jsonPath("$.skills", hasSize(6)))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("Kubernetes", "Docker", "AWS", "Terraform", "Linux", "CI/CD")));

        // F. Céges kezelő sikeresen módosítja a skilleket PUT /api/jobs/{id}/skills vegponton is -> 200 OK
        UpdateJobSkillsRequestDto validPutDto = new UpdateJobSkillsRequestDto(
                List.of("Kubernetes", "Terraform", "Google Cloud")
        );

        mockMvc.perform(put("/api/jobs/" + createdJobId + "/skills")
                        .header(HttpHeaders.AUTHORIZATION, managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPutDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdJobId))
                .andExpect(jsonPath("$.skills", hasSize(3)))
                .andExpect(jsonPath("$.skills", containsInAnyOrder("Kubernetes", "Terraform", "Google Cloud")));

        // G. Masik ceges kezelo probalja modositani az allast -> 403 Forbidden (Csak a letrehozo modolithatja)
        String otherManagerToken = registerUserAndGetToken(
                "Masik Kezelo",
                "other.manager@techcorp.hu",
                "SecretPass123",
                UserRole.COMPANY_MANAGER,
                "Other Corp"
        );

        mockMvc.perform(patch("/api/jobs/" + createdJobId + "/skills")
                        .header(HttpHeaders.AUTHORIZATION, otherManagerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPatchDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Csak a hirdetés létrehozója módosíthatja a szükséges skilleket."));
    }

    /**
     * 3. Kovetelmeny:
     * Teszteld, hogy amikor a jelentkezo lekeri a GET /api/jobs vegpontot Bearer tokennel, az allasok matchScore szerint
     * csokkeno sorrendben erkeznek vissza: a pontosan illeszkedo munkak elol vannak (matchScore > 0, matchedSkills kitoltve,
     * recommended = true), a nem passzolo munkak (matchScore == 0) pedig a lista vegere vannak hatrasorolva!
     */
    @Test
    @DisplayName("E2E: GET /api/jobs jelentkezoi Bearer tokennel matchScore szerint csokkeno sorrendben erkezik vissza")
    void testJobListOrderedByMatchScoreForApplicantWithUnmatchedAtEnd() throws Exception {
        // 1. Jelentkező bejelentkezése és skillek beállítása
        String applicantToken = loginAndGetToken("varadi1@gmail.com", "Start123");

        List<String> targetApplicantSkills = List.of("React ismeret", "TypeScript");
        UpdateProfileRequestDto updateProfileDto = new UpdateProfileRequestDto(
                "Varadi 1 (Jelentkezo)",
                "varadi1@gmail.com",
                null,
                null,
                null,
                targetApplicantSkills
        );

        mockMvc.perform(put("/api/auth/profile")
                        .header(HttpHeaders.AUTHORIZATION, applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileDto)))
                .andExpect(status().isOk());

        // 2. GET /api/jobs lekérdezése a jelentkező Bearer tokenjével
        MvcResult jobsResult = mockMvc.perform(get("/api/jobs")
                        .header(HttpHeaders.AUTHORIZATION, applicantToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jobsArray = objectMapper.readTree(jobsResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(jobsArray.isArray()).isTrue();
        assertThat(jobsArray.size()).isGreaterThanOrEqualTo(8);

        // 3. Ellenőrzések:
        // - Az első állás a legpontosabban illeszkedő Frontend munka ("Senior Frontend Fejlesztő")
        JsonNode firstJob = jobsArray.get(0);
        int firstScore = firstJob.get("matchScore").asInt();
        boolean firstRecommended = firstJob.get("recommended").asBoolean();
        JsonNode firstMatchedSkills = firstJob.get("matchedSkills");

        assertThat(firstScore).as("Az elso allasnak pozitiv matchScore-ral kell rendelkeznie").isGreaterThan(0);
        assertThat(firstRecommended).as("Az elso allasnak ajanlottnak (recommended = true) kell lennie").isTrue();
        assertThat(firstMatchedSkills.isArray()).isTrue();
        assertThat(firstMatchedSkills.size()).as("Az elso allasnal legalabb egy egyezo skillnek kell lennie").isGreaterThan(0);

        // Győződjünk meg róla, hogy az első állás a Senior Frontend Fejlesztő
        assertThat(firstJob.get("title").asText()).contains("Frontend");

        // 4. Csökkenő sorrend és hátrasorolás ellenőrzése
        int previousScore = Integer.MAX_VALUE;
        boolean reachedZeroScore = false;
        int positiveScoreCount = 0;
        int zeroScoreCount = 0;

        for (int i = 0; i < jobsArray.size(); i++) {
            JsonNode job = jobsArray.get(i);
            assertThat(job.hasNonNull("matchScore")).as("Minden allasnak kell rendelkeznie matchScore-ral").isTrue();
            assertThat(job.hasNonNull("recommended")).as("Minden allasnak kell rendelkeznie recommended mezovel").isTrue();
            assertThat(job.hasNonNull("matchedSkills")).as("Minden allasnak kell rendelkeznie matchedSkills listaval").isTrue();

            int currentScore = job.get("matchScore").asInt();
            boolean recommended = job.get("recommended").asBoolean();
            JsonNode matched = job.get("matchedSkills");

            // A pontszámoknak monoton csökkenőnek kell lenniük (vagy egyenlőnek)
            assertThat(currentScore)
                    .as("Az állásoknak matchScore szerint csökkenő sorrendben kell érkezniük (index: " + i + ")")
                    .isLessThanOrEqualTo(previousScore);

            if (currentScore > 0) {
                positiveScoreCount++;
                assertThat(reachedZeroScore)
                        .as("Pozitív pontszámú állás (" + job.get("title").asText() + ") nem szerepelhet 0 pontos állások után!")
                        .isFalse();
                assertThat(matched.size())
                        .as("Pozitív pontszám esetén legalább egy matchedSkill-nek kell lennie")
                        .isGreaterThan(0);
                assertThat(recommended)
                        .as("Egyező skill esetén recommended-nek true-nak kell lennie")
                        .isTrue();
            } else {
                zeroScoreCount++;
                reachedZeroScore = true;
                assertThat(currentScore).isEqualTo(0);
                assertThat(matched.size())
                        .as("0 pontszámnál nincs egyező skill")
                        .isEqualTo(0);
                assertThat(recommended)
                        .as("0 pontszámnál recommended = false")
                        .isFalse();
            }

            previousScore = currentScore;
        }

        assertThat(positiveScoreCount).as("Kell lennie pozitiv pontszamu allasnak").isGreaterThanOrEqualTo(2);
        assertThat(zeroScoreCount).as("Kell lennie 0 pontszamu, hatrasorolt allasnak").isGreaterThan(0);

        // 5. Egyedi állás lekérése (GET /api/jobs/{id}) a tokennel szintén kiszámítja a matchResult-ot
        long frontendJobId = firstJob.get("id").asLong();
        mockMvc.perform(get("/api/jobs/" + frontendJobId)
                        .header(HttpHeaders.AUTHORIZATION, applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(frontendJobId))
                .andExpect(jsonPath("$.matchScore").value(firstScore))
                .andExpect(jsonPath("$.recommended").value(true))
                .andExpect(jsonPath("$.matchedSkills", hasSize(firstMatchedSkills.size())));

        // 6. Hitelesítés nélkül lekérve az állásokat nincs matchScore és nincs ajánlási pontozás
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matchScore").doesNotExist())
                .andExpect(jsonPath("$[0].recommended").doesNotExist());
    }

    // --- Segédmetódusok ---

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return "Bearer " + responseNode.get("token").asText();
    }

    private String registerUserAndGetToken(String name, String email, String password, UserRole role, String companyName) throws Exception {
        RegisterRequestDto registerDto = new RegisterRequestDto(name, email, password, role, companyName);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return "Bearer " + responseNode.get("token").asText();
    }
}
