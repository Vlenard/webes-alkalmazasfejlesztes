import type { FC } from 'react'
import { Link } from 'react-router-dom'
import { StatusBadge } from '../../../shared/components/StatusBadge'
import type { ApplicantApplicationItem } from '../types/job'

export interface ApplicantApplicationCardProps {
  application: ApplicantApplicationItem
  className?: string
}

/**
 * Elementary card component for displaying an applicant's submitted application.
 * Replaces emojis with minimalist functional SVGs and integrates StatusBadge.
 */
export const ApplicantApplicationCard: FC<ApplicantApplicationCardProps> = ({
  application,
  className = '',
}) => {
  const isAccepted = application.status === 'ACCEPTED'
  const isRejected = application.status === 'REJECTED'
  const isPending = application.status === 'PENDING'

  return (
    <div
      className={`card bg-base-100 border border-base-200 shadow-sm hover:shadow-md transition-shadow ${className}`.trim()}
    >
      <div className="card-body p-6">
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
          <div>
            <h3 className="card-title text-xl font-bold">
              <Link to={`/jobs/${application.jobId}`} className="hover:underline">
                {application.jobTitle}
              </Link>
            </h3>
            <p className="text-sm font-medium text-primary mt-0.5">{application.company}</p>
            <p className="text-xs text-base-content/60 mt-1">Helyszín: {application.location}</p>
          </div>

          <div className="flex items-center gap-2">
            <StatusBadge status={application.status} perspective="applicant" />
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-4 text-xs text-base-content/70 mt-2 pt-2 border-t border-base-200">
          <span className="inline-flex items-center gap-1.5">
            <svg
              className="w-3.5 h-3.5 shrink-0"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth="2"
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <span>Beküldve: {new Date(application.submittedAt).toLocaleDateString('hu-HU')}</span>
          </span>
          {application.resumeFileName && (
            <span className="badge badge-ghost py-2 px-4 h-auto inline-flex items-center gap-1.5">
              <svg
                className="w-3.5 h-3.5 shrink-0"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                aria-hidden="true"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13"
                />
              </svg>
              <span>{application.resumeFileName}</span>
            </span>
          )}
        </div>

        {/* Státusz üzenetek a követelmények szerint */}
        {isAccepted && (
          <div role="alert" className="alert alert-success shadow-sm mt-4 text-white">
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
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <span className="font-semibold">
              Továbbjutott! A cég értesíti Önt a továbbiakról.
            </span>
          </div>
        )}

        {isRejected && (
          <div role="alert" className="alert alert-error shadow-sm mt-4 text-white">
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
                d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <span className="font-semibold">
              Sajnáljuk, a jelentkezését elutasították.
            </span>
          </div>
        )}

        {isPending && (
          <div role="alert" className="alert alert-warning shadow-sm mt-4">
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
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <span>{application.statusMessage || 'Jelentkezése elbírálás alatt áll.'}</span>
          </div>
        )}
      </div>
    </div>
  )
}

export default ApplicantApplicationCard
