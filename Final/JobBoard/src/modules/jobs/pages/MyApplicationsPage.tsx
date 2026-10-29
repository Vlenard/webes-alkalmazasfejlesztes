import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { jobService } from '../services/jobService'
import { ApplicantApplicationCard } from '../components/ApplicantApplicationCard'
import type { ApplicantApplicationItem } from '../types/job'

export function MyApplicationsPage() {
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
          className="btn btn-outline py-2 px-4 self-start sm:self-auto inline-flex items-center gap-1.5"
          disabled={isLoading}
        >
          {isLoading ? (
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
                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                />
              </svg>
              <span>Frissítés</span>
            </>
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
            aria-hidden="true"
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
            <div className="flex justify-center text-base-content/40">
              <svg
                className="w-16 h-16"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                strokeWidth="1.5"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
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
          {applications.map((app) => (
            <ApplicantApplicationCard key={app.id} application={app} />
          ))}
        </div>
      )}
    </div>
  )
}

export default MyApplicationsPage
