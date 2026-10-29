export interface MatchScoreBadgeProps {
  score?: number | null
  showZero?: boolean
  className?: string
}

export const MatchScoreBadge = ({
  score,
  showZero = false,
  className,
}: MatchScoreBadgeProps) => {
  if (typeof score !== 'number' || Number.isNaN(score)) {
    return null
  }

  const rounded = Math.round(score)

  if (rounded <= 0 && !showZero) {
    return null
  }

  let colorClass = 'badge badge-ghost py-2 px-3 h-auto font-semibold'
  if (rounded >= 70) {
    colorClass = 'badge badge-success text-white py-2 px-3 h-auto font-semibold'
  } else if (rounded >= 30) {
    colorClass = 'badge badge-warning py-2 px-3 h-auto font-semibold'
  }

  const badgeClass = className ? `${colorClass} ${className}` : colorClass

  return (
    <span className={badgeClass}>
      {rounded}% illeszkedés
    </span>
  )
}

export default MatchScoreBadge
