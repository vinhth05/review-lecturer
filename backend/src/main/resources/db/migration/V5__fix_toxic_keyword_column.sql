-- V5: Safely change toxic_keywords.keyword column to NVARCHAR(100)
-- Drops dependent unique constraints/indexes on the column, alters it, and recreates a unique index.

SET NOCOUNT ON;

DECLARE @sql NVARCHAR(MAX) = N'';

-- Drop key constraints that depend on the column
SELECT @sql = @sql + 'ALTER TABLE [' + OBJECT_SCHEMA_NAME(kc.parent_object_id) + '].[' + OBJECT_NAME(kc.parent_object_id) + '] DROP CONSTRAINT [' + kc.name + '];'
FROM sys.key_constraints kc
JOIN sys.index_columns ic ON kc.parent_object_id = ic.object_id AND kc.unique_index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE kc.parent_object_id = OBJECT_ID('dbo.toxic_keywords') AND c.name = 'keyword';

-- Drop non-key indexes that reference the column (exclude primary key)
SELECT @sql = @sql + 'DROP INDEX [' + i.name + '] ON [' + OBJECT_SCHEMA_NAME(i.object_id) + '].[' + OBJECT_NAME(i.object_id) + '];'
FROM sys.indexes i
JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
WHERE i.object_id = OBJECT_ID('dbo.toxic_keywords') AND c.name = 'keyword' AND i.is_primary_key = 0;

IF @sql IS NOT NULL AND LEN(@sql) > 0
BEGIN
    PRINT 'Executing cleanup SQL: ' + LEFT(@sql, 4000);
    EXEC sp_executesql @sql;
END

-- Now safely alter the column to NVARCHAR(100) NOT NULL (matches entity columnDefinition)
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.toxic_keywords') AND name = 'keyword')
BEGIN
    ALTER TABLE dbo.toxic_keywords ALTER COLUMN keyword NVARCHAR(100) NOT NULL;
END

-- Recreate a unique index on the keyword column if it does not exist
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes i
    JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
    JOIN sys.columns c ON ic.object_id = c.object_id AND ic.column_id = c.column_id
    WHERE i.object_id = OBJECT_ID('dbo.toxic_keywords') AND c.name = 'keyword' AND i.is_unique = 1
)
BEGIN
    CREATE UNIQUE INDEX ux_toxic_keywords_keyword ON dbo.toxic_keywords(keyword);
END

SET NOCOUNT OFF;
