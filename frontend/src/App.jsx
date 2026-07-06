import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ThemeProvider } from './contexts/ThemeContext';
import MainLayout from './layouts/MainLayout';
import RoleGuard from './routes/RoleGuard';

import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import VerifyOtp from './pages/VerifyOtp';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import AdminDashboard from './pages/AdminDashboard';
import FacultyManagement from './pages/FacultyManagement';
import SubjectManagement from './pages/SubjectManagement';
import LecturerManagement from './pages/LecturerManagement';
import Statistics from './pages/Statistics';
import ToxicKeywords from './pages/ToxicKeywords';
import NotFound from './pages/NotFound';
import Unauthorized from './pages/Unauthorized';

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
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
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;

