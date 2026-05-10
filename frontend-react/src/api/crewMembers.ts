import request from './request'
import type { PageQuery, PageResult } from '../types/api'

export type CrewStatus = 'ON_DUTY' | 'ON_LEAVE' | 'UNASSIGNED' | 'RESIGNED'

export interface CrewMember {
  id: number
  crewNo: string
  name: string
  gender: string
  phone: string
  certificateNo: string
  position: string
  shipId: number | null
  shipName: string | null
  status: CrewStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface CrewMemberQuery extends PageQuery {
  keyword?: string
  position?: string
  shipId?: number
  status?: CrewStatus
}

export interface CreateCrewMemberRequest {
  crewNo: string
  name: string
  gender: string
  phone: string
  certificateNo: string
  position: string
  shipId?: number
  status: CrewStatus
  remark?: string
}

export type UpdateCrewMemberRequest = Omit<CreateCrewMemberRequest, 'crewNo'>

export function getCrewMembers(params: CrewMemberQuery) {
  return request.get<unknown, PageResult<CrewMember>>('/crew-members', { params })
}

export function getCrewMember(id: number) {
  return request.get<unknown, CrewMember>(`/crew-members/${id}`)
}

export function createCrewMember(data: CreateCrewMemberRequest) {
  return request.post<unknown, { id: number }>('/crew-members', data)
}

export function updateCrewMember(id: number, data: UpdateCrewMemberRequest) {
  return request.put<unknown, null>(`/crew-members/${id}`, data)
}

export function updateCrewMemberStatus(id: number, status: CrewStatus) {
  return request.patch<unknown, null>(`/crew-members/${id}/status`, { status })
}

export function deleteCrewMember(id: number) {
  return request.delete<unknown, null>(`/crew-members/${id}`)
}
