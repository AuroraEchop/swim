export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  page: number
  pageSize: number
  total: number
  totalPages: number
}
