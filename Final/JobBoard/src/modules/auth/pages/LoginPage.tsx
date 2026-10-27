import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export const LoginPage = () => {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setIsSubmitting(true)

    try {
      const response = await login({ email, password })
      
      const destination = response.user.role === 'COMPANY_MANAGER'
        ? '/employer'
        : (location.state?.from?.pathname || '/jobs')

      navigate(destination, { replace: true })
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('Váratlan hiba történt a bejelentkezés során.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex justify-center items-center py-8">
      <div className="card bg-base-100 w-full max-w-md shadow-md card-border">
        <div className="card-body">
          <h2 className="card-title text-2xl font-bold justify-center mb-2">Bejelentkezés</h2>
          <p className="text-center text-sm text-base-content/70 mb-4">
            Jelentkezz be a fiókodba az álláskereséshez vagy hirdetések kezeléséhez!
          </p>

          {error && (
            <div role="alert" className="alert alert-error text-sm mb-4">
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Email cím</span>
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="pelda@email.hu"
                className="input input-bordered w-full"
                autoComplete="email"
              />
            </div>

            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Jelszó</span>
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="input input-bordered w-full"
                autoComplete="current-password"
              />
            </div>

            <div className="pt-2">
              <button
                type="submit"
                disabled={isSubmitting}
                className="btn btn-primary py-2 px-4 w-full"
              >
                {isSubmitting ? (
                  <>
                    <span className="loading loading-spinner loading-sm"></span>
                    Bejelentkezés folyamatban...
                  </>
                ) : (
                  'Bejelentkezés'
                )}
              </button>
            </div>
          </form>

          <div className="divider my-4">vagy</div>

          <p className="text-center text-sm">
            Még nincs fiókod?{' '}
            <Link to="/register" className="link link-primary font-semibold">
              Regisztrálj itt!
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default LoginPage
