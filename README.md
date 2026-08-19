# CTU Review Platform

CTU Review Platform is a Spring Boot and React application for authenticated,
anonymous lecturer feedback. The system treats moderation, abuse reports and
sessions as explicit business workflows rather than simple CRUD records.

## Core capabilities

- Student registration, OTP verification and role-based access control.
- Anonymous lecturer reviews with rate limiting and toxic-content checks.
- Auditable review moderation: `PENDING -> APPROVED | REJECTED`.
- Report resolution that preserves evidence: dismiss or reject the review.
- Optimistic concurrency control on administrative decisions.
- Rotating, hashed refresh tokens with replay detection and session revocation.
- Correlation IDs, structured API errors, Actuator health and Prometheus metrics.
- Flyway-owned SQL Server schema, Redis, Kafka and container health checks.

The detailed design and operational rules are in
[`docs/ENTERPRISE_ARCHITECTURE.md`](docs/ENTERPRISE_ARCHITECTURE.md).

## Technology

- Backend: Java 21, Spring Boot 3.4, Spring Security, JPA, Flyway and Maven.
- Frontend: React 19, Vite, React Router, Axios, Tailwind and shadcn/ui.
- Infrastructure: SQL Server, Redis, Kafka, Nginx and Docker Compose.

## Repository layout

```text
CTU-Review-Platform/
|-- backend/          # Spring Boot API and Flyway migrations
|-- frontend/         # React application and Nginx configuration
|-- docs/             # Architecture and operational guidance
`-- docker-compose.yml
```

## Prerequisites

- JDK 21 and Maven 3.9+
- Node.js 20+
- Docker Desktop with Docker Compose

## Run with Docker Compose

Create a private local environment file first:

```powershell
Copy-Item .env.example .env
```

Replace every placeholder in `.env`. Compose intentionally fails fast when
`DB_PASSWORD`, `JWT_SECRET`, or `REVIEW_SECRET_KEY` is missing. Never commit
the resulting `.env` file.

```powershell
docker compose up --build -d
docker compose ps
```

The frontend is served at `http://localhost`; the backend API is available at
`http://localhost:8080/api/v1`. The container uses the production profile, so
Swagger is disabled. Health is available at
`http://localhost:8080/api/v1/actuator/health`.

## Local development

Start the infrastructure after creating `.env`:

```powershell
docker compose up -d sqlserver redis zookeeper kafka-broker
```

Run the backend with the required database/JWT/review environment variables
provided by your shell or IDE:

```powershell
Set-Location backend
mvn spring-boot:run
```

Run the frontend in another terminal:

```powershell
Set-Location frontend
npm ci
npm run dev
```

Swagger is available in the development profile at
`http://localhost:8080/api/v1/swagger-ui/index.html`.

Seeded role accounts are disabled by default. Enable `APP_SEED_ENABLED` only
for local development and provide all account identities and strong passwords
explicitly.

## Verification

```powershell
Set-Location backend
mvn clean test

Set-Location ../frontend
npm run lint
npm run build
```

CI runs the same backend, frontend, acceptance and Compose-model checks. Flyway
migrations under `backend/src/main/resources/db/migration` are versioned source
artifacts and must ship with every backend build.

## Security notes

- Supply production secrets through a secret manager; do not reuse keys.
- Access tokens are accepted only through the `Authorization: Bearer` header.
- Password changes, resets, account locks and verification changes revoke
  refresh sessions.
- Swagger is disabled and Actuator access is restricted in production.

This repository is intended for educational and academic use.
