# CTU Review Platform - Complete API Documentation

## 📌 Quick Reference

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/api/auth/register` | ❌ | Register new user |
| POST | `/api/auth/verify` | ❌ | Verify email OTP |
| POST | `/api/auth/login` | ❌ | Login user |
| POST | `/api/auth/forgot-password` | ❌ | Request password reset |
| POST | `/api/auth/reset-password` | ❌ | Reset password |
| PUT | `/api/auth/profile` | ✅ | Update user profile |
| POST | `/api/auth/change-password` | ✅ | Change password |

---

## 🔐 Authentication Methods

### Method 1: Authorization Header
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" http://localhost:8080/api/endpoint
```

### Method 2: Cookie
```bash
curl -b "AUTH_TOKEN=YOUR_JWT_TOKEN" http://localhost:8080/api/endpoint
```

---

## 📝 Complete API Examples

### 1️⃣ USER REGISTRATION

**Description**: Register a new user account

**Endpoint**: `POST /api/auth/register`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "studentCode": "SV20001",
  "fullName": "Nguyen Van A",
  "email": "nguyenvana@student.ctu.edu.vn",
  "password": "SecurePass@123",
  "facultyId": 1
}
```

**Validation Rules**:
- `studentCode`: 50 chars max, unique
- `fullName`: Required, non-empty
- `email`: Valid email format, unique
- `password`: Min 6 characters recommended
- `facultyId`: Must exist in database

**Success Response** (201):
```json
{
  "message": "Đăng ký thành công. Vui lòng kiểm tra email để xác thực OTP."
}
```

**Error Responses**:
```json
// 400 - Email already exists
{
  "status": 400,
  "message": "Email đã tồn tại"
}

// 400 - Invalid faculty
{
  "status": 400,
  "message": "Khoa không tồn tại"
}

// 400 - Student code exists
{
  "status": 400,
  "message": "Mã số sinh viên đã tồn tại"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "studentCode": "SV20001",
    "fullName": "Nguyen Van A",
    "email": "nguyenvana@student.ctu.edu.vn",
    "password": "SecurePass@123",
    "facultyId": 1
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

**What Happens Behind the Scenes**:
1. ✅ Validate email format
2. ✅ Check email uniqueness in database
3. ✅ Check student code uniqueness
4. ✅ Validate faculty exists
5. ✅ Hash password using BCrypt
6. ✅ Create user record (status: unverified)
7. ✅ Generate 6-digit OTP
8. ✅ Store OTP in Redis (TTL: 5 minutes)
9. ✅ Send email with OTP (async)
10. ✅ Return success message

---

### 2️⃣ VERIFY EMAIL OTP

**Description**: Verify user email with OTP sent via email

**Endpoint**: `POST /api/auth/verify`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "nguyenvana@student.ctu.edu.vn",
  "otp": "123456"
}
```

**Validation Rules**:
- `email`: Must be valid email format
- `otp`: Must be 6 digits, numeric only

**Success Response** (200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJuZ3V5ZW52YW5hQHN0dWRlbnQuY3R1LmVkdS52biIsInVpZCI6MSwiucm9sZSI6IlNUVURFTlQiLCJpYXQiOjE2MzUzNDIwMDAsImV4cCI6MTYzNTQyODQwMH0.signed_signature_here",
  "role": "STUDENT",
  "verified": true,
  "fullName": "Nguyen Van A",
  "facultyName": "Information Technology"
}
```

**Error Responses**:
```json
// 400 - OTP expired
{
  "status": 400,
  "message": "OTP đã hết hạn"
}

// 400 - OTP invalid
{
  "status": 400,
  "message": "OTP không hợp lệ"
}
```

**cURL Example**:
```bash
# Get OTP from Redis first (for testing)
OTP=$(redis-cli GET "OTP:nguyenvana@student.ctu.edu.vn")

# Verify with OTP
curl -X POST http://localhost:8080/api/auth/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyenvana@student.ctu.edu.vn",
    "otp": "'$OTP'"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

**Flow**:
1. ✅ Receive email and OTP from client
2. ✅ Query Redis for stored OTP
3. ✅ Compare OTP values
4. ✅ Delete OTP from Redis (consumed)
5. ✅ Mark user as verified
6. ✅ Generate JWT token
7. ✅ Return token and user info

**Token Usage**:
```bash
# Save token
TOKEN="<token_from_response>"

# Use in subsequent requests
curl -X GET http://localhost:8080/api/profile \
  -H "Authorization: Bearer $TOKEN"
```

---

### 3️⃣ USER LOGIN

**Description**: Login with email and password

**Endpoint**: `POST /api/auth/login`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "nguyenvana@student.ctu.edu.vn",
  "password": "SecurePass@123"
}
```

**Success Response** (200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "STUDENT",
  "verified": true,
  "fullName": "Nguyen Van A",
  "facultyName": "Information Technology"
}
```

**Error Responses**:
```json
// 400 - Invalid credentials
{
  "status": 400,
  "message": "Thông tin đăng nhập không hợp lệ"
}

// 400 - Email not verified
{
  "status": 400,
  "message": "Tài khoản chưa được xác thực OTP"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyenvana@student.ctu.edu.vn",
    "password": "SecurePass@123"
  }' \
  -w "\nHTTP Status: %{http_code}\n" \
  -c cookies.txt
```

**Process**:
1. ✅ Validate email format
2. ✅ Load user from database
3. ✅ Verify password matches hash
4. ✅ Check if email is verified
5. ✅ Generate JWT token
6. ✅ Return token and user details

---

### 4️⃣ FORGOT PASSWORD - REQUEST RESET

**Description**: Request password reset OTP

**Endpoint**: `POST /api/auth/forgot-password`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "nguyenvana@student.ctu.edu.vn"
}
```

**Success Response** (200):
```json
{
  "message": "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi."
}
```

**Security Note**: Returns same message regardless of whether email exists (prevents user enumeration)

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyenvana@student.ctu.edu.vn"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

**Process**:
1. ✅ Query user by email (silently skip if not found)
2. ✅ Generate 6-digit OTP
3. ✅ Store in Redis (TTL: 5 minutes)
4. ✅ Send email with OTP (async)
5. ✅ Return generic success message

---

### 5️⃣ RESET PASSWORD

**Description**: Reset password using OTP

**Endpoint**: `POST /api/auth/reset-password`

**Request Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "nguyenvana@student.ctu.edu.vn",
  "otp": "123456",
  "newPassword": "NewSecurePass@456",
  "confirmPassword": "NewSecurePass@456"
}
```

**Validation Rules**:
- `newPassword`: Min 6 characters
- `confirmPassword`: Must match `newPassword`
- `otp`: Must be 6 digits

**Success Response** (200):
```json
{
  "message": "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại."
}
```

**Error Responses**:
```json
// 400 - Passwords don't match
{
  "status": 400,
  "message": "Mật khẩu xác nhận không khớp"
}

// 400 - Invalid or expired OTP
{
  "status": 400,
  "message": "OTP không hợp lệ"
}

// 400 - Email not found
{
  "status": 400,
  "message": "OTP hoặc email không hợp lệ"
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nguyenvana@student.ctu.edu.vn",
    "otp": "123456",
    "newPassword": "NewSecurePass@456",
    "confirmPassword": "NewSecurePass@456"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

**Process**:
1. ✅ Validate passwords match
2. ✅ Find user by email
3. ✅ Verify OTP against Redis
4. ✅ Delete OTP from Redis
5. ✅ Hash new password
6. ✅ Update user password
7. ✅ Return success message

---

### 6️⃣ UPDATE USER PROFILE (PROTECTED)

**Description**: Update user profile information

**Endpoint**: `PUT /api/auth/profile`

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Request Body**:
```json
{
  "fullName": "Nguyen Van A Updated",
  "facultyId": 2
}
```

**Success Response** (200):
```json
{
  "studentCode": "SV20001",
  "fullName": "Nguyen Van A Updated",
  "email": "nguyenvana@student.ctu.edu.vn",
  "facultyName": "Economics",
  "role": "STUDENT",
  "verified": true
}
```

**Error Responses**:
```json
// 401 - No token provided
{
  "status": 401,
  "message": "Unauthorized"
}

// 404 - User not found (should not happen)
{
  "status": 404,
  "message": "Người dùng không tồn tại"
}
```

**cURL Example**:
```bash
TOKEN="your_jwt_token_here"

curl -X PUT http://localhost:8080/api/auth/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "fullName": "Nguyen Van A Updated",
    "facultyId": 2
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

**Process**:
1. ✅ Extract user from JWT token
2. ✅ Load user from database
3. ✅ Validate faculty exists
4. ✅ Update full name and faculty
5. ✅ Save to database
6. ✅ Return updated profile

---

### 7️⃣ CHANGE PASSWORD (PROTECTED)

**Description**: Change password for authenticated user

**Endpoint**: `POST /api/auth/change-password`

**Request Headers**:
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
```

**Request Body**:
```json
{
  "currentPassword": "SecurePass@123",
  "newPassword": "NewSecurePass@789",
  "confirmPassword": "NewSecurePass@789"
}
```

**Success Response** (200):
```json
{
  "message": "Đổi mật khẩu thành công"
}
```

**Error Responses**:
```json
// 400 - Wrong current password
{
  "status": 400,
  "message": "Mật khẩu hiện tại không đúng"
}

// 400 - Passwords don't match
{
  "status": 400,
  "message": "Mật khẩu xác nhận không khớp"
}

// 400 - New password same as current
{
  "status": 400,
  "message": "Mật khẩu mới phải khác mật khẩu hiện tại"
}
```

**cURL Example**:
```bash
TOKEN="your_jwt_token_here"

curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "currentPassword": "SecurePass@123",
    "newPassword": "NewSecurePass@789",
    "confirmPassword": "NewSecurePass@789"
  }' \
  -w "\nHTTP Status: %{http_code}\n"
```

**Process**:
1. ✅ Extract user from JWT token
2. ✅ Load user from database
3. ✅ Verify current password matches
4. ✅ Validate new password != current
5. ✅ Hash new password
6. ✅ Update password in database
7. ✅ Return success message

---

## 🚀 Complete Testing Workflow

### Step 1: Register
```bash
#!/bin/bash

echo "=== Step 1: Register User ==="
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "studentCode": "SV99999",
    "fullName": "Test User",
    "email": "testuser@student.ctu.edu.vn",
    "password": "TestPass@123",
    "facultyId": 1
  }')

echo "Response: $REGISTER_RESPONSE"
```

### Step 2: Get OTP from Redis
```bash
echo "=== Step 2: Get OTP from Redis ==="
OTP=$(redis-cli GET "OTP:testuser@student.ctu.edu.vn")
echo "OTP: $OTP"
```

### Step 3: Verify Email
```bash
echo "=== Step 3: Verify Email with OTP ==="
VERIFY_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@student.ctu.edu.vn",
    "otp": "'$OTP'"
  }')

echo "Response: $VERIFY_RESPONSE"

# Extract token
TOKEN=$(echo $VERIFY_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token: $TOKEN"
```

### Step 4: Use Protected Endpoint
```bash
echo "=== Step 4: Update Profile (Protected) ==="
curl -s -X PUT http://localhost:8080/api/auth/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "fullName": "Test User Updated",
    "facultyId": 1
  }' | jq .
```

### Step 5: Change Password
```bash
echo "=== Step 5: Change Password ==="
curl -s -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "currentPassword": "TestPass@123",
    "newPassword": "NewTestPass@456",
    "confirmPassword": "NewTestPass@456"
  }' | jq .
```

### Step 6: Logout (Client-side)
```bash
# Logout is client-side - simply discard the token
echo "=== Step 6: Logout ==="
echo "Token discarded. User logged out."
```

---

## 📊 Response Status Codes

| Code | Meaning | Scenario |
|------|---------|----------|
| 200 | OK | Successful GET/POST/PUT |
| 201 | Created | Resource created |
| 400 | Bad Request | Validation error, invalid OTP, expired OTP |
| 401 | Unauthorized | Missing/invalid JWT token |
| 404 | Not Found | Resource not found |
| 500 | Internal Error | Server error |

---

## 🔍 Debugging Tips

### Check JWT Token
```bash
# Decode JWT (without verification - don't use in production)
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." | cut -d'.' -f2 | base64 -d | jq .
```

### Monitor Redis in Real-Time
```bash
redis-cli MONITOR
```

### Check Application Logs
```bash
# If running with Maven
tail -f target/application.log

# If running as JAR
tail -f logs/application.log
```

### Test Email Sending
```bash
# Check if email was sent (look for async log)
grep "OTP email sent" logs/application.log

# Check if email failed
grep "Failed to send OTP" logs/application.log
```

---

## 🛡️ Security Best Practices

1. **Always use HTTPS in production**
2. **Never expose JWT secrets in logs**
3. **Rotate JWT secrets regularly**
4. **Use strong passwords (min 12 characters)**
5. **Implement rate limiting on all endpoints**
6. **Monitor failed login attempts**
7. **Log all authentication events**
8. **Keep dependencies updated**

---

## 📝 Common Errors & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "Email đã tồn tại" | Email registered twice | Use different email |
| "OTP đã hết hạn" | OTP expired (5 min TTL) | Request new OTP |
| "OTP không hợp lệ" | Wrong OTP | Check Redis: `redis-cli GET "OTP:email"` |
| "Tài khoản chưa được xác thực OTP" | Email not verified | Verify email first |
| "Thông tin đăng nhập không hợp lệ" | Wrong password | Check password |
| 401 Unauthorized | Missing JWT | Include Authorization header |
| Redis connection error | Redis not running | Start Redis: `redis-server` |

---

## 📚 Related Documentation

- [JWT Claims Structure](JWT.md)
- [Database Schema](DATABASE.md)
- [Troubleshooting Guide](TROUBLESHOOTING.md)
- [Performance Tuning](PERFORMANCE.md)

---

**Last Updated**: May 2026  
**API Version**: 1.0.0  
**Maintained By**: Backend Team
