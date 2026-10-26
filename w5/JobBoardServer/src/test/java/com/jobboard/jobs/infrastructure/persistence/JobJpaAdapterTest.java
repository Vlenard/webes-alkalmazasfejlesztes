package com.jobboard.jobs.infrastructure.persistence;

import com.jobboard.jobs.domain.model.Job;
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
                List.of("Java 21", "Spring Boot", "JPA")
        );

        Job saved = jobJpaAdapter.save(job);
        assertThat(saved.getId()).isNotNull();

        Optional<Job> found = jobJpaAdapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spring Boot Fejlesztő");
        assertThat(found.get().getRequirements()).containsExactly("Java 21", "Spring Boot", "JPA");
    }

    @Test
    @DisplayName("searchJobs helyesen szűr cím, cég és badge alapján")
    void shouldSearchAndFilterJobs() {
        Job job1 = Job.createNew("React Dev", "Alfa Kft", JobType.FULL_TIME, "BP", "React", "1M", "Desc", List.of());
        Job job2 = Job.createNew("Java Dev", "Beta Kft", JobType.REMOTE, "Remote", "Java", "1.2M", "Desc", List.of());
        jobJpaAdapter.save(job1);
        jobJpaAdapter.save(job2);

        List<Job> reactJobs = jobJpaAdapter.searchJobs("Alfa", "React");
        assertThat(reactJobs).hasSize(1);
        assertThat(reactJobs.get(0).getTitle()).isEqualTo("React Dev");

        List<Job> allJobs = jobJpaAdapter.searchJobs(null, null);
        assertThat(allJobs).hasSize(2);
    }
}
