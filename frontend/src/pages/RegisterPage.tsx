import { FormEvent, useEffect, useState } from 'react';
import { api } from '@/services/api';
import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { Input } from '@/components/Input';
import { SectionTitle } from '@/components/SectionTitle';

interface Faculty {
  id: number;
  name: string;
}

export function RegisterPage() {
  const [faculties, setFaculties] = useState<Faculty[]>([]);
  const [studentCode, setStudentCode] = useState('');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [facultyId, setFacultyId] = useState('');
  const [otp, setOtp] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    api.get('/metadata/faculties').then((response) => {
      setFaculties(response.data);
    }).catch(() => {
      setFaculties([
        { id: 1, name: 'Khoa Công nghệ thông tin' },
        { id: 2, name: 'Khoa Kinh tế' },
        { id: 3, name: 'Khoa Sư phạm' },
        { id: 4, name: 'Khoa Công nghệ thực phẩm' },
        { id: 5, name: 'Khoa Nông nghiệp' },
        { id: 6, name: 'Khoa Thủy sản' }
      ]);
    });
  }, []);

  async function register(event: FormEvent) {
    event.preventDefault();
    try {
      const payload = {
        studentCode,
        fullName,
        email,
        password,
        facultyId: Number(facultyId)
      };
      const { data } = await api.post('/auth/register', payload);
      setMessage(data);
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Đăng ký thất bại');
    }
  }

  async function verifyOtp() {
    try {
      await api.post('/auth/verify', { email, otp });
      setMessage('Xác thực OTP thành công. Bạn có thể đăng nhập.');
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'OTP không hợp lệ');
    }
  }

  return (
    <div className="page form-page fade-up">
      <Card>
        <SectionTitle
          eyebrow="Register"
          title="Đăng ký sinh viên CTU"
          description="Email bắt buộc @student.ctu.edu.vn và có mã số sinh viên."
        />
        <form className="form" onSubmit={register}>
          <label>Mã số sinh viên</label>
          <Input value={studentCode} onChange={(event) => setStudentCode(event.target.value)} required />
          <label>Họ và tên</label>
          <Input value={fullName} onChange={(event) => setFullName(event.target.value)} required />
          <label>Email CTU</label>
          <Input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
          <label>Mật khẩu</label>
          <Input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
          <label>Khoa</label>
          <select className="input" value={facultyId} onChange={(event) => setFacultyId(event.target.value)} required>
            <option value="">Chọn khoa</option>
            {faculties.map((faculty) => (
              <option key={faculty.id} value={faculty.id}>
                {faculty.name}
              </option>
            ))}
          </select>
          <Button type="submit">Đăng ký</Button>
        </form>
        <div className="form" style={{ marginTop: 12 }}>
          <label>OTP</label>
          <Input value={otp} onChange={(event) => setOtp(event.target.value)} />
          <Button tone="secondary" onClick={verifyOtp}>Xác thực OTP</Button>
          <small>{message}</small>
        </div>
      </Card>
    </div>
  );
}
