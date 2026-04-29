# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Auto Hall Landing Page Builder** - Full-stack application for creating and managing landing pages with a Spring Boot REST API backend and React frontend. Uses PostgreSQL with JSONB for flexible content storage.

**Current Branch**: `feature/dashboard-data`

## Architecture

### Backend (Spring Boot 3.5.14, Java 21, Maven)

Layered architecture with:
- **controllers/**: REST endpoints at `/api/v1/landing-pages`
- **services/**: Business logic with `@Transactional`
- **repositories/**: Spring Data JPA (`JpaRepository<LandingPage, UUID>`)
- **entities/**: JPA entities using Lombok extensively

Key technical decisions:
- **Lombok**: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`
- **JSONB**: `content` field stored as PostgreSQL JSONB (`Map<String, Object>`)
- **UUID primary keys**: `GenerationType.UUID`
- **Automatic timestamps**: `@PrePersist` and `@PreUpdate` hooks
- **CORS**: `@CrossOrigin(origins = "*")` (development only - **must restrict for production**)
- **Validation**: Minimal; service sets default status="DRAFT" if null

### Frontend (React 19, Vite 8, Tailwind CSS v4)

Modern React stack:
- **Build**: Vite with `@vitejs/plugin-react`, ES modules (`"type": "module"`)
- **Styling**: Tailwind CSS v4 with PostCSS (`@tailwindcss/postcss`) and `autoprefixer`
- **Custom theme**: Tailwind config extends with `autohall` brand color (`#004a99`)
- **HTTP**: Axios configured in `src/api/axiosConfig.js` → `http://localhost:8080/api/v1`
- **Animations**: Framer Motion v12
- **Icons**: lucide-react
- **Linting**: ESLint with React hooks and Vite refresh rules
- **Components**: `src/components/` for reusable UI; `PageCard.jsx` implements Porsche/Lando Norris dark aesthetic

### Data Model

**LandingPage** entity:
```java
UUID id (PK)
String title (required)
String slug (unique, required)
String description (optional)
String status (DRAFT/PUBLISHED/ARCHIVED, default=DRAFT)
Map<String, Object> content (JSONB, flexible structure)
LocalDateTime createdAt (auto)
LocalDateTime updatedAt (auto)
```

The `content` JSONB field is designed to hold arbitrary structured data. Currently, frontend `PageCard` expects `hero_image` and `price` keys.

### Infrastructure

- **Database**: PostgreSQL 16 via Docker Compose (`infra/docker-compose.yml`)
  - Container: `ah-lp-builder-db`
  - DB: `ah_lp_builder_db`
  - Credentials: `ah_admin_lp` / `AH_Secure_Vault_2026!`
  - Port: 5432
  - Volume: `ah_lp_data` (persistent)

## Development Commands

### Backend

```bash
cd backend

# Use Maven wrapper (recommended for consistent Maven version)
./mvnw spring-boot:run              # Linux/Mac
mvnw.cmd spring-boot:run            # Windows

# Or use system Maven if installed
mvn spring-boot:run

# Build fat JAR
./mvnw clean package
# Output: backend/target/lp-builder-api-0.0.1-SNAPSHOT.jar

# Run tests
./mvnw test

# Single test
./mvnw test -Dtest=LandingPageServiceTest

# Clean build with tests
./mvnw clean test

# Skip tests for faster build
./mvnw clean package -DskipTests
```

**Notes**:
- DevTools enabled for hot reload
- Server port: 8080 (configurable in `application.properties`)
- Hibernate `ddl-auto=update` auto-updates schema on startup

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Development server (http://localhost:5173)
npm run dev

# Build for production (outputs to dist/)
npm run build

# Preview production build locally
npm run preview

# Lint codebase
npm run lint
```

**Notes**:
- Vite config: `vite.config.js` (React plugin only)
- ESLint config: `eslint.config.js` (flat config format)
- Tailwind config: `tailwind.config.js` (custom `autohall` color available)
- PostCSS config: `postcss.config.js` with `@tailwindcss/postcss`

### Full Stack Workflow

1. **Start database** (once):
   ```bash
   docker compose -f infra/docker-compose.yml up -d
   ```

2. **Start backend** (in `backend/`):
   ```bash
   ./mvnw spring-boot:run   # or mvn spring-boot:run
   ```

3. **Start frontend** (in `frontend/`):
   ```bash
   npm run dev
   ```

4. **Access**:
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080/api/v1/landing-pages
   - Check backend health: http://localhost:8080/actuator/health (if actuator on classpath)

5. **Stop database**:
   ```bash
   docker compose -f infra/docker-compose.yml down
   # To also remove data volume:
   docker compose -f infra/docker-compose.yml down -v
   ```

## Configuration

### Backend Configuration

File: `backend/src/main/resources/application.properties`

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/ah_lp_builder_db
spring.datasource.username=ah_admin_lp
spring.datasource.password=AH_Secure_Vault_2026!
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Server
server.port=8080
```

**Production override**: Use environment variables or Spring profiles (e.g., `application-prod.properties`). Never commit real credentials.

### Frontend Configuration

- **Axios client**: `frontend/src/api/axiosConfig.js` → baseURL `http://localhost:8080/api/v1`
- **Vite**: `frontend/vite.config.js` (simple React plugin, no custom server setup)
- **Tailwind**: `frontend/tailwind.config.js` (content scan + custom `autohall` color)
- **PostCSS**: `frontend/postcss.config.js` (Tailwind v4 requires `@tailwindcss/postcss`)

### VSCode Settings

`.vscode/settings.json`:
```json
{
  "java.compile.nullAnalysis.mode": "automatic"
}
```

This enables enhanced null analysis (helpful with Lombok). Recommend installing Lombok extension.

### Database Docker Compose

File: `infra/docker-compose.yml`

```yaml
services:
  database:
    image: postgres:16-alpine
    container_name: ah-lp-builder-db
    restart: always
    environment:
      POSTGRES_USER: ah_admin_lp
      POSTGRES_PASSWORD: AH_Secure_Vault_2026!
      POSTGRES_DB: ah_lp_builder_db
    ports:
      - "5432:5432"
    volumes:
      - ah_lp_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ah_admin_lp -d ah_lp_builder_db"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  ah_lp_data:
```

## Testing

### Backend Tests

- **Run all tests**: `./mvnw test`
- **Current test suite**: Only `LpBuilderApiApplicationTests` (sanity context load)
- **Single test**: `./mvnw test -Dtest=ClassName`
- **Test reports**: `backend/target/surefire-reports/`

### Frontend Tests

- **None configured**. Consider adding Vitest + React Testing Library.
- To set up: `npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom`

## API Reference

**Base URL**: `http://localhost:8080/api/v1/landing-pages`

| Method | Endpoint        | Description                     | Request Body (LandingPage JSON) |
|--------|-----------------|---------------------------------|---------------------------------|
| GET    | `/`             | List all landing pages          | None                            |
| GET    | `/{id}`         | Get by UUID                     | None                            |
| GET    | `/slug/{slug}`  | Get by slug (public resolution) | None                            |
| POST   | `/`             | Create or update                | `{title, slug, description?, status?, content?}` |
| DELETE | `/{id}`         | Delete page                     | None                            |

**Response codes**:
- 200 OK for GET
- 201 Created for POST
- 204 No Content for DELETE
- 404 Not Found if resource doesn't exist

**Entity**: See "Data Model" section above.

**Content JSONB structure** (frontend convention):
```json
{
  "hero_image": "https://...",
  "price": "€29.999",
  // ... other custom fields
}
```

Frontend `PageCard` component currently expects `hero_image` and `price` keys.

## Important Notes

1. **CORS**: `@CrossOrigin(origins = "*")` in controller. **Restrict to frontend origin** (e.g., `http://localhost:5173`) before any production deployment.
2. **Database credentials**: Hardcoded in `application.properties`. Use Spring profiles or environment variables in production.
3. **Schema management**: `ddl-auto=update` is convenient for development but not production-safe. Plan to integrate Flyway or Liquibase for versioned migrations.
4. **Slug uniqueness**: Repository has `existsBySlug(String)` but service doesn't enforce it. Add validation if you need to prevent duplicate slugs (will violate unique constraint).
5. **JSONB content**: No schema enforcement at DB level beyond being valid JSON. Frontend should validate shape; backend could use Jackson `@JsonDeserialize` with a DTO if needed.
6. **Ports**: Backend 8080, Frontend 5173, PostgreSQL 5432. Ensure no conflicts; adjust if needed.
7. **Lombok**: IDE must have Lombok plugin; annotation processing enabled. Maven compiler plugin is configured to use Lombok.
8. **Tailwind v4**: Uses new `@tailwindcss/postcss` package. Content scanning configured in `tailwind.config.js`. Custom class `bg-autohall` available for brand color.
9. **React 19**: Uses latest React with new JSX transform. ESLint config includes `react-refresh` for Vite fast refresh.
10. **Git branch**: Working on `feature/dashboard-data`; main branch is `main`.

## Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| Backend fails to start: connection refused | PostgreSQL not running | `docker compose -f infra/docker-compose.yml up -d` and check `docker compose ps` |
| Frontend API requests 404/CORS error | Backend not reachable or CORS misconfigured | Ensure backend on port 8080; check browser console for preflight failures; verify backend `@CrossOrigin` |
| Port 8080 already in use | Another process using port | Change `server.port` in `application.properties` or stop the other process |
| Maven build fails: can't find lombok | Lombok not in annotationProcessorPath (should be configured) | Ensure `backend/pom.xml` includes Lombok in `annotationProcessorPaths` for both compile and test-compile executions |
| Frontend build fails: Tailwind classes not applied | Tailwind v4 config issue | Ensure `@tailwindcss/postcss` in dependencies; check `tailwind.config.js` content paths include `src/**/*.{js,jsx}` |
| npm install hangs or fails | Node version incompatible or network issues | Use Node 18+; check `~/.npmrc` for registry; try `npm cache clean --force` |
| Docker volume persists old data | Previous container data not cleared | `docker compose down -v` then `up -d` (warning: deletes all data) |
| Hibernate creates unexpected schema | `ddl-auto=update` will evolve schema; may not match expectations | Review SQL logs (enabled with `show-sql=true`); consider switching to `validate` or using migrations |

## Code Style & Conventions

- **Java**: Standard Spring Boot conventions; Lombok to reduce boilerplate; packages under `com.autohall.lpbuilderapi`
- **JavaScript/React**: Functional components with hooks; ES6+ syntax; Tailwind utility-first; Framer Motion for animations; lucide-react for icons
- **Naming**: camelCase for variables/methods; PascalCase for components/classes; kebab-case for CSS classes; SCREAMING_SNAKE_CASE for constants (rare)
- **Imports**: Grouped (React, external libs, internal). Use named imports unless default needed.
- **Files**: React components as `.jsx`; utilities as `.js`; Java `.java`; configs with standard extensions

## Future Considerations

- Add proper input validation (Bean Validation: `@NotBlank`, `@Size`, etc.)
- Implement slug uniqueness check in `LandingPageService.savePage()` using `existsBySlug()`
- Create DTOs for request/response instead of exposing entities directly
- Add pagination to `GET /` endpoint (currently returns all)
- Add sorting/filtering capabilities
- Add authentication/authorization (Spring Security + JWT or session)
- Consider using environment variables for DB credentials (Spring `@Value` or `application.yml` with placeholders)
- Add integration tests for controller layer (`@WebMvcTest`)
- Add frontend testing (Vitest + RTL)
- Implement frontend forms for creating/editing landing pages
- Consider separate microservice for image uploads/CDN integration
- Add rate limiting on API
- Add request logging with MDC for tracing
- Set up CI/CD pipeline (GitHub Actions)

## Additional Resources

- Backend: `backend/src/main/resources/application.properties` (config), `backend/pom.xml` (dependencies)
- Frontend: `frontend/package.json` (scripts), `frontend/vite.config.js` (build config), `frontend/tailwind.config.js` (styling)
- Docker: `infra/docker-compose.yml` (PostgreSQL service)
- API: `backend/src/main/java/com/autohall/lpbuilderapi/controllers/LandingPageController.java`
- Entity: `backend/src/main/java/com/autohall/lpbuilderapi/entities/LandingPage.java`
- Service: `backend/src/main/java/com/autohall/lpbuilderapi/services/LandingPageService.java`
- Repository: `backend/src/main/java/com/autohall/lpbuilderapi/repositories/LandingPageRepository.java`
- Axios config: `frontend/src/api/axiosConfig.js`
- Main frontend: `frontend/src/App.jsx` (grid of PageCards)

## Git Workflow

- Main branch: `main`
- Feature branches: `feature/*`
- Follow conventional commits (e.g., `feat:`, `fix:`, `refactor:`). Check `git log` for examples.
- Do not commit secrets, environment files, or IDE-specific files (`.vscode/` is already tracked but only contains settings; ensure no credentials).
- `.gitignore` exists and excludes `target/`, `node_modules/`, `.env`, etc.

---

**Last updated**: 2026-04-29 (current branch: `feature/dashboard-data`)  
**Technology versions**: Spring Boot 3.5.14, Java 21, React 19, Vite 8, Tailwind v4, PostgreSQL 16, Framer Motion 12
