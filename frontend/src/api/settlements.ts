import { request } from './request'
import type { PageResult } from '../types/api'
import type { SettlementStatus } from './transportOrders'

export interface Settlement {
  id: number
  settlementNo: string
  transportOrderId: number
  transportOrderNo: string
  customerName: string
  freightAmount: number
  additionalFee: number
  discountAmount: number
  receivableAmount: number
  receivedAmount: number
  status: SettlementStatus
  settledAt?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface SettlementQuery {
  keyword?: string
  transportOrderId?: number
  customerName?: string
  status?: SettlementStatus | ''
  startDate?: string
  endDate?: string
  page: number
  pageSize: number
}

export interface CreateSettlementRequest {
  transportOrderId: number
  freightAmount: number
  additionalFee?: number
  discountAmount?: number
  receivedAmount?: number
  remark?: string
}

export type UpdateSettlementRequest = Omit<CreateSettlementRequest, 'transportOrderId'>

export interface UpdateSettlementPaymentRequest {
  receivedAmount: number
  paymentTime?: string
}

export interface SettlementPaymentResponse {
  status: SettlementStatus
  settledAt?: string
}

export function getSettlements(params: SettlementQuery) {
  return request.get<unknown, PageResult<Settlement>>('/settlements', { params })
}

export function getSettlement(id: number) {
  return request.get<unknown, Settlement>(`/settlements/${id}`)
}

export function createSettlement(data: CreateSettlementRequest) {
  return request.post<unknown, { id: number; settlementNo: string }>('/settlements', data)
}

export function updateSettlement(id: number, data: UpdateSettlementRequest) {
  return request.put<unknown, void>(`/settlements/${id}`, data)
}

export function updateSettlementPayment(id: number, data: UpdateSettlementPaymentRequest) {
  return request.patch<unknown, SettlementPaymentResponse>(`/settlements/${id}/payment`, data)
}

export function deleteSettlement(id: number) {
  return request.delete<unknown, void>(`/settlements/${id}`)
}
