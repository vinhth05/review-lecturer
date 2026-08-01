# CTU Review Platform

A modern web platform that enables students at Can Tho University (CTU) to review, rate, and provide constructive feedback on lecturers. The platform promotes transparency, helps students make informed course selections, and provides valuable insights for improving teaching quality.

## Features

- Student authentication using JWT
- Secure role-based authorization with Spring Security
- Browse lecturers and courses
- Submit, edit, and delete lecturer reviews
- Rating system with average score calculation
- Search and filter lecturers
- RESTful API architecture
- Responsive user interface built with React and Material UI
- Redis caching for improved performance
- Kafka integration for asynchronous event processing

## Tech Stack

### Backend

- Java 21
- Spring Boot 3.x
- Spring Security
- JWT Authentication
- Spring Data JPA (Hibernate)
- Microsoft SQL Server
- Redis
- Apache Kafka
- Maven

### Frontend

- React 19
- Vite
- React Router
- Material UI (MUI)
- Axios
- Context API

### Infrastructure

- Docker
- Docker Compose
- SQL Server
- Redis
- Apache Kafka
- Zookeeper

---

## Project Structure

```text
CTU-Review-Platform/
├── backend/          # Spring Boot REST API
├── frontend/         # React application
├── docker-compose.yml
└── README.md
```

---

## Prerequisites

Before running the project, make sure the following software is installed:

- JDK 21
- Node.js 20+
- Maven 3.9+
- Docker
- Docker Compose

---

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd CTU-Review-Platform
```

### 2. Start Infrastructure Services

Start SQL Server, Redis, Kafka, and Zookeeper:

```bash
docker-compose up -d sqlserver redis zookeeper kafka-broker
```

Wait until all services are fully initialized before starting the backend.

---

### 3. Run the Backend

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

Backend API:

```
http://localhost:8080
```

---

### 4. Run the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend:

```
http://localhost:5173
```

---

## Run the Entire Application with Docker

To start the complete application using Docker:

```bash
docker-compose up -d
```

Available services:

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |

---

## API Documentation

After the backend starts successfully:

- Swagger UI:
  ```
  http://localhost:8080/swagger-ui/index.html
  ```

- OpenAPI Specification:
  ```
  http://localhost:8080/v3/api-docs
  ```

---

## Development

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm run dev
```

---

## Security

- JWT-based authentication
- Password encryption using BCrypt
- Role-based access control (RBAC)
- Spring Security protection
- Stateless REST API

---

## Future Improvements

- Lecturer profile analytics
- Course review system
- Review moderation
- Email verification
- Notification system
- Admin dashboard
- Mobile responsive improvements

---

## Authors

Developed by the **CTU Review Platform Team**.
---
## License

This project is intended for educational and academic purposes.
