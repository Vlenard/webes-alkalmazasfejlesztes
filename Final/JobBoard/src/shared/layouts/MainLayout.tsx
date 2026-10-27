import { useState, useEffect } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../modules/auth/context/AuthContext'

export const MainLayout = () => {
  const { user, isAuthenticated, logout } = useAuth()
  const [drawerOpen, setDrawerOpen] = useState(false)
  const location = useLocation()

  // Close mobile drawer automatically if route changes
  const [prevPathname, setPrevPathname] = useState(location.pathname)
  if (location.pathname !== prevPathname) {
    setPrevPathname(location.pathname)
    setDrawerOpen(false)
  }

  // Close drawer on Escape key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setDrawerOpen(false)
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  const toggleDrawer = () => setDrawerOpen((prev) => !prev)
  const closeDrawer = () => setDrawerOpen(false)

  return (
    <div className="drawer min-h-screen">
      <input
        id="main-drawer"
        type="checkbox"
        className="drawer-toggle"
        checked={drawerOpen}
        onChange={(e) => setDrawerOpen(e.target.checked)}
      />

      <div className="drawer-content flex flex-col min-h-screen bg-base-200">
        {/* Sticky top navbar */}
        <header className="sticky top-0 z-30 navbar bg-base-100 shadow-sm border-b border-base-300 px-3 sm:px-6">
          {/* Mobile hamburger menu toggle button */}
          <div className="flex-none lg:hidden">
            <button
              type="button"
              aria-label="Menü megnyitása"
              className="btn btn-ghost border border-base-300 rounded-lg hover:bg-base-200 flex items-center justify-center cursor-pointer text-base-content"
              style={{
                width: '2.75rem',
                height: '2.75rem',
                minHeight: '2.75rem',
                padding: '0.375rem',
                boxSizing: 'border-box',
              }}
              onClick={toggleDrawer}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth={2.5}
                strokeLinecap="round"
                strokeLinejoin="round"
                style={{ width: '1.5rem', height: '1.5rem', display: 'block' }}
              >
                <line x1="4" y1="6" x2="20" y2="6" />
                <line x1="4" y1="12" x2="20" y2="12" />
                <line x1="4" y1="18" x2="20" y2="18" />
              </svg>
            </button>
          </div>

          {/* Brand title */}
          <div className="flex-1 px-2">
            <NavLink
              to={isAuthenticated ? (user?.role === 'COMPANY_MANAGER' ? '/employer' : '/jobs') : '/'}
              className="btn btn-ghost text-xl font-bold py-2 px-4"
            >
              JobBoard
            </NavLink>
          </div>

          {/* Desktop navbar menu + auth buttons */}
          <div className="hidden lg:flex items-center gap-4">
            <nav>
              <ul className="menu menu-horizontal px-1 gap-1 text-sm">
                {!isAuthenticated && (
                  <li>
                    <NavLink
                      to="/"
                      end
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2 px-4' : 'py-2 px-4')}
                    >
                      Kezdőlap
                    </NavLink>
                  </li>
                )}
                <li>
                  <NavLink
                    to="/jobs"
                    className={({ isActive }) => (isActive ? 'active font-semibold py-2 px-4' : 'py-2 px-4')}
                  >
                    Álláslehetőségek
                  </NavLink>
                </li>
                {user?.role === 'APPLICANT' && (
                  <li>
                    <NavLink
                      to="/my-applications"
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2 px-4' : 'py-2 px-4')}
                    >
                      Jelentkezéseim
                    </NavLink>
                  </li>
                )}
                {user?.role === 'COMPANY_MANAGER' && (
                  <li>
                    <NavLink
                      to="/employer"
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2 px-4' : 'py-2 px-4')}
                    >
                      Céges Kezelőpult
                    </NavLink>
                  </li>
                )}
                {isAuthenticated && (
                  <li>
                    <NavLink
                      to="/profile"
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2 px-4' : 'py-2 px-4')}
                    >
                      Profilom
                    </NavLink>
                  </li>
                )}
              </ul>
            </nav>

            <div className="flex items-center gap-2">
              {!isAuthenticated || !user ? (
                <>
                  <NavLink to="/login" className="btn btn-ghost py-2 px-4">
                    Belépés
                  </NavLink>
                  <NavLink to="/register" className="btn btn-primary py-2 px-4">
                    Regisztráció
                  </NavLink>
                </>
              ) : (
                <>
                  <NavLink
                    to="/profile"
                    className={({ isActive }) =>
                      `flex items-center gap-2 px-3 py-1.5 rounded-lg border border-base-300 transition-colors ${
                        isActive ? 'bg-base-200 border-primary font-semibold' : 'hover:bg-base-200'
                      }`
                    }
                    title="Profil megtekintése és szerkesztése"
                  >
                    <span
                      className="text-sm font-medium truncate max-w-[180px]"
                      title={user.name}
                    >
                      {user.name}
                    </span>
                    <span className="badge badge-ghost py-1 px-2.5 text-xs h-auto shrink-0">
                      {user.role === 'COMPANY_MANAGER' ? 'Céges kezelő' : 'Jelentkező'}
                    </span>
                  </NavLink>
                  <button onClick={logout} className="btn btn-outline py-2 px-4 shrink-0">
                    Kilépés
                  </button>
                </>
              )}
            </div>
          </div>
        </header>

        {/* Main page content */}
        <main className="flex-1 max-w-6xl w-full mx-auto px-3 sm:px-6 py-4">
          <Outlet />
        </main>

        {/* Footer */}
        <footer className="footer footer-center p-4 bg-base-100 text-base-content border-t border-base-300">
          <aside>
            <p>© 2026 JobBoard - Minden jog fenntartva</p>
          </aside>
        </footer>
      </div>

      {/* Mobile sidebar drawer */}
      <div className="drawer-side z-50">
        <label
          aria-label="Menü bezárása"
          className="drawer-overlay"
          style={{ padding: 0 }}
          onClick={closeDrawer}
        ></label>
        <div className="bg-base-100 min-h-full w-80 p-4 flex flex-col justify-between text-base-content shadow-xl">
          <div className="flex flex-col gap-4">
            {/* Drawer Header with Title and Close Button */}
            <div className="flex items-center justify-between pb-3 border-b border-base-300">
              <NavLink
                to={isAuthenticated ? (user?.role === 'COMPANY_MANAGER' ? '/employer' : '/jobs') : '/'}
                className="text-xl font-bold tracking-tight px-2"
                onClick={closeDrawer}
              >
                JobBoard
              </NavLink>
              <button
                type="button"
                aria-label="Menü bezárása"
                className="btn btn-ghost border border-base-300 rounded-lg hover:bg-base-200 flex items-center justify-center cursor-pointer text-base-content"
                style={{
                  width: '2.5rem',
                  height: '2.5rem',
                  minHeight: '2.5rem',
                  padding: '0.375rem',
                  boxSizing: 'border-box',
                }}
                onClick={closeDrawer}
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth={2.5}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  style={{ width: '1.25rem', height: '1.25rem', display: 'block' }}
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
              </button>
            </div>

            {/* Authenticated user profile card in drawer */}
            {isAuthenticated && user && (
              <NavLink
                to="/profile"
                onClick={closeDrawer}
                className="bg-base-200 p-3 rounded-box border border-base-300 flex flex-col gap-1 hover:border-primary transition-colors cursor-pointer"
                title="Profil megtekintése és szerkesztése"
              >
                <div className="flex items-center justify-between">
                  <span className="text-xs text-base-content/60 font-semibold uppercase tracking-wider">
                    Bejelentkezve
                  </span>
                  <span className="text-xs text-primary font-medium">Szerkesztés →</span>
                </div>
                <div className="font-semibold text-base truncate" title={user.name}>
                  {user.name}
                </div>
                <div className="flex items-center justify-between mt-1 gap-2">
                  <span className="text-xs text-base-content/70 truncate">{user.email}</span>
                  <span className="badge badge-ghost text-xs shrink-0">
                    {user.role === 'COMPANY_MANAGER' ? 'Céges kezelő' : 'Jelentkező'}
                  </span>
                </div>
              </NavLink>
            )}

            {/* Mobile navigation links */}
            <nav>
              <ul className="menu menu-vertical w-full p-0 gap-1 text-base">
                {!isAuthenticated && (
                  <li>
                    <NavLink
                      to="/"
                      end
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2.5 px-4' : 'py-2.5 px-4')}
                      onClick={closeDrawer}
                    >
                      Kezdőlap
                    </NavLink>
                  </li>
                )}
                <li>
                  <NavLink
                    to="/jobs"
                    className={({ isActive }) => (isActive ? 'active font-semibold py-2.5 px-4' : 'py-2.5 px-4')}
                    onClick={closeDrawer}
                  >
                    Álláslehetőségek
                  </NavLink>
                </li>
                {user?.role === 'APPLICANT' && (
                  <li>
                    <NavLink
                      to="/my-applications"
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2.5 px-4' : 'py-2.5 px-4')}
                      onClick={closeDrawer}
                    >
                      Jelentkezéseim
                    </NavLink>
                  </li>
                )}
                {user?.role === 'COMPANY_MANAGER' && (
                  <li>
                    <NavLink
                      to="/employer"
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2.5 px-4' : 'py-2.5 px-4')}
                      onClick={closeDrawer}
                    >
                      Céges Kezelőpult
                    </NavLink>
                  </li>
                )}
                {isAuthenticated && (
                  <li>
                    <NavLink
                      to="/profile"
                      className={({ isActive }) => (isActive ? 'active font-semibold py-2.5 px-4' : 'py-2.5 px-4')}
                      onClick={closeDrawer}
                    >
                      Profilom (Adatmódosítás)
                    </NavLink>
                  </li>
                )}
              </ul>
            </nav>
          </div>

          {/* Drawer auth actions */}
          <div className="pt-4 border-t border-base-300 flex flex-col gap-2">
            {!isAuthenticated || !user ? (
              <>
                <NavLink
                  to="/login"
                  className="btn btn-ghost w-full justify-center"
                  onClick={closeDrawer}
                >
                  Belépés
                </NavLink>
                <NavLink
                  to="/register"
                  className="btn btn-primary w-full justify-center"
                  onClick={closeDrawer}
                >
                  Regisztráció
                </NavLink>
              </>
            ) : (
              <button
                type="button"
                onClick={() => {
                  closeDrawer()
                  logout()
                }}
                className="btn btn-outline btn-error w-full justify-center"
              >
                Kilépés
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
