# Đặc Tả Kỹ Thuật Backend (Backend Specification)

Tài liệu này cung cấp đặc tả chi tiết về hệ thống Backend của **CTU Review Platform**, được xây dựng trên nền tảng **Java Spring Boot**.

---

## 1. Kiến Trúc Tổng Quan (Architecture)
Hệ thống tuân thủ mô hình **Clean Architecture / Layered Architecture**:
- **Controllers**: Nằm trong package `com.example.ctu.controller`. Chịu trách nhiệm tiếp nhận HTTP Request, gọi Services và trả về HTTP Response.
- **Services**: Nằm trong package `com.example.ctu.service`. Chứa logic nghiệp vụ (Business Rules).
- **Repositories**: Kế thừa `JpaRepository` nằm trong `com.example.ctu.repository`. Giao tiếp với Cơ sở dữ liệu (Database).
- **Entities**: Mapping với cấu trúc bảng trong CSDL.
- **DTOs**: (Data Transfer Objects) Nằm trong `com.example.ctu.dto`. Phục vụ việc truyền tải dữ liệu giữa Frontend và Backend.
- **Security**: Quản lý Authentication (JWT) và Authorization (Role-based).
- **OTP/Mail**: Quản lý gửi nhận OTP qua Email và Kafka.

---

## 2. Xác Thực & Phân Quyền (Authentication & Authorization)

### 2.1 Flow Xác Thực (Auth Flow)
- Cơ chế: **JWT (JSON Web Token)** kết hợp với **Refresh Token**.
- Token được gửi trong Header: `Authorization: Bearer <access_token>`.
- Vòng đời:
  1. Người dùng đăng nhập thành công sẽ nhận được `token` (Access Token) và `refreshToken`.
  2. Khi Access Token hết hạn (Lỗi HTTP 401), Client gọi API `/auth/refresh-token` với `refreshToken` để lấy token mới mà không cần đăng nhập lại.

### 2.2 Phân Quyền (Roles)
Hệ thống bao gồm 3 vai trò (Roles) chính:
- **`STUDENT`**: Sinh viên. Có quyền đăng nhập, cập nhật profile, viết đánh giá (Review), và báo cáo (Report). Yêu cầu tài khoản phải ở trạng thái `is_verified = true` và `is_locked = false`.
- **`ADMIN`**: Quản trị viên. Có quyền xem danh sách đánh giá/người dùng, duyệt/từ chối đánh giá, khóa/mở khóa người dùng, xuất dữ liệu (CSV).
- **`SUPER_ADMIN`**: Quản trị viên cấp cao. Có tất cả quyền của ADMIN, thêm các đặc quyền: Thêm/Sửa/Xóa Khoa (Faculty), Môn học (Subject), Giảng viên (Lecturer), Từ khóa độc hại (Toxic Keywords), Xóa người dùng, Import giảng viên từ CTU.

---

## 3. Cấu Trúc Dữ Liệu (Entities / Database Models)

### 3.1 Users (Người Dùng)
- **id**: Long (PK)
- **studentCode**: String (Unique) - Mã số sinh viên
- **fullName**: String - Họ và tên
- **email**: String (Unique)
- **passwordHash**: String - Mật khẩu mã hóa
- **faculty_id**: Long (FK) - Thuộc Khoa nào
- **role**: Enum (`STUDENT`, `ADMIN`, `SUPER_ADMIN`)
- **verified**: Boolean - Đã xác thực OTP chưa
- **locked**: Boolean - Có bị khóa không
- **createdAt**: Instant

### 3.2 Faculties (Khoa)
- **id**: Long (PK)
- **name**: String
- **code**: String (Unique)

### 3.3 Subjects (Môn Học)
- **id**: Long (PK)
- **name**: String
- **code**: String (Unique)
- **faculty_id**: Long (FK)

### 3.4 Lecturers (Giảng Viên)
- **id**: Long (PK)
- **lecturerCode**: String (Unique) - Mã giảng viên
- **fullName**: String - Họ tên giảng viên
- **faculty_id**: Long (FK)
- **subject_id**: Long (FK - nullable)
- **status**: Enum (`ACTIVE`, `HIDDEN`)

### 3.5 Reviews (Đánh Giá)
- **id**: Long (PK)
- **lecturer_id**: Long (FK)
- **anonymousHash**: String - Mã hóa ẩn danh người đánh giá
- **ratingClarity**: Integer (1-5) - Rõ ràng
- **ratingFairness**: Integer (1-5) - Công bằng
- **ratingPressure**: Integer (1-5) - Áp lực
- **ratingWorkload**: Integer (1-5) - Khối lượng công việc
- **ratingSupport**: Integer (1-5) - Hỗ trợ
- **comment**: String (NVARCHAR) - Nhận xét chi tiết
- **semester**: String - Học kỳ
- **academicYear**: String - Năm học
- **approved**: Boolean - Đã được duyệt chưa
- **createdAt**: Instant

---

## 4. Chi Tiết API (Format Nhận Request & Trả Response)

Định dạng mặc định của Backend:
- **Content-Type**: `application/json`
- Lỗi trả về có dạng JSON: `{ "message": "Nội dung lỗi", "status": 400, "timestamp": "...", "path": "..." }`

### 4.1 Nhóm API Xác Thực (Auth) - Public
**1. Đăng ký tài khoản mới**
- `POST /auth/register`
- **Request Body:**
  ```json
  {
    "studentCode": "B2012345",
    "fullName": "Nguyễn Văn A",
    "email": "nva@student.ctu.edu.vn",
    "password": "password123",
    "confirmPassword": "password123",
    "facultyId": 1
  }
  ```
- **Response (200 OK):** String (`"Đăng ký thành công, vui lòng kiểm tra email để lấy mã OTP."`)

**2. Xác thực OTP**
- `POST /auth/verify`
- **Request Body:**
  ```json
  {
    "email": "nva@student.ctu.edu.vn",
    "otp": "123456"
  }
  ```
- **Response (200 OK):** `AuthResponse`
  ```json
  {
    "token": "eyJhbGciOi...",
    "refreshToken": "e3b0c442...",
    "role": "STUDENT",
    "verified": true,
    "fullName": "Nguyễn Văn A",
    "facultyName": "Công nghệ thông tin"
  }
  ```

**3. Đăng nhập**
- `POST /auth/login`
- **Request Body:**
  ```json
  {
    "email": "nva@student.ctu.edu.vn",
    "password": "password123"
  }
  ```
- **Response (200 OK):** `AuthResponse` (Cấu trúc như `/auth/verify`)

**4. Yêu cầu đổi mật khẩu (Quên mật khẩu)**
- `POST /auth/forgot-password`
- **Request Body:**
  ```json
  {
    "email": "nva@student.ctu.edu.vn"
  }
  ```
- **Response (200 OK):** String (`"OTP đã được gửi..."`)

**5. Đặt lại mật khẩu**
- `POST /auth/reset-password`
- **Request Body:**
  ```json
  {
    "email": "nva@student.ctu.edu.vn",
    "otp": "123456",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
  }
  ```
- **Response (200 OK):** String (`"Cập nhật mật khẩu thành công"`)

**6. Refresh Token**
- `POST /auth/refresh-token`
- **Request Body:**
  ```json
  {
    "refreshToken": "e3b0c442..."
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "token": "eyJhbGciOi...",
    "refreshToken": "new-refresh-token..."
  }
  ```

### 4.2 Nhóm API Sinh Viên (Students) - Cần Token
**1. Lấy thông tin cá nhân**
- `GET /students/me`
- **Response (200 OK):**
  ```json
  {
    "studentCode": "B2012345",
    "fullName": "Nguyễn Văn A",
    "email": "nva@student.ctu.edu.vn",
    "facultyName": "Công nghệ thông tin",
    "role": "STUDENT",
    "verified": true
  }
  ```

**2. Cập nhật thông tin**
- `PUT /students/me`
- **Request Body:**
  ```json
  {
    "fullName": "Nguyễn Văn B",
    "facultyId": 2
  }
  ```
- **Response (200 OK):** (Trả về cục dữ liệu mới giống `GET /students/me`)

**3. Đổi mật khẩu trong lúc đang đăng nhập**
- `POST /students/me/change-password`
- **Request Body:**
  ```json
  {
    "currentPassword": "oldpassword123",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
  }
  ```

### 4.3 Nhóm API Đánh Giá (Reviews) - Quyền STUDENT
**1. Gửi Đánh Giá Mới**
- `POST /reviews`
- **Request Body:**
  ```json
  {
    "lecturerId": 1,
    "ratingClarity": 4,
    "ratingFairness": 5,
    "ratingPressure": 3,
    "ratingWorkload": 4,
    "ratingSupport": 5,
    "comment": "Thầy giảng rất hay và nhiệt tình...",
    "semester": "1",
    "academicYear": "2023-2024"
  }
  ```
- **Response (200 OK):** Đối tượng Review vừa tạo.

**2. Báo cáo đánh giá vi phạm**
- `POST /reports`
- **Request Body:**
  ```json
  {
    "reviewId": 10,
    "reason": "Sử dụng ngôn từ không phù hợp"
  }
  ```

### 4.4 Nhóm API Dữ Liệu Chung (Public)
**1. Lấy toàn bộ Khoa**
- `GET /metadata/faculties`
- **Response (200 OK):**
  ```json
  [
    { "id": 1, "name": "Công nghệ thông tin", "code": "DI" }
  ]
  ```

**2. Lấy toàn bộ Môn (Hỗ trợ query `?facultyCode=DI`)**
- `GET /metadata/subjects`
- **Response (200 OK):**
  ```json
  [
    { "id": 1, "name": "Cấu trúc dữ liệu", "code": "CT175", "faculty": { "id": 1, "name": "CNTT", "code": "DI" } }
  ]
  ```

**3. Danh sách Giảng viên (Tóm tắt)**
- `GET /lecturers` (Hỗ trợ `?facultyCode=...&subjectCode=...`)
- **Response (200 OK):** (List)
  ```json
  [
    {
      "id": 1,
      "lecturerCode": "00123",
      "fullName": "Nguyễn Văn Thầy",
      "facultyName": "Công nghệ thông tin",
      "subjectName": "Cấu trúc dữ liệu",
      "status": "ACTIVE",
      "averageRating": 4.5,
      "reviewCount": 20
    }
  ]
  ```

**4. Danh sách Giảng viên (Phân trang)**
- `GET /lecturers/page?page=0&size=12`
- **Response (200 OK):** Page object chứa mảng `content` tương tự API trên.
  ```json
  {
    "content": [ ...danh sách... ],
    "page": 0,
    "size": 12,
    "totalElements": 100,
    "totalPages": 9,
    "first": true,
    "last": false
  }
  ```

**5. Chi tiết Giảng viên**
- `GET /lecturers/{id}`
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "lecturerCode": "00123",
    "fullName": "Nguyễn Văn Thầy",
    "facultyName": "Công nghệ thông tin",
    "subjectName": "Cấu trúc dữ liệu",
    "status": "ACTIVE",
    "averageRating": 4.5,
    "averageClarity": 4.6,
    "averageFairness": 4.5,
    "averagePressure": 3.0,
    "averageWorkload": 4.0,
    "averageSupport": 4.8,
    "reviewCount": 20,
    "distribution": [
      { "score": 5, "count": 10 },
      { "score": 4, "count": 8 }
    ],
    "semesterComparisons": [
      { "semester": "1", "academicYear": "2023-2024", "averageRating": 4.2, "reviewCount": 5 }
    ],
    "latestReviews": [
      {
        "id": 50,
        "averageRating": 4.6,
        "ratingClarity": 5,
        "ratingFairness": 5,
        "ratingPressure": 4,
        "ratingWorkload": 4,
        "ratingSupport": 5,
        "comment": "Tuyệt vời",
        "semester": "1",
        "academicYear": "2023-2024",
        "createdAt": "2023-11-20T10:00:00Z"
      }
    ]
  }
  ```

### 4.5 Nhóm API Quản Trị (Admin & Super Admin)
*(Tất cả API Quản trị đều nhận và trả về dữ liệu tương ứng với DTO. Hầu hết các request PATCH/POST/DELETE chỉ cần `{ "id" }` trên URL và trả về Entity mới cập nhật hoặc 204 No Content).*

Ví dụ: **Cấp quyền (Super Admin)**
- `PATCH /admin/users/{id}/role`
- **Request Body:** `{ "role": "ADMIN" }`

Ví dụ: **Lấy thống kê**
- `GET /admin/statistics`
- **Response (200 OK):**
  ```json
  {
    "totalStudents": 1500,
    "totalFaculties": 12,
    "totalSubjects": 100,
    "totalLecturers": 300,
    "totalReviews": 5000,
    "pendingReviews": 15,
    "facultyStatistics": [
      { "facultyId": 1, "facultyName": "CNTT", "lecturerCount": 50, "reviewCount": 1000, "averageRating": 4.2, "pendingReviewCount": 5 }
    ],
    "topLecturers": [ ... ],
    "topReportedLecturers": [ ... ]
  }
  ```

---

## 5. Quy Tắc Nghiệp Vụ (Business & Validation Rules)
1. **Mật khẩu**: Yêu cầu tối thiểu 6 ký tự. Khi gửi request phải có field `confirmPassword` trùng khớp với `password` hoặc `newPassword`.
2. **Đánh Giá (Review)**:
   - Comment phải từ `10` đến `1000` ký tự.
   - Các điểm rating (Clarity, Fairness, Pressure, Workload, Support) bắt buộc phải là số nguyên từ `1` đến `5`.
3. **Từ khóa độc hại (Toxic Filter)**: Các comment nếu chứa từ khóa nằm trong bảng `toxic_keywords` có thể bị chặn hoặc đánh dấu cần duyệt khắt khe hơn.
4. **Pagination (Phân trang)**:
   - Các API có phân trang sử dụng query parameters `page` (bắt đầu từ 0) và `size`.
   - Max size cho API danh sách giảng viên là `48`.
5. **Trạng Thái Sinh Viên**: Nếu `locked = true`, hệ thống từ chối cấp token khi login. Nếu `verified = false`, sinh viên có thể không truy cập được các chức năng cốt lõi.
