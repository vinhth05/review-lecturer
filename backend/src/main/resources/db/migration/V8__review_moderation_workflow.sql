-- Make review moderation an explicit, auditable workflow while retaining
-- is_approved as a backwards-compatible read model for existing queries.

IF COL_LENGTH('reviews', 'moderation_status') IS NULL
BEGIN
    ALTER TABLE reviews ADD moderation_status NVARCHAR(20) NULL;
    UPDATE reviews
    SET moderation_status = CASE WHEN is_approved = 1 THEN 'APPROVED' ELSE 'PENDING' END;
    ALTER TABLE reviews ALTER COLUMN moderation_status NVARCHAR(20) NOT NULL;
END

IF COL_LENGTH('reviews', 'moderation_reason') IS NULL
    ALTER TABLE reviews ADD moderation_reason NVARCHAR(1000) NULL;

IF COL_LENGTH('reviews', 'moderated_by') IS NULL
    ALTER TABLE reviews ADD moderated_by BIGINT NULL;

IF COL_LENGTH('reviews', 'moderated_at') IS NULL
    ALTER TABLE reviews ADD moderated_at DATETIME2 NULL;

IF COL_LENGTH('reviews', 'version') IS NULL
    ALTER TABLE reviews ADD version BIGINT NOT NULL CONSTRAINT df_reviews_version DEFAULT 0;

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'ck_reviews_moderation_status')
    ALTER TABLE reviews ADD CONSTRAINT ck_reviews_moderation_status
        CHECK (moderation_status IN ('PENDING', 'APPROVED', 'REJECTED'));

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_reviews_moderated_by')
    ALTER TABLE reviews ADD CONSTRAINT fk_reviews_moderated_by
        FOREIGN KEY (moderated_by) REFERENCES users(id);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'ix_reviews_moderation_status' AND object_id = OBJECT_ID('reviews'))
    CREATE INDEX ix_reviews_moderation_status ON reviews(moderation_status, created_at DESC);
