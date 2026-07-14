import axios from 'axios'
import { clearStoredAuth, getStoredToken } from './authStorage'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') {
      return response
    }
    const payload = response.data
    if (!payload || payload.code !== 200) {
      if (payload?.code === 401) {
        clearStoredAuth()
      }
      return Promise.reject(new Error(payload?.message || '请求失败'))
    }
    return payload.data
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.data?.code === 401) {
      clearStoredAuth()
    }
    return Promise.reject(new Error(error.response?.data?.message || error.message || '网络异常'))
  },
)

export default request
