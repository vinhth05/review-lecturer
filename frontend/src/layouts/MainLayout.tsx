import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Button } from '@/components/Button';
import { useAuthStore } from '@/store/authStore';
import { useUiStore } from '@/store/uiStore';
import './MainLayout.css';

export function MainLayout() {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const darkMode = useUiStore((state) => state.darkMode);
  const toggleDarkMode = useUiStore((state) => state.toggleDarkMode);
  const navigate = useNavigate();

  return (
    <div className="shell">
      <header className="topbar">
        <Link to="/" className="brand">
          <span>CTU</span>
          <small>Lecturer Review</small>
        </Link>
        <nav className="menu">
          <NavLink to="/lecturers">Giảng viên</NavLink>
          {user ? <NavLink to="/profile">Hồ sơ</NavLink> : null}
          {user && user.role !== 'STUDENT' ? <NavLink to="/admin">Admin</NavLink> : null}
        </nav>
        <div className="actions">
          <Button tone="ghost" onClick={toggleDarkMode}>
            {darkMode ? 'Light' : 'Dark'}
          </Button>
          {user ? (
            <Button
              tone="secondary"
              onClick={() => {
                logout();
                navigate('/login');
              }}
            >
              Đăng xuất
            </Button>
          ) : (
            <>
              <Button tone="ghost" onClick={() => navigate('/login')}>
                Đăng nhập
              </Button>
              <Button onClick={() => navigate('/register')}>Đăng ký</Button>
            </>
          )}
        </div>
      </header>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
