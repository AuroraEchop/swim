import request from './request'
import type { LoginResponse, CurrentUser } from '../types/auth'

export function login(username: string, password: string) {
  return request.post<unknown, LoginResponse>('/auth/login', { username, password })
}

export function getCurrentUser() {
  return request.get<unknown, CurrentUser>('/auth/me')
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request.put<unknown, null>('/auth/password', { oldPassword, newPassword })
}

export function logout() {
  return request.post<unknown, null>('/auth/logout')
}
