# Original User Request

## Initial Request — 2026-08-06T02:16:39Z

Full-stack refactor of the CTU Review Platform to enterprise production standards. This includes setting up database migrations using Flyway, enhancing security and error handling in Spring Boot, optimization of React 19 frontend with Axios interceptors and error boundaries, and building a fully containerized docker-compose setup.

Working directory: d:/He/Project
Integrity mode: demo

## Requirements

### R1. Backend Database Migrations (Flyway)
Migrate the existing database schema to Flyway migrations. Move SQL Server schema definition and initial seed data into standard SQL migrations in the resources directory (`db/migration/`). Disable Hibernate's `ddl-auto` update mode in production configurations.

### R2. Backend Code Quality & Security
- Standardize REST API responses with a consistent response wrapper.
- Configure global exception handling to output clean, structured, and informative errors.
- Ensure Spring Security configurations follow best practices (CORS allowed origins configured via environment variables, CSRF protection, secure JWT validation with refresh tokens).
- Configure logging with proper profiles (INFO/WARN/ERROR) and avoid raw stdout prints in production code.

### R3. Frontend Enterprise Best Practices
- Implement global Axios interceptors for request/response handling, specifically handling automatic JWT token refresh when a 401 Unauthorized is returned.
- Implement React Error Boundaries to catch UI crashes gracefully and display user-friendly fallbacks.
- Use react-hook-form and Zod validation for all input forms (Register, Login, Change Password, Create Review, etc.) to ensure strict data validation before sending API calls.
- Optimize component loading and asset bundles.

### R4. Complete Dockerization
- Create a multi-stage production Dockerfile for backend (building the jar and running it on a minimal JRE image).
- Create a production Dockerfile for frontend (building assets and serving them via Nginx).
- Update the main `docker-compose.yml` to support building and running backend and frontend, linking them correctly to sqlserver, redis, and kafka with proper health checks and dependencies.

## Acceptance Criteria

### Backend Database Migrations
- [ ] Flyway runs successfully on application startup, creating all tables, indexes, and inserting initial seed data.
- [ ] Hibernate `ddl-auto` is set to `none` or `validate` for production profiles.

### Backend Functionality & Security
- [ ] All REST API endpoints return uniform JSON responses.
- [ ] All unit and integration tests compile and run successfully via `mvn clean test`.
- [ ] Access Tokens and Refresh Tokens work as expected; expired tokens return 401 and are refreshable.

### Frontend Quality & Build
- [ ] Frontend compiles successfully with `npm run build` without typescript/linter errors.
- [ ] React UI handles auth expiry automatically using interceptors to refresh JWT tokens in the background.
- [ ] Forms perform correct Zod validation and display meaningful validation errors.

### Docker Deployment
- [ ] `docker-compose up --build -d` successfully builds and launches all containers: sqlserver, redis, kafka-broker, backend, and frontend.
- [ ] All services start up cleanly, communicate correctly, and can be accessed on their respective ports.
