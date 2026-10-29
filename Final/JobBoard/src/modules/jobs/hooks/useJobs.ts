import { useState, useEffect, useCallback } from 'react'
import { jobService } from '../services/jobService'
import type { Job } from '../types/job'

export interface UseJobsOptions {
  search?: string
  badge?: string
  userSkillsKey?: string
}

export function useJobs(options?: UseJobsOptions) {
  const [jobs, setJobs] = useState<Job[]>([])
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  const fetchJobs = useCallback((signal?: AbortSignal) => {
    setIsLoading(true)
    setError(null)

    return jobService
      .getJobs({ signal, search: options?.search, badge: options?.badge })
      .then((data) => {
        setJobs(data)
        setIsLoading(false)
        return data
      })
      .catch((err) => {
        if (err?.name !== 'AbortError') {
          setError('Nem sikerült betölteni az állásajánlatokat.')
          setIsLoading(false)
        }
      })
  }, [options?.search, options?.badge])

  useEffect(() => {
    const controller = new AbortController()
    fetchJobs(controller.signal)

    return () => {
      controller.abort()
    }
  }, [fetchJobs, options?.userSkillsKey])

  const refetch = useCallback(() => {
    return fetchJobs()
  }, [fetchJobs])

  return { jobs, isLoading, error, refetch }
}
