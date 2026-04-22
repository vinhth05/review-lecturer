import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, Tooltip } from 'recharts';
import { api } from '@/services/api';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { SectionTitle } from '@/components/SectionTitle';

export function AdminDashboardPage() {
  const [pending, setPending] = useState<any[]>([]);
  const [stats, setStats] = useState<any>(null);

  const load = async () => {
    const [pendingRes, statsRes] = await Promise.all([
      api.get('/admin/reviews/pending'),
      api.get('/admin/statistics')
    ]);
    setPending(pendingRes.data);
    setStats(statsRes.data);
  };

  useEffect(() => {
    load().catch(() => {
      setPending([]);
      setStats(null);
    });
  }, []);

  async function approve(id: string) {
    await api.patch(`/admin/reviews/${id}/approve`);
    await load();
  }

  return (
    <div className="page fade-up">
      <SectionTitle eyebrow="Admin" title="Dashboard quản trị" description="Duyệt review, thống kê theo khoa, top giảng viên." />
      {stats ? (
        <div className="card-grid">
          <Card>
            <h3>Thống kê tổng quan</h3>
            <p>Tổng sinh viên: {stats.totalStudents}</p>
            <p>Tổng giảng viên: {stats.totalLecturers}</p>
            <p>Tổng review: {stats.totalReviews}</p>
            <p>Review chờ duyệt: {stats.pendingReviews}</p>
          </Card>
          <Card>
            <h3>Thống kê theo khoa</h3>
            <div style={{ width: '100%', height: 260 }}>
              <ResponsiveContainer>
                <BarChart data={stats.facultyStatistics ?? []}>
                  <XAxis dataKey="facultyName" hide />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="averageRating" fill="var(--primary)" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </Card>
        </div>
      ) : null}

      <Card style={{ marginTop: 14 }}>
        <h3>Review chờ duyệt</h3>
        <div className="table-scroll">
          <table className="table">
            <thead>
              <tr>
                <th>Giảng viên</th>
                <th>Khoa</th>
                <th>Bình luận</th>
                <th>Report</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {pending.map((item) => (
                <tr key={item.id}>
                  <td>{item.lecturerName}</td>
                  <td>{item.facultyName}</td>
                  <td>{item.comment}</td>
                  <td>{item.reportCount}</td>
                  <td>
                    <Button onClick={() => approve(item.id)}>Duyệt</Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
