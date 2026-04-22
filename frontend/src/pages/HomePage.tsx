import { Link } from 'react-router-dom';
import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { SectionTitle } from '@/components/SectionTitle';

export function HomePage() {
  return (
    <div className="page fade-up">
      <section className="hero card-grid">
        <Card>
          <SectionTitle
            eyebrow="Nền tảng CTU"
            title="Đánh giá giảng viên ẩn danh, minh bạch, có kiểm duyệt"
            description="Hệ thống chỉ dành cho sinh viên CTU với xác thực email @student.ctu.edu.vn và OTP."
          />
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <Link to="/lecturers">
              <Button>Xem giảng viên</Button>
            </Link>
            <Link to="/register">
              <Button tone="secondary">Tạo tài khoản</Button>
            </Link>
          </div>
        </Card>
        <Card>
          <SectionTitle
            eyebrow="Tính năng"
            title="Đúng yêu cầu bảo mật học thuật"
          />
          <ul className="feature-list">
            <li>Ẩn danh bằng SHA-256(studentCode + secret)</li>
            <li>Review cần admin duyệt trước khi hiển thị</li>
            <li>Rate limit 5 review/ngày</li>
            <li>Report nội dung vi phạm</li>
            <li>Dashboard thống kê theo khoa và toàn hệ thống</li>
          </ul>
        </Card>
      </section>
    </div>
  );
}
