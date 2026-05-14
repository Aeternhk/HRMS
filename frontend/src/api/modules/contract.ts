import { request } from '@/utils/request'

// 合同数据类型
export interface Contract {
  id: number
  userId: number
  userName: string
  userUsername: string
  contractType: string
  contractTypeName: string
  contractNo: string
  startDate: string
  endDate: string
  contractPeriod: number | null
  signDate: string
  status: number
  statusName: string
  remark: string
  createTime: string
}

export const getContractList = (params: any) => {
  return request.get('/contract/list', { params })
}

export const getContractById = (id: number) => {
  return request.get(`/contract/${id}`)
}

export const createContract = (data: Partial<Contract>) => {
  return request.post('/contract', data)
}

export const updateContract = (id: number, data: Partial<Contract>) => {
  return request.put(`/contract/${id}`, data)
}

export const deleteContract = (id: number) => {
  return request.delete(`/contract/${id}`)
}

export const terminateContract = (id: number, data: { remark?: string }) => {
  return request.put(`/contract/${id}/terminate`, data)
}

export const getExpiringContracts = () => {
  return request.get('/contract/expiring')
}

export const getContractStats = () => {
  return request.get('/contract/stats')
}
