package com.jobboard.shared.infrastructure.seeder;

import com.jobboard.auth.application.port.out.PasswordEncoderPort;
import com.jobboard.auth.application.port.out.UserRepositoryPort;
import com.jobboard.auth.domain.model.User;
import com.jobboard.auth.domain.model.UserRole;
import com.jobboard.jobs.application.port.out.JobRepositoryPort;
import com.jobboard.jobs.domain.model.Job;
import com.jobboard.jobs.domain.model.JobStatus;
import com.jobboard.jobs.domain.model.JobType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Initializes database with default users and preset job listings matching the frontend.
 */
@Component
public class JobDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobDataSeeder.class);

    private final JobRepositoryPort jobRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;

    @Autowired
    public JobDataSeeder(JobRepositoryPort jobRepositoryPort,
                         @Autowired(required = false) UserRepositoryPort userRepositoryPort,
                         @Autowired(required = false) PasswordEncoderPort passwordEncoderPort) {
        this.jobRepositoryPort = jobRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    public JobDataSeeder(JobRepositoryPort jobRepositoryPort) {
        this(jobRepositoryPort, null, null);
    }

    @Override
    public void run(String... args) {
        Long defaultCompanyManagerId = null;

        if (userRepositoryPort != null && passwordEncoderPort != null) {
            String defaultPasswordHash = passwordEncoderPort.encode("password123");

            User manager = userRepositoryPort.findByEmail("hr@techcorp.hu").orElseGet(() -> {
                log.info("Alapértelmezett céges kezelő (hr@techcorp.hu) létrehozása...");
                User u = User.createNew("TechCorp HR", "hr@techcorp.hu", defaultPasswordHash, UserRole.COMPANY_MANAGER, "TechCorp Hungary");
                return userRepositoryPort.save(u);
            });
            defaultCompanyManagerId = manager.getId();

            if (userRepositoryPort.findByEmail("jelentkezo@pelda.hu").isEmpty()) {
                log.info("Alapértelmezett jelentkező (jelentkezo@pelda.hu) létrehozása...");
                User applicant = User.createNew("Példa Jelentkező", "jelentkezo@pelda.hu", defaultPasswordHash, UserRole.APPLICANT, null);
                userRepositoryPort.save(applicant);
            }
        }

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
                            LocalDate.of(2026, 9, 14),
                            JobStatus.ACTIVE,
                            defaultCompanyManagerId
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
                            LocalDate.of(2026, 9, 13),
                            JobStatus.ACTIVE,
                            defaultCompanyManagerId
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
                            LocalDate.of(2026, 9, 12),
                            JobStatus.ACTIVE,
                            defaultCompanyManagerId
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
                            LocalDate.of(2026, 9, 10),
                            JobStatus.ACTIVE,
                            defaultCompanyManagerId
                    )
            );

            initialJobs.forEach(jobRepositoryPort::save);
            log.info("Kezdeti adatok sikeresen betöltve. Összes hirdetés: {}", jobRepositoryPort.count());
        }

        // Backfill createdByUserId for any existing jobs in database that have null createdByUserId
        if (userRepositoryPort != null) {
            final Long fallbackManagerId = defaultCompanyManagerId;
            List<Job> allJobs = jobRepositoryPort.findAll();
            List<User> allUsers = userRepositoryPort.findAll();

            for (Job job : allJobs) {
                if (job.getCreatedByUserId() == null) {
                    Long targetUserId = allUsers.stream()
                            .filter(User::isCompanyManager)
                            .filter(u -> u.getCompanyName() != null && !u.getCompanyName().isBlank()
                                    && u.getCompanyName().trim().equalsIgnoreCase(job.getCompany().trim()))
                            .map(User::getId)
                            .findFirst()
                            .orElse(fallbackManagerId);

                    if (targetUserId != null) {
                        Job updated = job.withCreatedByUserId(targetUserId);
                        jobRepositoryPort.save(updated);
                        log.info("Job #{} ('{}', cég: '{}') hozzárendelve createdByUserId={}",
                                job.getId(), job.getTitle(), job.getCompany(), targetUserId);
                    }
                }
            }
        }
    }
}
