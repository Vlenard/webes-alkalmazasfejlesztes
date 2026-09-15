import type { Job, JobApplication } from '../types/job'

const PRESET_JOBS: Job[] = [
  {
    id: 1,
    title: 'Senior Frontend Fejlesztő',
    company: 'TechCorp Hungary',
    type: 'Full-time',
    location: 'Budapest / Hibrid',
    badge: 'React',
    salary: '1 200 000 - 1 600 000 HUF',
    description: 'Modern React + TypeScript alapú SPA fejlesztése kiemelt vállalati partnerek számára.',
    requirements: ['3+ év React tapasztalat', 'TypeScript és modern tooling ismerete', 'Tailwind CSS / daisyUI gyakorlat'],
    postedAt: '2026-09-14',
  },
  {
    id: 2,
    title: 'Fullstack Fejlesztő (Node.js + React)',
    company: 'NextWave Digital',
    type: 'Remote',
    location: 'Remote',
    badge: 'Node.js',
    salary: '1 000 000 - 1 400 000 HUF',
    description: 'Skálázható mikroszolgáltatások és reszponzív felületek tervezése és implementálása.',
    requirements: ['Node.js / Express vagy NestJS', 'React és REST API tervezés', 'PostgreSQL vagy MongoDB'],
    postedAt: '2026-09-13',
  },
  {
    id: 3,
    title: 'UI/UX & Product Designer',
    company: 'InnoStudio',
    type: 'Part-time',
    location: 'Budapest',
    badge: 'Figma',
    salary: '600 000 - 800 000 HUF',
    description: 'Felhasználóbarát felületek, design rendszerek és prototípusok tervezése Figma környezetben.',
    requirements: ['Figma komponens-rendszerek ismerete', 'Design system tapasztalat', 'User journey mapping'],
    postedAt: '2026-09-12',
  },
  {
    id: 4,
    title: 'DevOps & Cloud Mérnök',
    company: 'CloudScale Solutions',
    type: 'Full-time',
    location: 'Remote / Budapest',
    badge: 'Kubernetes',
    salary: '1 300 000 - 1 800 000 HUF',
    description: 'CI/CD pipeline-ok karbantartása, konténerizáció és felhő infrastruktúra automatizáció.',
    requirements: ['Docker, Kubernetes', 'GitHub Actions / GitLab CI', 'AWS vagy GCP tapasztalat'],
    postedAt: '2026-09-10',
  },
]

// In-memory store for submitted applications
const PRESET_APPLICATIONS: JobApplication[] = []

// Simulate realistic network latency
const delay = (ms: number, signal?: AbortSignal) =>
  new Promise((resolve, reject) => {
    const timer = setTimeout(resolve, ms)
    signal?.addEventListener('abort', () => {
      clearTimeout(timer)
      reject(new DOMException('Request aborted', 'AbortError'))
    })
  })

export const jobService = {
  /**
   * Fetches the list of all available jobs.
   */
  async getJobs(options?: { signal?: AbortSignal; search?: string; badge?: string }): Promise<Job[]> {
    await delay(350, options?.signal)

    let results = [...PRESET_JOBS]

    if (options?.search) {
      const q = options.search.toLowerCase()
      results = results.filter(
        (j) => j.title.toLowerCase().includes(q) || j.company.toLowerCase().includes(q)
      )
    }

    if (options?.badge) {
      results = results.filter((j) => j.badge.toLowerCase() === options.badge?.toLowerCase())
    }

    return results
  },

  /**
   * Fetches a single job by its ID.
   */
  async getJobById(id: number, options?: { signal?: AbortSignal }): Promise<Job | null> {
    await delay(250, options?.signal)
    const found = PRESET_JOBS.find((j) => j.id === id)
    return found ? { ...found } : null
  },

  /**
   * Submits a job application (simulating an HTTP POST).
   */
  async submitApplication(application: Omit<JobApplication, 'submittedAt'>): Promise<{ success: boolean; message: string }> {
    await delay(500)
    
    // Simple mock validation
    if (!application.applicantName.trim() || !application.applicantEmail.trim()) {
      throw new Error('A név és az email megadása kötelező.')
    }

    const newApplication: JobApplication = {
      ...application,
      submittedAt: new Date().toISOString(),
    }

    PRESET_APPLICATIONS.push(newApplication)
    return {
      success: true,
      message: 'A jelentkezést sikeresen rögzítettük!',
    }
  },
}
