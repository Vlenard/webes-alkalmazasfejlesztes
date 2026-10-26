package com.jobboard.jobs.application.service;

import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.jobs.application.port.in.CreateJobUseCase.CreateJobCommand;
import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import com.jobboard.jobs.domain.model.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepositoryPort jobRepositoryPort;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        jobService = new JobService(jobRepositoryPort);
    }

    @Test
    @DisplayName("getJobs delegál a JobRepositoryPort searchJobs metódusához és kiszűri a CLOSED hirdetéseket")
    void shouldDelegateSearchToPortAndFilterClosedJobs() {
        Job activeJob = new Job(1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 1L);
        Job closedJob = new Job(2L, "Vue Dev", "TechCorp", JobType.FULL_TIME, "BP", "Vue", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.CLOSED, 1L);

        when(jobRepositoryPort.searchJobs("tech", null)).thenReturn(List.of(activeJob, closedJob));

        List<Job> results = jobService.getJobs("tech", null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("React Dev");
        assertThat(results.get(0).getStatus()).isEqualTo(JobStatus.ACTIVE);
    }

    @Test
    @DisplayName("getJobsByCreator visszaadja a cég összes hirdetését (aktív és lezárt)")
    void shouldReturnAllJobsByCreator() {
        Job activeJob = new Job(1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 5L);
        Job closedJob = new Job(2L, "Vue Dev", "TechCorp", JobType.FULL_TIME, "BP", "Vue", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.CLOSED, 5L);

        when(jobRepositoryPort.findByCreatedByUserId(5L)).thenReturn(List.of(activeJob, closedJob));

        List<Job> results = jobService.getJobsByCreator(5L);

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("getJobById visszaadja az állást ha létezik")
    void shouldReturnJobWhenExists() {
        Job job = new Job(1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc", List.of(), LocalDate.now());
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(job));

        Job result = jobService.getJobById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getJobById JobNotFoundException-t dob ha nem létezik")
    void shouldThrowWhenJobNotFound() {
        when(jobRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.getJobById(999L))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("createJob elmenti és visszaadja az új állást")
    void shouldCreateJobSuccessfully() {
        CreateJobCommand command = new CreateJobCommand(
                "Java Dev", "Backend Kft", JobType.REMOTE, "Remote", "Java", "1.2M", "Leírás", List.of("Java"), 10L
        );
        Job savedJob = new Job(10L, "Java Dev", "Backend Kft", JobType.REMOTE, "Remote", "Java", "1.2M", "Leírás", List.of("Java"), LocalDate.now(), JobStatus.ACTIVE, 10L);
        when(jobRepositoryPort.save(any(Job.class))).thenReturn(savedJob);

        Job result = jobService.createJob(command);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Java Dev");
        assertThat(result.getCreatedByUserId()).isEqualTo(10L);
        verify(jobRepositoryPort).save(any(Job.class));
    }

    @Test
    @DisplayName("updateJobStatus sikeresen lezárja vagy újranyitja a hirdetést ha a létrehozó kéri")
    void shouldUpdateStatusWhenCreatorRequests() {
        Job existingJob = new Job(1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 10L);
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(existingJob));
        when(jobRepositoryPort.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.updateJobStatus(1L, JobStatus.CLOSED, 10L);

        assertThat(result.getStatus()).isEqualTo(JobStatus.CLOSED);
        verify(jobRepositoryPort).save(any(Job.class));
    }

    @Test
    @DisplayName("updateJobStatus UnauthorizedAccessException-t dob ha nem a létrehozó kéri a módosítást")
    void shouldThrowUnauthorizedWhenDifferentUserAttemptsStatusChange() {
        Job existingJob = new Job(1L, "React Dev", "TechCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 10L);
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(existingJob));

        assertThatThrownBy(() -> jobService.updateJobStatus(1L, JobStatus.CLOSED, 99L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Csak a hirdetés létrehozója módosíthatja vagy nyithatja újra a hirdetést.");
    }
}
