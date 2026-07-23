# Sơ đồ Thực thể - Mối quan hệ (Entity-Relationship Diagram - ERD)

Dưới đây là sơ đồ ERD chi tiết cho cơ sở dữ liệu của dự án **CTU Review Platform**, mô tả các bảng, các trường thông tin, kiểu dữ liệu, các khóa (Primary Key - PK, Foreign Key - FK) và mối quan hệ giữa chúng.

```mermaid
erDiagram
    faculties {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR name "Tên khoa"
        NVARCHAR code UK "Mã khoa (Unique)"
        DATETIME2 created_at "Thời gian tạo"
    }

    subjects {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR name "Tên môn học"
        NVARCHAR code UK "Mã môn học (Unique)"
        BIGINT faculty_id FK "Liên kết faculties"
        DATETIME2 created_at "Thời gian tạo"
    }

    users {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR student_code UK "Mã số sinh viên/quản trị (Unique)"
        NVARCHAR full_name "Họ và tên"
        NVARCHAR email UK "Email đăng nhập (Unique)"
        NVARCHAR password_hash "Mật khẩu đã mã hóa"
        BIGINT faculty_id FK "Khoa trực thuộc"
        NVARCHAR role "Vai trò (STUDENT, ADMIN, SUPER_ADMIN)"
        BIT is_verified "Trạng thái xác thực email"
        BIT is_locked "Trạng thái khóa tài khoản"
        DATETIME2 created_at "Thời gian tạo"
    }

    pending_registrations {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR student_code UK "Mã số sinh viên (Unique)"
        NVARCHAR full_name "Họ và tên"
        NVARCHAR email UK "Email đăng ký (Unique)"
        NVARCHAR password_hash "Mật khẩu tạm thời"
        BIGINT faculty_id FK "Khoa chọn đăng ký"
        DATETIME2 created_at "Thời gian yêu cầu"
        DATETIME2 expires_at "Thời gian hết hạn OTP"
    }

    otp_tokens {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR email "Email nhận OTP"
        NVARCHAR token "Mã OTP (6 số)"
        DATETIME2 expires_at "Thời gian hết hạn"
        BIT used "Đã sử dụng hay chưa"
        DATETIME2 created_at "Thời gian tạo"
    }

    lecturers {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR lecturer_code UK "Mã giảng viên (Unique)"
        NVARCHAR full_name "Họ và tên giảng viên"
        BIGINT faculty_id FK "Khoa trực thuộc"
        BIGINT subject_id FK "Môn học phụ trách (Nullable)"
        NVARCHAR status "Trạng thái (ACTIVE, HIDDEN)"
        DATETIME2 created_at "Thời gian tạo"
    }

    reviews {
        BIGINT id PK "IDENTITY(1,1)"
        BIGINT lecturer_id FK "Giảng viên được đánh giá"
        NVARCHAR anonymous_hash "Mã hóa ẩn danh sinh viên (SHA-256)"
        INT rating_clarity "Điểm rõ ràng (1-5)"
        INT rating_fairness "Điểm công bằng (1-5)"
        INT rating_pressure "Điểm áp lực (1-5)"
        INT rating_workload "Điểm khối lượng công việc (1-5)"
        INT rating_support "Điểm hỗ trợ (1-5)"
        NVARCHAR comment "Bình luận chi tiết"
        NVARCHAR semester "Học kỳ (1, 2, Hè)"
        NVARCHAR academic_year "Năm học (VD: 2023-2024)"
        BIT is_approved "Trạng thái phê duyệt"
        DATETIME2 created_at "Thời gian tạo"
    }

    reports {
        BIGINT id PK "IDENTITY(1,1)"
        BIGINT review_id FK "Review bị báo cáo"
        NVARCHAR reason "Lý do báo cáo vi phạm"
        DATETIME2 created_at "Thời gian báo cáo"
    }

    toxic_keywords {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR keyword UK "Từ khóa độc hại (Unique)"
        DATETIME2 created_at "Thời gian tạo"
    }

    settings {
        BIGINT id PK "IDENTITY(1,1)"
        NVARCHAR key_name UK "Tên cấu hình (Unique)"
        NVARCHAR value "Giá trị cấu hình"
        NVARCHAR value_type "Kiểu dữ liệu (STRING, NUMBER, BOOLEAN, JSON)"
        NVARCHAR description "Mô tả cấu hình"
        BIT is_sensitive "Cấu hình nhạy cảm cần ẩn"
        DATETIME2 created_at "Thời gian tạo"
        DATETIME2 updated_at "Thời gian cập nhật"
    }

    notifications {
        BIGINT id PK "IDENTITY(1,1)"
        BIGINT user_id FK "Tài khoản nhận thông báo"
        NVARCHAR title "Tiêu đề thông báo"
        NVARCHAR message "Nội dung chi tiết"
        NVARCHAR type "Loại thông báo (NEW_FEEDBACK, NEW_REPORT...)"
        BIGINT related_id "ID thực thể liên quan (Review/Report)"
        BIT is_read "Đã đọc hay chưa"
        DATETIME2 read_at "Thời gian đọc"
        DATETIME2 created_at "Thời gian tạo"
    }

    audit_logs {
        BIGINT id PK "IDENTITY(1,1)"
        BIGINT user_id FK "Tài khoản thực hiện (Admin)"
        NVARCHAR action "Hành động (CREATE, UPDATE, DELETE...)"
        NVARCHAR entity_type "Loại đối tượng tác động"
        BIGINT entity_id "ID đối tượng tác động"
        NVARCHAR old_values "Dữ liệu cũ trước khi thay đổi (JSON)"
        NVARCHAR new_values "Dữ liệu mới sau khi thay đổi (JSON)"
        NVARCHAR description "Mô tả chi tiết"
        NVARCHAR ip_address "Địa chỉ IP"
        NVARCHAR user_agent "Thông tin trình duyệt"
        DATETIME2 created_at "Thời gian thực hiện"
    }

    refresh_tokens {
        BIGINT id PK "IDENTITY(1,1)"
        BIGINT user_id FK "Tài khoản liên kết"
        NVARCHAR token UK "Mã Refresh Token (Unique)"
        DATETIME2 expiry_date "Thời gian hết hạn"
        DATETIME2 created_at "Thời gian tạo"
    }

    %% Relationships
    faculties ||--o{ subjects : "có nhiều"
    faculties ||--o{ users : "chứa"
    faculties ||--o{ lecturers : "thuộc về"
    faculties ||--o{ pending_registrations : "lưu thông tin đăng ký"

    subjects ||--o{ lecturers : "được giảng dạy bởi"

    users ||--o{ refresh_tokens : "sở hữu"
    users ||--o{ notifications : "nhận"
    users ||--o{ audit_logs : "tạo nhật ký"

    lecturers ||--o{ reviews : "nhận đánh giá"

    reviews ||--o{ reports : "bị báo cáo"
```

## Giải thích chi tiết các mối quan hệ:

1. **Khoa (Faculties) & Môn học (Subjects):** Một khoa có thể quản lý nhiều môn học (`1 - N`).
2. **Khoa (Faculties) & Người dùng (Users)/Giảng viên (Lecturers):** Cả người dùng (sinh viên) và giảng viên đều bắt buộc trực thuộc một khoa cụ thể (`1 - N`).
3. **Môn học (Subjects) & Giảng viên (Lecturers):** Một môn học có thể được phụ trách giảng dạy bởi giảng viên. Cột `subject_id` trong bảng `lecturers` có thể mang giá trị `NULL` nếu giảng viên đó chưa được gán môn học cụ thể.
4. **Giảng viên (Lecturers) & Đánh giá (Reviews):** Một giảng viên có thể nhận được nhiều đánh giá từ các sinh viên khác nhau (`1 - N`).
5. **Đánh giá (Reviews) & Báo cáo vi phạm (Reports):** Một đánh giá có thể bị báo cáo vi phạm nhiều lần bởi các sinh viên khác nhau (`1 - N`).
6. **Người dùng (Users) & Token / Nhật ký / Thông báo:**
   - Mỗi người dùng có thể có nhiều `refresh_tokens` trong quá trình đăng nhập trên nhiều thiết bị.
   - Mỗi người dùng có thể nhận nhiều `notifications` từ hệ thống.
   - Các tài khoản quản trị (Admin/Super Admin) khi thực hiện hành động sẽ tạo ra nhiều `audit_logs` để kiểm toán hệ thống.
