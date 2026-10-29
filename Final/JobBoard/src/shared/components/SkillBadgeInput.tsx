import React, { useState } from 'react'
import { SkillBadge } from './SkillBadge'

export interface SkillBadgeInputProps {
  /** Currently selected skills */
  skills: string[]
  /** Callback fired when skills change */
  onChange: (skills: string[]) => void
  /** Disables input and remove interactions */
  disabled?: boolean
  /** Custom placeholder for text input */
  placeholder?: string
  /** Optional form label above input */
  label?: string
  /** Optional helper text below input */
  helperText?: string
  /** Optional list of quick-pick suggested skills */
  suggestedSkills?: string[]
}

/**
 * SkillBadgeInput component
 *
 * Responsive daisyUI 5 input for managing professional skills as badges.
 * Supports adding by typing and pressing Enter or clicking Add, deduplication,
 * and rendering selected and suggested skills via SkillBadge.
 */
export const SkillBadgeInput: React.FC<SkillBadgeInputProps> = ({
  skills,
  onChange,
  disabled = false,
  placeholder = 'Pl. React ismeret, TypeScript, Docker...',
  label,
  helperText,
  suggestedSkills = [],
}) => {
  const [inputValue, setInputValue] = useState('')

  const handleAddSkill = (skillToAdd?: string) => {
    if (disabled) return

    const raw = skillToAdd ?? inputValue
    const trimmed = raw.trim()
    if (!trimmed) return

    const isDuplicate = skills.some(
      (existing) => existing.trim().toLowerCase() === trimmed.toLowerCase()
    )

    if (!isDuplicate) {
      onChange([...skills, trimmed])
    }

    if (!skillToAdd) {
      setInputValue('')
    }
  }

  const addSkill = handleAddSkill

  const handleRemoveSkill = (skillToRemove: string) => {
    if (disabled) return
    onChange(skills.filter((skill) => skill !== skillToRemove))
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleAddSkill()
    }
  }

  const availableSuggestions = suggestedSkills.filter(
    (suggestion) =>
      !skills.some(
        (existing) => existing.trim().toLowerCase() === suggestion.trim().toLowerCase()
      )
  )

  return (
    <div className="w-full form-control">
      {label && (
        <label className="label pb-1.5 whitespace-normal">
          <span className="label-text font-medium text-base-content whitespace-normal break-words">{label}</span>
        </label>
      )}

      {/* Input and Add Button */}
      <div className="join w-full">
        <input
          type="text"
          className="input input-bordered join-item flex-1 focus:outline-none"
          placeholder={placeholder}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={disabled}
          aria-label={label || 'Készség megadása'}
        />
        <button
          type="button"
          className="btn btn-primary join-item"
          onClick={() => handleAddSkill()}
          disabled={disabled || !inputValue.trim()}
        >
          Hozzáadás
        </button>
      </div>

      {/* Existing Skill Badges */}
      {skills.length > 0 && (
        <div className="flex flex-wrap gap-2 mt-3" aria-label="Kiválasztott készségek">
          {skills.map((skill, index) => (
            <SkillBadge
              key={`${skill}-${index}`}
              skill={skill}
              variant="neutral"
              onRemove={handleRemoveSkill}
              disabled={disabled}
            />
          ))}
        </div>
      )}

      {/* Suggested Skills */}
      {availableSuggestions.length > 0 && (
        <div className="mt-2.5">
          <div className="text-xs text-base-content/60 mb-1.5 font-medium">Javasolt készségek:</div>
          <div className="flex flex-wrap gap-1.5">
            {availableSuggestions.map((suggestion) => (
              <SkillBadge
                key={suggestion}
                skill={suggestion}
                variant="suggestion"
                onClick={addSkill}
                disabled={disabled}
              />
            ))}
          </div>
        </div>
      )}

      {/* Helper Text */}
      {helperText && (
        <div className="label pt-1.5 whitespace-normal">
          <span className="label-text-alt text-base-content/70 whitespace-normal break-words">{helperText}</span>
        </div>
      )}
    </div>
  )
}

export default SkillBadgeInput
