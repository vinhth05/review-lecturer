# CTU Review Platform - Complete Production Setup Guide

## 📋 Table of Contents
1. [System Architecture](#system-architecture)
2. [Technology Stack](#technology-stack)
3. [Prerequisites](#prerequisites)
4. [Project Setup](#project-setup)
5. [Configuration](#configuration)
6. [Database Setup](#database-setup)
7. [API Endpoints](#api-endpoints)
8. [Authentication Flow](#authentication-flow)
9. [Redis OTP Management](#redis-otp-management)
10. [Security Features](#security-features)
11. [Testing](#testing)
12. [Deployment](#deployment)

---

## System Architecture

### High-Level Architecture

```
┌─────────────────┐
│   Client App    │  (Frontend)
└────────┬────────┘
         │ REST API
         ▼
┌─────────────────────────────────────┐
│  Spring Boot 3.x Application        │
├─────────────────────────────────────┤
│ • JWT Authentication                │
│ • Spring Security 6                 │
│ • Stateless Session Management      │
└────────┬────────────────┬───────────┘
         │                │
         ▼                ▼
        ┌────────┐     ┌──────────┐     ┌────────┐
        │  SQL   │     │  Redis   │     │ Kafka  │
        │ Server │     │  Cache   │     │ Broker │
        └────────┘     └──────────┘     └────────┘
          ▲              ▲               ▲
          │              │               │
        Spring Data JPA  StringRedisTemplate  Spring Kafka
```

### Authentication Flow

```
User Registration:
┌────────┐  Register  ┌──────────────┐  Generate OTP  ┌───────────┐
│ Client │──────────→ │ Auth Service │──────────────→ │   Redis   │
└────────┘            └──────────────┘                └───────────┘
            │
            ▼
          ┌─────────────┐
          │   Database  │
          └─────────────┘
            │
        ┌─────────┴─────────┐
        ▼                   ▼
      ┌─────────────────┐  ┌──────────────┐
      │ Save User State │  │ Publish OTP  │
      └─────────────────┘  │ to Kafka     │
               └──────┬───────┘
                ▼
                ┌──────────────┐
                │  Send Email  │
                └──────────────┘

User Verification & Login:
┌────────┐  Send OTP  ┌──────────────┐  Verify OTP  ┌───────────┐
│ Client │──────────→ │ Auth Service │────────────→ │   Redis   │
└────────┘            └──────────────┘              └───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │ Generate JWT Token  │
                    └─────────────────────┘
                              │
                              ▼
                         ┌─────────────┐
                         │   Client    │ (Token in Response)
                         └─────────────┘

Password Reset:
┌────────┐  Forgot    ┌──────────────┐  Generate OTP  ┌───────────┐
│ Client │─Password→ │ Auth Service │──────────────→ │   Redis   │
└────────┘            └──────────────┘                └───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │  Send Reset Email   │
                    └─────────────────────┘
                              │
┌────────┐  Reset     ┌──────────────┐  Verify OTP  ┌───────────┐
│ Client │─Password→ │ Auth Service │────────────→ │   Redis   │
└────────┘            └──────────────┘              └───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │  Update Password    │
                    │  (BCrypt Hashed)    │
                    └─────────────────────┘
```

---

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21 (LTS) | Primary Language |
| **Spring Boot** | 3.4.1 | Application Framework |
| **Spring Security** | 6.x | Authentication & Authorization |
| **Spring Data JPA** | 3.4.1 | Database ORM |
| **Spring Kafka** | 3.4.1 | Kafka messaging |
| **Spring Mail** | 3.4.1 | Email Sending |
| **Thymeleaf** | 3.x | Email Templates |
| **JWT (JJWT)** | 0.12.6 | Token Management |
| **SQL Server** | 2019+ | Primary Database |
| **Redis** | 7.x+ | OTP Cache & Session Store |
| **BCrypt** | Spring Security | Password Encryption |
| **Maven** | 3.8.x+ | Build Tool |
| **Lombok** | 1.18.38 | Code Generation |

---

## Prerequisites

### System Requirements
- **Java 21 (LTS)** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.8.x+** - [Download](https://maven.apache.org/download.cgi)
- **SQL Server 2019+** - [Download](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)
- **Redis 7.x+** - [Download](https://redis.io/download) or use Docker
- **Kafka 3.x+** - [Download](https://kafka.apache.org/downloads) or use Docker

### Verify Installation
```bash
# Check Java version
java -version

# Check Maven version
mvn -version

# Check Redis (if installed locally)
redis-cli --version
```

---

## Project Setup

### Step 1: Clone & Navigate
```bash
git clone <repository-url>
cd Project/backend
```

### Step 2: Build Project
```bash
# Clean build
mvn clean install

# Build only (skip tests)
mvn clean package -DskipTests
```

### Step 3: Start Redis Server

**Option A: Docker (Recommended)**
```bash
# Start Redis container
docker run -d --name ctu-redis -p 6379:6379 redis:7-alpine

# Verify Redis is running
redis-cli ping
# Expected output: PONG

# Stop Redis
docker stop ctu-redis

# Resume Redis
docker start ctu-redis
```

**Option B: Local Installation**
```bash
# Windows
redis-server.exe

# macOS
redis-server

# Linux
redis-server
```

### Step 4: Start Kafka

**Option A: Docker (Recommended)**
```bash
docker run -d --name ctu-kafka -p 9092:9092 \
  -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  bitnami/kafka:3
```

**Option B: Local Installation**
- Start ZooKeeper (if needed)
- Start Kafka broker on `localhost:9092`

### Step 5: Start SQL Server

**Option A: Docker**
```bash
docker run -d \
  --name ctu-sqlserver \
  -p 1433:1433 \
  -e MSSQL_SA_PASSWORD=YourPassword123! \
  -e MSSQL_PID=Developer \
  mcr.microsoft.com/mssql/server:2022-latest
```

**Option B: Windows Installation**
- Download from SQL Server downloads page
- Run installer and follow setup wizard

### Step 6: Create Database
```sql
-- Connect to SQL Server with SQL Server Management Studio (SSMS)
-- Or use sqlcmd from command line

CREATE DATABASE ctu_review;
USE ctu_review;
```

### Step 7: Run Application
```bash
# Navigate to backend directory
cd Project/backend

# Run Maven Spring Boot
mvn spring-boot:run

# Or run JAR directly
java -jar target/review-ctu-0.0.1-SNAPSHOT.jar

# Expected output:
# Started CtuReviewPlatformApplication in X.XXX seconds
```

### Step 8: Verify Application
```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}

# Check Swagger UI
# Open browser: http://localhost:8080/swagger-ui.html
```

---

## Configuration

### Environment Variables

Create a `.env` file or set system environment variables:

```bash
# Database Configuration
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=ctu_review;encrypt=true;trustServerCertificate=true
DB_USER=sa
DB_PASSWORD=YourPassword123

# Redis Configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=          # Leave empty for local Redis

# JWT Configuration
JWT_SECRET=your-secret-key-at-least-32-characters-long-for-security
JWT_EXPIRATION_MINUTES=1440     # 24 hours

# OTP Configuration
OTP_TTL_MINUTES=5               # OTP expires in 5 minutes
OTP_APP_NAME=CTU Review Platform
OTP_MAIL_SUBJECT=Your OTP Code

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC_OTP_EMAIL=otp-email-topic

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password  # Use app-specific password for Gmail
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Seed Accounts (for testing)
APP_SEED_ADMIN_EMAIL=admin@ctu.edu.vn
APP_SEED_ADMIN_PASSWORD=Admin@123
```

### application.yml

Create or update `backend/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: ctu-review-platform
  
  # Data Source
  datasource:
    url: ${DB_URL:jdbc:sqlserver://localhost:1433;databaseName=ctu_review;encrypt=true;trustServerCertificate=true}
    username: ${DB_USER:sa}
    password: ${DB_PASSWORD:123456}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  
  # JPA/Hibernate
  jpa:
    hibernate:
      ddl-auto: update              # auto | validate | create | create-drop | update
    show-sql: false
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServerDialect
        jdbc:
          time_zone: UTC
  
  # Redis
  redis:
    host: ${SPRING_REDIS_HOST:localhost}
    port: ${SPRING_REDIS_PORT:6379}
    password: ${SPRING_REDIS_PASSWORD:}
    timeout: 60000ms

  # Kafka
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: ${KAFKA_CONSUMER_GROUP:otp-email-group}
      auto-offset-reset: latest
    admin:
      auto-create: true
  
  # Mail
  mail:
    host: ${MAIL_HOST:localhost}
    port: ${MAIL_PORT:1025}
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    properties:
      mail:
        smtp:
          auth: ${MAIL_SMTP_AUTH:false}
          starttls:
            enable: ${MAIL_SMTP_STARTTLS:false}

# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /

# Application Properties
app:
  jwt:
    secret: ${JWT_SECRET:change-me-change-me-change-me-change-me}
    expiration-minutes: ${JWT_EXPIRATION_MINUTES:1440}
  
  otp:
    ttl-minutes: ${OTP_TTL_MINUTES:5}
    app-name: ${OTP_APP_NAME:CTU Review Platform}
    mail-subject: ${OTP_MAIL_SUBJECT:Your OTP Code}
  
  cors:
    allowed-origins:
      - ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

---

## Database Setup

### Initialize Database Schema

The application uses **Hibernate auto DDL** (`spring.jpa.hibernate.ddl-auto=update`), which automatically creates/updates tables on startup.

### Manual Table Creation (Optional)

If you prefer manual control, execute these SQL scripts:

```sql
-- Users Table
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_code VARCHAR(50) NOT NULL UNIQUE,
    full_name NVARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    faculty_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    is_verified BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_users_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_student_code ON users(student_code);
```

### Seed Test Data

The application automatically creates seed accounts via `RoleTestAccountBootstrap`:

| Account | Email | Password | Role |
|---------|-------|----------|------|
| Admin | admin@ctu.edu.vn | Admin@123 | ADMIN |
| Super Admin | superadmin@ctu.edu.vn | Super@123 | SUPER_ADMIN |
| Student | student01@student.ctu.edu.vn | Student@123 | STUDENT |

---

## API Endpoints

### Authentication Endpoints

#### 1. Send OTP
```
POST /api/auth/send-otp
Content-Type: application/json

{
  "email": "student@example.com"
}

Response (202 Accepted)
```

#### 2. Verify OTP
```
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "student@example.com",
  "otp": "123456"
}

Response (200 OK)
{
  "verified": true
}
```

#### 3. Register New User
```
POST /api/auth/register
Content-Type: application/json

{
  "studentCode": "SV12345",
  "fullName": "Tran Van A",
  "email": "student@example.com",
  "password": "Password@123",
  "facultyId": 1
}

Response (201 Created):
{
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác thực OTP."
}
```

#### 4. Verify Email OTP
```
POST /api/auth/verify
Content-Type: application/json

{
  "email": "student@example.com",
  "otp": "123456"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "STUDENT",
  "verified": true,
  "fullName": "Tran Van A",
  "facultyName": "Information Technology"
}
```

#### 5. Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "student@example.com",
  "password": "Password@123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "STUDENT",
  "verified": true,
  "fullName": "Tran Van A",
  "facultyName": "Information Technology"
}
```

#### 6. Forgot Password - Request Reset
```
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "student@example.com"
}

Response (200 OK):
{
  "message": "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi."
}
```

#### 7. Reset Password
```
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "student@example.com",
  "otp": "123456",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}

Response (200 OK):
{
  "message": "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."
}
```

### Protected Endpoints (Require JWT)

#### Change Password
```
POST /api/auth/change-password
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "currentPassword": "Password@123",
  "newPassword": "NewPassword@456",
  "confirmPassword": "NewPassword@456"
}
```

#### Update Profile
```
PUT /api/auth/profile
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "fullName": "Tran Van B",
  "facultyId": 2
}
```

---

## Authentication Flow

### JWT Token Structure

```
Header.Payload.Signature

Example:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
.eyJzdWIiOiJlbWFpbEBleGFtcGxlLmNvbSIsInVpZCI6MSwiucm9sZSI6IlNUVURFTlQiLCJpYXQiOjE2MzUzNDIwMDAsImV4cCI6MTYzNTQyODQwMH0
.signed_signature_here
```

### Token Validation

1. **Token Source**: Authorization header or AUTH_TOKEN cookie
2. **Format**: `Authorization: Bearer <token>`
3. **Validation**:
   - Signature verification
   - Expiration check
   - Subject (email) verification

### Using Token in Requests

```bash
# Option 1: Authorization Header
curl -X GET http://localhost:8080/api/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Option 2: Cookie (auto-handled by browser)
# Cookie: AUTH_TOKEN=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## Redis OTP Management

### OTP Key Format

```
otp:{email}
Example: otp:student@example.com
```

### OTP Lifecycle

1. **Generation** (50ms)
   - Generate 6-digit code using SecureRandom
   - Save to Redis with TTL (5 minutes)

2. **Delivery** (Async)
  - Publish message to Kafka
  - Send email from Kafka consumer

3. **Verification** (On demand)
   - Compare user input with Redis value
   - Delete OTP after successful verification
   - Return 400 if expired or invalid

4. **Expiration** (Automatic)
   - Redis automatically deletes after TTL expires
   - Client re-requests new OTP

### Redis Commands (Debugging)

```bash
# Connect to Redis
redis-cli

# View all OTP keys
KEYS otp:*

# Check OTP value
GET otp:student@example.com

# Check remaining TTL (seconds)
TTL otp:student@example.com

# Manually delete OTP
DEL otp:student@example.com

# Monitor Redis in real-time
MONITOR
```

---

## Security Features

### 1. Password Security
- **Algorithm**: BCrypt with salt
- **Strength**: Min 6 characters (configurable)
- **Hashing**: Automatic on registration & password reset

### 2. JWT Security
- **Algorithm**: HMAC SHA-256
- **Secret**: Min 32 characters (configurable via environment)
- **Expiration**: 24 hours (configurable)
- **Claims**: User ID, Email, Role

### 3. OTP Security
- **Generation**: Cryptographically secure (SecureRandom)
- **Length**: 6 digits (1,000,000 combinations)
- **TTL**: 5 minutes
- **Storage**: Redis (in-memory, encrypted at rest)
- **Rate Limiting**: Max 3 requests per 60 seconds

### 4. Session Management
- **Type**: Stateless (JWT-based)
- **CSRF**: Disabled (stateless API)
- **CORS**: Configured for allowed origins

### 5. Request Validation
- **Email**: RFC 5322 compliant
- **Password**: Strength requirements enforced
- **OTP**: Numeric only, 6 digits
- **SQL Injection**: Prevented via parameterized queries (Spring Data)

### 6. Error Handling
- **Generic Messages**: No information leakage
- **Logging**: Detailed server-side logging
- **Status Codes**: Appropriate HTTP codes

---

## Testing

### Manual Testing with cURL

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "studentCode": "SV999",
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "TestPass@123",
    "facultyId": 1
  }'

# 2. Get OTP from Redis (debugging)
redis-cli GET "otp:test@example.com"
# Output: 123456 (example)

# 3. Verify OTP
curl -X POST http://localhost:8080/api/auth/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456"
  }'

# 4. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass@123"
  }'

# Response will include JWT token
# Copy the token for next requests

# 5. Use Token (Protected Endpoint)
TOKEN="your_jwt_token_here"
curl -X GET http://localhost:8080/api/profile \
  -H "Authorization: Bearer $TOKEN"

# 6. Forgot Password
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com"
  }'

# 7. Reset Password
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456",
    "newPassword": "NewPass@456",
    "confirmPassword": "NewPass@456"
  }'
```

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn test jacoco:report
```

### Integration Tests

```bash
# Run integration tests only
mvn verify -Dgroups=integration

# Run with debug logging
mvn test -Ddebug
```

---

## Deployment

### Production Checklist

- [ ] Change JWT secret to strong value (32+ characters)
- [ ] Set production database URL
- [ ] Configure production email credentials
- [ ] Update CORS allowed origins
- [ ] Enable HTTPS/TLS
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Configure logging level to INFO/WARN
- [ ] Set up monitoring & alerting
- [ ] Configure backup strategy
- [ ] Test disaster recovery

### Build for Deployment

```bash
# Clean build
mvn clean package

# JAR location: target/review-ctu-0.0.1-SNAPSHOT.jar

# Run with production profile
java -Dspring.profiles.active=prod -jar target/review-ctu-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

Create `Dockerfile`:

```dockerfile
FROM openjdk:21-slim

WORKDIR /app

COPY target/review-ctu-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
```

Build and run:

```bash
# Build image
docker build -t ctu-review-backend:latest .

# Run container
docker run -d \
  --name ctu-backend \
  -p 8080:8080 \
  -e DB_URL=jdbc:sqlserver://host.docker.internal:1433;databaseName=ctu_review \
  -e SPRING_REDIS_HOST=redis \
  ctu-review-backend:latest
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    environment:
      MSSQL_SA_PASSWORD: YourPassword123!
      MSSQL_PID: Developer
    ports:
      - "1433:1433"
    volumes:
      - sqlserver_data:/var/opt/mssql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:sqlserver://sqlserver:1433;databaseName=ctu_review
      DB_USER: sa
      DB_PASSWORD: YourPassword123!
      SPRING_REDIS_HOST: redis
      JWT_SECRET: your-production-secret-key-here
      MAIL_HOST: smtp.gmail.com
      MAIL_PORT: 587
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
    depends_on:
      - sqlserver
      - redis

volumes:
  sqlserver_data:
  redis_data:
```

Run with Docker Compose:

```bash
docker-compose up -d
```

---

## Troubleshooting

### Issue: OTP emails not sending

**Solutions**:
1. Check email configuration (SMTP credentials)
2. Enable "Less secure apps" or use app-specific password (Gmail)
3. Check application logs: `tail -f logs/application.log`
4. Verify email template exists: `backend/src/main/resources/templates/mail/otp.html`

### Issue: Redis connection failed

**Solutions**:
1. Verify Redis is running: `redis-cli ping`
2. Check Redis configuration in `application.yml`
3. Use Docker if local installation fails
4. Check firewall rules for port 6379

### Issue: Database connection failed

**Solutions**:
1. Verify SQL Server is running
2. Check credentials in `application.yml`
3. Verify database exists: `CREATE DATABASE ctu_review;`
4. Check network connectivity to database server

### Issue: JWT token invalid

**Solutions**:
1. Verify JWT_SECRET matches across environment and code
2. Check token expiration: TTL is 24 hours by default
3. Ensure token format: `Authorization: Bearer <token>`
4. Check server clock synchronization

---

## Production Best Practices

1. **Security**:
   - Use environment variables for secrets
   - Enable HTTPS/TLS
   - Implement rate limiting on all endpoints
   - Regular security audits

2. **Performance**:
   - Enable Redis caching
   - Use connection pooling for database
   - Implement CDN for static assets
   - Monitor response times

3. **Monitoring**:
   - Enable application logging
   - Set up alerts for errors
   - Monitor Redis memory usage
   - Monitor database query performance

4. **Backup**:
   - Daily database backups
   - Point-in-time recovery enabled
   - Test restore procedures regularly

5. **Documentation**:
   - Maintain API documentation
   - Keep deployment guides updated
   - Document troubleshooting procedures

---

## Support & Additional Resources

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **JWT (JJWT)**: https://github.com/jwtk/jjwt
- **Redis**: https://redis.io/documentation
- **SQL Server**: https://docs.microsoft.com/sql/

---

## License

This project is proprietary software for CTU Review Platform.

---

**Generated**: $(date)  
**Version**: 1.0.0  
**Last Updated**: May 2026
