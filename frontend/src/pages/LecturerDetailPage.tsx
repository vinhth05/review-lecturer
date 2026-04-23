import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip } from 'recharts';
import { api } from '@/services/api';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { Input } from '@/components/Input';
import { SectionTitle } from '@/components/SectionTitle';

const REVIEW_CRITERIA: Array<{ key: keyof Pick<FormState, 'ratingClarity' | 'ratingFairness' | 'ratingPressure' | 'ratingWorkload' | 'ratingSupport'>; label: string }> = [
  { key: 'ratingClarity', label: 'Dễ hiểu' },
  { key: 'ratingFairness', label: 'Công bằng' },
  { key: 'ratingPressure', label: 'Áp lực' },
  { key: 'ratingWorkload', label: 'Khối lượng' },
  { key: 'ratingSupport', label: 'Hỗ trợ' }
];

const CTU_SEMESTERS = [
  { value: 'HK1', label: 'Học kỳ 1 (HK1)' },
  { value: 'HK2', label: 'Học kỳ 2 (HK2)' },
  { value: 'HK3', label: 'Học kỳ hè (HK3)' }
];

function getCurrentSemester() {
  const month = new Date().getMonth() + 1;
  if (month >= 9) return 'HK1';
  if (month >= 5) return 'HK3';
  return 'HK2';
}

function getCurrentAcademicYear() {
  const year = new Date().getFullYear();
  const month = new Date().getMonth() + 1;
  const startYear = month >= 9 ? year : year - 1;
  return `${startYear}-${startYear + 1}`;
}

function buildAcademicYearOptions(count = 5) {
  const currentYear = new Date().getFullYear();
  const month = new Date().getMonth() + 1;
  const startYear = month >= 9 ? currentYear : currentYear - 1;

  return Array.from({ length: count }, (_, index) => {
    const year = startYear - index;
    return {
      value: `${year}-${year + 1}`,
      label: `Năm học ${year}-${year + 1}`
    };
  });
}

interface FormState {
  ratingClarity: number;
  ratingFairness: number;
  ratingPressure: number;
  ratingWorkload: number;
  ratingSupport: number;
  comment: string;
  semester: string;
  academicYear: string;
}

export function LecturerDetailPage() {
  const { id } = useParams();
  const [data, setData] = useState<any>(null);
  const [message, setMessage] = useState('');
  const academicYearOptions = useMemo(() => buildAcademicYearOptions(), []);
  const [form, setForm] = useState<FormState>({
    ratingClarity: 5,
    ratingFairness: 5,
    ratingPressure: 3,
    ratingWorkload: 3,
    ratingSupport: 5,
    comment: '',
    semester: getCurrentSemester(),
    academicYear: getCurrentAcademicYear()
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
      await api.post('/reviews', { ...form, lecturerId: Number(id) });
      setMessage('Đã gửi review. Chờ admin duyệt.');
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Gửi review thất bại');
    }
  }

  function renderStars(key: keyof Pick<FormState, 'ratingClarity' | 'ratingFairness' | 'ratingPressure' | 'ratingWorkload' | 'ratingSupport'>) {
    const value = form[key];
    return (
      <div className="rating-stars" role="group" aria-label={`Đánh giá ${key}`}>
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            className={`star-btn ${star <= value ? 'active' : ''}`}
            onClick={() => setForm((prev) => ({ ...prev, [key]: star }))}
            aria-label={`${star} sao`}
            title={`${star} sao`}
          >
            ★
          </button>
        ))}
        <span className="rating-value">{value}/5</span>
      </div>
    );
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
          <div className="rating-grid">
            {REVIEW_CRITERIA.map((criterion) => (
              <div key={criterion.key} className="rating-item">
                <p className="rating-label">{criterion.label}</p>
                {renderStars(criterion.key)}
              </div>
            ))}
          </div>
          <label>Bình luận</label>
          <textarea
            className="input review-textarea"
            value={form.comment}
            onChange={(event) => setForm((prev) => ({ ...prev, comment: event.target.value }))}
            placeholder="Chia sẻ trải nghiệm học tập của bạn..."
            rows={4}
          />
          <div className="grid-two">
            <label>
              Học kỳ
              <select
                className="input"
                value={form.semester}
                onChange={(event) => setForm((prev) => ({ ...prev, semester: event.target.value }))}
              >
                {CTU_SEMESTERS.map((semester) => (
                  <option key={semester.value} value={semester.value}>
                    {semester.label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Năm học
              <select
                className="input"
                value={form.academicYear}
                onChange={(event) => setForm((prev) => ({ ...prev, academicYear: event.target.value }))}
              >
                {academicYearOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </label>
          </div>
          <Button type="submit">Gửi review</Button>
          <small>{message}</small>
        </form>
      </Card>
    </div>
  );
}
