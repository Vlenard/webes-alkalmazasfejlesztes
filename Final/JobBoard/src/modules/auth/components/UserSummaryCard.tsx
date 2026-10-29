import React from 'react'
import type { AuthUser } from '../types/auth'
import { SkillBadgeList } from '../../../shared/components/SkillBadgeList'

export interface UserSummaryCardProps {
  user: AuthUser | null
  className?: string
}

/**
 * UserSummaryCard component
 *
 * User profile summary card extracted from ProfilePage.
 * Displays avatar monogram, user name, role badge, optional company name badge,
 * email, user ID, and applicant skills via SkillBadgeList.
 */
export const UserSummaryCard: React.FC<UserSummaryCardProps> = ({
  user,
  className = '',
}) => {
  const userInitial = user?.name ? user.name.charAt(0).toUpperCase() : 'U'
  const isCompanyManager = user?.role === 'COMPANY_MANAGER'

  return (
    <div className={`card bg-base-100 border border-base-300 shadow-sm p-6 ${className}`.trim()}>
      <div className="flex flex-col sm:flex-row items-center sm:items-start gap-5">
        {/* Avatar initial badge */}
        <div className="w-16 h-16 rounded-full bg-primary text-primary-content flex items-center justify-center text-2xl font-bold select-none shrink-0 shadow-sm">
          {userInitial}
        </div>

        <div className="flex-1 text-center sm:text-left space-y-1">
          <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
            <h2 className="text-xl font-bold text-base-content">{user?.name}</h2>
            <span className="badge badge-neutral text-xs">
              {isCompanyManager ? 'Céges kezelő' : 'Álláskereső'}
            </span>
            {user?.companyName && (
              <span className="badge badge-outline text-xs">
                {user.companyName}
              </span>
            )}
          </div>
          <p className="text-sm text-base-content/70">{user?.email}</p>
          <p className="text-xs text-base-content/50">
            Fiók azonosító: #{user?.id}
          </p>

          {/* Applicant Skills Badges in Summary */}
          {!isCompanyManager && (
            <div className="pt-2">
              <span className="text-xs text-base-content/60 font-medium block mb-1.5">
                Rögzített készségek:
              </span>
              <SkillBadgeList
                skills={user?.skills}
                size="xs"
                className="justify-center sm:justify-start"
                emptyText="Még nincsenek készségek rögzítve. Az alábbi űrlapon adhatod meg őket az ajánlórendszer működéséhez."
              />
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default UserSummaryCard
