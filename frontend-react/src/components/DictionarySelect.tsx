import { useEffect, useState } from 'react'
import { Select } from 'antd'
import { getDictItems } from '../api/dictionaries'

interface DictionarySelectProps {
  dictType: string
  value?: string
  onChange?: (value: string) => void
  placeholder?: string
  disabled?: boolean
}

export default function DictionarySelect({ dictType, value, onChange, placeholder = '请选择', disabled }: DictionarySelectProps) {
  const [options, setOptions] = useState<{ label: string; value: string }[]>([])

  useEffect(() => {
    getDictItems(dictType).then((items) => {
      setOptions(items.filter((i) => i.enabled).map((i) => ({ label: i.label, value: i.value })))
    })
  }, [dictType])

  return (
    <Select
      value={value}
      onChange={onChange}
      options={options}
      placeholder={placeholder}
      disabled={disabled}
      allowClear
      showSearch
      optionFilterProp="label"
      style={{ width: '100%' }}
    />
  )
}
