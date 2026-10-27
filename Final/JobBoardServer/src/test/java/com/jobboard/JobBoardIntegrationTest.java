package com.jobboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.applications.infrastructure.web.dto.SubmitApplicationRequestDto;
import com.jobboard.applications.infrastructure.web.dto.UpdateApplicationStatusRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobStatusRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JobBoardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Integrációs teszt: A seeded állások lekérdezhetők, szűrhetők és jelentkezés adható le")
    void testFullJobBoardWorkflow() throws Exception {
        // 1. Ellenőrizzük, hogy a kezdő aktív állások elérhetők a nyilvános listában (mind ACTIVE, a lezártak rejtve)
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        // 2. Keresés technológia / cím alapján
        mockMvc.perform(get("/api/jobs").param("search", "Node.js"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].company").value("NextWave Digital"));

        // 3. Egy állás lekérése
        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Senior Frontend Fejlesztő"))
                .andExpect(jsonPath("$.badge").value("React"));

        // 4. Jelentkezés leadása a frontend formátumával
        SubmitApplicationRequestDto applicationRequest = new SubmitApplicationRequestDto(
                1L,
                "Teszt Elek",
                "teszt.elek@example.com",
                "resume_elek.pdf"
        );

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("A jelentkezést sikeresen rögzítettük!"))
                .andExpect(jsonPath("$.applicationId").isNumber());

        // Újra jelentkezés megakadályozása (400 Bad Request)
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Már jelentkeztél erre az állásra."));

        // 5. Ellenőrizzük, hogy a jelentkezés megjelenik az álláshoz tartozó listában
        mockMvc.perform(get("/api/jobs/1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].applicantName").value("Teszt Elek"))
                .andExpect(jsonPath("$[0].applicantEmail").value("teszt.elek@example.com"));
    }

    @Test
    @DisplayName("Integrációs teszt: A statikus weboldal és az SPA kliens oldali útvonalak (index.html) kiszolgálásra kerülnek")
    void testSpaAndStaticRoutesServed() throws Exception {
        // Gyökér útvonal
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // React Router kliens útvonalak újratöltésekor is index.html-t kap a böngésző
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        // Az API útvonalak továbbra is a REST vezérlőkhöz jutnak (JSON választ adnak)
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Teljes end-to-end munkafolyamat: Bejelentkezés, hirdetés lezárás/újranyitás, multipart CV és státusz elbírálás")
    void testEndToEndStatusAndResumeWorkflow() throws Exception {
        // 1. Céges kezelő belépése (JobDataSeeder által létrehozott hr@techcorp.hu)
        MvcResult hrLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "hr@techcorp.hu",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode hrJson = objectMapper.readTree(hrLoginResult.getResponse().getContentAsString());
        String hrToken = "Bearer " + hrJson.get("token").asText();
        Long hrUserId = hrJson.get("user").get("id").asLong();

        // 2. Jelentkező belépése (JobDataSeeder által létrehozott jelentkezo@pelda.hu)
        MvcResult applicantLoginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "jelentkezo@pelda.hu",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode applicantJson = objectMapper.readTree(applicantLoginResult.getResponse().getContentAsString());
        String applicantToken = "Bearer " + applicantJson.get("token").asText();

        // 3. Céges kezelő új álláshirdetést hoz létre
        CreateJobRequestDto newJobDto = new CreateJobRequestDto(
                "Lead DevOps Mérnök", "TechCorp Hungary", "Full-time", "Budapest", "DevOps",
                "2 200 000 HUF", "Infrastruktúra vezetés", List.of("Kubernetes", "Terraform")
        );

        MvcResult createJobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newJobDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdByUserId").value(hrUserId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        Long createdJobId = objectMapper.readTree(createJobResult.getResponse().getContentAsString()).get("id").asLong();

        // 4. Jelentkező feltölti a PDF önéletrajzát multipart form-data kéréssel
        byte[] samplePdf = "%PDF-1.4 mock cv content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile resumeFile = new MockMultipartFile("resume", "oneletrajz.pdf", "application/pdf", samplePdf);

        MvcResult applyResult = mockMvc.perform(multipart("/api/applications")
                        .file(resumeFile)
                        .param("jobId", createdJobId.toString())
                        .param("applicantName", "Példa Jelentkező")
                        .param("applicantEmail", "jelentkezo@pelda.hu")
                        .header("Authorization", applicantToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        Long applicationId = objectMapper.readTree(applyResult.getResponse().getContentAsString()).get("applicationId").asLong();

        // 5. Céges kezelő lezárja a hirdetést (CLOSED)
        mockMvc.perform(patch("/api/jobs/" + createdJobId + "/status")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateJobStatusRequestDto("CLOSED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        // 6. Ellenőrizzük, hogy a lezárt hirdetés NEM jelenik meg a nyilvános GET /api/jobs listában
        MvcResult publicJobsResult = mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode publicJobs = objectMapper.readTree(publicJobsResult.getResponse().getContentAsString());
        for (JsonNode jobNode : publicJobs) {
            org.junit.jupiter.api.Assertions.assertNotEquals(createdJobId, jobNode.get("id").asLong(), "Lezárt állás nem jelenhet meg a nyilvános listában!");
        }

        // 7. Lezárt hirdetésre NEM lehet jelentkezni (400 Bad Request)
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SubmitApplicationRequestDto(
                                createdJobId, "Későn Ébredő", "keson@example.com", "cv.pdf"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lezárt álláshirdetésre nem lehet jelentkezni."));

        // 8. Céges kezelő viszont látja a saját GET /api/jobs/my listájában
        mockMvc.perform(get("/api/jobs/my").header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + createdJobId + ")].status").value("CLOSED"));

        // 9. Céges kezelő letölti az önéletrajzot
        mockMvc.perform(get("/api/applications/" + applicationId + "/resume")
                        .header("Authorization", hrToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"oneletrajz.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(samplePdf));

        // 10. Céges kezelő elfogadja a jelentkezést (ACCEPTED)
        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateApplicationStatusRequestDto("ACCEPTED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 11. Jelentkező lekéri a saját jelentkezéseit (GET /api/applications/my)
        mockMvc.perform(get("/api/applications/my").header("Authorization", applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + applicationId + ")].status").value("ACCEPTED"))
                .andExpect(jsonPath("$[?(@.id == " + applicationId + ")].statusMessage").value("A cég értesíti Önt a továbbiakról."));

        // 12. Céges kezelő újra megnyitja az állást (ACTIVE)
        mockMvc.perform(patch("/api/jobs/" + createdJobId + "/status")
                        .header("Authorization", hrToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateJobStatusRequestDto("ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
