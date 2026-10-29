import type { FC } from 'react'
import { Link } from 'react-router-dom'
import { StatusBadge } from '../../../shared/components/StatusBadge'
import { SkillBadgeList } from '../../../shared/components/SkillBadgeList'
import type { Job } from '../types/job'

export interface EmployerJobCardProps {
  job: Job
  isSelected: boolean
  isUpdatingStatus: boolean
  onToggleStatus: (job: Job) => void
  onEditSkills: (job: Job) => void
  onSelectJob: (job: Job) => void
  className?: string
}

/**
 * EmployerJobCard component
 *
 * Displays a single employer job posting card with status indicator,
 * skill tags, and action buttons for status toggle, skills editing,
 * and viewing applications.
 */
export const EmployerJobCard: FC<EmployerJobCardProps> = ({
  job,
  isSelected,
  isUpdatingStatus,
  onToggleStatus,
  onEditSkills,
  onSelectJob,
  className = '',
}) => {
  const isActive = job.status === 'ACTIVE'

  return (
    <div
      className={`card bg-base-100 border transition-all ${
        isSelected
          ? 'border-primary ring-2 ring-primary/20 shadow-md'
          : 'border-base-200 shadow-sm hover:shadow'
      } ${className}`.trim()}
    >
      <div className="card-body p-5">
        {/* Felső információs blokk */}
        <div>
          <div className="flex items-center gap-2 flex-wrap mb-1">
            {job.badge && (
              <span className="badge badge-outline py-2 px-4 h-auto font-semibold">
                {job.badge}
              </span>
            )}
            <StatusBadge status={job.status ?? 'ACTIVE'} closedDetailText />
          </div>
          <h3 className="text-lg font-bold">
            <Link to={`/jobs/${job.id}`} className="hover:underline">
              {job.title}
            </Link>
          </h3>
          <p className="text-xs text-base-content/70 mt-0.5">
            {job.company} • {job.location} • {job.type}
          </p>
          {job.salary && (
            <p className="text-xs font-semibold text-success mt-1">
              Bérsáv: {job.salary}
            </p>
          )}
        </div>

        {/* Hirdetéshez tartozó skillek */}
        {job.skills && job.skills.length > 0 && (
          <SkillBadgeList skills={job.skills} variant="outline" className="mt-2" />
        )}

        {/* Reszponzív műveleti sáv: külön sávban a card alján, nem nyomja össze a szöveget */}
        <div className="card-actions flex-wrap justify-between items-center pt-3 mt-2 border-t border-base-200 gap-2">
          <div className="flex items-center gap-2 flex-wrap">
            <button
              type="button"
              onClick={() => onToggleStatus(job)}
              disabled={isUpdatingStatus}
              className={`btn py-2 px-4 ${
                isActive ? 'btn-outline btn-warning' : 'btn-outline btn-success'
              }`}
            >
              {isUpdatingStatus ? (
                <span className="loading loading-spinner loading-xs"></span>
              ) : isActive ? (
                'Hirdetés lezárása'
              ) : (
                'Hirdetés újranyitása'
              )}
            </button>
            <button
              type="button"
              onClick={() => onEditSkills(job)}
              className="btn btn-outline py-2 px-4"
            >
              Skillek szerkesztése
            </button>
          </div>
          <button
            type="button"
            onClick={() => onSelectJob(job)}
            className={`btn py-2 px-4 ${
              isSelected ? 'btn-primary' : 'btn-outline btn-primary'
            }`}
          >
            Jelentkezők megtekintése
          </button>
        </div>
      </div>
    </div>
  )
}

export default EmployerJobCard
