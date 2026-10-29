import { useState, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import { JobCard } from '../components/JobCard'
import { JobSearchBar } from '../components/JobSearchBar'
import { RecommendedJobsSection } from '../components/RecommendedJobsSection'
import { useJobs } from '../hooks/useJobs'
import { useAuth } from '../../auth/context/AuthContext'
import { Pagination } from '../../../shared/components/Pagination'

const DEFAULT_PAGE_SIZE = 4
const PAGE_SIZE_OPTIONS = [4, 6, 8, 12]

export default function JobsPage() {
  const { user } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const [searchTerm, setSearchTerm] = useState('')

  // Derive pagination directly from URL query parameters (single source of truth)
  const rawPage = parseInt(searchParams.get('page') || '1', 10)
  const pageFromUrl = isNaN(rawPage) || rawPage < 1 ? 1 : rawPage

  const rawSize = parseInt(searchParams.get('size') || '', 10)
  const pageSize = PAGE_SIZE_OPTIONS.includes(rawSize) ? rawSize : DEFAULT_PAGE_SIZE

  const userSkillsKey = user?.skills?.join(',') || ''

  const { jobs, isLoading, error } = useJobs({
    search: searchTerm,
    userSkillsKey,
  })

  const isApplicantWithSkills = Boolean(
    user && user.role === 'APPLICANT' && user.skills && user.skills.length > 0
  )

  // Sort jobs by matchScore descending when applicant has skills
  const sortedJobs = useMemo(() => {
    if (!isApplicantWithSkills) {
      return jobs
    }

    return [...jobs].sort((a, b) => {
      const scoreA = a.matchScore ?? 0
      const scoreB = b.matchScore ?? 0

      // Primary sort: matchScore descending (0% non-matching pushed to the end)
      if (scoreB !== scoreA) {
        return scoreB - scoreA
      }

      // Secondary sort: number of matched skills descending
      const matchedCountA = a.matchedSkills?.length ?? 0
      const matchedCountB = b.matchedSkills?.length ?? 0
      if (matchedCountB !== matchedCountA) {
        return matchedCountB - matchedCountA
      }

      return (a.id ?? 0) - (b.id ?? 0)
    })
  }, [jobs, isApplicantWithSkills])

  // Calculate total pages for current jobs list
  const totalPages = Math.max(1, Math.ceil(sortedJobs.length / pageSize))
  const activePage = Math.min(pageFromUrl, totalPages)

  // Sliced jobs for the active page
  const paginatedJobs = useMemo(() => {
    const startIndex = (activePage - 1) * pageSize
    return sortedJobs.slice(startIndex, startIndex + pageSize)
  }, [sortedJobs, activePage, pageSize])

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
      <JobSearchBar searchTerm={searchTerm} onSearchChange={handleSearchChange} />

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

      {!isLoading && !error && (
        <>
          <RecommendedJobsSection
            jobs={jobs}
            userSkills={user?.skills}
            isApplicant={Boolean(user && user.role === 'APPLICANT')}
          />

          {sortedJobs.length === 0 ? (
            <div className="text-center py-12 bg-base-100 rounded-box border border-base-200">
              <p className="text-base-content/70">Nincs a keresésnek megfelelő állásajánlat.</p>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-2 border-b border-base-200">
                <div>
                  <h2 className="text-xl font-bold text-base-content">
                    {isApplicantWithSkills
                      ? 'Összes állásajánlat (illeszkedés szerint rendezve)'
                      : 'Összes állásajánlat'}
                  </h2>
                  <p className="text-sm text-base-content/70">
                    {isApplicantWithSkills
                      ? 'Az állások a profiljában megadott készségekhez való illeszkedés szerint vannak rangsorolva.'
                      : 'Böngésszen az aktuális nyitott pozíciók között.'}
                  </p>
                </div>
                <div className="text-sm text-base-content/60 font-medium">
                  {sortedJobs.length} {sortedJobs.length === 1 ? 'állásajánlat' : 'állásajánlat'}
                </div>
              </div>

              <div className="grid gap-4 md:grid-cols-2">
                {paginatedJobs.map((job) => (
                  <JobCard key={job.id} job={job} />
                ))}
              </div>

              <Pagination
                currentPage={activePage}
                totalPages={totalPages}
                totalItems={sortedJobs.length}
                pageSize={pageSize}
                pageSizeOptions={PAGE_SIZE_OPTIONS}
                onPageChange={handlePageChange}
                onPageSizeChange={handlePageSizeChange}
                showCounter
                showPageSizeSelector
              />
            </div>
          )}
        </>
      )}
    </div>
  )
}
