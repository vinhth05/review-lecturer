import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts';
import { api } from '@/services/api';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { Input } from '@/components/Input';
import { SectionTitle } from '@/components/SectionTitle';

export function LecturerDetailPage() {
  const { id } = useParams();
  const [data, setData] = useState<any>(null);
  const [message, setMessage] = useState('');
  const [form, setForm] = useState({
    ratingClarity: 5,
    ratingFairness: 5,
    ratingPressure: 3,
    ratingWorkload: 3,
    ratingSupport: 5,
    comment: '',
    semester: 'HK1',
    academicYear: '2025-2026'
  });

  useEffect(() => {
    if (!id) return;
    api.get(`/lecturers/${id}`).then((response) => setData(response.data));
  }, [id]);

  const radar = useMemo(() => {
    if (!data) return [];
    return [
      { criterion: 'Clarity', value: data.averageClarity },
      { criterion: 'Fairness', value: data.averageFairness },
      { criterion: 'Pressure', value: data.averagePressure },
      { criterion: 'Workload', value: data.averageWorkload },
      { criterion: 'Support', value: data.averageSupport }
    ];
  }, [data]);

  async function submitReview(event: FormEvent) {
    event.preventDefault();
    if (!id) return;
    try {
      await api.post('/reviews', { ...form, lecturerId: id });
      setMessage('Đã gửi review. Chờ admin duyệt.');
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Gửi review thất bại');
    }
  }

  if (!data) {
    return <div className="page">Đang tải dữ liệu giảng viên...</div>;
  }

  return (
    <div className="page fade-up">
      <SectionTitle
        eyebrow={data.facultyName}
        title={data.fullName}
        description={`${data.subjectName ?? 'Chưa gán môn'} • ${data.reviewCount} lượt đánh giá`}
      />
      <div className="card-grid">
        <Card>
          <h3>Radar 5 tiêu chí</h3>
          <div style={{ width: '100%', height: 280 }}>
            <ResponsiveContainer>
              <RadarChart data={radar}>
                <PolarGrid />
                <PolarAngleAxis dataKey="criterion" />
                <PolarRadiusAxis domain={[0, 5]} />
                <Radar dataKey="value" fill="var(--primary)" stroke="var(--primary)" fillOpacity={0.45} />
              </RadarChart>
            </ResponsiveContainer>
          </div>
        </Card>
        <Card>
          <h3>Phân bố điểm</h3>
          <div style={{ width: '100%', height: 280 }}>
            <ResponsiveContainer>
              <BarChart data={data.distribution}>
                <XAxis dataKey="score" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="count" fill="var(--accent)" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </div>

      <Card style={{ marginTop: 14 }}>
        <h3>Gửi review</h3>
        <form className="form" onSubmit={submitReview}>
          <div className="grid-two">
            {['ratingClarity', 'ratingFairness', 'ratingPressure', 'ratingWorkload', 'ratingSupport'].map((key) => (
              <label key={key}>
                {key}
                <Input
                  type="number"
                  min={1}
                  max={5}
                  value={String((form as any)[key])}
                  onChange={(event) => setForm((prev) => ({ ...prev, [key]: Number(event.target.value) }))}
                />
              </label>
            ))}
          </div>
          <label>Bình luận</label>
          <textarea
            className="input"
            value={form.comment}
            onChange={(event) => setForm((prev) => ({ ...prev, comment: event.target.value }))}
          />
          <div className="grid-two">
            <label>
              Học kỳ
              <Input value={form.semester} onChange={(event) => setForm((prev) => ({ ...prev, semester: event.target.value }))} />
            </label>
            <label>
              Năm học
              <Input value={form.academicYear} onChange={(event) => setForm((prev) => ({ ...prev, academicYear: event.target.value }))} />
            </label>
          </div>
          <Button type="submit">Gửi review</Button>
          <small>{message}</small>
        </form>
      </Card>
    </div>
  );
}
