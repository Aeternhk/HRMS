<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import * as salaryApi from '@/api/modules/salary'
import { departmentOptions as allDeptOptions, getTopDepartmentId } from '@/utils/deptData'

// 薪资数据类型（对齐后端返回）
interface SalaryRecord {
  id: number
  userId?: number
  employeeNo: string
  name: string
  department: string
  departmentId: number
  month: string
  baseSalary: number
  positionAllowance: number
  performance: number
  overtimePay: number
  bonus: number
  mealAllowance: number
  transportAllowance: number
  socialSecurity: number
  housingFund: number
  otherDeduction: number
  netSalary: number
  status: number   // 0=未发放 1=已发放
  statusText: string
  grantDate?: string
  createTime?: string
}

// 查询参数
const queryParams = ref({
  keyword: '',
  departmentId: undefined as number | undefined,
  month: dayjs().format('YYYY-MM'),
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

// 薪资数据（从API获取）
const loading = ref(false)
const salaryData = ref<SalaryRecord[]>([])
const total = ref(0)

// 部门选项（使用统一数据源）
const departmentOptions = allDeptOptions

// 状态选项（对齐后端：0=未发放 1=已发放）
const statusOptions = [
  { value: 0, label: '待发放' },
  { value: 1, label: '已发放' }
]

// 表格列配置
const columns = [
  { colKey: 'employeeNo', title: '工号', width: '100' },
  { colKey: 'name', title: '姓名', width: '80' },
  { colKey: 'department', title: '部门', width: '100' },
  { colKey: 'baseSalary', title: '基本工资', width: '100' },
  { colKey: 'positionAllowance', title: '岗位津贴', width: '90' },
  { colKey: 'performance', title: '绩效工资', width: '100' },
  { colKey: 'overtimePay', title: '加班费', width: '80' },
  { colKey: 'otherDeduction', title: '扣款', width: '70' },
  { colKey: 'netSalary', title: '实发工资', width: '110' },
  { colKey: 'status', title: '发放状态', width: '90' },
  { colKey: 'action', title: '操作', width: '160', align: 'center' }
]

// 统计数据（基于当前页数据）
const stats = computed(() => {
  const data = salaryData.value
  const paid = data.filter(r => r.status === 1)
  const pending = data.filter(r => r.status === 0)
  return {
    totalAmount: data.reduce((sum, r) => sum + (r.netSalary || 0), 0),
    paidAmount: paid.reduce((sum, r) => sum + (r.netSalary || 0), 0),
    pendingAmount: pending.reduce((sum, r) => sum + (r.netSalary || 0), 0),
    totalCount: data.length,
    paidCount: paid.length
  }
})

// 加载薪资数据
const fetchSalaries = async () => {
  loading.value = true
  try {
    const res: any = await salaryApi.getSalaryPage({
      keyword: queryParams.value.keyword || undefined,
      departmentId: queryParams.value.departmentId,
      month: queryParams.value.month || undefined,
      status: queryParams.value.status,
      page: queryParams.value.page,
      pageSize: queryParams.value.pageSize
    })
    if (res && res.records) {
      salaryData.value = res.records
      total.value = res.total || 0
    }
  } catch (e) {
    console.error('加载薪资列表失败:', e)
    salaryData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 首次加载
fetchSalaries()

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1
  fetchSalaries()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    keyword: '',
    departmentId: undefined,
    month: dayjs().format('YYYY-MM'),
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
  fetchSalaries()
}

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref<SalaryRecord | null>(null)

const openDetailDialog = async (row: SalaryRecord) => {
  try {
    const res: any = await salaryApi.getSalaryDetail(row.id)
    detailData.value = res
  } catch (e) {
    detailData.value = row
  }
  detailDialogVisible.value = true
}

// 工资条弹窗
const payslipDialogVisible = ref(false)
const payslipData = ref<SalaryRecord | null>(null)

const openPayslipDialog = (row: SalaryRecord) => {
  payslipData.value = row
  payslipDialogVisible.value = true
}

// 工资核算
const handleCalculate = () => {
  MessagePlugin.info('正在核算本月工资...')
}

// 导出
const handleExport = () => {
  MessagePlugin.info('正在导出工资数据...')
}

// 删除
const handleDelete = (row: SalaryRecord) => {
  const dialog = DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除 ${row.month} 月 ${row.name} 的工资记录吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        await salaryApi.deleteSalary(row.id)
        total.value--
        MessagePlugin.success('删除成功')
        dialog.destroy()
        fetchSalaries() // 刷新列表
      } catch (e: any) {
        MessagePlugin.error(e.message || '删除失败')
        dialog.destroy()
      }
    }
  })
}
</script>

<template>
  <div class="salary-page">
    <t-card title="薪资管理">
      <template #actions>
        <t-space>
          <t-button theme="primary" @click="handleCalculate">
            <template #icon>
              <t-icon name="calculator" />
            </template>
            工资核算
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
          <t-form-item label="发放月份">
            <t-date-picker
              v-model="queryParams.month"
              format="YYYY-MM"
              value-format="YYYY-MM"
              mode="month"
              allow-input
              clearable
            />
          </t-form-item>
          <t-form-item label="发放状态">
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
              <t-icon name="money-circle" style="color: #0052d9; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">¥ {{ stats.totalAmount.toLocaleString() }}</p>
                <p class="stat-label">本月工资总额</p>
              </div>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-content">
              <t-icon name="user-1" style="color: #00a870; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">{{ stats.totalCount }}人</p>
                <p class="stat-label">本月发放人数</p>
              </div>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-content">
              <t-icon name="chart-bar" style="color: #ff7a00; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">¥ {{ Math.round(stats.totalAmount / stats.totalCount).toLocaleString() }}</p>
                <p class="stat-label">人均工资</p>
              </div>
            </div>
          </t-card>
        </t-col>
        <t-col :xs="12" :sm="6">
          <t-card class="stat-mini">
            <div class="stat-content">
              <t-icon name="check-circle" style="color: #9747ff; font-size: 24px" />
              <div class="stat-info">
                <p class="stat-value">{{ stats.paidCount }}/{{ stats.totalCount }}</p>
                <p class="stat-label">已发放/总人数</p>
              </div>
            </div>
          </t-card>
        </t-col>
      </t-row>

      <!-- 表格 -->
      <t-table
        :data="salaryData"
        :columns="columns"
        row-key="id"
        :hover="true"
        :loading="loading"
        :stripe="true"
      >
        <!-- 基本工资 -->
        <template #baseSalary="{ row }">
          ¥ {{ row.baseSalary.toLocaleString() }}
        </template>
        <!-- 岗位津贴 -->
        <template #positionAllowance="{ row }">
          ¥ {{ row.positionAllowance.toLocaleString() }}
        </template>
        <!-- 绩效工资 -->
        <template #performance="{ row }">
          ¥ {{ row.performance.toLocaleString() }}
        </template>
        <!-- 加班费 -->
        <template #overtimePay="{ row }">
          {{ row.overtimePay > 0 ? `¥ ${row.overtimePay}` : '-' }}
        </template>
        <!-- 扣款 -->
        <template #otherDeduction="{ row }">
          {{ row.otherDeduction > 0 ? `¥ ${row.otherDeduction}` : '-' }}
        </template>
        <!-- 实发工资 -->
        <template #netSalary="{ row }">
          <span style="color: #00a870; font-weight: 600">¥ {{ row.netSalary.toLocaleString() }}</span>
        </template>
        <!-- 发放状态 -->
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'warning'" size="small">
            {{ row.status === 1 ? '已发放' : '待发放' }}
          </t-tag>
        </template>
        <!-- 操作 -->
        <template #action="{ row }">
          <t-space>
            <t-button variant="text" size="small" @click="openDetailDialog(row)">详情</t-button>
            <t-button variant="text" size="small" @click="openPayslipDialog(row)">工资条</t-button>
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

    <!-- 详情弹窗 -->
    <t-dialog
      v-model:visible="detailDialogVisible"
      title="工资详情"
      width="600px"
      :footer="false"
    >
      <t-descriptions v-if="detailData" :column="2" bordered>
        <t-descriptions-item label="工号">{{ detailData.employeeNo }}</t-descriptions-item>
        <t-descriptions-item label="姓名">{{ detailData.name }}</t-descriptions-item>
        <t-descriptions-item label="部门">{{ detailData.department }}</t-descriptions-item>
        <t-descriptions-item label="发放月份">{{ detailData.month }}</t-descriptions-item>
        <t-descriptions-item label="基本工资">¥ {{ detailData.baseSalary.toLocaleString() }}</t-descriptions-item>
        <t-descriptions-item label="岗位津贴">¥ {{ detailData.positionAllowance.toLocaleString() }}</t-descriptions-item>
        <t-descriptions-item label="绩效工资">¥ {{ detailData.performance.toLocaleString() }}</t-descriptions-item>
        <t-descriptions-item label="加班费">¥ {{ detailData.overtimePay?.toLocaleString() || 0 }}</t-descriptions-item>
        <t-descriptions-item label="奖金">¥ {{ (detailData.bonus || 0).toLocaleString() }}</t-descriptions-item>
        <t-descriptions-item label="扣款">¥ {{ (detailData.otherDeduction || 0).toLocaleString() }}</t-descriptions-item>
        <t-descriptions-item label="社保">¥ {{ detailData.socialSecurity }}</t-descriptions-item>
        <t-descriptions-item label="公积金">¥ {{ detailData.housingFund }}</t-descriptions-item>
        <t-descriptions-item label="实发工资">
          <span style="color: #00a870; font-weight: 600; font-size: 16px">
            ¥ {{ detailData.netSalary.toLocaleString() }}
          </span>
        </t-descriptions-item>
        <t-descriptions-item label="发放状态">
          <t-tag :theme="detailData.status === 1 ? 'success' : 'warning'">
            {{ detailData.statusText || (detailData.status === 1 ? '已发放' : '待发放') }}
          </t-tag>
        </t-descriptions-item>
        <t-descriptions-item v-if="detailData.grantDate" label="发放日期" :span="2">
          {{ detailData.grantDate }}
        </t-descriptions-item>
      </t-descriptions>
    </t-dialog>

    <!-- 工资条弹窗 -->
    <t-dialog
      v-model:visible="payslipDialogVisible"
      title="员工工资条"
      width="480px"
      :footer="false"
      placement="center"
    >
      <div v-if="payslipData" class="payslip">
        <div class="payslip-header">
          <h3>工资条</h3>
          <p>{{ payslipData.month }}月</p>
        </div>
        <div class="payslip-section">
          <h4>基本信息</h4>
          <t-descriptions :column="2" size="small">
            <t-descriptions-item label="工号">{{ payslipData.employeeNo }}</t-descriptions-item>
            <t-descriptions-item label="姓名">{{ payslipData.name }}</t-descriptions-item>
            <t-descriptions-item label="部门">{{ payslipData.department }}</t-descriptions-item>
          </t-descriptions>
        </div>
        <div class="payslip-section">
          <h4>应发项目</h4>
          <div class="payslip-row">
            <span>基本工资</span>
            <span>¥ {{ payslipData.baseSalary.toLocaleString() }}</span>
          </div>
          <div class="payslip-row">
            <span>岗位津贴</span>
            <span>¥ {{ payslipData.positionAllowance.toLocaleString() }}</span>
          </div>
          <div class="payslip-row">
            <span>绩效工资</span>
            <span>¥ {{ payslipData.performance.toLocaleString() }}</span>
          </div>
          <div class="payslip-row">
            <span>加班费</span>
            <span>¥ {{ (payslipData.overtimePay || 0).toLocaleString() }}</span>
          </div>
          <div class="payslip-row">
            <span>奖金</span>
            <span>¥ {{ (payslipData.bonus || 0).toLocaleString() }}</span>
          </div>
          <div class="payslip-row total">
            <span>应发合计</span>
            <span>¥ {{ (payslipData.baseSalary + payslipData.positionAllowance + payslipData.performance + (payslipData.overtimePay || 0) + (payslipData.bonus || 0)).toLocaleString() }}</span>
          </div>
        </div>
        <div class="payslip-section">
          <h4>扣除项目</h4>
          <div class="payslip-row">
            <span>社保</span>
            <span>-¥ {{ payslipData.socialSecurity }}</span>
          </div>
          <div class="payslip-row">
            <span>公积金</span>
            <span>-¥ {{ payslipData.housingFund }}</span>
          </div>
          <div class="payslip-row">
            <span>其他扣款</span>
            <span>-¥ {{ (payslipData.otherDeduction || 0) }}</span>
          </div>
          <div class="payslip-row total">
            <span>扣除合计</span>
            <span>-¥ {{ (payslipData.socialSecurity + payslipData.housingFund + (payslipData.otherDeduction || 0)) }}</span>
          </div>
        </div>
        <div class="payslip-footer">
          <div class="net-salary">
            <span>实发工资</span>
            <span class="amount">¥ {{ payslipData.netSalary.toLocaleString() }}</span>
          </div>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.salary-page {
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

/* 工资条样式 */
.payslip {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
}

.payslip-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e7e7e7;
}

.payslip-header h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
}

.payslip-header p {
  margin: 0;
  color: #666;
}

.payslip-section {
  margin-bottom: 16px;
}

.payslip-section h4 {
  font-size: 14px;
  color: #333;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px dashed #e7e7e7;
}

.payslip-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
}

.payslip-row.total {
  font-weight: 600;
  padding-top: 12px;
  margin-top: 8px;
  border-top: 1px solid #dcdcdc;
}

.payslip-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 2px solid #0052d9;
}

.net-salary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.net-salary .amount {
  font-size: 24px;
  color: #00a870;
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
