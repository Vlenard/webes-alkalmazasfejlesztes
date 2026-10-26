package com.jobboard.shared.infrastructure.seeder;

import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Initializes database with the initial preset job listings matching the frontend.
 */
@Component
public class JobDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobDataSeeder.class);

    private final JobRepositoryPort jobRepositoryPort;

    public JobDataSeeder(JobRepositoryPort jobRepositoryPort) {
        this.jobRepositoryPort = jobRepositoryPort;
    }

    @Override
    public void run(String... args) {
        if (jobRepositoryPort.count() == 0) {
            log.info("Adatbázis üres - kezdeti JobBoard álláshirdetések betöltése...");

            List<Job> initialJobs = List.of(
                    new Job(
                            null,
                            "Senior Frontend Fejlesztő",
                            "TechCorp Hungary",
                            JobType.FULL_TIME,
                            "Budapest / Hibrid",
                            "React",
                            "1 200 000 - 1 600 000 HUF",
                            "Modern React + TypeScript alapú SPA fejlesztése kiemelt vállalati partnerek számára.",
                            List.of(
                                    "3+ év React tapasztalat",
                                    "TypeScript és modern tooling ismerete",
                                    "Tailwind CSS / daisyUI gyakorlat"
                            ),
                            LocalDate.of(2026, 9, 14)
                    ),
                    new Job(
                            null,
                            "Fullstack Fejlesztő (Node.js + React)",
                            "NextWave Digital",
                            JobType.REMOTE,
                            "Remote",
                            "Node.js",
                            "1 000 000 - 1 400 000 HUF",
                            "Skálázható mikroszolgáltatások és reszponzív felületek tervezése és implementálása.",
                            List.of(
                                    "Node.js / Express vagy NestJS",
                                    "React és REST API tervezés",
                                    "PostgreSQL vagy MongoDB"
                            ),
                            LocalDate.of(2026, 9, 13)
                    ),
                    new Job(
                            null,
                            "UI/UX & Product Designer",
                            "InnoStudio",
                            JobType.PART_TIME,
                            "Budapest",
                            "Figma",
                            "600 000 - 800 000 HUF",
                            "Felhasználóbarát felületek, design rendszerek és prototípusok tervezése Figma környezetben.",
                            List.of(
                                    "Figma komponens-rendszerek ismerete",
                                    "Design system tapasztalat",
                                    "User journey mapping"
                            ),
                            LocalDate.of(2026, 9, 12)
                    ),
                    new Job(
                            null,
                            "DevOps & Cloud Mérnök",
                            "CloudScale Solutions",
                            JobType.FULL_TIME,
                            "Remote / Budapest",
                            "Kubernetes",
                            "1 300 000 - 1 800 000 HUF",
                            "CI/CD pipeline-ok karbantartása, konténerizáció és felhő infrastruktúra automatizáció.",
                            List.of(
                                    "Docker, Kubernetes",
                                    "GitHub Actions / GitLab CI",
                                    "AWS vagy GCP tapasztalat"
                            ),
                            LocalDate.of(2026, 9, 10)
                    )
            );

            initialJobs.forEach(jobRepositoryPort::save);
            log.info("Kezdeti adatok sikeresen betöltve. Összes hirdetés: {}", jobRepositoryPort.count());
        }
    }
}
