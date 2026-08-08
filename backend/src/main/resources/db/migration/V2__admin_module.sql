-- Migration: admin module tables and indexes

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'toxic_keywords')
BEGIN
    CREATE TABLE toxic_keywords (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        keyword NVARCHAR(200) NOT NULL,
        created_by BIGINT NULL,
        is_deleted BIT DEFAULT 0 NOT NULL
    );
    CREATE UNIQUE INDEX ix_toxic_keywords_keyword ON toxic_keywords(keyword) WHERE is_deleted = 0;
END

-- Indexes for performance
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_users_email')
    CREATE INDEX ix_users_email ON [users](email);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_users_student_code')
    CREATE INDEX ix_users_student_code ON [users](student_code);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_reviews_is_approved')
    CREATE INDEX ix_reviews_is_approved ON [reviews](is_approved);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_reviews_lecturer_id')
    CREATE INDEX ix_reviews_lecturer_id ON [reviews](lecturer_id);

-- Soft delete column additions (if not present)
IF COL_LENGTH('users', 'is_deleted') IS NULL
BEGIN
    ALTER TABLE [users] ADD is_deleted BIT DEFAULT 0 NOT NULL;
END

IF COL_LENGTH('users', 'is_locked') IS NULL
BEGIN
    ALTER TABLE [users] ADD is_locked BIT DEFAULT 0 NOT NULL;
END

IF COL_LENGTH('faculties', 'is_deleted') IS NULL
BEGIN
    ALTER TABLE [faculties] ADD is_deleted BIT DEFAULT 0 NOT NULL;
END

IF COL_LENGTH('subjects', 'is_deleted') IS NULL
BEGIN
    ALTER TABLE [subjects] ADD is_deleted BIT DEFAULT 0 NOT NULL;
END

-- Foreign key constraints should already exist for reviews -> users, reviews -> lecturers etc. Add if missing intentionally omitted here for safety.

GO
