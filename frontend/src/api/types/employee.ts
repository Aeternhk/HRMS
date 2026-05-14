// 员工数据类型 - 完全对齐 sys_user 表结构
export interface Employee {
  id?: number
  
  // 认证字段
  username?: string          // 工号（部门编码+序号，如 PROD001、SMT003）
  password?: string          // 密码（BCrypt加密）
  roleId?: number            // 角色：1=超管 2=人事主管 3=部门主管 4=普通员工
  loginType?: number         // 登录类型
  status?: number            // 状态：1=正常，0=禁用
  lastLoginTime?: string     // 最后登录时间
  lastLoginIp?: string       // 最后登录IP

  // 基本信息
  name: string               // 姓名
  gender: string             // 性别
  nation?: string            // 民族
  idCard?: string            // 身份证号
  birthday?: string          // 出生日期
  maritalStatus?: string     // 婚姻状态
  phone: string              // 手机号
  emergencyContact?: string  // 紧急联系人
  emergencyPhone?: string    // 紧急联系电话
  nativePlace?: string       // 籍贯
  address?: string           // 家庭住址
  email?: string             // 邮箱
  education?: string         // 学历
  graduateSchool?: string    // 毕业院校
  major?: string             // 专业

  // 职位信息
  entryDate?: string         // 入职日期
  positiveDate?: string      // 转正日期
  contractStartDate?: string // 合同开始日期
  contractEndDate?: string   // 合同结束日期
  departmentId?: number      // 部门ID
  departmentName?: string    // 部门名称（冗余显示）
  position?: string          // 职位
  rank?: string              // 职级
  employeeStatus?: number    // 员工状态(1正式/2试用/3实习/4离职/5退休)
  baseSalary?: number        // 基本工资
  photo?: string             // 照片
  avatar?: string            // 头像

  roleName?: string          // 角色名称（冗余显示）
  
  // 时间戳
  createTime?: string
  updateTime?: string
}

// 部门编码映射（用于工号生成）
export const DEPT_CODE_MAP: Record<number, string> = {
  1: 'PROD',   // 生产部
  2: 'QLT',    // 品质部
  3: 'RD',     // 研发部
  4: 'PUR',    // 采购部
  5: 'WH',     // 仓储部
  6: 'FIN',    // 财务部
  7: 'HR',     // 人事部
  8: 'ADMIN',  // 行政部
  101: 'SMT',  // SMT车间
  102: 'LAM',  // 贴合车间
  103: 'ASM',  // 组装车间
  104: 'PKG',  // 包装车间
  201: 'IQC',  // IQC
  202: 'OQC',  // OQC
  203: 'QE',   // QE
}

// 员工查询参数
export interface EmployeeQuery {
  page: number
  pageSize: number
  name?: string
  phone?: string
  departmentId?: number
  status?: number
  entryDateStart?: string
  entryDateEnd?: string
}

// 员工分页响应
export interface EmployeePageVO {
  records: Employee[]
  total: number
  page: number
  pageSize: number
}

// 部门数据
export interface Department {
  id: number
  name: string
  parentId?: number
  sortOrder?: number
  status?: number
  createTime?: string
  updateTime?: string
}

// 部门树节点
export interface DepartmentTreeVO {
  id: number
  name: string
  parentId?: number
  sortOrder?: number
  children?: DepartmentTreeVO[]
}
