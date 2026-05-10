import { useEffect, useState } from 'react'
import { Select } from 'antd'
import { getTransportOrders } from '../api/transportOrders'

interface TransportOrderSelectProps {
  value?: number
  onChange?: (value: number) => void
  placeholder?: string
  disabled?: boolean
}

export default function TransportOrderSelect({ value, onChange, placeholder = '请选择运输任务', disabled }: TransportOrderSelectProps) {
  const [options, setOptions] = useState<{ label: string; value: number }[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    setLoading(true)
    getTransportOrders({ pageSize: 100 })
      .then((res) => {
        setOptions(res.records.map((t) => ({ label: `${t.orderNo} - ${t.cargoName}`, value: t.id })))
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
