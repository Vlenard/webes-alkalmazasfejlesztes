import { useState } from 'react'
import { jobService } from '../services/jobService'

interface ApplicationFormProps {
  jobId: number
  jobTitle: string
  onClose?: () => void
  onSuccess?: () => void
}

export const ApplicationForm = ({ jobId, jobTitle, onClose, onSuccess }: ApplicationFormProps) => {
  const [applicantName, setApplicantName] = useState('')
  const [applicantEmail, setApplicantEmail] = useState('')
  const [resumeFile, setResumeFile] = useState<File | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitted, setIsSubmitted] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMessage(null)

    if (!applicantName.trim()) {
      setErrorMessage('Kérjük, add meg a teljes nevedet!')
      return
    }

    if (!applicantEmail.trim() || !applicantEmail.includes('@')) {
      setErrorMessage('Kérjük, adj meg egy érvényes email címet!')
      return
    }

    setIsSubmitting(true)

    try {
      await jobService.submitApplication({
        jobId,
        applicantName,
        applicantEmail,
        resumeFileName: resumeFile ? resumeFile.name : 'placeholder_resume.pdf',
      })
      setIsSubmitted(true)
      if (onSuccess) {
        onSuccess()
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message)
      } else {
        setErrorMessage('Hiba történt a jelentkezés beküldése során.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isSubmitted) {
    return (
      <div className="card bg-base-100 border border-base-300 p-6 text-center space-y-4">
        <div className="text-success text-5xl">✓</div>
        <h3 className="text-xl font-bold">Sikeres jelentkezés!</h3>
        <p className="text-base-content/80 text-sm">
          Köszönjük a jelentkezésedet a <strong>{jobTitle}</strong> pozícióra! Hamarosan felvesszük veled a kapcsolatot a(z) <strong>{applicantEmail}</strong> címen.
        </p>
        <div className="pt-2 flex justify-center gap-2">
          {onClose && (
            <button type="button" className="btn btn-primary btn-sm" onClick={onClose}>
              Bezárás
            </button>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="card bg-base-100 border border-base-300 shadow-sm p-6">
      <h3 className="text-xl font-bold mb-1">Jelentkezés az állásra</h3>
      <p className="text-sm text-base-content/70 mb-4">{jobTitle}</p>

      {errorMessage && (
        <div className="alert alert-error text-sm mb-4">
          <span>{errorMessage}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Név mező */}
        <div className="form-control">
          <label className="label">
            <span className="label-text font-medium">Teljes név *</span>
          </label>
          <input
            type="text"
            required
            placeholder="pl. Kiss János"
            className="input input-bordered w-full"
            value={applicantName}
            onChange={(e) => setApplicantName(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        {/* Email mező */}
        <div className="form-control">
          <label className="label">
            <span className="label-text font-medium">Email cím *</span>
          </label>
          <input
            type="email"
            required
            placeholder="pl. kiss.janos@example.com"
            className="input input-bordered w-full"
            value={applicantEmail}
            onChange={(e) => setApplicantEmail(e.target.value)}
            disabled={isSubmitting}
          />
        </div>

        {/* Önéletrajz feltöltés (placeholder) */}
        <div className="form-control">
          <label className="label">
            <span className="label-text font-medium">Önéletrajz feltöltése (opcionális / placeholder)</span>
          </label>
          <input
            type="file"
            accept=".pdf,.doc,.docx"
            className="file-input file-input-bordered w-full"
            onChange={(e) => {
              if (e.target.files && e.target.files.length > 0) {
                setResumeFile(e.target.files[0])
              }
            }}
            disabled={isSubmitting}
          />
          <label className="label">
            <span className="label-text-alt text-base-content/60">
              {resumeFile ? `Kiválasztva: ${resumeFile.name}` : 'Támogatott formátumok: PDF, DOC, DOCX (szimulált feltöltés)'}
            </span>
          </label>
        </div>

        {/* Gombok */}
        <div className="flex justify-end gap-2 pt-2">
          {onClose && (
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Mégsem
            </button>
          )}
          <button
            type="submit"
            className="btn btn-primary btn-sm"
            disabled={isSubmitting}
          >
            {isSubmitting ? (
              <>
                <span className="loading loading-spinner loading-xs"></span>
                Küldés folyamatban...
              </>
            ) : (
              'Jelentkezés elküldése'
            )}
          </button>
        </div>
      </form>
    </div>
  )
}
