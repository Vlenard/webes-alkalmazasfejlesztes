import React, { useMemo } from 'react'
import { SkillBadge } from './SkillBadge'

export interface SkillBadgeListProps {
  skills?: string[]
  matchedSkills?: string[]
  variant?: 'neutral' | 'outline' | 'match-highlight'
  size?: 'xs' | 'sm' | 'md'
  emptyText?: string
  onRemoveSkill?: (skill: string) => void
  disabled?: boolean
  className?: string
}

/**
 * SkillBadgeList component
 *
 * Renders a list of skills using SkillBadge components with support for
 * case-insensitive matched skill highlighting, skill removal, and custom empty states.
 */
export const SkillBadgeList: React.FC<SkillBadgeListProps> = ({
  skills,
  matchedSkills,
  variant = 'neutral',
  size = 'sm',
  emptyText,
  onRemoveSkill,
  disabled = false,
  className = '',
}) => {
  const matchedSet = useMemo(() => {
    if (variant !== 'match-highlight' || !matchedSkills || matchedSkills.length === 0) {
      return null
    }
    return new Set(matchedSkills.map((s) => s.trim().toLowerCase()))
  }, [variant, matchedSkills])

  if (!skills || skills.length === 0) {
    if (emptyText) {
      return <p className={`text-xs text-base-content/50 italic ${className}`.trim()}>{emptyText}</p>
    }
    return null
  }

  const containerClasses = `flex flex-wrap items-center gap-1.5 ${className}`.trim()

  return (
    <div className={containerClasses}>
      {skills.map((skill, index) => {
        const badgeVariant =
          variant === 'match-highlight'
            ? matchedSet && matchedSet.has(skill.trim().toLowerCase())
              ? 'matched'
              : 'missing'
            : variant

        return (
          <SkillBadge
            key={`${skill}-${index}`}
            skill={skill}
            variant={badgeVariant}
            size={size}
            onRemove={onRemoveSkill}
            disabled={disabled}
          />
        )
      })}
    </div>
  )
}

export default SkillBadgeList
