import type { Job, JobApplication } from '../types/job'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export const jobService = {
  /**
   * Fetches the list of all available jobs from the backend API.
   * Supports search keyword and technology badge query parameters.
   */
  async getJobs(options?: { signal?: AbortSignal; search?: string; badge?: string }): Promise<Job[]> {
    const params = new URLSearchParams()
    if (options?.search) {
      params.append('search', options.search)
    }
    if (options?.badge) {
      params.append('badge', options.badge)
    }

    const queryStr = params.toString() ? `?${params.toString()}` : ''
    const response = await fetch(`${API_BASE_URL}/jobs${queryStr}`, {
      signal: options?.signal,
    })

    if (!response.ok) {
      throw new Error(`Hiba az álláshirdetések betöltésekor (${response.status})`)
    }

    return response.json()
  },

  /**
   * Fetches a single job by its ID from the backend API.
   */
  async getJobById(id: number, options?: { signal?: AbortSignal }): Promise<Job | null> {
    const response = await fetch(`${API_BASE_URL}/jobs/${id}`, {
      signal: options?.signal,
    })

    if (response.status === 404) {
      return null
    }

    if (!response.ok) {
      throw new Error(`Hiba az álláshirdetés betöltésekor (${response.status})`)
    }

    return response.json()
  },

  /**
   * Submits a job application to the backend API (HTTP POST /api/applications).
   */
  async submitApplication(application: Omit<JobApplication, 'submittedAt'>): Promise<{ success: boolean; message: string }> {
    const response = await fetch(`${API_BASE_URL}/applications`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(application),
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const message = data?.message || 'Hiba történt a jelentkezés beküldése során.'
      throw new Error(message)
    }

    return {
      success: data?.success ?? true,
      message: data?.message ?? 'A jelentkezést sikeresen rögzítettük!',
    }
  },
}
