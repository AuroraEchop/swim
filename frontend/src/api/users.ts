import { request } from './request'
import type { PageResult } from '../types/api'

export type UserStatus = 'ENABLED' | 'DISABLED'

export interface User {
  id: number
  username: string
  realName: string
  phone?: string
  email?: string
  roleId: number
  roleName: string
  roleCode: string
  status: UserStatus
  createdAt?: string
  updatedAt?: string
}

export interface UserQuery {
  username?: string
  realName?: string
  roleId?: number
  status?: UserStatus | ''
  page: number
  pageSize: number
}

export interface CreateUserRequest {
  username: string
  password: string
  realName: string
  phone?: string
  email?: string
  roleId: number
  status: UserStatus
}

export type UpdateUserRequest = Omit<CreateUserRequest, 'username' | 'password'>

export function getUsers(params: UserQuery) {
  return request.get<unknown, PageResult<User>>('/users', { params })
}

export function getUser(id: number) {
  return request.get<unknown, User>(`/users/${id}`)
}

export function createUser(data: CreateUserRequest) {
  return request.post<unknown, { id: number }>('/users', data)
}

export function updateUser(id: number, data: UpdateUserRequest) {
  return request.put<unknown, void>(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete<unknown, void>(`/users/${id}`)
}
