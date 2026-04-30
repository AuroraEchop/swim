import { request } from './request'
import type { CurrentUser, LoginResponse } from '../types/auth'

export interface LoginRequest {
  username: string
  password: string
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}

export function login(data: LoginRequest) {
  return request.post<unknown, LoginResponse>('/auth/login', data)
}

export function getCurrentUser() {
  return request.get<unknown, CurrentUser>('/auth/me')
}

export function updatePassword(data: UpdatePasswordRequest) {
  return request.put<unknown, void>('/auth/password', data)
}

export function logout() {
  return request.post<unknown, void>('/auth/logout')
}
