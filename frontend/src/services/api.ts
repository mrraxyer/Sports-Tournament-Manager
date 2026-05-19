import axios from 'axios'

const STORAGE_KEY = 'stm.auth.session'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const rawSession = window.localStorage.getItem(STORAGE_KEY)

    if (rawSession) {
      try {
        const session = JSON.parse(rawSession) as { accessToken?: string; tokenType?: string }
        if (session.accessToken) {
          config.headers = config.headers ?? {}
          config.headers.Authorization = `${session.tokenType ?? 'Bearer'} ${session.accessToken}`
        }
      } catch {
        window.localStorage.removeItem(STORAGE_KEY)
      }
    }
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        window.localStorage.removeItem(STORAGE_KEY)
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  },
)

export default api
