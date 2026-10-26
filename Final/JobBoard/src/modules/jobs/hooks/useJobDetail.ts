import { useState, useEffect } from 'react'
import { jobService } from '../services/jobService'
import type { Job } from '../types/job'

export function useJobDetail(id: number | null) {
  const [job, setJob] = useState<Job | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (id === null || isNaN(id)) {
      setIsLoading(false)
      setError('Érvénytelen állás azonosító.')
      return
    }

    const controller = new AbortController()
    setIsLoading(true)
    setError(null)

    jobService
      .getJobById(id, { signal: controller.signal })
      .then((data) => {
        if (!data) {
          setError('A keresett állás nem található.')
        } else {
          setJob(data)
        }
        setIsLoading(false)
      })
      .catch((err) => {
        if (err.name !== 'AbortError') {
          setError('Hiba történt az állás betöltése közben.')
          setIsLoading(false)
        }
      })

    return () => {
      controller.abort()
    }
  }, [id])

  return { job, isLoading, error }
}
