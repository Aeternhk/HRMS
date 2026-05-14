import { request } from '@/utils/request'
import type { Employee, EmployeeQuery, EmployeePageVO, Department, DepartmentTreeVO } from '@/api/types'

// 获取员工分页列表
export const getEmployeePage = (params: EmployeeQuery) => {
  return request.get<EmployeePageVO>('/employee/list', { params })
}

// 获取员工列表（别名，兼容调用）
export const getEmployeeList = (params: EmployeeQuery) => {
  return request.get<EmployeePageVO>('/employee/list', { params })
}

// 获取员工详情
export const getEmployeeById = (id: number) => {
  return request.get<Employee>('/employee/' + id)
}

// 新增员工
export const addEmployee = (data: Partial<Employee>) => {
  return request.post('/employee', data)
}

// 更新员工
export const updateEmployee = (data: Partial<Employee>) => {
  return request.put('/employee/' + data.id, data)
}

// 删除员工
export const deleteEmployee = (id: number) => {
  return request.delete('/employee/' + id)
}

// 批量删除员工
export const batchDeleteEmployee = (ids: number[]) => {
  return request.delete('/employee/batch', { data: { ids } })
}

// 批量导入员工
export const batchImportEmployee = (data: Partial<Employee>[]) => {
  return request.post<{ successCount: number; failCount: number; errors: string[] }>('/employee/batch', data)
}

// 导出员工
export const exportEmployee = (params?: Partial<EmployeeQuery>) => {
  return request.get('/employee/export', { params, responseType: 'blob' })
}

// ==================== 部门相关 ====================

// 获取部门树
export const getDepartmentTree = () => {
  return request.get<DepartmentTreeVO[]>('/department/tree')
}

// 获取部门列表
export const getDepartmentList = () => {
  return request.get<Department[]>('/department/list')
}

// 获取部门详情
export const getDepartmentById = (id: number) => {
  return request.get<Department>('/department/' + id)
}

// 新增部门
export const addDepartment = (data: Partial<Department>) => {
  return request.post('/department', data)
}

// 更新部门
export const updateDepartment = (data: Partial<Department>) => {
  return request.put('/department/' + data.id, data)
}

// 删除部门
export const deleteDepartment = (id: number) => {
  return request.delete('/department/' + id)
}

// 获取部门员工
export const getDepartmentEmployees = (departmentId: number) => {
  return request.get<Employee[]>('/department/' + departmentId + '/employees')
}

// 获取各部门员工数量统计
export const getDepartmentEmployeeCounts = () => {
  return request.get<Record<number, number>>('/department/employee-counts')
}
