<script setup lang="ts">
import { ref, onMounted, computed, h, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store'
import * as dashboardApi from '@/api/modules/dashboard'
import * as employeeApi from '@/api/modules/employee'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

const router = useRouter()

// 内联 SVG 图标
const IconUser = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z' })
])
const IconTime = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z' })
])
const IconCheck = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z' })
])
const IconList = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z' })
])
const IconUserAdd = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z' })
])
const IconDoc = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z' })
])
const IconTrend = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z' })
])
const IconPoll = () => h('svg', { viewBox: '0 0 24 24', fill: 'currentColor', width: '1em', height: '1em' }, [
  h('path', { d: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z' })
])

const userStore = useUserStore()

// 统计数据
const stats = ref({
  totalEmployees: 0,
  todayAttendance: 0,
  pendingLeaves: 0,
  activeVotes: 0
})

// 图表实例
const employeeChartRef = ref<HTMLDivElement>()
const departmentChartRef = ref<HTMLDivElement>()
let employeeChart: echarts.ECharts | null = null
let departmentChart: echarts.ECharts | null = null

// 模拟数据（后端接口未就绪时使用）
const mockData = {
  employeeTrend: [
    { month: '2025-11', count: 245 },
    { month: '2025-12', count: 258 },
    { month: '2026-01', count: 267 },
    { month: '2026-02', count: 275 },
    { month: '2026-03', count: 289 },
    { month: '2026-04', count: 302 }
  ],
  departmentDistribution: [
    { name: '生产部', value: 120 },
    { name: '研发部', value: 45 },
    { name: '品质部', value: 35 },
    { name: '采购部', value: 15 },
    { name: '仓储部', value: 25 },
    { name: '财务部', value: 12 },
    { name: '人事部', value: 8 },
    { name: '行政部', value: 10 }
  ],
  recentEmployees: [
    { name: '张伟', department: 'SMT车间', position: '操作工', entryDate: '2026-04-02' },
    { name: '李娜', department: '贴合车间', position: '技术员', entryDate: '2026-04-01' },
    { name: '王强', department: '研发部', position: '工程师', entryDate: '2026-03-28' },
    { name: '刘芳', department: '品质部', position: '质检员', entryDate: '2026-03-25' },
    { name: '陈军', department: '组装车间', position: '班组长', entryDate: '2026-03-20' }
  ],
  expiringContracts: [
    { name: '赵敏', department: '研发部', contractEnd: '2026-04-15', daysLeft: 9 },
    { name: '钱伟', department: 'SMT车间', contractEnd: '2026-04-20', daysLeft: 14 },
    { name: '孙丽', department: '品质部', contractEnd: '2026-04-25', daysLeft: 19 },
    { name: '周杰', department: '贴合车间', contractEnd: '2026-05-01', daysLeft: 25 }
  ],
  todoItems: [
    { id: 1, title: '审批张伟的请假申请', type: 'leave', priority: 'high' },
    { id: 2, title: '处理李娜的加班申请', type: 'overtime', priority: 'medium' },
    { id: 3, title: '更新4月份考勤数据', type: 'attendance', priority: 'low' }
  ]
}

// 统计数据
const fetchStats = async () => {
  try {
    const res = await dashboardApi.getDashboardStats()
    if (res && typeof res === 'object') {
      stats.value = {
        totalEmployees: res.totalEmployees || 0,
        todayAttendance: res.todayAttendance || 0,
        pendingLeaves: res.pendingLeaves || 0,
        activeVotes: res.activeVotes || 0
      }
    }
  } catch (e) {
    // 使用模拟数据
    stats.value = {
      totalEmployees: 302,
      todayAttendance: 285,
      pendingLeaves: 8,
      activeVotes: 2
    }
  }
}

// 初始化员工趋势图表
const initEmployeeChart = () => {
  if (!employeeChartRef.value) return
  
  employeeChart = echarts.init(employeeChartRef.value)
  const data = mockData.employeeTrend
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(d => d.month),
      axisLabel: {
        color: '#666',
        formatter: (value: string) => dayjs(value).format('MM月')
      }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLabel: { color: '#666' }
    },
    series: [{
      name: '员工人数',
      type: 'bar',
      data: data.map(d => d.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#0052d9' },
          { offset: 1, color: '#6fb1fc' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%'
    }]
  }
  
  employeeChart.setOption(option)
}

// 初始化部门分布图表
const initDepartmentChart = () => {
  if (!departmentChartRef.value) return
  
  departmentChart = echarts.init(departmentChartRef.value)
  const data = mockData.departmentDistribution
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#666' }
    },
    series: [{
      name: '部门分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      data: data.map((d, i) => ({
        value: d.value,
        name: d.name,
        itemStyle: {
          color: ['#0052d9', '#4364f7', '#6fb1fc', '#8e9cff', '#a8b1ff', '#c3c8ff', '#d8dcff', '#ebefff'][i]
        }
      }))
    }]
  }
  
  departmentChart.setOption(option)
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  employeeChart?.resize()
  departmentChart?.resize()
}

// 跳转到投票页面
const goToVote = () => {
  router.push('/vote')
}

// 跳转到审批页面
const goToApproval = () => {
  router.push('/attendance')
}

// 跳转到考勤/请假管理
const goToAttendance = () => {
  router.push('/attendance')
}

// 跳转到员工管理
const goToEmployee = () => {
  router.push('/employee')
}

onMounted(() => {
  fetchStats()
  // 使用 nextTick 确保 DOM 完全渲染后再初始化图表
  nextTick(() => {
    initEmployeeChart()
    initDepartmentChart()
  })
  window.addEventListener('resize', handleResize)
})
</script>

<template>
  <div class="dashboard-page">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">下午好，{{ userStore.userName || '管理员' }}</h1>
        <p class="welcome-desc">今天是 {{ dayjs().format('YYYY年MM月DD日 dddd') }}，祝您工作顺利！</p>
      </div>
      <div class="quick-actions">
        <t-button theme="primary" variant="outline" @click="goToApproval">
          <template #icon>
            <IconCheck />
          </template>
          待办审批
        </t-button>
        <t-button theme="warning" variant="outline" @click="goToVote">
          <template #icon>
            <IconPoll />
          </template>
          进行投票
        </t-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card stat-employee">
        <div class="stat-icon">
          <IconUser />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalEmployees }}</div>
          <div class="stat-label">员工总数</div>
        </div>
        <div class="stat-trend trend-up">
          <IconTrend />
          <span>+12</span>
        </div>
      </div>

      <div class="stat-card stat-attendance">
        <div class="stat-icon">
          <IconCheck />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayAttendance }}</div>
          <div class="stat-label">今日考勤</div>
        </div>
        <div class="stat-percent">
          <span>{{ stats.totalEmployees > 0 ? Math.round(stats.todayAttendance / stats.totalEmployees * 100) : 0 }}%</span>
        </div>
      </div>

      <div class="stat-card stat-leave">
        <div class="stat-icon">
          <IconTime />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingLeaves }}</div>
          <div class="stat-label">待审批假单</div>
        </div>
        <t-tag theme="warning" variant="light">待处理</t-tag>
      </div>

      <div class="stat-card stat-vote">
        <div class="stat-icon">
          <IconPoll />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.activeVotes }}</div>
          <div class="stat-label">进行中投票</div>
        </div>
        <t-tag theme="success" variant="light">进行中</t-tag>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">员工趋势</h3>
          <span class="chart-subtitle">近6个月员工人数变化</span>
        </div>
        <div ref="employeeChartRef" class="chart-container"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">部门人员分布</h3>
          <span class="chart-subtitle">各部门人员占比</span>
        </div>
        <div ref="departmentChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 底部数据区域 -->
    <div class="bottom-grid">
      <!-- 待办事项 -->
      <div class="data-card">
        <div class="card-header">
          <h3 class="card-title">
            <IconList />
            待办事项
          </h3>
          <t-button variant="text" size="small" @click="goToAttendance">查看更多</t-button>
        </div>
        <t-list :split="true">
          <t-list-item v-for="item in mockData.todoItems" :key="item.id">
            <div class="todo-item">
              <t-tag :theme="item.priority === 'high' ? 'danger' : item.priority === 'medium' ? 'warning' : 'default'" size="small">
                {{ item.priority === 'high' ? '紧急' : item.priority === 'medium' ? '一般' : '低' }}
              </t-tag>
              <span class="todo-title">{{ item.title }}</span>
              <t-button variant="text" size="small">处理</t-button>
            </div>
          </t-list-item>
        </t-list>
      </div>

      <!-- 最近入职 -->
      <div class="data-card">
        <div class="card-header">
          <h3 class="card-title">
            <IconUserAdd />
            最近入职
          </h3>
          <t-button variant="text" size="small" @click="router.push('/employee')">查看更多</t-button>
        </div>
        <t-list :split="true">
          <t-list-item v-for="emp in mockData.recentEmployees" :key="emp.name">
            <div class="employee-item">
              <t-avatar size="small">{{ emp.name.slice(0, 1) }}</t-avatar>
              <div class="employee-info">
                <span class="employee-name">{{ emp.name }}</span>
                <span class="employee-dept">{{ emp.department }} · {{ emp.position }}</span>
              </div>
              <span class="entry-date">{{ dayjs(emp.entryDate).format('MM/DD') }}</span>
            </div>
          </t-list-item>
        </t-list>
      </div>

      <!-- 合同到期提醒 -->
      <div class="data-card">
        <div class="card-header">
          <h3 class="card-title">
            <IconDoc />
            合同到期提醒
          </h3>
          <t-button variant="text" size="small" @click="goToEmployee">查看更多</t-button>
        </div>
        <t-list :split="true">
          <t-list-item v-for="contract in mockData.expiringContracts" :key="contract.name">
            <div class="contract-item">
              <div class="contract-info">
                <span class="contract-name">{{ contract.name }}</span>
                <span class="contract-dept">{{ contract.department }}</span>
              </div>
              <div class="contract-deadline">
                <span class="deadline-date">{{ dayjs(contract.contractEnd).format('MM/DD') }}</span>
                <t-tag :theme="contract.daysLeft <= 10 ? 'danger' : 'warning'" size="small">
                  {{ contract.daysLeft }}天
                </t-tag>
              </div>
            </div>
          </t-list-item>
        </t-list>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: 0;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #0052d9 0%, #4364f7 100%);
  border-radius: 12px;
  margin-bottom: 24px;
  color: #fff;
}

.welcome-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
}

.welcome-desc {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-employee .stat-icon {
  background: rgba(0, 82, 217, 0.1);
  color: #0052d9;
}

.stat-attendance .stat-icon {
  background: rgba(0, 165, 92, 0.1);
  color: #00a55c;
}

.stat-leave .stat-icon {
  background: rgba(228, 138, 0, 0.1);
  color: #e48a00;
}

.stat-vote .stat-icon {
  background: rgba(77, 144, 142, 0.1);
  color: #4d908e;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
}

.trend-up {
  background: rgba(0, 165, 92, 0.1);
  color: #00a55c;
}

.stat-percent {
  font-size: 14px;
  color: #00a55c;
  font-weight: 600;
}

/* 图表区域 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.chart-subtitle {
  font-size: 12px;
  color: #999;
}

.chart-container {
  height: 280px;
  width: 100%;
}

/* 底部数据区域 */
.bottom-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.data-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 待办事项 */
.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.todo-title {
  flex: 1;
  font-size: 14px;
  color: #333;
}

/* 员工列表 */
.employee-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.employee-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.employee-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.employee-dept {
  font-size: 12px;
  color: #999;
}

.entry-date {
  font-size: 12px;
  color: #999;
}

/* 合同列表 */
.contract-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.contract-info {
  display: flex;
  flex-direction: column;
}

.contract-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.contract-dept {
  font-size: 12px;
  color: #999;
}

.contract-deadline {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.deadline-date {
  font-size: 12px;
  color: #999;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .quick-actions {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
