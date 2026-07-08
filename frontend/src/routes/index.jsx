import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import Landing from '@/pages/public/Landing';
import Login from '@/pages/public/Login';
import Register from '@/pages/public/Register';
import StudentLayout from '@/layouts/StudentLayout';
import Dashboard from '@/pages/student/Dashboard';
import Lecturers from '@/pages/student/Lecturers';
import LecturerDetail from '@/pages/student/LecturerDetail';
import SubmitReview from '@/pages/student/SubmitReview';
import Profile from '@/pages/student/Profile';
import AdminLayout from '@/layouts/AdminLayout';

// We'll import pages here as we build them
// For now, placeholder components
const Placeholder = ({ title }) => (
  <div className="flex items-center justify-center min-h-screen bg-background text-foreground">
    <h1 className="text-2xl font-bold">{title} (WIP)</h1>
  </div>
);

export default function AppRoutes() {
  const { isAuthenticated, isStudent, isAdmin } = useAuth();

  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<Landing />} />
      <Route path="/login" element={!isAuthenticated ? <Login /> : <Navigate to={isAdmin ? "/admin" : "/student"} />} />
      <Route path="/register" element={!isAuthenticated ? <Register /> : <Navigate to={isAdmin ? "/admin" : "/student"} />} />

      {/* Student Routes */}
      <Route path="/student" element={isAuthenticated && isStudent ? <StudentLayout /> : <Navigate to="/login" />}>
        <Route index element={<Dashboard />} />
        <Route path="lecturers" element={<Lecturers />} />
        <Route path="lecturers/:id" element={<LecturerDetail />} />
        <Route path="lecturers/:id/review" element={<SubmitReview />} />
        <Route path="profile" element={<Profile />} />
        <Route path="reviews" element={<Placeholder title="My Reviews" />} />
        <Route path="favorites" element={<Placeholder title="Favorite Lecturers" />} />
        <Route path="settings" element={<Placeholder title="Settings" />} />
      </Route>
      
      {/* Admin Routes */}
      <Route path="/admin" element={isAuthenticated && isAdmin ? <AdminLayout /> : <Navigate to="/login" />}>
        <Route index element={<Placeholder title="Admin Dashboard" />} />
        <Route path="users" element={<Placeholder title="Users Management" />} />
        <Route path="lecturers" element={<Placeholder title="Lecturers Management" />} />
        <Route path="faculties" element={<Placeholder title="Faculties Management" />} />
        <Route path="subjects" element={<Placeholder title="Subjects Management" />} />
        <Route path="reviews" element={<Placeholder title="Reviews Moderation" />} />
        <Route path="reports" element={<Placeholder title="Reports Management" />} />
        <Route path="toxic-keywords" element={<Placeholder title="Toxic Keywords" />} />
      </Route>
      
      {/* Catch all */}
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}
