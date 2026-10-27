import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import type { AuthResponse, AuthUser, LoginRequest, RegisterRequest, UpdateProfileRequest } from '../types/auth'
import { authService } from '../services/authService'

export interface AuthContextType {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (payload: LoginRequest) => Promise<AuthResponse>
  register: (payload: RegisterRequest) => Promise<AuthResponse>
  updateProfile: (payload: UpdateProfileRequest) => Promise<AuthResponse>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(() => authService.getToken())
  const [user, setUser] = useState<AuthUser | null>(() => authService.getStoredUser())
  const [isLoading, setIsLoading] = useState<boolean>(true)

  useEffect(() => {
    const verifySession = async () => {
      const storedToken = authService.getToken()
      if (!storedToken) {
        setToken(null)
        setUser(null)
        setIsLoading(false)
        return
      }

      try {
        const verifiedUser = await authService.getMe(storedToken)
        setUser(verifiedUser)
        setToken(storedToken)
        // Ensure localStorage is in sync with latest user details
        authService.saveSession({ token: storedToken, user: verifiedUser })
      } catch (err) {
        console.warn('Session verification failed, clearing session', err)
        authService.clearSession()
        setToken(null)
        setUser(null)
      } finally {
        setIsLoading(false)
      }
    }

    verifySession()
  }, [])

  const login = async (payload: LoginRequest): Promise<AuthResponse> => {
    const response = await authService.login(payload)
    setToken(response.token)
    setUser(response.user)
    return response
  }

  const register = async (payload: RegisterRequest): Promise<AuthResponse> => {
    const response = await authService.register(payload)
    setToken(response.token)
    setUser(response.user)
    return response
  }

  const updateProfile = async (payload: UpdateProfileRequest): Promise<AuthResponse> => {
    const currentToken = token || authService.getToken()
    if (!currentToken) {
      throw new Error('Nincs érvényes bejelentkezett munkamenet.')
    }
    const response = await authService.updateProfile(currentToken, payload)
    setToken(response.token)
    setUser(response.user)
    return response
  }

  const logout = () => {
    authService.clearSession()
    setToken(null)
    setUser(null)
  }

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading,
    login,
    register,
    updateProfile,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
