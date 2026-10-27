package com.jobboard.applications.application.service;

import com.jobboard.auth.domain.exception.UnauthorizedAccessException;
import com.jobboard.applications.application.port.in.DownloadResumeUseCase.ResumeFile;
import com.jobboard.applications.application.port.in.GetApplicationsUseCase.ApplicantApplicationView;
import com.jobboard.applications.application.port.in.SubmitApplicationUseCase.SubmitApplicationCommand;
import com.jobboard.applications.application.port.out.ApplicationRepositoryPort;
import com.jobboard.applications.domain.exception.InvalidApplicationException;
import com.jobboard.applications.domain.model.ApplicationStatus;
import com.jobboard.applications.domain.model.JobApplication;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    private Job activeJob;
    private Job closedJob;

    @BeforeEach
    void setUp() {
        applicationService = new ApplicationService(applicationRepositoryPort, jobRepositoryPort);
        activeJob = new Job(1L, "React Fejlesztő", "TechCorp", JobType.FULL_TIME, "BP", "React", "1.2M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 10L);
        closedJob = new Job(2L, "Vue Fejlesztő", "TechCorp", JobType.FULL_TIME, "BP", "Vue", "1.2M", "Desc",
                List.of(), LocalDate.now(), JobStatus.CLOSED, 10L);
    }

    @Test
    @DisplayName("Sikeres jelentkezés leadása ha az állás létezik és ACTIVE")
    void shouldSubmitApplicationWhenJobExistsAndIsActive() {
        SubmitApplicationCommand command = new SubmitApplicationCommand(
                1L, "Nagy Anna", "anna@example.com", "cv.pdf", 100L, "application/pdf", "data".getBytes(StandardCharsets.UTF_8)
        );
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(activeJob));

        JobApplication saved = new JobApplication(100L, 1L, "Nagy Anna", "anna@example.com", "cv.pdf", Instant.now(),
                100L, ApplicationStatus.PENDING, "application/pdf", "data".getBytes(StandardCharsets.UTF_8));
        when(applicationRepositoryPort.save(any(JobApplication.class))).thenReturn(saved);

        JobApplication result = applicationService.submitApplication(command);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getApplicantName()).isEqualTo("Nagy Anna");
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        verify(jobRepositoryPort).findById(1L);
        verify(applicationRepositoryPort).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Hiba dobása ha a jelentkező már jelentkezett az adott állásra")
    void shouldThrowInvalidApplicationWhenApplicantAlreadyApplied() {
        SubmitApplicationCommand command = new SubmitApplicationCommand(
                1L, "Nagy Anna", "anna@example.com", "cv.pdf", 100L, "application/pdf", "data".getBytes(StandardCharsets.UTF_8)
        );
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(activeJob));
        when(applicationRepositoryPort.existsByJobIdAndApplicant(1L, 100L, "anna@example.com")).thenReturn(true);

        assertThatThrownBy(() -> applicationService.submitApplication(command))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Már jelentkeztél erre az állásra.");

        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Hiba dobása ha LEZÁRT (CLOSED) álláshirdetésre próbálnak jelentkezni")
    void shouldThrowInvalidApplicationWhenJobIsClosed() {
        SubmitApplicationCommand command = new SubmitApplicationCommand(2L, "Nagy Anna", "anna@example.com", "cv.pdf");
        when(jobRepositoryPort.findById(2L)).thenReturn(Optional.of(closedJob));

        assertThatThrownBy(() -> applicationService.submitApplication(command))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("Lezárt álláshirdetésre nem lehet jelentkezni.");

        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("JobNotFoundException dobása ha nem létező állásra jelentkeznek")
    void shouldThrowJobNotFoundWhenJobDoesNotExist() {
        SubmitApplicationCommand command = new SubmitApplicationCommand(999L, "Nagy Anna", "anna@example.com", "cv.pdf");
        when(jobRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.submitApplication(command))
                .isInstanceOf(JobNotFoundException.class)
                .hasMessageContaining("999");

        verify(applicationRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("getApplicationsForApplicant visszaadja a jelentkező saját jelentkezéseit a megfelelő státuszüzenettel")
    void shouldReturnApplicationsForApplicantWithStatusMessage() {
        JobApplication app = new JobApplication(50L, 1L, "Nagy Anna", "anna@example.com", "cv.pdf", Instant.now(),
                100L, ApplicationStatus.ACCEPTED, "application/pdf", null);

        when(applicationRepositoryPort.findByApplicant(100L, "anna@example.com")).thenReturn(List.of(app));
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(activeJob));

        List<ApplicantApplicationView> views = applicationService.getApplicationsForApplicant(100L, "anna@example.com");

        assertThat(views).hasSize(1);
        ApplicantApplicationView view = views.get(0);
        assertThat(view.jobTitle()).isEqualTo("React Fejlesztő");
        assertThat(view.company()).isEqualTo("TechCorp");
        assertThat(view.status()).isEqualTo("ACCEPTED");
        assertThat(view.statusMessage()).isEqualTo("A cég értesíti Önt a továbbiakról.");
    }

    @Test
    @DisplayName("updateStatus sikeresen frissíti a jelentkezés státuszát a hirdetés létrehozója számára")
    void shouldUpdateStatusByCreator() {
        JobApplication app = new JobApplication(50L, 1L, "Nagy Anna", "anna@example.com", "cv.pdf", Instant.now(),
                100L, ApplicationStatus.PENDING, "application/pdf", null);
        when(applicationRepositoryPort.findById(50L)).thenReturn(Optional.of(app));
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(activeJob));
        when(applicationRepositoryPort.save(any(JobApplication.class))).thenAnswer(i -> i.getArgument(0));

        JobApplication updated = applicationService.updateStatus(50L, ApplicationStatus.ACCEPTED, 10L);

        assertThat(updated.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
        verify(applicationRepositoryPort).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("updateStatus 403 / UnauthorizedAccessException ha nem a létrehozó próbál státuszt módosítani")
    void shouldThrowUnauthorizedWhenDifferentEmployerAttemptsStatusChange() {
        JobApplication app = new JobApplication(50L, 1L, "Nagy Anna", "anna@example.com", "cv.pdf", Instant.now(),
                100L, ApplicationStatus.PENDING, "application/pdf", null);
        when(applicationRepositoryPort.findById(50L)).thenReturn(Optional.of(app));
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(activeJob));

        assertThatThrownBy(() -> applicationService.updateStatus(50L, ApplicationStatus.ACCEPTED, 999L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("Csak az álláshirdetés létrehozója módosíthatja");
    }

    @Test
    @DisplayName("getResume visszaadja a feltöltött önéletrajzot a létrehozónak")
    void shouldReturnResumeForEmployer() {
        byte[] resumeBytes = "Mock PDF data".getBytes(StandardCharsets.UTF_8);
        JobApplication app = new JobApplication(50L, 1L, "Nagy Anna", "anna@example.com", "cv.pdf", Instant.now(),
                100L, ApplicationStatus.PENDING, "application/pdf", resumeBytes);
        when(applicationRepositoryPort.findById(50L)).thenReturn(Optional.of(app));
        when(jobRepositoryPort.findById(1L)).thenReturn(Optional.of(activeJob));

        ResumeFile resume = applicationService.getResume(50L, 10L);

        assertThat(resume.fileName()).isEqualTo("cv.pdf");
        assertThat(resume.contentType()).isEqualTo("application/pdf");
        assertThat(resume.data()).isEqualTo(resumeBytes);
    }
}
