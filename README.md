# CTU Lecturer Review Platform - Complete Backend Documentation

![Java](https://img.shields.io/badge/Java-21-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green) ![Status](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)

Nền tảng đánh giá giảng viên ẩn danh cho sinh viên CTU, có kiểm duyệt nội dung và dashboard thống kê cho admin.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features Implemented](#features-implemented)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [API Overview](#api-overview)
- [Documentation](#documentation)
- [Database Schema](#database-schema)
- [Security Features](#security-features)

---

## Overview

Production-ready Spring Boot 3.x authentication system with:
- ✅ Complete user registration with email verification
- ✅ JWT-based stateless authentication
- ✅ Redis OTP storage with Kafka email delivery (5-minute TTL)
- ✅ Secure password reset flow
- ✅ Comprehensive error handling
- ✅ Full API documentation

---

## Features Implemented

### ✅ Authentication System
- [x] User registration with password encryption (BCrypt)
- [x] Email verification using Kafka OTP emails (6-digit, 5-minute TTL)
- [x] User login with JWT token generation (24-hour expiration)
- [x] Forgot password with OTP-based reset
- [x] Password strength validation
- [x] Account status management (enabled/disabled)

### ✅ Security Implementation
- [x] Stateless session management (no server-side sessions)
- [x] CSRF protection disabled (REST API)
- [x] CORS configuration for allowed origins
- [x] SQL injection prevention (parameterized queries)
- [x] Input validation on all endpoints
- [x] Generic error messages (no information leakage)

### ✅ Infrastructure
- [x] Redis cache for OTP storage with auto-expiry
- [x] Kafka-based OTP email delivery
- [x] Async email sending (non-blocking)
- [x] Database connection pooling (HikariCP)
- [x] Structured exception handling
- [x] Application health checks
- [x] Comprehensive logging

### ✅ Original Features (Still Intact)
- [x] Anonymous lecturer reviews (SHA-256 hashing)
- [x] Admin review moderation system
- [x] Report system for inappropriate reviews
- [x] Dashboard with statistics
- [x] Multi-faculty support
- [x] WebSocket support
- [x] Thymeleaf templates

---

## Tech Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21 LTS | Runtime Environment |
| **Spring Boot** | 3.4.1 | Web Framework |
| **Spring Security** | 6.x | Authentication & Authorization |
| **Spring Data JPA** | 3.4.1 | ORM & Database Access |
| **Spring Kafka** | 3.4.1 | Kafka messaging |
| **JWT (JJWT)** | 0.12.6 | Token Management |
| **SQL Server** | 2019+ | Primary Database |
| **Redis** | 7.x+ | Cache & OTP Storage |
| **JavaMailSender** | 3.4.1 | Email Sending |
| **Thymeleaf** | 3.x | Email Templates |
| **Maven** | 3.8+ | Build Tool |
| **Lombok** | 1.18.38 | Code Generation |

---

## Quick Start

### Prerequisites
```bash
# Install Java 21
java -version  # Should be 21.x

# Verify Maven
mvn -version   # Should be 3.8+

# Ensure Redis is running
redis-cli ping  # Expected output: PONG

# Ensure SQL Server is running
sqlcmd -S localhost -U sa -P password -Q "SELECT @@VERSION"
```

### Clone & Build
```bash
# Clone repository
git clone <repo-url>
cd Project/backend

# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Application available at: http://localhost:8080
```

### Verify Installation
```bash
# Health check
curl http://localhost:8080/actuator/health | jq .
# Expected: {"status":"UP"}

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

---

## Project Structure

```
backend/
├── src/main/java/com/example/ctu/
│   ├── controller/                 # REST API Endpoints
│   │   ├── AuthController.java     # Auth endpoints
│   │   ├── AdminController.java    # Admin operations
│   │   ├── StudentController.java  # Student operations
│   │   └── ReviewController.java   # Review management
│   │
│   ├── service/                    # Business Logic
│   │   ├── AuthService.java        # Authentication logic
│   │   ├── ReviewService.java      # Review processing
│   │
│   ├── otp/                        # OTP flow (Kafka + Redis)
│   │   ├── OtpService.java         # OTP generation & Redis storage
│   │   ├── OtpKafkaProducer.java   # Kafka producer
│   │   ├── OtpKafkaConsumer.java   # Kafka consumer
│   │   ├── OtpMailService.java     # Email delivery
│   │   └── dto/                    # OTP DTOs
│   │
│   ├── repository/                 # Database Access
│   │   ├── UserRepository.java
│   │   ├── ReviewRepository.java
│   │   ├── FacultyRepository.java
│   │   └── PendingRegistrationRepository.java
│   │
│   ├── entity/                     # JPA Entity Classes
│   │   ├── User.java               # User profile
│   │   ├── Review.java             # Review data
│   │   ├── Faculty.java            # Faculty info
│   │   └── enums/Role.java         # User roles
│   │
│   ├── dto/                        # Data Transfer Objects
│   │   ├── auth/AuthDtos.java      # Auth request/response
│   │   ├── review/ReviewDtos.java  # Review DTOs
│   │   └── admin/AdminDtos.java    # Admin DTOs
│   │
│   ├── security/                   # Security Components
│   │   ├── JwtService.java         # JWT token operations
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   │
│   ├── config/                     # Spring Configuration
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   ├── AppProperties.java
│   │   └── OpenApiConfig.java
│   │
│   ├── exception/                  # Exception Handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BadRequestException.java
│   │   └── ApiError.java
│   │
│   └── CtuReviewPlatformApplication.java
│
├── src/main/resources/
│   ├── application.yml             # Configuration
│   ├── application-dev.yml         # Dev profile
│   ├── application-prod.yml        # Prod profile
│   ├── templates/mail/             # Email templates
│   │   ├── otp-email.html
│   │   └── password-reset.html
│   └── db/migration/               # Database scripts
│
├── src/test/java/                  # Unit & Integration Tests
├── pom.xml                         # Maven dependencies
└── Dockerfile                      # Container definition
```

---

## API Overview

### Authentication Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/send-otp` | Send OTP email | ❌ |
| POST | `/api/auth/verify-otp` | Verify OTP code | ❌ |
| POST | `/api/auth/register` | Create new account | ❌ |
| POST | `/api/auth/verify` | Verify email with OTP | ❌ |
| POST | `/api/auth/login` | Login user | ❌ |
| POST | `/api/auth/forgot-password` | Request password reset | ❌ |
| POST | `/api/auth/reset-password` | Reset password with OTP | ❌ |
| PUT | `/api/auth/profile` | Update profile | ✅ JWT |
| POST | `/api/auth/change-password` | Change password | ✅ JWT |

### Example Request/Response

#### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "studentCode": "SV20001",
    "fullName": "Nguyen Van A",
    "email": "student@example.com",
    "password": "SecurePass@123",
    "facultyId": 1
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@example.com",
    "password": "SecurePass@123"
  }'

# Response
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "STUDENT",
  "verified": true,
  "fullName": "Nguyen Van A"
}
```

---

## Documentation

### Complete Guides Available

| Document | Purpose |
|----------|---------|
| [**SETUP_GUIDE.md**](./SETUP_GUIDE.md) | Complete setup, configuration, and deployment |
| [**API_DOCUMENTATION.md**](./API_DOCUMENTATION.md) | Detailed API reference with cURL examples |
| [**ARCHITECTURE.md**](./ARCHITECTURE.md) | System design, flows, and implementation details |
| [**TESTING_AND_TROUBLESHOOTING.md**](./TESTING_AND_TROUBLESHOOTING.md) | Testing strategies and 10+ troubleshooting scenarios |

### Quick Reference

**Getting Started?** → Start with [SETUP_GUIDE.md](./SETUP_GUIDE.md)  
**Want to Use API?** → Read [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)  
**Understanding Code?** → Check [ARCHITECTURE.md](./ARCHITECTURE.md)  
**Having Issues?** → See [TESTING_AND_TROUBLESHOOTING.md](./TESTING_AND_TROUBLESHOOTING.md)

---

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    student_code VARCHAR(50) NOT NULL UNIQUE,
    full_name NVARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,  -- BCrypt hash
    faculty_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    is_verified BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);
```

### OTP Storage (Redis)
```
Key Format: otp:{email}
Value: 6-digit numeric code
TTL: 5 minutes (300 seconds)
Auto-cleanup: After expiry or successful verification
```

### Key Relationships
```
Users
 ├─ faculty_id → Faculties.id
 ├─ role → {STUDENT, ADMIN, SUPER_ADMIN}
 └─ reviews → Reviews.user_id

Reviews
 ├─ lecturer_id → Lecturers.id
 ├─ is_approved → {true, false}
 └─ anonymous_hash → SHA-256(student_code + SECRET_KEY)
```

---

## Security Features

### Password Security
```
Algorithm:  BCrypt with salt rounds=10
Encoding:   Automatic on registration & password reset
Strength:   Min 6 characters (configurable)
Storage:    60-character hash in database
```

### JWT Token Security
```
Algorithm:  HMAC SHA-256
Secret:     Min 32 characters (from environment)
Expiration: 24 hours (configurable)
Claims:     User ID, Email, Role
```

### OTP Security
```
Generation:   Cryptographically secure (SecureRandom)
Format:       6 digits, numeric only
TTL:          5 minutes (configurable)
Storage:      Redis (in-memory, encrypted at rest)
```

### API Security
```
Session:      Stateless (JWT-based)
CSRF:         Disabled (REST API)
CORS:         Configured for allowed origins
SQL Injection: Prevented (parameterized queries)
Input Valid:  All endpoints validated
Error Msgs:   Generic (no information leakage)
```

---

## Configuration

### Environment Variables
```bash
# Database
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ctu_review
DB_USER=sa
DB_PASSWORD=YourPassword

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key-at-least-32-characters-long
JWT_EXPIRATION_MINUTES=1440

# OTP
OTP_TTL_MINUTES=5
OTP_APP_NAME=CTU Review Platform
OTP_MAIL_SUBJECT=Your OTP Code

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_OTP_EMAIL=otp-email-topic

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

---

## 🚀 Deployment

### Docker Deployment
```bash
# Build image
docker build -t ctu-backend:latest .

# Run container
docker run -d -p 8080:8080 \
  -e DB_URL=<db_url> \
  -e SPRING_REDIS_HOST=redis \
  ctu-backend:latest
```

### Docker Compose
```bash
docker-compose up -d
```

---

## 📞 Support

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health | jq

# Redis connection
redis-cli ping

# Database connection
sqlcmd -S localhost -U sa -P password -Q "SELECT @@VERSION"
```

### Common Commands
```bash
# Run application
mvn spring-boot:run

# Run tests
mvn test

# Build JAR
mvn clean package

# View logs
tail -f logs/application.log | grep -i "error"
```

---

## 📄 License

Proprietary software for CTU Review Platform  
All rights reserved © 2024

---

## 🔗 Useful Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/projects/spring-security)
- [JWT.io Debugger](https://jwt.io/)
- [Redis Documentation](https://redis.io/documentation)
- [SQL Server Docs](https://docs.microsoft.com/sql/)

---

**Status**: ✅ Production Ready  
**Last Updated**: May 2026  
**Version**: 1.0.0
- `GET /admin/statistics`
- `POST /admin/faculties`
- `POST /admin/subjects`
- `POST /admin/lecturers`
- `POST /admin/lecturers/import/ctu`
- `GET /admin/toxic-keywords`
- `POST /admin/toxic-keywords`
- `DELETE /admin/toxic-keywords/{id}`

Ví dụ import giảng viên từ web CTU:

```json
POST /admin/lecturers/import/ctu
{
	"maxPages": 5,
	"fallbackFacultyId": "11111111-1111-1111-1111-111111111111"
}
```

### Metadata
- `GET /metadata/faculties`
- `GET /metadata/subjects?facultyCode=...`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 5. Chạy bằng Docker

```bash
docker compose up --build
```

- App (Thymeleaf): `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- MailHog UI (OTP inbox): `http://localhost:8025`

## 6. Chạy local thủ công

### Chạy backend bằng 1 lệnh (Windows PowerShell)

```powershell
cd d:\He\Project
.\run-all.ps1
```

Nếu PowerShell báo lỗi ExecutionPolicy, dùng file BAT (không cần đổi policy):

```bat
cd /d d:\He\Project
run-all.bat
```

Lệnh trên sẽ mở 1 cửa sổ terminal chạy backend (`mvn spring-boot:run`).

### Backend

```bash
cd backend
mvn spring-boot:run
```

### UI (Thymeleaf)

Truy cập trực tiếp tại `http://localhost:8080`.

## 7. Bảo mật và ẩn danh

- Hash ẩn danh sinh viên theo secret key, không truy ngược trực tiếp sang user id.
- Từ khóa độc hại được lọc trước khi nhận review.
- Có cơ chế report cộng đồng.
- Không có API chỉnh sửa review sau khi gửi.

## 8. Seed data

Đã có seed qua bootstrap Java:
- 6 khoa mẫu CTU
- Môn học mẫu theo khoa
- Giảng viên mẫu
- Tài khoản test cho admin, student, super admin

## 9. Roadmap startup (toàn quốc)

- Bổ sung bảng `universities` và cột `university_id` ở `faculties`.
- Tách `tenant` theo trường để đa tenancy.
- Gói Premium: sentiment nâng cao, ranking sâu theo ngành, theo vùng.
- Public API cho đối tác học thuật.
- AI service riêng cho toxic detection và sentiment scoring.
