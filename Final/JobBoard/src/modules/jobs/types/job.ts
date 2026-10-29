export type JobStatus = 'ACTIVE' | 'CLOSED'
export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export interface Job {
  id: number
  title: string
  company: string
  type: 'Full-time' | 'Part-time' | 'Contract' | 'Remote'
  location: string
  badge: string
  salary: string
  description: string
  requirements: string[]
  postedAt: string
  status?: JobStatus
  createdByUserId?: number | null
  skills?: string[]
  matchScore?: number
  matchedSkills?: string[]
  missingSkills?: string[]
  recommended?: boolean
}

export interface ApplicantApplicationItem {
  id: number
  jobId: number
  jobTitle: string
  company: string
  location: string
  status: ApplicationStatus
  statusMessage: string
  resumeFileName?: string | null
  submittedAt: string
}

export interface EmployerApplicationItem {
  id: number
  jobId: number
  applicantName: string
  applicantEmail: string
  resumeFileName?: string | null
  status: ApplicationStatus
  hasResume: boolean
  submittedAt: string
}

export interface CreateJobPayload {
  title: string
  company: string
  type: 'Full-time' | 'Part-time' | 'Contract' | 'Remote'
  location: string
  badge: string
  salary: string
  description: string
  requirements: string[]
  skills?: string[]
}

export interface JobApplication {
  jobId: number
  applicantName: string
  applicantEmail: string
  resumeFileName?: string
  submittedAt: string
}
