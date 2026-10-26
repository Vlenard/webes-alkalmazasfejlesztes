package com.jobboard.jobs.domain.model;

import com.jobboard.jobs.domain.exception.InvalidJobDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobTest {

    @Test
    @DisplayName("Sikeres Job entitás létrehozás érvényes adatokkal")
    void shouldCreateJobWhenDataIsValid() {
        Job job = Job.createNew(
                "Frontend Fejlesztő",
                "DevCorp",
                JobType.FULL_TIME,
                "Budapest",
                "React",
                "1 000 000 HUF",
                "Fejlesztői feladatok ellátása.",
                List.of("React", "TypeScript")
        );

        assertThat(job.getTitle()).isEqualTo("Frontend Fejlesztő");
        assertThat(job.getCompany()).isEqualTo("DevCorp");
        assertThat(job.getType()).isEqualTo(JobType.FULL_TIME);
        assertThat(job.getLocation()).isEqualTo("Budapest");
        assertThat(job.getBadge()).isEqualTo("React");
        assertThat(job.getRequirements()).containsExactly("React", "TypeScript");
        assertThat(job.getPostedAt()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Hiba dobása ha a megnevezés üres")
    void shouldThrowExceptionWhenTitleIsBlank() {
        assertThatThrownBy(() -> Job.createNew(
                "",
                "DevCorp",
                JobType.FULL_TIME,
                "Budapest",
                "React",
                "1 000 000 HUF",
                "Fejlesztői feladatok.",
                List.of()
        )).isInstanceOf(InvalidJobDataException.class)
          .hasMessageContaining("A pozíció megnevezése nem lehet üres");
    }

    @Test
    @DisplayName("Hiba dobása ha a cég neve üres")
    void shouldThrowExceptionWhenCompanyIsBlank() {
        assertThatThrownBy(() -> Job.createNew(
                "Backend Fejlesztő",
                "   ",
                JobType.FULL_TIME,
                "Budapest",
                "Java",
                "1 200 000 HUF",
                "Fejlesztői feladatok.",
                List.of()
        )).isInstanceOf(InvalidJobDataException.class)
          .hasMessageContaining("A cég neve nem lehet üres");
    }

    @Test
    @DisplayName("Keresési egyezés ellenőrzése cím és cégnév alapján kis/nagybetű függetlenül")
    void shouldMatchSearchQueryCaseInsensitively() {
        Job job = Job.createNew(
                "Senior Java Fejlesztő",
                "CloudTech Inc.",
                JobType.REMOTE,
                "Remote",
                "Spring Boot",
                "1 500 000 HUF",
                "Mikroszolgáltatások építése.",
                List.of()
        );

        assertThat(job.matchesSearch("java")).isTrue();
        assertThat(job.matchesSearch("JAVA")).isTrue();
        assertThat(job.matchesSearch("cloudtech")).isTrue();
        assertThat(job.matchesSearch("frontend")).isFalse();
        assertThat(job.matchesSearch(null)).isTrue();
        assertThat(job.matchesSearch("")).isTrue();
    }

    @Test
    @DisplayName("Badge szerinti szűrés ellenőrzése")
    void shouldMatchBadgeFilter() {
        Job job = Job.createNew(
                "Senior Java Fejlesztő",
                "CloudTech Inc.",
                JobType.REMOTE,
                "Remote",
                "Spring Boot",
                "1 500 000 HUF",
                "Mikroszolgáltatások építése.",
                List.of()
        );

        assertThat(job.matchesBadge("Spring Boot")).isTrue();
        assertThat(job.matchesBadge("spring boot")).isTrue();
        assertThat(job.matchesBadge("React")).isFalse();
        assertThat(job.matchesBadge(null)).isTrue();
    }
}
