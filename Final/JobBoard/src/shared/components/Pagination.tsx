import type { FC } from 'react'

export interface PaginationProps {
  currentPage: number
  totalPages: number
  totalItems: number
  pageSize: number
  onPageChange: (page: number) => void
  onPageSizeChange?: (size: number) => void
  pageSizeOptions?: number[]
  itemLabel?: string
  showCounter?: boolean
  showPageSizeSelector?: boolean
}

export const Pagination: FC<PaginationProps> = ({
  currentPage,
  totalPages,
  totalItems,
  pageSize,
  onPageChange,
  onPageSizeChange,
  pageSizeOptions = [4, 6, 8, 12],
  itemLabel = 'állásajánlat',
  showCounter = false,
  showPageSizeSelector = false,
}) => {
  if (totalItems === 0) {
    return null
  }

  const startIndex = (currentPage - 1) * pageSize + 1
  const endIndex = Math.min(currentPage * pageSize, totalItems)

  const getPageNumbers = () => {
    if (totalPages <= 7) {
      return Array.from({ length: totalPages }, (_, i) => i + 1)
    }

    const pages: (number | string)[] = []
    if (currentPage <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i)
      pages.push('...')
      pages.push(totalPages)
    } else if (currentPage >= totalPages - 3) {
      pages.push(1)
      pages.push('...')
      for (let i = totalPages - 4; i <= totalPages; i++) pages.push(i)
    } else {
      pages.push(1)
      pages.push('...')
      pages.push(currentPage - 1)
      pages.push(currentPage)
      pages.push(currentPage + 1)
      pages.push('...')
      pages.push(totalPages)
    }
    return pages
  }

  const pageNumbers = getPageNumbers()

  return (
    <nav
      className={`flex flex-col md:flex-row items-center ${
        showCounter || (showPageSizeSelector && onPageSizeChange)
          ? 'justify-between'
          : 'justify-center'
      } gap-4 mt-6 pt-4 border-t border-base-200`}
      aria-label="Lapozó navigáció"
    >
      {/* Informative counter */}
      {showCounter && (
        <div className="text-sm text-base-content/70 order-2 md:order-1">
          <span>
            <strong className="text-base-content font-medium">{startIndex}–{endIndex}</strong> /{' '}
            <strong className="text-base-content font-medium">{totalItems}</strong> {itemLabel}{' '}
            <span className="hidden sm:inline">
              ({currentPage}. oldal / {totalPages})
            </span>
          </span>
        </div>
      )}

      {/* Pagination button group (daisyUI join + join-item + btn) */}
      <div className="join order-1 md:order-2 flex-wrap justify-center">
        <button
          type="button"
          className="join-item btn btn-sm"
          disabled={currentPage <= 1}
          onClick={() => onPageChange(currentPage - 1)}
          aria-label="Előző oldal"
        >
          « Előző
        </button>

        {pageNumbers.map((p, idx) => {
          if (p === '...') {
            return (
              <button
                key={`ellipsis-${idx}`}
                type="button"
                disabled
                className="join-item btn btn-sm btn-disabled cursor-default"
                aria-hidden="true"
              >
                ...
              </button>
            )
          }

          const pageNum = p as number
          const isActive = pageNum === currentPage

          return (
            <button
              key={pageNum}
              type="button"
              className={`join-item btn btn-sm ${
                isActive ? 'btn-active btn-primary font-bold' : ''
              }`}
              onClick={() => onPageChange(pageNum)}
              aria-current={isActive ? 'page' : undefined}
              aria-label={`${pageNum}. oldal`}
            >
              {pageNum}
            </button>
          )
        })}

        <button
          type="button"
          className="join-item btn btn-sm"
          disabled={currentPage >= totalPages}
          onClick={() => onPageChange(currentPage + 1)}
          aria-label="Következő oldal"
        >
          Következő »
        </button>
      </div>

      {/* Page size selector */}
      {showPageSizeSelector && onPageSizeChange && pageSizeOptions && pageSizeOptions.length > 0 && (
        <div className="flex items-center gap-2 text-sm text-base-content/70 order-3">
          <label htmlFor="page-size-select" className="shrink-0">
            Megjelenítés:
          </label>
          <select
            id="page-size-select"
            className="select select-bordered select-sm"
            value={pageSize}
            onChange={(e) => onPageSizeChange(Number(e.target.value))}
            aria-label="Találatok száma oldalanként"
          >
            {pageSizeOptions.map((opt) => (
              <option key={opt} value={opt}>
                {opt} / oldal
              </option>
            ))}
          </select>
        </div>
      )}
    </nav>
  )
}
