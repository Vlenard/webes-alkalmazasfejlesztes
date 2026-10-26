# Architecture & System Design

## Overview
The **JobBoardServer** backend service is engineered using **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** bounded contexts.

## Technical Stack
- **Framework**: Spring Boot 3.3.4 (Java 21 / 27)
- **Persistence**: Spring Data JPA + Hibernate
- **Database**: PostgreSQL (Development & Production), H2 in-memory (Automated Testing & Fallback)
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
           | ApplicationRestCtr |        | SubmitApplicationUC   |    |
           +--------------------+        +-----------+-----------+    |
                                                     |                |
                                                     v                |
                                         +-----------------------+    |
                                         |    Domain Model       |    |
                                         |   (Job, Application)  |    |
                                         +-----------+-----------+    |
                                                     |                |
        |    OUTBOUND ADAPTER                        v                |
        |   (Driven / Secondary)         +-----------------------+    |
        |  +--------------------+        |  JobRepositoryPort    |    |
        |  |   JobJpaAdapter    |<-------| ApplicationRepoPort   |    |
        |  +---------+----------+        +-----------------------+    |
        +------------|------------------------------------------------+
                     v
             [PostgreSQL / JPA]
```

### 1. Hexagonal Core (Domain Layer)
Located under `com.jobboard.<domain>.domain`:
- **Purity**: Zero framework imports. No Spring, no Jakarta Persistence, no Jackson annotations in business methods.
- **Invariants**: Entities enforce business rules during construction and operation (`Job`, `JobApplication`, `JobType`).
- **Domain Exceptions**: Explicit semantic exceptions (`JobNotFoundException`, `InvalidJobDataException`, `InvalidApplicationException`).

### 2. Application Layer (Ports & Use Cases)
Located under `com.jobboard.<domain>.application`:
- **Inbound Ports (`port.in`)**: Define use case contracts for driving adapters (e.g., `GetJobsUseCase`, `GetJobDetailUseCase`, `CreateJobUseCase`, `SubmitApplicationUseCase`).
- **Outbound Ports (`port.out`)**: Define SPI contracts for persistence and external systems (e.g., `JobRepositoryPort`, `ApplicationRepositoryPort`).
- **Services (`service`)**: Orchestrate use cases, verify domain business invariants across boundaries, and coordinate with outbound ports.

### 3. Adapters Layer (Infrastructure)
Located under `com.jobboard.<domain>.infrastructure`:
- **Web Adapters (`infrastructure.web`)**: REST controllers, DTOs matching frontend specifications, Bean Validation, and Web Mappers.
- **Persistence Adapters (`infrastructure.persistence`)**: JPA entities, Spring Data repositories, mapping logic (`toDomain`, `toJpaEntity`), and repository adapters implementing outbound ports.

---

## Bounded Contexts

### 1. Jobs Bounded Context (`com.jobboard.jobs`)
Responsible for managing job postings, job requirements, salary ranges, employment types, and search filtering.
- **Endpoints**:
  - `GET /api/jobs`: List all jobs with optional `search` and `badge` query parameters.
  - `GET /api/jobs/{id}`: Retrieve details for a single job posting.
  - `POST /api/jobs`: Post a new job opportunity.

### 2. Applications Bounded Context (`com.jobboard.applications`)
Responsible for receiving, validating, and recording job applications submitted by candidates.
- **Endpoints**:
  - `POST /api/applications`: Submit application matching frontend body `{ jobId, applicantName, applicantEmail, resumeFileName }`.
  - `POST /api/jobs/{jobId}/applications`: Alternative nested path for submitting applications.
  - `GET /api/jobs/{jobId}/applications`: Retrieve applications for a specific job.
  - `GET /api/applications`: Retrieve all submitted applications.

### 3. Shared Context (`com.jobboard.shared`)
Cross-cutting infrastructure concerns:
- `WebCorsConfig`: CORS configuration enabling browser communication with frontend on `http://localhost:5173`.
- `OpenApiConfig`: Swagger OpenAPI specification available at `/swagger-ui.html`.
- `GlobalExceptionHandler`: Unified error formatting returning structured error responses and validation details.
- `JobDataSeeder`: Seeds the 4 preset frontend listings upon initial startup.

---

## Database Schema (PostgreSQL)

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

---

## Frontend Integration Contract

The REST API corresponds 1:1 with the TypeScript types defined in the frontend:
```typescript
// GET /api/jobs, GET /api/jobs/:id -> Job
interface Job {
  id: number
  title: string
  company: string
  type: 'Full-time' | 'Part-time' | 'Contract' | 'Remote'
  location: string
  badge: string
  salary: string
  description: string
  requirements: string[]
  postedAt: string
}

// POST /api/applications -> Response
interface ApplicationResponse {
  success: boolean
  message: string
  applicationId: number
  submittedAt: string
}
```
