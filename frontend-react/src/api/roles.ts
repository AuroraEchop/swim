import request from './request'

export interface Role {
  id: number
  roleName: string
  roleCode: string
  description?: string
  permissions?: string[]
}

export interface CreateRoleRequest {
  roleName: string
  roleCode: string
  description?: string
  permissions?: string[]
}

export type UpdateRoleRequest = Omit<CreateRoleRequest, 'roleCode'>

export function getRoles() {
  return request.get<unknown, Role[]>('/roles')
}

export function createRole(data: CreateRoleRequest) {
  return request.post<unknown, { id: number }>('/roles', data)
}

export function updateRole(id: number, data: UpdateRoleRequest) {
  return request.put<unknown, null>(`/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<unknown, null>(`/roles/${id}`)
}
