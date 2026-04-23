CREATE TABLE toxic_keywords (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    keyword NVARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);

CREATE INDEX idx_toxic_keywords_keyword ON toxic_keywords(keyword);
