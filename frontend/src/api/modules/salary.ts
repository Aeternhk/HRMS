import { request } from '@/utils/request'

// ==================== 薪资管理接口 ====================

export interface SalaryRecord {
  id: number
  userId: number
  employeeNo: string
  name: string
  department: string
  departmentId: number
  month: string
  baseSalary: number
  positionAllowance: number
  performance: number
  overtimePay: number
  bonus: number
  mealAllowance: number
  transportAllowance: number
  socialInsurance: number
  housingFund: number
  otherDeduction: number
  netSalary: number
  status: number
  statusText: string
  grantDate?: string
  createTime?: string
}

export interface SalaryQuery {
  keyword?: string
  departmentId?: number | undefined
  month?: string
  status?: number | undefined
  page: number
  pageSize: number
}

// 分页查询薪资列表
export const getSalaryPage = (params: SalaryQuery) => {
  return request.get('/salary/page', { params })
}

// 获取薪资详情
export const getSalaryDetail = (id: number) => {
  return request.get<SalaryRecord>('/salary/' + id)
}

// 新建薪资记录
export const createSalary = (data: Partial<SalaryRecord>) => {
  return request.post('/salary', data)
}

// 更新薪资记录
export const updateSalary = (id: number, data: Partial<SalaryRecord>) => {
  return request.put('/salary/' + id, data)
}

// 删除薪资记录
export const deleteSalary = (id: number) => {
  return request.delete('/salary/' + id)
}

// 批量发放
export const grantSalaries = (ids: number[]) => {
  return request.post('/salary/grant', ids)
}
