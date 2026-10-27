import { useState, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { JobCard } from '../components/JobCard'
import { useJobs } from '../hooks/useJobs'
import { Pagination } from '../../../shared/components/Pagination'

const DEFAULT_PAGE_SIZE = 4
const PAGE_SIZE_OPTIONS = [4, 6, 8, 12]

export default function JobsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [searchTerm, setSearchTerm] = useState('')

  // Derive pagination directly from URL query parameters (single source of truth)
  const rawPage = parseInt(searchParams.get('page') || '1', 10)
  const pageFromUrl = isNaN(rawPage) || rawPage < 1 ? 1 : rawPage

  const rawSize = parseInt(searchParams.get('size') || '', 10)
  const pageSize = PAGE_SIZE_OPTIONS.includes(rawSize) ? rawSize : DEFAULT_PAGE_SIZE

  const { jobs, isLoading, error } = useJobs({
    search: searchTerm,
  })

  // Calculate total pages for current jobs list
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

  const handlePageSizeChange = (newSize: number) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev)
      next.delete('page')
      if (newSize === DEFAULT_PAGE_SIZE) {
        next.delete('size')
      } else {
        next.set('size', newSize.toString())
      }
      return next
    })
  }

  const handleSearchChange = (val: string) => {
    setSearchTerm(val)
    if (searchParams.has('page')) {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev)
        next.delete('page')
        return next
      })
    }
  }

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
            onChange={(e) => handleSearchChange(e.target.value)}
          />
          {searchTerm && (
            <button
              type="button"
              className="btn btn-ghost py-2 px-4"
              onClick={() => handleSearchChange('')}
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
        <>
          <div className="grid gap-4 md:grid-cols-2">
            {paginatedJobs.map((job) => (
              <JobCard key={job.id} job={job} />
            ))}
          </div>

          <Pagination
            currentPage={activePage}
            totalPages={totalPages}
            totalItems={jobs.length}
            pageSize={pageSize}
            pageSizeOptions={PAGE_SIZE_OPTIONS}
            onPageChange={handlePageChange}
            onPageSizeChange={handlePageSizeChange}
            showCounter
            showPageSizeSelector
          />
        </>
      )}
    </div>
  )
}
