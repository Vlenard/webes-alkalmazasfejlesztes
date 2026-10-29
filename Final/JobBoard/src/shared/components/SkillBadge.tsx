import React from 'react'

export interface SkillBadgeProps {
  skill: string
  variant?: 'neutral' | 'matched' | 'missing' | 'outline' | 'suggestion'
  size?: 'xs' | 'sm' | 'md'
  onRemove?: (skill: string) => void
  onClick?: (skill: string) => void
  disabled?: boolean
  className?: string
}

/**
 * SkillBadge component
 *
 * Elementary skill badge component that standardizes skill label rendering
 * across the entire JobBoard application.
 */
export const SkillBadge: React.FC<SkillBadgeProps> = ({
  skill,
  variant = 'neutral',
  size = 'sm',
  onRemove,
  onClick,
  disabled = false,
  className = '',
}) => {
  const isSuggestionOrAction = (variant === 'suggestion' || !!onClick) && !onRemove

  if (isSuggestionOrAction) {
    const sizeClass = size === 'xs' ? 'badge-xs text-xs' : size === 'md' ? 'badge-md' : 'badge-sm'
    return (
      <button
        type="button"
        onClick={() => onClick?.(skill)}
        disabled={disabled}
        className={`badge badge-outline ${sizeClass} gap-1 py-2.5 px-2.5 hover:badge-neutral transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed ${className}`.trim()}
      >
        + {skill}
      </button>
    )
  }

  let badgeClasses = ''

  if (variant === 'matched') {
    const sizeClass = size === 'xs' ? 'badge-xs text-xs' : size === 'md' ? 'badge-md' : 'badge-sm'
    badgeClasses = `badge badge-success ${sizeClass} py-2 px-3 font-medium`
  } else if (variant === 'missing' || variant === 'outline') {
    const sizeClass = size === 'xs' ? 'badge-xs text-xs' : size === 'md' ? 'badge-md' : 'badge-sm'
    badgeClasses = `badge badge-outline ${sizeClass} py-2 px-3`
  } else {
    // variant === 'neutral' (default)
    if (onRemove) {
      badgeClasses =
        size === 'xs'
          ? 'badge badge-neutral gap-1.5 py-2 px-2.5 text-xs font-medium'
          : 'badge badge-neutral gap-1.5 py-3 px-3 text-sm font-medium'
    } else {
      if (size === 'xs') {
        badgeClasses = 'badge badge-neutral text-xs py-2 px-2.5'
      } else if (size === 'md') {
        badgeClasses = 'badge badge-neutral badge-md py-2 px-2.5 font-medium'
      } else {
        badgeClasses = 'badge badge-neutral badge-sm py-2 px-2.5 font-medium'
      }
    }
  }

  const finalClassName = `${badgeClasses} ${className}`.trim()

  return (
    <span className={finalClassName}>
      <span>{skill}</span>
      {onRemove && !disabled && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation()
            onRemove(skill)
          }}
          className="btn btn-ghost btn-xs btn-circle h-4 w-4 min-h-0 p-0 text-neutral-content/80 hover:text-neutral-content hover:bg-neutral-content/20"
          aria-label={`${skill} eltávolítása`}
          title={`${skill} eltávolítása`}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
            className="w-3.5 h-3.5"
            aria-hidden="true"
          >
            <path
              fillRule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clipRule="evenodd"
            />
          </svg>
        </button>
      )}
    </span>
  )
}

export default SkillBadge
