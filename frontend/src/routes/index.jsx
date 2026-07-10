import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import Landing from '@/pages/public/Landing';
import Login from '@/pages/public/Login';
import Register from '@/pages/public/Register';
import Verify from '@/pages/public/Verify';
import ForgotPassword from '@/pages/public/ForgotPassword';
import ResetPassword from '@/pages/public/ResetPassword';
import StudentLayout from '@/layouts/StudentLayout';
import Dashboard from '@/pages/student/Dashboard';
import Lecturers from '@/pages/student/Lecturers';
import LecturerDetail from '@/pages/student/LecturerDetail';
import SubmitReview from '@/pages/student/SubmitReview';
import Profile from '@/pages/student/Profile';
import MyReviews from '@/pages/student/MyReviews';
import AdminLayout from '@/layouts/AdminLayout';
import AdminDashboard from '@/pages/admin/Dashboard';
import AdminUsers from '@/pages/admin/Users';
import AdminLecturers from '@/pages/admin/Lecturers';
import AdminFaculties from '@/pages/admin/Faculties';
import AdminSubjects from '@/pages/admin/Subjects';
import AdminReviews from '@/pages/admin/Reviews';
import AdminReports from '@/pages/admin/Reports';
import AdminToxicKeywords from '@/pages/admin/ToxicKeywords';

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
      <Route path="/verify" element={!isAuthenticated ? <Verify /> : <Navigate to={isAdmin ? "/admin" : "/student"} />} />
      <Route path="/forgot-password" element={!isAuthenticated ? <ForgotPassword /> : <Navigate to={isAdmin ? "/admin" : "/student"} />} />
      <Route path="/reset-password" element={!isAuthenticated ? <ResetPassword /> : <Navigate to={isAdmin ? "/admin" : "/student"} />} />

      {/* Student Routes */}
      <Route path="/student" element={isAuthenticated && isStudent ? <StudentLayout /> : <Navigate to="/login" />}>
        <Route index element={<Dashboard />} />
        <Route path="lecturers" element={<Lecturers />} />
        <Route path="lecturers/:id" element={<LecturerDetail />} />
        <Route path="lecturers/:id/review" element={<SubmitReview />} />
        <Route path="profile" element={<Profile />} />
        <Route path="reviews" element={<MyReviews />} />
        <Route path="favorites" element={<Placeholder title="Favorite Lecturers" />} />
        <Route path="settings" element={<Placeholder title="Settings" />} />
      </Route>
      
      {/* Admin Routes */}
      <Route path="/admin" element={isAuthenticated && isAdmin ? <AdminLayout /> : <Navigate to="/login" />}>
        <Route index element={<AdminDashboard />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="lecturers" element={<AdminLecturers />} />
        <Route path="faculties" element={<AdminFaculties />} />
        <Route path="subjects" element={<AdminSubjects />} />
        <Route path="reviews" element={<AdminReviews />} />
        <Route path="reports" element={<AdminReports />} />
        <Route path="toxic-keywords" element={<AdminToxicKeywords />} />
      </Route>
      
      {/* Catch all */}
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}
