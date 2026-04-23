-- Demo accounts for quick login
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@ctu.edu.vn')
BEGIN
    INSERT INTO users (student_code, full_name, email, password_hash, faculty_id, role, is_verified, created_at)
    VALUES (
        'AD0001',
        N'Quản trị viên CTU',
        'admin@ctu.edu.vn',
        '$2a$10$6OQ8DchhMGjwPzXjnIQfRORuTgFpLCRWxfJLUPHRyL323lY04Qq1m',
        (SELECT id FROM faculties WHERE code = 'FIT'),
        'ADMIN',
        1,
        SYSUTCDATETIME()
    );
END

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'student01@student.ctu.edu.vn')
BEGIN
    INSERT INTO users (student_code, full_name, email, password_hash, faculty_id, role, is_verified, created_at)
    VALUES (
        'SV0001',
        N'Sinh viên Demo',
        'student01@student.ctu.edu.vn',
        '$2a$10$8H.zNbOQO81kkDkPA9w5new9uM6gVf6zAH6lO9b2xSAbmd5PkZy8.',
        (SELECT id FROM faculties WHERE code = 'ECO'),
        'STUDENT',
        1,
        SYSUTCDATETIME()
    );
END