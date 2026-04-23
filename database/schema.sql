-- CTU Lecturer Review Platform schema and seed script for SQL Server

CREATE TABLE faculties (
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	name NVARCHAR(255) NOT NULL,
	code NVARCHAR(50) NOT NULL UNIQUE,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE subjects (
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	name NVARCHAR(255) NOT NULL,
	code NVARCHAR(50) NOT NULL UNIQUE,
	faculty_id BIGINT NOT NULL,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
	CONSTRAINT fk_subject_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

CREATE TABLE users (
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	student_code NVARCHAR(50) NOT NULL UNIQUE,
	full_name NVARCHAR(255) NOT NULL,
	email NVARCHAR(255) NOT NULL UNIQUE,
	password_hash NVARCHAR(255) NOT NULL,
	faculty_id BIGINT NOT NULL,
	role NVARCHAR(30) NOT NULL,
	is_verified BIT NOT NULL DEFAULT 0,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
	CONSTRAINT fk_user_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

CREATE TABLE lecturers (
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	lecturer_code NVARCHAR(50) NOT NULL UNIQUE,
	full_name NVARCHAR(255) NOT NULL,
	faculty_id BIGINT NOT NULL,
	subject_id BIGINT NULL,
	status NVARCHAR(30) NOT NULL,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
	CONSTRAINT fk_lecturer_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id),
	CONSTRAINT fk_lecturer_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE reviews (
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	lecturer_id BIGINT NOT NULL,
	anonymous_hash NVARCHAR(128) NOT NULL,
	rating_clarity INT NOT NULL,
	rating_fairness INT NOT NULL,
	rating_pressure INT NOT NULL,
	rating_workload INT NOT NULL,
	rating_support INT NOT NULL,
	comment NVARCHAR(MAX) NOT NULL,
	semester NVARCHAR(50) NOT NULL,
	academic_year NVARCHAR(20) NOT NULL,
	is_approved BIT NOT NULL DEFAULT 0,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
	CONSTRAINT fk_review_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers(id),
	CONSTRAINT uq_review_semester UNIQUE (anonymous_hash, lecturer_id, semester, academic_year),
	CONSTRAINT ck_review_rating_clarity CHECK (rating_clarity BETWEEN 1 AND 5),
	CONSTRAINT ck_review_rating_fairness CHECK (rating_fairness BETWEEN 1 AND 5),
	CONSTRAINT ck_review_rating_pressure CHECK (rating_pressure BETWEEN 1 AND 5),
	CONSTRAINT ck_review_rating_workload CHECK (rating_workload BETWEEN 1 AND 5),
	CONSTRAINT ck_review_rating_support CHECK (rating_support BETWEEN 1 AND 5)
);

CREATE TABLE reports (
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	review_id BIGINT NOT NULL,
	reason NVARCHAR(1000) NOT NULL,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
	CONSTRAINT fk_report_review FOREIGN KEY (review_id) REFERENCES reviews(id)
);

CREATE TABLE otp_tokens (
	id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
	email NVARCHAR(255) NOT NULL,
	token NVARCHAR(20) NOT NULL,
	expires_at DATETIME2 NOT NULL,
	used BIT NOT NULL DEFAULT 0,
	created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE INDEX idx_reviews_lecturer_approved ON reviews(lecturer_id, is_approved, created_at DESC);
CREATE INDEX idx_reviews_hash_created ON reviews(anonymous_hash, created_at DESC);
CREATE INDEX idx_reports_review ON reports(review_id, created_at DESC);

INSERT INTO faculties (id, name, code) VALUES
('11111111-1111-1111-1111-111111111111', N'Khoa Công nghệ thông tin', 'FIT'),
('22222222-2222-2222-2222-222222222222', N'Khoa Kinh tế', 'ECO'),
('33333333-3333-3333-3333-333333333333', N'Khoa Sư phạm', 'EDU'),
('44444444-4444-4444-4444-444444444444', N'Khoa Công nghệ thực phẩm', 'FST'),
('55555555-5555-5555-5555-555555555555', N'Khoa Nông nghiệp', 'AGR'),
('66666666-6666-6666-6666-666666666666', N'Khoa Thủy sản', 'AQU');

INSERT INTO subjects (id, name, code, faculty_id) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', N'Lập trình Web', 'WEB101', '11111111-1111-1111-1111-111111111111'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', N'Cơ sở dữ liệu', 'DB201', '11111111-1111-1111-1111-111111111111'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', N'Kinh tế vi mô', 'ECO101', '22222222-2222-2222-2222-222222222222'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', N'Tâm lý giáo dục', 'EDU201', '33333333-3333-3333-3333-333333333333'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', N'Công nghệ chế biến', 'FST101', '44444444-4444-4444-4444-444444444444');

INSERT INTO lecturers (id, lecturer_code, full_name, faculty_id, subject_id, status) VALUES
('10101010-1010-1010-1010-101010101010', 'GVIT001', N'Th.S Nguyễn Văn A', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE'),
('20202020-2020-2020-2020-202020202020', 'GVECO001', N'TS Trần Thị B', '22222222-2222-2222-2222-222222222222', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'ACTIVE'),
('30303030-3030-3030-3030-303030303030', 'GVEDU001', N'Th.S Lê Văn C', '33333333-3333-3333-3333-333333333333', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'ACTIVE'),
('40404040-4040-4040-4040-404040404040', 'GVFST001', N'TS Phạm Thị D', '44444444-4444-4444-4444-444444444444', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'ACTIVE');
