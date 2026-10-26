import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useJobDetail } from '../hooks/useJobDetail'
import { ApplicationForm } from '../components/ApplicationForm'

export default function JobDetailPage() {
  const { id } = useParams<{ id: string }>()
  const jobId = id ? parseInt(id, 10) : null
  const { job, isLoading, error } = useJobDetail(jobId)
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
        <div className="alert alert-error">
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

  return (
    <div className="space-y-6 my-6">
      {/* Breadcrumb / Vissza gomb */}
      <div className="flex items-center justify-between">
        <Link to="/jobs" className="btn btn-sm btn-ghost gap-1">
          ← Vissza az összes álláshoz
        </Link>
        <span className="text-xs text-base-content/60">Közzétéve: {job.postedAt}</span>
      </div>

      {/* Fő fejléc kártya */}
      <div className="card bg-base-100 shadow-md border border-base-200">
        <div className="card-body">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <span className="badge badge-primary badge-outline font-semibold mb-2">
                {job.badge}
              </span>
              <h1 className="text-3xl font-bold">{job.title}</h1>
              <p className="text-lg font-medium text-primary mt-1">{job.company}</p>
            </div>
            <div className="flex flex-col sm:flex-row gap-2 md:self-start">
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setShowApplicationModal(true)}
              >
                Jelentkezés az állásra
              </button>
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
            <p className="text-base-content/80 leading-relaxed">{job.description}</p>
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
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => setShowApplicationModal(true)}
            >
              Jelentkezés beküldése
            </button>
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
