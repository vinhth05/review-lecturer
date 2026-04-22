import { useEffect, useState } from 'react';
import { api } from '@/services/api';
import { Card } from '@/components/Card';
import { SectionTitle } from '@/components/SectionTitle';

export function ProfilePage() {
  const [profile, setProfile] = useState<any>(null);

  useEffect(() => {
    api.get('/students/me').then((response) => setProfile(response.data)).catch(() => setProfile(null));
  }, []);

  return (
    <div className="page fade-up">
      <SectionTitle eyebrow="Tài khoản" title="Hồ sơ cá nhân" />
      <Card>
        {profile ? (
          <>
            <p>Họ tên: {profile.fullName}</p>
            <p>Email: {profile.email}</p>
            <p>MSSV: {profile.studentCode}</p>
            <p>Khoa: {profile.facultyName}</p>
            <p>Role: {profile.role}</p>
            <p>Đã xác thực: {profile.verified ? 'Có' : 'Chưa'}</p>
          </>
        ) : (
          <p>Không thể tải hồ sơ.</p>
        )}
      </Card>
    </div>
  );
}
