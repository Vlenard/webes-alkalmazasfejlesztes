import type {
  Job,
  JobStatus,
  JobApplication,
  ApplicantApplicationItem,
  EmployerApplicationItem,
  CreateJobPayload,
} from '../types/job'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
const TOKEN_KEY = 'jobboard_token'

function getAuthHeaders(): Record<string, string> {
  const token = localStorage.getItem(TOKEN_KEY)
  const headers: Record<string, string> = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  return headers
}

export const jobService = {
  /**
   * Fetches the list of all available active jobs from the backend API.
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
      headers: {
        ...getAuthHeaders(),
      },
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
      headers: {
        ...getAuthHeaders(),
      },
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
   * Fetches all jobs created by the currently logged-in employer (COMPANY_MANAGER).
   */
  async getMyEmployerJobs(): Promise<Job[]> {
    const response = await fetch(`${API_BASE_URL}/jobs/my`, {
      headers: {
        ...getAuthHeaders(),
      },
    })

    if (!response.ok) {
      const data = await response.json().catch(() => null)
      const errorMsg = data?.message || `Nem sikerült lekérni a saját hirdetéseket (${response.status})`
      throw new Error(errorMsg)
    }

    return response.json()
  },

  /**
   * Creates a new job posting (COMPANY_MANAGER).
   */
  async createJob(payload: CreateJobPayload): Promise<Job> {
    const response = await fetch(`${API_BASE_URL}/jobs`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
      body: JSON.stringify(payload),
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const errorMsg = data?.message || `Nem sikerült létrehozni az álláshirdetést (${response.status})`
      throw new Error(errorMsg)
    }

    return data
  },

  /**
   * Updates a job's status (ACTIVE or CLOSED). Only allowed for the creator employer.
   */
  async updateJobStatus(jobId: number, status: JobStatus): Promise<Job> {
    const response = await fetch(`${API_BASE_URL}/jobs/${jobId}/status`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
      body: JSON.stringify({ status }),
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const errorMsg = data?.message || `Nem sikerült módosítani a hirdetés státuszát (${response.status})`
      throw new Error(errorMsg)
    }

    return data
  },

  /**
   * Submits a job application with a multipart resume file (.pdf or .docx).
   */
  async submitApplicationWithFile(data: {
    jobId: number
    applicantName: string
    applicantEmail: string
    resumeFile: File
  }): Promise<{ success: boolean; message: string }> {
    const formData = new FormData()
    formData.append('jobId', data.jobId.toString())
    formData.append('applicantName', data.applicantName)
    formData.append('applicantEmail', data.applicantEmail)
    formData.append('resume', data.resumeFile)

    const response = await fetch(`${API_BASE_URL}/applications`, {
      method: 'POST',
      headers: {
        ...getAuthHeaders(),
      },
      body: formData,
    })

    const responseData = await response.json().catch(() => null)

    if (!response.ok) {
      const message = responseData?.message || responseData?.error || 'Hiba történt a jelentkezés beküldése során.'
      throw new Error(message)
    }

    return {
      success: responseData?.success ?? true,
      message: responseData?.message ?? 'A jelentkezést sikeresen rögzítettük!',
    }
  },

  /**
   * Submits a job application (legacy JSON format fallback).
   */
  async submitApplication(application: Omit<JobApplication, 'submittedAt'>): Promise<{ success: boolean; message: string }> {
    const response = await fetch(`${API_BASE_URL}/applications`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
      body: JSON.stringify(application),
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const message = data?.message || data?.error || 'Hiba történt a jelentkezés beküldése során.'
      throw new Error(message)
    }

    return {
      success: data?.success ?? true,
      message: data?.message ?? 'A jelentkezést sikeresen rögzítettük!',
    }
  },

  /**
   * Fetches all applications of the currently logged-in applicant.
   */
  async getMyApplications(): Promise<ApplicantApplicationItem[]> {
    const response = await fetch(`${API_BASE_URL}/applications/my`, {
      headers: {
        ...getAuthHeaders(),
      },
    })

    if (!response.ok) {
      const data = await response.json().catch(() => null)
      const errorMsg = data?.message || `Nem sikerült lekérni a jelentkezéseket (${response.status})`
      throw new Error(errorMsg)
    }

    return response.json()
  },

  /**
   * Fetches all applications for a specific job (COMPANY_MANAGER).
   */
  async getJobApplications(jobId: number): Promise<EmployerApplicationItem[]> {
    const response = await fetch(`${API_BASE_URL}/jobs/${jobId}/applications`, {
      headers: {
        ...getAuthHeaders(),
      },
    })

    if (!response.ok) {
      const data = await response.json().catch(() => null)
      const errorMsg = data?.message || `Nem sikerült lekérni a jelentkezőket (${response.status})`
      throw new Error(errorMsg)
    }

    return response.json()
  },

  /**
   * Updates an application status (ACCEPTED or REJECTED) by an employer.
   */
  async updateApplicationStatus(
    applicationId: number,
    status: 'ACCEPTED' | 'REJECTED'
  ): Promise<EmployerApplicationItem> {
    const response = await fetch(`${API_BASE_URL}/applications/${applicationId}/status`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        ...getAuthHeaders(),
      },
      body: JSON.stringify({ status }),
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const errorMsg = data?.message || `Nem sikerült módosítani a jelentkezés állapotát (${response.status})`
      throw new Error(errorMsg)
    }

    return data
  },

  /**
   * Downloads an applicant's resume file as a binary blob and triggers browser download.
   */
  async downloadResume(applicationId: number, fallbackFileName = 'resume.pdf'): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/applications/${applicationId}/resume`, {
      headers: {
        ...getAuthHeaders(),
      },
    })

    if (!response.ok) {
      const data = await response.json().catch(() => null)
      const errorMsg = data?.message || `Nem sikerült letölteni az önéletrajzot (${response.status})`
      throw new Error(errorMsg)
    }

    let fileName = fallbackFileName
    const contentDisposition = response.headers.get('content-disposition')
    if (contentDisposition) {
      const match = contentDisposition.match(/filename=["']?([^"';]+)["']?/)
      if (match && match[1]) {
        fileName = match[1]
      }
    }

    const blob = await response.blob()
    const blobUrl = window.URL.createObjectURL(blob)
    const downloadAnchor = document.createElement('a')
    downloadAnchor.href = blobUrl
    downloadAnchor.download = fileName
    document.body.appendChild(downloadAnchor)
    downloadAnchor.click()
    downloadAnchor.remove()
    window.URL.revokeObjectURL(blobUrl)
  },
}
