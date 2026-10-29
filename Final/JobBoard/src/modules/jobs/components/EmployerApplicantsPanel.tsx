import { StatusBadge } from '../../../shared/components/StatusBadge'
import type { EmployerApplicationItem, Job } from '../types/job'

export interface EmployerApplicantsPanelProps {
  selectedJob: Job | null
  applications: EmployerApplicationItem[]
  isLoading: boolean
  error: string | null
  actionLoadingAppId: number | null
  downloadingAppId: number | null
  onUpdateStatus: (applicationId: number, status: 'ACCEPTED' | 'REJECTED') => void
  onDownloadResume: (app: EmployerApplicationItem) => void
  className?: string
}

export const EmployerApplicantsPanel = ({
  selectedJob,
  applications,
  isLoading,
  error,
  actionLoadingAppId,
  downloadingAppId,
  onUpdateStatus,
  onDownloadResume,
  className = '',
}: EmployerApplicantsPanelProps) => {
  return (
    <div className={`space-y-4 ${className}`.trim()}>
      {/* Távtartó elem, amellyel a jelentkezők doboza pontosan a bal oldali első hirdetés kártyával egy vonalban kezdődik */}
      <div className="hidden lg:block h-8" aria-hidden="true" />

      <div className="card bg-base-100 border border-base-200 shadow-sm">
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
              <span className="badge badge-ghost py-2 px-4 h-auto">
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

          {selectedJob && isLoading && (
            <div className="py-12 flex justify-center items-center">
              <span className="loading loading-spinner loading-md text-primary"></span>
            </div>
          )}

          {selectedJob && error && (
            <div role="alert" className="alert alert-error text-sm my-2">
              <span>{error}</span>
            </div>
          )}

          {selectedJob && !isLoading && !error && applications.length === 0 && (
            <div className="py-10 text-center text-base-content/60 text-sm">
              <p>Erre a hirdetésre még nem érkezett jelentkezés.</p>
            </div>
          )}

          {selectedJob && !isLoading && !error && applications.length > 0 && (
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
                        <span className="text-[11px] text-base-content/60 flex items-center gap-1 mt-0.5">
                          <svg
                            className="w-3.5 h-3.5 shrink-0"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                            aria-hidden="true"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth="2"
                              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                            />
                          </svg>
                          <span>{new Date(app.submittedAt).toLocaleDateString('hu-HU')}</span>
                        </span>
                      </div>

                      <div>
                        <StatusBadge status={app.status} perspective="employer" />
                      </div>
                    </div>

                    {/* Önéletrajz letöltése és döntési gombok flex-wrap elrendezésben, hogy ne lógjanak ki keskeny kijelzőn */}
                    <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-base-300">
                      <button
                        type="button"
                        onClick={() => onDownloadResume(app)}
                        disabled={isDownloading || !app.hasResume}
                        className="btn btn-outline py-2 px-4 gap-1.5 shrink-0"
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
                            <svg
                              className="w-4 h-4 shrink-0"
                              fill="none"
                              stroke="currentColor"
                              viewBox="0 0 24 24"
                              aria-hidden="true"
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth="2"
                                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                              />
                            </svg>
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
                          onClick={() => onUpdateStatus(app.id, 'ACCEPTED')}
                          disabled={isProcessing || isAccepted}
                          className={`btn py-2 px-4 ${
                            isAccepted
                              ? 'btn-disabled bg-success/20 text-success'
                              : 'btn-success text-white'
                          }`}
                        >
                          {isProcessing ? '...' : '✓ Elfogadás'}
                        </button>
                        <button
                          type="button"
                          onClick={() => onUpdateStatus(app.id, 'REJECTED')}
                          disabled={isProcessing || isRejected}
                          className={`btn py-2 px-4 ${
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
  )
}

export default EmployerApplicantsPanel
