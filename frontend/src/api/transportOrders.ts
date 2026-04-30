import { request } from './request'
import type { PageResult } from '../types/api'

export type TransportStatus = 'PENDING' | 'IN_TRANSIT' | 'ARRIVED' | 'CANCELLED'
export type SettlementStatus = 'UNSETTLED' | 'PARTIAL' | 'SETTLED'

export interface TransportOrder {
  id: number
  orderNo: string
  cargoName: string
  cargoType: string
  cargoWeight: number
  originPort: string
  destinationPort: string
  shipId: number
  shipName: string
  customerName: string
  customerPhone?: string
  plannedDepartureTime: string
  plannedArrivalTime: string
  actualDepartureTime?: string
  actualArrivalTime?: string
  status: TransportStatus
  settlementId?: number
  settlementStatus: SettlementStatus
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface TransportOrderQuery {
  keyword?: string
  shipId?: number
  originPort?: string
  destinationPort?: string
  status?: TransportStatus | ''
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}

export interface CreateTransportOrderRequest {
  cargoName: string
  cargoType: string
  cargoWeight: number
  originPort: string
  destinationPort: string
  shipId: number
  customerName: string
  customerPhone?: string
  plannedDepartureTime: string
  plannedArrivalTime: string
  remark?: string
}

export type UpdateTransportOrderRequest = CreateTransportOrderRequest

export interface UpdateTransportStatusRequest {
  status: TransportStatus
  actualDepartureTime?: string
  actualArrivalTime?: string
}

export function getTransportOrders(params: TransportOrderQuery) {
  return request.get<unknown, PageResult<TransportOrder>>('/transport-orders', { params })
}

export function getTransportOrder(id: number) {
  return request.get<unknown, TransportOrder>(`/transport-orders/${id}`)
}

export function createTransportOrder(data: CreateTransportOrderRequest) {
  return request.post<unknown, { id: number; orderNo: string }>('/transport-orders', data)
}

export function updateTransportOrder(id: number, data: UpdateTransportOrderRequest) {
  return request.put<unknown, void>(`/transport-orders/${id}`, data)
}

export function updateTransportOrderStatus(id: number, data: UpdateTransportStatusRequest) {
  return request.patch<unknown, void>(`/transport-orders/${id}/status`, data)
}

export function deleteTransportOrder(id: number) {
  return request.delete<unknown, void>(`/transport-orders/${id}`)
}
