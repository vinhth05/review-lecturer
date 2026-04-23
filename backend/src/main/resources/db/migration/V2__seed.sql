INSERT INTO faculties (name, code) VALUES
(N'Khoa Công nghệ thông tin', 'FIT'),
(N'Khoa Kinh tế', 'ECO'),
(N'Khoa Sư phạm', 'EDU'),
(N'Khoa Công nghệ thực phẩm', 'FST'),
(N'Khoa Nông nghiệp', 'AGR'),
(N'Khoa Thủy sản', 'AQU');

INSERT INTO subjects (name, code, faculty_id) VALUES
(N'Lập trình Web', 'WEB101', (SELECT id FROM faculties WHERE code = 'FIT')),
(N'Cơ sở dữ liệu', 'DB201', (SELECT id FROM faculties WHERE code = 'FIT')),
(N'Kinh tế vi mô', 'ECO101', (SELECT id FROM faculties WHERE code = 'ECO')),
(N'Tâm lý giáo dục', 'EDU201', (SELECT id FROM faculties WHERE code = 'EDU')),
(N'Công nghệ chế biến', 'FST101', (SELECT id FROM faculties WHERE code = 'FST'));

INSERT INTO lecturers (lecturer_code, full_name, faculty_id, subject_id, status) VALUES
('GVIT001', N'Th.S Nguyễn Văn A', (SELECT id FROM faculties WHERE code = 'FIT'), (SELECT id FROM subjects WHERE code = 'WEB101'), 'ACTIVE'),
('GVECO001', N'TS Trần Thị B', (SELECT id FROM faculties WHERE code = 'ECO'), (SELECT id FROM subjects WHERE code = 'ECO101'), 'ACTIVE'),
('GVEDU001', N'Th.S Lê Văn C', (SELECT id FROM faculties WHERE code = 'EDU'), (SELECT id FROM subjects WHERE code = 'EDU201'), 'ACTIVE'),
('GVFST001', N'TS Phạm Thị D', (SELECT id FROM faculties WHERE code = 'FST'), (SELECT id FROM subjects WHERE code = 'FST101'), 'ACTIVE');
