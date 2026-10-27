import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'

export default function HomePage() {
  const { isAuthenticated, isLoading, user } = useAuth()

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <span className="loading loading-spinner loading-lg text-primary"></span>
      </div>
    )
  }

  if (isAuthenticated) {
    return (
      <Navigate
        to={user?.role === 'COMPANY_MANAGER' ? '/employer' : '/jobs'}
        replace
      />
    )
  }

  return (
    <div className="hero bg-base-100 rounded-box shadow-md my-8 p-8">
      <div className="hero-content text-center">
        <div className="max-w-md">
          <h1 className="text-4xl font-bold">Üdvözöllek a JobBoard-on!</h1>
          <p className="py-6 text-base-content/80">
            Találd meg a számodra legmegfelelőbb pozíciót a legfrissebb állásajánlataink között.
          </p>
          <Link to="/jobs" className="btn btn-primary py-2 px-4">
            Állások böngészése
          </Link>
        </div>
      </div>
    </div>
  )
}
