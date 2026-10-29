import { useState, useEffect, type FormEvent } from 'react'
import { useAuth } from '../context/AuthContext'
import { SkillBadgeInput } from '../../../shared/components/SkillBadgeInput'
import { UserSummaryCard } from '../components/UserSummaryCard'

export const ProfilePage = () => {
  const { user, updateProfile } = useAuth()

  const [name, setName] = useState(user?.name || '')
  const [email, setEmail] = useState(user?.email || '')
  const [companyName, setCompanyName] = useState(user?.companyName || '')
  const [skills, setSkills] = useState<string[]>(user?.skills || [])

  // Password fields
  const [showPasswordSection, setShowPasswordSection] = useState(false)
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Keep form synchronized if user context updates
  useEffect(() => {
    if (user) {
      setName(user.name)
      setEmail(user.email)
      setCompanyName(user.companyName || '')
      setSkills(user.skills || [])
    }
  }, [user])

  const handleReset = () => {
    if (user) {
      setName(user.name)
      setEmail(user.email)
      setCompanyName(user.companyName || '')
      setSkills(user.skills || [])
    }
    setCurrentPassword('')
    setNewPassword('')
    setConfirmPassword('')
    setError(null)
    setSuccess(null)
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setSuccess(null)

    if (!name.trim()) {
      setError('A név megadása kötelező.')
      return
    }

    if (!email.trim() || !email.includes('@')) {
      setError('Érvényes e-mail cím megadása kötelező.')
      return
    }

    const isCompanyManager = user?.role === 'COMPANY_MANAGER'
    if (isCompanyManager && !companyName.trim()) {
      setError('Céges hirdető esetén a cégnév megadása kötelező.')
      return
    }

    // Password change validations if user entered any password field
    const wantsPasswordChange = !!(currentPassword || newPassword || confirmPassword)
    if (wantsPasswordChange) {
      if (!currentPassword) {
        setError('A jelszó módosításához kérjük, adja meg jelenlegi jelszavát.')
        return
      }
      if (!newPassword || newPassword.length < 6) {
        setError('Az új jelszónak legalább 6 karakter hosszúnak kell lennie.')
        return
      }
      if (newPassword !== confirmPassword) {
        setError('Az új jelszó és a megerősítés nem egyezik meg.')
        return
      }
    }

    setIsSubmitting(true)

    try {
      await updateProfile({
        name: name.trim(),
        email: email.trim(),
        companyName: isCompanyManager ? companyName.trim() : (companyName.trim() || null),
        currentPassword: wantsPasswordChange ? currentPassword : undefined,
        newPassword: wantsPasswordChange ? newPassword : undefined,
        skills: !isCompanyManager ? skills : undefined,
      })

      setSuccess('A profiladataidat és a beállításaidat sikeresen mentettük!')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setShowPasswordSection(false)
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('Nem sikerült a profiladatok frissítése. Kérjük, próbáld újra.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const isCompanyManager = user?.role === 'COMPANY_MANAGER'

  return (
    <div className="max-w-3xl mx-auto py-6 space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-base-content">Profil beállítások</h1>
        <p className="text-base-content/70 mt-1">
          Itt tekintheted meg és szerkesztheted a személyes és fiókadataidat.
        </p>
      </div>

      {/* User Summary Card */}
      <UserSummaryCard user={user} />

      {/* Alerts */}
      {success && (
        <div role="alert" className="alert alert-success shadow-sm">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5 shrink-0 stroke-current"
            fill="none"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <span className="text-sm font-medium">{success}</span>
        </div>
      )}

      {error && (
        <div role="alert" className="alert alert-error shadow-sm">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5 shrink-0 stroke-current"
            fill="none"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <span className="text-sm font-medium">{error}</span>
        </div>
      )}

      {/* Edit Form */}
      <form onSubmit={handleSubmit} className="card bg-base-100 border border-base-300 shadow-sm p-6 space-y-6">
        <div>
          <h3 className="text-lg font-semibold text-base-content">Személyes adatok</h3>
          <p className="text-xs text-base-content/60">Alapvető azonosító adatok a felületen.</p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {/* Teljes név */}
          <div className="form-control">
            <label className="label py-1">
              <span className="label-text font-medium">Teljes név *</span>
            </label>
            <input
              type="text"
              required
              className="input input-bordered w-full"
              value={name}
              onChange={(e) => setName(e.target.value)}
              disabled={isSubmitting}
            />
          </div>

          {/* E-mail cím */}
          <div className="form-control">
            <label className="label py-1">
              <span className="label-text font-medium">E-mail cím *</span>
            </label>
            <input
              type="email"
              required
              className="input input-bordered w-full"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={isSubmitting}
            />
          </div>
        </div>

        {/* Cégnév mező */}
        <div className="form-control">
          <label className="label py-1">
            <span className="label-text font-medium">
              Cégnév {isCompanyManager && '*'}
            </span>
          </label>
          <input
            type="text"
            required={isCompanyManager}
            placeholder={isCompanyManager ? 'pl. TechCorp Hungary Kft.' : 'pl. Jelenlegi munkahely (opcionális)'}
            className="input input-bordered w-full"
            value={companyName}
            onChange={(e) => setCompanyName(e.target.value)}
            disabled={isSubmitting}
          />
          <p className="text-xs text-base-content/60 mt-1.5 whitespace-normal break-words leading-relaxed">
            {isCompanyManager
              ? 'Céges hirdetőként ez a név jelenik meg az Ön által feladott álláshirdetéseknél.'
              : 'Opcionálisan megadható jelenlegi vagy korábbi munkahely/szervezet.'}
          </p>
        </div>

        {/* Szakmai készségek (Skillek) – kizárólag APPLICANT esetén */}
        {!isCompanyManager && (
          <div className="border-t border-base-300 pt-4 space-y-3">
            <div>
              <h3 className="text-lg font-semibold text-base-content">Szakmai készségek (Skillek)</h3>
              <p className="text-xs text-base-content/60">
                A megadott készségek alapján rangsoroljuk az állásajánlatokat és számítjuk ki az illeszkedési mutatót.
              </p>
            </div>

            <SkillBadgeInput
              skills={skills}
              onChange={setSkills}
              disabled={isSubmitting}
              placeholder="pl. React ismeret, TypeScript, Spring Boot..."
              helperText="A készségek hozzáadásához gépeld be az elnevezést és nyomj Entert vagy kattints a Hozzáadás gombra."
              suggestedSkills={[
                'React ismeret',
                'TypeScript',
                'JavaScript',
                'Node.js',
                'Java',
                'Spring Boot',
                'PostgreSQL',
                'Python',
                'Docker',
                'Figma',
              ]}
            />
          </div>
        )}

        {/* Jelszócsere szekció */}
        <div className="border-t border-base-300 pt-4">
          <div className="flex items-center justify-between mb-3">
            <div>
              <h3 className="text-base font-semibold text-base-content">Jelszó módosítása</h3>
              <p className="text-xs text-base-content/60">
                Opcionális. Ha nem kívánod megváltoztatni a jelszavadat, hagyd üresen.
              </p>
            </div>
            <button
              type="button"
              className="btn btn-sm btn-ghost"
              onClick={() => {
                setShowPasswordSection((prev) => !prev)
                if (showPasswordSection) {
                  setCurrentPassword('')
                  setNewPassword('')
                  setConfirmPassword('')
                }
              }}
            >
              {showPasswordSection ? 'Mégsem módosítom' : 'Jelszócsere'}
            </button>
          </div>

          {showPasswordSection && (
            <div className="bg-base-200/50 p-4 rounded-lg border border-base-300 space-y-4">
              <div className="form-control">
                <label className="label py-1">
                  <span className="label-text font-medium text-sm">Jelenlegi jelszó *</span>
                </label>
                <input
                  type="password"
                  placeholder="Add meg a mostani jelszavadat"
                  className="input input-bordered w-full"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  disabled={isSubmitting}
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="form-control">
                  <label className="label py-1">
                    <span className="label-text font-medium text-sm">Új jelszó * (min. 6 karakter)</span>
                  </label>
                  <input
                    type="password"
                    placeholder="Legalább 6 karakter"
                    className="input input-bordered w-full"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    disabled={isSubmitting}
                  />
                </div>

                <div className="form-control">
                  <label className="label py-1">
                    <span className="label-text font-medium text-sm">Új jelszó megerősítése *</span>
                  </label>
                  <input
                    type="password"
                    placeholder="Ismételd meg az új jelszót"
                    className="input input-bordered w-full"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    disabled={isSubmitting}
                  />
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col-reverse sm:flex-row items-center justify-end gap-3 pt-2 border-t border-base-300">
          <button
            type="button"
            className="btn btn-outline w-full sm:w-auto"
            onClick={handleReset}
            disabled={isSubmitting}
          >
            Visszaállítás
          </button>

          <button
            type="submit"
            className="btn btn-primary w-full sm:w-auto"
            disabled={isSubmitting}
          >
            {isSubmitting ? (
              <>
                <span className="loading loading-spinner loading-xs"></span>
                Mentés folyamatban...
              </>
            ) : (
              'Módosítások mentése'
            )}
          </button>
        </div>
      </form>
    </div>
  )
}

export default ProfilePage
