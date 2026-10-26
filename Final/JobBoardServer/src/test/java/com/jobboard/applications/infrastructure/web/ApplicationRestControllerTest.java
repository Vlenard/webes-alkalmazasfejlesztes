package com.jobboard.applications.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.model.UserRole;
import com.jobboard.applications.application.port.in.DownloadResumeUseCase;
import com.jobboard.applications.application.port.in.GetApplicationsUseCase;
import com.jobboard.applications.application.port.in.GetApplicationsUseCase.ApplicantApplicationView;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase;
import com.jobboard.applications.application.port.in.UpdateApplicationStatusUseCase;
import com.jobboard.applications.domain.model.ApplicationStatus;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.web.dto.SubmitApplicationRequestDto;
import com.jobboard.applications.infrastructure.web.dto.UpdateApplicationStatusRequestDto;
import com.jobboard.applications.infrastructure.web.mapper.ApplicationWebMapper;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.shared.infrastructure.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @MockBean
    private UpdateApplicationStatusUseCase updateApplicationStatusUseCase;

    @MockBean
    private DownloadResumeUseCase downloadResumeUseCase;

    @MockBean
    private AuthenticateTokenUseCase authenticateTokenUseCase;

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
    @DisplayName("POST /api/applications multipart fájlfeltöltéssel sikeresen létrehozza a jelentkezést")
    void shouldSubmitApplicationWithMultipart() throws Exception {
        MockMultipartFile resumeFile = new MockMultipartFile(
                "resume", "my_cv.pdf", "application/pdf", "%PDF-1.4 mock cv".getBytes(StandardCharsets.UTF_8)
        );

        JobApplication saved = new JobApplication(
                20L, 1L, "Nagy Anna", "anna@example.com", "my_cv.pdf", Instant.now(),
                100L, ApplicationStatus.PENDING, "application/pdf", resumeFile.getBytes()
        );
        when(submitApplicationUseCase.submitApplication(any())).thenReturn(saved);

        mockMvc.perform(multipart("/api/applications")
                        .file(resumeFile)
                        .param("jobId", "1")
                        .param("applicantName", "Nagy Anna")
                        .param("applicantEmail", "anna@example.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.applicationId").value(20L));
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
    @DisplayName("GET /api/applications/my visszaadja a bejelentkezett jelentkező saját jelentkezéseit")
    void shouldReturnMyApplications() throws Exception {
        String token = "Bearer applicant-token";
        AuthenticateTokenUseCase.AuthenticatedUser applicant =
                new AuthenticateTokenUseCase.AuthenticatedUser(5L, "Jelentkező", "app@pelda.hu", UserRole.APPLICANT, null);
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(applicant);

        ApplicantApplicationView view = new ApplicantApplicationView(
                10L, 1L, "React Fejlesztő", "TechCorp", "Budapest",
                "ACCEPTED", "A cég értesíti Önt a továbbiakról.", "cv.pdf", Instant.parse("2026-10-26T15:00:00Z")
        );
        when(getApplicationsUseCase.getApplicationsForApplicant(5L, "app@pelda.hu")).thenReturn(List.of(view));

        mockMvc.perform(get("/api/applications/my").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].jobTitle").value("React Fejlesztő"))
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$[0].statusMessage").value("A cég értesíti Önt a továbbiakról."));
    }

    @Test
    @DisplayName("PATCH /api/applications/{id}/status frissíti a jelentkezés státuszát ha cégvezető kéri")
    void shouldUpdateApplicationStatus() throws Exception {
        String token = "Bearer company-token";
        AuthenticateTokenUseCase.AuthenticatedUser manager =
                new AuthenticateTokenUseCase.AuthenticatedUser(10L, "HR", "hr@techcorp.hu", UserRole.COMPANY_MANAGER, "TechCorp");
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(manager);

        JobApplication updated = new JobApplication(
                10L, 1L, "Kiss János", "kiss@example.com", "cv.pdf", Instant.now(),
                5L, ApplicationStatus.ACCEPTED, "application/pdf", null
        );
        when(updateApplicationStatusUseCase.updateStatus(eq(10L), eq(ApplicationStatus.ACCEPTED), eq(10L)))
                .thenReturn(updated);

        UpdateApplicationStatusRequestDto request = new UpdateApplicationStatusRequestDto("ACCEPTED");

        mockMvc.perform(patch("/api/applications/10/status")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @DisplayName("PATCH /api/applications/{id}/status 403 Forbidden ha nem cégvezető kéri")
    void shouldReturn403WhenNonManagerUpdatesApplicationStatus() throws Exception {
        String token = "Bearer applicant-token";
        AuthenticateTokenUseCase.AuthenticatedUser applicant =
                new AuthenticateTokenUseCase.AuthenticatedUser(5L, "Jelentkező", "app@pelda.hu", UserRole.APPLICANT, null);
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(applicant);

        UpdateApplicationStatusRequestDto request = new UpdateApplicationStatusRequestDto("ACCEPTED");

        mockMvc.perform(patch("/api/applications/10/status")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/applications/{id}/resume letölti az önéletrajzot megfelelő fejlécekkel")
    void shouldDownloadResume() throws Exception {
        String token = "Bearer company-token";
        AuthenticateTokenUseCase.AuthenticatedUser manager =
                new AuthenticateTokenUseCase.AuthenticatedUser(10L, "HR", "hr@techcorp.hu", UserRole.COMPANY_MANAGER, "TechCorp");
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(manager);

        byte[] pdfBytes = "%PDF-1.4 file content".getBytes(StandardCharsets.UTF_8);
        DownloadResumeUseCase.ResumeFile resumeFile = new DownloadResumeUseCase.ResumeFile("my_cv.pdf", "application/pdf", pdfBytes);
        when(downloadResumeUseCase.getResume(10L, 10L)).thenReturn(resumeFile);

        mockMvc.perform(get("/api/applications/10/resume").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my_cv.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes(pdfBytes));
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
