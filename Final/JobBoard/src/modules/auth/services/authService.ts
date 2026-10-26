import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest } from '../types/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'
export const TOKEN_KEY = 'jobboard_token'
export const USER_KEY = 'jobboard_user'

export const authService = {
  getToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },

  getStoredUser(): AuthUser | null {
    try {
      const data = localStorage.getItem(USER_KEY)
      return data ? JSON.parse(data) : null
    } catch {
      return null
    }
  },

  saveSession(auth: AuthResponse): void {
    try {
      localStorage.setItem(TOKEN_KEY, auth.token)
      localStorage.setItem(USER_KEY, JSON.stringify(auth.user))
    } catch (err) {
      console.error('Failed to save session to localStorage', err)
    }
  },

  clearSession(): void {
    try {
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    } catch (err) {
      console.error('Failed to clear session from localStorage', err)
    }
  },

  async login(payload: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const errorMsg = data?.message || data?.error || `Sikertelen bejelentkezés (${response.status})`
      throw new Error(errorMsg)
    }

    this.saveSession(data)
    return data
  },

  async register(payload: RegisterRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const errorMsg = data?.message || data?.error || `Sikertelen regisztráció (${response.status})`
      throw new Error(errorMsg)
    }

    this.saveSession(data)
    return data
  },

  async getMe(token: string): Promise<AuthUser> {
    const response = await fetch(`${API_BASE_URL}/auth/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    const data = await response.json().catch(() => null)

    if (!response.ok) {
      const errorMsg = data?.message || data?.error || `Nem sikerült lekérni a felhasználói adatokat (${response.status})`
      throw new Error(errorMsg)
    }

    return data
  },
}
