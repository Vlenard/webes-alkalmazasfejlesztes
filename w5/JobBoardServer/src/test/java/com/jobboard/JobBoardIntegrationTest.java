package com.jobboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.applications.infrastructure.web.dto.SubmitApplicationRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobBoardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Integrációs teszt: A seeded állások lekérdezhetők, szűrhetők és jelentkezés adható le")
    void testFullJobBoardWorkflow() throws Exception {
        // 1. Ellenőrizzük, hogy a kezdő 4 állás elérhető
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].id").exists());

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

        // 5. Ellenőrizzük, hogy a jelentkezés megjelenik az álláshoz tartozó listában
        mockMvc.perform(get("/api/jobs/1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].applicantName").value("Teszt Elek"))
                .andExpect(jsonPath("$[0].applicantEmail").value("teszt.elek@example.com"));
    }
}
