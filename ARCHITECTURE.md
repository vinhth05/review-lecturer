# CTU Review Platform - Complete Architecture & Implementation

## 🏗️ System Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────┐
│              Presentation Layer                      │
│  (REST Controllers, Request/Response Handling)       │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│              Application Layer                       │
│  (Business Logic, Service Classes)                   │
├─────────────────────────────────────────────────────┤
│ • AuthService          • OtpService                 │
│ • ReviewService        • RateLimitService           │
│ • RedisOtpService      • EmailService               │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│              Data Access Layer                       │
│  (Repositories, Database Access)                     │
├─────────────────────────────────────────────────────┤
│ • UserRepository       • OtpTokenRepository          │
│ • ReviewRepository     • FacultyRepository           │
│ • StringRedisTemplate  • Spring Data JPA             │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│              Infrastructure Layer                    │
│  (External Services, Databases)                      │
├─────────────────────────────────────────────────────┤
│ • SQL Server 2019+     • Redis 7.x+                 │
│ • JavaMailSender       • Spring Security            │
│ • JWT Token Service    • BCrypt Password Encoder    │
└─────────────────────────────────────────────────────┘
```

---

## 🔐 Security Architecture

### Security Layers

```
┌─────────────────────────────────────────────────────┐
│         1. Input Validation                          │
│    (Bean Validation, Custom Validators)              │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│         2. Authentication Filter                     │
│         (JwtAuthenticationFilter)                    │
├─────────────────────────────────────────────────────┤
│ • Extract JWT from header/cookie                    │
│ • Validate JWT signature                            │
│ • Extract claims (email, role)                      │
│ • Load user details                                 │
│ • Set security context                              │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│         3. Authorization Check                       │
│   (SecurityConfig, @PreAuthorize annotations)       │
├─────────────────────────────────────────────────────┤
│ • Check user role (STUDENT, ADMIN, etc.)           │
│ • Verify endpoint permissions                       │
│ • Enforce method-level security                     │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│         4. Business Logic Layer                      │
│     (AuthService, ReviewService, etc.)              │
├─────────────────────────────────────────────────────┤
│ • Validate OTP                                      │
│ • Check rate limits                                 │
│ • Verify business rules                             │
└────────────────┬────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────┐
│         5. Exception Handling                        │
│   (GlobalExceptionHandler, Custom Exceptions)       │
├─────────────────────────────────────────────────────┤
│ • Catch all exceptions                              │
│ • Return appropriate error responses                │
│ • Log security events                               │
└─────────────────────────────────────────────────────┘
```

---

## 📦 Project Structure

```
backend/
│
├── src/main/java/com/example/ctu/
│   │
│   ├── controller/                 # REST Endpoints
│   │   ├── AuthController.java
│   │   ├── AdminController.java
│   │   ├── StudentController.java
│   │   └── ReviewController.java
│   │
│   ├── service/                    # Business Logic
│   │   ├── AuthService.java
│   │   ├── OtpService.java
│   │   ├── RedisOtpService.java
│   │   ├── ReviewService.java
│   │   ├── RateLimitService.java
│   │   └── EmailService.java
│   │
│   ├── repository/                 # Database Access
│   │   ├── UserRepository.java
│   │   ├── OtpTokenRepository.java
│   │   ├── ReviewRepository.java
│   │   └── FacultyRepository.java
│   │
│   ├── entity/                     # JPA Entities
│   │   ├── User.java
│   │   ├── OtpToken.java
│   │   ├── Review.java
│   │   ├── Faculty.java
│   │   └── enums/
│   │       └── Role.java
│   │
│   ├── dto/                        # Data Transfer Objects
│   │   ├── auth/
│   │   │   └── AuthDtos.java
│   │   ├── review/
│   │   │   └── ReviewDtos.java
│   │   └── admin/
│   │       └── AdminDtos.java
│   │
│   ├── security/                   # Security Components
│   │   ├── JwtService.java
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
│   │   ├── ResourceNotFoundException.java
│   │   └── ApiError.java
│   │
│   └── CtuReviewPlatformApplication.java
│
├── src/main/resources/
│   │
│   ├── application.yml             # Main config
│   ├── application-dev.yml         # Dev profile
│   ├── application-prod.yml        # Prod profile
│   │
│   ├── templates/
│   │   └── mail/
│   │       ├── otp.html
│   │       ├── password-reset.html
│   │       └── verification.html
│   │
│   ├── static/
│   │   └── css/
│   │       ├── site.css
│   │       └── bootstrap.min.css
│   │
│   └── db/
│       └── migration/
│           ├── V1__create_tables.sql
│           └── V2__seed_data.sql
│
├── src/test/java/com/example/ctu/
│   ├── service/
│   │   ├── AuthServiceTest.java
│   │   └── OtpServiceTest.java
│   └── controller/
│       └── AuthControllerTest.java
│
└── pom.xml
```

---

## 🔄 Authentication Flow Diagrams

### Registration & Email Verification Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 1. POST /api/auth/register
       │    (studentCode, fullName, email, password, facultyId)
       ▼
┌──────────────────────────────────────────┐
│        AuthController.register()         │
└──────┬───────────────────────────────────┘
       │
       │ 2. Validate Request
       │    • Email format
       │    • Faculty exists
       │    • Email uniqueness
       ▼
┌──────────────────────────────────────────┐
│     AuthService.register()               │
└──────┬───────────────────────────────────┘
       │
       │ 3. Hash Password
       │    Password ──→ BCrypt ──→ Hash
       │
       │ 4. Save User (unverified)
       │    to Database
       │
       │ 5. Generate OTP
       │    SecureRandom ──→ 6-digit
       ▼
┌──────────────────────────────────────────┐
│      OtpService.generateAndSend()        │
└──────┬───────────────────────────────────┘
       │
       ├─→ 6a. Store OTP in Redis
       │       Key: OTP:email
       │       Value: 123456
       │       TTL: 5 minutes
       │
       ├─→ 6b. Send Email (Async)
       │       • Read HTML template
       │       • Replace {{otp}} placeholder
       │       • Send via JavaMailSender
       │
       └─→ 7. Return 201 to Client
            "Registration successful. Check email."
           
            ▼
       ┌─────────────┐
       │   Client    │
       │ (Check Email)
       └─────────────┘
           │
           │ 8. User receives email with OTP
           │    E.g., OTP = "123456"
           │
           │ 9. POST /api/auth/verify
           │    (email, otp)
           ▼
┌──────────────────────────────────────────┐
│       AuthController.verify()            │
└──────┬───────────────────────────────────┘
       │
       │ 10. Call OtpService.verify()
       ▼
┌──────────────────────────────────────────┐
│      OtpService.verify()                 │
└──────┬───────────────────────────────────┘
       │
       │ 11. Compare with Redis
       │     Input OTP (123456)
       │     ===
       │     Redis OTP (123456) ✓
       │
       │ 12. Delete OTP from Redis
       │     (One-time use)
       │
       │ 13. Mark User verified
       ▼
┌──────────────────────────────────────────┐
│     AuthService.createAuthResponse()     │
└──────┬───────────────────────────────────┘
       │
       │ 14. Generate JWT Token
       │     • Subject: email
       │     • Claims: userId, role
       │     • Expiry: 24 hours
       │
       │ 15. Return Response with Token
       ▼
┌─────────────────────────────────────────┐
│   Client                                │
│ {                                       │
│   "token": "eyJhbGc...",               │
│   "role": "STUDENT",                   │
│   "verified": true,                    │
│   "fullName": "Name"                   │
│ }                                       │
└─────────────────────────────────────────┘
```

### Login Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/login
       │ (email, password)
       ▼
┌──────────────────────────────────────┐
│  AuthController.login()              │
└──────┬───────────────────────────────┘
       │
       │ Validate Request
       ▼
┌──────────────────────────────────────┐
│  AuthService.login()                 │
└──────┬───────────────────────────────┘
       │
       ├─→ Load User by Email
       │   from Database
       │
       ├─→ Authenticate with
       │   AuthenticationManager
       │   • Load password hash
       │   • Compare passwords
       │   • BCrypt.matches()
       │
       ├─→ Verify Email Verified
       │   user.isVerified() == true
       │
       ├─→ Generate JWT Token
       │
       └─→ Return Token + User Info
           ▼
       ┌─────────────┐
       │   Client    │
       │  (Token)    │
       └─────────────┘
           │
           │ Save Token in localStorage
           │ or sessionStorage
           │
           ├─→ All subsequent requests
           │   include:
           │   Authorization: Bearer <token>
           │
           │ Token is decoded & verified
           │ at each request
           │
           └─→ User logged in!
```

### Password Reset Flow

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/forgot-password
       │ (email)
       ▼
┌──────────────────────────────────────┐
│ AuthController.forgotPassword()      │
└──────┬───────────────────────────────┘
       │
       │ ✓ Verify user exists (optional)
       │ ✓ Generate OTP
       │ ✓ Store in Redis
       │ ✓ Send email
       │
       └─→ Return generic message
           (no user enumeration)
           ▼
       ┌─────────────┐
       │   Client    │
       │(Check Email)
       └─────────────┘
           │
           │ Receives email with OTP
           │ & reset link
           │
           │ POST /api/auth/reset-password
           │ (email, otp, newPassword)
           ▼
┌──────────────────────────────────────┐
│ AuthController.resetPassword()       │
└──────┬───────────────────────────────┘
       │
       │ Validate passwords match
       ▼
┌──────────────────────────────────────┐
│ AuthService.resetPassword()          │
└──────┬───────────────────────────────┘
       │
       ├─→ Verify OTP
       │   Redis lookup ✓
       │
       ├─→ Delete OTP
       │   (One-time use)
       │
       ├─→ Load User
       │
       ├─→ Hash new password
       │   BCrypt
       │
       ├─→ Update password
       │   in Database
       │
       └─→ Return success
           User can now login
           with new password
```

---

## 🗄️ Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_code VARCHAR(50) NOT NULL UNIQUE,
    full_name NVARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,  -- BCrypt hash (60 chars)
    faculty_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    is_verified BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_users_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_student_code UNIQUE (student_code)
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_student_code ON users(student_code);
```

**Fields Explanation**:
- `id`: Auto-increment primary key
- `student_code`: Unique student identifier (50 chars max)
- `full_name`: User's full name (Vietnamese)
- `email`: Unique email address
- `password_hash`: BCrypt hashed password (60 characters)
- `faculty_id`: Foreign key to faculties table
- `role`: User role (STUDENT, ADMIN, SUPER_ADMIN)
- `is_verified`: Boolean for email verification status
- `created_at`: Record creation timestamp

### OTP Tokens Table

```sql
CREATE TABLE otp_tokens (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(20) NOT NULL,
    expires_at DATETIME NOT NULL,
    used BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_otp_tokens_email ON otp_tokens(email);
CREATE INDEX idx_otp_tokens_expires_at ON otp_tokens(expires_at);
```

**Note**: In production, OTP is stored in Redis (in-memory cache), not database.

### Redis Key Format

```
Key: OTP:{email}
Value: 6-digit numeric code
TTL: 5 minutes (300 seconds)
Example: OTP:student@example.com → 123456
```

---

## 🔐 JWT Token Structure

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "user@example.com",
  "uid": 1,
  "role": "STUDENT",
  "iat": 1635342000,
  "exp": 1635428400
}
```

**Claims**:
- `sub` (subject): User email
- `uid` (custom): User ID
- `role` (custom): User role
- `iat` (issued at): Token creation timestamp
- `exp` (expiration): Token expiration timestamp

### Signature
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

### Full Token Example
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidWlkIjoxLCJyb2xlIjoiU1RVREVOVCJ9.
signed_signature_here
```

---

## 🔌 API Gateway & Request Flow

### Request Processing Pipeline

```
1. CLIENT REQUEST
   ├─ URL: http://localhost:8080/api/auth/login
   ├─ Method: POST
   ├─ Headers: Content-Type: application/json
   └─ Body: {"email": "...", "password": "..."}

   │
   ▼

2. SPRING DISPATCHER SERVLET
   └─ Intercept request, route to handler

   │
   ▼

3. SECURITY FILTER CHAIN
   ├─ CorsFilter (Handle CORS)
   ├─ JwtAuthenticationFilter
   │  ├─ Extract token from header/cookie
   │  ├─ Validate JWT signature
   │  ├─ Load user details
   │  └─ Set SecurityContext
   └─ Other security filters

   │
   ▼

4. CONTROLLER HANDLER
   ├─ @PostMapping("/login")
   ├─ Receive request object
   ├─ Validate @Valid constraints
   └─ Call AuthService

   │
   ▼

5. SERVICE LAYER
   ├─ AuthService.login()
   ├─ AuthenticationManager.authenticate()
   ├─ Load user from database
   ├─ Verify password
   ├─ Generate JWT token
   └─ Return response

   │
   ▼

6. RESPONSE HANDLER
   ├─ Convert to JSON
   ├─ Add HTTP headers
   ├─ Set status code (200)
   └─ Serialize response

   │
   ▼

7. CLIENT RECEIVES
   ├─ Status: 200 OK
   ├─ Headers: Content-Type: application/json
   └─ Body: {"token": "...", "role": "STUDENT", ...}
```

---

## 🛡️ Exception Handling Flow

### Global Exception Handler

```
┌────────────────────────────────┐
│ Exception Thrown               │
│ (anywhere in application)      │
└──────┬─────────────────────────┘
       │
       ▼
┌────────────────────────────────┐
│ Spring catches exception       │
│ DispatcherServlet              │
└──────┬─────────────────────────┘
       │
       ▼
┌────────────────────────────────────────┐
│ GlobalExceptionHandler intercepts      │
│ @ControllerAdvice                      │
│ @ExceptionHandler                      │
└──────┬─────────────────────────────────┘
       │
       ├─→ ResourceNotFoundException
       │   ├─ Status: 404
       │   └─ Message: "Resource not found"
       │
       ├─→ BadRequestException
       │   ├─ Status: 400
       │   └─ Message: Business logic error
       │
       ├─→ AuthenticationException
       │   ├─ Status: 401
       │   └─ Message: "Unauthorized"
       │
       ├─→ AccessDeniedException
       │   ├─ Status: 403
       │   └─ Message: "Forbidden"
       │
       └─→ Generic Exception
           ├─ Status: 500
           └─ Message: "Internal server error"
       
       │
       ▼
┌────────────────────────────────────────┐
│ Return ApiError Response               │
│ {                                      │
│   "status": 400,                      │
│   "message": "Error message",         │
│   "timestamp": "2024-01-01T..."       │
│ }                                      │
└────────────────────────────────────────┘
```

---

## 📊 Database Connection Pool

### HikariCP Configuration

```
Default settings (Spring Boot):
├─ Max pool size: 10
├─ Min idle: 10
├─ Connection timeout: 30 seconds
├─ Idle timeout: 10 minutes
└─ Max lifetime: 30 minutes

Configuration in application.yml:
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
      idle-timeout: 300000
      max-lifetime: 1200000
```

---

## 🔄 OTP Rate Limiting

### Rate Limit Implementation

```
┌──────────────────────────────────┐
│ User requests OTP (forgot-pwd)   │
└──────┬───────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────┐
│ RateLimitService.checkRateLimit()        │
└──────┬───────────────────────────────────┘
       │
       ├─→ Redis Key: RATE_LIMIT:{email}
       │
       ├─→ Get current count
       │   count = redis.get(key)
       │
       ├─→ Check if count >= 3
       │   IF YES ──→ Block request (429)
       │   IF NO  ──→ Continue
       │
       ├─→ Increment count
       │   redis.increment(key)
       │
       └─→ Set TTL to 60 seconds
           redis.expire(key, 60)

Example:
Time 00:00 - Request 1 ✓ (count: 1)
Time 00:10 - Request 2 ✓ (count: 2)
Time 00:20 - Request 3 ✓ (count: 3)
Time 00:30 - Request 4 ✗ (blocked: 429)
Time 01:00 - Request 5 ✓ (count reset)
```

---

## 📧 Email Service Architecture

### Email Sending Flow

```
┌──────────────────────────────┐
│ AuthService.register()       │
└──────┬───────────────────────┘
       │
       │ otpService.generateAndSend(email)
       ▼
┌──────────────────────────────┐
│ OtpService.generateAndSend()│
└──────┬───────────────────────┘
       │
       ├─→ Generate OTP
       │   SecureRandom.nextInt(1000000)
       │
       ├─→ Store in Redis
       │   redisOtpService.saveOtp(email, otp)
       │
       └─→ Send Email (Async)
           @Async sendEmail()
           │
           ▼
       ┌──────────────────────────────────┐
       │ OtpService.sendEmail()           │
       └──────┬───────────────────────────┘
              │
              ├─→ Create Context
              │   context.setVariable("otp", "123456")
              │   context.setVariable("ttl", "5")
              │
              ├─→ Process Template
              │   TemplateEngine.process("mail/otp", context)
              │   └─ Replaces [[${otp}]] with actual value
              │
              ├─→ Create MimeMessage
              │   helper.setFrom("noreply@ctu-review.local")
              │   helper.setTo(email)
              │   helper.setSubject("OTP Code")
              │   helper.setText(htmlContent, true)
              │
              └─→ Send via JavaMailSender
                  mailSender.send(mimeMessage)
                  │
                  ├─ Success: Log sent
                  └─ Failure: Log error (don't throw)

Result: Email received by user in 1-5 seconds
```

### Email Template (Thymeleaf)

```html
<html>
  <body>
    <p>Your OTP: [[${otp}]]</p>
    <p>Valid for: [[${ttlMinutes}]] minutes</p>
  </body>
</html>
```

---

## 🚀 Performance Optimization

### Caching Strategy

```
┌─────────────────────────────────────────────────┐
│ User Authentication Request                     │
└──────┬──────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────┐
│ Check Redis Cache (JwtToken)                    │
├─────────────────────────────────────────────────┤
│ Key: JWT:{userId}                               │
│ TTL: 24 hours                                   │
└──────┬──────────────────────────────────────────┘
       │
       ├─ HIT ──→ Return cached user details
       │
       ├─ MISS ──→ Query database
       │          └─ Load User + Authorities
       │             └─ Cache result in Redis
       │
       ▼
       Return User Details
```

### Query Optimization

```
Without optimization:
SELECT * FROM users u
LEFT JOIN faculties f ON u.faculty_id = f.id
└─ N+1 queries problem

With optimization:
@EntityGraph(attributePaths = {"faculty"})
Optional<User> findByEmail(String email);
└─ Single query with JOIN

Result: 50% faster user lookups
```

---

## 🧪 Testing Strategy

### Unit Testing

```
AuthServiceTest:
├─ testRegister_Success()
├─ testRegister_EmailExists()
├─ testLogin_Success()
├─ testLogin_InvalidCredentials()
├─ testVerifyOtp_Success()
├─ testVerifyOtp_Expired()
└─ testResetPassword_Success()

OtpServiceTest:
├─ testGenerateOtp_Format()
├─ testSaveOtp_Redis()
├─ testVerifyOtp_Success()
└─ testVerifyOtp_Invalid()
```

### Integration Testing

```
AuthControllerIntegrationTest:
├─ testRegisterAndVerifyFlow()
├─ testLoginFlow()
├─ testPasswordResetFlow()
└─ testJwtTokenValidation()

Result: End-to-end flow testing
```

---

## 📈 Scalability Considerations

### Horizontal Scaling

```
Load Balancer (Nginx)
│
├─ Instance 1 (8080)
│  ├─ JVM Heap: 2GB
│  └─ Max Connections: 100
│
├─ Instance 2 (8080)
│  ├─ JVM Heap: 2GB
│  └─ Max Connections: 100
│
└─ Instance 3 (8080)
   ├─ JVM Heap: 2GB
   └─ Max Connections: 100

All instances share:
├─ SQL Server (Replicated)
├─ Redis Cluster (Master-Slave)
└─ File Storage (NFS)
```

### Database Optimization

```
Bottleneck 1: User Lookups
└─ Solution: Index on email column

Bottleneck 2: OTP Operations
└─ Solution: Use Redis (in-memory)

Bottleneck 3: Email Sending
└─ Solution: Async processing (@Async)

Bottleneck 4: Auth on every request
└─ Solution: JWT (stateless, no session lookup)
```

---

## 📝 Code Quality Standards

### Code Review Checklist

- [ ] All methods have Javadoc comments
- [ ] No hardcoded values (use constants/config)
- [ ] No null pointer exceptions (use Optional)
- [ ] All exceptions logged with context
- [ ] All DTOs use validation annotations
- [ ] No SQL injection (use parameterized queries)
- [ ] No sensitive data in logs
- [ ] Consistent error messages
- [ ] Unit test coverage > 80%
- [ ] No deprecated methods used

### Naming Conventions

```java
// Classes
public class AuthService { }           // PascalCase
public interface UserRepository { }    // PascalCase

// Methods
public void authenticateUser() { }     // camelCase
public boolean isEmailVerified() { }   // is/has/can prefix for boolean

// Constants
public static final String OTP_KEY = "OTP:{}";  // UPPER_SNAKE_CASE

// Variables
private String userEmail;              // camelCase
private List<User> activeUsers;        // Plural for collections
```

---

## 🔒 Security Checklist

- [ ] All passwords hashed with BCrypt
- [ ] All emails validated before use
- [ ] All OTPs are 6 digits, numeric only
- [ ] Rate limiting on auth endpoints
- [ ] CSRF protection disabled (stateless API)
- [ ] CORS configured for allowed origins only
- [ ] JWT secrets in environment variables
- [ ] SQL injection prevention (parameterized queries)
- [ ] XXS prevention (output encoding)
- [ ] No sensitive data in logs
- [ ] HTTPS enforced in production
- [ ] Secrets not in version control

---

**Document Version**: 1.0.0  
**Last Updated**: May 2026  
**Maintainer**: Backend Architecture Team
