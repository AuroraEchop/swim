import type { TagProps } from 'antd'

export interface StatusMeta {
  label: string
  color: TagProps['color']
}

export const statusMap: Record<string, StatusMeta> = {
  ENABLED: { label: '启用', color: 'success' },
  DISABLED: { label: '停用', color: 'default' },
  IDLE: { label: '空闲', color: 'success' },
  SAILING: { label: '运输中', color: 'processing' },
  MAINTENANCE: { label: '维修中', color: 'warning' },
  ON_DUTY: { label: '在岗', color: 'success' },
  ON_LEAVE: { label: '休假', color: 'warning' },
  UNASSIGNED: { label: '待分配', color: 'default' },
  RESIGNED: { label: '离职', color: 'default' },
  PENDING: { label: '待出发', color: 'warning' },
  IN_TRANSIT: { label: '运输中', color: 'processing' },
  ARRIVED: { label: '已到达', color: 'success' },
  CANCELLED: { label: '已取消', color: 'default' },
  UNSETTLED: { label: '未结算', color: 'warning' },
  PARTIAL: { label: '部分结算', color: 'processing' },
  SETTLED: { label: '已结算', color: 'success' },
}

export const shipStatusOptions = [
  { label: '空闲', value: 'IDLE' },
  { label: '运输中', value: 'SAILING' },
  { label: '维修中', value: 'MAINTENANCE' },
  { label: '停用', value: 'DISABLED' },
]

export const crewStatusOptions = [
  { label: '在岗', value: 'ON_DUTY' },
  { label: '休假', value: 'ON_LEAVE' },
  { label: '待分配', value: 'UNASSIGNED' },
  { label: '离职', value: 'RESIGNED' },
]

export const transportStatusOptions = [
  { label: '待出发', value: 'PENDING' },
  { label: '运输中', value: 'IN_TRANSIT' },
  { label: '已到达', value: 'ARRIVED' },
  { label: '已取消', value: 'CANCELLED' },
]

export const settlementStatusOptions = [
  { label: '未结算', value: 'UNSETTLED' },
  { label: '部分结算', value: 'PARTIAL' },
  { label: '已结算', value: 'SETTLED' },
]

export const userStatusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' },
]

export function getStatusMeta(status?: string): StatusMeta {
  if (!status) return { label: '-', color: 'default' }
  return statusMap[status] || { label: status, color: 'default' }
}
