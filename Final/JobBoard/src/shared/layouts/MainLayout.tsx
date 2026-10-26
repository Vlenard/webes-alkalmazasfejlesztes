import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../modules/auth/context/AuthContext'

export const MainLayout = () => {
  const { user, isAuthenticated, logout } = useAuth()

  return (
    <div className="min-h-screen bg-base-200 flex flex-col">
      <header className="sticky top-0 z-50 navbar bg-base-100 shadow-sm border-b border-base-300 px-3 sm:px-6 py-2 flex-wrap gap-y-2 justify-between">
        <div className="flex items-center flex-wrap gap-1 sm:gap-4">
          <NavLink to="/" className="btn btn-ghost text-xl font-bold px-2">
            JobBoard
          </NavLink>
          <nav>
            <ul className="menu menu-horizontal flex-wrap px-0 sm:px-1 gap-1 text-sm">
              <li>
                <NavLink
                  to="/"
                  end
                  className={({ isActive }) => (isActive ? 'active font-semibold' : '')}
                >
                  Kezdőlap
                </NavLink>
              </li>
              <li>
                <NavLink
                  to="/jobs"
                  className={({ isActive }) => (isActive ? 'active font-semibold' : '')}
                >
                  Álláslehetőségek
                </NavLink>
              </li>
              {user?.role === 'APPLICANT' && (
                <li>
                  <NavLink
                    to="/my-applications"
                    className={({ isActive }) => (isActive ? 'active font-semibold' : '')}
                  >
                    Jelentkezéseim
                  </NavLink>
                </li>
              )}
              {user?.role === 'COMPANY_MANAGER' && (
                <li>
                  <NavLink
                    to="/employer"
                    className={({ isActive }) => (isActive ? 'active font-semibold' : '')}
                  >
                    Céges Kezelőpult
                  </NavLink>
                </li>
              )}
            </ul>
          </nav>
        </div>

        <div className="flex items-center flex-wrap gap-2 ml-auto">
          {!isAuthenticated || !user ? (
            <>
              <NavLink to="/login" className="btn btn-ghost btn-sm">
                Belépés
              </NavLink>
              <NavLink to="/register" className="btn btn-primary btn-sm">
                Regisztráció
              </NavLink>
            </>
          ) : (
            <>
              <div className="flex items-center gap-1.5 sm:gap-2">
                <span
                  className="text-sm font-medium truncate max-w-[120px] sm:max-w-[200px]"
                  title={user.name}
                >
                  {user.name}
                </span>
                <span
                  className={`badge badge-sm shrink-0 ${
                    user.role === 'COMPANY_MANAGER' ? 'badge-primary' : 'badge-neutral'
                  }`}
                >
                  {user.role === 'COMPANY_MANAGER' ? 'Céges kezelő' : 'Jelentkező'}
                </span>
              </div>
              <button onClick={logout} className="btn btn-outline btn-sm shrink-0">
                Kilépés
              </button>
            </>
          )}
        </div>
      </header>

      <main className="flex-1 max-w-6xl w-full mx-auto px-3 sm:px-6 py-4">
        <Outlet />
      </main>

      <footer className="footer footer-center p-4 bg-base-100 text-base-content border-t border-base-300">
        <aside>
          <p>© 2026 JobBoard - Minden jog fenntartva</p>
        </aside>
      </footer>
    </div>
  )
}
