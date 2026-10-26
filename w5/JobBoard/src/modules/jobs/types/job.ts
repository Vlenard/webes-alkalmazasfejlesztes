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
}

export interface JobApplication {
  jobId: number
  applicantName: string
  applicantEmail: string
  resumeFileName?: string
  submittedAt: string
}
