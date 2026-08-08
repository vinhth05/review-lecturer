-- Migration: Add admin infrastructure tables (RefreshToken, AuditLog, Settings, Notifications)

-- ==================== RefreshTokens Table ====================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'refresh_tokens')
BEGIN
    CREATE TABLE refresh_tokens (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        token NVARCHAR(500) NOT NULL UNIQUE,
        user_id BIGINT NOT NULL,
        expires_at DATETIME2 NOT NULL,
        revoked BIT DEFAULT 0 NOT NULL,
        created_at DATETIME2 DEFAULT GETUTCDATE() NOT NULL,
        rotated_at DATETIME2 NULL,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
    CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens(user_id);
    CREATE INDEX ix_refresh_tokens_token ON refresh_tokens(token);
    CREATE INDEX ix_refresh_tokens_expires_at ON refresh_tokens(expires_at);
END

-- ==================== Audit Logs Table ====================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'audit_logs')
BEGIN
    CREATE TABLE audit_logs (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        action NVARCHAR(50) NOT NULL,
        entity_type NVARCHAR(100),
        entity_id BIGINT,
        old_values NVARCHAR(MAX),
        new_values NVARCHAR(MAX),
        description NVARCHAR(500),
        ip_address NVARCHAR(50),
        user_agent NVARCHAR(500),
        created_at DATETIME2 DEFAULT GETUTCDATE() NOT NULL,
        FOREIGN KEY (user_id) REFERENCES users(id)
    );
    CREATE INDEX ix_audit_logs_user_id ON audit_logs(user_id);
    CREATE INDEX ix_audit_logs_created_at ON audit_logs(created_at);
    CREATE INDEX ix_audit_logs_action ON audit_logs(action);
    CREATE INDEX ix_audit_logs_entity ON audit_logs(entity_type, entity_id);
END

-- ==================== Settings Table ====================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'settings')
BEGIN
    CREATE TABLE settings (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        key_name NVARCHAR(100) NOT NULL UNIQUE,
        value NVARCHAR(MAX),
        value_type NVARCHAR(50),
        description NVARCHAR(500),
        is_sensitive BIT DEFAULT 0 NOT NULL,
        created_at DATETIME2 DEFAULT GETUTCDATE() NOT NULL,
        updated_at DATETIME2 DEFAULT GETUTCDATE() NOT NULL
    );
    CREATE INDEX ix_settings_key_name ON settings(key_name);
END

-- ==================== Notifications Table ====================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'notifications')
BEGIN
    CREATE TABLE notifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        title NVARCHAR(200) NOT NULL,
        message NVARCHAR(MAX) NOT NULL,
        type NVARCHAR(50),
        related_id BIGINT,
        is_read BIT DEFAULT 0 NOT NULL,
        read_at DATETIME2 NULL,
        created_at DATETIME2 DEFAULT GETUTCDATE() NOT NULL,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );
    CREATE INDEX ix_notifications_user_id ON notifications(user_id);
    CREATE INDEX ix_notifications_is_read ON notifications(is_read);
    CREATE INDEX ix_notifications_created_at ON notifications(created_at);
END

-- ==================== Add soft delete columns if missing ====================
IF COL_LENGTH('reviews', 'is_deleted') IS NULL
BEGIN
    ALTER TABLE reviews ADD is_deleted BIT DEFAULT 0 NOT NULL;
END

IF COL_LENGTH('lecturers', 'is_deleted') IS NULL
BEGIN
    ALTER TABLE lecturers ADD is_deleted BIT DEFAULT 0 NOT NULL;
END

GO
