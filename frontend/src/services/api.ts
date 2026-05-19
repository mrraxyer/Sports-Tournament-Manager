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
  async (error) => {
    if (!axios.isAxiosError(error)) return Promise.reject(error)

    const originalRequest = (error.config ?? {}) as any
    // If unauthorized and request not yet retried, try refresh flow
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      if (typeof window !== 'undefined') {
        const rawSession = window.localStorage.getItem(STORAGE_KEY)
        if (rawSession) {
          try {
            const session = JSON.parse(rawSession) as { accessToken?: string; tokenType?: string }

            if (session.accessToken) {
              // Call refresh endpoint with the current token using a plain axios instance
              const refreshUrl = (api.defaults.baseURL ?? '/api') + '/auth/refresh'
              const refreshResp = await axios.post(
                refreshUrl,
                null,
                {
                  headers: {
                    Authorization: `${session.tokenType ?? 'Bearer'} ${session.accessToken}`,
                  },
                },
              )

              const newPayload = refreshResp?.data?.data
              if (newPayload?.accessToken) {
                // persist new session and retry original request with new header
                window.localStorage.setItem(STORAGE_KEY, JSON.stringify(newPayload))
                originalRequest.headers = originalRequest.headers ?? {}
                originalRequest.headers.Authorization = `${newPayload.tokenType ?? 'Bearer'} ${newPayload.accessToken}`
                return api(originalRequest)
              }
            }
          } catch (e) {
            // refresh failed — fall through to logout
          }
        }
        // no session or refresh failed: clear and redirect
        window.localStorage.removeItem(STORAGE_KEY)
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  },
)

export default api
