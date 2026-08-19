-- Store only SHA-256 fingerprints of refresh tokens. A database leak can no
-- longer be used directly to mint sessions.

IF COL_LENGTH('refresh_tokens', 'token_hash') IS NULL
BEGIN
    ALTER TABLE refresh_tokens ADD token_hash NVARCHAR(64) NULL;

    UPDATE refresh_tokens
    SET token_hash = LOWER(CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', CONVERT(VARCHAR(500), token)), 2))
    WHERE token_hash IS NULL;

    ALTER TABLE refresh_tokens ALTER COLUMN token_hash NVARCHAR(64) NOT NULL;
    CREATE UNIQUE INDEX ux_refresh_tokens_token_hash ON refresh_tokens(token_hash);

    -- Retain the legacy column for a reversible migration, but remove secrets.
    DECLARE @legacyTokenConstraint SYSNAME;
    SELECT TOP 1 @legacyTokenConstraint = constraint_object.name
    FROM sys.key_constraints constraint_object
    JOIN sys.index_columns index_column
      ON index_column.object_id = constraint_object.parent_object_id
     AND index_column.index_id = constraint_object.unique_index_id
    JOIN sys.columns column_object
      ON column_object.object_id = index_column.object_id
     AND column_object.column_id = index_column.column_id
    WHERE constraint_object.parent_object_id = OBJECT_ID('refresh_tokens')
      AND constraint_object.type = 'UQ'
      AND column_object.name = 'token';

    IF @legacyTokenConstraint IS NOT NULL
        EXEC('ALTER TABLE refresh_tokens DROP CONSTRAINT [' + @legacyTokenConstraint + ']');

    ALTER TABLE refresh_tokens ALTER COLUMN token NVARCHAR(500) NULL;
    UPDATE refresh_tokens SET token = NULL;
END
