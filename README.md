# CTU Review Platform

Modern platform for students to review and evaluate lecturers at CTU.

## Architecture

- **Backend**: Java 21, Spring Boot 3.x REST API, Spring Security, JWT, JPA/Hibernate, SQL Server, Redis, Kafka.
- **Frontend**: React 19, Vite, JSX, React Router, Material UI, Axios, Context API.

## Prerequisites

- JDK 21
- Node.js 20+
- Docker & Docker Compose (for database and messaging infrastructure)

## Getting Started

### 1. Start Infrastructure Services

Run the following command in the root directory to start SQL Server, Redis, and Kafka:

```bash
docker-compose up -d sqlserver redis zookeeper kafka-broker
```

### 2. Run Backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```
The backend API will run on `http://localhost:8080`.

### 3. Run Frontend

```bash
cd frontend
npm install
npm run dev
```
The frontend application will be available at `http://localhost:5173`.

## Docker Full Stack

You can also run both the backend and frontend entirely via Docker:

```bash
docker-compose up -d
```

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080`

## Authors

CTU
