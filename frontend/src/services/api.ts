import axios from 'axios'

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
    const rawSession = window.localStorage.getItem('stm.auth.session')

    if (rawSession) {
      try {
        const session = JSON.parse(rawSession) as { accessToken?: string; tokenType?: string }
        if (session.accessToken) {
          config.headers = config.headers ?? {}
          config.headers.Authorization = `${session.tokenType ?? 'Bearer'} ${session.accessToken}`
        }
      } catch {
        window.localStorage.removeItem('stm.auth.session')
      }
    }
  }

  return config
})

export default api
