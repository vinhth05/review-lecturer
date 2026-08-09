-- V6__seed_initial_data.sql
-- Seed faculties
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'FIT')
    INSERT INTO faculties (name, code) VALUES (N'Khoa Công nghệ thông tin', 'FIT');
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'ECO')
    INSERT INTO faculties (name, code) VALUES (N'Khoa Kinh tế', 'ECO');
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'EDU')
    INSERT INTO faculties (name, code) VALUES (N'Khoa Sư phạm', 'EDU');
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'FST')
    INSERT INTO faculties (name, code) VALUES (N'Khoa Công nghệ thực phẩm', 'FST');
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'AGR')
    INSERT INTO faculties (name, code) VALUES (N'Khoa Nông nghiệp', 'AGR');
IF NOT EXISTS (SELECT 1 FROM faculties WHERE code = 'AQU')
    INSERT INTO faculties (name, code) VALUES (N'Khoa Thủy sản', 'AQU');

-- Seed subjects
IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'WEB101')
    INSERT INTO subjects (name, code, faculty_id) VALUES (N'Lập trình Web', 'WEB101', (SELECT id FROM faculties WHERE code = 'FIT'));
IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'DB201')
    INSERT INTO subjects (name, code, faculty_id) VALUES (N'Cơ sở dữ liệu', 'DB201', (SELECT id FROM faculties WHERE code = 'FIT'));
IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'ECO101')
    INSERT INTO subjects (name, code, faculty_id) VALUES (N'Kinh tế vi mô', 'ECO101', (SELECT id FROM faculties WHERE code = 'ECO'));
IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'EDU201')
    INSERT INTO subjects (name, code, faculty_id) VALUES (N'Tâm lý giáo dục', 'EDU201', (SELECT id FROM faculties WHERE code = 'EDU'));
IF NOT EXISTS (SELECT 1 FROM subjects WHERE code = 'FST101')
    INSERT INTO subjects (name, code, faculty_id) VALUES (N'Công nghệ chế biến', 'FST101', (SELECT id FROM faculties WHERE code = 'FST'));

-- Seed lecturers
IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVIT001')
    INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status) VALUES ('GVIT001', N'Th.S Nguyễn Văn A', (SELECT id FROM faculties WHERE code = 'FIT'), (SELECT id FROM subjects WHERE code = 'WEB101'), 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVECO001')
    INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status) VALUES ('GVECO001', N'TS Trần Thị B', (SELECT id FROM faculties WHERE code = 'ECO'), (SELECT id FROM subjects WHERE code = 'ECO101'), 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVEDU001')
    INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status) VALUES ('GVEDU001', N'Th.S Lê Văn C', (SELECT id FROM faculties WHERE code = 'EDU'), (SELECT id FROM subjects WHERE code = 'EDU201'), 'ACTIVE');
IF NOT EXISTS (SELECT 1 FROM lecturers WHERE lecturer_code = 'GVFST001')
    INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status) VALUES ('GVFST001', N'TS Phạm Thị D', (SELECT id FROM faculties WHERE code = 'FST'), (SELECT id FROM subjects WHERE code = 'FST101'), 'ACTIVE');
