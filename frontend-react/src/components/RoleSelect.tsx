import { useEffect, useState } from 'react'
import { Select } from 'antd'
import { getRoles } from '../api/roles'

interface RoleSelectProps {
  value?: number
  onChange?: (value: number) => void
  placeholder?: string
  disabled?: boolean
}

export default function RoleSelect({ value, onChange, placeholder = '请选择角色', disabled }: RoleSelectProps) {
  const [options, setOptions] = useState<{ label: string; value: number }[]>([])

  useEffect(() => {
    getRoles().then((roles) => {
      setOptions(roles.map((r) => ({ label: r.roleName, value: r.id })))
    })
  }, [])

  return (
    <Select
      value={value}
      onChange={onChange}
      options={options}
      placeholder={placeholder}
      disabled={disabled}
      allowClear
      style={{ width: '100%' }}
    />
  )
}
