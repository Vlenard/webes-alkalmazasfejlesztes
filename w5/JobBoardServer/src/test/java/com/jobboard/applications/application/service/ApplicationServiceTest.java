package com.jobboard.applications.application.service;

import com.jobboard.applications.application.port.in.SubmitApplicationUseCase.SubmitApplicationCommand;
import com.jobboard.applications.application.port.out.ApplicationRepositoryPort;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.exception.JobNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepositoryPort applicationRepositoryPort;

    @Mock
    private JobRepositoryPort jobRepositoryPort;

    private ApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(applicationRepositoryPort, jobRepositoryPort);
    }

    @Test
    @DisplayName("Sikeres jelentkezés leadása ha az állás létezik")
    void shouldSubmitApplicationWhenJobExists() {
        SubmitApplicationCommand command = new SubmitApplicationCommand(1L, "Nagy Anna", "anna@example.com", "cv.pdf");
        when(jobRepositoryPort.existsById(1L)).thenReturn(true);

        JobApplication saved = new JobApplication(100L, 1L, "Nagy Anna", "anna@example.com", "cv.pdf", Instant.now());
        when(applicationRepositoryPort.save(any(JobApplication.class))).thenReturn(saved);

        JobApplication result = applicationService.submitApplication(command);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getApplicantName()).isEqualTo("Nagy Anna");
        verify(jobRepositoryPort).existsById(1L);
        verify(applicationRepositoryPort).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("JobNotFoundException dobása ha nem létező állásra jelentkeznek")
    void shouldThrowJobNotFoundWhenJobDoesNotExist() {
        SubmitApplicationCommand command = new SubmitApplicationCommand(999L, "Nagy Anna", "anna@example.com", "cv.pdf");
        when(jobRepositoryPort.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> applicationService.submitApplication(command))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("999");

        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("getApplicationsByJobId visszaadja a releváns jelentkezéseket")
    void shouldReturnApplicationsForJob() {
        when(jobRepositoryPort.existsById(1L)).thenReturn(true);
        JobApplication app = new JobApplication(10L, 1L, "Kovács Béla", "bela@example.com", null, Instant.now());
        when(applicationRepositoryPort.findByJobId(1L)).thenReturn(List.of(app));

        List<JobApplication> list = applicationService.getApplicationsByJobId(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getApplicantName()).isEqualTo("Kovács Béla");
    }
}
