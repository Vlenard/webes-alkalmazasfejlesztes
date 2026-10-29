import { useEffect, useState, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'
import { jobService } from '../services/jobService'
import { Pagination } from '../../../shared/components/Pagination'
import { JobSkillsEditorModal } from '../components/JobSkillsEditorModal'
import { EmployerJobCard } from '../components/EmployerJobCard'
import { EmployerApplicantsPanel } from '../components/EmployerApplicantsPanel'
import { CreateJobModal } from '../components/CreateJobModal'
import type {
  Job,
  JobStatus,
  EmployerApplicationItem,
} from '../types/job'

const DEFAULT_PAGE_SIZE = 4

export function EmployerDashboardPage() {
  const { user } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()

  // Jobs state
  const [jobs, setJobs] = useState<Job[]>([])
  const [isLoadingJobs, setIsLoadingJobs] = useState(true)
  const [jobsError, setJobsError] = useState<string | null>(null)
  const [statusUpdatingJobId, setStatusUpdatingJobId] = useState<number | null>(null)

  // Pagination state derived from URL search parameters
  const rawPage = parseInt(searchParams.get('page') || '1', 10)
  const pageFromUrl = isNaN(rawPage) || rawPage < 1 ? 1 : rawPage
  const pageSize = DEFAULT_PAGE_SIZE

  const totalPages = Math.max(1, Math.ceil(jobs.length / pageSize))
  const activePage = Math.min(pageFromUrl, totalPages)

  // Sliced jobs for the active page
  const paginatedJobs = useMemo(() => {
    const startIndex = (activePage - 1) * pageSize
    return jobs.slice(startIndex, startIndex + pageSize)
  }, [jobs, activePage, pageSize])

  const handlePageChange = (newPage: number) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev)
      if (newPage <= 1) {
        next.delete('page')
      } else {
        next.set('page', newPage.toString())
      }
      return next
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  // Selected job & Applications state
  const [selectedJob, setSelectedJob] = useState<Job | null>(null)
  const [applications, setApplications] = useState<EmployerApplicationItem[]>([])
  const [isLoadingApps, setIsLoadingApps] = useState(false)
  const [appsError, setAppsError] = useState<string | null>(null)
  const [actionLoadingAppId, setActionLoadingAppId] = useState<number | null>(null)
  const [downloadingAppId, setDownloadingAppId] = useState<number | null>(null)

  // Create Job Modal state
  const [showCreateModal, setShowCreateModal] = useState(false)

  // Job Skills Editor Modal state
  const [editingSkillsJob, setEditingSkillsJob] = useState<Job | null>(null)

  const handleJobSkillsSaved = (updatedJob: Job) => {
    setJobs((prevJobs) =>
      prevJobs.map((j) => (j.id === updatedJob.id ? updatedJob : j))
    )
    if (selectedJob?.id === updatedJob.id) {
      setSelectedJob(updatedJob)
    }
  }

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

  const handleJobCreated = async () => {
    await loadEmployerJobs()
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev)
      next.delete('page')
      return next
    })
  }

  return (
    <div className="space-y-8 my-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-3xl font-bold">Céges Kezelőpult</h1>
            <span className="badge badge-ghost py-2 px-4 h-auto">COMPANY_MANAGER</span>
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
            className="btn btn-outline py-2 px-4"
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
            className="btn btn-primary py-2 px-4"
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
          <div className="flex items-center justify-between h-8">
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
                  className="btn btn-primary py-2 px-4"
                  onClick={() => setShowCreateModal(true)}
                >
                  Hirdetés feladása most
                </button>
              </div>
            </div>
          )}

          {!isLoadingJobs &&
            paginatedJobs.map((job) => (
              <EmployerJobCard
                key={job.id}
                job={job}
                isSelected={selectedJob?.id === job.id}
                isUpdatingStatus={statusUpdatingJobId === job.id}
                onToggleStatus={handleToggleJobStatus}
                onEditSkills={setEditingSkillsJob}
                onSelectJob={handleSelectJob}
              />
            ))}

          {!isLoadingJobs && jobs.length > 0 && (
            <Pagination
              currentPage={activePage}
              totalPages={totalPages}
              totalItems={jobs.length}
              pageSize={pageSize}
              onPageChange={handlePageChange}
            />
          )}
        </div>

        {/* Jobb oldal: Kiválasztott Hirdetés Jelentkezői (5 oszlop) - Mobilon NEM sticky, csak lg:sticky lg:top-8 */}
        <EmployerApplicantsPanel
          className="lg:col-span-5 lg:sticky lg:top-8 w-full"
          selectedJob={selectedJob}
          applications={applications}
          isLoading={isLoadingApps}
          error={appsError}
          actionLoadingAppId={actionLoadingAppId}
          downloadingAppId={downloadingAppId}
          onUpdateStatus={handleUpdateApplicationStatus}
          onDownloadResume={handleDownloadResume}
        />
      </div>

      {/* Új Álláshirdetés Létrehozása Modal */}
      <CreateJobModal
        isOpen={showCreateModal}
        defaultCompany={user?.companyName || ''}
        onClose={() => setShowCreateModal(false)}
        onCreated={handleJobCreated}
      />

      {/* Job Skills Editor Modal */}
      <JobSkillsEditorModal
        job={editingSkillsJob}
        isOpen={Boolean(editingSkillsJob)}
        onClose={() => setEditingSkillsJob(null)}
        onSaved={handleJobSkillsSaved}
      />
    </div>
  )
}

export default EmployerDashboardPage
