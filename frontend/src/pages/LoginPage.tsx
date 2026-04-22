import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '@/services/api';
import { useAuthStore } from '@/store/authStore';
import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { Input } from '@/components/Input';
import { SectionTitle } from '@/components/SectionTitle';

export function LoginPage() {
  const navigate = useNavigate();
  const setUser = useAuthStore((state) => state.setUser);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setMessage('Đang xử lý...');
    try {
      const { data } = await api.post('/auth/login', { email, password });
      setUser(data);
      setMessage('Đăng nhập thành công');
      navigate('/lecturers');
    } catch (error: any) {
      setMessage(error?.response?.data?.message ?? 'Đăng nhập thất bại');
    }
  }

  return (
    <div className="page form-page fade-up">
      <Card>
        <SectionTitle eyebrow="Auth" title="Đăng nhập" description="Chỉ sinh viên và quản trị viên trong hệ thống." />
        <form className="form" onSubmit={onSubmit}>
          <label>Email</label>
          <Input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
          <label>Mật khẩu</label>
          <Input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
          <Button type="submit">Đăng nhập</Button>
          <small>{message}</small>
        </form>
      </Card>
    </div>
  );
}
