import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import type { UserRole } from '../types/auth'

export const RegisterPage = () => {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState<UserRole>('APPLICANT')
  const [companyName, setCompanyName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    if (role === 'COMPANY_MANAGER' && !companyName.trim()) {
      setError('Céges regisztráció esetén a cégnév megadása kötelező!')
      return
    }

    setIsSubmitting(true)

    try {
      const response = await register({
        name: name.trim(),
        email: email.trim(),
        password,
        role,
        companyName: role === 'COMPANY_MANAGER' ? companyName.trim() : undefined,
      })

      const destination = response.user.role === 'COMPANY_MANAGER' ? '/employer' : '/jobs'
      navigate(destination, { replace: true })
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('Váratlan hiba történt a regisztráció során.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="flex justify-center items-center py-8">
      <div className="card bg-base-100 w-full max-w-lg shadow-md card-border">
        <div className="card-body">
          <h2 className="card-title text-2xl font-bold justify-center mb-2">Fiók létrehozása</h2>
          <p className="text-center text-sm text-base-content/70 mb-4">
            Csatlakozz a JobBoard közösségéhez jelentkezőként vagy munkáltatóként!
          </p>

          {error && (
            <div role="alert" className="alert alert-error text-sm mb-4">
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Célválasztás (Jelentkező vs Céges kezelő) */}
            <fieldset className="fieldset">
              <legend className="fieldset-legend font-semibold text-base mb-2">
                Milyen céllal regisztrálsz?
              </legend>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full">
                <label
                  className={`flex flex-col py-2 px-4 border rounded-xl cursor-pointer transition-all ${
                    role === 'APPLICANT'
                      ? 'border-primary bg-primary/5 shadow-xs'
                      : 'border-base-300 hover:border-base-content/40'
                  }`}
                >
                  <div className="flex items-center gap-2 mb-1">
                    <input
                      type="radio"
                      name="role"
                      value="APPLICANT"
                      checked={role === 'APPLICANT'}
                      onChange={() => setRole('APPLICANT')}
                      className="radio radio-primary radio-sm"
                    />
                    <span className="font-bold text-sm">Jelentkező</span>
                  </div>
                  <span className="text-xs text-base-content/70 pl-6">
                    Álláskeresőként szeretnék jelentkezni meghirdetett munkákra
                  </span>
                </label>

                <label
                  className={`flex flex-col py-2 px-4 border rounded-xl cursor-pointer transition-all ${
                    role === 'COMPANY_MANAGER'
                      ? 'border-primary bg-primary/5 shadow-xs'
                      : 'border-base-300 hover:border-base-content/40'
                  }`}
                >
                  <div className="flex items-center gap-2 mb-1">
                    <input
                      type="radio"
                      name="role"
                      value="COMPANY_MANAGER"
                      checked={role === 'COMPANY_MANAGER'}
                      onChange={() => setRole('COMPANY_MANAGER')}
                      className="radio radio-primary radio-sm"
                    />
                    <span className="font-bold text-sm">Céges kezelő</span>
                  </div>
                  <span className="text-xs text-base-content/70 pl-6">
                    Céges kezelőként szeretnék álláshirdetéseket feladni és jelentkezőket bírálni
                  </span>
                </label>
              </div>
            </fieldset>

            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Teljes név</span>
              </label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Pl. Kovács Béla"
                className="input input-bordered w-full"
                autoComplete="name"
              />
            </div>

            {role === 'COMPANY_MANAGER' && (
              <div className="form-control">
                <label className="label py-2 px-4">
                  <span className="label-text font-medium">
                    Cég neve <span className="text-error">*</span>
                  </span>
                </label>
                <input
                  type="text"
                  required
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                  placeholder="Pl. Tech Innovations Kft."
                  className="input input-bordered w-full"
                  autoComplete="organization"
                />
              </div>
            )}

            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Email cím</span>
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="kovacs.bela@example.hu"
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
                placeholder="Legalább 6 karakter"
                className="input input-bordered w-full"
                autoComplete="new-password"
                minLength={6}
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
                    Regisztráció folyamatban...
                  </>
                ) : (
                  'Regisztráció'
                )}
              </button>
            </div>
          </form>

          <div className="divider my-4">vagy</div>

          <p className="text-center text-sm">
            Már van fiókod?{' '}
            <Link to="/login" className="link link-primary font-semibold">
              Jelentkezz be!
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default RegisterPage
