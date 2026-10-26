package com.jobboard.jobs.infrastructure.persistence;

import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import com.jobboard.jobs.domain.model.JobType;
import com.jobboard.jobs.infrastructure.persistence.adapter.JobJpaAdapter;
import com.jobboard.jobs.infrastructure.persistence.mapper.JobPersistenceMapper;
import com.jobboard.jobs.infrastructure.persistence.repository.SpringDataJobRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JobJpaAdapter.class, JobPersistenceMapper.class})
class JobJpaAdapterTest {

    @Autowired
    private JobJpaAdapter jobJpaAdapter;

    @Autowired
    private SpringDataJobRepository springDataJobRepository;

    @Test
    @DisplayName("Álláshirdetés perzisztálása és lekérdezése JPA adapteren keresztül")
    void shouldSaveAndFindJob() {
        Job job = Job.createNew(
                "Spring Boot Fejlesztő",
                "JavaTech",
                JobType.FULL_TIME,
                "Budapest",
                "Java",
                "1 400 000 HUF",
                "Backend fejlesztés",
                List.of("Java 21", "Spring Boot", "JPA"),
                10L
        );

        Job saved = jobJpaAdapter.save(job);
        assertThat(saved.getId()).isNotNull();

        Optional<Job> found = jobJpaAdapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spring Boot Fejlesztő");
        assertThat(found.get().getRequirements()).containsExactly("Java 21", "Spring Boot", "JPA");
        assertThat(found.get().getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(found.get().getCreatedByUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("searchJobs csak ACTIVE hirdetéseket ad vissza és elrejti a CLOSED hirdetéseket")
    void shouldSearchAndFilterActiveJobsOnly() {
        Job activeJob = Job.createNew("React Dev", "Alfa Kft", JobType.FULL_TIME, "BP", "React", "1M", "Desc", List.of(), 5L);
        Job savedActive = jobJpaAdapter.save(activeJob);

        Job closedJob = new Job(null, "Closed Dev", "Alfa Kft", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.CLOSED, 5L);
        jobJpaAdapter.save(closedJob);

        List<Job> activeResults = jobJpaAdapter.searchJobs("Alfa", "React");
        assertThat(activeResults).hasSize(1);
        assertThat(activeResults.get(0).getTitle()).isEqualTo("React Dev");
        assertThat(activeResults.get(0).getStatus()).isEqualTo(JobStatus.ACTIVE);
    }

    @Test
    @DisplayName("findByCreatedByUserId visszaadja a cégvezető összes hirdetését (aktív és lezárt)")
    void shouldFindAllJobsByCreator() {
        Job activeJob = new Job(null, "React Dev", "Alfa Kft", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 100L);
        Job closedJob = new Job(null, "Closed Dev", "Alfa Kft", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.CLOSED, 100L);
        Job otherJob = new Job(null, "Other Dev", "Beta Kft", JobType.FULL_TIME, "BP", "Java", "1M", "Desc",
                List.of(), LocalDate.now(), JobStatus.ACTIVE, 200L);

        jobJpaAdapter.save(activeJob);
        jobJpaAdapter.save(closedJob);
        jobJpaAdapter.save(otherJob);

        List<Job> creatorJobs = jobJpaAdapter.findByCreatedByUserId(100L);
        assertThat(creatorJobs).hasSize(2);
    }
}
