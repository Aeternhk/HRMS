import { request } from '@/utils/request'

// 加班数据类型
export interface Overtime {
  id: number
  userId: number
  userName: string
  userUsername: string
  overtimeDate: string
  startTime: string
  endTime: string
  hours: number | string
  reason: string
  status: number
  statusName: string
  approverId?: number
  approverName?: string
  approveTime?: string
  createTime: string
}

export const getOvertimeList = (params: any) => {
  return request.get('/overtime/list', { params })
}

export const getOvertimeById = (id: number) => {
  return request.get(`/overtime/${id}`)
}

export const createOvertime = (data: Partial<Overtime>) => {
  return request.post('/overtime', data)
}

export const updateOvertime = (id: number, data: Partial<Overtime>) => {
  return request.put(`/overtime/${id}`, data)
}

export const deleteOvertime = (id: number) => {
  return request.delete(`/overtime/${id}`)
}

export const approveOvertime = (id: number, data: { status: number; approveRemark?: string; approverId?: number }) => {
  return request.put(`/overtime/${id}/approve`, data)
}

export const getOvertimeStats = () => {
  return request.get('/overtime/stats')
}
