# Project Status & Changelog

## Current Status
- **Stack**: Java 21, Spring Boot 3.3.4, Spring Data JPA, PostgreSQL 16, H2 Database, Springdoc OpenAPI.
- **Architecture**: Hexagonal Architecture (Ports and Adapters) & Domain-Driven Design (DDD).
- **Health**: Builds cleanly with `./mvnw clean test` (28/28 tests passing).
- **Database**: PostgreSQL database `jobboard` running on port 5432; in-memory H2 test profile active for tests.

## Implemented Features & Milestones
- [x] Initialized Spring Boot 3 Maven project with Maven Wrapper (`mvnw`).
- [x] Configured PostgreSQL database and H2 test profiles.
- [x] Designed and implemented Hexagonal Architecture (Ports and Adapters):
  - Domain Layer (`jobs.domain`, `applications.domain`) with pure business logic and invariants.
  - Application Layer with Inbound Ports (`GetJobsUseCase`, `SubmitApplicationUseCase`, etc.) and Outbound Ports (`JobRepositoryPort`, `ApplicationRepositoryPort`).
  - Web Adapters (`JobRestController`, `ApplicationRestController`) matching frontend schemas.
  - Persistence Adapters (`JobJpaAdapter`, `ApplicationJpaAdapter`) using Spring Data JPA.
- [x] Implemented API endpoints matching the frontend:
  - `GET /api/jobs` (with optional search keyword and badge filtering)
  - `GET /api/jobs/{id}`
  - `POST /api/jobs`
  - `POST /api/applications`
  - `POST /api/jobs/{jobId}/applications`
  - `GET /api/jobs/{jobId}/applications`
  - `GET /api/applications`
- [x] Implemented `JobDataSeeder` preloading the 4 frontend preset jobs into PostgreSQL.
- [x] Configured CORS for frontend development (`http://localhost:5173`).
- [x] Configured OpenAPI / Swagger documentation at `/swagger-ui.html`.
- [x] Written comprehensive test suite (28 automated tests):
  - Domain entity unit tests (`JobTest`, `JobApplicationTest`).
  - Application service unit tests (`JobServiceTest`, `ApplicationServiceTest`).
  - WebMvc controller slice tests (`JobRestControllerTest`, `ApplicationRestControllerTest`).
  - JPA persistence adapter tests (`JobJpaAdapterTest`).
  - End-to-end integration test (`JobBoardIntegrationTest`).
- [x] Created AI development guidelines (`AGENTS.md`) and Antigravity skills.

## Next Steps / Roadmap
- [ ] Implement file storage integration for CV upload (local disk / S3).
- [ ] Connect frontend `jobService.ts` to call this backend directly via `fetch`/`axios`.
- [ ] Add JWT authentication & authorization for employer role (posting jobs).
- [ ] Add pagination for job listings (`Pageable`).
