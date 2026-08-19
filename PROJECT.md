# Project: CTU Review Platform Refactor

## Architecture
- **Backend**: Spring Boot 3.4.1 (Java 21), Apache Maven, JPA/Hibernate, Flyway, Spring Security, JWT (jjwt 0.12.6), Redis, Kafka, SQL Server 2022
- **Frontend**: React 19.2.7, Vite 8.1.1, Tailwind CSS 3.4, Shadcn UI, React Query v5, Axios 1.18, react-hook-form 7.81, Zod 4.4, React Router v7, Nginx
- **Infrastructure**: Docker & Docker Compose multi-container setup (SQL Server 2022, Redis, Zookeeper, Kafka, Backend JRE, Frontend Nginx)

## Code Layout
- `backend/`: Spring Boot Java application
  - `src/main/java/com/example/ctu/`: Source code
  - `src/main/resources/db/migration/`: Flyway migration scripts (`V1` through `V5`)
  - `src/main/resources/application.properties`: Configuration & logging profiles
  - `src/test/`: Unit & integration tests
  - `Dockerfile`: Multi-stage Docker build (Maven -> JRE 21)
- `frontend/`: React SPA
  - `src/components/`: Reusable UI components & `ErrorBoundary.jsx`
  - `src/pages/`: Public, Student, Admin pages
  - `src/services/api/`: Axios client & interceptors (`axiosInstance.js`)
  - `src/routes/`: Route definitions & dynamic Suspense loading
  - `vite.config.js`: Build configuration & Rollup chunk optimization
  - `Dockerfile`: Multi-stage Docker build (Node 20 -> Nginx)
  - `nginx.conf`: Production SPA reverse proxy routing
- `database/`: Raw SQL schema reference & seed scripts
- `docker-compose.yml`: Main container orchestration file

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Flyway Migration Dependencies | Add `flyway-core` and `flyway-database-sqlserver` to `backend/pom.xml` | M1 | R1 |
| 2 | Baseline Migration Script (V1) | Create `V1__init_schema.sql` consolidating schema and fixing seed `NEWID()` identity bug | M1 | R1 |
| 3 | Hibernate DDL Auto Config | Set `spring.jpa.hibernate.ddl-auto=validate` (or `none`) in `application.properties` | M1 | R1 |
| 4 | Response Wrapper Standardization | Refactor `OtpController` & `MetadataController` to return uniform `ApiResponse<T>` DTOs | M2 | R2 |
| 5 | Global Exception Handling | Structure exception handling to output clean, informative JSON error payloads | M2 | R2 |
| 6 | Security CORS & JWT Alignment | Verify CORS allowed origins env var mapping and 401 unauthorized token refresh endpoint contract | M2 | R2 |
| 7 | Logging Profile Configuration | Configure INFO/WARN/ERROR logging levels per profile and purge raw stdout prints | M2 | R2 |
| 8 | React Error Boundary | Implement `src/components/ErrorBoundary.jsx` and wrap route layouts to prevent total UI crashes | M3 | R3 |
| 9 | Axios Interceptor 401 Auto Refresh | Verify and align Axios 401 refresh interceptor with Spring Security refresh token endpoint | M3 | R3 |
| 10 | Strict Zod Form Validation | Ensure strict Zod schema validation and error feedback across all user/admin forms | M3 | R3 |
| 11 | Frontend Bundle & Asset Optimization | Configure Rollup `manualChunks` in `vite.config.js` and skeleton Suspense fallbacks | M3 | R3 |
| 12 | Multi-stage Backend Dockerfile | Multi-stage build producing minimal JRE runtime image with actuator healthcheck | M4 | R4 |
| 13 | Production Frontend Nginx Dockerfile | Multi-stage build compiling React assets served via production Nginx with SPA routing | M4 | R4 |
| 14 | Docker Compose Integration & Healthchecks | Uncomment backend/frontend in `docker-compose.yml`, configure healthchecks & dual Kafka listeners | M4 | R4 |
| 15 | E2E Acceptance & Adversarial Hardening | Verify full system via 4-tier E2E test suite and adversarial coverage checks | M5 | Acceptance |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| 1 | M1: Flyway Database Migrations | Features 1, 2, 3 | None | DONE |
| 2 | M2: Backend Quality & Security | Features 4, 5, 6, 7 | M1 | DONE |
| 3 | M3: Frontend Best Practices | Features 8, 9, 10, 11 | M2 | DONE |
| 4 | M4: Dockerization & Docker Compose | Features 12, 13, 14 | M1, M2, M3 | DONE |
| 5 | M5: Final E2E Verification & Hardening | Feature 15 | M1, M2, M3, M4 | IN_PROGRESS |


## Interface Contracts
### Auth API Contract
- `POST /api/v1/auth/send-otp`: Request `{ "email": string }` -> Response `ApiResponse<Void>`
- `POST /api/v1/auth/verify-otp`: Request `{ "email": string, "otp": string }` -> Response `ApiResponse<OtpVerifyResponse>`
- `POST /api/v1/auth/refresh-token`: Request `{ "refreshToken": string }` -> Response `ApiResponse<JwtAuthResponse>` (containing `token`, `refreshToken`)
- Error Response Format: `{ "success": false, "message": string, "error": string, "timestamp": string }`
