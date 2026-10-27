import { useState, useEffect, type ChangeEvent, type FormEvent } from 'react'
import { useAuth } from '../../auth/context/AuthContext'
import { jobService } from '../services/jobService'

interface ApplicationFormProps {
  jobId: number
  jobTitle: string
  onClose?: () => void
  onSuccess?: () => void
}

export const ApplicationForm = ({ jobId, jobTitle, onClose, onSuccess }: ApplicationFormProps) => {
  const { user } = useAuth()

  const [applicantName, setApplicantName] = useState(user?.name || '')
  const [applicantEmail, setApplicantEmail] = useState(user?.email || '')
  const [resumeFile, setResumeFile] = useState<File | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isSubmitted, setIsSubmitted] = useState(false)

  // Sync user details if loaded asynchronously
  useEffect(() => {
    if (user) {
      if (!applicantName && user.name) {
        setApplicantName(user.name)
      }
      if (!applicantEmail && user.email) {
        setApplicantEmail(user.email)
      }
    }
  }, [user, applicantName, applicantEmail])

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    setErrorMessage(null)
    const file = e.target.files?.[0]
    if (!file) {
      setResumeFile(null)
      return
    }

    const lowerName = file.name.toLowerCase()
    if (!lowerName.endsWith('.pdf') && !lowerName.endsWith('.docx')) {
      setErrorMessage('Csak PDF (.pdf) vagy DOCX (.docx) formátumú önéletrajz tölthető fel!')
      setResumeFile(null)
      e.target.value = ''
      return
    }

    setResumeFile(file)
  }

  const handleSubmit = async (e: FormEvent) => {
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

    if (!resumeFile) {
      setErrorMessage('Kérjük, tölts fel egy önéletrajzot (.pdf vagy .docx)!')
      return
    }

    const lowerName = resumeFile.name.toLowerCase()
    if (!lowerName.endsWith('.pdf') && !lowerName.endsWith('.docx')) {
      setErrorMessage('Csak PDF (.pdf) vagy DOCX (.docx) formátumú önéletrajz tölthető fel!')
      return
    }

    setIsSubmitting(true)

    try {
      await jobService.submitApplicationWithFile({
        jobId,
        applicantName: applicantName.trim(),
        applicantEmail: applicantEmail.trim(),
        resumeFile,
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
            <button type="button" className="btn btn-primary py-2 px-4" onClick={onClose}>
              Bezárás
            </button>
          )}
        </div>
      </div>
    )
  }

  return (
    <div className="card bg-base-100 border border-base-300 shadow-sm p-6">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-xl font-bold">Jelentkezés az állásra</h3>
          <p className="text-sm text-base-content/70">{jobTitle}</p>
        </div>
        {onClose && (
          <button
            type="button"
            className="btn btn-ghost py-2 px-4"
            onClick={onClose}
            aria-label="Bezárás"
          >
            ✕
          </button>
        )}
      </div>

      {errorMessage && (
        <div role="alert" className="alert alert-error text-sm mb-4">
          <span>{errorMessage}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Név mező */}
        <div className="form-control">
          <label className="label py-2 px-4">
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
          <label className="label py-2 px-4">
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

        {/* Önéletrajz feltöltés: kizárólag PDF vagy DOCX */}
        <div className="form-control">
          <label className="label py-2 px-4">
            <span className="label-text font-medium">Önéletrajz feltöltése (.pdf vagy .docx) *</span>
          </label>
          <input
            type="file"
            accept=".pdf,.docx"
            required
            className="file-input file-input-bordered w-full"
            onChange={handleFileChange}
            disabled={isSubmitting}
          />
          <label className="label py-2 px-4">
            <span className="label-text-alt text-base-content/60">
              {resumeFile
                ? `Kiválasztott fájl: ${resumeFile.name} (${Math.round(resumeFile.size / 1024)} KB)`
                : 'Kizárólag PDF (.pdf) és Word (.docx) dokumentum támogatott.'}
            </span>
          </label>
        </div>

        {/* Gombok */}
        <div className="flex justify-end gap-2 pt-2">
          {onClose && (
            <button
              type="button"
              className="btn btn-ghost py-2 px-4"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Mégsem
            </button>
          )}
          <button
            type="submit"
            className="btn btn-primary py-2 px-4"
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
