<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import { departmentOptions as allDeptOptions, getTopDepartmentId } from '@/utils/deptData'

// 考勤数据类型
interface AttendanceRecord {
  id: number
  date: string
  employeeNo: string
  name: string
  department: string
  departmentId: number
  checkIn: string
  checkInStatus: 'normal' | 'late' | 'missing'
  checkOut: string
  checkOutStatus: 'normal' | 'early' | 'missing'
  workHours: number
  status: 'normal' | 'abnormal'
  remark: string
}

// 查询参数
const queryParams = ref({
  keyword: '',
  departmentId: undefined as number | undefined,
  dateRange: [] as string[],
  status: undefined as string | undefined,
  page: 1,
  pageSize: 10
})

// 考勤数据
const loading = ref(false)
const attendanceData = ref<AttendanceRecord[]>([
  { id: 1, date: '2026-04-06', employeeNo: 'EMP001', name: '张三', department: '生产部', departmentId: 1, checkIn: '08:25', checkInStatus: 'normal', checkOut: '17:35', checkOutStatus: 'normal', workHours: 8.2, status: 'normal', remark: '' },
  { id: 2, date: '2026-04-06', employeeNo: 'EMP002', name: '李四', department: '品质部', departmentId: 2, checkIn: '08:30', checkInStatus: 'normal', checkOut: '17:28', checkOutStatus: 'normal', workHours: 7.97, status: 'normal', remark: '' },
  { id: 3, date: '2026-04-06', employeeNo: 'EMP003', name: '王五', department: '研发部', departmentId: 3, checkIn: '08:45', checkInStatus: 'late', checkOut: '18:00', checkOutStatus: 'normal', workHours: 8.25, status: 'abnormal', remark: '堵车' },
  { id: 4, date: '2026-04-06', employeeNo: 'EMP004', name: '赵六', department: '人事部', departmentId: 7, checkIn: '08:30', checkInStatus: 'normal', checkOut: '17:30', checkOutStatus: 'normal', workHours: 8.0, status: 'normal', remark: '' },
  { id: 5, date: '2026-04-05', employeeNo: 'EMP001', name: '张三', department: '生产部', departmentId: 1, checkIn: '08:28', checkInStatus: 'normal', checkOut: '17:32', checkOutStatus: 'normal', workHours: 8.07, status: 'normal', remark: '' },
  { id: 6, date: '2026-04-05', employeeNo: 'EMP002', name: '李四', department: '品质部', departmentId: 2, checkIn: '08:35', checkInStatus: 'late', checkOut: '17:30', checkOutStatus: 'normal', workHours: 7.92, status: 'abnormal', remark: '地铁延误' },
  { id: 7, date: '2026-04-05', employeeNo: 'EMP003', name: '王五', department: '研发部', departmentId: 3, checkIn: '08:30', checkInStatus: 'normal', checkOut: '18:05', checkOutStatus: 'normal', workHours: 8.58, status: 'normal', remark: '' },
  { id: 8, date: '2026-04-05', employeeNo: 'EMP004', name: '赵六', department: '人事部', departmentId: 7, checkIn: '08:30', checkInStatus: 'normal', checkOut: '', checkOutStatus: 'missing', workHours: 0, status: 'abnormal', remark: '忘记打卡' }
])

const total = ref(8)
// 过滤后用于表格展示的数据
const displayData = ref<AttendanceRecord[]>([])

// 部门选项（使用统一数据源）
const departmentOptions = allDeptOptions

// 状态选项
const statusOptions = [
  { value: 'normal', label: '正常' },
  { value: 'abnormal', label: '异常' },
  { value: 'late', label: '迟到' },
  { value: 'early', label: '早退' },
  { value: 'missing', label: '缺卡' }
]

// 表格列配置
const columns = [
  { colKey: 'date', title: '日期', width: '110' },
  { colKey: 'employeeNo', title: '工号', width: '100' },
  { colKey: 'name', title: '姓名', width: '80' },
  { colKey: 'department', title: '部门', width: '100' },
  { colKey: 'checkIn', title: '上班打卡', width: '100' },
  { colKey: 'checkInStatus', title: '上班状态', width: '90' },
  { colKey: 'checkOut', title: '下班打卡', width: '100' },
  { colKey: 'checkOutStatus', title: '下班状态', width: '90' },
  { colKey: 'workHours', title: '出勤时长', width: '100' },
  { colKey: 'status', title: '考勤结果', width: '90' },
  { colKey: 'remark', title: '备注', width: '100', ellipsis: true },
  { colKey: 'action', title: '操作', width: '120', align: 'center' }
]

// 统计数据
const stats = computed(() => ({
  normal: displayData.value.filter(r => r.status === 'normal').length,
  late: displayData.value.filter(r => r.checkInStatus === 'late').length,
  early: displayData.value.filter(r => r.checkOutStatus === 'early').length,
  missing: displayData.value.filter(r => r.checkInStatus === 'missing' || r.checkOutStatus === 'missing').length
}))

// 搜索
const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    let filtered = [...attendanceData.value]
    if (queryParams.value.keyword) {
      filtered = filtered.filter(r => 
        r.name.includes(queryParams.value.keyword) || 
        r.employeeNo.includes(queryParams.value.keyword)
      )
    }
    if (queryParams.value.departmentId) {
      // 父子部门模糊匹配
      filtered = filtered.filter(r =>
        getTopDepartmentId(r.departmentId) === getTopDepartmentId(queryParams.value.departmentId!)
      )
    }
    if (queryParams.value.dateRange && queryParams.value.dateRange.length === 2) {
      const [start, end] = queryParams.value.dateRange
      filtered = filtered.filter(r => r.date >= start && r.date <= end)
    }
    if (queryParams.value.status) {
      filtered = filtered.filter(r => r.status === queryParams.value.status)
    }
    displayData.value = filtered
    total.value = filtered.length
    queryParams.value.page = 1
    loading.value = false
  }, 300)
}

// 重置
const handleReset = () => {
  queryParams.value = {
    keyword: '',
    departmentId: undefined,
    dateRange: [],
    status: undefined,
    page: 1,
    pageSize: 10
  }
  handleSearch()
}

// 分页变化
const handlePageChange = (pageInfo: { page: number; pageSize: number }) => {
  queryParams.value.page = pageInfo.page
  queryParams.value.pageSize = pageInfo.pageSize
}

// 编辑弹窗
const editDialogVisible = ref(false)
const editFormRef = ref()
const editFormData = ref<Partial<AttendanceRecord>>({})
const editLoading = ref(false)

// 打开编辑弹窗
const openEditDialog = (row: AttendanceRecord) => {
  editFormData.value = { ...row }
  editDialogVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  try {
    await editFormRef.value?.validate()
    editLoading.value = true
    
    // 模拟提交
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 更新数据
    const index = attendanceData.value.findIndex(r => r.id === editFormData.value.id)
    if (index !== -1) {
      attendanceData.value[index] = { ...attendanceData.value[index], ...editFormData.value }
    }
    
    MessagePlugin.success('更新成功')
    editDialogVisible.value = false
  } catch (e: any) {
    if (e !== true) {
      MessagePlugin.error(e.message || '操作失败')
    }
  } finally {
    editLoading.value = false
  }
}

// 删除
const handleDelete = (row: AttendanceRecord) => {
  const dialog = DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除 ${row.date} ${row.name} 的考勤记录吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        await new Promise(resolve => setTimeout(resolve, 300))
        const index = attendanceData.value.findIndex(r => r.id === row.id)
        if (index !== -1) {
          attendanceData.value.splice(index, 1)
        }
        const dIndex = displayData.value.findIndex(r => r.id === row.id)
        if (dIndex !== -1) {
          displayData.value.splice(dIndex, 1)
        }
        total.value--
        MessagePlugin.success('删除成功')
        dialog.destroy()
      } catch (e) {
        MessagePlugin.error('删除失败')
      }
    }
  })
}

// 导出
const handleExport = () => {
  MessagePlugin.info('正在导出考勤数据...')
}

// 批量导入
const handleImport = () => {
  MessagePlugin.info('打开批量导入功能...')
}

// 初始化 displayData
onMounted(() => {
  displayData.value = [...attendanceData.value]
})
</script>

<template>
  <div class="attendance-page">
    <t-card title="考勤管理">
      <template #actions>
        <t-space>
          <t-button theme="primary" @click="handleExport">
            <template #icon>
              <t-icon name="download" />
            </template>
            导出报表
          </t-button>
          <t-button variant="outline" @click="handleImport">
            <template #icon>
              <t-icon name="upload" />
            </template>
            批量导入
          </t-button>
        </t-space>
      </template>

      <!-- 搜索区域 -->
      <div class="search-bar mb-4">
        <t-form layout="inline">
          <t-form-item label="关键词">
            <t-input
              v-model="queryParams.keyword"
              placeholder="姓名/工号"
              clearable
              style="width: 150px"
              @enter="handleSearch"
            />
          </t-form-item>
          <t-form-item label="部门">
            <t-select
              v-model="queryParams.departmentId"
              placeholder="请选择部门"
              clearable
              style="width: 150px"
            >
              <t-option v-for="item in departmentOptions" :key="item.value" :value="item.value" :label="item.label" />
            </t-select>
          </t-form-item>
          <t-form-item label="日期范围">
            <t-date-range-picker v-model="queryParams.dateRange" allow-input clearable />
          </t-form-item>
          <t-form-item label="考勤状态">
            <t-select
              v-model="queryParams.status"
              placeholder="请选择状态"
              clearable
              style="width: 120px"
            >
              <t-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
            </t-select>
          </t-form-item>
        </t-form>
        <div class="search-actions">
          <t-button theme="primary" @click="handleSearch">
            <template #icon>
              <t-icon name="search" />
            </template>
            搜索
          </t-button>
          <t-button variant="outline" @click="handleReset">重置</t-button>
        </div>
      </div>

      <!-- 统计卡片 -->
      <t-row :gutter="[16, 16]" class="mb-4">
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-mini-content">
              <span class="stat-mini-value" style="color: #00a870">{{ stats.normal }}</span>
              <span class="stat-mini-label">正常出勤</span>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-mini-content">
              <span class="stat-mini-value" style="color: #ff7a00">{{ stats.late }}</span>
              <span class="stat-mini-label">迟到</span>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-mini-content">
              <span class="stat-mini-value" style="color: #e34d57">{{ stats.early }}</span>
              <span class="stat-mini-label">早退</span>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-mini-content">
              <span class="stat-mini-value" style="color: #9747ff">{{ stats.missing }}</span>
              <span class="stat-mini-label">缺卡</span>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <!-- 表格 -->
      <t-table
        :data="displayData"
        :columns="columns"
        row-key="id"
        :hover="true"
        :loading="loading"
        :stripe="true"
      >
        <!-- 上班打卡 -->
        <template #checkIn="{ row }">
          <span :style="{ color: row.checkInStatus === 'normal' ? '#00a870' : row.checkInStatus === 'late' ? '#ff7a00' : '#e34d57' }">
            {{ row.checkIn || '-' }}
          </span>
        </template>
        <!-- 上班状态 -->
        <template #checkInStatus="{ row }">
          <t-tag v-if="row.checkInStatus === 'normal'" theme="success" size="small">正常</t-tag>
          <t-tag v-else-if="row.checkInStatus === 'late'" theme="warning" size="small">迟到</t-tag>
          <t-tag v-else theme="danger" size="small">缺卡</t-tag>
        </template>
        <!-- 下班打卡 -->
        <template #checkOut="{ row }">
          <span :style="{ color: row.checkOutStatus === 'normal' ? '#00a870' : row.checkOutStatus === 'early' ? '#ff7a00' : '#e34d57' }">
            {{ row.checkOut || '-' }}
          </span>
        </template>
        <!-- 下班状态 -->
        <template #checkOutStatus="{ row }">
          <t-tag v-if="row.checkOutStatus === 'normal'" theme="success" size="small">正常</t-tag>
          <t-tag v-else-if="row.checkOutStatus === 'early'" theme="warning" size="small">早退</t-tag>
          <t-tag v-else theme="danger" size="small">缺卡</t-tag>
        </template>
        <!-- 出勤时长 -->
        <template #workHours="{ row }">
          {{ row.workHours.toFixed(1) }}h
        </template>
        <!-- 考勤结果 -->
        <template #status="{ row }">
          <t-tag :theme="row.status === 'normal' ? 'success' : 'danger'" size="small">
            {{ row.status === 'normal' ? '正常' : '异常' }}
          </t-tag>
        </template>
        <!-- 备注 -->
        <template #remark="{ row }">
          <span :style="{ color: row.remark ? '#666' : '#999' }">{{ row.remark || '-' }}</span>
        </template>
        <!-- 操作 -->
        <template #action="{ row }">
          <t-space>
            <t-button variant="text" size="small" @click="openEditDialog(row)">编辑</t-button>
            <t-button variant="text" size="small" theme="danger" @click="handleDelete(row)">删除</t-button>
          </t-space>
        </template>
      </t-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <t-pagination
          v-model:page="queryParams.page"
          v-model:pageSize="queryParams.pageSize"
          :total="total"
          :page-size-options="[10, 20, 50]"
          @change="handlePageChange"
        />
      </div>
    </t-card>

    <!-- 编辑弹窗 -->
    <t-dialog
      v-model:visible="editDialogVisible"
      title="编辑考勤记录"
      width="500px"
      :footer="false"
    >
      <t-form
        ref="editFormRef"
        :model="editFormData"
        label-width="100px"
        layout="vertical"
      >
        <t-form-item label="工号">
          <t-input :model-value="editFormData.employeeNo" disabled />
        </t-form-item>
        <t-form-item label="姓名">
          <t-input :model-value="editFormData.name" disabled />
        </t-form-item>
        <t-form-item label="上班打卡">
          <t-time-picker v-model="editFormData.checkIn" format="HH:mm" />
        </t-form-item>
        <t-form-item label="下班打卡">
          <t-time-picker v-model="editFormData.checkOut" format="HH:mm" />
        </t-form-item>
        <t-form-item label="备注">
          <t-input v-model="editFormData.remark" placeholder="请输入备注" />
        </t-form-item>
        <div class="form-footer">
          <t-space>
            <t-button theme="primary" :loading="editLoading" @click="handleEditSubmit">确定</t-button>
            <t-button variant="outline" @click="editDialogVisible = false">取消</t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>
  </div>
</template>

<style scoped>
.attendance-page {
  width: 100%;
}

.mb-4 {
  margin-bottom: 16px;
}

.search-bar {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.search-actions {
  display: flex;
  gap: 8px;
}

.stat-mini {
  text-align: center;
}

.stat-mini-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-mini-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-mini-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

/* 表格样式优化 */
:deep(.t-table) {
  font-size: 14px;
}

:deep(.t-table th),
:deep(.t-table td) {
  text-align: center;
  vertical-align: middle;
}

:deep(.t-table td:first-child),
:deep(.t-table th:first-child) {
  text-align: center;
}
</style>
