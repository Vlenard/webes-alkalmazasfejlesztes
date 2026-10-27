import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { jobService } from '../services/jobService'
import type { ApplicantApplicationItem } from '../types/job'

export default function MyApplicationsPage() {
  const [applications, setApplications] = useState<ApplicantApplicationItem[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadApplications = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const data = await jobService.getMyApplications()
      setApplications(data)
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('Nem sikerült betölteni a jelentkezéseket.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    loadApplications()
  }, [])

  return (
    <div className="space-y-6 my-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Jelentkezéseim</h1>
          <p className="text-sm text-base-content/70 mt-1">
            Itt követheted nyomon a leadott állásjelentkezéseid aktuális státuszát.
          </p>
        </div>
        <button
          type="button"
          onClick={loadApplications}
          className="btn btn-outline py-2 px-4 self-start sm:self-auto"
          disabled={isLoading}
        >
          {isLoading ? (
            <span className="loading loading-spinner loading-xs"></span>
          ) : (
            '↻ Frissítés'
          )}
        </button>
      </div>

      {isLoading && (
        <div className="flex justify-center items-center py-20">
          <span className="loading loading-spinner loading-lg text-primary"></span>
        </div>
      )}

      {error && (
        <div role="alert" className="alert alert-error">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="stroke-current shrink-0 h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <span>{error}</span>
        </div>
      )}

      {!isLoading && !error && applications.length === 0 && (
        <div className="card bg-base-100 border border-base-200 text-center py-16 px-4">
          <div className="max-w-md mx-auto space-y-4">
            <div className="text-5xl">📄</div>
            <h2 className="text-xl font-bold">Még nincs leadott jelentkezésed</h2>
            <p className="text-base-content/70 text-sm">
              Böngéssz a nyitott álláshirdetések között, és töltsd fel önéletrajzodat a számodra
              szimpatikus pozíciókra!
            </p>
            <Link to="/jobs" className="btn btn-primary py-2 px-4 mt-2">
              Álláshirdetések böngészése
            </Link>
          </div>
        </div>
      )}

      {!isLoading && !error && applications.length > 0 && (
        <div className="space-y-4">
          {applications.map((app) => {
            const isAccepted = app.status === 'ACCEPTED'
            const isRejected = app.status === 'REJECTED'
            const isPending = app.status === 'PENDING'

            return (
              <div
                key={app.id}
                className="card bg-base-100 border border-base-200 shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="card-body p-6">
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
                    <div>
                      <h3 className="card-title text-xl font-bold">
                        <Link to={`/jobs/${app.jobId}`} className="hover:underline">
                          {app.jobTitle}
                        </Link>
                      </h3>
                      <p className="text-sm font-medium text-primary mt-0.5">{app.company}</p>
                      <p className="text-xs text-base-content/60 mt-1">Helyszín: {app.location}</p>
                    </div>

                    <div className="flex items-center gap-2">
                      {isAccepted && (
                        <span className="badge badge-success text-white py-2 px-4 h-auto font-semibold">
                          Továbbjutott
                        </span>
                      )}
                      {isRejected && (
                        <span className="badge badge-error text-white py-2 px-4 h-auto font-semibold">
                          Elutasítva
                        </span>
                      )}
                      {isPending && (
                        <span className="badge badge-warning py-2 px-4 h-auto font-semibold">
                          Elbírálás alatt
                        </span>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-4 text-xs text-base-content/70 mt-2 pt-2 border-t border-base-200">
                    <span>
                      📅 Beküldve: {new Date(app.submittedAt).toLocaleDateString('hu-HU')}
                    </span>
                    {app.resumeFileName && (
                      <span className="badge badge-ghost py-2 px-4 h-auto">
                        📎 {app.resumeFileName}
                      </span>
                    )}
                  </div>

                  {/* Státusz üzenetek a követelmények szerint */}
                  {isAccepted && (
                    <div role="alert" className="alert alert-success shadow-sm mt-4 text-white">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="stroke-current shrink-0 h-6 w-6"
                        fill="none"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="2"
                          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                      <span className="font-semibold">
                        Továbbjutott! A cég értesíti Önt a továbbiakról.
                      </span>
                    </div>
                  )}

                  {isRejected && (
                    <div role="alert" className="alert alert-error shadow-sm mt-4 text-white">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="stroke-current shrink-0 h-6 w-6"
                        fill="none"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="2"
                          d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                      <span className="font-semibold">
                        Sajnáljuk, a jelentkezését elutasították.
                      </span>
                    </div>
                  )}

                  {isPending && (
                    <div role="alert" className="alert alert-warning shadow-sm mt-4">
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="stroke-current shrink-0 h-6 w-6"
                        fill="none"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="2"
                          d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                      <span>{app.statusMessage || 'Jelentkezése elbírálás alatt áll.'}</span>
                    </div>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
