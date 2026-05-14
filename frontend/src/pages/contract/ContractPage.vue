<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import type { Contract } from '@/api/modules/contract'

const queryParams = ref({
  keyword: '',
  contractType: undefined as string | undefined,
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

const loading = ref(false)
const contractData = ref<Contract[]>([])
const total = ref(0)

interface Employee {
  id: number
  username: string
  name: string
}
const employeeList = ref<Employee[]>([])

// 到期提醒数据
const expiringList = ref<any[]>([])
const showExpiring = ref(false)

const contractTypeOptions = [
  { value: 'labor', label: '劳动合同' },
  { value: 'service', label: '劳务合同' },
  { value: 'internship', label: '实习协议' },
  { value: 'confidentiality', label: '保密协议' },
  { value: 'training', label: '培训协议' }
]

const statusOptions = [
  { value: 1, label: '有效' },
  { value: 0, label: '已过期' },
  { value: 2, label: '已解除' }
]

const columns = [
  { colKey: 'userName', title: '员工', width: '90' },
  { colKey: 'contractNo', title: '合同编号', width: '140' },
  { colKey: 'contractTypeName', title: '类型', width: '90' },
  { colKey: 'startDate', title: '开始日期', width: '110' },
  { colKey: 'endDate', title: '结束日期', width: '110' },
  { colKey: 'contractPeriod', title: '期限(月)', width: '80', align: 'center' },
  { colKey: 'statusName', title: '状态', width: '80', align: 'center' },
  { colKey: 'signDate', title: '签订日期', width: '110' },
  { colKey: 'action', title: '操作', width: '200', align: 'center', fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await import('@/api/modules/contract').then(m => m.getContractList(queryParams.value))
    if (res.data?.code === 200) {
      contractData.value = res.data.data.list || []
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
  contractData.value = [
    { id: 1, userId: 1, userName: '系统管理员', userUsername: 'admin', contractType: 'labor', contractTypeName: '劳动合同', contractNo: 'HT-2020-001', startDate: '2020-01-01', endDate: '2025-12-31', contractPeriod: 60, signDate: '2020-01-01', status: 1, statusName: '有效', remark: '', createTime: '2026-04-08' },
    { id: 2, userId: 2, userName: '李四', userUsername: 'HR001', contractType: 'labor', contractTypeName: '劳动合同', contractNo: 'HT-2021-001', startDate: '2021-06-01', endDate: '2027-05-31', contractPeriod: 72, signDate: '2021-06-01', status: 1, statusName: '有效', remark: '', createTime: '2026-04-08' },
    { id: 3, userId: 3, userName: '赵六', userUsername: 'EMP001', contractType: 'labor', contractTypeName: '劳动合同', contractNo: 'HT-2021-002', startDate: '2021-09-15', endDate: '2024-09-14', contractPeriod: 36, signDate: '2021-09-15', status: 0, statusName: '已过期', remark: '合同已过期，需续签', createTime: '2026-04-08' },
    { id: 4, userId: 4, userName: '王五', userUsername: 'EMP002', contractType: 'labor', contractTypeName: '劳动合同', contractNo: 'HT-2022-001', startDate: '2022-01-10', endDate: '2025-01-09', contractPeriod: 36, signDate: '2022-01-10', status: 1, statusName: '有效', remark: '', createTime: '2026-04-08' },
    { id: 5, userId: 5, userName: '钱七', userUsername: 'EMP003', contractType: 'labor', contractTypeName: '劳动合同', contractNo: 'HT-2019-001', startDate: '2019-05-20', endDate: '2029-05-19', contractPeriod: 120, signDate: '2019-05-20', status: 1, statusName: '有效', remark: '长期合同', createTime: '2026-04-08' },
    { id: 6, userId: 6, userName: '张三', userUsername: 'EMP004', contractType: 'internship', contractTypeName: '实习协议', contractNo: 'SX-2025-001', startDate: '2025-03-15', endDate: '2026-03-14', contractPeriod: 12, signDate: '2025-03-15', status: 0, statusName: '已过期', remark: '实习期结束，待转正', createTime: '2026-04-08' },
    { id: 7, userId: 4, userName: '王五', userUsername: 'EMP002', contractType: 'confidentiality', contractTypeName: '保密协议', contractNo: 'BM-2022-001', startDate: '2022-01-10', endDate: '2032-01-09', contractPeriod: 120, signDate: '2022-01-10', status: 1, statusName: '有效', remark: '', createTime: '2026-04-08' }
  ]
  total.value = contractData.value.length
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

const handleReset = () => { queryParams.value = { keyword: '', contractType: undefined, status: undefined, page: 1, pageSize: 10 }; loadData() }

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增合同')
const dialogLoading = ref(false)
const isEditing = ref(false)
const formRef = ref()

const formData = ref({
  userId: undefined as number | undefined,
  contractType: 'labor',
  contractNo: '',
  startDate: '' as string,
  endDate: '' as string,
  contractPeriod: 36 as number,
  signDate: '' as string,
  remark: ''
})

const formRules = {
  userId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  contractNo: [{ required: true, message: '请输入合同编号', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  signDate: [{ required: true, message: '请选择签订日期', trigger: 'change' }]
}

// 自动计算合同期限
const calcPeriod = () => {
  if (formData.value.startDate && formData.value.endDate) {
    const start = dayjs(formData.value.startDate)
    const end = dayjs(formData.value.endDate)
    formData.value.contractPeriod = end.diff(start, 'month')
  }
}

const openAddDialog = () => {
  dialogTitle.value = '新增合同'
  isEditing.value = false
  formData.value = { userId: undefined, contractType: 'labor', contractNo: '', startDate: '', endDate: '', contractPeriod: 36, signDate: '', remark: '' }
  dialogVisible.value = true
}

const openEditDialog = (row: Contract) => {
  dialogTitle.value = '编辑合同'
  isEditing.value = true
  formData.value = {
    userId: row.userId,
    contractType: row.contractType,
    contractNo: row.contractNo,
    startDate: row.startDate,
    endDate: row.endDate,
    contractPeriod: Number(row.contractPeriod) || 0,
    signDate: row.signDate,
    remark: row.remark || ''
  }
  ; (formData.value as any).id = row.id
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    dialogLoading.value = true

    if (isEditing.value) {
      const idx = contractData.value.findIndex(c => c.id === (formData.value as any).id)
      if (idx !== -1) {
        Object.assign(contractData.value[idx], {
          ...formData.value,
          contractTypeName: contractTypeOptions.find(t => t.value === formData.value.contractType)?.label,
          statusName: statusOptions.find(s => s.value === contractData.value[idx].status)?.label
        })
      }
      MessagePlugin.success('更新成功')
    } else {
      const newContract: Contract = {
        id: Date.now(),
        userId: formData.value.userId!,
        userName: employeeList.value.find(e => e.id === formData.value.userId)?.name || '',
        userUsername: employeeList.value.find(e => e.id === formData.value.userId)?.username || '',
        contractType: formData.value.contractType,
        contractTypeName: contractTypeOptions.find(t => t.value === formData.value.contractType)?.label || '',
        contractNo: formData.value.contractNo,
        startDate: formData.value.startDate,
        endDate: formData.value.endDate,
        contractPeriod: formData.value.contractPeriod,
        signDate: formData.value.signDate,
        status: 1,
        statusName: '有效',
        remark: formData.value.remark,
        createTime: dayjs().format('YYYY-MM-DD HH:mm')
      }
      contractData.value.unshift(newContract)
      total.value++
      MessagePlugin.success('新增成功')
    }

    dialogVisible.value = false
  } catch (e: any) {
    if (e !== true) MessagePlugin.error(e.message || '操作失败')
  } finally {
    dialogLoading.value = false
  }
}

const deleteContract = (row: Contract) => {
  DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除「${row.userName}」的合同吗？`,
    confirmBtn: '确定', cancelBtn: '取消',
    onConfirm: () => {
      const idx = contractData.value.findIndex(c => c.id === row.id)
      if (idx !== -1) { contractData.value.splice(idx, 1); total.value-- }
      MessagePlugin.success('删除成功')
    }
  })
}

// 终止合同
const terminateContract = (row: Contract) => {
  DialogPlugin.confirm({
    title: '终止合同',
    content: `确定要解除「${row.userName}」的合同（${row.contractNo}）吗？`,
    confirmBtn: '确认解除',
    cancelBtn: '取消',
    theme: 'warning',
    onConfirm: () => {
      const idx = contractData.value.findIndex(c => c.id === row.id)
      if (idx !== -1) {
        contractData.value[idx].status = 2
        contractData.value[idx].statusName = '已解除'
      }
      MessagePlugin.success('合同已解除')
    }
  })
}

// 查看到期提醒
const openExpiringDialog = async () => {
  showExpiring.value = true
  // 模拟到期数据
  expiringList.value = [
    { id: 4, userName: '王五', userUsername: 'EMP002', contractNo: 'HT-2022-001', contractTypeName: '劳动合同', endDate: '2025-05-06', daysLeft: -338 }, // 已过期但状态还是有效的情况
    { id: 1, userName: '系统管理员', contractNo: 'HT-2020-001', contractTypeName: '劳动合同', endDate: '2025-12-31', daysLeft: -99 }
  ].filter((item: any) => item.daysLeft <= 30 && item.daysLeft > -365)
}

// 辅助函数
const isExpiringSoon = (endDate: string): boolean => {
  if (!endDate) return false
  const end = dayjs(endDate)
  const daysLeft = end.diff(dayjs(), 'day')
  return daysLeft >= 0 && daysLeft <= 30
}

const getDaysLeft = (endDate: string): number => {
  return dayjs(endDate).diff(dayjs(), 'day')
}

onMounted(() => { loadData(); loadEmployees() })
</script>

<template>
  <div class="contract-page">
    <!-- 搜索区域 -->
    <t-card class="search-card" :bordered="false">
      <t-form :model="queryParams" layout="inline">
        <t-form-item label="关键词">
          <t-input v-model="queryParams.keyword" placeholder="搜索员工/合同编号" clearable style="width: 180px" @enter="handleSearch" />
        </t-form-item>
        <t-form-item label="合同类型">
          <t-select v-model="queryParams.contractType" placeholder="全部" clearable style="width: 120px">
            <t-option v-for="item in contractTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
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
            <t-button variant="outline" theme="warning" @click="openExpiringDialog">
              <template #icon><t-icon name="error-circle" /></template>到期提醒
            </t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </t-card>

    <!-- 数据表格 -->
    <t-card class="table-card" :bordered="false">
      <template #header>
        <div class="table-header">
          <div class="table-title"><span>合同管理</span><t-tag theme="primary" variant="light">{{ total }} 条</t-tag></div>
          <t-button theme="primary" @click="openAddDialog"><template #icon><t-icon name="add-circle" /></template>新增合同</t-button>
        </div>
      </template>

      <t-table :data="contractData" :loading="loading" :columns="columns" :hover="true" :row-key="'id'" stripe>
        <template #statusName="{ row }">
          <t-tag v-if="row.status === 1" theme="success" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else-if="row.status === 0" theme="default" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else theme="danger" size="small">{{ row.statusName }}</t-tag>
        </template>
        <!-- 合同到期预警 -->
        <template #endDate="{ row }">
          <span>{{ row.endDate }}</span>
          <t-tag v-if="row.status === 1 && isExpiringSoon(row.endDate)" theme="warning" size="small" style="margin-left: 4px">
            {{ getDaysLeft(row.endDate) }}天后到期
          </t-tag>
        </template>
        <template #action="{ row }">
          <div class="action-buttons">
            <t-button variant="text" size="small" @click="openEditDialog(row)">编辑</t-button>
            <t-button v-if="row.status === 1" variant="text" size="small" theme="warning" @click="terminateContract(row)">解除</t-button>
            <t-button variant="text" size="small" theme="danger" @click="deleteContract(row)">删除</t-button>
          </div>
        </template>
      </t-table>

      <div class="pagination-wrapper">
        <t-pagination v-model:current="queryParams.page" v-model:page-size="queryParams.pageSize" :total="total" show-page-size size="small" @change="loadData" />
      </div>
    </t-card>

    <!-- 新增/编辑弹窗 -->
    <t-dialog v-model:visible="dialogVisible" :header="dialogTitle" width="600px" :footer="false" placement="center" destroy-on-close>
      <t-form ref="formRef" :data="formData" :rules="formRules" label-width="100px" layout="vertical" class="contract-form">
        <t-form-item label="所属员工" name="userId">
          <t-select v-model="formData.userId" placeholder="请选择员工" filterable>
            <t-option v-for="emp in employeeList" :key="emp.id" :value="emp.id" :label="`${emp.name} (${emp.username})`" />
          </t-select>
        </t-form-item>
        <t-row :gutter="16">
          <t-col :span="8">
            <t-form-item label="合同类型" name="contractType">
              <t-select v-model="formData.contractType">
                <t-option v-for="item in contractTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
              </t-select>
            </t-form-item>
          </t-col>
          <t-col :span="16">
            <t-form-item label="合同编号" name="contractNo">
              <t-input v-model="formData.contractNo" placeholder="如 HT-2026-001" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-row :gutter="16">
          <t-col :span="8">
            <t-form-item label="开始日期" name="startDate">
              <t-date-picker v-model="formData.startDate" enable-time-picker={false} format="YYYY-MM-DD" @change="calcPeriod" />
            </t-form-item>
          </t-col>
          <t-col :span="8">
            <t-form-item label="结束日期" name="endDate">
              <t-date-picker v-model="formData.endDate" enable-time-picker={false} format="YYYY-MM-DD" @change="calcPeriod" />
            </t-form-item>
          </t-col>
          <t-col :span="8">
            <t-form-item label="合同期限(月)">
              <t-input-number v-model="formData.contractPeriod" :min="1" theme="normal" style="width: 100%" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="签订日期" name="signDate">
          <t-date-picker v-model="formData.signDate" enable-time-picker={false} format="YYYY-MM-DD" />
        </t-form-item>
        <t-form-item label="备注">
          <t-textarea v-model="formData.remark" placeholder="可选填备注信息" :autosize="{ minRows: 2, maxRows: 4 }" />
        </t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="dialogVisible = false">取消</t-button>
        <t-button theme="primary" :loading="dialogLoading" @click="handleSubmit">{{ isEditing ? '保存' : '创建' }}</t-button>
      </div>
    </t-dialog>

    <!-- 到期提醒弹窗 -->
    <t-dialog v-model:visible="showExpiring" header="合同到期提醒" width="600px" :footer="false" placement="center" destroy-on-close>
      <t-alert theme="warning" title="以下合同将在30天内到期或已过期，请及时处理" :close="true" style="margin-bottom: 16px;" />
      <t-table :data="expiringList" :hover="true" :row-key="'id'" size="small" bordered stripe :columns="[
        { colKey: 'userName', title: '员工', width: 100 },
        { colKey: 'contractNo', title: '合同编号', width: 150 },
        { colKey: 'contractTypeName', title: '类型', width: 100 },
        { colKey: 'endDate', title: '到期日', width: 120 },
        { colKey: 'daysLeft', title: '剩余天数', width: 100, align: 'center' }
      ]">
        <template #daysLeft="{ row }">
          <t-tag v-if="row.daysLeft > 0" theme="warning" size="small">{{ row.daysLeft }}天</t-tag>
          <t-tag v-else theme="danger" size="small">已过期{{ Math.abs(row.daysLeft) }}天</t-tag>
        </template>
      </t-table>
    </t-dialog>
  </div>
</template>

<style scoped>
.contract-page { display: flex; flex-direction: column; gap: 16px; }
.search-card { padding: 4px 0; }
.table-card { flex: 1; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.table-title { display: flex; align-items: center; gap: 12px; font-size: 16px; font-weight: 500; }
:::deep(.t-table th), :::deep(.t-table td) { text-align: center; vertical-align: middle; }
:::deep(.t-table td:first-child), :::deep(.t-table th:first-child) { text-align: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; padding-top: 16px; }
.contract-form { padding: 16px 0; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 20px; margin-top: 8px; border-top: 1px solid #e7e7e7; }
.action-buttons { display: flex; justify-content: center; align-items: center; gap: 4px; min-width: 180px; }
</style>
