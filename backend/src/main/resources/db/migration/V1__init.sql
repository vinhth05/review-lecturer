CREATE TABLE faculties (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    code NVARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE TABLE subjects (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    code NVARCHAR(50) NOT NULL UNIQUE,
    faculty_id UNIQUEIDENTIFIER NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_subject_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

CREATE TABLE users (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    student_code NVARCHAR(50) NOT NULL UNIQUE,
    full_name NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    faculty_id UNIQUEIDENTIFIER NOT NULL,
    role NVARCHAR(30) NOT NULL,
    is_verified BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_user_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id)
);

CREATE TABLE lecturers (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    lecturer_code NVARCHAR(50) NOT NULL UNIQUE,
    full_name NVARCHAR(255) NOT NULL,
    faculty_id UNIQUEIDENTIFIER NOT NULL,
    subject_id UNIQUEIDENTIFIER NULL,
    status NVARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_lecturer_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id),
    CONSTRAINT fk_lecturer_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
);

CREATE TABLE reviews (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    lecturer_id UNIQUEIDENTIFIER NOT NULL,
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
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    review_id UNIQUEIDENTIFIER NOT NULL,
    reason NVARCHAR(1000) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_report_review FOREIGN KEY (review_id) REFERENCES reviews(id)
);

CREATE TABLE otp_tokens (
    id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
    email NVARCHAR(255) NOT NULL,
    token NVARCHAR(20) NOT NULL,
    expires_at DATETIME2 NOT NULL,
    used BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE INDEX idx_reviews_lecturer_approved ON reviews(lecturer_id, is_approved, created_at DESC);
CREATE INDEX idx_reviews_hash_created ON reviews(anonymous_hash, created_at DESC);
CREATE INDEX idx_reports_review ON reports(review_id, created_at DESC);
