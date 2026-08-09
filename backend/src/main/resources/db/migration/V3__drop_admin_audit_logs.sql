-- Drop the admin audit log table now that audit logging has been removed.

IF OBJECT_ID('admin_audit_logs', 'U') IS NOT NULL
BEGIN
    DROP TABLE admin_audit_logs;
END
GO
