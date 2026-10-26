import { useState, useEffect } from 'react'
import { jobService } from '../services/jobService'
import type { Job } from '../types/job'

interface UseJobsOptions {
  search?: string
  badge?: string
}

export function useJobs(options?: UseJobsOptions) {
  const [jobs, setJobs] = useState<Job[]>([])
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()
    setIsLoading(true)
    setError(null)

    jobService
      .getJobs({ signal: controller.signal, search: options?.search, badge: options?.badge })
      .then((data) => {
        setJobs(data)
        setIsLoading(false)
      })
      .catch((err) => {
        if (err.name !== 'AbortError') {
          setError('Nem sikerült betölteni az állásajánlatokat.')
          setIsLoading(false)
        }
      })

    return () => {
      controller.abort()
    }
  }, [options?.search, options?.badge])

  return { jobs, isLoading, error }
}
