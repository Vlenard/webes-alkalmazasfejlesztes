---
name: spring-boot-best-practices
description: Best practices for developing REST APIs, validation, persistence, and test slicing with Spring Boot 3+.
---

# Spring Boot 3+ Engineering Best Practices

This skill outlines performance, reliability, and code quality standards for Spring Boot 3+ backend development.

## 1. REST API Design & Contracts
- **DTOs as Java Records**: Use immutable Java `record` classes for request and response payloads.
- **Explicit HTTP Status Codes**: Return `201 Created` for resource creation (with `Location` header), `200 OK` for reads/updates, `204 No Content` for deletes.
- **OpenAPI Integration**: Annotate controllers and endpoints with `@Tag` and `@Operation` for automatic Swagger documentation.

## 2. Validation & Error Handling
- **Declarative Validation**: Use Jakarta annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Positive`, etc.) on request DTOs.
- **Unified Error Responses**: Handle exceptions centrally using `@RestControllerAdvice`.
- Return structured error JSON including HTTP status, error reason, message, path, and detailed validation error maps.
- Do NOT expose raw stack traces or internal exception classes to API consumers.

## 3. Persistence & JPA Best Practices
- **Explicit Fetching**: Avoid LazyInitializationException by configuring eager element collections or explicit entity graphs where appropriate.
- **Disable Open-Session-In-View**: Set `spring.jpa.open-in-view=false` in production to prevent unintended lazy queries during serialization.
- **Dedicated Persistence Entities**: Never expose JPA `@Entity` classes directly over REST APIs. Always translate through domain models and DTOs.

## 4. Test Slicing Strategy
- **Unit Tests**: Test pure domain logic without Spring context (`@ExtendWith(MockitoExtension.class)`).
- **Web Slice Tests**: Test controllers in isolation using `@WebMvcTest(MyController.class)`. Mock use cases with `@MockBean`.
- **JPA Slice Tests**: Test repositories and persistence adapters using `@DataJpaTest`.
- **Integration Tests**: Verify full end-to-end flows with `@SpringBootTest` and `@AutoConfigureMockMvc` using an active `@ActiveProfiles("test")`.

## 5. Build & Verification
- Always execute `./mvnw clean test` to verify that all tests pass prior to submitting changes.
