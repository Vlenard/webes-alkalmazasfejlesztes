package com.jobboard.applications.infrastructure.persistence;

import com.jobboard.applications.domain.model.ApplicationStatus;
import com.jobboard.applications.domain.model.JobApplication;
import com.jobboard.applications.infrastructure.persistence.adapter.ApplicationJpaAdapter;
import com.jobboard.applications.infrastructure.persistence.mapper.ApplicationPersistenceMapper;
import com.jobboard.applications.infrastructure.persistence.repository.SpringDataJobApplicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ApplicationJpaAdapter.class, ApplicationPersistenceMapper.class})
class ApplicationJpaAdapterTest {

    @Autowired
    private ApplicationJpaAdapter applicationJpaAdapter;

    @Autowired
    private SpringDataJobApplicationRepository springDataJobApplicationRepository;

    @Test
    @DisplayName("Jelentkezés és önéletrajz bináris adat perzisztálása és visszatöltése JPA adapteren keresztül")
    void shouldSaveAndFindApplication() {
        byte[] pdfBytes = "%PDF-1.4 mock content".getBytes(StandardCharsets.UTF_8);
        JobApplication application = new JobApplication(
                null,
                1L,
                "Teszt Elek",
                "elek@teszt.hu",
                "elek_cv.pdf",
                Instant.now(),
                5L,
                ApplicationStatus.PENDING,
                "application/pdf",
                pdfBytes
        );

        JobApplication saved = applicationJpaAdapter.save(application);
        assertThat(saved.getId()).isNotNull();

        Optional<JobApplication> found = applicationJpaAdapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getApplicantName()).isEqualTo("Teszt Elek");
        assertThat(found.get().getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(found.get().getResumeFileName()).isEqualTo("elek_cv.pdf");
        assertThat(found.get().getResumeData()).isEqualTo(pdfBytes);
        assertThat(found.get().hasResume()).isTrue();
    }

    @Test
    @DisplayName("findByApplicant visszaadja a felhasználó vagy email alapján a jelentkezéseket")
    void shouldFindApplicationsByApplicant() {
        JobApplication app1 = new JobApplication(
                null, 1L, "Kiss János", "janos@example.com", "cv.pdf", Instant.now(),
                10L, ApplicationStatus.ACCEPTED, "application/pdf", null
        );
        JobApplication app2 = new JobApplication(
                null, 2L, "Kiss János", "janos@example.com", "cv.docx", Instant.now(),
                10L, ApplicationStatus.PENDING, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", null
        );
        JobApplication appOther = new JobApplication(
                null, 1L, "Másik Béla", "bela@example.com", "cv.pdf", Instant.now(),
                20L, ApplicationStatus.PENDING, "application/pdf", null
        );

        applicationJpaAdapter.save(app1);
        applicationJpaAdapter.save(app2);
        applicationJpaAdapter.save(appOther);

        List<JobApplication> userApps = applicationJpaAdapter.findByApplicant(10L, "janos@example.com");
        assertThat(userApps).hasSize(2);
    }

    @Test
    @DisplayName("existsByJobIdAndApplicant helyesen ellenőrzi a felhasználóazonosító vagy email egyezést")
    void shouldCheckExistingApplication() {
        JobApplication app = new JobApplication(
                null, 1L, "Kiss János", "janos@example.com", "cv.pdf", Instant.now(),
                10L, ApplicationStatus.PENDING, "application/pdf", null
        );
        applicationJpaAdapter.save(app);

        assertThat(applicationJpaAdapter.existsByJobIdAndApplicant(1L, 10L, null)).isTrue();
        assertThat(applicationJpaAdapter.existsByJobIdAndApplicant(1L, null, "JANOS@EXAMPLE.COM")).isTrue();
        assertThat(applicationJpaAdapter.existsByJobIdAndApplicant(1L, 99L, "other@example.com")).isFalse();
        assertThat(applicationJpaAdapter.existsByJobIdAndApplicant(2L, 10L, "janos@example.com")).isFalse();
    }
}
