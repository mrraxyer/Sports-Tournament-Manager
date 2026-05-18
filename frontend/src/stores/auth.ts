import { defineStore } from 'pinia'
import axios from 'axios'
import api from '../services/api'

const STORAGE_KEY = 'stm.auth.session'

export interface UsuarioSesion {
  usuariosId: number | null
  nombre: string
  correo: string
  rol: string | null
}

export interface LoginSession {
  accessToken: string
  tokenType: string
  expiresAt: string
  usuario: UsuarioSesion
}

interface ApiResponse<T> {
  status: number
  message: string
  data: T
  timestamp?: string
  errors?: string[]
  path?: string
}

interface LoginPayload {
  correo: string
  contrasena: string
}

function normalizeSession(payload: LoginSession): LoginSession {
  return {
    accessToken: payload.accessToken,
    tokenType: payload.tokenType || 'Bearer',
    expiresAt: payload.expiresAt,
    usuario: {
      usuariosId: payload.usuario?.usuariosId ?? null,
      nombre: payload.usuario?.nombre ?? '',
      correo: payload.usuario?.correo ?? '',
      rol: payload.usuario?.rol ?? null,
    },
  }
}

function readStoredSession(): LoginSession | null {
  if (typeof window === 'undefined') {
    return null
  }

  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return null
  }

  try {
    return normalizeSession(JSON.parse(raw) as LoginSession)
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

function persistSession(session: LoginSession | null) {
  if (typeof window === 'undefined') {
    return
  }

  if (session) {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } else {
    window.localStorage.removeItem(STORAGE_KEY)
  }
}

function setAuthorizationHeader(session: LoginSession | null) {
  if (session?.accessToken) {
    api.defaults.headers.common.Authorization = `${session.tokenType || 'Bearer'} ${session.accessToken}`
    return
  }

  delete api.defaults.headers.common.Authorization
}

function resolveLoginError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.'
    }

    const backendMessage =
      (error.response.data as { message?: string; errors?: string[] } | undefined)?.message ??
      (error.response.data as { errors?: string[] } | undefined)?.errors?.[0]

    if (error.response.status === 401) {
      return backendMessage ?? 'Credenciales incorrectas. Revisa tu correo y contraseña.'
    }

    if (error.response.status >= 500) {
      return backendMessage ?? 'El servidor presentó un problema al iniciar sesión.'
    }

    return backendMessage ?? 'No fue posible completar el inicio de sesión.'
  }

  if (error instanceof Error) {
    return error.message
  }

  return 'No fue posible completar el inicio de sesión.'
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    session: readStoredSession() as LoginSession | null,
    loading: false,
    error: '' as string,
  }),

  getters: {
    isAuthenticated(state): boolean {
      return Boolean(state.session?.accessToken)
    },
  },

  actions: {
    hydrateSession() {
      const storedSession = readStoredSession()
      this.session = storedSession
      setAuthorizationHeader(storedSession)
    },

    async login(credentials: LoginPayload) {
      this.loading = true
      this.error = ''

      try {
        const response = await api.post<ApiResponse<LoginSession>>('/auth/login', credentials)
        const payload = response.data.data

        if (!payload?.accessToken) {
          throw new Error('La respuesta de autenticación no incluyó un token válido.')
        }

        const session = normalizeSession(payload)
        this.session = session
        persistSession(session)
        setAuthorizationHeader(session)
        return session
      } catch (error) {
        const message = resolveLoginError(error)
        this.error = message
        return Promise.reject(new Error(message))
      } finally {
        this.loading = false
      }
    },

    logout() {
      this.session = null
      this.error = ''
      persistSession(null)
      setAuthorizationHeader(null)
    },
  },
})

