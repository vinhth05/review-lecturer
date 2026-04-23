-- Migration: Reorganize CTU Faculties and Re-associate Subjects
-- Description: Adds 17 specific CTU schools/faculties and moves existing data to correct entities

-- 1. Create a temporary mapping for keywords to faculty codes
IF OBJECT_ID('tempdb..#KeywordFacultyMap') IS NOT NULL DROP TABLE #KeywordFacultyMap;

CREATE TABLE #KeywordFacultyMap (
    Keyword NVARCHAR(100),
    FacultyCode NVARCHAR(20)
);

-- 2. Define the new target faculties if they don't exist, and update existing ones
-- Codes: TECH, ICT, ECO, AGRI, EDU, FISH, NSCI, PSHH, LAW, ENR, FLANG, HG, ST, GRAD, MDI, BFT, HSP

IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'TECH') INSERT INTO faculties (name, code, created_at) VALUES (N'Trường Bách khoa', 'TECH', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'ICT') INSERT INTO faculties (name, code, created_at) VALUES (N'Trường Công nghệ Thông tin & Truyền thông', 'ICT', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'ECO') INSERT INTO faculties (name, code, created_at) VALUES (N'Trường Kinh tế', 'ECO', SYSUTCDATETIME()); ELSE UPDATE faculties SET name = N'Trường Kinh tế' WHERE code = 'ECO';
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'AGRI') 
BEGIN
    IF EXISTS (SELECT 1 FROM faculties WHERE code = 'AGR') UPDATE faculties SET name = N'Trường Nông nghiệp', code = 'AGRI' WHERE code = 'AGR';
    ELSE INSERT INTO faculties (name, code, created_at) VALUES (N'Trường Nông nghiệp', 'AGRI', SYSUTCDATETIME());
END;
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'EDU') INSERT INTO faculties (name, code, created_at) VALUES (N'Trường Sư phạm', 'EDU', SYSUTCDATETIME()); ELSE UPDATE faculties SET name = N'Trường Sư phạm' WHERE code = 'EDU';
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'FISH')
BEGIN
    IF EXISTS (SELECT 1 FROM faculties WHERE code = 'AQU') UPDATE faculties SET name = N'Trường Thủy sản', code = 'FISH' WHERE code = 'AQU';
    ELSE INSERT INTO faculties (name, code, created_at) VALUES (N'Trường Thủy sản', 'FISH', SYSUTCDATETIME());
END;
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'NSCI') INSERT INTO faculties (name, code, created_at) VALUES (N'Trường Khoa học Tự nhiên', 'NSCI', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'PSHH') INSERT INTO faculties (name, code, created_at) VALUES (N'Khoa Khoa học Chính trị, Xã hội và Nhân văn', 'PSHH', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'LAW') INSERT INTO faculties (name, code, created_at) VALUES (N'Khoa Luật', 'LAW', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'ENR') INSERT INTO faculties (name, code, created_at) VALUES (N'Khoa Môi trường & Tài nguyên Thiên nhiên', 'ENR', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'FLANG') INSERT INTO faculties (name, code, created_at) VALUES (N'Khoa Ngoại ngữ', 'FLANG', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'HG') INSERT INTO faculties (name, code, created_at) VALUES (N'Cơ sở Hậu Giang', 'HG', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'ST') INSERT INTO faculties (name, code, created_at) VALUES (N'Cơ sở Sóc Trăng', 'ST', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'GRAD') INSERT INTO faculties (name, code, created_at) VALUES (N'Khoa Sau đại học', 'GRAD', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'MDI') INSERT INTO faculties (name, code, created_at) VALUES (N'Viện Mekong', 'MDI', SYSUTCDATETIME());
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'BFT')
BEGIN
    IF EXISTS (SELECT 1 FROM faculties WHERE code = 'FST') UPDATE faculties SET name = N'Viện Công nghệ Sinh học và Thực phẩm', code = 'BFT' WHERE code = 'FST';
    ELSE INSERT INTO faculties (name, code, created_at) VALUES (N'Viện Công nghệ Sinh học và Thực phẩm', 'BFT', SYSUTCDATETIME());
END;
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'HSP') INSERT INTO faculties (name, code, created_at) VALUES (N'Trường THPT Thực hành Sư phạm', 'HSP', SYSUTCDATETIME());

-- Handle the old FIT code if it was not renamed above
IF EXISTS (SELECT 1 FROM faculties WHERE code = 'FIT') 
BEGIN
    -- Move FIT to ICT but ICT already exists above, so we just move data and delete FIT
    DECLARE @OldFitId BIGINT, @NewIctId BIGINT;
    SELECT @OldFitId = id FROM faculties WHERE code = 'FIT';
    SELECT @NewIctId = id FROM faculties WHERE code = 'ICT';
    
    UPDATE subjects SET faculty_id = @NewIctId WHERE faculty_id = @OldFitId;
    UPDATE lecturers SET faculty_id = @NewIctId WHERE faculty_id = @OldFitId;
    DELETE FROM faculties WHERE id = @OldFitId;
END;

-- 3. Populate Keyword Map
INSERT INTO #KeywordFacultyMap (Keyword, FacultyCode) VALUES
-- ICT
(N'CN Thông tin', 'ICT'), (N'Tin học', 'ICT'), (N'Máy tính', 'ICT'), (N'Lập trình', 'ICT'), (N'Phần mềm', 'ICT'),
-- TECH
(N'Xây Dựng', 'TECH'), (N'Điện', 'TECH'), (N'Cơ khí', 'TECH'), (N'Tự động hóa', 'TECH'), (N'Chế tạo máy', 'TECH'), (N'Cơ điện tử', 'TECH'), (N'Vật liệu', 'TECH'), (N'Kiến trúc', 'TECH'), (N'Hệ thống công nghiệp', 'TECH'), (N'Công nghệ Micro Robot', 'TECH'),
-- NSCI
(N'Toán', 'NSCI'), (N'Vật lý', 'NSCI'), (N'Hóa', 'NSCI'), (N'Sinh học', 'NSCI'), (N'Dược', 'NSCI'), (N'Nano', 'NSCI'),
-- LAW
(N'Luật', 'LAW'),
-- ENR
(N'Môi trường', 'ENR'), (N'Đất', 'ENR'), (N'Tài nguyên', 'ENR'), (N'Địa KT', 'ENR'), (N'Biển', 'ENR'), (N'Khí hậu', 'ENR'),
-- FLANG
(N'Ngôn ngữ', 'FLANG'), (N'Tiếng Anh', 'FLANG'), (N'Tiếng Pháp', 'FLANG'), (N'Tiếng Nhật', 'FLANG'),
-- BFT
(N'Công nghệ thực phẩm', 'BFT'), (N'Sinh học hợp chất', 'BFT'), (N'Thực phẩm', 'BFT'),
-- AGRI
(N'Nông nghiệp', 'AGRI'), (N'Chăn nuôi', 'AGRI'), (N'Thú y', 'AGRI'), (N'Trồng trọt', 'AGRI'), (N'Bảo vệ thực vật', 'AGRI'), (N'Nông học', 'AGRI'),
-- FISH
(N'Thủy sản', 'FISH'), (N'Nuôi trồng thủy sản', 'FISH'),
-- PSHH
(N'Sư phạm Khoa học Xã hội', 'PSHH'), (N'Triết học', 'PSHH'), (N'Lịch sử', 'PSHH'), (N'Văn học', 'PSHH'), (N'Tâm lý', 'PSHH'), (N'Địa lý', 'PSHH'), (N'Chính trị', 'PSHH');

-- 4. Re-map Subjects to correct Faculties based on keywords
UPDATE s
SET s.faculty_id = f.id
FROM subjects s
CROSS JOIN #KeywordFacultyMap km
INNER JOIN faculties f ON f.code = km.FacultyCode
WHERE (s.name LIKE '%' + km.Keyword + '%' OR s.code LIKE '%' + km.Keyword + '%');

-- 5. Re-map Lecturers based on their associated Subject
UPDATE l
SET l.faculty_id = s.faculty_id
FROM lecturers l
INNER JOIN subjects s ON s.id = l.subject_id
WHERE l.faculty_id <> s.faculty_id;

-- Cleanup
DROP TABLE #KeywordFacultyMap;
