import { Routes, Route } from 'react-router-dom'
import { MainLayout } from '../../shared/layouts/MainLayout'
import HomePage from '../../modules/home/pages/HomePage'
import JobsPage from '../../modules/jobs/pages/JobsPage'
import JobDetailPage from '../../modules/jobs/pages/JobDetailPage'
import NotFoundPage from '../../shared/components/NotFoundPage'

export const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<MainLayout />}>
        <Route index element={<HomePage />} />
        <Route path="jobs" element={<JobsPage />} />
        <Route path="jobs/:id" element={<JobDetailPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
