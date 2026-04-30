import { request } from './request'

export interface Role {
  id: number
  roleName: string
  roleCode: string
  description?: string
  builtIn: boolean
}

export interface CreateRoleRequest {
  roleName: string
  roleCode: string
  description?: string
  permissions?: string[]
}

export interface UpdateRoleRequest {
  roleName: string
  description?: string
  permissions?: string[]
}

export function getRoles() {
  return request.get<unknown, Role[]>('/roles')
}

export function createRole(data: CreateRoleRequest) {
  return request.post<unknown, { id: number }>('/roles', data)
}

export function updateRole(id: number, data: UpdateRoleRequest) {
  return request.put<unknown, void>(`/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<unknown, void>(`/roles/${id}`)
}
