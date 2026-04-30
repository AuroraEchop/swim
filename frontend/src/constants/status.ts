export type StatusTagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

export interface StatusMeta {
  label: string
  type: StatusTagType
}

export const statusMap: Record<string, StatusMeta> = {
  ENABLED: { label: '启用', type: 'success' },
  DISABLED: { label: '停用', type: 'info' },
  IDLE: { label: '空闲', type: 'success' },
  SAILING: { label: '运输中', type: 'primary' },
  MAINTENANCE: { label: '维修中', type: 'warning' },
  ON_DUTY: { label: '在岗', type: 'success' },
  ON_LEAVE: { label: '休假', type: 'warning' },
  UNASSIGNED: { label: '待分配', type: 'info' },
  RESIGNED: { label: '离职', type: 'info' },
  PENDING: { label: '待出发', type: 'warning' },
  IN_TRANSIT: { label: '运输中', type: 'primary' },
  ARRIVED: { label: '已到达', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' },
  UNSETTLED: { label: '未结算', type: 'warning' },
  PARTIAL: { label: '部分结算', type: 'primary' },
  SETTLED: { label: '已结算', type: 'success' },
}

export const shipStatusOptions = [
  { label: '空闲', value: 'IDLE' },
  { label: '运输中', value: 'SAILING' },
  { label: '维修中', value: 'MAINTENANCE' },
  { label: '停用', value: 'DISABLED' },
] as const

export const crewStatusOptions = [
  { label: '在岗', value: 'ON_DUTY' },
  { label: '休假', value: 'ON_LEAVE' },
  { label: '待分配', value: 'UNASSIGNED' },
  { label: '离职', value: 'RESIGNED' },
] as const

export const transportStatusOptions = [
  { label: '待出发', value: 'PENDING' },
  { label: '运输中', value: 'IN_TRANSIT' },
  { label: '已到达', value: 'ARRIVED' },
  { label: '已取消', value: 'CANCELLED' },
] as const

export const settlementStatusOptions = [
  { label: '未结算', value: 'UNSETTLED' },
  { label: '部分结算', value: 'PARTIAL' },
  { label: '已结算', value: 'SETTLED' },
] as const

export function getStatusMeta(status?: string): StatusMeta {
  if (!status) {
    return { label: '-', type: 'info' }
  }
  return statusMap[status] || { label: status, type: 'info' }
}
