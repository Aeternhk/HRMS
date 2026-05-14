<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import type { Leave } from '@/api/modules/leave'

// 查询参数
const queryParams = ref({
  keyword: '',
  leaveType: undefined as string | undefined,
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

// 数据
const loading = ref(false)
const leaveData = ref<Leave[]>([])
const total = ref(0)

// 请假类型选项（对应数据库字典）
const leaveTypeOptions = [
  { value: 'annual', label: '年假' },
  { value: 'sick', label: '病假' },
  { value: 'personal', label: '事假' },
  { value: 'marriage', label: '婚假' },
  { value: 'maternity', label: '产假' },
  { value: 'paternity', label: '陪产假' },
  { value: 'bereavement', label: '丧假' }
]

// 状态选项
const statusOptions = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已拒绝' }
]

// 员工列表（用于选择申请人）
interface Employee {
  id: number
  username: string
  name: string
  departmentName?: string
}
const employeeList = ref<Employee[]>([])

// 表格列
const columns = [
  { colKey: 'userName', title: '申请人', width: '100' },
  { colKey: 'userUsername', title: '工号', width: '90' },
  { colKey: 'leaveTypeName', title: '请假类型', width: '90' },
  { colKey: 'startDate', title: '开始日期', width: '110' },
  { colKey: 'endDate', title: '结束日期', width: '110' },
  { colKey: 'days', title: '天数', width: '70', align: 'center' },
  { colKey: 'reason', title: '原因', ellipsis: true, width: '150' },
  { colKey: 'statusName', title: '状态', width: '80', align: 'center' },
  { colKey: 'approverName', title: '审批人', width: '80' },
  { colKey: 'createTime', title: '申请时间', width: '150' },
  { colKey: 'action', title: '操作', width: '160', align: 'center', fixed: 'right' }
]

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res: any = await import('@/api/modules/leave').then(m => m.getLeaveList(queryParams.value))
    if (res.data?.code === 200) {
      leaveData.value = res.data.data.list || []
      total.value = res.data.data.total || 0
    } else {
      // 使用模拟数据作为后备
      loadMockData()
    }
  } catch {
    loadMockData()
  } finally {
    loading.value = false
  }
}

// 模拟数据
const loadMockData = () => {
  leaveData.value = [
    { id: 1, userId: 6, userName: '张三', userUsername: 'EMP004', leaveType: 'personal', leaveTypeName: '事假', startDate: '2026-04-10', endDate: '2026-04-11', days: 2, reason: '家中有事需要处理', status: 0, statusName: '待审批', createTime: '2026-04-08 09:30' },
    { id: 2, userId: 3, userName: '赵六', userUsername: 'EMP001', leaveType: 'annual', leaveTypeName: '年假', startDate: '2026-04-15', endDate: '2026-04-18', days: 4, reason: '年度休假计划', status: 1, statusName: '已通过', approverId: 2, approverName: '李四', approveTime: '2026-04-07 14:00', createTime: '2026-04-06 10:20' },
    { id: 3, userId: 4, userName: '王五', userUsername: 'EMP002', leaveType: 'sick', leaveTypeName: '病假', startDate: '2026-04-05', endDate: '2026-04-05', days: 1, reason: '感冒发烧需就医', status: 1, statusName: '已通过', approverId: 5, approverName: '钱七', approveTime: '2026-04-04 08:30', createTime: '2026-04-04 07:15' },
    { id: 4, userId: 2, userName: '李四', userUsername: 'HR001', leaveType: 'marriage', leaveTypeName: '婚假', startDate: '2026-05-01', endDate: '2026-05-10', days: 10, reason: '结婚登记休婚假', status: 0, statusName: '待审批', createTime: '2026-04-07 16:45' },
    { id: 5, userId: 5, userName: '钱七', userUsername: 'EMP003', leaveType: 'bereavement', leaveTypeName: '丧假', startDate: '2026-03-28', endDate: '2026-03-30', days: 3, reason: '长辈去世', status: 1, statusName: '已通过', approverId: 1, approverName: '管理员', approveTime: '2026-03-27 09:00', createTime: '2026-03-26 15:00' }
  ]
  total.value = leaveData.value.length
}

// 加载员工列表（用于新增时选择）
const loadEmployees = async () => {
  try {
    const res: any = await import('@/api/modules/employee').then(m => m.getEmployeeList({ page: 1, pageSize: 100 }))
    if (res.data?.code === 200) {
      employeeList.value = res.data.data.list || []
      return
    }
  } catch { /* ignore */ }
  // 模拟员工数据
  employeeList.value = [
    { id: 1, username: 'admin', name: '系统管理员' },
    { id: 2, username: 'HR001', name: '李四' },
    { id: 3, username: 'EMP001', name: '赵六' },
    { id: 4, username: 'EMP002', name: '王五' },
    { id: 5, username: 'EMP003', name: '钱七' },
    { id: 6, username: 'EMP004', name: '张三' }
  ]
}

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.value = { keyword: '', leaveType: undefined, status: undefined, page: 1, pageSize: 10 }
  loadData()
}

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增请假申请')
const dialogLoading = ref(false)
const isEditing = ref(false)
const formRef = ref()

const formData = ref({
  userId: undefined as number | undefined,
  leaveType: 'personal',
  startDate: '' as string,
  endDate: '' as string,
  days: 0.5 as number,
  reason: ''
})

const formRules = {
  userId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  reason: [{ required: true, message: '请填写请假原因', trigger: 'blur' }]
}

// 计算天数
const calcDays = () => {
  if (formData.value.startDate && formData.value.endDate) {
    const start = dayjs(formData.value.startDate)
    const end = dayjs(formData.value.endDate)
    formData.value.days = end.diff(start, 'day') + 1
  }
}

const openAddDialog = () => {
  dialogTitle.value = '新增请假申请'
  isEditing.value = false
  formData.value = { userId: undefined, leaveType: 'personal', startDate: '', endDate: '', days: 0.5, reason: '' }
  dialogVisible.value = true
}

const openEditDialog = (row: Leave) => {
  dialogTitle.value = '编辑请假申请'
  isEditing.value = true
  formData.value = {
    userId: row.userId,
    leaveType: row.leaveType,
    startDate: row.startDate,
    endDate: row.endDate,
    days: Number(row.days),
    reason: row.reason
  }
  dialogVisible.value = true
}

// 审批弹窗
const approveDialogVisible = ref(false)
const approveFormRef = ref()
const currentRow = ref<Leave | null>(null)
const approveFormData = ref({
  status: 1,
  approveRemark: ''
})

const openApproveDialog = (row: Leave) => {
  currentRow.value = row
  approveFormData.value = { status: 1, approveRemark: '' }
  approveDialogVisible.value = true
}

const handleApprove = async () => {
  if (!currentRow.value) return
  try {
    await approveFormRef.value?.validate()
    dialogLoading.value = true
    const { approveLeave } = await import('@/api/modules/leave')
    await approveLeave(currentRow.value.id, approveFormData.value)

    // 更新本地状态
    const idx = leaveData.value.findIndex(l => l.id === currentRow.value!.id)
    if (idx !== -1) {
      leaveData.value[idx].status = approveFormData.value.status
      leaveData.value[idx].statusName = approveFormData.value.status === 1 ? '已通过' : '已拒绝'
      leaveData.value[idx].approveRemark = approveFormData.value.approveRemark
      leaveData.value[idx].approverName = '当前用户'
      leaveData.value[idx].approveTime = dayjs().format('YYYY-MM-DD HH:mm')
    }

    MessagePlugin.success(approveFormData.value.status === 1 ? '审批通过' : '已驳回')
    approveDialogVisible.value = false
  } catch (e: any) {
    if (e !== true) MessagePlugin.error(e.message || '操作失败')
  } finally {
    dialogLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    dialogLoading.value = true

    if (isEditing.value) {
      const idx = leaveData.value.findIndex(l => l.id === (formData.value as any).id)
      if (idx !== -1) {
        Object.assign(leaveData.value[idx], {
          ...formData.value,
          leaveTypeName: leaveTypeOptions.find(t => t.value === formData.value.leaveType)?.label
        })
      }
      MessagePlugin.success('更新成功')
    } else {
      const newLeave: Leave = {
        id: Date.now(),
        userId: formData.value.userId!,
        userName: employeeList.value.find(e => e.id === formData.value.userId)?.name || '',
        userUsername: employeeList.value.find(e => e.id === formData.value.userId)?.username || '',
        leaveType: formData.value.leaveType,
        leaveTypeName: leaveTypeOptions.find(t => t.value === formData.value.leaveType)?.label || '',
        startDate: formData.value.startDate,
        endDate: formData.value.endDate,
        days: formData.value.days,
        reason: formData.value.reason,
        status: 0,
        statusName: '待审批',
        createTime: dayjs().format('YYYY-MM-DD HH:mm')
      }
      leaveData.value.unshift(newLeave)
      total.value++
      MessagePlugin.success('提交成功，等待审批')
    }

    dialogVisible.value = false
  } catch (e: any) {
    if (e !== true) MessagePlugin.error(e.message || '操作失败')
  } finally {
    dialogLoading.value = false
  }
}

const deleteLeave = (row: Leave) => {
  DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除「${row.userName}」的请假记录吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: () => {
      const idx = leaveData.value.findIndex(l => l.id === row.id)
      if (idx !== -1) {
        leaveData.value.splice(idx, 1)
        total.value--
      }
      MessagePlugin.success('删除成功')
    }
  })
}

onMounted(() => {
  loadData()
  loadEmployees()
})
</script>

<template>
  <div class="leave-page">
    <!-- 搜索区域 -->
    <t-card class="search-card" :bordered="false">
      <t-form :model="queryParams" layout="inline">
        <t-form-item label="关键词">
          <t-input v-model="queryParams.keyword" placeholder="搜索员工姓名/工号" clearable style="width: 180px" @enter="handleSearch" />
        </t-form-item>
        <t-form-item label="请假类型">
          <t-select v-model="queryParams.leaveType" placeholder="全部" clearable style="width: 120px">
            <t-option v-for="item in leaveTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item label="状态">
          <t-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100px">
            <t-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item>
          <t-space>
            <t-button theme="primary" @click="handleSearch">查询</t-button>
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
            <span>请假记录</span>
            <t-tag theme="primary" variant="light">{{ total }} 条</t-tag>
          </div>
          <t-button theme="primary" @click="openAddDialog">
            <template #icon><t-icon name="add-circle" /></template>
            新增请假
          </t-button>
        </div>
      </template>

      <t-table :data="leaveData" :loading="loading" :columns="columns" :hover="true" :row-key="'id'" stripe>
        <!-- 状态 -->
        <template #statusName="{ row }">
          <t-tag v-if="row.status === 0" theme="warning" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else-if="row.status === 1" theme="success" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else theme="danger" size="small">{{ row.statusName }}</t-tag>
        </template>
        <!-- 操作 -->
        <template #action="{ row }">
          <div class="action-buttons">
            <t-button v-if="row.status === 0" variant="text" size="small" theme="success" @click="openApproveDialog(row)">审批</t-button>
            <t-button variant="text" size="small" @click="openEditDialog(row)">编辑</t-button>
            <t-button variant="text" size="small" theme="danger" @click="deleteLeave(row)">删除</t-button>
          </div>
        </template>
      </t-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <t-pagination v-model:current="queryParams.page" v-model:page-size="queryParams.pageSize" :total="total" show-page-size size="small" @change="loadData" />
      </div>
    </t-card>

    <!-- 新增/编辑弹窗 -->
    <t-dialog v-model:visible="dialogVisible" :header="dialogTitle" width="600px" :footer="false" placement="center" destroy-on-close>
      <t-form ref="formRef" :data="formData" :rules="formRules" label-width="100px" layout="vertical" class="leave-form">
        <t-form-item label="申请人" name="userId">
          <t-select v-model="formData.userId" placeholder="请选择申请人" filterable>
            <t-option v-for="emp in employeeList" :key="emp.id" :value="emp.id" :label="`${emp.name} (${emp.username})`" />
          </t-select>
        </t-form-item>
        <t-form-item label="请假类型" name="leaveType">
          <t-select v-model="formData.leaveType" placeholder="请选择">
            <t-option v-for="item in leaveTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="开始日期" name="startDate">
              <t-date-picker v-model="formData.startDate" enable-time-picker={false} format="YYYY-MM-DD" @change="calcDays" />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="结束日期" name="endDate">
              <t-date-picker v-model="formData.endDate" enable-time-picker={false} format="YYYY-MM-DD" @change="calcDays" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="请假天数">
          <t-input-number v-model="formData.days" :min="0.5" :step="0.5" theme="normal" style="width: 160px" />
          <span class="form-hint">天</span>
        </t-form-item>
        <t-form-item label="请假原因" name="reason">
          <t-textarea v-model="formData.reason" placeholder="请填写请假原因" :autosize="{ minRows: 3, maxRows: 6 }" />
        </t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="dialogVisible = false">取消</t-button>
        <t-button theme="primary" :loading="dialogLoading" @click="handleSubmit">{{ isEditing ? '保存' : '提交申请' }}</t-button>
      </div>
    </t-dialog>

    <!-- 审批弹窗 -->
    <t-dialog v-model:visible="approveDialogVisible" header="审批请假" width="480px" :footer="false" placement="center" destroy-on-close>
      <div v-if="currentRow" class="approve-info">
        <p><strong>申请人：</strong>{{ currentRow.userName }}（{{ currentRow.userUsername }}）</p>
        <p><strong>请假类型：</strong>{{ currentRow.leaveTypeName }}</p>
        <p><strong>请假时间：</strong>{{ currentRow.startDate }} ~ {{ currentRow.endDate }}（{{ currentRow.days }}天）</p>
        <p><strong>原因：</strong>{{ currentRow.reason }}</p>
      </div>
      <t-form ref="approveFormRef" :data="approveFormData" label-width="80px" layout="vertical" class="approve-form">
        <t-form-item label="审批结果">
          <t-radio-group v-model="approveFormData.status">
            <t-radio :value="1">通过</t-radio>
            <t-radio :value="2">拒绝</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="审批备注">
          <t-textarea v-model="approveFormData.approveRemark" placeholder="可选填审批备注" :autosize="{ minRows: 2, maxRows: 4 }" />
        </t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="approveDialogVisible = false">取消</t-button>
        <t-button theme="primary" :loading="dialogLoading" @click="handleApprove">确定</t-button>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.leave-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card { padding: 4px 0; }
.table-card { flex: 1; }
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.table-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 500;
}
:::deep(.t-table th), :::deep(.t-table td) { text-align: center; vertical-align: middle; }
:::deep(.t-table td:first-child), :::deep(.t-table th:first-child) { text-align: center; }
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
}
.leave-form, .approve-form { padding: 16px 0; }
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  margin-top: 8px;
  border-top: 1px solid #e7e7e7;
}
.action-buttons { display: flex; justify-content: center; align-items: center; gap: 4px; min-width: 140px; }
.form-hint { margin-left: 8px; color: #999; font-size: 13px; }
.approve-info p { margin: 6px 0; color: #333; font-size: 14px; }
.approve-info { padding: 12px 0; border-bottom: 1px solid #f0f0f0; margin-bottom: 16px; }
</style>
