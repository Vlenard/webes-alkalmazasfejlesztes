import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'
import { jobService } from '../services/jobService'
import type {
  Job,
  JobStatus,
  EmployerApplicationItem,
  CreateJobPayload,
} from '../types/job'

export default function EmployerDashboardPage() {
  const { user } = useAuth()

  // Jobs state
  const [jobs, setJobs] = useState<Job[]>([])
  const [isLoadingJobs, setIsLoadingJobs] = useState(true)
  const [jobsError, setJobsError] = useState<string | null>(null)
  const [statusUpdatingJobId, setStatusUpdatingJobId] = useState<number | null>(null)

  // Selected job & Applications state
  const [selectedJob, setSelectedJob] = useState<Job | null>(null)
  const [applications, setApplications] = useState<EmployerApplicationItem[]>([])
  const [isLoadingApps, setIsLoadingApps] = useState(false)
  const [appsError, setAppsError] = useState<string | null>(null)
  const [actionLoadingAppId, setActionLoadingAppId] = useState<number | null>(null)
  const [downloadingAppId, setDownloadingAppId] = useState<number | null>(null)

  // Create Job Modal state
  const [showCreateModal, setShowCreateModal] = useState(false)
  const [isCreating, setIsCreating] = useState(false)
  const [createError, setCreateError] = useState<string | null>(null)
  const [createForm, setCreateForm] = useState<CreateJobPayload>({
    title: '',
    company: user?.companyName || '',
    type: 'Full-time',
    location: '',
    badge: 'React',
    salary: '',
    description: '',
    requirements: [],
  })
  const [requirementsInput, setRequirementsInput] = useState('')

  // Load employer jobs on mount and sync from server
  const loadEmployerJobs = async () => {
    setIsLoadingJobs(true)
    setJobsError(null)
    try {
      const data = await jobService.getMyEmployerJobs()
      setJobs(data)
    } catch (err: unknown) {
      if (err instanceof Error) {
        setJobsError(err.message)
      } else {
        setJobsError('Nem sikerült betölteni az álláshirdetéseket.')
      }
    } finally {
      setIsLoadingJobs(false)
    }
  }

  useEffect(() => {
    loadEmployerJobs()
  }, [])

  // Keep company prefilled if user loads later
  useEffect(() => {
    if (user?.companyName && !createForm.company) {
      setCreateForm((prev) => ({ ...prev, company: user.companyName || '' }))
    }
  }, [user?.companyName, createForm.company])

  // Select job and load its applications
  const handleSelectJob = async (job: Job) => {
    setSelectedJob(job)
    setIsLoadingApps(true)
    setAppsError(null)
    try {
      const data = await jobService.getJobApplications(job.id)
      setApplications(data)
    } catch (err: unknown) {
      if (err instanceof Error) {
        setAppsError(err.message)
      } else {
        setAppsError('Nem sikerült lekérni a jelentkezőket.')
      }
    } finally {
      setIsLoadingApps(false)
    }
  }

  // Toggle Job Status (ACTIVE <-> CLOSED) and immediately sync from server
  const handleToggleJobStatus = async (job: Job) => {
    const newStatus: JobStatus = job.status === 'ACTIVE' ? 'CLOSED' : 'ACTIVE'
    setStatusUpdatingJobId(job.id)
    try {
      const updatedJob = await jobService.updateJobStatus(job.id, newStatus)
      await loadEmployerJobs()
      if (selectedJob?.id === job.id) {
        setSelectedJob(updatedJob)
      }
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Nem sikerült módosítani a hirdetés státuszát.')
    } finally {
      setStatusUpdatingJobId(null)
    }
  }

  // Handle Application status update (ACCEPTED / REJECTED)
  const handleUpdateApplicationStatus = async (
    applicationId: number,
    newStatus: 'ACCEPTED' | 'REJECTED'
  ) => {
    setActionLoadingAppId(applicationId)
    try {
      const updated = await jobService.updateApplicationStatus(applicationId, newStatus)
      setApplications((prev) =>
        prev.map((app) => (app.id === applicationId ? updated : app))
      )
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Nem sikerült módosítani a jelentkező státuszát.')
    } finally {
      setActionLoadingAppId(null)
    }
  }

  // Handle resume download
  const handleDownloadResume = async (app: EmployerApplicationItem) => {
    setDownloadingAppId(app.id)
    try {
      await jobService.downloadResume(app.id, app.resumeFileName || 'oneletrajz.pdf')
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Nem sikerült letölteni az önéletrajzot.')
    } finally {
      setDownloadingAppId(null)
    }
  }

  // Create Job submit and immediately sync list from server
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
      }
      await jobService.createJob(payload)
      await loadEmployerJobs()
      setShowCreateModal(false)
      // Reset form
      setCreateForm({
        title: '',
        company: user?.companyName || '',
        type: 'Full-time',
        location: '',
        badge: 'React',
        salary: '',
        description: '',
        requirements: [],
      })
      setRequirementsInput('')
    } catch (err: unknown) {
      setCreateError(err instanceof Error ? err.message : 'Hiba történt a hirdetés létrehozása során.')
    } finally {
      setIsCreating(false)
    }
  }

  return (
    <div className="space-y-8 my-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-3xl font-bold">Céges Kezelőpult</h1>
            <span className="badge badge-primary badge-sm">COMPANY_MANAGER</span>
          </div>
          <p className="text-sm text-base-content/70 mt-1">
            {user?.companyName
              ? `${user.companyName} hirdetéseinek és jelentkezőinek kezelése`
              : 'Hirdetések feladása, státuszváltás és jelentkezők elbírálása'}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={loadEmployerJobs}
            className="btn btn-outline btn-sm"
            disabled={isLoadingJobs}
          >
            {isLoadingJobs ? (
              <span className="loading loading-spinner loading-xs"></span>
            ) : (
              '↻ Frissítés'
            )}
          </button>
          <button
            type="button"
            onClick={() => setShowCreateModal(true)}
            className="btn btn-primary btn-sm"
          >
            + Új hirdetés feladása
          </button>
        </div>
      </div>

      {jobsError && (
        <div role="alert" className="alert alert-error">
          <span>{jobsError}</span>
        </div>
      )}

      {/* Main Content: Jobs List and Applications Inspector */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Bal oldal: Saját Hirdetések (7 oszlop) */}
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold">
              Saját álláshirdetéseim ({jobs.length})
            </h2>
          </div>

          {isLoadingJobs && (
            <div className="flex justify-center items-center py-16 bg-base-100 rounded-box border border-base-200">
              <span className="loading loading-spinner loading-lg text-primary"></span>
            </div>
          )}

          {!isLoadingJobs && jobs.length === 0 && (
            <div className="card bg-base-100 border border-base-200 text-center py-12 p-6">
              <p className="text-base-content/70 mb-4">
                Még nem adtál fel egyetlen álláshirdetést sem.
              </p>
              <div>
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={() => setShowCreateModal(true)}
                >
                  Hirdetés feladása most
                </button>
              </div>
            </div>
          )}

          {!isLoadingJobs &&
            jobs.map((job) => {
              const isActive = job.status === 'ACTIVE'
              const isSelected = selectedJob?.id === job.id
              const isUpdating = statusUpdatingJobId === job.id

              return (
                <div
                  key={job.id}
                  className={`card bg-base-100 border transition-all ${
                    isSelected
                      ? 'border-primary ring-2 ring-primary/20 shadow-md'
                      : 'border-base-200 shadow-sm hover:shadow'
                  }`}
                >
                  <div className="card-body p-5">
                    {/* Felső információs blokk */}
                    <div>
                      <div className="flex items-center gap-2 flex-wrap mb-1">
                        <span className="badge badge-outline badge-sm font-semibold">
                          {job.badge}
                        </span>
                        {isActive ? (
                          <span className="badge badge-success text-white badge-sm font-medium">
                            Aktív
                          </span>
                        ) : (
                          <span className="badge badge-neutral badge-sm font-medium">
                            Lezárva (Jelentkezőknek rejtett)
                          </span>
                        )}
                      </div>
                      <h3 className="text-lg font-bold">
                        <Link to={`/jobs/${job.id}`} className="hover:underline">
                          {job.title}
                        </Link>
                      </h3>
                      <p className="text-xs text-base-content/70 mt-0.5">
                        {job.company} • {job.location} • {job.type}
                      </p>
                      {job.salary && (
                        <p className="text-xs font-semibold text-success mt-1">
                          Bérsáv: {job.salary}
                        </p>
                      )}
                    </div>

                    {/* Reszponzív műveleti sáv: külön sávban a card alján, nem nyomja össze a szöveget */}
                    <div className="card-actions flex-wrap justify-between items-center pt-3 mt-2 border-t border-base-200 gap-2">
                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          onClick={() => handleToggleJobStatus(job)}
                          disabled={isUpdating}
                          className={`btn btn-sm ${
                            isActive ? 'btn-outline btn-warning' : 'btn-outline btn-success'
                          }`}
                        >
                          {isUpdating ? (
                            <span className="loading loading-spinner loading-xs"></span>
                          ) : isActive ? (
                            'Hirdetés lezárása'
                          ) : (
                            'Hirdetés újranyitása'
                          )}
                        </button>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleSelectJob(job)}
                        className={`btn btn-sm ${
                          isSelected ? 'btn-primary' : 'btn-outline btn-primary'
                        }`}
                      >
                        Jelentkezők megtekintése
                      </button>
                    </div>
                  </div>
                </div>
              )
            })}
        </div>

        {/* Jobb oldal: Kiválasztott Hirdetés Jelentkezői (5 oszlop) - Mobilon NEM sticky, csak lg:sticky lg:top-20 */}
        <div className="lg:col-span-5 lg:sticky lg:top-20 w-full">
          <div className="card bg-base-100 border border-base-200 shadow-md">
            <div className="card-body p-5">
              <div className="flex items-center justify-between border-b border-base-200 pb-3 mb-3">
                <h3 className="font-bold text-lg">
                  {selectedJob ? (
                    <span>
                      Jelentkezők: <span className="text-primary">{selectedJob.title}</span>
                    </span>
                  ) : (
                    'Jelentkezők listája'
                  )}
                </h3>
                {selectedJob && (
                  <span className="badge badge-sm badge-ghost">
                    {applications.length} fő
                  </span>
                )}
              </div>

              {!selectedJob && (
                <div className="py-12 text-center text-base-content/60 text-sm">
                  <p>
                    Válassz ki egy hirdetést a bal oldali listából a{' '}
                    <strong>„Jelentkezők megtekintése”</strong> gombra kattintva!
                  </p>
                </div>
              )}

              {selectedJob && isLoadingApps && (
                <div className="py-12 flex justify-center items-center">
                  <span className="loading loading-spinner loading-md text-primary"></span>
                </div>
              )}

              {selectedJob && appsError && (
                <div role="alert" className="alert alert-error text-sm my-2">
                  <span>{appsError}</span>
                </div>
              )}

              {selectedJob && !isLoadingApps && !appsError && applications.length === 0 && (
                <div className="py-10 text-center text-base-content/60 text-sm">
                  <p>Erre a hirdetésre még nem érkezett jelentkezés.</p>
                </div>
              )}

              {selectedJob && !isLoadingApps && !appsError && applications.length > 0 && (
                <div className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
                  {applications.map((app) => {
                    const isProcessing = actionLoadingAppId === app.id
                    const isDownloading = downloadingAppId === app.id
                    const isAccepted = app.status === 'ACCEPTED'
                    const isRejected = app.status === 'REJECTED'

                    return (
                      <div
                        key={app.id}
                        className="p-4 rounded-box bg-base-200/60 border border-base-200 space-y-3"
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div>
                            <h4 className="font-bold text-sm">{app.applicantName}</h4>
                            <a
                              href={`mailto:${app.applicantEmail}`}
                              className="text-xs text-primary hover:underline block break-all"
                            >
                              {app.applicantEmail}
                            </a>
                            <span className="text-[11px] text-base-content/60 block mt-0.5">
                              📅 {new Date(app.submittedAt).toLocaleDateString('hu-HU')}
                            </span>
                          </div>

                          <div>
                            {app.status === 'ACCEPTED' && (
                              <span className="badge badge-success text-white badge-xs">
                                Elfogadva
                              </span>
                            )}
                            {app.status === 'REJECTED' && (
                              <span className="badge badge-error text-white badge-xs">
                                Elutasítva
                              </span>
                            )}
                            {app.status === 'PENDING' && (
                              <span className="badge badge-warning badge-xs">
                                Elbírálásra vár
                              </span>
                            )}
                          </div>
                        </div>

                        {/* Önéletrajz letöltése és döntési gombok flex-wrap elrendezésben, hogy ne lógjanak ki keskeny kijelzőn */}
                        <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-base-300">
                          <button
                            type="button"
                            onClick={() => handleDownloadResume(app)}
                            disabled={isDownloading || !app.hasResume}
                            className="btn btn-xs btn-outline gap-1 shrink-0"
                            title={
                              app.hasResume
                                ? 'Önéletrajz letöltése (PDF/DOCX)'
                                : 'Nincs csatolt önéletrajz'
                            }
                          >
                            {isDownloading ? (
                              <span className="loading loading-spinner loading-xs"></span>
                            ) : (
                              <>
                                <span>📥</span>
                                <span className="truncate max-w-[130px] sm:max-w-[160px]">
                                  {app.resumeFileName || 'CV Letöltése'}
                                </span>
                              </>
                            )}
                          </button>

                          {/* Döntési gombok (Elfogadás / Elutasítás) */}
                          <div className="flex flex-wrap items-center gap-1.5 shrink-0">
                            <button
                              type="button"
                              onClick={() => handleUpdateApplicationStatus(app.id, 'ACCEPTED')}
                              disabled={isProcessing || isAccepted}
                              className={`btn btn-xs ${
                                isAccepted
                                  ? 'btn-disabled bg-success/20 text-success'
                                  : 'btn-success text-white'
                              }`}
                            >
                              {isProcessing ? '...' : '✓ Elfogadás'}
                            </button>
                            <button
                              type="button"
                              onClick={() => handleUpdateApplicationStatus(app.id, 'REJECTED')}
                              disabled={isProcessing || isRejected}
                              className={`btn btn-xs ${
                                isRejected
                                  ? 'btn-disabled bg-error/20 text-error'
                                  : 'btn-error text-white'
                              }`}
                            >
                              {isProcessing ? '...' : '✕ Elutasítás'}
                            </button>
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Új Álláshirdetés Létrehozása Modal: reszponzív méretezéssel */}
      {showCreateModal && (
        <div className="modal modal-open">
          <div className="modal-box w-11/12 max-w-2xl max-h-[90vh] overflow-y-auto bg-base-100 p-4 sm:p-6">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold">Új álláshirdetés létrehozása</h3>
              <button
                type="button"
                className="btn btn-sm btn-circle btn-ghost"
                onClick={() => setShowCreateModal(false)}
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
                  <label className="label">
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
                  <label className="label">
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
                  <label className="label">
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
                  <label className="label">
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
                  <label className="label">
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
                <label className="label">
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
                <label className="label">
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
                <label className="label">
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
                <label className="label">
                  <span className="label-text-alt text-base-content/60">
                    Minden új sor külön pontként jelenik majd meg az álláshirdetésben.
                  </span>
                </label>
              </div>

              {/* Gombok */}
              <div className="modal-action">
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => setShowCreateModal(false)}
                  disabled={isCreating}
                >
                  Mégsem
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={isCreating}
                >
                  {isCreating ? (
                    <>
                      <span className="loading loading-spinner loading-xs"></span>
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
            onClick={() => setShowCreateModal(false)}
          ></div>
        </div>
      )}
    </div>
  )
}
