import { request } from './request'
import type { PageResult } from '../types/api'

export type ShipStatus = 'IDLE' | 'SAILING' | 'MAINTENANCE' | 'DISABLED'

export interface Ship {
  id: number
  shipNo: string
  shipName: string
  shipType: string
  loadCapacity: number
  homePort?: string
  status: ShipStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface ShipQuery {
  keyword?: string
  type?: string
  homePort?: string
  status?: ShipStatus | ''
  page: number
  pageSize: number
}

export interface CreateShipRequest {
  shipNo: string
  shipName: string
  shipType: string
  loadCapacity: number
  homePort?: string
  status: ShipStatus
  remark?: string
}

export type UpdateShipRequest = Omit<CreateShipRequest, 'shipNo'>

export function getShips(params: ShipQuery) {
  return request.get<unknown, PageResult<Ship>>('/ships', { params })
}

export function getShip(id: number) {
  return request.get<unknown, Ship>(`/ships/${id}`)
}

export function createShip(data: CreateShipRequest) {
  return request.post<unknown, { id: number }>('/ships', data)
}

export function updateShip(id: number, data: UpdateShipRequest) {
  return request.put<unknown, void>(`/ships/${id}`, data)
}

export function updateShipStatus(id: number, status: ShipStatus) {
  return request.patch<unknown, void>(`/ships/${id}/status`, { status })
}

export function deleteShip(id: number) {
  return request.delete<unknown, void>(`/ships/${id}`)
}
