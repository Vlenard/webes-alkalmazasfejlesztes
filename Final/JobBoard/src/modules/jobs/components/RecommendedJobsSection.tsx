import { Link } from 'react-router-dom'
import type { Job } from '../types/job'
import { SkillBadgeList } from '../../../shared/components/SkillBadgeList'
import { JobCard } from './JobCard'

export interface RecommendedJobsSectionProps {
  jobs: Job[]
  userSkills?: string[]
  isApplicant: boolean
}

// Extended job type supporting recommendation fields
type JobWithRecommendation = Job & {
  skills?: string[]
  matchScore?: number
  matchedSkills?: string[]
  missingSkills?: string[]
  recommended?: boolean
}

export const RecommendedJobsSection = ({
  jobs,
  userSkills,
  isApplicant,
}: RecommendedJobsSectionProps) => {
  // 1. Only display recommendations for applicants
  if (!isApplicant) {
    return null
  }

  // 2. If the applicant has not provided any skills yet, display an informative call-to-action card
  if (!userSkills || userSkills.length === 0) {
    return (
      <div className="card bg-base-100 border border-base-300 shadow-sm p-5 mb-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <h3 className="text-lg font-bold text-base-content">
              Személyre szabott állásajánlások
            </h3>
            <p className="text-sm text-base-content/70">
              A személyre szabott állásajánlásokhoz adja meg a skilljeit a Profil oldalon!
            </p>
          </div>
          <Link
            to="/profile"
            className="btn btn-primary shrink-0"
          >
            Skillek megadása a Profilomban
          </Link>
        </div>
      </div>
    )
  }

  // 3. User has skills: filter jobs where matchScore > 0, sort descending, and take the top matching jobs (up to 4)
  const matchingJobs = (jobs as JobWithRecommendation[])
    .filter((job) => (job.matchScore ?? 0) > 0)
    .sort((a, b) => (b.matchScore ?? 0) - (a.matchScore ?? 0))
    .slice(0, 4)

  return (
    <div className="card bg-base-100 border-2 border-primary/20 shadow-md p-5 sm:p-6 mb-8">
      {/* Header section with title, applicant skill badges, and profile edit link */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-base-200">
        <div>
          <h2 className="text-xl font-bold text-base-content">
            Neked ajánlott állások – Legjobb illeszkedés
          </h2>
          <div className="flex flex-wrap items-center gap-1.5 mt-2">
            <span className="text-xs font-semibold text-base-content/70 mr-1">
              Saját skillek:
            </span>
            <SkillBadgeList skills={userSkills} variant="neutral" />
          </div>
        </div>
        <Link
          to="/profile"
          className="btn btn-outline btn-primary btn-sm shrink-0 self-start md:self-center"
        >
          Profil skillek szerkesztése
        </Link>
      </div>

      {/* Grid of recommended jobs or informative empty state */}
      {matchingJobs.length > 0 ? (
        <div className="grid gap-4 md:grid-cols-2 mt-6">
          {matchingJobs.map((job) => (
            <JobCard key={job.id} job={job} />
          ))}
        </div>
      ) : (
        <div className="mt-6 p-6 rounded-box bg-base-200/60 border border-base-200 text-center">
          <p className="text-sm text-base-content/80 font-medium">
            Jelenleg egyik aktív hirdetés sem egyezik a megadott skillekkel, ezért alább az összes állás látható.
          </p>
        </div>
      )}
    </div>
  )
}

export default RecommendedJobsSection
