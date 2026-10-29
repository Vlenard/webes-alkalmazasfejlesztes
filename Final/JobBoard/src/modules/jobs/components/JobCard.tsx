import { Link } from 'react-router-dom'
import type { Job } from '../types/job'
import { useAuth } from '../../auth/context/AuthContext'
import { MatchScoreBadge } from './MatchScoreBadge'
import { SkillBadgeList } from '../../../shared/components/SkillBadgeList'

export interface JobCardProps {
  job: Job
}

export const JobCard = ({ job }: JobCardProps) => {
  const { user } = useAuth()
  const isApplicant = user?.role === 'APPLICANT'

  return (
    <div className="card bg-base-100 shadow-md hover:shadow-lg transition-shadow border border-base-200 h-full flex flex-col justify-between">
      <div className="card-body p-6 flex flex-col justify-between h-full">
        <div className="flex flex-col gap-3">
          <div className="flex justify-between items-start gap-2">
            <div className="min-w-0 flex-1">
              <h3 className="card-title text-lg font-bold">
                <Link to={`/jobs/${job.id}`} className="hover:underline">
                  {job.title}
                </Link>
              </h3>
              <p className="text-sm font-medium text-primary">{job.company}</p>
            </div>
            <div className="flex flex-wrap items-center gap-1.5 justify-end shrink-0">
              <MatchScoreBadge score={job.matchScore} showZero={isApplicant} />
              <span className="badge badge-primary badge-outline py-2 px-4 h-auto font-semibold">
                {job.badge}
              </span>
            </div>
          </div>

          <p className="text-sm text-base-content/80 my-1 line-clamp-2">{job.description}</p>

          <SkillBadgeList
            skills={job.skills}
            matchedSkills={job.matchedSkills}
            variant="match-highlight"
            className="my-1"
          />
        </div>

        <div className="mt-auto pt-3 flex flex-col gap-3">
          <div className="flex flex-wrap gap-2 text-xs text-base-content/70">
            <span className="badge badge-ghost py-2 px-4 h-auto">{job.location}</span>
            <span className="badge badge-ghost py-2 px-4 h-auto">{job.type}</span>
            <span className="badge badge-success py-2 px-4 h-auto font-semibold">{job.salary}</span>
          </div>

          <div className="card-actions justify-end mt-2">
            <Link to={`/jobs/${job.id}`} className="btn btn-outline btn-primary py-2 px-4">
              Részletek
            </Link>
            <Link to={`/jobs/${job.id}`} className="btn btn-primary py-2 px-4">
              Jelentkezés
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default JobCard
