import { lazy, Suspense } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';

const Landing = lazy(() => import('@/pages/public/Landing'));
const Login = lazy(() => import('@/pages/public/Login'));
const Register = lazy(() => import('@/pages/public/Register'));
const Verify = lazy(() => import('@/pages/public/Verify'));
const ForgotPassword = lazy(() => import('@/pages/public/ForgotPassword'));
const ResetPassword = lazy(() => import('@/pages/public/ResetPassword'));
const StudentLayout = lazy(() => import('@/layouts/StudentLayout'));
const Dashboard = lazy(() => import('@/pages/student/Dashboard'));
const Lecturers = lazy(() => import('@/pages/student/Lecturers'));
const LecturerDetail = lazy(() => import('@/pages/student/LecturerDetail'));
const SubmitReview = lazy(() => import('@/pages/student/SubmitReview'));
const Profile = lazy(() => import('@/pages/student/Profile'));
const MyReviews = lazy(() => import('@/pages/student/MyReviews'));
const AdminLayout = lazy(() => import('@/layouts/AdminLayout'));
const AdminDashboard = lazy(() => import('@/pages/admin/Dashboard'));
const AdminUsers = lazy(() => import('@/pages/admin/Users'));
const AdminLecturers = lazy(() => import('@/pages/admin/Lecturers'));
const AdminFaculties = lazy(() => import('@/pages/admin/Faculties'));
const AdminSubjects = lazy(() => import('@/pages/admin/Subjects'));
const AdminReviews = lazy(() => import('@/pages/admin/Reviews'));
const AdminReports = lazy(() => import('@/pages/admin/Reports'));
const AdminToxicKeywords = lazy(() => import('@/pages/admin/ToxicKeywords'));

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
    <Suspense fallback={<Placeholder title="Loading..." />}>
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
    </Suspense>
  );
}
