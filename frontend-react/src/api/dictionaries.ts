import request from './request'

export type DictType = 'SHIP_TYPE' | 'CARGO_TYPE' | 'PORT' | 'CREW_POSITION'

export interface DictItem {
  id: number
  dictType: string
  label: string
  value: string
  sort: number
  enabled: boolean
}

export interface CreateDictItemRequest {
  dictType: string
  label: string
  value: string
  sort?: number
  enabled?: boolean
}

export interface UpdateDictItemRequest {
  label: string
  value: string
  sort?: number
  enabled?: boolean
}

export function getDictItems(type: string) {
  return request.get<unknown, DictItem[]>(`/dictionaries/${type}`)
}

export function createDictItem(data: CreateDictItemRequest) {
  return request.post<unknown, { id: number }>('/dictionaries', data)
}

export function updateDictItem(id: number, data: UpdateDictItemRequest) {
  return request.put<unknown, null>(`/dictionaries/${id}`, data)
}

export function deleteDictItem(id: number) {
  return request.delete<unknown, null>(`/dictionaries/${id}`)
}
