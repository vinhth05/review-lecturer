import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import MainLayout from './layouts/MainLayout';
import RoleGuard from './routes/RoleGuard';

const Home = React.lazy(() => import('./pages/Home'));
const Login = React.lazy(() => import('./pages/Login'));
const Register = React.lazy(() => import('./pages/Register'));
const VerifyOtp = React.lazy(() => import('./pages/VerifyOtp'));
const ForgotPassword = React.lazy(() => import('./pages/ForgotPassword'));
const ResetPassword = React.lazy(() => import('./pages/ResetPassword'));
const Dashboard = React.lazy(() => import('./pages/Dashboard'));
const Profile = React.lazy(() => import('./pages/Profile'));
const AdminDashboard = React.lazy(() => import('./pages/AdminDashboard'));
const FacultyManagement = React.lazy(() => import('./pages/FacultyManagement'));
const SubjectManagement = React.lazy(() => import('./pages/SubjectManagement'));
const LecturerManagement = React.lazy(() => import('./pages/LecturerManagement'));
const Statistics = React.lazy(() => import('./pages/Statistics'));
const ToxicKeywords = React.lazy(() => import('./pages/ToxicKeywords'));
const NotFound = React.lazy(() => import('./pages/NotFound'));
const Unauthorized = React.lazy(() => import('./pages/Unauthorized'));

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <React.Suspense fallback={<div>Loading...</div>}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/verify-otp" element={<VerifyOtp />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />
            <Route path="/unauthorized" element={<Unauthorized />} />

            <Route path="/" element={<MainLayout />}>
              <Route index element={<Home />} />
              <Route path="dashboard" element={
                <RoleGuard allowedRoles={['ROLE_STUDENT']}>
                  <Dashboard />
                </RoleGuard>
              } />
              <Route path="profile" element={
                <RoleGuard>
                  <Profile />
                </RoleGuard>
              } />
              
              {/* Admin Routes */}
              <Route path="admin" element={
                <RoleGuard allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}>
                  <AdminDashboard />
                </RoleGuard>
              } />
              <Route path="admin/faculties" element={<RoleGuard allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}><FacultyManagement /></RoleGuard>} />
              <Route path="admin/subjects" element={<RoleGuard allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}><SubjectManagement /></RoleGuard>} />
              <Route path="admin/lecturers" element={<RoleGuard allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}><LecturerManagement /></RoleGuard>} />
              <Route path="admin/statistics" element={<RoleGuard allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}><Statistics /></RoleGuard>} />
              <Route path="admin/toxic-keywords" element={<RoleGuard allowedRoles={['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']}><ToxicKeywords /></RoleGuard>} />

              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </React.Suspense>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;

