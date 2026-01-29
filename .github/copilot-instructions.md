<!-- Repository-specific instructions for AI coding agents -->
# Copilot instructions — taxi-brousse (Spring Boot)

Short and actionable guidance for working in this repo.

1) Project summary
- Java Spring Boot web app (Java 21, Maven). Uses Thymeleaf for server-side UI and PostgreSQL as the primary datastore.
- Key packages: `entity` (JPA entities), `repository` (Spring Data JPA interfaces), `service` (business logic), `controller` (web + API controllers).

2) Quick setup / commands
- Prereqs: JDK 21, Maven, PostgreSQL.
- Start app locally: `mvn spring-boot:run` (or build: `mvn -DskipTests package` then run the jar).
- Run tests: `mvn test`.
- Load DB schema/data: `psql -U postgres -f sql/Taxi_brousse.sql` (adjust DB creds in `src/main/resources/application.properties`).

3) Key architecture notes (big picture)
- MVC + REST hybrid: controllers under `controller` return Thymeleaf views (UI) while `controller/api` provides JSON endpoints.
- Persistence: JPA entities live in `entity`. Repositories extend `JpaRepository` in `repository` and are injected into services/controllers.
- Business logic belongs in `service` classes (annotated `@Service`); controllers should orchestrate request/response and delegate to services or repositories.
- Views are in `src/main/resources/templates/<module>/*.html`; static assets are in `src/main/resources/static` (CSS/JS/images).
- Async tasks: app enables async execution (`@EnableAsync` in `TaxiBrousseApplication`). Some services use `@Async` (e.g., `DiffusionService`).

4) Project-specific patterns & examples
- Controller -> view mapping: controller returns a view name matching the file in `templates`. Example: `VoyageController#listVoyages` returns `"voyage/list"` → `templates/voyage/list.html`.
- Flash messages & redirects: controllers use `RedirectAttributes` and `addFlashAttribute("success"/"error", ...)` for UI messaging; follow this style for new actions.
- Repository usage: prefer `repository.findById(id).orElseThrow(() -> new RuntimeException("..."))` for concise failure handling (used across controllers).
- API controllers return `ResponseEntity` JSON objects (see `controller/api/PricingApiController.java`) that build nested maps for complex payloads — follow the same shape for backward compatibility.
- Entities use Lombok builders (e.g., `Voyage.builder()`); Lombok is on the classpath so use it consistently.

5) Important files to inspect before changing behavior
- `pom.xml` — project SDK (Java 21), dependencies (Spring Boot starters, Lombok, Postgres), devtools enabled.
- `src/main/resources/application.properties` — DB URL, username, password, JPA `ddl-auto` (currently `validate`) — change carefully.
- `sql/Taxi_brousse.sql` — canonical schema + seed data used by developers.
- `src/main/java/com/itu/taxi_brousse/util/config/GlobalControllerAdvice.java` — adds shared model attributes (`currentPath`) used by templates.

6) Common developer tasks & gotchas
- Changing schema: update `sql/Taxi_brousse.sql` and adjust `spring.jpa.hibernate.ddl-auto` when needed locally; production expectations may require manual migrations.
- Hot reload: devtools included; modify templates/static and Spring Boot DevTools should reload the app.
- Tests: unit and integration tests are run by Maven; keep public APIs stable when editing `controller/api` endpoints.

7) How an AI agent should make changes
- Favor small, focused changes: add a new service + unit tests, update a controller route, or add a template.
- Follow existing patterns (use `@Service`, `@Repository`, `@Controller` annotations; use `RedirectAttributes` for user messages; use `orElseThrow` for missing entities).
- When adding DB columns/entities, update the `entity` class, repository (if needed), service logic, and templates that read/write the field. Mention DB migration in the PR.
- No extra, no overdoing: avoid using inline imports, complex design patterns, or unnecessary abstractions. Keep it simple and consistent with existing code. Don't add more than asked, do as I say.
- For API changes, maintain the current JSON shapes; if changing shapes, add a migration note in PR and update callers (templates or JS) accordingly.

8) Example endpoints and locations (useful references)
- UI: `GET /voyage/list` → `VoyageController` → `templates/voyage/list.html`.
- API: `GET /api/pricing/voyage/{voyageId}` → `controller/api/PricingApiController.java`.

If anything here is unclear or you'd like examples tailored to a specific change (new entity, endpoint, or template), tell me which area and I'll expand or adjust these instructions.
