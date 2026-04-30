import axios from 'axios'
import type { ApiResponse } from '../types/api'
import { ElMessage } from 'element-plus'

export const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('shipping_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && typeof body === 'object' && 'code' in body) {
      return body.data
    }
    return response.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || '请求失败，请稍后重试'
    if (status === 401) {
      localStorage.removeItem('shipping_token')
      localStorage.removeItem('shipping_user')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)
