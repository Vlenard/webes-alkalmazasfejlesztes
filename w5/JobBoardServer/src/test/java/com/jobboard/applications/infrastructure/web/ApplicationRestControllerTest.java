package com.jobboard.applications.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.applications.application.port.in.GetApplicationsUseCase;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.web.dto.SubmitApplicationRequestDto;
import com.jobboard.applications.infrastructure.web.mapper.ApplicationWebMapper;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.shared.infrastructure.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationRestController.class)
@Import({ApplicationWebMapper.class, GlobalExceptionHandler.class})
class ApplicationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubmitApplicationUseCase submitApplicationUseCase;

    @MockBean
    private GetApplicationsUseCase getApplicationsUseCase;

    @Test
    @DisplayName("POST /api/applications sikeresen beküldi a jelentkezést 201 CREATED státusszal")
    void shouldSubmitApplicationSuccessfully() throws Exception {
        SubmitApplicationRequestDto request = new SubmitApplicationRequestDto(
                1L,
                "Kiss János",
                "kiss.janos@example.com",
                "resume.pdf"
        );

        JobApplication saved = new JobApplication(
                10L,
                1L,
                "Kiss János",
                "kiss.janos@example.com",
                "resume.pdf",
                Instant.parse("2026-09-15T10:00:00Z")
        );

        when(submitApplicationUseCase.submitApplication(any())).thenReturn(saved);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("A jelentkezést sikeresen rögzítettük!"))
                .andExpect(jsonPath("$.applicationId").value(10L));
    }

    @Test
    @DisplayName("POST /api/applications 400 Bad Request-et ad ha hibás az email")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        SubmitApplicationRequestDto invalidRequest = new SubmitApplicationRequestDto(
                1L,
                "Kiss János",
                "not-an-email",
                null
        );

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.applicantEmail").exists());
    }

    @Test
    @DisplayName("POST /api/applications 404-et ad ha a hivatkozott munka nem létezik")
    void shouldReturn404WhenJobDoesNotExist() throws Exception {
        SubmitApplicationRequestDto request = new SubmitApplicationRequestDto(
                999L,
                "Kiss János",
                "kiss.janos@example.com",
                null
        );

        when(submitApplicationUseCase.submitApplication(any()))
                .thenThrow(new JobNotFoundException(999L));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/jobs/{jobId}/applications visszaadja a jelentkezések listáját")
    void shouldReturnApplicationsForJob() throws Exception {
        JobApplication app = new JobApplication(
                1L, 1L, "Kiss János", "kiss@example.com", "cv.pdf", Instant.parse("2026-09-15T12:00:00Z")
        );

        when(getApplicationsUseCase.getApplicationsByJobId(1L)).thenReturn(List.of(app));

        mockMvc.perform(get("/api/jobs/1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicantName").value("Kiss János"))
                .andExpect(jsonPath("$[0].jobId").value(1));
    }
}
