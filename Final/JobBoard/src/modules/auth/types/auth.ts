export type UserRole = 'APPLICANT' | 'COMPANY_MANAGER'

export interface AuthUser {
  id: number
  name: string
  email: string
  role: UserRole
  companyName?: string | null
}

export interface AuthResponse {
  token: string
  user: AuthUser
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  role: UserRole
  companyName?: string
}
