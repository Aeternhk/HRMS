import { request } from '@/utils/request'

// 获取仪表盘统计数据
export const getDashboardStats = () => {
  return request.get('/dashboard/stats')
}

// 获取员工趋势数据
export const getEmployeeTrend = (months: number = 6) => {
  return request.get('/dashboard/employee/trend', { params: { months } })
}

// 获取部门人员分布
export const getDepartmentDistribution = () => {
  return request.get('/dashboard/department/distribution')
}

// 获取考勤统计数据
export const getAttendanceStats = () => {
  return request.get('/dashboard/attendance/stats')
}

// 获取待办事项
export const getTodoList = () => {
  return request.get('/dashboard/todo')
}

// 获取最近入职员工
export const getRecentEmployees = (limit: number = 5) => {
  return request.get('/dashboard/employee/recent', { params: { limit } })
}

// 获取即将到期合同
export const getExpiringContracts = (days: number = 30) => {
  return request.get('/dashboard/contract/expiring', { params: { days } })
}
