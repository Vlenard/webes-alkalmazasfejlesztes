package com.jobboard.shared.infrastructure.seeder;

import com.jobboard.applications.application.port.out.ApplicationRepositoryPort;
import com.jobboard.applications.domain.model.ApplicationStatus;
import com.jobboard.applications.domain.model.JobApplication;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Initializes database with default users, rich job listings, and sample job applications
 * covering all features of the Final version (roles, search/filter, status lifecycles, and resume attachments).
 */
@Component
public class JobDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(JobDataSeeder.class);

    private static final byte[] SAMPLE_PDF_BYTES = "%PDF-1.4 mock cv content".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SAMPLE_DOCX_BYTES = "PK\003\004 mock docx cv content".getBytes(StandardCharsets.UTF_8);

    private final JobRepositoryPort jobRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final ApplicationRepositoryPort applicationRepositoryPort;

    @Autowired
    public JobDataSeeder(JobRepositoryPort jobRepositoryPort,
                         @Autowired(required = false) UserRepositoryPort userRepositoryPort,
                         @Autowired(required = false) PasswordEncoderPort passwordEncoderPort,
                         @Autowired(required = false) ApplicationRepositoryPort applicationRepositoryPort) {
        this.jobRepositoryPort = jobRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.applicationRepositoryPort = applicationRepositoryPort;
    }

    public JobDataSeeder(JobRepositoryPort jobRepositoryPort) {
        this(jobRepositoryPort, null, null, null);
    }

    @Override
    public void run(String... args) {
        User varadi1 = null;
        User varadi2 = null;
        List<User> sampleApplicants = new ArrayList<>();

        if (userRepositoryPort != null && passwordEncoderPort != null) {
            // 1. Két dedikált felhasználó (Start123 jelszóval)
            varadi1 = upsertUser(
                    "Váradi 1 (Jelentkező)",
                    "varadi1@gmail.com",
                    "Start123",
                    UserRole.APPLICANT,
                    null,
                    List.of("React ismeret", "TypeScript", "Tailwind CSS", "Java")
            );
            varadi2 = upsertUser(
                    "Váradi 2 (Kezelő)",
                    "varadi2@gmail.com",
                    "Start123",
                    UserRole.COMPANY_MANAGER,
                    "TechCorp Hungary",
                    List.of()
            );

            // 2. További jelentkezők a példák és demonstráció kedvéért (Start123 jelszóval)
            sampleApplicants.add(upsertUser(
                    "Kovács Anna",
                    "kovacs.anna@gmail.com",
                    "Start123",
                    UserRole.APPLICANT,
                    null,
                    List.of("React ismeret", "TypeScript", "Tailwind CSS", "HTML/CSS")
            ));
            sampleApplicants.add(upsertUser(
                    "Nagy Péter",
                    "nagy.peter@gmail.com",
                    "Start123",
                    UserRole.APPLICANT,
                    null,
                    List.of("Java", "Spring Boot", "PostgreSQL", "Docker")
            ));
            sampleApplicants.add(upsertUser(
                    "Tóth Eszter",
                    "toth.eszter@gmail.com",
                    "Start123",
                    UserRole.APPLICANT,
                    null,
                    List.of("Figma", "Design System", "UI/UX", "Prototyping")
            ));
            sampleApplicants.add(upsertUser(
                    "Szabó Dávid",
                    "szabo.david@gmail.com",
                    "Start123",
                    UserRole.APPLICANT,
                    null,
                    List.of("React Native", "TypeScript", "Mobile Development")
            ));
            sampleApplicants.add(upsertUser(
                    "Kiss Gábor",
                    "kiss.gabor@gmail.com",
                    "Start123",
                    UserRole.APPLICANT,
                    null,
                    List.of("Python", "Machine Learning", "PostgreSQL", "Linux")
            ));

            // 3. Visszafelé kompatibilis korábbi tesztfelhasználók
            upsertUser("TechCorp HR", "hr@techcorp.hu", "password123", UserRole.COMPANY_MANAGER, "TechCorp Hungary", List.of());
            upsertUser("Példa Jelentkező", "jelentkezo@pelda.hu", "password123", UserRole.APPLICANT, null, List.of("React ismeret", "JavaScript", "HTML/CSS"));
        }

        Long managerId = varadi2 != null ? varadi2.getId() : null;

        // 4. Álláshirdetések betöltése (8 ACTIVE, 2 CLOSED, TechCorp + más cégek)
        List<Job> allJobs = seedJobs(managerId);

        // 5. Jelentkezések betöltése (különböző státuszok, önéletrajz fájlok, jelentkezők)
        if (applicationRepositoryPort != null && varadi1 != null) {
            seedApplications(allJobs, varadi1, sampleApplicants);
        }
    }

    private User upsertUser(String name, String email, String rawPassword, UserRole role, String companyName) {
        return upsertUser(name, email, rawPassword, role, companyName, List.of());
    }

    private User upsertUser(String name, String email, String rawPassword, UserRole role, String companyName, List<String> initialSkills) {
        String encoded = passwordEncoderPort.encode(rawPassword);
        Optional<User> existing = userRepositoryPort.findByEmail(email);
        if (existing.isPresent()) {
            User u = existing.get();
            boolean needsUpdate = !passwordEncoderPort.matches(rawPassword, u.getPasswordHash())
                    || u.getRole() != role
                    || (companyName != null && !companyName.equals(u.getCompanyName()));
            boolean skillsNeedUpdate = (u.getSkills() == null || u.getSkills().isEmpty()) && (initialSkills != null && !initialSkills.isEmpty());
            if (needsUpdate || skillsNeedUpdate) {
                List<String> skillsToSet = (u.getSkills() != null && !u.getSkills().isEmpty()) ? u.getSkills() : initialSkills;
                User updated = new User(u.getId(), name, u.getEmail(), encoded, role, companyName, u.getCreatedAt(), skillsToSet);
                log.info("Felhasználó frissítése: {} ({}) skillekkel: {}", email, role, skillsToSet);
                return userRepositoryPort.save(updated);
            }
            return u;
        } else {
            log.info("Új felhasználó létrehozása: {} ({}) skillekkel: {}", email, role, initialSkills);
            User newUser = User.createNew(name, email, encoded, role, companyName, initialSkills);
            return userRepositoryPort.save(newUser);
        }
    }

    private List<Job> seedJobs(Long varadiManagerId) {
        if (jobRepositoryPort.count() == 0) {
            log.info("Adatbázis üres - 10 db komplex JobBoard álláshirdetés betöltése...");

            List<Job> initialJobs = List.of(
                    // 1. TechCorp Hungary - Senior Frontend (ACTIVE) - 0 kezdeti jelentkezéssel a teszt workflowhoz
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
                            LocalDate.of(2026, 9, 20),
                            JobStatus.ACTIVE,
                            varadiManagerId,
                            resolveDefaultSkillsForJobTitle("Senior Frontend Fejlesztő")
                    ),
                    // 2. NextWave Digital - Fullstack Node.js (ACTIVE)
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
                            LocalDate.of(2026, 9, 19),
                            JobStatus.ACTIVE,
                            null,
                            resolveDefaultSkillsForJobTitle("Fullstack Fejlesztő (Node.js + React)")
                    ),
                    // 3. InnoStudio - UI/UX Designer (ACTIVE)
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
                            LocalDate.of(2026, 9, 18),
                            JobStatus.ACTIVE,
                            null,
                            resolveDefaultSkillsForJobTitle("UI/UX & Product Designer")
                    ),
                    // 4. CloudScale Solutions - DevOps (ACTIVE)
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
                            LocalDate.of(2026, 9, 17),
                            JobStatus.ACTIVE,
                            null,
                            resolveDefaultSkillsForJobTitle("DevOps & Cloud Mérnök")
                    ),
                    // 5. TechCorp Hungary - Senior Backend Java (ACTIVE)
                    new Job(
                            null,
                            "Senior Backend Fejlesztő (Java / Spring Boot)",
                            "TechCorp Hungary",
                            JobType.FULL_TIME,
                            "Budapest",
                            "Java",
                            "1 400 000 - 1 900 000 HUF",
                            "Nagyvállalati REST microservice architektúra tervezése és implementálása Spring Boot és Hexagonal Architecture alapokon.",
                            List.of(
                                    "5+ év Java / Spring Boot fejlesztési tapasztalat",
                                    "Domain-Driven Design és tiszta architektúra elvek",
                                    "PostgreSQL, JPA/Hibernate optimalizáció",
                                    "JUnit 5 és Mockito tesztelési kultúra"
                            ),
                            LocalDate.of(2026, 9, 16),
                            JobStatus.ACTIVE,
                            varadiManagerId,
                            resolveDefaultSkillsForJobTitle("Senior Backend Fejlesztő (Java / Spring Boot)")
                    ),
                    // 6. TechCorp Hungary - Mobilfejlesztő (ACTIVE)
                    new Job(
                            null,
                            "Mobilalkalmazás Fejlesztő (React Native)",
                            "TechCorp Hungary",
                            JobType.CONTRACT,
                            "Debrecen / Hibrid",
                            "Mobile",
                            "1 100 000 - 1 500 000 HUF",
                            "Cross-platform mobilappok fejlesztése Android és iOS platformokra modern React Native ökoszisztémában.",
                            List.of(
                                    "2+ év React Native és TypeScript tapasztalat",
                                    "Native modulok integrálása (iOS / Android)",
                                    "Offline-first állapotkezelés és REST API integráció"
                            ),
                            LocalDate.of(2026, 9, 15),
                            JobStatus.ACTIVE,
                            varadiManagerId,
                            resolveDefaultSkillsForJobTitle("Mobilalkalmazás Fejlesztő (React Native)")
                    ),
                    // 7. TechCorp Hungary - Adatbázis Adminisztrátor (ACTIVE)
                    new Job(
                            null,
                            "Adatbázis Adminisztrátor & SQL Szakértő",
                            "TechCorp Hungary",
                            JobType.PART_TIME,
                            "Budapest",
                            "PostgreSQL",
                            "700 000 - 950 000 HUF",
                            "PostgreSQL adatbázisok performancia hangolása, replikáció, biztonsági mentési stratégiák kidolgozása.",
                            List.of(
                                    "PostgreSQL adminisztrációs tapasztalat",
                                    "Komplex query optimalizálás és indexelés",
                                    "Magas rendelkezésre állás (HA) kialakítása"
                            ),
                            LocalDate.of(2026, 9, 14),
                            JobStatus.ACTIVE,
                            varadiManagerId,
                            resolveDefaultSkillsForJobTitle("Adatbázis Adminisztrátor & SQL Szakértő")
                    ),
                    // 8. TechCorp Hungary - AI & Machine Learning Mérnök (ACTIVE)
                    new Job(
                            null,
                            "AI & Machine Learning Mérnök",
                            "TechCorp Hungary",
                            JobType.REMOTE,
                            "Remote",
                            "Python",
                            "1 500 000 - 2 100 000 HUF",
                            "LLM alapú asszisztensek, Retrieval-Augmented Generation (RAG) pipeline-ok és gépi tanulási modellek integrálása üzleti folyamatokba.",
                            List.of(
                                    "Python, PyTorch / TensorFlow gyakorlat",
                                    "LLM és RAG architektúrák mélyreható ismerete",
                                    "Docker és modell deployment tapasztalat"
                            ),
                            LocalDate.of(2026, 9, 13),
                            JobStatus.ACTIVE,
                            varadiManagerId,
                            resolveDefaultSkillsForJobTitle("AI & Machine Learning Mérnök")
                    ),
                    // 9. TechCorp Hungary - Junior Frontend Gyakornok (CLOSED)
                    new Job(
                            null,
                            "Junior Frontend Gyakornok",
                            "TechCorp Hungary",
                            JobType.PART_TIME,
                            "Budapest",
                            "JavaScript",
                            "400 000 - 550 000 HUF",
                            "HTML/CSS és modern JavaScript alapú felületek fejlesztésének elsajátítása mentorált gyakornoki programban.",
                            List.of(
                                    "Aktív nappali tagozatos hallgatói jogviszony",
                                    "Alapvető JavaScript, HTML, CSS tudás",
                                    "Nyitottság modern keretrendszerek megismerésére"
                            ),
                            LocalDate.of(2026, 8, 25),
                            JobStatus.CLOSED,
                            varadiManagerId,
                            resolveDefaultSkillsForJobTitle("Junior Frontend Gyakornok")
                    ),
                    // 10. TechCorp Hungary - Kiberbiztonsági Elemző (CLOSED)
                    new Job(
                            null,
                            "Kiberbiztonsági Elemző",
                            "TechCorp Hungary",
                            JobType.FULL_TIME,
                            "Szeged / Hibrid",
                            "Security",
                            "1 250 000 - 1 700 000 HUF",
                            "Infrastruktúra sérülékenység-vizsgálat, biztonsági auditok végzése és incidenskezelés.",
                            List.of(
                                    "Hálózati és alkalmazásbiztonsági ismeretek",
                                    "Penetration testing és SIEM eszközök tapasztalata",
                                    "Releváns certifikáció (OSCP, CEH, CISSP) előny"
                            ),
                            LocalDate.of(2026, 8, 10),
                            JobStatus.CLOSED,
                            varadiManagerId,
                            resolveDefaultSkillsForJobTitle("Kiberbiztonsági Elemző")
                    )
            );

            List<Job> savedJobs = new ArrayList<>();
            for (Job j : initialJobs) {
                savedJobs.add(jobRepositoryPort.save(j));
            }
            log.info("Kezdeti adatok sikeresen betöltve. Összes hirdetés: {}", savedJobs.size());
            return savedJobs;
        }

        // Ha már léteznek hirdetések az adatbázisban, visszatöltjük a kezelőazonosítót és pótoljuk az üres skilleket
        List<Job> allJobs = jobRepositoryPort.findAll();
        for (Job job : allJobs) {
            boolean needsSave = false;
            Job target = job;
            if (varadiManagerId != null && "TechCorp Hungary".equalsIgnoreCase(target.getCompany()) && target.getCreatedByUserId() == null) {
                target = target.withCreatedByUserId(varadiManagerId);
                needsSave = true;
            }
            if (target.getSkills() == null || target.getSkills().isEmpty()) {
                List<String> defaultSkills = resolveDefaultSkillsForJobTitle(target.getTitle());
                if (!defaultSkills.isEmpty()) {
                    target = target.withSkillsChangedBy(defaultSkills, target.getCreatedByUserId());
                    needsSave = true;
                }
            }
            if (needsSave) {
                jobRepositoryPort.save(target);
            }
        }
        return allJobs;
    }

    private List<String> resolveDefaultSkillsForJobTitle(String title) {
        if (title == null) {
            return List.of();
        }
        String t = title.toLowerCase();
        if (t.contains("senior frontend")) {
            return List.of("React ismeret", "TypeScript", "Tailwind CSS", "HTML/CSS", "JavaScript");
        } else if (t.contains("fullstack")) {
            return List.of("Node.js", "React", "TypeScript", "PostgreSQL", "REST API");
        } else if (t.contains("ui/ux")) {
            return List.of("Figma", "Design System", "UI/UX", "Prototyping", "Wireframing");
        } else if (t.contains("devops")) {
            return List.of("Docker", "Kubernetes", "AWS", "CI/CD", "Linux");
        } else if (t.contains("backend") || t.contains("spring boot")) {
            return List.of("Java", "Spring Boot", "PostgreSQL", "Docker", "REST API", "Hibernate");
        } else if (t.contains("mobilalkalmazás") || t.contains("react native")) {
            return List.of("React Native", "TypeScript", "Mobile Development", "Android", "iOS");
        } else if (t.contains("adatbázis") || t.contains("sql")) {
            return List.of("PostgreSQL", "SQL", "Database Optimization", "Linux", "Docker");
        } else if (t.contains("machine learning") || t.contains("ai")) {
            return List.of("Python", "Machine Learning", "PyTorch", "Docker", "AI");
        } else if (t.contains("junior frontend") || t.contains("gyakornok")) {
            return List.of("JavaScript", "HTML/CSS", "React ismeret", "Git");
        } else if (t.contains("kiberbiztonsági") || t.contains("security")) {
            return List.of("Security", "Penetration Testing", "Linux", "Networking");
        }
        return List.of();
    }


    private void seedApplications(List<Job> allJobs, User varadi1, List<User> applicants) {
        if (applicationRepositoryPort.count() > 0) {
            return;
        }

        Job job3 = findJobByTitle(allJobs, "UI/UX & Product Designer");
        Job job4 = findJobByTitle(allJobs, "DevOps & Cloud Mérnök");
        Job job5 = findJobByTitle(allJobs, "Senior Backend Fejlesztő (Java / Spring Boot)");
        Job job6 = findJobByTitle(allJobs, "Mobilalkalmazás Fejlesztő (React Native)");
        Job job7 = findJobByTitle(allJobs, "Adatbázis Adminisztrátor & SQL Szakértő");
        Job job8 = findJobByTitle(allJobs, "AI & Machine Learning Mérnök");
        Job job9 = findJobByTitle(allJobs, "Junior Frontend Gyakornok");

        User kovacsAnna = findApplicantByEmail(applicants, "kovacs.anna@gmail.com");
        User nagyPeter = findApplicantByEmail(applicants, "nagy.peter@gmail.com");
        User tothEszter = findApplicantByEmail(applicants, "toth.eszter@gmail.com");
        User szaboDavid = findApplicantByEmail(applicants, "szabo.david@gmail.com");
        User kissGabor = findApplicantByEmail(applicants, "kiss.gabor@gmail.com");

        List<JobApplication> apps = new ArrayList<>();
        Instant now = Instant.now();

        // 1. Váradi 1 jelentkezései (mindhárom állapot lefedése a jelentkezői nézetben: ACCEPTED, REJECTED, PENDING)
        if (job5 != null) {
            apps.add(new JobApplication(
                    null, job5.getId(), varadi1.getName(), varadi1.getEmail(),
                    "varadi1_java_backend_cv.pdf", now.minus(Duration.ofDays(4)),
                    varadi1.getId(), ApplicationStatus.ACCEPTED,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }

        if (job4 != null) {
            apps.add(new JobApplication(
                    null, job4.getId(), varadi1.getName(), varadi1.getEmail(),
                    "varadi1_devops_cv.docx", now.minus(Duration.ofDays(6)),
                    varadi1.getId(), ApplicationStatus.REJECTED,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", SAMPLE_DOCX_BYTES
            ));
        }

        if (job8 != null) {
            apps.add(new JobApplication(
                    null, job8.getId(), varadi1.getName(), varadi1.getEmail(),
                    "varadi1_ml_engineer_resume.pdf", now.minus(Duration.ofDays(1)),
                    varadi1.getId(), ApplicationStatus.PENDING,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }

        // 2. További jelentkezők a kezelői nézet és elbírálás demonstrálásához
        // Job 5 (Senior Backend) - Kovács Anna (PENDING) & Nagy Péter (REJECTED)
        if (job5 != null && kovacsAnna != null) {
            apps.add(new JobApplication(
                    null, job5.getId(), kovacsAnna.getName(), kovacsAnna.getEmail(),
                    "kovacs_anna_backend_cv.pdf", now.minus(Duration.ofDays(3)),
                    kovacsAnna.getId(), ApplicationStatus.PENDING,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }
        if (job5 != null && nagyPeter != null) {
            apps.add(new JobApplication(
                    null, job5.getId(), nagyPeter.getName(), nagyPeter.getEmail(),
                    "nagy_peter_backend_cv.docx", now.minus(Duration.ofDays(5)),
                    nagyPeter.getId(), ApplicationStatus.REJECTED,
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", SAMPLE_DOCX_BYTES
            ));
        }

        // Job 6 (Mobilalkalmazás Fejlesztő) - Tóth Eszter (PENDING) & Szabó Dávid (ACCEPTED)
        if (job6 != null && tothEszter != null) {
            apps.add(new JobApplication(
                    null, job6.getId(), tothEszter.getName(), tothEszter.getEmail(),
                    "toth_eszter_mobile_cv.pdf", now.minus(Duration.ofDays(2)),
                    tothEszter.getId(), ApplicationStatus.PENDING,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }
        if (job6 != null && szaboDavid != null) {
            apps.add(new JobApplication(
                    null, job6.getId(), szaboDavid.getName(), szaboDavid.getEmail(),
                    "szabo_david_mobile_cv.pdf", now.minus(Duration.ofDays(4)),
                    szaboDavid.getId(), ApplicationStatus.ACCEPTED,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }

        // Job 7 (Adatbázis Adminisztrátor) - Kiss Gábor (PENDING)
        if (job7 != null && kissGabor != null) {
            apps.add(new JobApplication(
                    null, job7.getId(), kissGabor.getName(), kissGabor.getEmail(),
                    "kiss_gabor_dba_cv.pdf", now.minus(Duration.ofHours(14)),
                    kissGabor.getId(), ApplicationStatus.PENDING,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }

        // Job 8 (AI & Machine Learning Mérnök) - Kiss Gábor (PENDING)
        if (job8 != null && kissGabor != null) {
            apps.add(new JobApplication(
                    null, job8.getId(), kissGabor.getName(), kissGabor.getEmail(),
                    "kiss_gabor_ai_resume.pdf", now.minus(Duration.ofHours(20)),
                    kissGabor.getId(), ApplicationStatus.PENDING,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }

        // Job 9 (Junior Frontend Gyakornok - CLOSED) - Kovács Anna (ACCEPTED)
        if (job9 != null && kovacsAnna != null) {
            apps.add(new JobApplication(
                    null, job9.getId(), kovacsAnna.getName(), kovacsAnna.getEmail(),
                    "kovacs_anna_intern_cv.pdf", now.minus(Duration.ofDays(12)),
                    kovacsAnna.getId(), ApplicationStatus.ACCEPTED,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }

        // Job 3 (UI/UX & Product Designer - InnoStudio) - Tóth Eszter (PENDING)
        if (job3 != null && tothEszter != null) {
            apps.add(new JobApplication(
                    null, job3.getId(), tothEszter.getName(), tothEszter.getEmail(),
                    "toth_eszter_design_portfolio.pdf", now.minus(Duration.ofDays(2)),
                    tothEszter.getId(), ApplicationStatus.PENDING,
                    "application/pdf", SAMPLE_PDF_BYTES
            ));
        }

        for (JobApplication app : apps) {
            applicationRepositoryPort.save(app);
        }
        log.info("Kezdeti jelentkezések sikeresen betöltve. Összes leadott minta jelentkezés: {}", apps.size());
    }

    private Job findJobByTitle(List<Job> jobs, String title) {
        return jobs.stream()
                .filter(j -> j.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElse(null);
    }

    private User findApplicantByEmail(List<User> users, String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
}
