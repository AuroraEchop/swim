import { request } from './request'

export interface DictionaryItem {
  id: number
  dictType: string
  label: string
  value: string
  sort: number
  enabled: boolean
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface CreateDictionaryItemRequest {
  dictType: string
  label: string
  value: string
  sort?: number
  enabled?: boolean
  remark?: string
}

export interface UpdateDictionaryItemRequest {
  label: string
  value: string
  sort?: number
  enabled?: boolean
  remark?: string
}

export function getDictionaryItems(type: string) {
  return request.get<unknown, DictionaryItem[]>(`/dictionaries/${type}`)
}

export function createDictionaryItem(data: CreateDictionaryItemRequest) {
  return request.post<unknown, { id: number }>('/dictionaries', data)
}

export function updateDictionaryItem(id: number, data: UpdateDictionaryItemRequest) {
  return request.put<unknown, void>(`/dictionaries/${id}`, data)
}

export function deleteDictionaryItem(id: number) {
  return request.delete<unknown, void>(`/dictionaries/${id}`)
}
