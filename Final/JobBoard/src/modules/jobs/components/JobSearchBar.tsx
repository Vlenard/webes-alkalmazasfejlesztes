export interface JobSearchBarProps {
  searchTerm: string
  onSearchChange: (value: string) => void
  placeholder?: string
  className?: string
}

export const JobSearchBar = ({
  searchTerm,
  onSearchChange,
  placeholder = 'Keresés pozíció vagy cég alapján...',
  className = '',
}: JobSearchBarProps) => {
  return (
    <div
      className={`sticky top-20 z-40 bg-base-100 p-4 rounded-box shadow-md border border-base-300 ${className}`.trim()}
    >
      <div className="flex items-center gap-3">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-5 w-5 text-base-content/60 shrink-0"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <input
          type="text"
          placeholder={placeholder}
          className="input input-bordered w-full"
          value={searchTerm}
          onChange={(e) => onSearchChange(e.target.value)}
        />
        {searchTerm && (
          <button
            type="button"
            className="btn btn-ghost py-2 px-4"
            onClick={() => onSearchChange('')}
          >
            Törlés
          </button>
        )}
      </div>
    </div>
  )
}

export default JobSearchBar
