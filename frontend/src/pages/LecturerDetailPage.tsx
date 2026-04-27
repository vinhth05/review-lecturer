import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import {
  Bar,
  BarChart,
  PolarAngleAxis,
  PolarGrid,
  PolarRadiusAxis,
  Radar,
  RadarChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from 'recharts';
import { api } from '@/services/api';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { formatScore } from '@/utils/format';
import { useAuthStore } from '@/store/authStore';

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

interface LecturerDetailResponse {
  id: number;
  lecturerCode?: string;
  fullName: string;
  facultyName?: string;
  subjectName?: string;
  status?: string;
  averageClarity: number;
  averageFairness: number;
  averagePressure: number;
  averageWorkload: number;
  averageSupport: number;
  reviewCount: number;
  distribution: Array<{ score: number; count: number }>;
  semesterComparisons: Array<{ semester: string; academicYear: string; averageRating: number; reviewCount: number }>;
  latestReviews: Array<{
    id: number;
    ratingClarity: number;
    ratingFairness: number;
    ratingPressure: number;
    ratingWorkload: number;
    ratingSupport: number;
    comment: string;
    semester: string;
    academicYear: string;
    createdAt: string;
  }>;
}

interface HighlightItem {
  label: string;
  value: number;
  tone: 'good' | 'warn' | 'strong' | 'soft';
}

function clampAverage(value: number | undefined) {
  return typeof value === 'number' && Number.isFinite(value) ? value : 0;
}

function formatCreatedAt(createdAt: string) {
  const date = new Date(createdAt);
  if (Number.isNaN(date.getTime())) {
    return 'Mới đây';
  }

  const diffInHours = Math.round((Date.now() - date.getTime()) / 36e5);
  if (diffInHours < 24) {
    return `${Math.max(diffInHours, 1)} giờ trước`;
  }

  const diffInDays = Math.round(diffInHours / 24);
  if (diffInDays < 7) {
    return `${diffInDays} ngày trước`;
  }

  return date.toLocaleDateString('vi-VN');
}

function formatCriteriaScore(value: number) {
  return `${formatScore(value)}/5`;
}

function getOverallAverage(data: LecturerDetailResponse | null) {
  if (!data) return 0;
  return (
    clampAverage(data.averageClarity ?? 0) +
    clampAverage(data.averageFairness ?? 0) +
    clampAverage(data.averagePressure ?? 0) +
    clampAverage(data.averageWorkload ?? 0) +
    clampAverage(data.averageSupport ?? 0)
  ) / 5;
}

function getRatingTone(score: number): HighlightItem['tone'] {
  if (score >= 4.4) return 'strong';
  if (score >= 3.8) return 'good';
  if (score >= 3) return 'soft';
  return 'warn';
}

function getFitLabel(score: number) {
  if (score >= 4.6) return 'TEAM QUA MÔN';
  if (score >= 4) return 'TINH THẦN THÉP';
  if (score >= 3.2) return 'HỌC CHẮC TƯ DUY';
  return 'CẦN CHUẨN BỊ KĨ';
}

function getReviewHighlights(review: LecturerDetailResponse['latestReviews'][number]) {
  const metrics = [
    { label: 'Giảng cuốn', value: review.ratingClarity },
    { label: 'Công bằng', value: review.ratingFairness },
    { label: 'Áp lực', value: review.ratingPressure },
    { label: 'Khối lượng', value: review.ratingWorkload },
    { label: 'Hỗ trợ', value: review.ratingSupport }
  ];

  return metrics
    .sort((a, b) => b.value - a.value)
    .slice(0, 2)
    .map((item) => item.label);
}

export function LecturerDetailPage() {
  const { id } = useParams();
  const authUser = useAuthStore((state) => state.user);
  const [data, setData] = useState<LecturerDetailResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const academicYearOptions = useMemo(() => buildAcademicYearOptions(), []);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [submitSuccess, setSubmitSuccess] = useState(false);
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
    let isMounted = true;
    setLoading(true);
    setError('');

    api
      .get(`/lecturers/${id}`)
      .then((response) => {
        if (isMounted) {
          setData(response.data);
        }
      })
      .catch(() => {
        if (isMounted) {
          setError('Không tải được dữ liệu giảng viên.');
        }
      })
      .finally(() => {
        if (isMounted) {
          setLoading(false);
        }
      });

    return () => {
      isMounted = false;
    };
  }, [id]);

  const isFormValid = useMemo(() => {
    const minCommentLength = 10;
    const hasComment = form.comment.trim().length >= minCommentLength;
    const hasRatings = form.ratingClarity > 0 && form.ratingFairness > 0 && form.ratingPressure > 0 && form.ratingWorkload > 0 && form.ratingSupport > 0;
    const hasSemester = form.semester.length > 0;
    const hasAcademicYear = form.academicYear.length > 0;
    return hasComment && hasRatings && hasSemester && hasAcademicYear;
  }, [form]);

  const commentLength = form.comment.trim().length;
  const minCommentLength = 10;
  const maxCommentLength = 1000;
  const isCommentValid = commentLength >= minCommentLength && commentLength <= maxCommentLength;

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

  const overallAverage = useMemo(() => getOverallAverage(data), [data]);

  const highlights = useMemo<HighlightItem[]>(() => {
    if (!data) return [];

    return [
      { label: 'Giảng cuốn, dễ hiểu', value: clampAverage(data.averageClarity ?? 0), tone: getRatingTone(data.averageClarity ?? 0) },
      { label: 'Công bằng', value: clampAverage(data.averageFairness ?? 0), tone: getRatingTone(data.averageFairness ?? 0) },
      { label: 'Áp lực & độ khó', value: clampAverage(data.averagePressure ?? 0), tone: getRatingTone(data.averagePressure ?? 0) },
      { label: 'Khối lượng bài tập', value: clampAverage(data.averageWorkload ?? 0), tone: getRatingTone(data.averageWorkload ?? 0) },
      { label: 'Support nhiệt tình', value: clampAverage(data.averageSupport ?? 0), tone: getRatingTone(data.averageSupport ?? 0) }
    ]
      .sort((left, right) => right.value - left.value)
      .slice(0, 4);
  }, [data]);

  const bestFitLabel = useMemo(() => getFitLabel(overallAverage), [overallAverage]);

  const topSemesters = useMemo(() => {
    if (!data) return [];
    return [...data.semesterComparisons]
      .sort((left, right) => right.averageRating - left.averageRating)
      .slice(0, 3);
  }, [data]);

  const distributionData = useMemo(() => {
    if (!data) return [];
    return [...data.distribution].sort((left, right) => left.score - right.score);
  }, [data]);

  const chartTickStyle = useMemo(() => ({ fill: 'var(--muted)', fontSize: 12 }), []);
  const chartTooltipStyle = useMemo(
    () => ({
      contentStyle: {
        background: 'var(--bg-elevated)',
        border: '1px solid var(--border)',
        borderRadius: '14px',
        boxShadow: 'var(--shadow)',
        color: 'var(--text)'
      },
      labelStyle: { color: 'var(--text)', fontWeight: 700 },
      itemStyle: { color: 'var(--muted)' },
      cursor: { fill: 'rgba(111, 180, 255, 0.08)' }
    }),
    []
  );

  async function sharePage() {
    if (!data || typeof window === 'undefined') return;
    const text = `${data.fullName} - ${bestFitLabel} - ${formatScore(overallAverage)}/5`;
    try {
      await navigator.clipboard.writeText(`${window.location.href}\n${text}`);
      setMessage('Đã sao chép liên kết review.');
    } catch {
      setMessage('Không thể sao chép liên kết.');
    }
  }

  async function submitReview(event: FormEvent) {
    event.preventDefault();
    if (!id) return;
    if (!authUser?.token) {
      setSubmitError('Bạn cần đăng nhập để gửi review.');
      return;
    }
    if (!authUser.verified) {
      setSubmitError('Tài khoản chưa xác thực. Vui lòng xác thực trước khi gửi review.');
      return;
    }
    if (!isFormValid) {
      setSubmitError('Vui lòng điền đầy đủ thông tin và bình luận ít nhất 10 ký tự.');
      return;
    }

    setSubmitLoading(true);
    setSubmitError('');
    setSubmitSuccess(false);

    try {
      await api.post('/reviews', { ...form, lecturerId: Number(id) });
      const refreshed = await api.get(`/lecturers/${id}`);
      setData(refreshed.data);
      setSubmitSuccess(true);
      setSubmitError('');
      setMessage('Review đã được đăng và hiển thị ngay.');
      // Reset form after success
      setTimeout(() => {
        setForm({
          ratingClarity: 5,
          ratingFairness: 5,
          ratingPressure: 3,
          ratingWorkload: 3,
          ratingSupport: 5,
          comment: '',
          semester: getCurrentSemester(),
          academicYear: getCurrentAcademicYear()
        });
        setSubmitSuccess(false);
      }, 3000);
    } catch (submitError: any) {
      const backendMessage = submitError?.response?.data?.message;
      if (backendMessage === 'Unauthenticated') {
        setSubmitError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
      } else {
        setSubmitError(backendMessage ?? 'Gửi review thất bại. Vui lòng thử lại.');
      }
      setSubmitSuccess(false);
    } finally {
      setSubmitLoading(false);
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
            disabled={submitLoading}
          >
            ★
          </button>
        ))}
        <span className="rating-value">{value}/5</span>
      </div>
    );
  }

  if (loading) {
    return <div className="page lecturer-detail-page fade-up">Đang tải dữ liệu giảng viên...</div>;
  }

  if (error || !data) {
    return (
      <div className="page lecturer-detail-page fade-up">
        <Card className="detail-empty-state">
          <p className="eyebrow">RATE MY LECTURER</p>
          <h2>Không tìm thấy trang giảng viên</h2>
          <p>{error || 'Dữ liệu hiện tại không khả dụng.'}</p>
          <Link to="/lecturers" className="btn btn-primary detail-empty-action">
            Quay lại danh sách
          </Link>
        </Card>
      </div>
    );
  }

  return (
    <div className="page lecturer-detail-page fade-up">
      <section className="lecturer-hero">
        <div className="lecturer-hero-copy">
          <Link to="/lecturers" className="back-link">
            ← Quay lại RATE MY LECTURER
          </Link>
          <div className="hero-kicker">{data.facultyName ?? 'Cộng đồng nhận xét'}</div>
          <h1>{data.fullName}</h1>
          <p className="hero-subtitle">
            {data.lecturerCode ? data.lecturerCode.toUpperCase() : 'LECTURER'} · {data.subjectName ?? 'Chưa gán môn'} · {data.status ?? 'ACTIVE'}
          </p>
          <div className="hero-tags">
            <span className="hero-tag hero-tag-strong">{bestFitLabel}</span>
            <span className="hero-tag">{data.reviewCount} reviews</span>
            <span className="hero-tag">{data.facultyName ?? 'Chưa cập nhật khoa'}</span>
          </div>
          <div className="hero-actions">
            <a href="#review-form" className="btn btn-primary">
              Viết review
            </a>
            <a href="#community" className="btn btn-ghost">
              Cộng đồng nhận xét
            </a>
          </div>
        </div>

        <Card className="hero-summary-card">
          <div className="summary-score">
            <div className="score-ring">
              <span>{formatScore(overallAverage)}</span>
              <small>/ 5.0</small>
            </div>
            <div>
              <p className="summary-title">KỶ LUẬT & CHUYÊN SÂU</p>
              <p className="summary-copy">Dựa trên {data.reviewCount} review đã duyệt</p>
            </div>
          </div>

          <div className="hero-metrics">
            <div>
              <strong>{formatCriteriaScore(data.averageClarity)}</strong>
              <span>Giảng cuốn, dễ hiểu</span>
            </div>
            <div>
              <strong>{formatCriteriaScore(data.averageSupport)}</strong>
              <span>Support nhiệt tình</span>
            </div>
            <div>
              <strong>{formatCriteriaScore(data.averagePressure)}</strong>
              <span>Áp lực & độ khó</span>
            </div>
            <div>
              <strong>{formatCriteriaScore(data.averageWorkload)}</strong>
              <span>Khối lượng bài</span>
            </div>
          </div>
        </Card>
      </section>

      <section className="detail-layout">
        <div className="detail-main-column">
          <Card className="detail-section-card">
            <div className="detail-section-heading">
              <div>
                <p className="eyebrow">CÁC MÔN GIẢNG DẠY</p>
                <h3>Thông tin giảng dạy</h3>
              </div>
              <span className="detail-section-meta">Mã giảng viên: {data.lecturerCode ?? 'N/A'}</span>
            </div>
            <div className="chip-row">
              <span className="detail-chip detail-chip-primary">{data.subjectName ?? 'Chưa cập nhật môn'}</span>
              <span className="detail-chip">{data.facultyName ?? 'Chưa cập nhật khoa'}</span>
              <span className="detail-chip">{data.status ?? 'ACTIVE'}</span>
              <span className="detail-chip">{bestFitLabel}</span>
            </div>
            <p className="detail-supporting-text">
              Trang này mô phỏng bố cục cộng đồng review: tên lớn, nhãn môn học, điểm tổng hợp và các thẻ trạng thái để người xem quét nhanh.
            </p>
          </Card>

          <div className="card-grid detail-grid-split">
            <Card className="detail-section-card">
              <div className="detail-section-heading compact">
                <div>
                  <p className="eyebrow">PHÂN PHỐI ĐÁNH GIÁ ĐỘ HỢP</p>
                  <h3>Điểm tổng hợp</h3>
                </div>
                <span className="detail-section-meta">{data.reviewCount} lượt</span>
              </div>
              <div className="chart-box chart-box-bars">
                <ResponsiveContainer>
                  <BarChart data={distributionData}>
                    <XAxis dataKey="score" tickLine={false} axisLine={false} tick={chartTickStyle} />
                    <YAxis tickLine={false} axisLine={false} allowDecimals={false} tick={chartTickStyle} />
                    <Tooltip {...chartTooltipStyle} />
                    <Bar dataKey="count" fill="var(--accent)" radius={[12, 12, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
              <div className="distribution-legend">
                {distributionData.map((item) => (
                  <div key={item.score}>
                    <strong>{item.score} sao</strong>
                    <span>{item.count} review</span>
                  </div>
                ))}
              </div>
            </Card>

            <Card className="detail-section-card">
              <div className="detail-section-heading compact">
                <div>
                  <p className="eyebrow">ĐẶC ĐIỂM NỔI BẬT</p>
                  <h3>Top tín hiệu nổi bật</h3>
                </div>
                <span className="detail-section-meta">Từ 5 tiêu chí</span>
              </div>
              <div className="highlight-list">
                {highlights.map((item) => (
                  <div key={item.label} className={`highlight-pill tone-${item.tone}`}>
                    <span>{item.label}</span>
                    <strong>{formatCriteriaScore(item.value)}</strong>
                  </div>
                ))}
              </div>
              <div className="chart-box chart-box-radar">
                <ResponsiveContainer>
                  <RadarChart data={radar}>
                    <PolarGrid stroke="rgba(159, 176, 200, 0.22)" />
                    <PolarAngleAxis dataKey="criterion" tick={chartTickStyle} />
                    <PolarRadiusAxis domain={[0, 5]} tick={false} axisLine={false} />
                    <Radar dataKey="value" fill="var(--primary)" stroke="var(--primary)" fillOpacity={0.4} />
                  </RadarChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </div>

          <Card className="detail-section-card">
            <div className="detail-section-heading compact">
              <div>
                <p className="eyebrow">CỘNG ĐỒNG NHẬN XÉT</p>
                <h3 id="community">Các review gần nhất</h3>
              </div>
              <span className="detail-section-meta">Hiển thị 10 review mới nhất</span>
            </div>

            <div className="review-list">
              {data.latestReviews.map((review) => {
                const averageReviewScore = (review.ratingClarity + review.ratingFairness + review.ratingPressure + review.ratingWorkload + review.ratingSupport) / 5;
                const reviewHighlights = getReviewHighlights(review);

                return (
                  <article key={review.id} className="review-card">
                    <div className="review-card-head">
                      <div>
                        <p className="review-fit">PHÙ HỢP CHO: {getFitLabel(averageReviewScore)}</p>
                        <h4>ẨN DANH</h4>
                      </div>
                      <div className="review-meta-badge">
                        <span>{formatScore(averageReviewScore)}</span>
                        <small>{review.semester} · {review.academicYear}</small>
                      </div>
                    </div>

                    <div className="review-tag-row">
                      {reviewHighlights.map((highlight) => (
                        <span key={highlight} className="review-tag">
                          {highlight}
                        </span>
                      ))}
                    </div>

                    <p className="review-comment">{review.comment}</p>

                    <div className="review-score-row">
                      <span>Độ hợp {review.ratingFairness}</span>
                      <span>Độ khó {review.ratingPressure}</span>
                      <span>Hỗ trợ {review.ratingSupport}</span>
                      <span>{formatCreatedAt(review.createdAt)}</span>
                    </div>

                    <div className="review-actions">
                      <button type="button" className="review-link" onClick={() => setMessage('Đã ghi nhận báo cáo review.')}>
                        Báo cáo
                      </button>
                      <button type="button" className="review-link" onClick={sharePage}>
                        Chia sẻ
                      </button>
                      <a href="#review-form" className="review-link">
                        Phản hồi
                      </a>
                    </div>
                  </article>
                );
              })}
            </div>
          </Card>
        </div>

        <aside className="detail-side-column">
          <Card className="detail-section-card ">
            <div className="detail-section-heading compact">
              <div>
                <p className="eyebrow">TỔNG QUAN</p>
                <h3>Điểm nổi bật</h3>
              </div>
            </div>
            <div className="summary-stack">
              <div>
                <strong>{formatScore(overallAverage)}</strong>
                <span>Đánh giá trung bình</span>
              </div>
              <div>
                <strong>{data.reviewCount}</strong>
                <span>Tổng review đã duyệt</span>
              </div>
              <div>
                <strong>{topSemesters[0] ? `${topSemesters[0].semester}${topSemesters[0].academicYear.slice(-2)}` : '--'}</strong>
                <span>Học kỳ nổi bật</span>
              </div>
            </div>

            <div className="semester-list">
              {topSemesters.map((item) => (
                <div key={`${item.semester}-${item.academicYear}`} className="semester-item">
                  <div>
                    <strong>{item.semester}</strong>
                    <span>{item.academicYear}</span>
                  </div>
                  <div>
                    <strong>{formatScore(item.averageRating)}</strong>
                    <span>{item.reviewCount} review</span>
                  </div>
                </div>
              ))}
            </div>

            <div className="sidebar-actions">
              <a href="#review-form" className="btn btn-primary">
                Viết review
              </a>
              <button type="button" className="btn btn-ghost" onClick={sharePage}>
                Chia sẻ trang
              </button>
            </div>
          </Card>

          <Card className="detail-section-card" id="review-form">
            <div className="detail-section-heading compact">
              <div>
                <p className="eyebrow">GÓP Ý SỬA ĐỔI</p>
                <h3>Viết review</h3>
              </div>
            </div>
            <form className="form detail-form" onSubmit={submitReview}>
              <div className="rating-grid rating-grid-compact">
                {REVIEW_CRITERIA.map((criterion) => (
                  <div key={criterion.key} className="rating-item">
                    <p className="rating-label">{criterion.label}</p>
                    {renderStars(criterion.key)}
                  </div>
                ))}
              </div>

              <label className="field-label">
                Bình luận
                <div className="textarea-wrapper">
                <textarea
                  className="input review-textarea"
                  value={form.comment}
                  onChange={(event) => setForm((prev) => ({ ...prev, comment: event.target.value }))}
                  placeholder="Chia sẻ trải nghiệm học tập của bạn..."
                  rows={5}
                    disabled={submitLoading}
                    maxLength={maxCommentLength}
                />
                  <div className="textarea-meta">
                    <span className={`char-count ${!isCommentValid ? 'invalid' : ''}`}>
                      {commentLength}/{maxCommentLength}
                    </span>
                    {commentLength < minCommentLength && commentLength > 0 && (
                      <span className="char-hint">Tối thiểu {minCommentLength} ký tự ({minCommentLength - commentLength} ký tự còn lại)</span>
                    )}
                  </div>
                </div>
              </label>

              <div className="grid-two">
                <label className="field-label">
                  Học kỳ
                  <select
                    className="input"
                    value={form.semester}
                    onChange={(event) => setForm((prev) => ({ ...prev, semester: event.target.value }))}
                    disabled={submitLoading}
                  >
                    {CTU_SEMESTERS.map((semester) => (
                      <option key={semester.value} value={semester.value}>
                        {semester.label}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="field-label">
                  Năm học
                  <select
                    className="input"
                    value={form.academicYear}
                    onChange={(event) => setForm((prev) => ({ ...prev, academicYear: event.target.value }))}
                    disabled={submitLoading}
                  >
                    {academicYearOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="form-actions">
                <Button type="submit" disabled={submitLoading || !isFormValid || !authUser?.token || !authUser.verified} className="submit-btn">
                  {submitLoading ? (
                    <>
                      <span className="spinner" aria-hidden="true">⏳</span>
                      <span>Đang gửi...</span>
                    </>
                  ) : (
                    'Gửi review'
                  )}
                </Button>
                {!authUser?.token && <small className="form-message error-message">Vui lòng đăng nhập để gửi review.</small>}
                {authUser?.token && !authUser.verified && <small className="form-message error-message">Tài khoản cần xác thực trước khi gửi review.</small>}
              </div>

              {submitSuccess && (
                <div className="form-message success-message">
                  ✓ Đã gửi review thành công và hiển thị ngay trên trang.
                </div>
              )}

              {submitError && (
                <div className="form-message error-message">
                  ✕ {submitError}
                </div>
              )}
            </form>
          </Card>
        </aside>
      </section>
    </div>
  );
}
