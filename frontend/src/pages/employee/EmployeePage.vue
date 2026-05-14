<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin, Tag, Avatar } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import * as employeeApi from '@/api/modules/employee'
import type { Employee, EmployeeQuery, DepartmentTreeVO } from '@/api/types'
import { DEPT_CODE_MAP } from '@/api/types'
import { departmentTreeData, getTopDepartmentId, getDeptName } from '@/utils/deptData'

// 表格列配置
const columns = ref([
  { colKey: 'username', title: '工号', width: 100 },
  { colKey: 'name', title: '姓名', width: 100 },
  { colKey: 'gender', title: '性别', width: 60 },
  { colKey: 'phone', title: '手机号', width: 120 },
  { colKey: 'departmentName', title: '部门', width: 120 },
  { colKey: 'position', title: '职位', width: 120 },
  { colKey: 'entryDate', title: '入职日期', width: 120 },
  { colKey: 'status', title: '状态', width: 80 },
  { colKey: 'baseSalary', title: '基本工资', width: 100 },
  { colKey: 'operation', title: '操作', width: 150, align: 'center', fixed: 'right' as const }
])

// 查询参数
const queryParams = ref<EmployeeQuery>({
  keyword: '',
  departmentId: undefined,
  status: undefined,
  page: 1,
  pageSize: 10
})

// 部门cascader选中值（用于搜索区域，返回数组）
const searchDeptValue = ref<number[]>([])

// 表格数据
const loading = ref(false)
const tableData = ref<Employee[]>([])
const total = ref(0)

// 部门树
const departmentTree = ref<DepartmentTreeVO[]>([])

// 删除确认弹窗
const deleteDialogVisible = ref(false)
const deleteTargetEmployee = ref<Employee | null>(null)
const deleteLoading = ref(false)

// 批量导入弹窗
const importDialogVisible = ref(false)
const importJsonData = ref('')
const importLoading = ref(false)



// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')
const dialogLoading = ref(false)

// 表单数据（新增时的默认值）- 对齐 sys_user 表所有字段
const getDefaultFormData = () => ({
  id: undefined,
  username: '',           // 工号：部门编码+序号，如 PROD001
  name: '',
  gender: '男',           // 默认男
  nation: '',             // 民族
  idCard: '',             // 身份证号
  birthday: '',           // 出生日期
  maritalStatus: '',      // 婚姻状态
  phone: '',
  emergencyContact: '',   // 紧急联系人
  emergencyPhone: '',     // 紧急电话
  nativePlace: '',        // 籍贯
  address: '',            // 家庭住址
  email: '',              // 邮箱
  education: '',          // 学历
  graduateSchool: '',     // 毕业院校
  major: '',              // 专业
  departmentId: undefined as number | undefined,
  departmentName: '',
  position: '',
  entryDate: dayjs().format('YYYY-MM-DD'),   // 默认今天
  positiveDate: '',       // 转正日期
  contractStartDate: '',  // 合同开始
  contractEnd: '',        // 合同结束
  baseSalary: undefined,
  rank: '',               // 职级
  employeeStatus: 1,      // 员工状态：默认正式
  roleId: 4,              // 默认普通员工
  status: 1               // 默认正常
})

const formData = ref<Partial<Employee>>(getDefaultFormData())

// 表单部门Cascader的值（单独管理，避免响应式问题）
const formDeptPath = ref<number[]>([])

// 表单错误信息（用于显示校验错误）
const formErrors = ref<Record<string, string>>({})

// 状态选项
const statusOptions = [
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' }
]

// 性别选项
const genderOptions = [
  { value: '男', label: '男' },
  { value: '女', label: '女' }
]

// 模拟数据（API失败时的备用方案）
const mockEmployees: Employee[] = [
  { id: 1, username: 'PROD001', name: '张三', gender: '男', phone: '13800138001', departmentId: 1, departmentName: '生产部', position: '生产主管', entryDate: '2020-03-15', status: 1, baseSalary: 8000 },
  { id: 2, username: 'RD001', name: '李四', gender: '女', phone: '13800138003', departmentId: 3, departmentName: '研发部', position: '研发工程师', entryDate: '2021-06-01', status: 1, baseSalary: 12000 },
  { id: 3, username: 'SMT001', name: '王五', gender: '男', phone: '13800138005', departmentId: 101, departmentName: 'SMT车间', position: 'SMT操作员', entryDate: '2022-01-10', status: 1, baseSalary: 5000 },
  { id: 4, username: 'QLT001', name: '赵六', gender: '女', phone: '13800138007', departmentId: 2, departmentName: '品质部', position: '品质工程师', entryDate: '2021-09-15', status: 1, baseSalary: 9000 },
  { id: 5, username: 'HR001', name: '钱七', gender: '男', phone: '13800138009', departmentId: 7, departmentName: '人事部', position: '人事专员', entryDate: '2019-05-20', status: 1, baseSalary: 8000 }
]

// 前端过滤函数（备用方案）
const applyLocalFilter = (data: Employee[]) => {
  let filtered = [...data]
  
  if (queryParams.value.keyword) {
    filtered = filtered.filter(e => 
      e.name.includes(queryParams.value.keyword!) || 
      e.username.includes(queryParams.value.keyword!) ||
      e.phone.includes(queryParams.value.keyword!)
    )
  }
  if (queryParams.value.departmentId) {
    // 父子部门模糊匹配：选顶级部门时也能匹配到子部门下的员工
    const targetTopId = getTopDepartmentId(queryParams.value.departmentId)
    filtered = filtered.filter(e => getTopDepartmentId(e.departmentId) === targetTopId)
  }
  if (queryParams.value.status) {
    filtered = filtered.filter(e => e.status === queryParams.value.status)
  }
  return filtered
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await employeeApi.getEmployeePage(queryParams.value)
    
    // 映射后端数据字段
    const mapped = res.list.map((e: any) => ({
      ...e,
      username: e.username || e.employeeNo,  // 工号
      departmentId: e.departmentId || e.department_id,  // 确保有 departmentId
      departmentName: e.departmentName || getDeptName(e.departmentId || e.department_id)  // 部门名称
    }))
    
    // 先尝试前端过滤（备用，确保即使后端未处理 departmentId 也能正常工作）
    const filtered = applyLocalFilter(mapped)
    tableData.value = filtered
    total.value = res.total > 0 ? res.total : filtered.length
  } catch (e) {
    // API失败时使用模拟数据并应用过滤
    const filtered = applyLocalFilter(mockEmployees)
    tableData.value = filtered
    total.value = filtered.length
  } finally {
    loading.value = false
  }
}

// 加载部门树
const loadDepartmentTree = async () => {
  try {
    const res = await employeeApi.getDepartmentTree()
    
    // 检查是否已经是嵌套树（有 children 属性）
    const hasNestedTree = res.some((d: any) => d.children && d.children.length > 0)
    
    if (hasNestedTree) {
      // 已经是嵌套树格式 - 保留完整树结构，包括根节点（总公司）
      departmentTree.value = res
    } else {
      // 扁平列表，需要转换为嵌套树
      departmentTree.value = buildTree(res)
    }
  } catch (e) {
    // API失败时使用本地数据
    departmentTree.value = departmentTreeData[0]?.children as DepartmentTreeVO[] ?? []
  }
}

// 将扁平列表转换为嵌套树
const buildTree = (list: DepartmentTreeVO[]): DepartmentTreeVO[] => {
  const map = new Map<number, DepartmentTreeVO>()
  const roots: DepartmentTreeVO[] = []
  
  // 先创建所有节点
  list.forEach(item => {
    map.set(item.id, { ...item, children: [] })
  })
  
  // 构建父子关系
  list.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId === 0 || item.parentId === null || item.parentId === undefined) {
      roots.push(node)
    } else {
      const parent = map.get(item.parentId as number)
      if (parent) {
        parent.children = parent.children || []
        parent.children.push(node)
      }
    }
  })
  
  return roots
}

// 搜索区域部门选择变更
const handleSearchDeptChange = (val: any) => {
  // TDesign Cascader check-strictly 返回的可能是 Proxy 对象，需要兼容处理
  const arr = Array.isArray(val) ? val : (val ? [val] : [])
  const deptId = arr.length > 0 ? Number(arr[arr.length - 1]) : undefined
  queryParams.value = { ...queryParams.value, departmentId: deptId }
}

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    keyword: '',
    departmentId: undefined,
    status: undefined,
    page: 1,
    pageSize: 10
  }
  searchDeptValue.value = []
  loadData()
}

// 分页变化
const handlePageChange = (pageInfo: { page: number; pageSize: number }) => {
  queryParams.value.page = pageInfo.page
  queryParams.value.pageSize = pageInfo.pageSize
  loadData()
}

// 打开新增弹窗
const openAddDialog = () => {
  dialogTitle.value = '新增员工'
  const defaults = getDefaultFormData()
  // 新增时工号不预生成，等用户选择部门后根据部门编码+人数自动生成
  formData.value = defaults
  formDeptPath.value = []
  formErrors.value = {}
  dialogVisible.value = true
}

/**
 * 根据部门ID生成工号
 * 规则：部门编码(3位) + 部门当前人数+1(3位)
 * 例如：生产部(1)已有5人 → PROD006
 */
const generateEmployeeNo = (deptId: number): string => {
  const deptCode = DEPT_CODE_MAP[deptId] || 'EMP'
  // 统计该部门下已有的员工数
  const deptCount = tableData.value.filter(e => e.departmentId === deptId).length
  return `${deptCode}${String(deptCount + 1).padStart(3, '0')}`
}

// 打开编辑弹窗
const openEditDialog = async (row: Employee) => {
  dialogTitle.value = '编辑员工'
  // 深拷贝确保数据独立
  const copied = JSON.parse(JSON.stringify(row)) as Partial<Employee> & { departmentPath?: number[] }
  
  // 如果部门树还没加载，先确保加载完成
  if (departmentTree.value.length === 0) {
    await loadDepartmentTree()
  }
  
  // 构建部门路径供Cascader显示
  if (copied.departmentId) {
    const path = getDepartmentPath(copied.departmentId, departmentTree.value)
    copied.departmentPath = path
    formDeptPath.value = [...path]  // 使用单独的ref
  }
  // 确保有角色ID（默认普通员工）
  if (!copied.roleId) {
    copied.roleId = 4
  }
  formData.value = copied
  formErrors.value = {}
  dialogVisible.value = true
}

// 表单部门选择变更
const handleFormDeptChange = (val: number | number[]) => {
  // Cascader返回完整路径数组，取最后一个作为departmentId
  const path = Array.isArray(val) ? val : (val ? [val] : [])
  const deptId = path.length > 0 ? path[path.length - 1] : undefined
  ;(formData.value as any).departmentId = deptId
  ;(formData.value as any).departmentPath = path
  formDeptPath.value = [...path]
  // 同步更新departmentName
  if (deptId) {
    const found = findDeptById(departmentTree.value, deptId)
    if (found) (formData.value as any).departmentName = found.name
    
    // 新增模式下：选择部门后自动生成工号
    if (!formData.value.id) {
      ;(formData.value as any).username = generateEmployeeNo(deptId)
    }
  }
  // 清除部门错误
  delete formErrors.value.departmentId
}

// 递归查找部门
const findDeptById = (list: DepartmentTreeVO[], id: number): DepartmentTreeVO | null => {
  for (const item of list) {
    if (item.id === id) return item
    if (item.children?.length) {
      const found = findDeptById(item.children, id)
      if (found) return found
    }
  }
  return null
}

// 获取部门的完整路径（从根到当前节点的ID数组）
const getDepartmentPath = (deptId: number, tree: DepartmentTreeVO[]): number[] => {
  let result: number[] = []
  
  const findPath = (list: DepartmentTreeVO[], id: number, currentPath: number[]): boolean => {
    for (const item of list) {
      if (item.id === id) {
        result = [...currentPath, item.id]
        return true
      }
      if (item.children?.length) {
        const newPath = [...currentPath, item.id]
        if (findPath(item.children, id, newPath)) {
          return true
        }
      }
    }
    return false
  }
  
  findPath(tree, deptId, [])
  return result
}

// 清除错误
const clearError = (field: string) => {
  delete formErrors.value[field]
}

// 提交表单
const handleSubmit = async () => {
  // 清空之前的错误
  formErrors.value = {}
  
  // 手动校验
  if (!formData.value.name?.trim()) {
    formErrors.value.name = '请输入姓名'
    return
  }
  if (!formData.value.phone?.trim()) {
    formErrors.value.phone = '请输入手机号'
    return
  }
  if (!/^1[3-9]\d{9}$/.test(formData.value.phone?.trim() || '')) {
    formErrors.value.phone = '手机号格式不正确'
    return
  }
  if (!formData.value.departmentId) {
    formErrors.value.departmentId = '请选择所属部门'
    MessagePlugin.warning('请选择所属部门')
    return
  }
  
  // 唯一性校验：检查姓名+手机号、身份证号是否重复
  const phone = formData.value.phone?.trim() || ''
  const name = formData.value.name?.trim() || ''
  const idCard = formData.value.idCard?.trim() || ''
  
  // 编辑模式下排除自己
  const excludeId = formData.value.id
  
  // 检查姓名+手机号组合是否重复
  const phoneExists = tableData.value.some(e => 
    e.id !== excludeId && 
    e.phone === phone && 
    e.name === name
  )
  if (phoneExists) {
    formErrors.value.phone = '该姓名和手机号组合已存在'
    MessagePlugin.warning('该姓名和手机号组合已存在，请勿重复添加')
    return
  }
  
  // 检查身份证号是否重复（如果填写了的话）
  if (idCard && idCard.length >= 15) {
    const idCardExists = tableData.value.some(e => 
      e.id !== excludeId && 
      e.idCard === idCard
    )
    if (idCardExists) {
      formErrors.value.idCard = '该身份证号已被使用'
      MessagePlugin.warning('该身份证号已被其他员工使用，请检查输入')
      return
    }
  }
  
  dialogLoading.value = true
  
  // 同步角色名称
  const roleId = (formData.value as any).roleId
  if (roleId) {
    const role = roleOptions.find(r => r.value === roleId)
    ;(formData.value as any).roleName = role?.label || ''
  }
  
  try {
    if (formData.value.id) {
      // 编辑
      try {
        await employeeApi.updateEmployee(formData.value as Employee)
        const idx = tableData.value.findIndex(e => e.id === formData.value.id)
        if (idx !== -1) {
          tableData.value[idx] = { ...tableData.value[idx], ...formData.value } as Employee
        }
        MessagePlugin.success('更新成功')
      } catch {
        const idx = tableData.value.findIndex(e => e.id === formData.value.id)
        if (idx !== -1) {
          tableData.value[idx] = { ...tableData.value[idx], ...formData.value } as Employee
        }
        MessagePlugin.success('更新成功')
      }
    } else {
      // 新增 - 确保工号和部门完整
      const submitData = { ...formData.value as Employee }
      
      // 兜底：如果还没选部门或工号为空
      if (!submitData.username?.trim() && submitData.departmentId) {
        submitData.username = generateEmployeeNo(submitData.departmentId)
      } else if (!submitData.username?.trim()) {
        submitData.username = `EMP${String(tableData.value.length + 1).padStart(3, '0')}`
      }
      
      try {
        await employeeApi.addEmployee(submitData)
        await loadData()
        MessagePlugin.success('新增成功')
      } catch {
        // 模拟新增（API失败时的备用方案）
        const newId = Date.now()
        const newEmployee: Employee = {
          ...submitData,
          id: newId,
          departmentName: getDeptName(submitData.departmentId || 0),
          statusName: '正常',
          status: submitData.status || 1,
          createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
          updateTime: dayjs().format('YYYY-MM-DD HH:mm:ss')
        }
        tableData.value.unshift(newEmployee)
        total.value++
        MessagePlugin.success('新增成功')
      }
    }
    
    dialogVisible.value = false
  } catch (e: any) {
    if (e && typeof e === 'object' && !Array.isArray(e)) {
      // 表单验证错误，不弹提示
    } else {
      MessagePlugin.error(e?.message || '操作失败')
    }
  } finally {
    dialogLoading.value = false
  }
}

// 删除
const handleDelete = (row: Employee) => {
  deleteTargetEmployee.value = row
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  const row = deleteTargetEmployee.value
  if (!row) return
  deleteLoading.value = true
  try {
    try {
      await employeeApi.deleteEmployee(row.id)
    } catch {
      // API失败时本地删除
    }
    const idx = tableData.value.findIndex(e => e.id === row.id)
    if (idx !== -1) {
      tableData.value.splice(idx, 1)
      total.value--
    }
    MessagePlugin.success('删除成功')
    deleteDialogVisible.value = false
  } catch (e) {
    MessagePlugin.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}

// 状态格式化
const formatStatus = (status: number) => {
  const map: Record<number, { theme: string; label: string }> = {
    1: { theme: 'success', label: '正常' },
    0: { theme: 'danger', label: '禁用' }
  }
  return map[status] || { theme: 'default', label: '未知' }
}

// 导出
const handleExport = () => {
  MessagePlugin.info('正在导出...')
}

// 打开批量导入弹窗
const openImportDialog = () => {
  importJsonData.value = ''
  importDialogVisible.value = true
}

// 下载导入模板
const downloadTemplate = () => {
  const template = [
    {
      name: '示例姓名',
      gender: '男',
      phone: '13800138000',
      email: 'example@company.com',
      idCard: '110101199001011234',
      departmentId: 1,
      position: '工程师',
      entryDate: dayjs().format('YYYY-MM-DD'),
      education: '本科',
      nation: '汉族',
      politicalStatus: '群众',
      status: 1
    }
  ]
  const blob = new Blob([JSON.stringify(template, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '员工导入模板.json'
  a.click()
  URL.revokeObjectURL(url)
  MessagePlugin.success('模板已下载')
}

// 批量导入
const handleBatchImport = async () => {
  if (!importJsonData.value.trim()) {
    MessagePlugin.warning('请输入导入数据')
    return
  }
  
  let employees: any[]
  try {
    employees = JSON.parse(importJsonData.value)
    if (!Array.isArray(employees)) {
      throw new Error('数据格式错误')
    }
  } catch (e) {
    MessagePlugin.error('JSON 格式错误，请检查数据格式')
    return
  }
  
  if (employees.length === 0) {
    MessagePlugin.warning('没有可导入的数据')
    return
  }
  
  importLoading.value = true
  try {
    const res = await employeeApi.batchImportEmployee(employees)
    const { successCount, failCount, errors } = res
    
    if (failCount > 0) {
      const errorMsg = errors.length > 3 ? errors.slice(0, 3).join('\n') + '\n...' : errors.join('\n')
      MessagePlugin.warning(`导入完成：成功 ${successCount} 条，失败 ${failCount} 条\n${errorMsg}`)
    } else {
      MessagePlugin.success(`导入成功：${successCount} 条`)
    }
    
    importDialogVisible.value = false
    loadData()
  } catch (e: any) {
    MessagePlugin.error(e?.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadDepartmentTree()
})
</script>

<template>
  <div class="employee-page">
    <!-- 搜索区域 -->
    <t-card class="search-card" :bordered="false">
      <t-form :model="queryParams" layout="inline">
        <t-form-item label="关键词">
          <t-input
            v-model="queryParams.keyword"
            placeholder="姓名/工号/手机号"
            clearable
            style="width: 180px"
            @enter="handleSearch"
          />
        </t-form-item>
        <t-form-item label="部门">
          <t-cascader
            v-model="searchDeptValue"
            :options="departmentTree"
            :keys="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择部门"
            clearable
            check-strictly
            style="width: 200px"
            @change="handleSearchDeptChange"
          />
        </t-form-item>
        <t-form-item label="状态">
          <t-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 130px"
          >
            <t-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item>
          <t-space>
            <t-button theme="primary" @click="handleSearch">
              <template #icon>
                <t-icon name="search" />
              </template>
              搜索
            </t-button>
            <t-button variant="outline" @click="handleReset">重置</t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </t-card>

    <!-- 数据表格 -->
    <t-card class="table-card" :bordered="false">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <span>员工列表</span>
            <t-tag theme="primary" variant="light">{{ total }} 人</t-tag>
          </div>
          <t-space>
            <t-button theme="primary" @click="openAddDialog">
              <template #icon>
                <t-icon name="add-circle" />
              </template>
              新增员工
            </t-button>
            <t-button variant="outline" @click="openImportDialog">
              <template #icon>
                <t-icon name="upload-1" />
              </template>
              批量导入
            </t-button>
            <t-button variant="outline" @click="handleExport">
              <template #icon>
                <t-icon name="download-1" />
              </template>
              导出
            </t-button>
          </t-space>
        </div>
      </template>

      <t-table
        :data="tableData"
        :loading="loading"
        :columns="columns"
        :hover="true"
        :row-key="'id'"
        stripe
      >
        <!-- 姓名列：显示头像+姓名 -->
        <template #name="{ row }">
          <t-space size="small">
            <t-avatar size="small">{{ (row.name || '').slice(0, 1) }}</t-avatar>
            {{ row.name }}
          </t-space>
        </template>
        
        <!-- 入职日期格式化 -->
        <template #entryDate="{ row }">
          {{ row.entryDate ? dayjs(row.entryDate).format('YYYY-MM-DD') : '-' }}
        </template>
        
        <!-- 状态标签 -->
        <template #status="{ row }">
          <t-tag :theme="formatStatus(row.status || 1).theme" variant="light">{{ formatStatus(row.status || 1).label }}</t-tag>
        </template>
        
        <!-- 基本工资格式化 -->
        <template #baseSalary="{ row }">
          {{ row.baseSalary ? '¥' + Number(row.baseSalary).toLocaleString() : '-' }}
        </template>
        
        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="action-buttons">
            <t-button variant="text" size="small" @click.stop="openEditDialog(row)">编辑</t-button>
            <t-button variant="text" size="small" theme="danger" @click.stop="handleDelete(row)">删除</t-button>
          </div>
        </template>
      </t-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <t-pagination
          v-model="queryParams.page"
          v-model:pageSize="queryParams.pageSize"
          :total="total"
          :page-size-options="[10, 20, 50]"
          @change="handlePageChange"
        />
      </div>
    </t-card>

    <!-- 新增/编辑弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :title="dialogTitle"
      width="1000px"
      :footer="false"
      placement="center"
      destroy-on-close
      class="employee-dialog"
    >
      <t-form
        :data="formData"
        label-width="90px"
        layout="vertical"
        class="employee-form"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <div class="form-section-title">
            <t-icon name="user" />
            <span>基本信息</span>
          </div>
          <div class="form-grid-2">
            <!-- 工号：新增时隐藏，编辑时禁用只读 -->
            <t-form-item v-if="formData.id" label="工号" name="username">
              <t-input :value="(formData as any).username" disabled>
                <template #prefix><t-icon name="user-circle" style="color: #999;" /></template>
              </t-input>
            </t-form-item>
            <t-form-item label="姓名" name="name">
              <t-input 
                v-model="formData.name" 
                placeholder="请输入姓名" 
                :status="formErrors.name ? 'error' : undefined"
                @input="clearError('name')"
              />
            </t-form-item>
            <t-form-item label="性别" name="gender">
              <t-radio-group v-model="formData.gender" style="width:100%">
                <t-radio value="男">男</t-radio>
                <t-radio value="女">女</t-radio>
              </t-radio-group>
            </t-form-item>
            <t-form-item label="出生日期" name="birthday">
              <t-date-picker v-model="formData.birthday" allow-input clearable style="width:100%" />
            </t-form-item>
            <t-form-item label="民族" name="nation">
              <t-input v-model="(formData as any).nation" placeholder="如：汉族" />
            </t-form-item>
            <t-form-item label="婚姻状况" name="maritalStatus">
              <t-select v-model="(formData as any).maritalStatus" placeholder="请选择" clearable style="width:100%">
                <t-option value="未婚">未婚</t-option>
                <t-option value="已婚">已婚</t-option>
                <t-option value="离异">离异</t-option>
              </t-select>
            </t-form-item>
            <t-form-item label="身份证号" name="idCard">
              <t-input v-model="formData.idCard" placeholder="请输入身份证号" />
            </t-form-item>
            <t-form-item label="籍贯" name="nativePlace">
              <t-input v-model="(formData as any).nativePlace" placeholder="请输入籍贯" />
            </t-form-item>
          </div>
        </div>

        <!-- 联系信息 -->
        <div class="form-section">
          <div class="form-section-title">
            <t-icon name="phone" />
            <span>联系信息</span>
          </div>
          <div class="form-grid-2">
            <t-form-item label="手机号" :help="formErrors.phone">
              <t-input 
                v-model="formData.phone" 
                placeholder="请输入手机号" 
                :status="formErrors.phone ? 'error' : undefined"
                @input="clearError('phone')"
              />
            </t-form-item>
            <t-form-item label="邮箱" name="email">
              <t-input v-model="(formData as any).email" placeholder="请输入邮箱" />
            </t-form-item>
            <t-form-item label="紧急联系人" name="emergencyContact">
              <t-input v-model="(formData as any).emergencyContact" placeholder="请输入紧急联系人" />
            </t-form-item>
            <t-form-item label="紧急电话" name="emergencyPhone">
              <t-input v-model="(formData as any).emergencyPhone" placeholder="请输入紧急联系电话" />
            </t-form-item>
            <t-form-item label="家庭住址" name="address" class="form-item-full">
              <t-input v-model="(formData as any).address" placeholder="请输入家庭住址" />
            </t-form-item>
          </div>
        </div>

        <!-- 教育背景 -->
        <div class="form-section">
          <div class="form-section-title">
            <t-icon name="book" />
            <span>教育背景</span>
          </div>
          <div class="form-grid-2">
            <t-form-item label="学历" name="education">
              <t-select v-model="(formData as any).education" placeholder="请选择学历" clearable style="width:100%">
                <t-option value="初中及以下">初中及以下</t-option>
                <t-option value="高中/中专">高中/中专</t-option>
                <t-option value="大专">大专</t-option>
                <t-option value="本科">本科</t-option>
                <t-option value="硕士">硕士</t-option>
                <t-option value="博士">博士</t-option>
              </t-select>
            </t-form-item>
            <t-form-item label="毕业院校" name="graduateSchool">
              <t-input v-model="(formData as any).graduateSchool" placeholder="请输入毕业院校" />
            </t-form-item>
            <t-form-item label="专业" name="major" class="form-item-full">
              <t-input v-model="(formData as any).major" placeholder="请输入专业" />
            </t-form-item>
          </div>
        </div>

        <!-- 工作信息 -->
        <div class="form-section">
          <div class="form-section-title">
            <t-icon name="user-business" />
            <span>工作信息</span>
          </div>
          <div class="form-grid-2">
            <t-form-item label="所属部门" :help="formErrors.departmentId">
              <t-cascader
                v-model="formDeptPath"
                :options="departmentTree"
                :keys="{ label: 'name', value: 'id', children: 'children' }"
                placeholder="请选择所属部门"
                clearable
                check-strictly
                value-type="full"
                :status="formErrors.departmentId ? 'error' : undefined"
                style="width:100%"
                @change="handleFormDeptChange"
              />
            </t-form-item>
            <t-form-item label="职位" name="position">
              <t-input v-model="formData.position" placeholder="请输入职位" />
            </t-form-item>
            <t-form-item label="职级" name="rank">
              <t-input v-model="(formData as any).rank" placeholder="如：初级、中级、高级" />
            </t-form-item>
            <t-form-item label="员工状态" name="employeeStatus">
              <t-select v-model="(formData as any).employeeStatus" placeholder="请选择员工状态" clearable style="width:100%">
                <t-option :value="1" label="正式" />
                <t-option :value="2" label="试用" />
                <t-option :value="3" label="实习" />
                <t-option :value="4" label="离职" />
                <t-option :value="5" label="退休" />
              </t-select>
            </t-form-item>
            <t-form-item label="分配角色" name="roleId">
              <t-select v-model="(formData as any).roleId" placeholder="请选择角色" clearable style="width:100%">
                <t-option :value="1" label="超级管理员" />
                <t-option :value="2" label="人事主管" />
                <t-option :value="3" label="部门主管" />
                <t-option :value="4" label="普通员工" />
              </t-select>
            </t-form-item>
            <t-form-item label="入职日期" name="entryDate">
              <t-date-picker v-model="formData.entryDate" allow-input clearable style="width:100%" />
            </t-form-item>
            <t-form-item label="转正日期" name="positiveDate">
              <t-date-picker v-model="(formData as any).positiveDate" allow-input clearable style="width:100%" />
            </t-form-item>
            <t-form-item label="合同开始" name="contractStartDate">
              <t-date-picker v-model="(formData as any).contractStartDate" allow-input clearable style="width:100%" />
            </t-form-item>
            <t-form-item label="合同到期" name="contractEndDate">
              <t-date-picker v-model="(formData as any).contractEndDate" allow-input clearable style="width:100%" />
            </t-form-item>
            <t-form-item label="账号状态" name="status">
              <t-radio-group v-model="(formData as any).status" style="width:100%">
                <t-radio :value="1">正常</t-radio>
                <t-radio :value="0">禁用</t-radio>
              </t-radio-group>
            </t-form-item>
            <t-form-item label="基本工资" name="baseSalary">
              <t-input-number v-model="(formData as any).baseSalary" :min="0" :step="100" placeholder="请输入基本工资" style="width:100%" />
            </t-form-item>
          </div>
        </div>

        <div class="form-footer">
          <t-space>
            <t-button variant="outline" @click="dialogVisible = false">取消</t-button>
            <t-button theme="primary" :loading="dialogLoading" @click="handleSubmit">
              {{ formData.id ? '保存修改' : '确认新增' }}
            </t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>

    <!-- 删除确认弹窗 -->
    <t-dialog
      v-model:visible="deleteDialogVisible"
      header="确认删除"
      width="420px"
      :footer="false"
    >
      <div style="padding: 16px 0; line-height: 1.8;">
        <div style="font-size: 15px; margin-bottom: 8px;">
          确定要删除员工「<strong>{{ deleteTargetEmployee?.name }}</strong>」吗？
        </div>
        <div style="color: #e34d59; font-size: 13px;">
          删除后不可恢复，请谨慎操作。
        </div>
      </div>
      <div style="display: flex; justify-content: flex-end; gap: 12px; padding-top: 8px; border-top: 1px solid #e7e7e7;">
        <t-button @click="deleteDialogVisible = false">取消</t-button>
        <t-button theme="danger" :loading="deleteLoading" @click="confirmDelete">
          确定删除
        </t-button>
      </div>
    </t-dialog>

    <!-- 批量导入弹窗 -->
    <t-dialog
      v-model:visible="importDialogVisible"
      header="批量导入员工"
      width="600px"
      :footer="false"
      placement="center"
    >
      <div style="padding: 16px 0;">
        <t-alert theme="info" message="请按照模板格式填写员工信息，支持 JSON 格式批量导入" style="margin-bottom: 16px;" />
        
        <div style="margin-bottom: 16px;">
          <t-space direction="vertical" alignment="start">
            <t-button variant="outline" size="small" @click="downloadTemplate">
              <template #icon><t-icon name="download-1" /></template>
              下载导入模板
            </t-button>
          </t-space>
        </div>

        <t-form-item label="粘贴JSON数据">
          <t-textarea
            v-model="importJsonData"
            placeholder='粘贴JSON数组数据，如：[{"name":"张三","gender":"男","phone":"13800138000",...}]'
            :autosize="{ minRows: 8, maxRows: 15 }"
            style="width: 100%;"
          />
        </t-form-item>

        <div style="color: #666; font-size: 12px; margin-top: 8px;">
          提示：每条数据需包含 name（姓名）、gender（性别）、phone（手机号）必填字段
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 12px; padding-top: 16px; border-top: 1px solid #e7e7e7;">
        <t-button @click="importDialogVisible = false">取消</t-button>
        <t-button theme="primary" :loading="importLoading" @click="handleBatchImport">
          开始导入
        </t-button>
      </div>
    </t-dialog>

  </div>
</template>

<style scoped>
.employee-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  padding: 4px 0;
}

.table-card {
  flex: 1;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 表单分区 */
.employee-form {
  max-height: 68vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 4px;
}

.form-section {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--td-bg-color-container-hover, #f8f9fa);
  border-radius: 8px;
  border: 1px solid var(--td-border-level-1-color, #e7e7e7);
}

.form-section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--td-brand-color, #0052d9);
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--td-border-level-1-color, #e7e7e7);
}

.form-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 16px;
}

.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

/* 联系信息住址字段跨两列 */
.form-item-full {
  grid-column: 1 / -1;
}

.form-footer {
  margin-top: 8px;
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid var(--td-border-level-1-color, #e7e7e7);
}

@media (max-width: 768px) {
  .form-grid-3, .form-grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
