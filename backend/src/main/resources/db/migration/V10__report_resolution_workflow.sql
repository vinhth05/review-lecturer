-- Preserve report history and model resolution as a workflow instead of
-- deleting evidence after an administrator takes action.

IF COL_LENGTH('reports', 'reporter_hash') IS NULL
BEGIN
    ALTER TABLE reports ADD reporter_hash NVARCHAR(128) NULL;
    UPDATE reports SET reporter_hash = CONCAT('legacy-', id) WHERE reporter_hash IS NULL;
    ALTER TABLE reports ALTER COLUMN reporter_hash NVARCHAR(128) NOT NULL;
END

IF COL_LENGTH('reports', 'status') IS NULL
BEGIN
    ALTER TABLE reports ADD status NVARCHAR(20) NULL;
    UPDATE reports SET status = 'PENDING' WHERE status IS NULL;
    ALTER TABLE reports ALTER COLUMN status NVARCHAR(20) NOT NULL;
END

IF COL_LENGTH('reports', 'resolution_note') IS NULL
    ALTER TABLE reports ADD resolution_note NVARCHAR(1000) NULL;

IF COL_LENGTH('reports', 'resolved_by') IS NULL
    ALTER TABLE reports ADD resolved_by BIGINT NULL;

IF COL_LENGTH('reports', 'resolved_at') IS NULL
    ALTER TABLE reports ADD resolved_at DATETIME2 NULL;

IF COL_LENGTH('reports', 'version') IS NULL
    ALTER TABLE reports ADD version BIGINT NOT NULL CONSTRAINT df_reports_version DEFAULT 0;

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'ck_reports_status')
    ALTER TABLE reports ADD CONSTRAINT ck_reports_status
        CHECK (status IN ('PENDING', 'DISMISSED', 'ACTIONED'));

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_reports_resolved_by')
    ALTER TABLE reports ADD CONSTRAINT fk_reports_resolved_by
        FOREIGN KEY (resolved_by) REFERENCES users(id);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_reports_status' AND object_id = OBJECT_ID('reports'))
    CREATE INDEX ix_reports_status ON reports(status, created_at DESC);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_reports_reporter_review' AND object_id = OBJECT_ID('reports'))
    CREATE INDEX ix_reports_reporter_review ON reports(reporter_hash, review_id, status);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ux_reports_pending_reporter_review' AND object_id = OBJECT_ID('reports'))
    CREATE UNIQUE INDEX ux_reports_pending_reporter_review
        ON reports(reporter_hash, review_id)
        WHERE status = 'PENDING';
