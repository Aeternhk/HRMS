<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import { departmentOptions as allDeptOptions, getTopDepartmentId } from '@/utils/deptData'

// 考核类型
type AssessmentType = 'monthly' | 'annual'

// 考核数据类型
interface Performance {
  id: number
  employeeNo: string
  name: string
  department: string
  departmentId: number
  period: string
  periodType: AssessmentType
  workAttitude: number
  workQuality: number
  workEfficiency: number
  teamwork: number
  innovation: number
  totalScore: number
  rank: string
  status: 'pending' | 'graded' | 'confirmed'
  statusName: string
  assessor: string
  assessTime?: string
  remark?: string
}

// 查询参数
const queryParams = ref({
  keyword: '',
  departmentId: undefined as number | undefined,
  quarter: '',
  status: undefined as string | undefined,
  page: 1,
  pageSize: 10
})

// 考核数据
const loading = ref(false)
const performanceData = ref<Performance[]>([
  { id: 1, employeeNo: 'EMP001', name: '张三', department: '生产部', departmentId: 1, period: '2026-Q1', periodType: 'monthly', workAttitude: 90, workQuality: 85, workEfficiency: 88, teamwork: 82, innovation: 75, totalScore: 84.8, rank: 'A', status: 'confirmed', statusName: '已确认', assessor: '钱七', assessTime: '2026-04-01' },
  { id: 2, employeeNo: 'EMP002', name: '李四', department: '品质部', departmentId: 2, period: '2026-Q1', periodType: 'monthly', workAttitude: 95, workQuality: 92, workEfficiency: 90, teamwork: 88, innovation: 85, totalScore: 90.8, rank: 'S', status: 'confirmed', statusName: '已确认', assessor: '赵六', assessTime: '2026-04-01' },
  { id: 3, employeeNo: 'EMP003', name: '王五', department: '研发部', departmentId: 3, period: '2026-Q1', periodType: 'monthly', workAttitude: 88, workQuality: 90, workEfficiency: 92, teamwork: 85, innovation: 95, totalScore: 89.9, rank: 'A', status: 'confirmed', statusName: '已确认', assessor: '李四', assessTime: '2026-04-01' },
  { id: 4, employeeNo: 'EMP004', name: '赵六', department: '人事部', departmentId: 7, period: '2026-Q1', periodType: 'monthly', workAttitude: 92, workQuality: 88, workEfficiency: 85, teamwork: 90, innovation: 80, totalScore: 87.3, rank: 'B', status: 'graded', statusName: '已评分', assessor: '人事主管', assessTime: '2026-04-02' },
  { id: 5, employeeNo: 'EMP005', name: '钱七', department: '生产部', departmentId: 1, period: '2026-Q1', periodType: 'monthly', workAttitude: 85, workQuality: 82, workEfficiency: 80, teamwork: 78, innovation: 70, totalScore: 79.5, rank: 'C', status: 'pending', statusName: '待评分', assessor: '-' },
  { id: 6, employeeNo: 'EMP006', name: '孙八', department: '采购部', departmentId: 4, period: '2026-Q1', periodType: 'monthly', workAttitude: 90, workQuality: 88, workEfficiency: 86, teamwork: 85, innovation: 78, totalScore: 85.9, rank: 'A', status: 'confirmed', statusName: '已确认', assessor: '采购主管', assessTime: '2026-04-01' }
])

const total = ref(6)
// 过滤后用于表格展示的数据
const displayData = ref<Performance[]>([])

// 部门选项（使用统一数据源，包含子部门）
const departmentOptions = allDeptOptions

// 季度选项
const quarterOptions = [
  { value: '2026-Q1', label: '2026年Q1' },
  { value: '2026-Q2', label: '2026年Q2' },
  { value: '2025-Q4', label: '2025年Q4' },
  { value: '2025-Q3', label: '2025年Q3' }
]

// 状态选项
const statusOptions = [
  { value: 'pending', label: '待评分' },
  { value: 'graded', label: '已评分' },
  { value: 'confirmed', label: '已确认' }
]

// 表格列配置
const columns = [
  { colKey: 'employeeNo', title: '工号', width: '90', ellipsis: true },
  { colKey: 'name', title: '姓名', width: '70', ellipsis: true },
  { colKey: 'department', title: '部门', width: '100', ellipsis: true },
  { colKey: 'period', title: '考核周期', width: '90', ellipsis: true },
  { colKey: 'workAttitude', title: '态度', width: '60', ellipsis: true },
  { colKey: 'workQuality', title: '质量', width: '60', ellipsis: true },
  { colKey: 'workEfficiency', title: '效率', width: '60', ellipsis: true },
  { colKey: 'teamwork', title: '协作', width: '60', ellipsis: true },
  { colKey: 'innovation', title: '创新', width: '60', ellipsis: true },
  { colKey: 'totalScore', title: '总分', width: '60', ellipsis: true },
  { colKey: 'rank', title: '等级', width: '60', align: 'center', ellipsis: true },
  { colKey: 'status', title: '状态', width: '80', ellipsis: true },
  { colKey: 'action', title: '操作', width: '150', align: 'center', fixed: 'right' }
]

// 获取分数颜色
const getScoreColor = (score: number) => {
  if (score >= 90) return '#00a870'
  if (score >= 80) return '#0052d9'
  if (score >= 70) return '#ff7a00'
  return '#e34d57'
}

// 获取等级主题色
const getRankTheme = (rank: string) => {
  const map: Record<string, string> = {
    'S': 'success',
    'A': 'primary',
    'B': 'warning',
    'C': 'danger'
  }
  return map[rank] || 'default'
}

// 统计数据
const stats = computed(() => {
  const confirmed = displayData.value.filter(r => r.status === 'confirmed')
  const avgScore = confirmed.length > 0 
    ? confirmed.reduce((sum, r) => sum + r.totalScore, 0) / confirmed.length 
    : 0
  return {
    total: displayData.value.length,
    confirmed: confirmed.length,
    pending: displayData.value.filter(r => r.status === 'pending').length,
    avgScore: avgScore.toFixed(1)
  }
})

// 搜索
const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    let filtered = [...performanceData.value]
    if (queryParams.value.keyword) {
      filtered = filtered.filter(r => 
        r.name.includes(queryParams.value.keyword) || 
        r.employeeNo.includes(queryParams.value.keyword)
      )
    }
    if (queryParams.value.departmentId) {
      // 父子部门模糊匹配：所选部门的顶级部门ID等于记录所在顶级部门ID即匹配
      filtered = filtered.filter(r =>
        getTopDepartmentId(r.departmentId) === getTopDepartmentId(queryParams.value.departmentId!)
      )
    }
    if (queryParams.value.quarter) {
      filtered = filtered.filter(r => r.period === queryParams.value.quarter)
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
    quarter: '',
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

// 发起考核弹窗
const launchDialogVisible = ref(false)
const launchFormRef = ref()
const launchLoading = ref(false)
const launchFormData = ref({
  period: '',
  periodType: 'monthly' as AssessmentType,
  departmentIds: [] as number[],
  remark: ''
})

const launchFormRules = {
  period: [{ required: true, message: '请选择考核周期', type: 'error' }],
  departmentIds: [{ required: true, message: '请选择考核部门', type: 'error' }]
}

const openLaunchDialog = () => {
  launchFormData.value = {
    period: '',
    periodType: 'monthly',
    departmentIds: [],
    remark: ''
  }
  launchDialogVisible.value = true
}

const handleLaunch = async () => {
  try {
    await launchFormRef.value?.validate()
    launchLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 500))
    
    MessagePlugin.success('发起考核成功')
    launchDialogVisible.value = false
  } catch (e: any) {
    if (e !== true) {
      MessagePlugin.error(e.message || '操作失败')
    }
  } finally {
    launchLoading.value = false
  }
}

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref<Performance | null>(null)

const openDetailDialog = (row: Performance) => {
  detailData.value = { ...row }
  detailDialogVisible.value = true
}

// 评分弹窗
const assessDialogVisible = ref(false)
const assessFormRef = ref()
const assessLoading = ref(false)
const assessFormData = ref<Partial<Performance>>({})

const openAssessDialog = (row: Performance) => {
  assessFormData.value = {
    ...row,
    workAttitude: row.workAttitude || 0,
    workQuality: row.workQuality || 0,
    workEfficiency: row.workEfficiency || 0,
    teamwork: row.teamwork || 0,
    innovation: row.innovation || 0
  }
  assessDialogVisible.value = true
}

const handleAssess = async () => {
  try {
    await assessFormRef.value?.validate()
    assessLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 计算总分
    const data = assessFormData.value
    if (data.workAttitude && data.workQuality && data.workEfficiency && data.teamwork && data.innovation) {
      data.totalScore = data.workAttitude * 0.2 + data.workQuality * 0.3 + data.workEfficiency * 0.25 + data.teamwork * 0.15 + data.innovation * 0.1
      data.rank = getRank(data.totalScore)
      data.status = 'graded'
      data.statusName = '已评分'
      data.assessTime = dayjs().format('YYYY-MM-DD')
    }
    
    const index = performanceData.value.findIndex(p => p.id === data.id)
    if (index !== -1) {
      performanceData.value[index] = { ...performanceData.value[index], ...data }
    }
    // 同步更新displayData
    const dIndex = displayData.value.findIndex(p => p.id === data.id)
    if (dIndex !== -1) {
      displayData.value[dIndex] = { ...displayData.value[dIndex], ...data }
    }
    
    MessagePlugin.success('评分成功')
    assessDialogVisible.value = false
  } catch (e: any) {
    if (e !== true) {
      MessagePlugin.error(e.message || '操作失败')
    }
  } finally {
    assessLoading.value = false
  }
}

const getRank = (score: number): string => {
  if (score >= 95) return 'S'
  if (score >= 85) return 'A'
  if (score >= 75) return 'B'
  return 'C'
}

// 删除
const handleDelete = (row: Performance) => {
  const dialog = DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除 ${row.period} ${row.name} 的考核记录吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        await new Promise(resolve => setTimeout(resolve, 300))
        const index = performanceData.value.findIndex(p => p.id === row.id)
        if (index !== -1) {
          performanceData.value.splice(index, 1)
        }
        // 同步更新displayData
        const dIndex = displayData.value.findIndex(p => p.id === row.id)
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
  MessagePlugin.info('正在导出考核报表...')
}

// 初始化displayData
onMounted(() => {
  displayData.value = [...performanceData.value]
})

// 计算当前评分总分
const calculateScore = computed(() => {
  const d = assessFormData.value
  if (d.workAttitude && d.workQuality && d.workEfficiency && d.teamwork && d.innovation) {
    return (d.workAttitude * 0.2 + d.workQuality * 0.3 + d.workEfficiency * 0.25 + d.teamwork * 0.15 + d.innovation * 0.1).toFixed(1)
  }
  return '0'
})
</script>

<template>
  <div class="performance-page">
    <t-card title="绩效考核">
      <template #actions>
        <t-space>
          <t-button theme="primary" @click="openLaunchDialog">
            <template #icon>
              <t-icon name="add" />
            </template>
            发起考核
          </t-button>
          <t-button variant="outline" @click="handleExport">
            <template #icon>
              <t-icon name="download" />
            </template>
            导出报表
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
          <t-form-item label="考核周期">
            <t-select
              v-model="queryParams.quarter"
              placeholder="请选择周期"
              clearable
              style="width: 150px"
            >
              <t-option v-for="item in quarterOptions" :key="item.value" :value="item.value" :label="item.label" />
            </t-select>
          </t-form-item>
          <t-form-item label="状态">
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
            查询
          </t-button>
          <t-button variant="outline" @click="handleReset">重置</t-button>
        </div>
      </div>

      <!-- 统计概览 -->
      <t-row :gutter="[16, 16]" class="mb-4">
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-content">
              <t-icon name="chart-line" style="color: #0052d9; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">{{ stats.total }}人</p>
                <p class="stat-label">考核人数</p>
              </div>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-content">
              <t-icon name="check-circle" style="color: #00a870; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">{{ stats.confirmed }}人</p>
                <p class="stat-label">已确认</p>
              </div>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-content">
              <t-icon name="time" style="color: #ff7a00; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">{{ stats.pending }}人</p>
                <p class="stat-label">待评分</p>
              </div>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-content">
              <t-icon name="chart-bar" style="color: #9747ff; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">{{ stats.avgScore }}分</p>
                <p class="stat-label">平均分</p>
              </div>
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
        <!-- 工作态度 -->
        <template #workAttitude="{ row }">{{ row.workAttitude }}分</template>
        <!-- 工作质量 -->
        <template #workQuality="{ row }">{{ row.workQuality }}分</template>
        <!-- 工作效率 -->
        <template #workEfficiency="{ row }">{{ row.workEfficiency }}分</template>
        <!-- 团队协作 -->
        <template #teamwork="{ row }">{{ row.teamwork }}分</template>
        <!-- 创新能力 -->
        <template #innovation="{ row }">{{ row.innovation }}分</template>
        <!-- 综合得分 -->
        <template #totalScore="{ row }">
          <span :style="{ color: getScoreColor(row.totalScore), fontWeight: 600 }">
            {{ row.totalScore.toFixed(1) }}
          </span>
        </template>
        <!-- 等级 -->
        <template #rank="{ row }">
          <t-tag :theme="getRankTheme(row.rank)" size="small">{{ row.rank }}</t-tag>
        </template>
        <!-- 状态 -->
        <template #status="{ row }">
          <t-tag v-if="row.status === 'pending'" theme="warning" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else-if="row.status === 'graded'" theme="primary" size="small">{{ row.statusName }}</t-tag>
          <t-tag v-else theme="success" size="small">{{ row.statusName }}</t-tag>
        </template>
        <!-- 操作 -->
        <template #action="{ row }">
          <t-space>
            <t-button variant="text" size="small" @click="openDetailDialog(row)">详情</t-button>
            <t-button variant="text" size="small" @click="openAssessDialog(row)">评分</t-button>
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

    <!-- 发起考核弹窗 -->
    <t-dialog
      v-model:visible="launchDialogVisible"
      title="发起考核"
      width="500px"
      :footer="false"
    >
      <t-form
        ref="launchFormRef"
        :model="launchFormData"
        :rules="launchFormRules"
        label-width="100px"
        layout="vertical"
      >
        <t-form-item label="考核周期" name="period">
          <t-select v-model="launchFormData.period" placeholder="请选择考核周期">
            <t-option v-for="item in quarterOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item label="考核类型">
          <t-radio-group v-model="launchFormData.periodType">
            <t-radio value="monthly">月度考核</t-radio>
            <t-radio value="annual">年度考核</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="考核部门" name="departmentIds">
          <t-checkbox-group v-model="launchFormData.departmentIds">
            <t-checkbox v-for="item in departmentOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-checkbox-group>
        </t-form-item>
        <t-form-item label="备注">
          <t-textarea v-model="launchFormData.remark" placeholder="请输入备注" :rows="2" />
        </t-form-item>
        <div class="form-footer">
          <t-space>
            <t-button theme="primary" :loading="launchLoading" @click="handleLaunch">发起</t-button>
            <t-button variant="outline" @click="launchDialogVisible = false">取消</t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>

    <!-- 详情弹窗 -->
    <t-dialog
      v-model:visible="detailDialogVisible"
      title="考核详情"
      width="600px"
      :footer="false"
    >
      <template v-if="detailData">
        <t-descriptions :column="2" bordered>
          <t-descriptions-item label="工号">{{ detailData.employeeNo }}</t-descriptions-item>
          <t-descriptions-item label="姓名">{{ detailData.name }}</t-descriptions-item>
          <t-descriptions-item label="部门">{{ detailData.department }}</t-descriptions-item>
          <t-descriptions-item label="考核周期">{{ detailData.period }}</t-descriptions-item>
          <t-descriptions-item label="综合得分">
            <span :style="{ color: getScoreColor(detailData.totalScore), fontWeight: 600, fontSize: '18px' }">
              {{ detailData.totalScore.toFixed(1) }}分
            </span>
          </t-descriptions-item>
          <t-descriptions-item label="等级">
            <t-tag :theme="getRankTheme(detailData.rank)" size="large">{{ detailData.rank }}</t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="状态">
            <t-tag :theme="detailData.status === 'confirmed' ? 'success' : detailData.status === 'graded' ? 'primary' : 'warning'">
              {{ detailData.statusName }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="评分人">{{ detailData.assessor }}</t-descriptions-item>
        </t-descriptions>
        
        <div class="score-details">
          <h4>评分明细</h4>
          <t-descriptions :column="2" bordered size="small">
            <t-descriptions-item label="工作态度（权重20%）">
              <span :style="{ color: getScoreColor(detailData.workAttitude) }">{{ detailData.workAttitude }}分</span>
            </t-descriptions-item>
            <t-descriptions-item label="工作质量（权重30%）">
              <span :style="{ color: getScoreColor(detailData.workQuality) }">{{ detailData.workQuality }}分</span>
            </t-descriptions-item>
            <t-descriptions-item label="工作效率（权重25%）">
              <span :style="{ color: getScoreColor(detailData.workEfficiency) }">{{ detailData.workEfficiency }}分</span>
            </t-descriptions-item>
            <t-descriptions-item label="团队协作（权重15%）">
              <span :style="{ color: getScoreColor(detailData.teamwork) }">{{ detailData.teamwork }}分</span>
            </t-descriptions-item>
            <t-descriptions-item label="创新能力（权重10%）" :span="2">
              <span :style="{ color: getScoreColor(detailData.innovation) }">{{ detailData.innovation }}分</span>
            </t-descriptions-item>
          </t-descriptions>
        </div>
      </template>
    </t-dialog>

    <!-- 评分弹窗 -->
    <t-dialog
      v-model:visible="assessDialogVisible"
      title="员工评分"
      width="580px"
      :footer="false"
      placement="center"
      :destroy-on-close="true"
      :close-on-overlay-click="true"
    >
      <t-form
        ref="assessFormRef"
        :model="assessFormData"
        label-width="100px"
        layout="vertical"
        class="assess-form"
      >
        <div class="form-row">
          <t-form-item label="工号" class="form-item-half">
            <t-input :model-value="assessFormData.employeeNo" disabled />
          </t-form-item>
          <t-form-item label="姓名" class="form-item-half">
            <t-input :model-value="assessFormData.name" disabled />
          </t-form-item>
        </div>
        <div class="score-row">
          <t-form-item label="工作态度" name="workAttitude" class="score-item">
            <template #label>
              <span>工作态度<span class="weight-tag">权重20%</span></span>
            </template>
            <t-input-number v-model="assessFormData.workAttitude" :min="0" :max="100" :step="1" style="width: 100%" />
          </t-form-item>
          <t-form-item label="工作质量" name="workQuality" class="score-item">
            <template #label>
              <span>工作质量<span class="weight-tag">权重30%</span></span>
            </template>
            <t-input-number v-model="assessFormData.workQuality" :min="0" :max="100" :step="1" style="width: 100%" />
          </t-form-item>
        </div>
        <div class="score-row">
          <t-form-item label="工作效率" name="workEfficiency" class="score-item">
            <template #label>
              <span>工作效率<span class="weight-tag">权重25%</span></span>
            </template>
            <t-input-number v-model="assessFormData.workEfficiency" :min="0" :max="100" :step="1" style="width: 100%" />
          </t-form-item>
          <t-form-item label="团队协作" name="teamwork" class="score-item">
            <template #label>
              <span>团队协作<span class="weight-tag">权重15%</span></span>
            </template>
            <t-input-number v-model="assessFormData.teamwork" :min="0" :max="100" :step="1" style="width: 100%" />
          </t-form-item>
        </div>
        <t-form-item label="创新能力" name="innovation">
          <template #label>
            <span>创新能力<span class="weight-tag">权重10%</span></span>
          </template>
          <t-input-number v-model="assessFormData.innovation" :min="0" :max="100" :step="1" style="width: 100%" />
        </t-form-item>
        <t-form-item label="综合得分" class="total-score-item">
          <span class="total-score">{{ calculateScore }}分</span>
        </t-form-item>
        <div class="form-footer">
          <t-space>
            <t-button theme="primary" :loading="assessLoading" @click="handleAssess">提交评分</t-button>
            <t-button variant="outline" @click="assessDialogVisible = false">取消</t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>
  </div>
</template>

<style scoped>
.performance-page {
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

.stat-mini :deep(.t-card__body) {
  padding: 16px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #000;
  margin: 0;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin: 0;
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

.score-details {
  margin-top: 24px;
}

.score-details h4 {
  font-size: 14px;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e7e7e7;
}

.total-score {
  font-size: 24px;
  font-weight: 700;
  color: #0052d9;
}

/* 评分表单样式 */
.assess-form {
  padding: 8px 0;
}

/* 输入框统一样式 - 固定宽度保持一致 */
.assess-form .t-input,
.assess-form .t-input-number,
.assess-form .t-select {
  width: 160px !important;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-item-half {
  flex: 1;
  min-width: 0;
}

.score-row {
  display: flex;
  gap: 16px;
}

.score-item {
  flex: 1;
  min-width: 0;
}

.weight-tag {
  font-size: 11px;
  color: #888;
  font-weight: normal;
  margin-left: 4px;
}

.total-score-item {
  margin-bottom: 0;
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
