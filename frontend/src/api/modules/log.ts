import { request } from '@/utils/request'

// 操作日志数据类型
export interface SysLogItem {
  id: number
  userId: number | null
  username: string
  operation: string
  method: string
  params: string
  ip: string
  createTime: string
}

export const getLogList = (params: any) => {
  return request.get('/log/list', { params })
}

export const getLogById = (id: number) => {
  return request.get(`/log/${id}`)
}

export const deleteLog = (id: number) => {
  return request.delete(`/log/${id}`)
}

export const clearLogs = () => {
  return request.delete('/log/clear')
}

export const getLogsByDateRange = (startDate: string, endDate: string) => {
  return request.get('/log/range', { params: { startDate, endDate } })
}

export const getLogStats = () => {
  return request.get('/log/stats')
}
