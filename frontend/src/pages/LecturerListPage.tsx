import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '@/services/api';
import { Card } from '@/components/Card';
import { Input } from '@/components/Input';
import { SectionTitle } from '@/components/SectionTitle';

interface Lecturer {
  id: string;
  fullName: string;
  facultyName: string;
  subjectName: string;
  averageRating: number;
  reviewCount: number;
}

export function LecturerListPage() {
  const [lecturers, setLecturers] = useState<Lecturer[]>([]);
  const [search, setSearch] = useState('');

  useEffect(() => {
    api.get('/lecturers').then((response) => setLecturers(response.data)).catch(() => setLecturers([]));
  }, []);

  const filtered = useMemo(() => {
    return lecturers.filter((lecturer) => lecturer.fullName.toLowerCase().includes(search.toLowerCase()));
  }, [lecturers, search]);

  return (
    <div className="page fade-up">
      <SectionTitle
        eyebrow="Danh sách"
        title="Giảng viên theo khoa, bộ môn"
        description="Sinh viên có thể lọc và xem thống kê tổng hợp trước khi gửi review."
      />
      <Input placeholder="Tìm giảng viên..." value={search} onChange={(event) => setSearch(event.target.value)} />
      <div className="card-grid" style={{ marginTop: 14 }}>
        {filtered.map((lecturer) => (
          <Card key={lecturer.id}>
            <h3>{lecturer.fullName}</h3>
            <p>{lecturer.facultyName} • {lecturer.subjectName}</p>
            <p>Điểm TB: {lecturer.averageRating.toFixed(2)} / 5</p>
            <p>Lượt đánh giá: {lecturer.reviewCount}</p>
            <Link to={`/lecturers/${lecturer.id}`} className="text-link">Xem chi tiết</Link>
          </Card>
        ))}
      </div>
    </div>
  );
}
