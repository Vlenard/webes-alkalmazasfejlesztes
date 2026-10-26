# AGENTS.md - Development & Architecture Guidelines

## Project Overview
This repository contains the **JobBoardServer** backend application, built with **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA**, **PostgreSQL**, **Hibernate**, and **OpenAPI (Swagger)**. It serves as the backend REST API for the JobBoard web application frontend.

## Architecture Guidelines (Hexagonal Architecture & Domain-Driven Design)
The project strictly enforces **Hexagonal Architecture (Ports and Adapters)** organized by **Domain-Driven Design (DDD)** bounded contexts.

### Directory Structure & Packaging by Domain
```
com.jobboard/
├── JobBoardServerApplication.java           # Spring Boot bootstrap
│
├── jobs/                                    # Jobs Bounded Context
│   ├── domain/                              # Hexagonal Core (Pure Java, ZERO framework imports)
│   │   ├── model/                           # Domain Entities & Value Objects (Job, JobType)
│   │   └── exception/                       # Domain Exceptions (JobNotFoundException, InvalidJobDataException)
│   ├── application/                         # Use Cases & Port Interfaces
│   │   ├── port/
│   │   │   ├── in/                          # Inbound (Driving) Ports (GetJobsUseCase, CreateJobUseCase, etc.)
│   │   │   └── out/                         # Outbound (Driven) Ports (JobRepositoryPort)
│   │   └── service/                         # Application Services (JobService)
│   └── infrastructure/                      # Hexagonal Adapters
│       ├── web/                             # Inbound Driving Adapter (REST Controllers, DTOs, Web Mappers)
│       └── persistence/                     # Outbound Driven Adapter (JPA Entities, Spring Data Repos, Adapters)
│
├── applications/                            # Applications Bounded Context
│   ├── domain/                              # Core Domain (JobApplication, Applicant info, validation rules)
│   ├── application/                         # Inbound/Outbound Ports & Application Services
│   └── infrastructure/                      # REST controller & JPA persistence adapters
│
└── shared/                                  # Cross-Cutting Infrastructure
    ├── infrastructure/
    │   ├── config/                          # WebCorsConfig, OpenApiConfig
    │   ├── error/                           # GlobalExceptionHandler, ErrorResponseDto
    │   └── seeder/                          # JobDataSeeder (loads preset frontend jobs)
```

## Rules for Agents & Developers
1. **Domain Isolation**:
   - The `domain/` package MUST NOT import any Spring, Jakarta, or database persistence libraries.
   - Domain entities (e.g., `Job`, `JobApplication`) encapsulate business rules, validation invariants, and immutability.
2. **Ports and Adapters (Hexagonal)**:
   - Application services only implement inbound ports (`application.port.in`) and call outbound ports (`application.port.out`).
   - Web controllers never directly access repositories or entities; they interact via Inbound Port Use Cases and use Web DTOs.
   - Persistence adapters implement Outbound Ports and map domain models to JPA entities and vice versa.
3. **Frontend API Compatibility**:
   - All response DTOs must align with the TypeScript definitions in `w5/JobBoard/src/modules/jobs/types/job.ts`:
     - `Job`: `id`, `title`, `company`, `type`, `location`, `badge`, `salary`, `description`, `requirements`, `postedAt`
     - `JobApplication`: `jobId`, `applicantName`, `applicantEmail`, `resumeFileName`, `submittedAt`
   - Application submission returns `{ success: true, message: "A jelentkezést sikeresen rögzítettük!" }`.
4. **Testing is Mandatory**:
   - Every domain entity must have pure unit tests verifying invariant rules.
   - Application services must have unit tests with Mockito mocking outbound ports.
   - Controllers must have `@WebMvcTest` slice tests verifying request/response mappings and status codes.
   - Repositories/adapters must have `@DataJpaTest` tests.
   - Integration tests must verify full workflows using `@SpringBootTest`.
   - Always run `./mvnw test` before finalizing tasks to ensure all tests pass.
5. **Documentation**:
   - Internal documentation (`docs/`, `AGENTS.md`) MUST be maintained in English.
   - Keep `docs/status.md` and `docs/architecture.md` updated with new features and architectural changes.
