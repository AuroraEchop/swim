import request from './request'
import type { PageQuery, PageResult } from '../types/api'

export type SettlementStatus = 'UNSETTLED' | 'PARTIAL' | 'SETTLED'

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
  settledAt?: string | null
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface SettlementQuery extends PageQuery {
  keyword?: string
  transportOrderId?: number
  customerName?: string
  status?: SettlementStatus
  startDate?: string
  endDate?: string
}

export interface CreateSettlementRequest {
  transportOrderId: number
  freightAmount: number
  additionalFee?: number
  discountAmount?: number
  receivedAmount?: number
  remark?: string
}

export interface UpdateSettlementRequest {
  freightAmount: number
  additionalFee?: number
  discountAmount?: number
  receivedAmount?: number
  remark?: string
}

export interface PaymentRequest {
  receivedAmount: number
  paymentTime: string
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
  return request.put<unknown, null>(`/settlements/${id}`, data)
}

export function updatePayment(id: number, data: PaymentRequest) {
  return request.patch<unknown, { status: string; settledAt: string | null }>(`/settlements/${id}/payment`, data)
}

export function deleteSettlement(id: number) {
  return request.delete<unknown, null>(`/settlements/${id}`)
}
