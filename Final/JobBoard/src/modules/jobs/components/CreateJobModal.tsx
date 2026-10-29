import React, { useState, useEffect, useCallback, type FormEvent } from 'react'
import { jobService } from '../services/jobService'
import { SkillBadgeInput } from '../../../shared/components/SkillBadgeInput'
import type { CreateJobPayload } from '../types/job'

export interface CreateJobModalProps {
  isOpen: boolean
  defaultCompany?: string
  onClose: () => void
  onCreated: () => Promise<void> | void
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
]

/**
 * CreateJobModal component
 *
 * Responsive daisyUI modal for creating a new job posting.
 * Manages its own internal form state, requirements parsing, and skills selection.
 */
export const CreateJobModal: React.FC<CreateJobModalProps> = ({
  isOpen,
  defaultCompany = '',
  onClose,
  onCreated,
}) => {
  const [isCreating, setIsCreating] = useState(false)
  const [createError, setCreateError] = useState<string | null>(null)
  const [requirementsInput, setRequirementsInput] = useState('')
  const [createForm, setCreateForm] = useState<CreateJobPayload>(() => ({
    title: '',
    company: defaultCompany,
    type: 'Full-time',
    location: '',
    badge: '',
    salary: '',
    description: '',
    requirements: [],
    skills: [],
  }))

  // Synchronize defaultCompany when it changes or when modal is opened
  useEffect(() => {
    if (defaultCompany) {
      setCreateForm((prev) => {
        if (!prev.company) {
          return { ...prev, company: defaultCompany }
        }
        return prev
      })
    }
  }, [defaultCompany])

  // Clear errors when modal opens
  useEffect(() => {
    if (isOpen) {
      setCreateError(null)
    }
  }, [isOpen])

  const handleClose = useCallback(() => {
    if (!isCreating) {
      setCreateError(null)
      onClose()
    }
  }, [isCreating, onClose])

  // Handle ESC key press to close modal safely
  useEffect(() => {
    if (!isOpen) return

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && !isCreating) {
        handleClose()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [isOpen, isCreating, handleClose])

  if (!isOpen) {
    return null
  }

  const handleCreateJobSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setCreateError(null)

    const requirements = requirementsInput
      .split('\n')
      .map((r) => r.trim())
      .filter((r) => r.length > 0)

    if (!createForm.title.trim() || !createForm.company.trim() || !createForm.location.trim()) {
      setCreateError('Kérjük, töltsd ki a kötelező mezőket (Pozíció, Cég, Helyszín)!')
      return
    }

    setIsCreating(true)
    try {
      const payload: CreateJobPayload = {
        ...createForm,
        requirements,
        skills: createForm.skills || [],
      }
      await jobService.createJob(payload)
      await onCreated()
      handleClose()
      // Reset form
      setCreateForm({
        title: '',
        company: defaultCompany || '',
        type: 'Full-time',
        location: '',
        badge: '',
        salary: '',
        description: '',
        requirements: [],
        skills: [],
      })
      setRequirementsInput('')
    } catch (err: unknown) {
      setCreateError(
        err instanceof Error ? err.message : 'Hiba történt a hirdetés létrehozása során.'
      )
    } finally {
      setIsCreating(false)
    }
  }

  return (
    <div
      className="modal modal-open"
      role="dialog"
      aria-modal="true"
      aria-labelledby="create-job-modal-title"
    >
      <div className="modal-box w-11/12 max-w-2xl max-h-[90vh] overflow-y-auto bg-base-100 p-4 sm:p-6 shadow-xl border border-base-300">
        <div className="flex justify-between items-center mb-4">
          <h3 id="create-job-modal-title" className="text-xl font-bold">
            Új álláshirdetés létrehozása
          </h3>
          <button
            type="button"
            className="btn btn-ghost py-2 px-4"
            onClick={handleClose}
            disabled={isCreating}
            aria-label="Bezárás"
          >
            ✕
          </button>
        </div>

        {createError && (
          <div role="alert" className="alert alert-error text-sm mb-4">
            <span>{createError}</span>
          </div>
        )}

        <form onSubmit={handleCreateJobSubmit} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Pozíció neve */}
            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Pozíció megnevezése *</span>
              </label>
              <input
                type="text"
                required
                placeholder="pl. Senior React Fejlesztő"
                className="input input-bordered w-full"
                value={createForm.title}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, title: e.target.value }))
                }
                disabled={isCreating}
              />
            </div>

            {/* Cég neve (előre kitöltve a profil cégnevével) */}
            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Cég neve *</span>
              </label>
              <input
                type="text"
                required
                placeholder="pl. TechCorp Hungary"
                className="input input-bordered w-full"
                value={createForm.company}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, company: e.target.value }))
                }
                disabled={isCreating}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {/* Típus */}
            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Munkarend *</span>
              </label>
              <select
                className="select select-bordered w-full"
                value={createForm.type}
                onChange={(e) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    type: e.target.value as CreateJobPayload['type'],
                  }))
                }
                disabled={isCreating}
              >
                <option value="Full-time">Full-time</option>
                <option value="Part-time">Part-time</option>
                <option value="Contract">Contract</option>
                <option value="Remote">Remote</option>
              </select>
            </div>

            {/* Helyszín */}
            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Helyszín *</span>
              </label>
              <input
                type="text"
                required
                placeholder="pl. Budapest / Hibrid"
                className="input input-bordered w-full"
                value={createForm.location}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, location: e.target.value }))
                }
                disabled={isCreating}
              />
            </div>

            {/* Technológia címke / badge */}
            <div className="form-control">
              <label className="label py-2 px-4">
                <span className="label-text font-medium">Technológia / Badge *</span>
              </label>
              <input
                type="text"
                required
                placeholder="pl. React, Java, DevOps"
                className="input input-bordered w-full"
                value={createForm.badge}
                onChange={(e) =>
                  setCreateForm((prev) => ({ ...prev, badge: e.target.value }))
                }
                disabled={isCreating}
              />
            </div>
          </div>

          {/* Bérsáv */}
          <div className="form-control">
            <label className="label py-2 px-4">
              <span className="label-text font-medium">Bérsáv / Fizetés</span>
            </label>
            <input
              type="text"
              placeholder="pl. 1 200 000 - 1 600 000 HUF"
              className="input input-bordered w-full"
              value={createForm.salary}
              onChange={(e) =>
                setCreateForm((prev) => ({ ...prev, salary: e.target.value }))
              }
              disabled={isCreating}
            />
          </div>

          {/* Leírás */}
          <div className="form-control">
            <label className="label py-2 px-4">
              <span className="label-text font-medium">Részletes pozíció leírás *</span>
            </label>
            <textarea
              required
              rows={4}
              placeholder="Ismertesd a csapatot, a projekt céljait és a napi feladatokat..."
              className="textarea textarea-bordered w-full"
              value={createForm.description}
              onChange={(e) =>
                setCreateForm((prev) => ({ ...prev, description: e.target.value }))
              }
              disabled={isCreating}
            ></textarea>
          </div>

          {/* Elvárások soronként */}
          <div className="form-control">
            <label className="label py-2 px-4">
              <span className="label-text font-medium">
                Követelmények / Elvárások (soronként egy)
              </span>
            </label>
            <textarea
              rows={3}
              placeholder="3+ év tapasztalat React és TypeScript technológiákkal&#10;Ismeret a modern CSS keretrendszerekben&#10;Agilis módszertanok ismerete"
              className="textarea textarea-bordered w-full font-mono text-sm"
              value={requirementsInput}
              onChange={(e) => setRequirementsInput(e.target.value)}
              disabled={isCreating}
            ></textarea>
            <label className="label py-2 px-4">
              <span className="label-text-alt text-base-content/60">
                Minden új sor külön pontként jelenik majd meg az álláshirdetésben.
              </span>
            </label>
          </div>

          {/* Szükséges szakmai készségek (Skillek) */}
          <SkillBadgeInput
            skills={createForm.skills || []}
            onChange={(newSkills) =>
              setCreateForm((prev) => ({ ...prev, skills: newSkills }))
            }
            disabled={isCreating}
            label="Szükséges szakmai készségek (Skillek)"
            placeholder="pl. React ismeret, TypeScript, Spring Boot..."
            helperText="A megadott készségek alapján kapnak az álláskeresők illeszkedési ajánlásokat."
            suggestedSkills={DEFAULT_SUGGESTED_SKILLS}
          />

          {/* Gombok */}
          <div className="modal-action">
            <button
              type="button"
              className="btn btn-ghost py-2 px-4"
              onClick={handleClose}
              disabled={isCreating}
            >
              Mégsem
            </button>
            <button
              type="submit"
              className="btn btn-primary py-2 px-4"
              disabled={isCreating}
            >
              {isCreating ? (
                <>
                  <span className="loading loading-spinner loading-xs" aria-hidden="true"></span>
                  Közzététel...
                </>
              ) : (
                'Hirdetés közzététele'
              )}
            </button>
          </div>
        </form>
      </div>
      <div
        className="modal-backdrop bg-black/40"
        onClick={handleClose}
        aria-hidden="true"
      ></div>
    </div>
  )
}

export default CreateJobModal
