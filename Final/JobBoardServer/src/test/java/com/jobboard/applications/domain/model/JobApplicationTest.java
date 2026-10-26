package com.jobboard.applications.domain.model;

import com.jobboard.applications.domain.exception.InvalidApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobApplicationTest {

    @Test
    @DisplayName("Sikeres JobApplication létrehozása érvényes adatokkal és PDF önéletrajzzal")
    void shouldCreateApplicationSuccessfully() {
        byte[] pdfBytes = "%PDF-1.4 mock".getBytes(StandardCharsets.UTF_8);
        JobApplication application = JobApplication.createNew(
                1L,
                "Kiss János",
                "kiss.janos@example.com",
                "cv.pdf",
                10L,
                "application/pdf",
                pdfBytes
        );

        assertThat(application.getJobId()).isEqualTo(1L);
        assertThat(application.getApplicantName()).isEqualTo("Kiss János");
        assertThat(application.getApplicantEmail()).isEqualTo("kiss.janos@example.com");
        assertThat(application.getResumeFileName()).isEqualTo("cv.pdf");
        assertThat(application.getSubmittedAt()).isNotNull();
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(application.getApplicantUserId()).isEqualTo(10L);
        assertThat(application.hasResume()).isTrue();
        assertThat(application.getResumeData()).isEqualTo(pdfBytes);
    }

    @Test
    @DisplayName("Sikeres DOCX önéletrajz kiterjesztés elfogadása")
    void shouldAcceptDocxResume() {
        JobApplication application = JobApplication.createNew(
                1L, "Nagy Éva", "eva@example.com", "oneletrajz.DOCX"
        );
        assertThat(application.getResumeFileName()).isEqualTo("oneletrajz.DOCX");
    }

    @Test
    @DisplayName("Hiba dobása nem támogatott fájlformátum esetén (.doc, .txt, .png)")
    void shouldThrowExceptionForUnsupportedFileExtensions() {
        assertThatThrownBy(() -> JobApplication.createNew(
                1L, "Kiss János", "kiss@example.com", "resume.txt"
        )).isInstanceOf(InvalidApplicationException.class)
          .hasMessageContaining("Csak PDF vagy DOCX formátumú önéletrajz tölthető fel.");

        assertThatThrownBy(() -> JobApplication.createNew(
                1L, "Kiss János", "kiss@example.com", "resume.doc"
        )).isInstanceOf(InvalidApplicationException.class)
          .hasMessageContaining("Csak PDF vagy DOCX formátumú önéletrajz tölthető fel.");

        assertThatThrownBy(() -> JobApplication.createNew(
                1L, "Kiss János", "kiss@example.com", "picture.png"
        )).isInstanceOf(InvalidApplicationException.class)
          .hasMessageContaining("Csak PDF vagy DOCX formátumú önéletrajz tölthető fel.");
    }

    @Test
    @DisplayName("Státusz módosítása (PENDING -> ACCEPTED / REJECTED)")
    void shouldUpdateStatusCorrectly() {
        JobApplication app = JobApplication.createNew(1L, "Kiss János", "kiss@example.com", "cv.pdf");
        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.PENDING);

        JobApplication accepted = app.withStatus(ApplicationStatus.ACCEPTED);
        assertThat(accepted.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);

        JobApplication rejected = app.withStatus(ApplicationStatus.REJECTED);
        assertThat(rejected.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    @DisplayName("Hiba dobása ha a státusz null")
    void shouldThrowWhenStatusIsNull() {
        JobApplication app = JobApplication.createNew(1L, "Kiss János", "kiss@example.com", "cv.pdf");
        assertThatThrownBy(() -> app.withStatus(null))
                .isInstanceOf(InvalidApplicationException.class)
                .hasMessageContaining("A státusz megadása kötelező.");
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
