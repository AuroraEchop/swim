import { request } from './request'

export interface DashboardSummary {
  shipCount: number
  crewCount: number
  pendingTransportCount: number
  inTransitCount: number
  unsettledCount: number
  totalReceivableAmount: number
  totalReceivedAmount: number
}

export interface RecentTransportOrder {
  id: number
  orderNo: string
  cargoName: string
  shipName: string
  originPort: string
  destinationPort: string
  status: string
  plannedDepartureTime: string
}

export function getDashboardSummary() {
  return request.get<unknown, DashboardSummary>('/dashboard/summary')
}

export function getRecentTransportOrders(limit = 5) {
  return request.get<unknown, RecentTransportOrder[]>('/dashboard/recent-transport-orders', {
    params: { limit },
  })
}
