<script setup lang="ts">
import { ref, onMounted } from 'vue'

const activeTab = ref('overview')

// 模拟数据
const overviewStats = ref([
  { title: '总员工数', value: 256, icon: 'user', color: '#0052d9', change: '+5', trend: 'up' },
  { title: '本月入职', value: 18, icon: 'login', color: '#00a870', change: '+3', trend: 'up' },
  { title: '本月离职', value: 3, icon: 'logout', color: '#e34d59', change: '-1', trend: 'down' },
  { title: '平均薪资', value: 9680, icon: 'wallet', color: '#e37318', change: '+200', trend: 'up', prefix: '¥' },
  { title: '出勤率', value: 96.5, icon: 'check-circle', color: '#0594fa', change: '+0.3', trend: 'up', suffix: '%' },
  { title: '待审批', value: 12, icon: 'time', color: '#ed7b2f', change: '-4', trend: 'down' }
])

// 部门人数分布
const deptData = ref([
  { name: '生产部', count: 120, percentage: 46.9 },
  { name: '研发部', count: 35, percentage: 13.7 },
  { name: '品质部', count: 28, percentage: 10.9 },
  { name: '仓储部', count: 25, percentage: 9.8 },
  { name: '采购部', count: 15, percentage: 5.9 },
  { name: '财务部', count: 12, percentage: 4.7 },
  { name: '人事部', count: 10, percentage: 3.9 },
  { name: '行政部', count: 11, percentage: 4.3 }
])

// 员工状态分布
const statusData = ref([
  { name: '正式员工', count: 210, color: '#0052d9' },
  { name: '试用期', count: 28, color: '#e37318' },
  { name: '实习', count: 15, color: '#00a870' },
  { name: '离职', count: 3, color: '#e34d59' }
])

// 薪资区间分布
const salaryData = ref([
  { range: '5000以下', count: 35, color: '#95a5b6' },
  { range: '5000-8000', count: 85, color: '#748ffc' },
  { range: '8000-12000', count: 90, color: '#4c6ef5' },
  { range: '12000-18000', count: 35, color: '#364fc7' },
  { range: '18000以上', count: 11, color: '#1c3879' }
])

// 入离职趋势（近6个月）
const trendData = ref([
  { month: '2025-11', entry: 12, leave: 5 },
  { month: '2025-12', entry: 15, leave: 3 },
  { month: '2026-01', entry: 8, leave: 6 },
  { month: '2026-02', entry: 20, leave: 4 },
  { month: '2026-03', entry: 14, leave: 2 },
  { month: '2026-04', entry: 18, leave: 3 }
])

// 考勤统计（当月）
const attendanceStats = ref({
  totalDays: 22,
  normalRate: 92.5,
  lateRate: 3.2,
  absentRate: 1.8,
  leaveRate: 2.5,
  details: [
    { date: '04-01', status: '正常' }, { date: '04-02', status: '正常' }, { date: '04-03', status: '正常' },
    { date: '04-07', status: '迟到' }, { date: '04-08', status: '正常' }, { date: '04-09', status: '请假' }
  ]
})

// 绩效等级分布
const perfData = ref([
  { grade: 'S(卓越)', count: 8, color: '#e34d59' },
  { grade: 'A(优秀)', count: 35, color: '#e37318' },
  { grade: 'B(良好)', count: 120, color: '#00a870' },
  { grade: 'C(合格)', count: 75, color: '#0052d9' },
  { grade: 'D(需改进)', count: 18, color: '#95a5b6' }
])
</script>

<template>
  <div class="report-page">
    <!-- 总览统计 -->
    <div class="stats-grid">
      <t-card v-for="stat in overviewStats" :key="stat.title" :bordered="false" class="stat-card">
        <div class="stat-icon" :style="{ background: stat.color + '15', color: stat.color }">●</div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.prefix || '' }}{{ stat.value }}{{ stat.suffix || '' }}</div>
          <div class="stat-title">{{ stat.title }}</div>
          <div class="stat-change" :class="stat.trend === 'up' ? 'up' : 'down'">{{ stat.change }} 较上月</div>
        </div>
      </t-card>
    </div>

    <t-tabs v-model:value="activeTab">
      <!-- 人员分析 -->
      <t-tab-panel value="people" label="人员结构分析">
        <div class="chart-row">
          <t-card title="部门人数分布" :bordered="false" class="chart-card half">
            <div class="bar-chart">
              <div v-for="item in deptData" :key="item.name" class="bar-item">
                <div class="bar-label">{{ item.name }}</div>
                <div class="bar-track"><div class="bar-fill" :style="{ width: item.percentage + '%' }"></div></div>
                <div class="bar-value">{{ item.count }}人</div>
              </div>
            </div>
          </t-card>
          <t-card title="员工状态分布" :bordered="false" class="chart-card half">
            <div class="pie-list">
              <div v-for="item in statusData" :key="item.name" class="pie-item">
                <span class="pie-dot" :style="{ background: item.color }"></span>
                <span class="pie-label">{{ item.name }}</span>
                <span class="pie-count">{{ item.count }}人</span>
              </div>
            </div>
            <div class="mini-pie-chart" style="margin-top:16px;">
              <svg viewBox="0 0 120 120" width="160" height="160">
                <circle cx="60" cy="60" r="50" fill="none" stroke="#f0f0f0" stroke-width="20"/>
                <circle cx="60" cy="60" r="50" fill="none" stroke="#0052d9" stroke-width="20"
                  stroke-dasharray="210 104" stroke-dashoffset="0" transform="rotate(-90 60 60)"/>
                <circle cx="60" cy="60" r="50" fill="none" stroke="#e37318" stroke-width="20"
                  stroke-dasharray="28 286" stroke-dashoffset="-210" transform="rotate(-90 60 60)"/>
                <circle cx="60" cy="60" r="50" fill="none" stroke="#00a870" stroke-width="20"
                  stroke-dasharray="15 299" stroke-dashoffset="-238" transform="rotate(-90 60 60)"/>
                <circle cx="60" cy="60" r="50" fill="none" stroke="#e34d59" stroke-width="20"
                  stroke-dasharray="3 311" stroke-dashoffset="-253" transform="rotate(-90 60 60)"/>
              </svg>
            </div>
          </t-card>
        </div>
      </t-tab-panel>

      <!-- 薪资分析 -->
      <t-tab-panel value="salary" label="薪资成本分析">
        <div class="chart-row">
          <t-card title="薪资区间分布" :bordered="false" class="chart-card half">
            <div class="bar-chart horizontal">
              <div v-for="item in salaryData" :key="item.range" class="h-bar-item">
                <div class="h-bar-label">{{ item.range }}</div>
                <div class="h-bar-track"><div class="h-bar-fill" :style="{ width: (item.count / 90 * 100) + '%', background: item.color }"></div></div>
                <div class="h-bar-value">{{ item.count }}人</div>
              </div>
            </div>
          </t-card>
          <t-card title="各部门平均薪资" :bordered="false" class="chart-card half">
            <table style="width:100%;font-size:14px;">
              <thead><tr><th>部门</th><th>人数</th><th>均薪</th><th>中位数</th></tr></thead>
              <tbody>
                <tr><td>研发部</td><td>35</td><td>¥15,600</td><td>¥14,500</td></tr>
                <tr><td>生产部</td><td>120</td><td>¥7,200</td><td>¥6,800</td></tr>
                <tr><td>品质部</td><td>28</td><td>¥7,500</td><td>¥7,200</td></tr>
                <tr><td>财务部</td><td>12</td><td>¥10,000</td><td>¥9,500</td></tr>
                <tr><td>人事部</td><td>10</td><td>¥8,500</td><td>¥8,000</td></tr>
                <tr><td>仓储部</td><td>25</td><td>¥6,500</td><td>¥6,200</td></tr>
              </tbody>
            </table>
          </t-card>
        </div>
      </t-tab-panel>

      <!-- 入离职趋势 -->
      <t-tab-panel value="turnover" label="入离职趋势">
        <t-card title="近6个月入离职趋势" :bordered="false" class="full-card">
          <div class="trend-chart">
            <div class="trend-y-axis">
              <span>25</span><span>20</span><span>15</span><span>10</span><span>5</span><span>0</span>
            </div>
            <div class="trend-body">
              <div v-for="item in trendData" :key="item.month" class="trend-col">
                <div class="trend-entry-bar" :style="{ height: (item.entry / 25 * 140) + 'px' }"></div>
                <div class="trend-leave-bar" :style="{ height: (item.leave / 25 * 140) + 'px' }"></div>
                <div class="trend-x-label">{{ item.month.slice(5) }}</div>
              </div>
            </div>
            <div class="trend-legend"><span class="legend-entry"></span>入职&nbsp;&nbsp;<span class="legend-leave"></span>离职</div>
          </div>
        </t-card>
      </t-tab-panel>

      <!-- 考勤统计 -->
      <t-tab-panel value="attendance" label="考勤统计分析">
        <div class="chart-row">
          <t-card title="当月考勤概况" :bordered="false" class="chart-card third">
            <div class="att-stats">
              <div class="att-stat-item"><div class="att-val att-green">{{ attendanceStats.normalRate }}%</div><div class="att-lbl">正常率</div></div>
              <div class="att-stat-item"><div class="att-val att-orange">{{ attendanceStats.lateRate }}%</div><div class="att-lbl">迟到率</div></div>
              <div class="att-stat-item"><div class="att-val att-red">{{ attendanceStats.absentRate }}%</div><div class="att-lbl">缺卡率</div></div>
              <div class="att-stat-item"><div class="att-val att-blue">{{ attendanceStats.leaveRate }}%</div><div class="att-lbl">请假率</div></div>
            </div>
          </t-card>
          <t-card title="考勤异常TOP5" :bordered="false" class="chart-card two-third">
            <t-table size="small" :data="[
              { rank: 1, name: '王五', dept: 'SMT车间', late: 4, absent: 2, leave: 1 },
              { rank: 2, name: '赵六', dept: '品质部', late: 3, absent: 1, leave: 2 },
              { rank: 3, name: '张三', dept: '生产部', late: 2, absent: 1, leave: 0 },
              { rank: 4, name: '钱七', dept: '生产部', late: 2, absent: 0, leave: 3 },
              { rank: 5, name: '李四', dept: '人事部', late: 1, absent: 0, leave: 1 }
            ]" :columns="[
              { colKey: 'rank', title: '#', width: 45, align: 'center' },
              { colKey: 'name', title: '姓名', width: 80 },
              { colKey: 'dept', title: '部门', width: 80 },
              { colKey: 'late', title: '迟到', width: 55, align: 'center' },
              { colKey: 'absent', title: '缺卡', width: 55, align: 'center' },
              { colKey: 'leave', title: '请假', width: 55, align: 'center' }
            ]" stripe />
          </t-card>
        </div>
      </t-tab-panel>

      <!-- 绩效分布 -->
      <t-tab-panel value="performance" label="绩效等级分布">
        <div class="chart-row">
          <t-card title="绩效等级分布" :bordered="false" class="chart-card half">
            <div class="perf-bars">
              <div v-for="item in perfData" :key="item.grade" class="perf-bar-item">
                <span class="perf-grade">{{ item.grade.split('(')[0] }}</span>
                <div class="perf-desc">{{ item.grade.match(/\((.+)\)/)?.[1] || '' }}</div>
                <div class="perf-bar-track"><div class="perf-bar-fill" :style="{ width: (item.count / 120 * 100) + '%', background: item.color }"></div></div>
                <span class="perf-count">{{ item.count }}人</span>
              </div>
            </div>
          </t-card>
          <t-card title="绩效与薪资关系" :bordered="false" class="chart-card half">
            <div style="padding:20px;text-align:center;color:#999;">
              <p>📊 综合各维度数据分析：</p>
              <ul style="text-align:left;display:inline-block;margin-top:12px;font-size:14px;line-height:2;">
                <li>S级/A级员工平均薪资：¥15,800</li>
                <li>B级员工平均薪资：¥10,200</li>
                <li>C/D级员工平均薪资：¥7,300</li>
                <li>绩效优秀者占比：16.8%</li>
                <li>需关注改进占比：7.0%</li>
              </ul>
            </div>
          </t-card>
        </div>
      </t-tab-panel>
    </t-tabs>
  </div>
</template>

<style scoped>
.report-page { display: flex; flex-direction: column; gap: 16px; }

/* 总览统计卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; }
.stat-card { padding: 16px !important; display: flex; align-items: center; gap: 12px; cursor: default; transition: transform .2s; }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.08); }
.stat-icon { font-size: 32px; line-height: 1; border-radius: 10px; width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.stat-info { min-width: 0; }
.stat-value { font-size: 24px; font-weight: 700; color: #333; line-height: 1.2; }
.stat-title { font-size: 13px; color: #999; margin: 2px 0; }
.stat-change { font-size: 12px; font-weight: 500; }
.stat-change.up { color: #00a870; }
.stat-change.down { color: #e34d59; }

/* 图表布局 */
.chart-row { display: flex; gap: 16px; }
.chart-card { flex: 1; }
.chart-card.half { flex: 1; min-width: 0; }
.chart-card.third { flex: 0 0 35%; }
.chart-card.two-third { flex: 1; }
.full-card { width: 100%; }

/* 柱状图 */
.bar-chart { padding: 8px 0; }
.bar-item { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.bar-label { width: 60px; text-align: right; font-size: 13px; flex-shrink: 0; }
.bar-track { flex: 1; height: 22px; background: #f0f0f0; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; background: #0052d9; border-radius: 4px; transition: width .5s ease; min-width: 4px; }
.bar-value { width: 40px; text-align: left; font-size: 13px; font-weight: 500; }

/* 饼图列表 */
.pie-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 12px; }
.pie-item { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.pie-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.pie-label { color: #666; }
.pie-count { margin-left: auto; font-weight: 500; }
.mini-pie-chart { display: flex; justify-content: center; }

/* 水平柱状图 */
.horizontal .h-bar-item { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.h-bar-label { width: 80px; text-align: right; font-size: 13px; flex-shrink: 0; }
.h-bar-track { flex: 1; height: 24px; background: #f0f0f0; border-radius: 4px; overflow: hidden; }
.h-bar-fill { height: 100%; border-radius: 4px; transition: width .5s ease; }
.h-bar-value { width: 40px; font-size: 13px; font-weight: 500; }

/* 趋势图 */
.trend-chart { display: flex; gap: 12px; align-items: stretch; padding: 16px 0; }
.trend-y-axis { display: flex; flex-direction: column; justify-content: space-between; padding-right: 8px; border-right: 1px solid #eee; width: 30px; flex-shrink: 0; }
.trend-y-axis span { font-size: 11px; color: #999; text-align: right; }
.trend-body { display: flex; flex: 1; align-items: flex-end; justify-content: space-around; gap: 8px; padding-bottom: 24px; position: relative; }
.trend-col { display: flex; flex-direction: column; align-items: center; gap: 0; flex: 1; }
.trend-entry-bar { width: 28px; background: #00a870; border-radius: 4px 4px 0 0; transition: height .3s; min-height: 4px; }
.trend-leave-bar { width: 28px; background: #e34d59; border-radius: 4px 4px 0 0; transition: height .3s; min-height: 4px; }
.trend-x-label { margin-top: 4px; font-size: 11px; color: #666; }
.trend-legend { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #666; padding-left: 8px; }
.legend-entry, .legend-leave { width: 12px; height: 12px; border-radius: 2px; display: inline-block; vertical-align: middle; }
.legend-entry { background: #00a870; } .legend-leave { background: #e34d59; }

/* 考勤统计 */
.att-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.att-stat-item { text-align: center; padding: 12px; background: #fafafa; border-radius: 8px; }
.att-val { font-size: 26px; font-weight: 700; line-height: 1.2; }
.att-lbl { font-size: 12px; color: #999; margin-top: 2px; }
.att-green { color: #00a870; } .att-orange { color: #e37318; } .att-red { color: #e34d59; } .att-blue { color: #0052d9; }

/* 绩效条形图 */
.perf-bars { padding: 8px 0; }
.perf-bar-item { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; }
.perf-grade { width: 28px; font-size: 18px; font-weight: 700; text-align: center; flex-shrink: 0; }
.perf-desc { width: 52px; font-size: 12px; color: #999; flex-shrink: 0; }
.perf-bar-track { flex: 1; height: 20px; background: #f0f0f0; border-radius: 4px; overflow: hidden; }
.perf-bar-fill { height: 100%; border-radius: 4px; transition: width .5s; }
.perf-count { width: 40px; font-size: 13px; font-weight: 500; text-align: right; }

/* 响应式 */
@media (max-width: 1200px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
}
</style>
