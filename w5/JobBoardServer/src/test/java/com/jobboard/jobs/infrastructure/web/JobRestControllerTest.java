package com.jobboard.jobs.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.jobs.application.port.in.CreateJobUseCase;
import com.jobboard.jobs.application.port.in.GetJobDetailUseCase;
import com.jobboard.jobs.application.port.in.GetJobsUseCase;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobType;
import com.jobboard.jobs.infrastructure.web.dto.CreateJobRequestDto;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobRestController.class)
@Import({JobWebMapper.class, GlobalExceptionHandler.class})
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
                LocalDate.of(2026, 9, 14)
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
                .andExpect(jsonPath("$[0].postedAt").value("2026-09-14"));
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
}
