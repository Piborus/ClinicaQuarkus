# AGENTS

## Project snapshot
- Quarkus REST API for a clinic domain, organized by layers: `resource` (HTTP), `service` (business), `repository` (Panache), `entity` (JPA) under `src/main/java/br/ce/clinica/`.
- Reactive stack end-to-end: Mutiny `Uni`/`Multi`, Hibernate Reactive Panache, and `@WithSession` on resources (e.g., `resource/PacienteResource.java`).
- Migrations live in `src/main/resources/db/migration/` and are applied via Flyway at startup (see `application.properties`).

## Architecture patterns (with examples)
- **Resource -> Service -> Repository**: REST endpoints call services, which orchestrate repositories and validations. Example flow in `resource/PacienteResource.java` -> `service/impl/PacienteServiceImpl.java` -> `repository/PacienteRepository.java`.
- **DTO mapping**: requests/responses are in `dto/`, with mapping helpers like `PacienteResponse.toResponse(...)` used inside services.
- **Soft delete**: entities are archived via status/deletado fields and related records updated together (see `PacienteServiceImpl.softDelete` and `softDeleteRelacionados`).
- **Dynamic filtering + pagination**: repositories build JPQL with filter fields/values and use `PanacheQuery` + `Page` (`repository/PacienteRepository.findPaginated`).
- **Centralized error mapping**: business exceptions extend `BusinessException` and are mapped in `exception/mapper/GlobalExceptionHandler.java` to a problem+json style response.
- **OpenAPI defaults**: common response documentation is added via `@ApiDocumentation` annotation (`openapi/ApiDocumentation.java`) on resources.

## Security & auth
- JWT auth with roles: resources use `@RolesAllowed` and `@Authenticated` (see `resource/PacienteResource.java`).
- Token issuance + refresh flow: `service/impl/AuthServiceImpl.java` uses `security/GenerateToken` and `security/RefreshToken`.
- Refresh tokens and password-recovery codes are stored in Redis (`security/RefreshToken.java`, `security/RecuperacaoSenhaRedisService.java`).

## Integrations
- **PostgreSQL**: reactive datasource configured in `application.properties` (runtime) with JDBC only for Flyway.
- **Redis**: used for refresh tokens and password recovery (`application.properties` + `security/*`).
- **Mailer + templates**: Qute templates in `src/main/resources/templates/mail/` and inline image from `META-INF/microprofile-jwt/imagens/` (see `service/impl/EmailServiceImpl.java`).
- **Scheduler**: reminder emails are sent by `scheduler/LembreteScheduler.java` every minute using the reactive mailer.

## Dev workflows (from repo)
- Dev mode: `./mvnw quarkus:dev` (Windows: `mvnw.cmd` exists in repo).
- Package: `./mvnw package` (see `README.md`).
- Native builds: `./mvnw package -Dnative` (see `README.md`).
- Local infra: `docker-compose.yaml` provides PostgreSQL + Redis; credentials come from environment variables.

## Testing conventions
- REST tests use `@QuarkusTest`, RestAssured, and `@TestSecurity` for role simulation (see `src/test/java/br/ce/clinica/resource/PacienteResourceTest.java`).
- Services are mocked in resource tests using `@InjectMock` (same file).

