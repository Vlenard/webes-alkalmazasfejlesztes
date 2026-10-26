---
name: spring-hexagonal-architecture
description: Guidelines and rules for developing with Hexagonal Architecture (Ports and Adapters) and Domain-Driven Design (DDD) in Spring Boot applications.
---

# Spring Boot Hexagonal Architecture & DDD Guidelines

This skill guides AI agents and developers on maintaining and extending the Hexagonal Architecture and Domain-Driven Design structure in the JobBoardServer application.

## Core Architectural Principles

### 1. Hexagonal Layers & Dependency Inversion
Dependencies always point inwards toward the Domain:
`Adapters (Infrastructure) -> Ports (Application) -> Domain Core`

The Hexagonal Core must never know about external frameworks, web protocols, or database implementations.

```
+-------------------------------------------------------------------+
|                        INFRASTRUCTURE                             |
|  [REST Controllers, JPA Repositories, DTOs, Database Entities]   |
|                                                                   |
|         +-----------------------------------------------+         |
|         |                  APPLICATION                  |         |
|         |   [Use Cases (In-Ports), Out-Ports, Services] |         |
|         |                                               |         |
|         |         +---------------------------+         |         |
|         |         |          DOMAIN           |         |         |
|         |         | [Entities, Value Objects, |         |         |
|         |         |      Business Rules]      |         |         |
|         |         +---------------------------+         |         |
|         +-----------------------------------------------+         |
+-------------------------------------------------------------------+
```

### 2. Domain-Driven Design Packaging
Group packages by Bounded Context first, then by hexagonal layers:

```
com.jobboard.<context>/
├── domain/                      # 1. Pure Domain Model
│   ├── model/                   # Entities, Value Objects, Enums
│   └── exception/               # Semantic Domain Exceptions
├── application/                 # 2. Application Orchestration
│   ├── port/
│   │   ├── in/                  # Driving / Inbound Ports (Use Cases)
│   │   └── out/                 # Driven / Outbound Ports (Repository SPI)
│   └── service/                 # Application Services
└── infrastructure/              # 3. Adapters & Frameworks
    ├── web/                     # Inbound REST Adapter (Controller, DTOs, Mappers)
    └── persistence/             # Outbound JPA Adapter (Entities, Repositories, Mappers)
```

## How to Implement a New Feature Step-by-Step

### Step 1: Model the Domain
- Create domain entity or value object in `<context>.domain.model`.
- Implement validation in constructors or factory methods.
- Ensure zero imports from `org.springframework.*` or `jakarta.persistence.*`.

```java
// Example: Domain Entity
public class Job {
    private final Long id;
    private final String title;
    // ...
    public Job(Long id, String title, ...) {
        if (title == null || title.isBlank()) {
            throw new InvalidJobDataException("Title cannot be blank");
        }
        this.id = id;
        this.title = title.trim();
    }
}
```

### Step 2: Define Application Ports
- **Inbound Port** (Use Case interface):
  ```java
  public interface GetJobDetailUseCase {
      Job getJobById(Long id);
  }
  ```
- **Outbound Port** (SPI interface):
  ```java
  public interface JobRepositoryPort {
      Optional<Job> findById(Long id);
  }
  ```

### Step 3: Implement the Application Service
- Wire dependencies via constructor injection.
- Mark with `@Service` and `@Transactional`.
- Depend strictly on Outbound Ports, never on JPA repositories directly.

### Step 4: Implement the Driving (Inbound) Web Adapter
- REST Controller annotated with `@RestController`, `@RequestMapping`.
- Accept and return DTOs (e.g. Java Records).
- Convert DTOs to/from Domain objects using dedicated Mappers.

### Step 5: Implement the Driven (Outbound) Persistence Adapter
- JPA Entity annotated with `@Entity`, `@Table`.
- Spring Data repository interface extending `JpaRepository`.
- Adapter component implementing the Outbound Port.
- Mapper converting between JPA entity and Domain model.

### Step 6: Write Tests
- Unit test for Domain model.
- Unit test for Application service using Mockito.
- Slice test for REST Controller using `@WebMvcTest`.
- Slice test for Persistence adapter using `@DataJpaTest`.
