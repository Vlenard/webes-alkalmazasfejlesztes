import { useState } from 'react'
import { JobCard } from '../components/JobCard'
import { useJobs } from '../hooks/useJobs'

export default function JobsPage() {
  const [searchTerm, setSearchTerm] = useState('')

  const { jobs, isLoading, error } = useJobs({
    search: searchTerm,
  })

  return (
    <div className="space-y-6 my-6">
      {/* Sticky search bar (just below navbar) */}
      <div className="sticky top-20 z-40 bg-base-100 p-4 rounded-box shadow-md border border-base-300">
        <div className="flex items-center gap-3">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5 text-base-content/60 shrink-0"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          <input
            type="text"
            placeholder="Keresés pozíció vagy cég alapján..."
            className="input input-bordered w-full"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {searchTerm && (
            <button
              type="button"
              className="btn btn-sm btn-ghost"
              onClick={() => setSearchTerm('')}
            >
              Törlés
            </button>
          )}
        </div>
      </div>

      {isLoading && (
        <div className="flex justify-center items-center py-16">
          <span className="loading loading-spinner loading-lg text-primary"></span>
        </div>
      )}

      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
        </div>
      )}

      {!isLoading && !error && jobs.length === 0 && (
        <div className="text-center py-12 bg-base-100 rounded-box border border-base-200">
          <p className="text-base-content/70">Nincs a keresésnek megfelelő állásajánlat.</p>
        </div>
      )}

      {!isLoading && !error && jobs.length > 0 && (
        <div className="grid gap-4 md:grid-cols-2">
          {jobs.map((job) => (
            <JobCard key={job.id} job={job} />
          ))}
        </div>
      )}
    </div>
  )
}
