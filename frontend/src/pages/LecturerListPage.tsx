import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '@/services/api';
import { Input } from '@/components/Input';

interface Lecturer {
  id: number;
  lecturerCode?: string;
  fullName: string;
  facultyName: string;
  subjectName: string;
  averageRating: number;
  reviewCount: number;
}



export function LecturerListPage() {
  const [lecturers, setLecturers] = useState<Lecturer[]>([]);
  const [search, setSearch] = useState('');
  const [selectedSpecialization, setSelectedSpecialization] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 24;

  useEffect(() => {
    api.get('/lecturers').then((response) => setLecturers(response.data)).catch(() => setLecturers([]));
  }, []);

  useEffect(() => {
    setCurrentPage(1);
  }, [search, selectedSpecialization]);

  const specializations = useMemo(() => {
    return Array.from(new Set(lecturers.map((lecturer) => lecturer.subjectName).filter(Boolean))).sort((a, b) =>
      a.localeCompare(b, 'vi')
    );
  }, [lecturers]);

  const filtered = useMemo(() => {
    return lecturers.filter((lecturer) => {
      const matchesSearch = lecturer.fullName.toLowerCase().includes(search.toLowerCase());
      const matchesSpecialization =
        selectedSpecialization === 'ALL' || lecturer.subjectName === selectedSpecialization;
      return matchesSearch && matchesSpecialization;
    });
  }, [lecturers, search, selectedSpecialization]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages);
    }
  }, [currentPage, totalPages]);

  const paginatedLecturers = useMemo(() => {
    const startIndex = (currentPage - 1) * pageSize;
    return filtered.slice(startIndex, startIndex + pageSize);
  }, [filtered, currentPage]);

  const visiblePages = useMemo(() => {
    if (totalPages <= 5) {
      return Array.from({ length: totalPages }, (_, index) => index + 1);
    }

    const pages = new Set<number>();
    pages.add(1);
    pages.add(totalPages);
    pages.add(currentPage);
    pages.add(currentPage - 1);
    pages.add(currentPage + 1);

    return Array.from(pages)
      .filter((page) => page >= 1 && page <= totalPages)
      .sort((a, b) => a - b);
  }, [currentPage, totalPages]);

  return (
    <div className="lecturer-hub fade-up">
      <div className="policy-banner">
        <span className="policy-icon">i</span>
        <span>Điều khoản sử dụng và Chính sách bảo mật mới đã được cập nhật</span>
        <a href="#">Điều khoản</a>
        <a href="#">Chính sách</a>
      </div>

      <div className="search-panel">
        <span className="search-icon">⌕</span>
        <Input
          className="search-input"
          placeholder="Tìm theo tên giảng viên..."
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
      </div>

      <div className="hub-toolbar">

        <select
          className="subject-filter"
          value={selectedSpecialization}
          onChange={(event) => setSelectedSpecialization(event.target.value)}
        >
          <option value="ALL">Bộ môn (tất cả)</option>
          {specializations.map((specialization) => (
            <option key={specialization} value={specialization}>
              {specialization}
            </option>
          ))}
        </select>
      </div>

      <div className="lecturer-grid">
        {paginatedLecturers.map((lecturer) => (
          <Link key={lecturer.id} to={`/lecturers/${lecturer.id}`} className="lecturer-card-link" aria-label={`Xem ${lecturer.fullName}`}>
            <article className="lecturer-card">
              <h3>{lecturer.fullName}</h3>
              <p className="major-text">{(lecturer.subjectName || 'Chưa cập nhật').toUpperCase()}</p>
              <div className="course-tags">
                <span className="course-tag">{lecturer.facultyName || 'N/A'}</span>
              </div>
              <div className="card-foot">
                <span>{lecturer.reviewCount} REVIEWS</span>
                <span className="card-arrow">→</span>
              </div>
            </article>
          </Link>
        ))}
      </div>

      <div className="pagination-bar">
        <span className="pagination-meta">
          Hiển thị {filtered.length === 0 ? 0 : (currentPage - 1) * pageSize + 1}-{Math.min(currentPage * pageSize, filtered.length)} / {filtered.length}
        </span>

        <div className="pagination-controls">
          <button
            type="button"
            className="pagination-btn"
            onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
            disabled={currentPage === 1}
          >
            Trước
          </button>

          {visiblePages.map((page) => (
            <button
              key={page}
              type="button"
              className={`pagination-btn ${page === currentPage ? 'active' : ''}`}
              onClick={() => setCurrentPage(page)}
            >
              {page}
            </button>
          ))}

          <button
            type="button"
            className="pagination-btn"
            onClick={() => setCurrentPage((page) => Math.min(totalPages, page + 1))}
            disabled={currentPage === totalPages}
          >
            Sau
          </button>
        </div>
      </div>
    </div>
  );
}
