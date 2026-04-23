    -- Seed demo data if the database is empty or missing reference rows

    UPDATE faculties SET name = N'Trường Công nghệ Thông tin & Truyền thông' WHERE code = 'FIT';
    UPDATE faculties SET name = N'Trường Kinh tế' WHERE code = 'ECO';
    UPDATE faculties SET name = N'Trường Sư phạm' WHERE code = 'EDU';
    UPDATE faculties SET name = N'Viện Công nghệ Sinh học và Thực phẩm' WHERE code = 'FST';
    UPDATE faculties SET name = N'Trường Nông nghiệp' WHERE code = 'AGR';
    UPDATE faculties SET name = N'Trường Thủy sản' WHERE code = 'AQU';

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'FIT')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường Công nghệ Thông tin & Truyền thông', 'FIT');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'ECO')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường Kinh tế', 'ECO');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'EDU')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường Sư phạm', 'EDU');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'FST')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Viện Công nghệ Sinh học và Thực phẩm', 'FST');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'AGR')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường Nông nghiệp', 'AGR');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'AQU')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường Thủy sản', 'AQU');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'BKO')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường Bách khoa', 'BKO');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'NAT')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường Khoa học Tự nhiên', 'NAT');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'PSH')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Khoa Khoa học Chính trị, Xã hội và Nhân văn', 'PSH');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'LAW')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Khoa Luật', 'LAW');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'ENT')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Khoa Môi trường & Tài nguyên Thiên nhiên', 'ENT');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'FLS')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Khoa Ngoại ngữ', 'FLS');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'HGI')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Cơ sở Hậu Giang', 'HGI');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'STG')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Cơ sở Sóc Trăng', 'STG');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'PGS')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Khoa Sau đại học', 'PGS');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'MKI')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Viện Mekong', 'MKI');
    END;

    IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'HSP')
    BEGIN
        INSERT INTO faculties (name, code)
        VALUES (N'Trường THPT Thực hành Sư phạm', 'HSP');
    END;

    IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'WEB101')
    BEGIN
        INSERT INTO subjects (name, code, faculty_id)
        VALUES (N'Lập trình Web', 'WEB101', (SELECT id FROM faculties WHERE code = 'FIT'));
    END;

    IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'DB201')
    BEGIN
        INSERT INTO subjects (name, code, faculty_id)
        VALUES (N'Cơ sở dữ liệu', 'DB201', (SELECT id FROM faculties WHERE code = 'FIT'));
    END;

    IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'ECO101')
    BEGIN
        INSERT INTO subjects (name, code, faculty_id)
        VALUES (N'Kinh tế vi mô', 'ECO101', (SELECT id FROM faculties WHERE code = 'ECO'));
    END;

    IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'EDU201')
    BEGIN
        INSERT INTO subjects (name, code, faculty_id)
        VALUES (N'Tâm lý giáo dục', 'EDU201', (SELECT id FROM faculties WHERE code = 'EDU'));
    END;

    IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'FST101')
    BEGIN
        INSERT INTO subjects (name, code, faculty_id)
        VALUES (N'Công nghệ chế biến', 'FST101', (SELECT id FROM faculties WHERE code = 'FST'));
    END;

    IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVIT001')
    BEGIN
        INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status)
        VALUES ('GVIT001', N'Th.S Nguyễn Văn A', (SELECT id FROM faculties WHERE code = 'FIT'), (SELECT id FROM subjects WHERE code = 'WEB101'), 'ACTIVE');
    END;

    IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVECO001')
    BEGIN
        INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status)
        VALUES ('GVECO001', N'TS Trần Thị B', (SELECT id FROM faculties WHERE code = 'ECO'), (SELECT id FROM subjects WHERE code = 'ECO101'), 'ACTIVE');
    END;

    IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVEDU001')
    BEGIN
        INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status)
        VALUES ('GVEDU001', N'Th.S Lê Văn C', (SELECT id FROM faculties WHERE code = 'EDU'), (SELECT id FROM subjects WHERE code = 'EDU201'), 'ACTIVE');
    END;

    IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVFST001')
    BEGIN
        INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status)
        VALUES ('GVFST001', N'TS Phạm Thị D', (SELECT id FROM faculties WHERE code = 'FST'), (SELECT id FROM subjects WHERE code = 'FST101'), 'ACTIVE');
    END;
