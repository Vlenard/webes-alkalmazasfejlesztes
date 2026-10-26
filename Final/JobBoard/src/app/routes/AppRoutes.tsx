import { Routes, Route } from 'react-router-dom'
import { MainLayout } from '../../shared/layouts/MainLayout'
import HomePage from '../../modules/home/pages/HomePage'
import JobsPage from '../../modules/jobs/pages/JobsPage'
import JobDetailPage from '../../modules/jobs/pages/JobDetailPage'
import LoginPage from '../../modules/auth/pages/LoginPage'
import RegisterPage from '../../modules/auth/pages/RegisterPage'
import { ProtectedRoute } from '../../modules/auth/components/ProtectedRoute'
import MyApplicationsPage from '../../modules/jobs/pages/MyApplicationsPage'
import EmployerDashboardPage from '../../modules/jobs/pages/EmployerDashboardPage'
import NotFoundPage from '../../shared/components/NotFoundPage'

export const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<MainLayout />}>
        <Route index element={<HomePage />} />
        <Route path="jobs" element={<JobsPage />} />
        <Route path="jobs/:id" element={<JobDetailPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
        <Route
          path="my-applications"
          element={
            <ProtectedRoute allowedRoles={['APPLICANT']}>
              <MyApplicationsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="employer"
          element={
            <ProtectedRoute allowedRoles={['COMPANY_MANAGER']}>
              <EmployerDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
