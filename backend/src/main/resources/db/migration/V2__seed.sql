INSERT INTO faculties (id, name, code) VALUES
('11111111-1111-1111-1111-111111111111', N'Khoa Công nghệ thông tin', 'FIT'),
('22222222-2222-2222-2222-222222222222', N'Khoa Kinh tế', 'ECO'),
('33333333-3333-3333-3333-333333333333', N'Khoa Sư phạm', 'EDU'),
('44444444-4444-4444-4444-444444444444', N'Khoa Công nghệ thực phẩm', 'FST'),
('55555555-5555-5555-5555-555555555555', N'Khoa Nông nghiệp', 'AGR'),
('66666666-6666-6666-6666-666666666666', N'Khoa Thủy sản', 'AQU');

INSERT INTO subjects (id, name, code, faculty_id) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', N'Lập trình Web', 'WEB101', '11111111-1111-1111-1111-111111111111'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', N'Cơ sở dữ liệu', 'DB201', '11111111-1111-1111-1111-111111111111'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', N'Kinh tế vi mô', 'ECO101', '22222222-2222-2222-2222-222222222222'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', N'Tâm lý giáo dục', 'EDU201', '33333333-3333-3333-3333-333333333333'),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', N'Công nghệ chế biến', 'FST101', '44444444-4444-4444-4444-444444444444');

INSERT INTO lecturers (id, lecturer_code, full_name, faculty_id, subject_id, status) VALUES
('10101010-1010-1010-1010-101010101010', 'GVIT001', N'Th.S Nguyễn Văn A', '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ACTIVE'),
('20202020-2020-2020-2020-202020202020', 'GVECO001', N'TS Trần Thị B', '22222222-2222-2222-2222-222222222222', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'ACTIVE'),
('30303030-3030-3030-3030-303030303030', 'GVEDU001', N'Th.S Lê Văn C', '33333333-3333-3333-3333-333333333333', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'ACTIVE'),
('40404040-4040-4040-4040-404040404040', 'GVFST001', N'TS Phạm Thị D', '44444444-4444-4444-4444-444444444444', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'ACTIVE');
