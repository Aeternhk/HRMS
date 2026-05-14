import { request } from '@/utils/request'

// 请假数据类型
export interface Leave {
  id: number
  userId: number
  userName: string
  userUsername: string
  leaveType: string
  leaveTypeName: string
  startDate: string
  endDate: string
  days: number | string
  reason: string
  status: number
  statusName: string
  approverId?: number
  approverName?: string
  approveTime?: string
  approveRemark?: string
  createTime: string
}

export const getLeaveList = (params: any) => {
  return request.get('/leave/list', { params })
}

export const getLeaveById = (id: number) => {
  return request.get(`/leave/${id}`)
}

export const createLeave = (data: Partial<Leave>) => {
  return request.post('/leave', data)
}

export const updateLeave = (id: number, data: Partial<Leave>) => {
  return request.put(`/leave/${id}`, data)
}

export const deleteLeave = (id: number) => {
  return request.delete(`/leave/${id}`)
}

export const approveLeave = (id: number, data: { status: number; approveRemark?: string; approverId?: number }) => {
  return request.put(`/leave/${id}/approve`, data)
}

export const getLeaveStats = () => {
  return request.get('/leave/stats')
}
