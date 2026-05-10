import { Tag } from 'antd'
import { getStatusMeta } from '../constants/status'

interface StatusTagProps {
  status?: string
}

export default function StatusTag({ status }: StatusTagProps) {
  const { label, color } = getStatusMeta(status)
  return <Tag color={color}>{label}</Tag>
}
