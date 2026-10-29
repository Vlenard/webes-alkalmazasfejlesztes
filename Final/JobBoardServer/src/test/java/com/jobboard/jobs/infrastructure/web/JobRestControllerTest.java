package com.jobboard.jobs.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.auth.application.port.in.AuthenticateTokenUseCase;
import com.jobboard.auth.application.port.out.UserRepositoryPort;
import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.auth.domain.model.UserRole;
import com.jobboard.jobs.application.port.in.CreateJobUseCase;
import com.jobboard.jobs.application.port.in.GetJobDetailUseCase;
import com.jobboard.jobs.application.port.in.GetJobsUseCase;
import com.jobboard.jobs.application.port.in.UpdateJobSkillsUseCase;
import com.jobboard.jobs.application.port.in.UpdateJobStatusUseCase;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import com.jobboard.jobs.domain.model.JobType;
import com.jobboard.jobs.domain.service.JobRecommendationMatcher;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobSkillsRequestDto;
import com.jobboard.jobs.infrastructure.web.dto.UpdateJobStatusRequestDto;
import com.jobboard.jobs.infrastructure.web.mapper.JobWebMapper;
import com.jobboard.shared.infrastructure.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobRestController.class)
@Import({JobWebMapper.class, JobRecommendationMatcher.class, GlobalExceptionHandler.class})
class JobRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetJobsUseCase getJobsUseCase;

    @MockBean
    private GetJobDetailUseCase getJobDetailUseCase;

    @MockBean
    private CreateJobUseCase createJobUseCase;

    @MockBean
    private UpdateJobStatusUseCase updateJobStatusUseCase;

    @MockBean
    private UpdateJobSkillsUseCase updateJobSkillsUseCase;

    @MockBean
    private AuthenticateTokenUseCase authenticateTokenUseCase;

    @MockBean
    private UserRepositoryPort userRepositoryPort;

    @Test
    @DisplayName("GET /api/jobs visszaadja az állások listáját 200 OK státusszal")
    void shouldReturnJobList() throws Exception {
        Job sampleJob = new Job(
                1L,
                "Senior Frontend Fejlesztő",
                "TechCorp Hungary",
                JobType.FULL_TIME,
                "Budapest / Hibrid",
                "React",
                "1 200 000 - 1 600 000 HUF",
                "Modern React app fejlesztés",
                List.of("React", "TypeScript"),
                LocalDate.of(2026, 9, 14),
                JobStatus.ACTIVE,
                10L
        );

        when(getJobsUseCase.getJobs(null, null)).thenReturn(List.of(sampleJob));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Senior Frontend Fejlesztő"))
                .andExpect(jsonPath("$[0].company").value("TechCorp Hungary"))
                .andExpect(jsonPath("$[0].type").value("Full-time"))
                .andExpect(jsonPath("$[0].badge").value("React"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].postedAt").value("2026-09-14"));
    }

    @Test
    @DisplayName("GET /api/jobs/my visszaadja a cég saját hirdetéseit")
    void shouldReturnMyJobsWhenCompanyManager() throws Exception {
        String token = "Bearer valid-token";
        AuthenticateTokenUseCase.AuthenticatedUser manager =
                new AuthenticateTokenUseCase.AuthenticatedUser(5L, "HR", "hr@techcorp.hu", UserRole.COMPANY_MANAGER, "TechCorp");
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(manager);

        Job myJob = new Job(
                1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.CLOSED, 5L
        );
        when(getJobsUseCase.getJobsByCreator(eq(5L), any())).thenReturn(List.of(myJob));

        mockMvc.perform(get("/api/jobs/my").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CLOSED"))
                .andExpect(jsonPath("$[0].createdByUserId").value(5));
    }

    @Test
    @DisplayName("GET /api/jobs/my 403 Forbidden ha nem cégvezető kérdezi le")
    void shouldReturn403WhenNotCompanyManager() throws Exception {
        String token = "Bearer applicant-token";
        AuthenticateTokenUseCase.AuthenticatedUser applicant =
                new AuthenticateTokenUseCase.AuthenticatedUser(2L, "Jelentkezo", "app@pelda.hu", UserRole.APPLICANT, null);
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(applicant);

        mockMvc.perform(get("/api/jobs/my").header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} visszaadja a megfelelő állást")
    void shouldReturnJobById() throws Exception {
        Job sampleJob = new Job(
                1L,
                "Senior Frontend Fejlesztő",
                "TechCorp Hungary",
                JobType.FULL_TIME,
                "Budapest",
                "React",
                "1.2M",
                "Leírás",
                List.of(),
                LocalDate.now()
        );

        when(getJobDetailUseCase.getJobById(1L)).thenReturn(sampleJob);

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Senior Frontend Fejlesztő"));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} 404 NOT FOUND hibát ad ha az állás nem létezik")
    void shouldReturn404WhenJobNotFound() throws Exception {
        when(getJobDetailUseCase.getJobById(999L)).thenThrow(new JobNotFoundException(999L));

        mockMvc.perform(get("/api/jobs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Az álláshirdetés nem található (ID: 999)"));
    }

    @Test
    @DisplayName("POST /api/jobs sikeresen létrehozza az állást 201 CREATED kóddal")
    void shouldCreateJob() throws Exception {
        CreateJobRequestDto request = new CreateJobRequestDto(
                "Lead Architect",
                "Global Systems",
                "Full-time",
                "Budapest",
                "Cloud",
                "2 000 000 HUF",
                "Architektúra tervezése",
                List.of("AWS", "Java")
        );

        Job createdJob = new Job(
                5L,
                request.title(),
                request.company(),
                JobType.FULL_TIME,
                request.location(),
                request.badge(),
                request.salary(),
                request.description(),
                request.requirements(),
                LocalDate.now()
        );

        when(createJobUseCase.createJob(any())).thenReturn(createdJob);

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/jobs/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Lead Architect"));
    }

    @Test
    @DisplayName("POST /api/jobs validációs hibát ad (400) ha hiányzik a cím")
    void shouldReturn400WhenValidationFails() throws Exception {
        CreateJobRequestDto invalidRequest = new CreateJobRequestDto(
                "",
                "Global Systems",
                "Full-time",
                "Budapest",
                "Cloud",
                "2 000 000 HUF",
                "Architektúra",
                List.of("AWS")
        );

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("PATCH /api/jobs/{id}/status módosítja az állás státuszát (ACTIVE <-> CLOSED)")
    void shouldUpdateJobStatus() throws Exception {
        String token = "Bearer company-token";
        AuthenticateTokenUseCase.AuthenticatedUser manager =
                new AuthenticateTokenUseCase.AuthenticatedUser(10L, "HR", "hr@techcorp.hu", UserRole.COMPANY_MANAGER, "TechCorp");
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(manager);

        Job updatedJob = new Job(
                1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.CLOSED, 10L
        );
        when(updateJobStatusUseCase.updateJobStatus(eq(1L), eq(JobStatus.CLOSED), eq(10L))).thenReturn(updatedJob);

        UpdateJobStatusRequestDto request = new UpdateJobStatusRequestDto("CLOSED");

        mockMvc.perform(patch("/api/jobs/1/status")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("PATCH /api/jobs/{id}/status 403 Forbidden ha nem a létrehozó próbálja módosítani")
    void shouldReturn403WhenUnauthorizedToUpdateStatus() throws Exception {
        String token = "Bearer company-token";
        AuthenticateTokenUseCase.AuthenticatedUser otherManager =
                new AuthenticateTokenUseCase.AuthenticatedUser(99L, "Other HR", "other@othercorp.hu", UserRole.COMPANY_MANAGER, "OtherCorp");
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(otherManager);

        when(updateJobStatusUseCase.updateJobStatus(eq(1L), eq(JobStatus.CLOSED), eq(99L)))
                .thenThrow(new UnauthorizedAccessException("Csak a hirdetés létrehozója módosíthatja vagy nyithatja újra a hirdetést."));

        UpdateJobStatusRequestDto request = new UpdateJobStatusRequestDto("CLOSED");

        mockMvc.perform(patch("/api/jobs/1/status")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Csak a hirdetés létrehozója módosíthatja vagy nyithatja újra a hirdetést."));
    }

    @Test
    @DisplayName("GET /api/jobs bejelentkezett APPLICANT esetén matchScore és matchedSkills szerint csökkenő sorrendben adja vissza az állásokat")
    void shouldReturnJobsSortedByMatchScoreWhenApplicant() throws Exception {
        String token = "Bearer applicant-token";
        AuthenticateTokenUseCase.AuthenticatedUser applicant =
                new AuthenticateTokenUseCase.AuthenticatedUser(7L, "Váradi", "varadi1@gmail.com", UserRole.APPLICANT, null, List.of("React", "TypeScript"));
        when(authenticateTokenUseCase.authenticateOptional(token)).thenReturn(applicant);

        Job nonMatchingJob = new Job(1L, "Python ML Dev", "AI Corp", JobType.FULL_TIME, "Remote", "Python", "1.5M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 2L, List.of("Python", "PyTorch"));
        Job matchingJob = new Job(2L, "React Senior Dev", "Frontend Corp", JobType.FULL_TIME, "BP", "React", "1.5M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 2L, List.of("React", "TypeScript", "Tailwind CSS"));

        when(getJobsUseCase.getJobs(null, null)).thenReturn(List.of(nonMatchingJob, matchingJob));

        mockMvc.perform(get("/api/jobs").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("React Senior Dev"))
                .andExpect(jsonPath("$[0].matchScore").isNumber())
                .andExpect(jsonPath("$[0].recommended").value(true))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].title").value("Python ML Dev"))
                .andExpect(jsonPath("$[1].matchScore").value(0))
                .andExpect(jsonPath("$[1].recommended").value(false));
    }

    @Test
    @DisplayName("GET /api/jobs ékezetes és stop-word-ös skillekkel rendelkező jelentkező esetén is helyesen rangsorol")
    void shouldRankAndMatchJobsWithStopWordsAndAccents() throws Exception {
        String token = "Bearer applicant-token";
        AuthenticateTokenUseCase.AuthenticatedUser applicant =
                new AuthenticateTokenUseCase.AuthenticatedUser(7L, "Váradi", "varadi1@gmail.com", UserRole.APPLICANT, null,
                        List.of("React ismeret", "Haladó Spring tapasztalat"));
        when(authenticateTokenUseCase.authenticateOptional(token)).thenReturn(applicant);

        Job zeroMatchJob = new Job(10L, "Go Backend", "Cloud Zrt", JobType.FULL_TIME, "Remote", "Go", "1.2M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 1L, List.of("Go", "Kubernetes"));
        Job perfectMatchJob = new Job(20L, "Fullstack Fejlesztő", "Web Corp", JobType.FULL_TIME, "BP", "React", "1.6M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 1L, List.of("React", "Spring Boot"));

        when(getJobsUseCase.getJobs(null, null)).thenReturn(List.of(zeroMatchJob, perfectMatchJob));

        mockMvc.perform(get("/api/jobs").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20))
                .andExpect(jsonPath("$[0].title").value("Fullstack Fejlesztő"))
                .andExpect(jsonPath("$[0].matchScore").isNumber())
                .andExpect(jsonPath("$[0].matchedSkills").isArray())
                .andExpect(jsonPath("$[0].recommended").value(true))
                .andExpect(jsonPath("$[1].id").value(10))
                .andExpect(jsonPath("$[1].matchScore").value(0))
                .andExpect(jsonPath("$[1].recommended").value(false));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} bejelentkezett APPLICANT esetén kiszámítja és kitölti a matchScore mezőket")
    void shouldReturnJobWithRecommendationDetailsWhenApplicant() throws Exception {
        String token = "Bearer applicant-token";
        AuthenticateTokenUseCase.AuthenticatedUser applicant =
                new AuthenticateTokenUseCase.AuthenticatedUser(7L, "Váradi", "varadi1@gmail.com", UserRole.APPLICANT, null, List.of("React ismeret"));
        when(authenticateTokenUseCase.authenticateOptional(token)).thenReturn(applicant);

        Job job = new Job(2L, "React Senior Dev", "Frontend Corp", JobType.FULL_TIME, "BP", "React", "1.5M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 2L, List.of("React", "TypeScript"));

        when(getJobDetailUseCase.getJobById(2L)).thenReturn(job);

        mockMvc.perform(get("/api/jobs/2").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.matchScore").isNumber())
                .andExpect(jsonPath("$.matchedSkills").isArray())
                .andExpect(jsonPath("$.missingSkills").isArray())
                .andExpect(jsonPath("$.recommended").value(true));
    }

    @Test
    @DisplayName("PATCH /api/jobs/{id}/skills frissíti a hirdetés skilljeit ha cégvezető kéri")
    void shouldUpdateJobSkillsWhenCompanyManager() throws Exception {
        String token = "Bearer manager-token";
        AuthenticateTokenUseCase.AuthenticatedUser manager =
                new AuthenticateTokenUseCase.AuthenticatedUser(10L, "HR", "hr@techcorp.hu", UserRole.COMPANY_MANAGER, "TechCorp");
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(manager);

        Job updatedJob = new Job(
                1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 10L, List.of("React", "TypeScript", "Tailwind CSS")
        );
        when(updateJobSkillsUseCase.updateJobSkills(eq(1L), any(), eq(10L))).thenReturn(updatedJob);

        UpdateJobSkillsRequestDto request = new UpdateJobSkillsRequestDto(List.of("React", "TypeScript", "Tailwind CSS"));

        mockMvc.perform(patch("/api/jobs/1/skills")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.skills[0]").value("React"))
                .andExpect(jsonPath("$.skills[1]").value("TypeScript"))
                .andExpect(jsonPath("$.skills[2]").value("Tailwind CSS"));
    }

    @Test
    @DisplayName("PUT /api/jobs/{id}/skills szintén frissíti a hirdetés skilljeit ha cégvezető kéri")
    void shouldPutJobSkillsWhenCompanyManager() throws Exception {
        String token = "Bearer manager-token";
        AuthenticateTokenUseCase.AuthenticatedUser manager =
                new AuthenticateTokenUseCase.AuthenticatedUser(10L, "HR", "hr@techcorp.hu", UserRole.COMPANY_MANAGER, "TechCorp");
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(manager);

        Job updatedJob = new Job(
                1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 10L, List.of("React", "Tailwind CSS")
        );
        when(updateJobSkillsUseCase.updateJobSkills(eq(1L), any(), eq(10L))).thenReturn(updatedJob);

        UpdateJobSkillsRequestDto request = new UpdateJobSkillsRequestDto(List.of("React", "Tailwind CSS"));

        mockMvc.perform(put("/api/jobs/1/skills")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.skills").isArray());
    }

    @Test
    @DisplayName("PATCH /api/jobs/{id}/skills 403 Forbidden ha nem cégvezető próbálja módosítani")
    void shouldReturn403WhenApplicantTriesToUpdateSkills() throws Exception {
        String token = "Bearer applicant-token";
        AuthenticateTokenUseCase.AuthenticatedUser applicant =
                new AuthenticateTokenUseCase.AuthenticatedUser(7L, "Váradi", "varadi1@gmail.com", UserRole.APPLICANT, null);
        when(authenticateTokenUseCase.authenticate(token)).thenReturn(applicant);

        UpdateJobSkillsRequestDto request = new UpdateJobSkillsRequestDto(List.of("React"));

        mockMvc.perform(patch("/api/jobs/1/skills")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PATCH /api/jobs/{id}/skills 403 Forbidden ha hiányzik az auth header")
    void shouldReturn403WhenAuthHeaderMissingForSkillsUpdate() throws Exception {
        UpdateJobSkillsRequestDto request = new UpdateJobSkillsRequestDto(List.of("React"));

        mockMvc.perform(patch("/api/jobs/1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}

