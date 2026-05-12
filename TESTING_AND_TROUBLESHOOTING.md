# CTU Review Platform - Complete Testing & Troubleshooting Guide

## 🧪 Testing Guide

### Part 1: Unit Testing

#### Running All Tests
```bash
# Navigate to backend directory
cd Project/backend

# Run all tests
mvn test

# Run specific test file
mvn test -Dtest=AuthServiceTest

# Run with debug output
mvn test -X

# Generate coverage report
mvn test jacoco:report
# View report: target/site/jacoco/index.html
```

#### Test Examples

```java
// AuthServiceTest.java
@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegisterSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
            "SV001", "John Doe", "john@example.com", 
            "Password@123", 1L
        );

        // Act
        String result = authService.register(request);

        // Assert
        assertThat(result).contains("thành công");
        verify(userRepository).save(any());
        verify(otpService).sendOtp("john@example.com");
    }

    @Test
    void testRegisterFailsWhenEmailExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
            "SV001", "John Doe", "existing@example.com", 
            "Password@123", 1L
        );
        when(userRepository.existsByEmail("existing@example.com"))
            .thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.register(request)
        );
        assertThat(exception.getMessage()).contains("đã tồn tại");
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        User user = User.builder()
            .id(1L)
            .email("john@example.com")
            .passwordHash(passwordEncoder.encode("Password@123"))
            .verified(true)
            .role(Role.STUDENT)
            .build();
        
        when(userRepository.findByEmail("john@example.com"))
            .thenReturn(Optional.of(user));
        
        AuthenticationManager authMgr = mock(AuthenticationManager.class);
        authMgr.authenticate(any());

        // Act
        AuthResponse response = authService.login(
            new LoginRequest("john@example.com", "Password@123")
        );

        // Assert
        assertThat(response.token()).isNotEmpty();
        assertThat(response.role()).isEqualTo(Role.STUDENT);
    }
}
```

---

### Part 2: Integration Testing

#### Running Integration Tests
```bash
# Run integration tests only
mvn verify -Dgroups=integration

# Run full test suite
mvn verify

# Run with live database
mvn verify -Dspring.profiles.active=test
```

#### Integration Test Workflow
```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StringRedisTemplate redis;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
        redis.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void testCompleteRegistrationFlow() throws Exception {
        // Step 1: Register
        mockMvc.perform(post("/api/auth/register")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "studentCode": "SV001",
                    "fullName": "Test User",
                    "email": "test@example.com",
                    "password": "Password@123",
                    "facultyId": 1
                }
            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(containsString("thành công")));

        // Step 2: Get OTP from Redis
        String otp = redis.opsForValue().get("otp:test@example.com");
        assertThat(otp).matches("\\d{6}");

        // Step 3: Verify email
        mockMvc.perform(post("/api/auth/verify")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "email": "test@example.com",
                    "otp": "%s"
                }
            """.formatted(otp)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.verified").value(true));

        // Step 4: Verify user is now verified in DB
        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(user.isVerified()).isTrue();

        // Step 5: Verify OTP is deleted from Redis
        String deletedOtp = redis.opsForValue().get("otp:test@example.com");
        assertThat(deletedOtp).isNull();
    }

    @Test
    void testLoginFlow() throws Exception {
        // Pre-populate database with verified user
        User user = User.builder()
            .studentCode("SV002")
            .fullName("Test User")
            .email("test2@example.com")
            .passwordHash(new BCryptPasswordEncoder().encode("Password@123"))
            .verified(true)
            .role(Role.STUDENT)
            .build();
        userRepository.save(user);

        // Perform login
        MvcResult result = mockMvc.perform(post("/api/auth/login")
            .contentType(APPLICATION_JSON)
            .content("""
                {
                    "email": "test2@example.com",
                    "password": "Password@123"
                }
            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

        // Extract token
        String responseBody = result.getResponse().getContentAsString();
        String token = JsonPath.read(responseBody, "$.token");
        assertThat(token).isNotEmpty();
    }
}
```

#### OTP Endpoint Integration Test (Kafka mocked)
```java
@SpringBootTest
@AutoConfigureMockMvc
class OtpControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redis;

    @MockBean
    private OtpKafkaProducer otpKafkaProducer;

    @Test
    void sendOtpStoresCodeInRedis() throws Exception {
        mockMvc.perform(post("/api/auth/send-otp")
            .contentType(APPLICATION_JSON)
            .content("{\"email\":\"otp-test@example.com\"}"))
            .andExpect(status().isAccepted());

        String otp = redis.opsForValue().get("otp:otp-test@example.com");
        assertThat(otp).matches("\\d{6}");
    }
}
```

Note: Redis must be running for OTP integration tests.

---

### Part 3: Manual API Testing with cURL

#### Test Suite Script

Create `test-api.sh`:

```bash
#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api"

# Helper function
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local expected_status=$4
    
    echo -e "${YELLOW}Testing: $method $endpoint${NC}"
    
    response=$(curl -s -w "\n%{http_code}" -X $method \
        -H "Content-Type: application/json" \
        -d "$data" \
        "$BASE_URL$endpoint")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n-1)
    
    if [ "$http_code" == "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS (Status: $http_code)${NC}"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    else
        echo -e "${RED}✗ FAIL (Expected: $expected_status, Got: $http_code)${NC}"
        echo "$body" | jq . 2>/dev/null || echo "$body"
    fi
    echo ""
}

echo "==================================="
echo "CTU Review Platform - API Test Suite"
echo "==================================="
echo ""

# Test 1: Register
echo -e "${YELLOW}[1/7] Testing User Registration${NC}"
test_endpoint "POST" "/auth/register" \
    '{
        "studentCode": "SV99999",
        "fullName": "Test User",
        "email": "testuser@example.com",
        "password": "TestPass@123",
        "facultyId": 1
    }' \
    "201"

# Test 2: Get OTP from Redis
echo -e "${YELLOW}[2/7] Getting OTP from Redis${NC}"
OTP=$(redis-cli GET "otp:testuser@example.com")
if [ -z "$OTP" ]; then
    echo -e "${RED}✗ FAIL: No OTP found in Redis${NC}"
    echo "Make sure Redis is running: redis-cli ping"
    exit 1
else
    echo -e "${GREEN}✓ PASS: OTP retrieved: $OTP${NC}"
fi
echo ""

# Test 3: Verify Email
echo -e "${YELLOW}[3/7] Testing Email Verification${NC}"
VERIFY_RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d '{
        "email": "testuser@example.com",
        "otp": "'$OTP'"
    }' \
    "$BASE_URL/auth/verify")

TOKEN=$(echo $VERIFY_RESPONSE | jq -r '.token' 2>/dev/null)
if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo -e "${RED}✗ FAIL: No token in response${NC}"
    echo "$VERIFY_RESPONSE"
    exit 1
else
    echo -e "${GREEN}✓ PASS: Token received${NC}"
    echo "Token: ${TOKEN:0:50}..."
fi
echo ""

# Test 4: Use Protected Endpoint
echo -e "${YELLOW}[4/7] Testing Protected Endpoint${NC}"
curl -s -X GET \
    -H "Authorization: Bearer $TOKEN" \
    "$BASE_URL/profile" | jq .
echo ""

# Test 5: Login
echo -e "${YELLOW}[5/7] Testing Login${NC}"
test_endpoint "POST" "/auth/login" \
    '{
        "email": "testuser@example.com",
        "password": "TestPass@123"
    }' \
    "200"

# Test 6: Forgot Password
echo -e "${YELLOW}[6/7] Testing Forgot Password${NC}"
test_endpoint "POST" "/auth/forgot-password" \
    '{
        "email": "testuser@example.com"
    }' \
    "200"

# Test 7: Reset Password
echo -e "${YELLOW}[7/7] Testing Password Reset${NC}"
NEW_OTP=$(redis-cli GET "otp:testuser@example.com")
if [ -z "$NEW_OTP" ]; then
    echo -e "${RED}✗ FAIL: No OTP found for password reset${NC}"
else
    test_endpoint "POST" "/auth/reset-password" \
        '{
            "email": "testuser@example.com",
            "otp": "'$NEW_OTP'",
            "newPassword": "NewPass@456",
            "confirmPassword": "NewPass@456"
        }' \
        "200"
fi

echo "==================================="
echo "Test Suite Complete"
echo "==================================="
```

Run the test suite:
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## 🔧 Troubleshooting Guide

### Issue 1: Application Won't Start

**Error**: `Error starting ApplicationContext`

**Solution**:
```bash
# Check Java version
java -version
# Should be 21 or higher

# Check if port 8080 is in use
netstat -an | grep 8080
# If in use, kill process:
lsof -i :8080
kill -9 <PID>

# Build fresh
mvn clean install

# Run with debug
mvn spring-boot:run -X
```

---

### Issue 2: Database Connection Failed

**Error**: `Cannot get a connection, pool error Timeout waiting for idle object`

**Solution**:
```bash
# Verify SQL Server is running
# Windows: Check SQL Server Configuration Manager
# Or from command line:
sqlcmd -S localhost -U sa -P YourPassword -Q "SELECT @@VERSION"

# Check connection string in application.yml
# Expected format:
# jdbc:sqlserver://localhost:1433;databaseName=ctu_review;encrypt=true;trustServerCertificate=true

# Create database if missing
sqlcmd -S localhost -U sa -P YourPassword -Q "CREATE DATABASE ctu_review"

# Test with database client
# Option 1: SQL Server Management Studio (SSMS)
# Option 2: DBeaver
# Option 3: sqlcmd command line

# Verify firewall allows port 1433
netstat -an | grep 1433
```

**Check logs**:
```bash
grep -i "sqlserver\|datasource\|connection" logs/application.log
```

---

### Issue 3: Redis Connection Failed

**Error**: `Cannot get Jedis connection; nested exception is redis.clients.jedis.exceptions.JedisConnectionException`

**Solution**:
```bash
# Check if Redis is running
redis-cli ping
# Expected: PONG

# If Redis not running, start it:
# Option 1: Docker (recommended)
docker run -d -p 6379:6379 redis:7-alpine
# OR resume existing container
docker start ctu-redis

# Option 2: Local installation
redis-server

# Check Redis connection parameters
# Expected in application.yml:
# spring:
#   redis:
#     host: localhost
#     port: 6379

# Verify firewall allows port 6379
netstat -an | grep 6379

# Test Redis from command line
redis-cli
> ping
> SET test "hello"
> GET test
> DEL test
> exit
```

---

### Issue 4: OTP Emails Not Sending

**Error**: `Failed to send OTP email to: user@example.com`

**Solution**:

#### Step 1: Verify Email Configuration
```bash
# Check application.yml
cat backend/src/main/resources/application.yml | grep -A 5 "spring.mail"

# Expected:
# spring:
#   mail:
#     host: smtp.gmail.com
#     port: 587
#     username: your-email@gmail.com
#     password: your-app-password
#     properties:
#       mail.smtp.auth: true
#       mail.smtp.starttls.enable: true
```

#### Step 2: Test Email Credentials (Gmail)
```bash
# For Gmail, use app-specific password:
# 1. Enable 2-Factor Authentication
# 2. Go to: https://myaccount.google.com/apppasswords
# 3. Select Mail + Windows Computer
# 4. Copy the generated 16-character password
# 5. Use in application.yml:
MAIL_PASSWORD=your-16-char-app-password

# Alternative: Use environment variables
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

#### Step 3: Check Logs
```bash
# Look for email-related logs
grep -i "mail\|email\|smtp\|otp" logs/application.log

# Expected success:
# "Sending OTP email to: user@example.com"
# "OTP email sent successfully to: user@example.com"

# Check for errors:
grep -i "failed.*mail\|exception.*mail" logs/application.log
```

#### Step 4: Test SMTP Connection
```bash
# From Linux/Mac:
telnet smtp.gmail.com 587

# From Windows (if telnet not available):
# Use online tool: https://www.mxtoolbox.com/diagnostic.html
# OR use OpenSSL:
openssl s_client -connect smtp.gmail.com:587 -starttls smtp
```

#### Step 5: Manual Email Test
```bash
# Create a simple Java program to test email:
java -cp ".:lib/*" com.example.MailTester

// MailTester.java
import org.springframework.mail.javamail.JavaMailSender;
public class MailTester {
    public static void main(String[] args) throws Exception {
        JavaMailSender sender = new JavaMailSender();
        // Send test email
    }
}
```

---

### Issue 5: OTP Invalid or Expired

**Error**: `OTP đã hết hạn` or `OTP không hợp lệ`

**Solution**:

#### Check OTP in Redis
```bash
# Connect to Redis CLI
redis-cli

# Check OTP value
> GET "otp:user@example.com"
# Output: 123456 or (nil)

# Check if Redis key exists
> EXISTS "otp:user@example.com"
# Output: 1 (exists) or 0 (not exists)

# Check remaining TTL
> TTL "otp:user@example.com"
# Output: remaining seconds (-1 = no expiry, -2 = doesn't exist)

# View all OTP keys
> KEYS "otp:*"
# Output: Lists all pending OTPs

# Delete specific OTP (for testing)
> DEL "otp:user@example.com"

# Monitor Redis in real-time
> MONITOR
# Then request OTP in another terminal to see operations
```

#### Check OTP TTL Configuration
```bash
# In application.yml:
app:
  otp:
    ttl-minutes: 5  # OTP valid for 5 minutes

# OTP is generated every time you request
# If you request multiple times, previous OTP is overwritten
```

#### Regenerate OTP
```bash
# Request new OTP through API
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'

# Get new OTP from Redis
redis-cli GET "otp:user@example.com"

# Use the new OTP within 5 minutes
```

---

### Issue 6: JWT Token Invalid or Expired

**Error**: `401 Unauthorized` or `Invalid JWT`

**Solution**:

#### Check Token Expiration
```bash
# Decode JWT (online tool):
# https://jwt.io/

# Paste your token and check:
# 1. Header - should show: {"alg":"HS256","typ":"JWT"}
# 2. Payload - should show:
#    {
#      "sub": "user@example.com",
#      "uid": 1,
#      "role": "STUDENT",
#      "iat": 1635342000,
#      "exp": 1635428400
#    }
# 3. Signature - should show "Signature Verified"

# Or use command line:
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." | \
  cut -d'.' -f2 | \
  base64 -d | \
  jq .
```

#### Check Token in Request
```bash
# Verify Authorization header format
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Correct format:
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" http://localhost:8080/api/profile

# Wrong format (missing "Bearer"):
curl -H "Authorization: YOUR_TOKEN_HERE" http://localhost:8080/api/profile  ✗

# Using cookie instead:
curl -b "AUTH_TOKEN=YOUR_TOKEN_HERE" http://localhost:8080/api/profile
```

#### Check JWT Secret
```bash
# JWT_SECRET must match between token generation and validation
# In application.yml:
app:
  jwt:
    secret: your-secret-key-at-least-32-characters-long-for-security
    expiration-minutes: 1440  # 24 hours

# If JWT_SECRET changes, all existing tokens become invalid
```

#### Refresh Token (Re-login)
```bash
# Tokens don't have refresh mechanism in this implementation
# To get new token:
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password@123"
  }'
```

---

### Issue 7: Rate Limit - Too Many OTP Requests

**Error**: `429 Too Many Requests`

**Solution**:

#### Check Rate Limit Configuration
```bash
# In application.yml:
app:
  otp:
    rate-limit-seconds: 60  # Window: 60 seconds
    # Max 3 requests per 60-second window

# This means:
Request 1 at 00:00 ✓
Request 2 at 00:10 ✓
Request 3 at 00:20 ✓
Request 4 at 00:30 ✗ (429 - blocked)
Request 5 at 01:00 ✓ (window reset)
```

#### Check Rate Limit Counter
```bash
# View rate limit in Redis
redis-cli GET "RATE_LIMIT:user@example.com"
# Output: count (1, 2, 3, etc.)

# Check TTL
redis-cli TTL "RATE_LIMIT:user@example.com"
# Output: remaining seconds

# Reset rate limit (for testing)
redis-cli DEL "RATE_LIMIT:user@example.com"
```

#### Wait Before Retrying
```bash
# After 3 requests in 60 seconds, wait 60 seconds
# Then try again

# Or modify configuration (not recommended for production):
# app.otp.rate-limit-seconds: 10  # Reduce window
```

---

### Issue 8: CORS Error

**Error**: `Access to XMLHttpRequest blocked by CORS policy`

**Solution**:

#### Check CORS Configuration
```bash
# In application.yml:
app:
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:5173

# Verify your frontend URL is in the list
# If not, add it:
app:
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:5173
      - http://yourfrontendurl.com
```

#### Verify CORS Response Headers
```bash
# Check response headers
curl -i -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST"

# Look for:
# Access-Control-Allow-Origin: http://localhost:3000
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE
# Access-Control-Allow-Headers: *
```

#### Test CORS Locally
```bash
# Temporarily disable CORS for testing
# In SecurityConfig.java:
.cors(cors -> cors.disable())  // ⚠️ Only for testing!

# Note: Always enable CORS in production
```

---

### Issue 9: User Not Found After Registration

**Error**: `400 Bad Request - User not found`

**Solution**:

#### Check Database
```sql
-- Connect to SQL Server
USE ctu_review;

-- Check if user exists
SELECT * FROM users WHERE email = 'user@example.com';

-- Check if pending registration exists
SELECT * FROM pending_registrations WHERE email = 'user@example.com';

-- Check user count
SELECT COUNT(*) FROM users;

-- Check with LIKE (case-insensitive)
SELECT * FROM users WHERE email LIKE '%user@example%';
```

#### Check User Verification Status
```sql
-- Check if user is verified
SELECT email, is_verified, role FROM users WHERE email = 'user@example.com';

-- Update verification status (if needed)
UPDATE users SET is_verified = 1 WHERE email = 'user@example.com';
```

#### Check Logs
```bash
# Look for registration logs
grep -i "registration\|user.*saved\|duplicate" logs/application.log

# Check for transaction rollback
grep -i "rollback\|transaction" logs/application.log

# Enable SQL logging
# In application.yml:
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        generate_statistics: true
```

---

### Issue 10: Password Reset Not Working

**Error**: OTP verified but password not reset

**Solution**:

#### Verify OTP Consumption
```bash
# Check if OTP was consumed (deleted from Redis)
redis-cli GET "otp:user@example.com"
# Should return (nil) after successful verification

# If OTP still exists, it wasn't consumed
# This might indicate verification failed
```

#### Check Password Hash
```sql
-- Verify new password was saved
SELECT email, password_hash FROM users WHERE email = 'user@example.com';

-- Password hash should be ~60 characters (BCrypt)
-- If shorter, it wasn't hashed properly

-- Check password update timestamp
SELECT email, updated_at FROM users WHERE email = 'user@example.com';
```

#### Test Password Reset Flow
```bash
# Complete flow
1. Request password reset
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'

2. Get OTP
OTP=$(redis-cli GET "otp:user@example.com")
echo "OTP: $OTP"

3. Reset password
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "otp": "'$OTP'",
    "newPassword": "NewPass@456",
    "confirmPassword": "NewPass@456"
  }'

4. Verify OTP is consumed
redis-cli GET "otp:user@example.com"
# Should be (nil)

5. Try login with new password
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "NewPass@456"
  }'
```

---

## 📊 Health Check Commands

### Health Check Script
```bash
#!/bin/bash

echo "=== CTU Review Platform - Health Check ==="
echo ""

# 1. Check Application
echo "1. Application Health:"
curl -s http://localhost:8080/actuator/health | jq .
echo ""

# 2. Check Database
echo "2. Database Status:"
curl -s http://localhost:8080/actuator/db | jq .
echo ""

# 3. Check Redis
echo "3. Redis Connection:"
redis-cli ping
echo ""

# 4. Check Application Metrics
echo "4. Application Metrics:"
curl -s http://localhost:8080/actuator/metrics | jq '.names[] | select(. | contains("http"))' | head -5
echo ""

# 5. Check Logs
echo "5. Recent Errors:"
tail -20 logs/application.log | grep -i "error\|exception" || echo "No errors found"
echo ""

echo "=== Health Check Complete ==="
```

---

## 🚀 Performance Testing

### Load Testing with Apache JMeter

```bash
# Install JMeter
# https://jmeter.apache.org/download_jmeter.html

# Create test plan:
# 1. Thread Group (100 threads, 10 second ramp-up)
# 2. HTTP Sampler (POST /api/auth/login)
# 3. View Results Tree
# 4. Aggregate Report

# Run from command line
jmeter -n -t test-plan.jmx -l results.jtl -j jmeter.log

# View results
jmeter -g results.jtl -o result-report/
```

### Stress Testing
```bash
# Using Apache Bench
ab -n 1000 -c 100 http://localhost:8080/api/auth/login

# Using wrk
wrk -t12 -c400 -d30s \
  --script post.lua \
  http://localhost:8080/api/auth/login

# Using Apache JMeter
# See section above
```

---

## 📚 Additional Resources

- [Spring Boot Troubleshooting](https://spring.io/guides/gs/spring-boot-docker/)
- [SQL Server Connection Strings](https://www.connectionstrings.com/sql-server/)
- [Redis Commands Reference](https://redis.io/commands/)
- [JWT.io Debugger](https://jwt.io/)
- [cURL Documentation](https://curl.se/docs/manual.html)

---

**Last Updated**: May 2026  
**Version**: 1.0.0  
**Maintained By**: Support Team
