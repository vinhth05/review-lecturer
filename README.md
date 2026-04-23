# CTU Lecturer Review Platform

Nền tảng đánh giá giảng viên ẩn danh cho sinh viên CTU, có kiểm duyệt nội dung và dashboard thống kê cho admin.

## 1. Mục tiêu đã đáp ứng

- Chỉ cho phép email domain `@student.ctu.edu.vn` đăng ký.
- Bắt buộc OTP verify trước khi đăng nhập.
- Không lưu `student_id` trong `reviews`; dùng `anonymous_hash = SHA-256(student_code + SECRET_KEY)`.
- Rate limit `5 review/ngày` trên mỗi sinh viên ẩn danh.
- Review gửi lên ở trạng thái chờ duyệt (`is_approved=false`).
- Admin duyệt trước khi hiển thị.
- Có report review vi phạm.
- Có dashboard admin theo khoa và top giảng viên.
- Thiết kế mở rộng cho đa trường trong giai đoạn startup (xem mục Roadmap).

## 2. Tech Stack

- Backend: Spring Boot 3 (Java 17), Spring Security, JPA, Flyway, Swagger, WebSocket
- Database: SQL Server
- Frontend: React + TypeScript + Vite + Zustand + Recharts
- Deploy: Docker + Docker Compose

## 3. Cấu trúc dự án

```text
backend/     # Spring Boot API
frontend/    # React application
database/    # SQL script tổng hợp
docs/        # ERD và tài liệu kiến trúc
docker-compose.yml
```

## 4. API chính

### Auth
- `POST /auth/register`
- `POST /auth/verify`
- `POST /auth/login`

### Student
- `GET /lecturers`
- `GET /lecturers/{id}`
- `POST /reviews`
- `POST /reports`
- `GET /students/me`

### Admin
- `GET /admin/reviews/pending`
- `PATCH /admin/reviews/{id}/approve`
- `PATCH /admin/lecturers/{id}/hide`
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

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- MailHog UI (OTP inbox): `http://localhost:8025`

## 6. Chạy local thủ công

### Chạy cả backend + frontend bằng 1 lệnh (Windows PowerShell)

```powershell
cd d:\He\Project
.\run-all.ps1
```

Nếu PowerShell báo lỗi ExecutionPolicy, dùng file BAT (không cần đổi policy):

```bat
cd /d d:\He\Project
run-all.bat
```

Lệnh trên sẽ mở 2 cửa sổ terminal riêng:
- 1 cửa sổ chạy backend (`mvn spring-boot:run`)
- 1 cửa sổ chạy frontend (`npm run dev`)

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

## 7. Bảo mật và ẩn danh

- Hash ẩn danh sinh viên theo secret key, không truy ngược trực tiếp sang user id.
- Từ khóa độc hại được lọc trước khi nhận review.
- Có cơ chế report cộng đồng.
- Không có API chỉnh sửa review sau khi gửi.

## 8. Seed data

Đã có seed:
- 6 khoa mẫu CTU
- Môn học mẫu theo khoa
- Giảng viên mẫu

Tệp: `backend/src/main/resources/db/migration/V2__seed.sql`

## 9. Roadmap startup (toàn quốc)

- Bổ sung bảng `universities` và cột `university_id` ở `faculties`.
- Tách `tenant` theo trường để đa tenancy.
- Gói Premium: sentiment nâng cao, ranking sâu theo ngành, theo vùng.
- Public API cho đối tác học thuật.
- AI service riêng cho toxic detection và sentiment scoring.
