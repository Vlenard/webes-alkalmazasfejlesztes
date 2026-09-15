import { Link } from 'react-router-dom'
import type { Job } from '../types/job'

interface JobCardProps {
  job: Job
}

export const JobCard = ({ job }: JobCardProps) => {
  return (
    <div className="card bg-base-100 shadow-md hover:shadow-lg transition-shadow border border-base-200">
      <div className="card-body">
        <div className="flex justify-between items-start gap-2">
          <div>
            <h3 className="card-title text-lg font-bold">
              <Link to={`/jobs/${job.id}`} className="hover:underline">
                {job.title}
              </Link>
            </h3>
            <p className="text-sm font-medium text-primary">{job.company}</p>
          </div>
          <span className="badge badge-primary badge-outline font-semibold">{job.badge}</span>
        </div>

        <p className="text-sm text-base-content/80 my-2 line-clamp-2">{job.description}</p>

        <div className="flex flex-wrap gap-2 text-xs text-base-content/70">
          <span className="badge badge-ghost badge-sm">{job.location}</span>
          <span className="badge badge-ghost badge-sm">{job.type}</span>
          <span className="badge badge-success badge-sm font-semibold">{job.salary}</span>
        </div>

        <div className="card-actions justify-end mt-4">
          <Link to={`/jobs/${job.id}`} className="btn btn-sm btn-outline btn-primary">
            Részletek
          </Link>
          <Link to={`/jobs/${job.id}`} className="btn btn-sm btn-primary">
            Jelentkezés
          </Link>
        </div>
      </div>
    </div>
  )
}
