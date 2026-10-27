# Architecture & System Design

## Overview
The **JobBoardServer** backend service is engineered using **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** bounded contexts.

## Technical Stack
- **Framework**: Spring Boot 3.3.4 (Java 21 / 27)
- **Persistence**: Spring Data JPA + Hibernate
- **Database**: PostgreSQL 16 (Development & Production), H2 in-memory (Automated Testing & Fallback)
- **API Documentation**: Springdoc OpenAPI / Swagger UI 2.6.0
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit Jupiter, AssertJ, Mockito, Spring Boot Test Slice (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`)

---

## Architectural Pattern: Hexagonal Architecture (Ports & Adapters)

```
        +-------------------------------------------------------------+
        |                     HEXAGON BOUNDARY                        |
        |                                                             |
        |    INBOUND ADAPTER               HEXAGONAL CORE             |
        |   (Driving / Primary)                                       |
        |  +--------------------+        +-----------------------+    |
[HTTP] --->| JobRestController  | ------>|    GetJobsUseCase     |    |
           | ApplicationRestCtr |        | UpdateJobStatusUC     |    |
           | AuthRestController |        | SubmitApplicationUC   |    |
           +--------------------+        | UpdateApplicationStUC |    |
                                         | DownloadResumeUC      |    |
                                         | AuthenticateTokenUC   |    |
                                         +-----------+-----------+    |
                                                     |                |
                                                     v                |
                                         +-----------------------+    |
                                         |    Domain Models      |    |
                                         |  (Job, JobApplication |    |
                                         |         User)         |    |
                                         +-----------+-----------+    |
                                                     |                |
        |    OUTBOUND ADAPTER                        v                |
        |   (Driven / Secondary)         +-----------------------+    |
        |  +--------------------+        |  JobRepositoryPort    |    |
        |  |   JobJpaAdapter    |<-------| ApplicationRepoPort   |    |
        |  | UserJpaAdapter     |        | UserRepositoryPort    |    |
        |  | JwtTokenAdapter    |        | TokenServicePort      |    |
        |  | PasswordEncoderAdp |        | PasswordEncoderPort   |    |
        |  +---------+----------+        +-----------------------+    |
        +------------|------------------------------------------------+
                     v
             [PostgreSQL / JPA]
```

### 1. Hexagonal Core (Domain Layer)
Located under `com.jobboard.<domain>.domain`:
- **Purity**: Zero framework imports. No Spring, no Jakarta Persistence, no Jackson annotations in business methods.
- **Invariants**: Entities enforce business rules during construction and operation (`Job`, `JobApplication`, `JobType`, `JobStatus`, `ApplicationStatus`, `User`, `UserRole`).
- **Domain Exceptions**: Explicit semantic exceptions (`JobNotFoundException`, `InvalidJobDataException`, `InvalidApplicationException`, `InvalidUserDataException`, `InvalidCredentialsException`, `UnauthorizedAccessException`, `UserAlreadyExistsException`).

### 2. Application Layer (Ports & Use Cases)
Located under `com.jobboard.<domain>.application`:
- **Inbound Ports (`port.in`)**: Define use case contracts for driving adapters:
  - `GetJobsUseCase`, `GetJobDetailUseCase`, `CreateJobUseCase`, `UpdateJobStatusUseCase`.
  - `SubmitApplicationUseCase`, `GetApplicationsUseCase`, `UpdateApplicationStatusUseCase`, `DownloadResumeUseCase`.
  - `RegisterUserUseCase`, `LoginUserUseCase`, `AuthenticateTokenUseCase`, `UpdateUserProfileUseCase`.
- **Outbound Ports (`port.out`)**: Define SPI contracts for persistence, security, and external services:
  - `JobRepositoryPort`, `ApplicationRepositoryPort`, `UserRepositoryPort`, `TokenServicePort`, `PasswordEncoderPort`.
- **Services (`service`)**: Orchestrate use cases, verify domain business invariants across boundaries, and coordinate with outbound ports.

### 3. Adapters Layer (Infrastructure)
Located under `com.jobboard.<domain>.infrastructure`:
- **Web Adapters (`infrastructure.web`)**: REST controllers, DTOs matching frontend specifications, Bean Validation, and Web Mappers.
- **Persistence Adapters (`infrastructure.persistence`)**: JPA entities, Spring Data repositories, mapping logic (`toDomain`, `toJpaEntity`), and repository adapters implementing outbound ports.
- **Security Adapters (`infrastructure.security`)**: Pure Java HS256 HMAC JWT implementation and SHA-256 salted password hashing.

---

## Bounded Contexts

### 1. Jobs Bounded Context (`com.jobboard.jobs`)
Responsible for managing job postings, job requirements, salary ranges, employment types, status lifecycle (`ACTIVE` / `CLOSED`), and search filtering.
- **Endpoints**:
  - `GET /api/jobs`: Public list of active jobs (`JobStatus.ACTIVE`) with optional `search` and `badge` filters. Closed postings are hidden.
  - `GET /api/jobs/my`: Retrieves all postings (`ACTIVE` and `CLOSED`) created by the authenticated company manager (`createdByUserId`).
  - `GET /api/jobs/{id}`: Retrieve details for a single job posting.
  - `POST /api/jobs`: Post a new job opportunity (`ACTIVE`), recording creator identity if authenticated.
  - `PATCH /api/jobs/{id}/status`: Close (`ACTIVE` -> `CLOSED`) or reopen (`CLOSED` -> `ACTIVE`) a posting. Enforces that only the creator can update status.

### 2. Applications Bounded Context (`com.jobboard.applications`)
Responsible for receiving, validating, recording, and evaluating job applications and attached candidate CVs.
- **Endpoints**:
  - `POST /api/applications` (`application/json` & `multipart/form-data`): Submit candidate application with mandatory `.pdf` or `.docx` validation. Rejects applications for closed jobs.
  - `POST /api/jobs/{jobId}/applications`: Alternative nested path for submitting applications.
  - `GET /api/applications/my`: Candidate review of their applications with status indicators (`ACCEPTED`, `REJECTED`, `PENDING`) and localized progress messages.
  - `GET /api/jobs/{jobId}/applications`: Retrieve applications for a specific job posting.
  - `GET /api/applications`: Retrieve all submitted applications.
  - `PATCH /api/applications/{id}/status`: Employer decisions (`ACCEPTED` / `REJECTED`) restricted to the creator of the associated job posting.
  - `GET /api/applications/{id}/resume`: Download resume binary with `Content-Disposition: attachment; filename="..."` and exact MIME type.

### 3. Auth Bounded Context (`com.jobboard.auth`)
Responsible for user registration, credentials hashing (SHA-256 + salt), HS256 JWT token generation and validation, and role-based principal extraction.
- **Roles**:
  - `APPLICANT`: Job seeker role.
  - `COMPANY_MANAGER`: Employer role managing jobs and evaluating applications.
- **Inbound Ports**:
  - `RegisterUserUseCase`: Register candidate or company manager.
  - `LoginUserUseCase`: Validate credentials and issue signed token.
  - `AuthenticateTokenUseCase`: Extract and verify token from `Authorization` header (`authenticate` and `authenticateOptional`).
- **Outbound Ports**:
  - `UserRepositoryPort`: Persistence SPI for user data.
  - `TokenServicePort`: Token generation & signature verification.
  - `PasswordEncoderPort`: Password hashing & verification.
- **Endpoints**:
  - `POST /api/auth/register`: Create user with `{ name, email, password, role, companyName }`.
  - `POST /api/auth/login`: Authenticate with `{ email, password }` returning `{ token, user }`.
  - `GET /api/auth/me`: Resolve current user info via `Authorization: Bearer <token>`.

### 4. Shared Context (`com.jobboard.shared`)
Cross-cutting infrastructure concerns:
- `WebCorsConfig`: CORS configuration enabling browser communication with frontend on `http://localhost:5173`, exposing `Authorization` and `Content-Disposition` and supporting `PATCH`.
- `OpenApiConfig`: Swagger OpenAPI specification available at `/swagger-ui.html`.
- `GlobalExceptionHandler`: Unified error formatting returning structured error responses and validation details (400, 401, 403, 404, 409, 500).
- `JobDataSeeder`: Seeds preset frontend listings and default test users (`hr@techcorp.hu` / `password123` and `jelentkezo@pelda.hu` / `password123`) upon initial startup.

---

## Database Schema (PostgreSQL)

### `users` Table
| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, GENERATED ALWAYS AS IDENTITY |
| `name` | `VARCHAR(255)` | NOT NULL |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE |
| `password_hash` | `VARCHAR(255)` | NOT NULL |
| `role` | `VARCHAR(50)` | NOT NULL (`APPLICANT`, `COMPANY_MANAGER`) |
| `company_name` | `VARCHAR(255)` | NULLABLE |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL |

### `jobs` Table
| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, GENERATED ALWAYS AS IDENTITY |
| `title` | `VARCHAR(255)` | NOT NULL |
| `company` | `VARCHAR(255)` | NOT NULL |
| `type` | `VARCHAR(255)` | NOT NULL (Full-time, Part-time, Contract, Remote) |
| `location` | `VARCHAR(255)` | NOT NULL |
| `badge` | `VARCHAR(255)` | NOT NULL |
| `salary` | `VARCHAR(255)` | NOT NULL |
| `description` | `TEXT` | NOT NULL |
| `posted_at` | `DATE` | NOT NULL |
| `status` | `VARCHAR(50)` | NOT NULL (`ACTIVE`, `CLOSED`) |
| `created_by_user_id` | `BIGINT` | NULLABLE |

### `job_requirements` Table
| Column | Type | Constraints |
|---|---|---|
| `job_id` | `BIGINT` | FOREIGN KEY REFERENCES `jobs(id)` ON DELETE CASCADE |
| `requirement` | `VARCHAR(255)` | NOT NULL |

### `job_applications` Table
| Column | Type | Constraints |
|---|---|---|
| `id` | `BIGINT` | PRIMARY KEY, GENERATED ALWAYS AS IDENTITY |
| `job_id` | `BIGINT` | NOT NULL |
| `applicant_name` | `VARCHAR(255)` | NOT NULL |
| `applicant_email` | `VARCHAR(255)` | NOT NULL |
| `resume_file_name`| `VARCHAR(255)` | NULLABLE |
| `submitted_at` | `TIMESTAMP WITH TIME ZONE` | NOT NULL |
| `applicant_user_id` | `BIGINT` | NULLABLE |
| `status` | `VARCHAR(50)` | NOT NULL (`PENDING`, `ACCEPTED`, `REJECTED`) |
| `resume_content_type` | `VARCHAR(255)` | NULLABLE |
| `resume_data` | `BYTEA` | NULLABLE (raw resume file binary) |
