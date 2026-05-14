<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import type { Overtime } from '@/api/modules/overtime'

// 查询参数
const queryParams = ref({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

const loading = ref(false)
const overtimeData = ref<Overtime[]>([])
const total = ref(0)

interface Employee {
  id: number
  username: string
  name: string
}
const employeeList = ref<Employee[]>([])

const statusOptions = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已拒绝' }
]

const columns = [
  { colKey: 'userName', title: '申请人', width: '100' },
  { colKey: 'userUsername', title: '工号', width: '90' },
  { colKey: 'overtimeDate', title: '加班日期', width: '110' },
  { colKey: 'startTime', title: '开始时间', width: '130' },
  { colKey: 'endTime', title: '结束时间', width: '130' },
  { colKey: 'hours', title: '时长(h)', width: '80', align: 'center' },
  { colKey: 'reason', title: '加班原因', ellipsis: true, width: '150' },
  { colKey: 'statusName', title: '状态', width: '80', align: 'center' },
  { colKey: 'approverName', title: '审批人', width: '80' },
  { colKey: 'createTime', title: '申请时间', width: '150' },
  { colKey: 'action', title: '操作', width: '160', align: 'center', fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await import('@/api/modules/overtime').then(m => m.getOvertimeList(queryParams.value))
    if (res.data?.code === 200) {
      overtimeData.value = res.data.data.list || []
      total.value = res.data.data.total || 0
    } else {
      loadMockData()
    }
  } catch {
    loadMockData()
  } finally {
    loading.value = false
  }
}

const loadMockData = () => {
  overtimeData.value = [
    { id: 1, userId: 6, userName: '张三', userUsername: 'EMP004', overtimeDate: '2026-04-08', startTime: '2026-04-08 18:30', endTime: '2026-04-08 21:30', hours: 3, reason: '生产线紧急订单赶工', status: 0, statusName: '待审批', createTime: '2026-04-08 17:00' },
    { id: 2, userId: 4, userName: '王五', userUsername: 'EMP002', overtimeDate: '2026-04-07', startTime: '2026-04-07 18:00', endTime: '2026-04-07 22:00', hours: 4, reason: '设备调试需要延长工作时间', status: 1, statusName: '已通过', approverId: 5, approverName: '钱七', createTime: '2026-04-06 15:30' },
    { id: 3, userId: 3, userName: '赵六', userUsername: 'EMP001', overtimeDate: '2026-04-05', startTime: '2026-04-05 19:00', endTime: '2026-04-05 21:00', hours: 2, reason: '客户验厂配合加班', status: 1, statusName: '已通过', approverId: 2, approverName: '李四', createTime: '2026-04-04 09:00' },
    { id: 4, userId: 5, userName: '钱七', userUsername: 'EMP003', overtimeDate: '2026-04-03', startTime: '2026-04-03 18:30', endTime: '2026-04-03 20:30', hours: 2, reason: '生产计划调整紧急处理', status: 2, statusName: '已拒绝', approverId: 1, approverName: '管理员', approveRemark: '理由不充分，建议提前规划', createTime: '2026-04-02 16:20' },
    { id: 5, userId: 6, userName: '张三', userUsername: 'EMP004', overtimeDate: '2026-04-01', startTime: '2026-04-01 18:00', endTime: '2026-04-01 22:00', hours: 4, reason: '月末盘点加班', status: 1, statusName: '已通过', approverId: 5, approverName: '钱七', createTime: '2026-03-31 10:00' }
  ]
  total.value = overtimeData.value.length
}

const loadEmployees = async () => {
  try {
    const res: any = await import('@/api/modules/employee').then(m => m.getEmployeeList({ page: 1, pageSize: 100 }))
    if (res.data?.code === 200) { employeeList.value = res.data.data.list || []; return }
  } catch {}
  employeeList.value = [
    { id: 1, username: 'admin', name: '系统管理员' },
    { id: 2, username: 'HR001', name: '李四' },
    { id: 3, username: 'EMP001', name: '赵六' },
    { id: 4, username: 'EMP002', name: '王五' },
    { id: 5, username: 'EMP003', name: '钱七' },
    { id: 6, username: 'EMP004', name: '张三' }
  ]
}

const handleSearch = () => { queryParams.value.page = 1; loadData() }

const handleReset = () => { queryParams.value = { keyword: '', status: undefined, page: 1, pageSize: 10 }; loadData() }

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增加班申请')
const dialogLoading = ref(false)
const isEditing = ref(false)
const formRef = ref()

const formData = ref({
  userId: undefined as number | undefined,
  overtimeDate: '' as string,
  startTime: '' as string,
  endTime: '' as string,
  hours: 2 as number,
  reason: ''
})

const formRules = {
  userId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
  overtimeDate: [{ required: true, message: '请选择加班日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请填写加班原因', trigger: 'blur' }]
}

// 计算时长
const calcHours = () => {
  if (formData.value.startTime && formData.value.endTime) {
    const start = dayjs(formData.value.startTime)
    const end = dayjs(formData.value.endTime)
    const diffHours = end.diff(start, 'hour', true)
    formData.value.hours = Math.round(diffHours * 10) / 10
  }
}

const openAddDialog = () => {
  dialogTitle.value = '新增加班申请'
  isEditing.value = false
  formData.value = { userId: undefined, overtimeDate: '', startTime: '', endTime: '', hours: 2, reason: '' }
  dialogVisible.value = true
}

const openEditDialog = (row: Overtime) => {
  dialogTitle.value = '编辑加班申请'
  isEditing.value = true
  formData.value = {
    userId: row.userId,
    overtimeDate: row.overtimeDate,
    startTime: row.startTime,
    endTime: row.endTime,
    hours: Number(row.hours),
    reason: row.reason
  }
  dialogVisible.value = true
}

// 审批弹窗
const approveDialogVisible = ref(false)
const approveFormRef = ref()
const currentRow = ref<Overtime | null>(null)
const approveFormData = ref({ status: 1, approveRemark: '' })

const openApproveDialog = (row: Overtime) => {
  currentRow.value = row
  approveFormData.value = { status: 1, approveRemark: '' }
  approveDialogVisible.value = true
}

const handleApprove = async () => {
  if (!currentRow.value) return
  try {
    await approveFormRef.value?.validate()
    dialogLoading.value = true
    const idx = overtimeData.value.findIndex(o => o.id === currentRow.value!.id)
    if (idx !== -1) {
      overtimeData.value[idx].status = approveFormData.value.status
      overtimeData.value[idx].statusName = approveFormData.value.status === 1 ? '已通过' : '已拒绝'
      overtimeData.value[idx].approveRemark = approveFormData.value.approveRemark
      overtimeData.value[idx].approverName = '当前用户'
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
      const idx = overtimeData.value.findIndex(o => o.id === (formData.value as any).id)
      if (idx !== -1) Object.assign(overtimeData.value[idx], formData.value)
      MessagePlugin.success('更新成功')
    } else {
      const newRecord: Overtime = {
        id: Date.now(),
        userId: formData.value.userId!,
        userName: employeeList.value.find(e => e.id === formData.value.userId)?.name || '',
        userUsername: employeeList.value.find(e => e.id === formData.value.userId)?.username || '',
        overtimeDate: formData.value.overtimeDate,
        startTime: formData.value.startTime,
        endTime: formData.value.endTime,
        hours: formData.value.hours,
        reason: formData.value.reason,
        status: 0,
        statusName: '待审批',
        createTime: dayjs().format('YYYY-MM-DD HH:mm')
      }
      overtimeData.value.unshift(newRecord)
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

const deleteOvertime = (row: Overtime) => {
  DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除「${row.userName}」的加班记录吗？`,
    confirmBtn: '确定', cancelBtn: '取消',
    onConfirm: () => {
      const idx = overtimeData.value.findIndex(o => o.id === row.id)
      if (idx !== -1) { overtimeData.value.splice(idx, 1); total.value-- }
      MessagePlugin.success('删除成功')
    }
  })
}

onMounted(() => { loadData(); loadEmployees() })
</script>

<template>
  <div class="overtime-page">
    <t-card class="search-card" :bordered="false">
      <t-form :model="queryParams" layout="inline">
        <t-form-item label="关键词">
          <t-input v-model="queryParams.keyword" placeholder="搜索员工姓名/工号" clearable style="width: 180px" @enter="handleSearch" />
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

    <t-card class="table-card" :bordered="false">
      <template #header>
        <div class="table-header">
          <div class="table-title"><span>加班记录</span><t-tag theme="primary" variant="light">{{ total }} 条</t-tag></div>
          <t-button theme="primary" @click="openAddDialog"><template #icon><t-icon name="add-circle" /></template>新增加班</t-button>
        </div>
      </template>

      <t-table :data="overtimeData" :loading="loading" :columns="columns" :hover="true" :row-key="'id'" stripe>
        <template #statusName="{ row }">
          <t-tag v-if="row.status === 0" theme="warning" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else-if="row.status === 1" theme="success" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else theme="danger" size="small">{{ row.statusName }}</t-tag>
        </template>
        <template #action="{ row }">
          <div class="action-buttons">
            <t-button v-if="row.status === 0" variant="text" size="small" theme="success" @click="openApproveDialog(row)">审批</t-button>
            <t-button variant="text" size="small" @click="openEditDialog(row)">编辑</t-button>
            <t-button variant="text" size="small" theme="danger" @click="deleteOvertime(row)">删除</t-button>
          </div>
        </template>
      </t-table>

      <div class="pagination-wrapper">
        <t-pagination v-model:current="queryParams.page" v-model:page-size="queryParams.pageSize" :total="total" show-page-size size="small" @change="loadData" />
      </div>
    </t-card>

    <!-- 新增/编辑弹窗 -->
    <t-dialog v-model:visible="dialogVisible" :header="dialogTitle" width="600px" :footer="false" placement="center" destroy-on-close>
      <t-form ref="formRef" :data="formData" :rules="formRules" label-width="100px" layout="vertical" class="overtime-form">
        <t-form-item label="申请人" name="userId">
          <t-select v-model="formData.userId" placeholder="请选择申请人" filterable>
            <t-option v-for="emp in employeeList" :key="emp.id" :value="emp.id" :label="`${emp.name} (${emp.username})`" />
          </t-select>
        </t-form-item>
        <t-form-item label="加班日期" name="overtimeDate">
          <t-date-picker v-model="formData.overtimeDate" enable-time-picker={false} format="YYYY-MM-DD" />
        </t-form-item>
        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="开始时间" name="startTime">
              <t-date-picker v-model="formData.startTime" enable-time-picker format="YYYY-MM-DD HH:mm" @change="calcHours" />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="结束时间" name="endTime">
              <t-date-picker v-model="formData.endTime" enable-time-picker format="YYYY-MM-DD HH:mm" @change="calcHours" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="加班时长">
          <t-input-number v-model="formData.hours" :min="0.5" :step="0.5" theme="normal" style="width: 160px" />
          <span class="form-hint">小时</span>
        </t-form-item>
        <t-form-item label="加班原因" name="reason">
          <t-textarea v-model="formData.reason" placeholder="请填写加班原因" :autosize="{ minRows: 3, maxRows: 6 }" />
        </t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="dialogVisible = false">取消</t-button>
        <t-button theme="primary" :loading="dialogLoading" @click="handleSubmit">{{ isEditing ? '保存' : '提交申请' }}</t-button>
      </div>
    </t-dialog>

    <!-- 审批弹窗 -->
    <t-dialog v-model:visible="approveDialogVisible" header="审批加班" width="480px" :footer="false" placement="center" destroy-on-close>
      <div v-if="currentRow" class="approve-info">
        <p><strong>申请人：</strong>{{ currentRow.userName }}（{{ currentRow.userUsername }}）</p>
        <p><strong>加班日期：</strong>{{ currentRow.overtimeDate }}</p>
        <p><strong>加班时段：</strong>{{ currentRow.startTime }} ~ {{ currentRow.endTime }}（{{ currentRow.hours }}h）</p>
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
.overtime-page { display: flex; flex-direction: column; gap: 16px; }
.search-card { padding: 4px 0; }
.table-card { flex: 1; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.table-title { display: flex; align-items: center; gap: 12px; font-size: 16px; font-weight: 500; }
:::deep(.t-table th), :::deep(.t-table td) { text-align: center; vertical-align: middle; }
:::deep(.t-table td:first-child), :::deep(.t-table th:first-child) { text-align: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; padding-top: 16px; }
.overtime-form, .approve-form { padding: 16px 0; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 20px; margin-top: 8px; border-top: 1px solid #e7e7e7; }
.action-buttons { display: flex; justify-content: center; align-items: center; gap: 4px; min-width: 140px; }
.form-hint { margin-left: 8px; color: #999; font-size: 13px; }
.approve-info p { margin: 6px 0; color: #333; font-size: 14px; }
.approve-info { padding: 12px 0; border-bottom: 1px solid #f0f0f0; margin-bottom: 16px; }
</style>
