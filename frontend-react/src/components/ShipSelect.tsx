import { useEffect, useState } from 'react'
import { Select } from 'antd'
import { getShips } from '../api/ships'

interface ShipSelectProps {
  value?: number
  onChange?: (value: number) => void
  placeholder?: string
  disabled?: boolean
}

export default function ShipSelect({ value, onChange, placeholder = '请选择船舶', disabled }: ShipSelectProps) {
  const [options, setOptions] = useState<{ label: string; value: number }[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    getShips({ pageSize: 100 })
      .then((res) => {
        setOptions(res.records.map((s) => ({ label: s.shipName, value: s.id })))
      })
      .finally(() => setLoading(false))
  }, [])

  return (
    <Select
      value={value}
      onChange={onChange}
      options={options}
      placeholder={placeholder}
      loading={loading}
      disabled={disabled}
      allowClear
      showSearch
      optionFilterProp="label"
      style={{ width: '100%' }}
    />
  )
}
