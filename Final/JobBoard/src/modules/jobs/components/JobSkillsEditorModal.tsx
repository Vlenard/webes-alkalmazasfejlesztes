import React, { useState, useEffect } from 'react'
import type { Job } from '../types/job'
import { jobService } from '../services/jobService'
import { SkillBadgeInput } from '../../../shared/components/SkillBadgeInput'

export interface JobSkillsEditorModalProps {
  job: Job | null
  isOpen: boolean
  onClose: () => void
  onSaved: (updatedJob: Job) => void
}

const DEFAULT_SUGGESTED_SKILLS = [
  'React ismeret',
  'TypeScript',
  'Tailwind CSS',
  'Node.js',
  'Java',
  'Spring Boot',
  'PostgreSQL',
  'Docker',
  'Kubernetes',
  'Python',
  'REST API',
  'Git',
]

/**
 * JobSkillsEditorModal component
 *
 * Symmetrical and responsive daisyUI modal allowing company managers (COMPANY_MANAGER)
 * to manage and update the required skills for an existing job posting.
 */
export const JobSkillsEditorModal: React.FC<JobSkillsEditorModalProps> = ({
  job,
  isOpen,
  onClose,
  onSaved,
}) => {
  const [skills, setSkills] = useState<string[]>([])
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Synchronize local skills state when a job is loaded or modal is opened
  useEffect(() => {
    if (isOpen && job) {
      setSkills(job.skills ? [...job.skills] : [])
      setError(null)
      setIsSaving(false)
    }
  }, [isOpen, job])

  // Handle ESC key press to close the modal safely
  useEffect(() => {
    if (!isOpen || !job) return

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !isSaving) {
        onClose()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [isOpen, job, isSaving, onClose])

  // Guard: if modal is closed or no job is provided, render nothing
  if (!isOpen || !job) {
    return null
  }

  const handleClose = () => {
    if (!isSaving) {
      setError(null)
      onClose()
    }
  }

  const handleSave = async (e?: React.FormEvent) => {
    if (e) {
      e.preventDefault()
    }
    if (isSaving || !job) return

    try {
      setIsSaving(true)
      setError(null)
      const updatedJob = await jobService.updateJobSkills(job.id, skills)
      onSaved(updatedJob)
      onClose()
    } catch (err: unknown) {
      const message =
        err instanceof Error
          ? err.message
          : 'Nem sikerült frissíteni a készségeket. Kérjük, próbáld újra később!'
      setError(message)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div
      className="modal modal-open"
      role="dialog"
      aria-modal="true"
      aria-labelledby="job-skills-modal-title"
    >
      <div className="modal-box w-11/12 max-w-2xl max-h-[90vh] overflow-y-auto bg-base-100 p-5 sm:p-6 shadow-xl border border-base-300">
        {/* Header with job info and close button */}
        <div className="flex items-start justify-between gap-4 pb-4 border-b border-base-200">
          <div>
            <h3 id="job-skills-modal-title" className="text-xl font-bold text-base-content">
              Elvárt készségek szerkesztése
            </h3>
            <p className="text-sm text-base-content/70 mt-1">
              <span className="font-semibold text-base-content">{job.title}</span> &bull; {job.company}
            </p>
          </div>
          <button
            type="button"
            className="btn btn-ghost btn-sm btn-circle shrink-0"
            onClick={handleClose}
            disabled={isSaving}
            aria-label="Ablak bezárása"
          >
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>

        {/* Error notification banner */}
        {error && (
          <div role="alert" className="alert alert-error text-sm mt-4">
            <svg
              className="w-5 h-5 shrink-0"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
            <span>{error}</span>
          </div>
        )}

        {/* Modal body with SkillBadgeInput */}
        <div className="mt-4 space-y-4">
          <SkillBadgeInput
            skills={skills}
            onChange={setSkills}
            disabled={isSaving}
            label="Szükséges szakmai készségek és kompetenciák"
            placeholder="Pl. React ismeret, TypeScript, Docker..."
            helperText="Adja hozzá a pozícióhoz elvárt szakmai készségeket. Az álláskeresők ezek alapján kapnak illeszkedési ajánlásokat."
            suggestedSkills={DEFAULT_SUGGESTED_SKILLS}
          />
        </div>

        {/* Footer actions with cancel and save buttons */}
        <div className="modal-action mt-6 pt-4 border-t border-base-200 flex items-center justify-end gap-3">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={handleClose}
            disabled={isSaving}
          >
            Mégsem
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => handleSave()}
            disabled={isSaving}
          >
            {isSaving ? (
              <>
                <span className="loading loading-spinner loading-xs" aria-hidden="true"></span>
                Mentés folyamatban...
              </>
            ) : (
              'Skillek mentése'
            )}
          </button>
        </div>
      </div>

      {/* Backdrop overlay for closing on outside click */}
      <div
        className="modal-backdrop bg-black/40"
        onClick={handleClose}
        aria-hidden="true"
      ></div>
    </div>
  )
}

export default JobSkillsEditorModal
