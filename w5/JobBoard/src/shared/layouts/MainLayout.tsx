import { NavLink, Outlet } from 'react-router-dom'

export const MainLayout = () => {
  return (
    <div className="min-h-screen bg-base-200 flex flex-col">
      <header className="sticky top-0 z-50 navbar bg-base-100 shadow-sm border-b border-base-300 px-4">
        <div className="flex-1">
          <NavLink to="/" className="btn btn-ghost text-xl font-bold">
            JobBoard
          </NavLink>
        </div>
        <nav className="flex-none">
          <ul className="menu menu-horizontal px-1 gap-1">
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
          </ul>
        </nav>
      </header>

      <main className="flex-1 container mx-auto p-4 max-w-4xl">
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
