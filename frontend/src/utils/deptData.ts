/**
 * 统一部门数据源
 * 所有页面的部门选项、部门 id 映射均使用此文件，确保全站一致。
 *
 * 部门 id 规则：
 *   1  - 生产部（含子部门 101~104）
 *   2  - 品质部（含子部门 201~203）
 *   3  - 研发部
 *   4  - 采购部
 *   5  - 仓储部
 *   6  - 财务部
 *   7  - 人事部
 *   8  - 行政部
 */

export interface DeptOption {
  value: number
  label: string
}

export interface DeptTree {
  id: number
  name: string
  code: string
  parentId: number | null
  level?: number
  sort: number
  managerId?: number | null
  managerName?: string
  phone?: string
  status: number
  value?: string | number
  label?: string
  children?: DeptTree[]
}

/** 扁平部门列表（用于 t-select 下拉） */
export const departmentOptions: DeptOption[] = [
  { value: 1, label: '生产部' },
  { value: 2, label: '品质部' },
  { value: 3, label: '研发部' },
  { value: 4, label: '采购部' },
  { value: 5, label: '仓储部' },
  { value: 6, label: '财务部' },
  { value: 7, label: '人事部' },
  { value: 8, label: '行政部' },
  { value: 101, label: 'SMT车间' },
  { value: 102, label: '贴合车间' },
  { value: 103, label: '组装车间' },
  { value: 104, label: '包装车间' },
  { value: 201, label: 'IQC' },
  { value: 202, label: 'OQC' },
  { value: 203, label: 'QE' }
]

/** 仅顶级部门（用于简化版下拉，如考勤/薪资/绩效） */
export const topDepartmentOptions: DeptOption[] = [
  { value: 1, label: '生产部' },
  { value: 2, label: '品质部' },
  { value: 3, label: '研发部' },
  { value: 4, label: '采购部' },
  { value: 5, label: '仓储部' },
  { value: 6, label: '财务部' },
  { value: 7, label: '人事部' },
  { value: 8, label: '行政部' }
]

/** 部门树结构（用于 t-tree / t-cascader）- 必须放在所有引用它的代码之前 */
export const departmentTreeData: DeptTree[] = [
  {
    id: 0, name: '总公司', code: 'ROOT', parentId: null, level: 0, sort: 0,
    managerName: '系统管理员', phone: '', status: 1, value: 'root-0', label: '总公司',
    children: [
      {
        id: 1, name: '生产部', code: 'PRODUCTION', parentId: 0, level: 1, sort: 1,
        managerName: '钱七', phone: '13800138009', status: 1, value: 'dept-1', label: '生产部',
        children: [
          { id: 101, name: 'SMT车间', code: 'SMT', parentId: 1, level: 2, sort: 1, managerName: '王五', phone: '13800138005', status: 1, value: 'dept-101', label: 'SMT车间', children: [] },
          { id: 102, name: '贴合车间', code: 'LAMINATION', parentId: 1, level: 2, sort: 2, managerName: '', phone: '', status: 1, value: 'dept-102', label: '贴合车间', children: [] },
          { id: 103, name: '组装车间', code: 'ASSEMBLY', parentId: 1, level: 2, sort: 3, managerName: '', phone: '', status: 1, value: 'dept-103', label: '组装车间', children: [] },
          { id: 104, name: '包装车间', code: 'PACKAGING', parentId: 1, level: 2, sort: 4, managerName: '', phone: '', status: 1, value: 'dept-104', label: '包装车间', children: [] }
        ]
      },
      {
        id: 2, name: '品质部', code: 'QUALITY', parentId: 0, level: 1, sort: 2,
        managerName: '赵六', phone: '13800138007', status: 1, value: 'dept-2', label: '品质部',
        children: [
          { id: 201, name: 'IQC', code: 'IQC', parentId: 2, level: 2, sort: 1, managerName: '', phone: '', status: 1, value: 'dept-201', label: 'IQC', children: [] },
          { id: 202, name: 'OQC', code: 'OQC', parentId: 2, level: 2, sort: 2, managerName: '', phone: '', status: 1, value: 'dept-202', label: 'OQC', children: [] },
          { id: 203, name: 'QE', code: 'QE', parentId: 2, level: 2, sort: 3, managerName: '', phone: '', status: 1, value: 'dept-203', label: 'QE', children: [] }
        ]
      },
      { id: 3, name: '研发部', code: 'RD', parentId: 0, level: 1, sort: 3, managerName: '李四', phone: '13800138003', status: 1, value: 'dept-3', label: '研发部', children: [] },
      { id: 4, name: '采购部', code: 'PURCHASE', parentId: 0, level: 1, sort: 4, managerName: '', phone: '', status: 1, value: 'dept-4', label: '采购部', children: [] },
      { id: 5, name: '仓储部', code: 'WAREHOUSE', parentId: 0, level: 1, sort: 5, managerName: '', phone: '', status: 1, value: 'dept-5', label: '仓储部', children: [] },
      { id: 6, name: '财务部', code: 'FINANCE', parentId: 0, level: 1, sort: 6, managerName: '', phone: '', status: 1, value: 'dept-6', label: '财务部', children: [] },
      { id: 7, name: '人事部', code: 'HR', parentId: 0, level: 1, sort: 7, managerName: '', phone: '', status: 1, value: 'dept-7', label: '人事部', children: [] },
      { id: 8, name: '行政部', code: 'ADMIN', parentId: 0, level: 1, sort: 8, managerName: '', phone: '', status: 1, value: 'dept-8', label: '行政部', children: [] }
    ]
  }
]

/** 根据 id 获取部门名称 */
export const getDeptName = (id: number): string => {
  return departmentOptions.find(d => d.value === id)?.label ?? '-'
}

/** 扁平化所有部门节点 */
const flattenDepartmentTree = (nodes: DeptTree[]): DeptTree[] => {
  const result: DeptTree[] = []
  const walk = (list: DeptTree[]) => {
    for (const node of list) {
      result.push(node)
      if (node.children?.length) walk(node.children)
    }
  }
  walk(nodes)
  return result
}

/** 所有部门列表（扁平，含子部门） */
export const allDepartments: DeptTree[] = flattenDepartmentTree(departmentTreeData)

/**
 * 获取某个部门 id 的顶级父部门 id
 * 例: 传入 101(SMT车间) 返回 1(生产部); 传入 3(研发部) 返回 3
 */
export const getTopDepartmentId = (id: number): number => {
  const dept = allDepartments.find(d => d.id === id)
  if (!dept) return id
  if (dept.parentId === 0 || dept.parentId === null) return id
  return getTopDepartmentId(dept.parentId)
}
