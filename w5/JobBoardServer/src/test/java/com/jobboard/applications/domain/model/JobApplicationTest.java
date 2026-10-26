package com.jobboard.applications.domain.model;

import com.jobboard.applications.domain.exception.InvalidApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobApplicationTest {

    @Test
    @DisplayName("Sikeres JobApplication létrehozása érvényes adatokkal")
    void shouldCreateApplicationSuccessfully() {
        JobApplication application = JobApplication.createNew(
                1L,
                "Kiss János",
                "kiss.janos@example.com",
                "cv.pdf"
        );

        assertThat(application.getJobId()).isEqualTo(1L);
        assertThat(application.getApplicantName()).isEqualTo("Kiss János");
        assertThat(application.getApplicantEmail()).isEqualTo("kiss.janos@example.com");
        assertThat(application.getResumeFileName()).isEqualTo("cv.pdf");
        assertThat(application.getSubmittedAt()).isNotNull();
    }

    @Test
    @DisplayName("Hiba dobása érvénytelen email cím esetén")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        assertThatThrownBy(() -> JobApplication.createNew(
                1L,
                "Kiss János",
                "invalid-email",
                null
        )).isInstanceOf(InvalidApplicationException.class)
          .hasMessageContaining("Érvényes email cím megadása kötelező");
    }

    @Test
    @DisplayName("Hiba dobása üres név esetén")
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> JobApplication.createNew(
                1L,
                "   ",
                "kiss.janos@example.com",
                null
        )).isInstanceOf(InvalidApplicationException.class)
          .hasMessageContaining("A jelentkező neve nem lehet üres");
    }

    @Test
    @DisplayName("Hiba dobása ha a jobId negatív vagy 0")
    void shouldThrowExceptionWhenJobIdIsInvalid() {
        assertThatThrownBy(() -> JobApplication.createNew(
                0L,
                "Kiss János",
                "kiss.janos@example.com",
                null
        )).isInstanceOf(InvalidApplicationException.class)
          .hasMessageContaining("Érvénytelen állás azonosító");
    }
}
