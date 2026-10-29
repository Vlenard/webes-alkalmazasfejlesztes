import type { FC } from 'react'

export interface StatusBadgeProps {
  status: 'ACTIVE' | 'CLOSED' | 'PENDING' | 'ACCEPTED' | 'REJECTED'
  perspective?: 'applicant' | 'employer'
  closedDetailText?: boolean
  className?: string
}

/**
 * Elementary status badge component for job postings and applications.
 * Adheres strictly to daisyUI badge styling without any decorative emojis.
 */
export const StatusBadge: FC<StatusBadgeProps> = ({
  status,
  perspective = 'employer',
  closedDetailText = false,
  className = '',
}) => {
  const getBadgeConfig = () => {
    switch (status) {
      case 'ACTIVE':
        return {
          baseClass: 'badge badge-success text-white py-2 px-4 h-auto font-medium',
          label: 'Aktív',
        }
      case 'CLOSED':
        if (closedDetailText) {
          return {
            baseClass: 'badge badge-neutral py-2 px-4 h-auto font-medium',
            label: 'Lezárva (Jelentkezőknek rejtett)',
          }
        }
        return {
          baseClass: 'badge badge-error text-white py-2 px-4 h-auto font-medium',
          label: 'Lezárva',
        }
      case 'ACCEPTED':
        return {
          baseClass: 'badge badge-success text-white py-2 px-4 h-auto font-semibold',
          label: perspective === 'applicant' ? 'Továbbjutott' : 'Elfogadva',
        }
      case 'REJECTED':
        return {
          baseClass: 'badge badge-error text-white py-2 px-4 h-auto font-semibold',
          label: 'Elutasítva',
        }
      case 'PENDING':
        return {
          baseClass: 'badge badge-warning py-2 px-4 h-auto font-semibold',
          label: perspective === 'applicant' ? 'Elbírálás alatt' : 'Elbírálásra vár',
        }
      default:
        return {
          baseClass: 'badge badge-ghost py-2 px-4 h-auto font-medium',
          label: status,
        }
    }
  }

  const { baseClass, label } = getBadgeConfig()
  const finalClassName = className ? `${baseClass} ${className}` : baseClass

  return <span className={finalClassName}>{label}</span>
}

export default StatusBadge
