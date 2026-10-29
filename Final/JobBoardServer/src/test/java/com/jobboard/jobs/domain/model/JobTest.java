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
    @DisplayName("Sikeres Job entitás létrehozás érvényes adatokkal, alapértelmezett ACTIVE státusszal")
    void shouldCreateJobWhenDataIsValid() {
        Job job = Job.createNew(
                "Frontend Fejlesztő",
                "DevCorp",
                JobType.FULL_TIME,
                "Budapest",
                "React",
                "1 000 000 HUF",
                "Fejlesztői feladatok ellátása.",
                List.of("React", "TypeScript"),
                10L
        );

        assertThat(job.getTitle()).isEqualTo("Frontend Fejlesztő");
        assertThat(job.getCompany()).isEqualTo("DevCorp");
        assertThat(job.getType()).isEqualTo(JobType.FULL_TIME);
        assertThat(job.getLocation()).isEqualTo("Budapest");
        assertThat(job.getBadge()).isEqualTo("React");
        assertThat(job.getRequirements()).containsExactly("React", "TypeScript");
        assertThat(job.getPostedAt()).isEqualTo(LocalDate.now());
        assertThat(job.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(job.isActive()).isTrue();
        assertThat(job.isClosed()).isFalse();
        assertThat(job.getCreatedByUserId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Státusz módosítása a létrehozó által (ACTIVE -> CLOSED -> ACTIVE)")
    void shouldAllowStatusChangeByCreator() {
        Job job = Job.createNew(
                "Backend Fejlesztő", "Tech Kft", JobType.FULL_TIME, "BP", "Java", "1.2M", "Desc", List.of(), 5L
        );

        Job closed = job.withStatusChangedBy(JobStatus.CLOSED, 5L);
        assertThat(closed.getStatus()).isEqualTo(JobStatus.CLOSED);
        assertThat(closed.isActive()).isFalse();
        assertThat(closed.isClosed()).isTrue();

        Job reopened = closed.withStatusChangedBy(JobStatus.ACTIVE, 5L);
        assertThat(reopened.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(reopened.isActive()).isTrue();
    }

    @Test
    @DisplayName("Hiba dobása ha nem a létrehozó próbálja módosítani a hirdetést")
    void shouldThrowExceptionWhenNonCreatorAttemptsStatusChange() {
        Job job = Job.createNew(
                "Backend Fejlesztő", "Tech Kft", JobType.FULL_TIME, "BP", "Java", "1.2M", "Desc", List.of(), 5L
        );

        assertThatThrownBy(() -> job.withStatusChangedBy(JobStatus.CLOSED, 99L))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessageContaining("Csak a hirdetés létrehozója módosíthatja vagy nyithatja újra a hirdetést.");
    }

    @Test
    @DisplayName("Hiba dobása ha a státusz null")
    void shouldThrowExceptionWhenStatusIsNull() {
        Job job = Job.createNew(
                "Backend Fejlesztő", "Tech Kft", JobType.FULL_TIME, "BP", "Java", "1.2M", "Desc", List.of(), 5L
        );

        assertThatThrownBy(() -> job.withStatusChangedBy(null, 5L))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessageContaining("A státusz megadása kötelező.");
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

    @Test
    @DisplayName("Skillek normalizálása, trimmelése és kis/nagybetű független deduplikálása az első formátum megőrzésével")
    void shouldNormalizeAndDeduplicateSkills() {
        Job job = Job.createNew(
                "Frontend Fejlesztő", "DevCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), 10L, List.of("  React  ", "typescript", "REACT", "", "   ", "TypeScript", "Tailwind CSS")
        );

        assertThat(job.getSkills()).containsExactly("React", "typescript", "Tailwind CSS");
    }

    @Test
    @DisplayName("Skillek sikeres módosítása a létrehozó által")
    void shouldAllowSkillsChangeByCreator() {
        Job job = Job.createNew(
                "Frontend Fejlesztő", "DevCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), 10L, List.of("React")
        );

        Job updated = job.withSkillsChangedBy(List.of("React", "TypeScript", "Next.js"), 10L);
        assertThat(updated.getSkills()).containsExactly("React", "TypeScript", "Next.js");
        assertThat(updated.getTitle()).isEqualTo(job.getTitle());
    }

    @Test
    @DisplayName("Hiba dobása ha nem a hirdetés létrehozója módosítaná a skilleket")
    void shouldThrowExceptionWhenNonCreatorAttemptsSkillsChange() {
        Job job = Job.createNew(
                "Frontend Fejlesztő", "DevCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), 10L, List.of("React")
        );

        // Eltérő azonosítójú felhasználó
        assertThatThrownBy(() -> job.withSkillsChangedBy(List.of("Java"), 99L))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessageContaining("Csak a hirdetés létrehozója módosíthatja a szükséges skilleket.");

        // Null azonosítójú felhasználó
        assertThatThrownBy(() -> job.withSkillsChangedBy(List.of("Java"), null))
                .isInstanceOf(InvalidJobDataException.class)
                .hasMessageContaining("Csak a hirdetés létrehozója módosíthatja a szükséges skilleket.");
    }

    @Test
    @DisplayName("withSkillsChangedBy: a skillek normalizálása, deduplikálása és az entitás mezőinek maradéktalan megőrzése")
    void shouldNormalizeSkillsAndPreserveAllJobFieldsOnSkillsChange() {
        Job job = new Job(
                123L,
                "Senior Backend Fejlesztő",
                "CloudTech Kft",
                JobType.REMOTE,
                "Budapest",
                "Java",
                "1 500 000 HUF",
                "Részletes leírás",
                List.of("Java 21", "Spring Boot"),
                LocalDate.of(2026, 1, 1),
                JobStatus.ACTIVE,
                42L,
                List.of("Java")
        );

        List<String> rawSkills = List.of(
                "  Vue.js  ",
                "vue.js",
                "VUE.JS",
                "   ",
                "",
                "Pinia",
                "  pinia  ",
                "TypeScript"
        );

        Job updated = job.withSkillsChangedBy(rawSkills, 42L);

        // Skillek ellenőrzése
        assertThat(updated.getSkills()).containsExactly("Vue.js", "Pinia", "TypeScript");
        assertThatThrownBy(() -> updated.getSkills().add("Docker"))
                .isInstanceOf(UnsupportedOperationException.class);

        // Összes többi mező megőrzésének ellenőrzése
        assertThat(updated.getId()).isEqualTo(123L);
        assertThat(updated.getTitle()).isEqualTo("Senior Backend Fejlesztő");
        assertThat(updated.getCompany()).isEqualTo("CloudTech Kft");
        assertThat(updated.getType()).isEqualTo(JobType.REMOTE);
        assertThat(updated.getLocation()).isEqualTo("Budapest");
        assertThat(updated.getBadge()).isEqualTo("Java");
        assertThat(updated.getSalary()).isEqualTo("1 500 000 HUF");
        assertThat(updated.getDescription()).isEqualTo("Részletes leírás");
        assertThat(updated.getRequirements()).containsExactly("Java 21", "Spring Boot");
        assertThat(updated.getPostedAt()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(updated.getStatus()).isEqualTo(JobStatus.ACTIVE);
        assertThat(updated.getCreatedByUserId()).isEqualTo(42L);

        // Null és üres skillek kezelése
        Job emptySkillsJob = job.withSkillsChangedBy(null, 42L);
        assertThat(emptySkillsJob.getSkills()).isEmpty();

        Job blankSkillsJob = job.withSkillsChangedBy(List.of("  ", ""), 42L);
        assertThat(blankSkillsJob.getSkills()).isEmpty();
    }

    @Test
    @DisplayName("withStatusChangedBy és withCreatedByUserId megőrzi a beállított skilleket")
    void shouldPreserveSkillsOnStatusAndCreatorChanges() {
        Job job = Job.createNew(
                "Frontend Fejlesztő", "DevCorp", JobType.FULL_TIME, "BP", "React", "1M", "Desc",
                List.of(), 10L, List.of("React", "TypeScript")
        );

        Job closed = job.withStatusChangedBy(JobStatus.CLOSED, 10L);
        assertThat(closed.getSkills()).containsExactly("React", "TypeScript");

        Job reassigned = job.withCreatedByUserId(20L);
        assertThat(reassigned.getSkills()).containsExactly("React", "TypeScript");
    }
}
