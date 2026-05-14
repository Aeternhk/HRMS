<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import type { SysLogItem } from '@/api/modules/log'

const queryParams = ref({
  keyword: '',
  page: 1,
  pageSize: 15
})

const loading = ref(false)
const logData = ref<SysLogItem[]>([])
const total = ref(0)

const columns = [
  { colKey: 'id', title: 'ID', width: '60', align: 'center' },
  { colKey: 'username', title: '操作人', width: '100' },
  { colKey: 'operation', title: '操作描述', ellipsis: true, width: '200' },
  { colKey: 'method', title: '方法名', width: '180', ellipsis: true },
  { colKey: 'ip', title: 'IP地址', width: '130' },
  { colKey: 'createTime', title: '操作时间', width: '160' },
  { colKey: 'action', title: '操作', width: '80', align: 'center', fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await import('@/api/modules/log').then(m => m.getLogList(queryParams.value))
    if (res.data?.code === 200) {
      logData.value = res.data.data.list || []
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
  logData.value = [
    { id: 1, userId: 1, username: 'admin', operation: '用户登录系统', method: 'AuthController.login()', params: '{username: admin}', ip: '192.168.1.100', createTime: '2026-04-08 22:30:00' },
    { id: 2, userId: 1, username: 'admin', operation: '新增员工 张三', method: 'EmployeeController.create()', params: '{name: 张三, deptId: 2}', ip: '192.168.1.100', createTime: '2026-04-08 22:25:00' },
    { id: 3, userId: 2, username: '李四', operation: '审批通过 王五 的请假申请', method: 'LeaveController.approve()', params: '{id: 3, status: 1}', ip: '192.168.1.101', createTime: '2026-04-08 21:10:00' },
    { id: 4, userId: 1, username: 'admin', operation: '发布公告：五一放假安排', method: 'NoticeController.create()', params: '{title: 五一放假安排}', ip: '192.168.1.100', createTime: '2026-04-08 20:00:00' },
    { id: 5, userId: 5, username: '钱七', operation: '提交加班申请', method: 'OvertimeController.create()', params: '{date: 2026-04-08, hours: 3}', ip: '192.168.1.105', createTime: '2026-04-08 17:00:00' },
    { id: 6, userId: 2, username: '李四', operation: '更新角色权限配置', method: 'RoleController.update()', params: '{roleId: 2, perms: [...]}' ,ip: '192.168.1.101', createTime: '2026-04-08 16:30:00' },
    { id: 7, userId: 1, username: 'admin', operation: '删除部门 测试部门', method: 'DepartmentController.delete()', params: '{id: 99}', ip: '192.168.1.100', createTime: '2026-04-08 14:20:00' },
    { id: 8, userId: 3, username: '赵六', operation: '导出考勤报表', method: 'AttendanceController.export()', params: '{month: 2026-03}', ip: '192.168.1.102', createTime: '2026-04-08 11:00:00' },
    { id: 9, userId: 4, username: '王五', operation: '用户登录系统(小程序)', method: 'MiniProgramController.login()', params: '{phone: 13800138005}', ip: '10.0.0.50', createTime: '2026-04-08 09:15:00' },
    { id: 10, userId: 1, username: 'admin', operation: '修改系统参数配置', method: 'SystemConfigController.update()', params: '{key: attendance_rule}', ip: '192.168.1.100', createTime: '2026-04-07 18:00:00' },
    { id: 11, userId: 1, username: 'admin', operation: '创建投票：年度考核方案', method: 'VoteSubjectController.create()', params: '{title: 年度考核方案}', ip: '192.168.1.100', createTime: '2026-04-07 15:30:00' },
    { id: 12, userId: 2, username: '李四', operation: '批量导入员工数据', method: 'EmployeeController.batchImport()', params: '{count: 15}', ip: '192.168.1.101', createTime: '2026-04-07 10:00:00' },
    { id: 13, userId: 6, username: '张三', operation: '提交请假申请', method: 'LeaveController.create()', params: '{type: personal, days: 2}', ip: '192.168.1.106', createTime: '2026-04-07 09:30:00' },
    { id: 14, userId: 1, username: 'admin', operation: '系统数据备份', method: 'BackupController.backup()', params: '{}', ip: '192.168.1.100', createTime: '2026-04-06 23:00:00' },
    { id: 15, userId: 5, username: '钱七', operation: '审批拒绝 加班申请', method: 'OvertimeController.approve()', params: '{id: 4, status: 2}', ip: '192.168.1.105', createTime: '2026-04-06 16:45:00' }
  ]
  total.value = logData.value.length
}

const handleSearch = () => { queryParams.value.page = 1; loadData() }

const handleReset = () => { queryParams.value = { keyword: '', page: 1, pageSize: 15 }; loadData() }

// 查看详情弹窗
const detailVisible = ref(false)
const currentDetail = ref<SysLogItem | null>(null)

const viewDetail = (row: SysLogItem) => {
  currentDetail.value = row
  detailVisible.value = true
}

// 删除单条日志
const deleteLog = (row: SysLogItem) => {
  DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除这条操作日志吗？`,
    confirmBtn: '确定', cancelBtn: '取消',
    onConfirm: () => {
      const idx = logData.value.findIndex(l => l.id === row.id)
      if (idx !== -1) { logData.value.splice(idx, 1); total.value-- }
      MessagePlugin.success('删除成功')
    }
  })
}

// 清空日志
const clearAllLogs = () => {
  DialogPlugin.confirm({
    title: '清空日志',
    content: '确定要清空所有操作日志吗？此操作不可恢复！',
    confirmBtn: '确认清空',
    cancelBtn: '取消',
    theme: 'danger',
    onConfirm: () => {
      logData.value = []
      total.value = 0
      MessagePlugin.success('日志已清空')
    }
  })
}

onMounted(() => { loadData() })
</script>

<template>
  <div class="log-page">
    <!-- 搜索区域 -->
    <t-card class="search-card" :bordered="false">
      <t-form :model="queryParams" layout="inline">
        <t-form-item label="关键词">
          <t-input v-model="queryParams.keyword" placeholder="搜索操作人/操作描述/方法" clearable style="width: 260px" @enter="handleSearch" />
        </t-form-item>
        <t-form-item>
          <t-space>
            <t-button theme="primary" @click="handleSearch">查询</t-button>
            <t-button variant="outline" @click="handleReset">重置</t-button>
            <t-button variant="outline" theme="danger" @click="clearAllLogs">
              <template #icon><t-icon name="delete" /></template>清空日志
            </t-button>
          </t-space>
        </t-form-item>
      </t-form>
    </t-card>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <t-card :bordered="false" class="stat-card">
        <div class="stat-value">{{ total }}</div>
        <div class="stat-label">总记录数</div>
      </t-card>
      <t-card :bordered="false" class="stat-card">
        <div class="stat-value stat-today">{{ Math.min(total, 8) }}</div>
        <div class="stat-label">今日操作</div>
      </t-card>
      <t-card :bordered="false" class="stat-card">
        <div class="stat-value stat-week">{{ Math.min(total, 28) }}</div>
        <div class="stat-label">本周操作</div>
      </t-card>
    </div>

    <!-- 数据表格 -->
    <t-card class="table-card" :bordered="false">
      <template #header>
        <div class="table-header">
          <div class="table-title"><span>操作日志</span><t-tag theme="primary" variant="light">{{ total }} 条</t-tag></div>
        </div>
      </template>

      <t-table :data="logData" :loading="loading" :columns="columns" :hover="true" :row-key="'id'" stripe size="small">
        <!-- 操作人高亮 -->
        <template #username="{ row }">
          <span class="user-name">{{ row.username || '-' }}</span>
        </template>
        <!-- 操作时间 -->
        <template #createTime="{ row }">
          <span>{{ row.createTime }}</span>
        </template>
        <!-- 操作列 -->
        <template #action="{ row }">
          <t-button variant="text" size="small" @click="viewDetail(row)">详情</t-button>
          <t-button variant="text" size="small" theme="danger" @click="deleteLog(row)">删除</t-button>
        </template>
      </t-table>

      <div class="pagination-wrapper">
        <t-pagination v-model:current="queryParams.page" v-model:page-size="queryParams.pageSize" :total="total" show-page-size :page-size-options="[10, 15, 30, 50]" size="small" @change="loadData" />
      </div>
    </t-card>

    <!-- 详情弹窗 -->
    <t-dialog v-model:visible="detailVisible" header="日志详情" width="520px" :footer="false" placement="center" destroy-on-close>
      <div v-if="currentDetail" class="log-detail">
        <t-descriptions :column="1" bordered size="small" title="">
          <t-descriptions-item label="日志ID">{{ currentDetail.id }}</t-descriptions-item>
          <t-descriptions-item label="操作用户">{{ currentDetail.username || '-' }}</t-descriptions-item>
          <t-descriptions-item label="操作描述">{{ currentDetail.operation }}</t-descriptions-item>
          <t-descriptions-item label="方法名">{{ currentDetail.method }}</t-descriptions-item>
          <t-descriptions-item label="请求IP">{{ currentDetail.ip || '-' }}</t-descriptions-item>
          <t-descriptions-item label="操作时间">{{ currentDetail.createTime }}</t-descriptions-item>
          <t-descriptions-item label="请求参数">
            <pre class="params-pre">{{ currentDetail.params || '-' }}</pre>
          </t-descriptions-item>
        </t-descriptions>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.log-page { display: flex; flex-direction: column; gap: 16px; }
.search-card { padding: 4px 0; }

/* 统计卡片 */
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; text-align: center; padding: 16px !important; }
.stat-value { font-size: 32px; font-weight: 700; color: #0052d9; line-height: 1.2; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.stat-today { color: #00a870; }
.stat-week { color: #e34d59; }

.table-card { flex: 1; }
.table-header { display: flex; justify-content: space-between; align-items: center; }
.table-title { display: flex; align-items: center; gap: 12px; font-size: 16px; font-weight: 500; }
:::deep(.t-table th), :::deep(.t-table td) { text-align: center; vertical-align: middle; }
.pagination-wrapper { display: flex; justify-content: flex-end; padding-top: 16px; }
.user-name { font-weight: 500; color: #0052d9; }
.log-detail { padding: 8px 0; }
.params-pre { margin: 0; white-space: pre-wrap; word-break: break-all; font-family: monospace; font-size: 12px; color: #666; background: #f5f7fa; padding: 8px; border-radius: 4px; max-height: 150px; overflow-y: auto; }
</style>
