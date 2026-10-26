import { useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'
import { useJobDetail } from '../hooks/useJobDetail'
import { ApplicationForm } from '../components/ApplicationForm'

export default function JobDetailPage() {
  const { id } = useParams<{ id: string }>()
  const jobId = id ? parseInt(id, 10) : null
  const { job, isLoading, error } = useJobDetail(jobId)
  const { user, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [showApplicationModal, setShowApplicationModal] = useState(false)

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-24">
        <span className="loading loading-spinner loading-lg text-primary"></span>
      </div>
    )
  }

  if (error || !job) {
    return (
      <div className="space-y-4 my-8">
        <div role="alert" className="alert alert-error">
          <span>{error || 'Az állásajánlat nem található.'}</span>
        </div>
        <div>
          <Link to="/jobs" className="btn btn-outline">
            ← Vissza az állásokhoz
          </Link>
        </div>
      </div>
    )
  }

  const isClosed = job.status === 'CLOSED'
  const isCompanyManager = user?.role === 'COMPANY_MANAGER'

  const handleApplyClick = () => {
    if (isClosed) {
      return
    }
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    if (isCompanyManager) {
      return
    }
    setShowApplicationModal(true)
  }

  return (
    <div className="space-y-6 my-6">
      {/* Breadcrumb / Vissza gomb */}
      <div className="flex items-center justify-between">
        <Link to="/jobs" className="btn btn-sm btn-ghost gap-1">
          ← Vissza az összes álláshoz
        </Link>
        <span className="text-xs text-base-content/60">Közzétéve: {job.postedAt}</span>
      </div>

      {/* Lezárt állás figyelmeztetés */}
      {isClosed && (
        <div role="alert" className="alert alert-warning shadow-sm">
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
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
          <div>
            <h4 className="font-bold">A hirdetés le van zárva!</h4>
            <div className="text-sm">Ez az álláshirdetés már lezárult, új jelentkezéseket nem fogad.</div>
          </div>
        </div>
      )}

      {/* Céges kezelő figyelmeztetés */}
      {isAuthenticated && isCompanyManager && (
        <div role="alert" className="alert alert-info shadow-sm">
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
              d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <span>Céges kezelőként vagy bejelentkezve, így erre az állásra nem nyújthatsz be jelentkezést.</span>
        </div>
      )}

      {/* Fő fejléc kártya */}
      <div className="card bg-base-100 shadow-md border border-base-200">
        <div className="card-body">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-2">
                <span className="badge badge-primary badge-outline font-semibold">
                  {job.badge}
                </span>
                {isClosed ? (
                  <span className="badge badge-error text-white font-medium">Lezárva</span>
                ) : (
                  <span className="badge badge-success text-white font-medium">Aktív</span>
                )}
              </div>
              <h1 className="text-3xl font-bold">{job.title}</h1>
              <p className="text-lg font-medium text-primary mt-1">{job.company}</p>
            </div>

            <div className="flex flex-col sm:flex-row gap-2 md:self-start">
              {isClosed ? (
                <button type="button" className="btn btn-disabled" disabled>
                  Hirdetés lezárva
                </button>
              ) : isCompanyManager ? (
                <button type="button" className="btn btn-disabled" disabled>
                  Céges kezelőként nem jelentkezhetsz
                </button>
              ) : !isAuthenticated ? (
                <button type="button" className="btn btn-primary" onClick={handleApplyClick}>
                  Belépés a jelentkezéshez
                </button>
              ) : (
                <button type="button" className="btn btn-primary" onClick={handleApplyClick}>
                  Jelentkezés az állásra
                </button>
              )}
            </div>
          </div>

          <div className="divider my-4"></div>

          {/* Címkék & Gyors infók */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 bg-base-200/50 p-4 rounded-box">
            <div>
              <span className="text-xs text-base-content/60 block">Helyszín</span>
              <span className="font-semibold text-sm">{job.location}</span>
            </div>
            <div>
              <span className="text-xs text-base-content/60 block">Munkarend</span>
              <span className="font-semibold text-sm">{job.type}</span>
            </div>
            <div>
              <span className="text-xs text-base-content/60 block">Fizetési sáv</span>
              <span className="font-semibold text-sm text-success">{job.salary}</span>
            </div>
            <div>
              <span className="text-xs text-base-content/60 block">Technológia</span>
              <span className="font-semibold text-sm">{job.badge}</span>
            </div>
          </div>

          {/* Leírás szekció */}
          <div className="mt-6 space-y-4">
            <h2 className="text-xl font-bold">Pozíció leírása</h2>
            <p className="text-base-content/80 leading-relaxed whitespace-pre-line">{job.description}</p>
          </div>

          {/* Követelmények szekció */}
          {job.requirements && job.requirements.length > 0 && (
            <div className="mt-6 space-y-3">
              <h2 className="text-xl font-bold">Elvárások és követelmények</h2>
              <ul className="list-disc list-inside space-y-1.5 text-base-content/80">
                {job.requirements.map((req) => (
                  <li key={req}>{req}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Alsó CTA sáv */}
          <div className="card-actions justify-end mt-8 pt-4 border-t border-base-200">
            <Link to="/jobs" className="btn btn-outline">
              Vissza
            </Link>
            {isClosed ? (
              <button type="button" className="btn btn-disabled" disabled>
                Hirdetés lezárva
              </button>
            ) : isCompanyManager ? (
              <button type="button" className="btn btn-disabled" disabled>
                Céges kezelőként nem jelentkezhetsz
              </button>
            ) : !isAuthenticated ? (
              <button type="button" className="btn btn-primary" onClick={handleApplyClick}>
                Belépés a jelentkezéshez
              </button>
            ) : (
              <button type="button" className="btn btn-primary" onClick={handleApplyClick}>
                Jelentkezés beküldése
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Jelentkezési Modal */}
      {showApplicationModal && (
        <div className="modal modal-open">
          <div className="modal-box max-w-lg p-0 overflow-hidden bg-base-100">
            <ApplicationForm
              jobId={job.id}
              jobTitle={job.title}
              onClose={() => setShowApplicationModal(false)}
            />
          </div>
          <div
            className="modal-backdrop bg-black/40"
            onClick={() => setShowApplicationModal(false)}
          ></div>
        </div>
      )}
    </div>
  )
}
