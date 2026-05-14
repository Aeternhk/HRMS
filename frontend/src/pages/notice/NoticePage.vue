<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'

// 公告数据类型
interface Notice {
  id: number
  title: string
  content: string
  type: number
  typeName: string
  status: number
  statusName: string
  creatorId: number
  creatorName: string
  publishTime: string
  createTime: string
  targetRoles: number[]
  targetRoleNames: string
}

// 角色数据类型
interface Role {
  id: number
  name: string
  code: string
}

// 查询参数
const queryParams = ref({
  keyword: '',
  type: undefined as number | undefined,
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

// 公告数据
const loading = ref(false)
const noticeData = ref<Notice[]>([])
const total = ref(0)

// 角色列表
const roleList = ref<Role[]>([
  { id: 1, name: '超级管理员', code: 'SUPER_ADMIN' },
  { id: 2, name: '人事主管', code: 'HR_MANAGER' },
  { id: 3, name: '部门主管', code: 'DEPT_MANAGER' },
  { id: 4, name: '普通员工', code: 'EMPLOYEE' }
])

// 公告类型选项
const typeOptions = [
  { value: 1, label: '普通公告' },
  { value: 2, label: '重要通知' },
  { value: 3, label: '紧急通知' }
]

// 状态选项
const statusOptions = [
  { value: 1, label: '已发布' },
  { value: 0, label: '草稿' }
]

// 表格列
const columns = [
  { colKey: 'title', title: '标题', width: '250', ellipsis: true },
  { colKey: 'type', title: '类型', width: '100' },
  { colKey: 'status', title: '状态', width: '80' },
  { colKey: 'targetRoleNames', title: '发布范围', width: '150', ellipsis: true },
  { colKey: 'creatorName', title: '创建人', width: '100' },
  { colKey: 'publishTime', title: '发布时间', width: '160' },
  { colKey: 'createTime', title: '创建时间', width: '160' },
  { colKey: 'action', title: '操作', width: '180', align: 'center', fixed: 'right' }
]

// 加载数据
const loadData = () => {
  loading.value = true
  // 模拟数据
  noticeData.value = [
    { id: 1, title: '关于2026年五一劳动节放假安排的通知', content: '放假安排详见附件...', type: 3, typeName: '紧急通知', status: 1, statusName: '已发布', creatorId: 1, creatorName: '管理员', publishTime: '2026-04-01 10:00', createTime: '2026-04-01 09:00', targetRoles: [1,2,3,4], targetRoleNames: '全体人员' },
    { id: 2, title: '公司年会通知', content: '年会定于本周五...', type: 2, typeName: '重要通知', status: 1, statusName: '已发布', creatorId: 1, creatorName: '管理员', publishTime: '2026-03-28 14:00', createTime: '2026-03-28 10:00', targetRoles: [1,2,3,4], targetRoleNames: '全体人员' },
    { id: 3, title: '考勤制度调整通知', content: '自下月起...', type: 2, typeName: '重要通知', status: 0, statusName: '草稿', creatorId: 2, creatorName: '人事主管', publishTime: '-', createTime: '2026-04-05 16:00', targetRoles: [1,2], targetRoleNames: '管理员,人事主管' },
    { id: 4, title: '办公区域管理规定', content: '为了营造良好办公环境...', type: 1, typeName: '普通公告', status: 1, statusName: '已发布', creatorId: 1, creatorName: '管理员', publishTime: '2026-03-20 09:00', createTime: '2026-03-20 08:30', targetRoles: [1,2,3,4], targetRoleNames: '全体人员' }
  ]
  total.value = noticeData.value.length
  loading.value = false
}

// 搜索
const handleSearch = () => {
  loadData()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    keyword: '',
    type: undefined,
    status: undefined,
    page: 1,
    pageSize: 10
  }
  loadData()
}

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增公告')
const dialogLoading = ref(false)
const isEditing = ref(false)
const formRef = ref()

const formData = ref<Partial<Notice>>({
  id: undefined,
  title: '',
  content: '',
  type: 1,
  status: 0,
  targetRoles: []
})

const formRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  targetRoles: [{ required: true, message: '请选择发布范围', trigger: 'change' }]
}

const openAddDialog = () => {
  dialogTitle.value = '新增公告'
  isEditing.value = false
  formData.value = {
    id: undefined,
    title: '',
    content: '',
    type: 1,
    status: 0,
    targetRoles: []
  }
  dialogVisible.value = true
}

const openEditDialog = (row: Notice) => {
  dialogTitle.value = '编辑公告'
  isEditing.value = true
  formData.value = { ...row, targetRoles: row.targetRoles }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    dialogLoading.value = true

    const targetRoleNames = formData.value.targetRoles?.map((id: number) => {
      const role = roleList.value.find(r => r.id === id)
      return role?.name || ''
    }).join(',') || ''

    if (isEditing.value) {
      const index = noticeData.value.findIndex(n => n.id === formData.value.id)
      if (index !== -1) {
        noticeData.value[index] = {
          ...noticeData.value[index],
          ...formData.value,
          targetRoleNames
        } as Notice
      }
      MessagePlugin.success('更新成功')
    } else {
      const newNotice: Notice = {
        id: Date.now(),
        title: formData.value.title || '',
        content: formData.value.content || '',
        type: formData.value.type || 1,
        typeName: typeOptions.find(t => t.value === formData.value.type)?.label || '',
        status: formData.value.status || 0,
        statusName: formData.value.status === 1 ? '已发布' : '草稿',
        creatorId: 1,
        creatorName: '管理员',
        publishTime: '-',
        createTime: dayjs().format('YYYY-MM-DD HH:mm'),
        targetRoles: formData.value.targetRoles || [],
        targetRoleNames
      }
      noticeData.value.unshift(newNotice)
      total.value++
      MessagePlugin.success('新增成功')
    }

    dialogVisible.value = false
  } catch (e: any) {
    if (e !== true) {
      MessagePlugin.error(e.message || '操作失败')
    }
  } finally {
    dialogLoading.value = false
  }
}

// 发布公告
const publishNotice = (row: Notice) => {
  const dialog = DialogPlugin.confirm({
    title: '确认发布',
    content: `确定要发布公告「${row.title}」吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      const index = noticeData.value.findIndex(n => n.id === row.id)
      if (index !== -1) {
        noticeData.value[index].status = 1
        noticeData.value[index].statusName = '已发布'
        noticeData.value[index].publishTime = dayjs().format('YYYY-MM-DD HH:mm')
      }
      MessagePlugin.success('发布成功')
      dialog.destroy()
    }
  })
}

// 删除公告
const deleteNotice = (row: Notice) => {
  const dialog = DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除公告「${row.title}」吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      const index = noticeData.value.findIndex(n => n.id === row.id)
      if (index !== -1) {
        noticeData.value.splice(index, 1)
        total.value--
      }
      MessagePlugin.success('删除成功')
      dialog.destroy()
    }
  })
}

// 全选/取消全选角色
const allRoleSelected = computed(() => {
  return formData.value.targetRoles?.length === roleList.value.length
})

const toggleAllRoles = (checked: boolean) => {
  if (checked) {
    formData.value.targetRoles = roleList.value.map(r => r.id)
  } else {
    formData.value.targetRoles = []
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="notice-page">
    <!-- 搜索区域 -->
    <t-card class="search-card" :bordered="false">
      <t-form :model="queryParams" layout="inline">
        <t-form-item label="关键词">
          <t-input
            v-model="queryParams.keyword"
            placeholder="标题/内容"
            clearable
            style="width: 180px"
            @enter="handleSearch"
          />
        </t-form-item>
        <t-form-item label="类型">
          <t-select
            v-model="queryParams.type"
            placeholder="全部"
            clearable
            style="width: 120px"
          >
            <t-option v-for="item in typeOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>
        <t-form-item label="状态">
          <t-select
            v-model="queryParams.status"
            placeholder="全部"
            clearable
            style="width: 100px"
          >
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
            <span>公告列表</span>
            <t-tag theme="primary" variant="light">{{ total }} 条</t-tag>
          </div>
          <t-button theme="primary" @click="openAddDialog">
            <template #icon>
              <t-icon name="add-circle" />
            </template>
            发布公告
          </t-button>
        </div>
      </template>

      <t-table
        :data="noticeData"
        :loading="loading"
        :columns="columns"
        :hover="true"
        :row-key="'id'"
        stripe
      >
        <!-- 类型 -->
        <template #type="{ row }">
          <t-tag v-if="row.type === 1" theme="default" size="small">{{ row.typeName }}</t-tag>
          <t-tag v-else-if="row.type === 2" theme="warning" size="small">{{ row.typeName }}</t-tag>
          <t-tag v-else theme="danger" size="small">{{ row.typeName }}</t-tag>
        </template>
        <!-- 状态 -->
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'default'" size="small">{{ row.statusName }}</t-tag>
        </template>
        <!-- 操作 -->
        <template #action="{ row }">
          <div class="action-buttons">
            <t-button v-if="row.status === 0" variant="text" size="small" theme="success" @click="publishNotice(row)">发布</t-button>
            <span v-else class="action-placeholder"></span>
            <t-button variant="text" size="small" @click="openEditDialog(row)">编辑</t-button>
            <t-button variant="text" size="small" theme="danger" @click="deleteNotice(row)">删除</t-button>
          </div>
        </template>
      </t-table>
    </t-card>

    <!-- 新增/编辑弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="dialogTitle"
      width="650px"
      :footer="false"
      placement="center"
    >
      <t-form
        ref="formRef"
        :data="formData"
        :rules="formRules"
        label-width="100px"
        layout="vertical"
        class="notice-form"
      >
        <t-form-item label="公告标题" name="title">
          <t-input v-model="formData.title" placeholder="请输入公告标题" />
        </t-form-item>

        <t-form-item label="公告类型" name="type">
          <t-radio-group v-model="formData.type">
            <t-radio :value="1">普通公告</t-radio>
            <t-radio :value="2">重要通知</t-radio>
            <t-radio :value="3">紧急通知</t-radio>
          </t-radio-group>
        </t-form-item>

        <t-form-item label="公告内容" name="content">
          <t-textarea
            v-model="formData.content"
            placeholder="请输入公告内容"
            :autosize="{ minRows: 4, maxRows: 8 }"
          />
        </t-form-item>

        <t-form-item label="发布范围" name="targetRoles">
          <div class="role-checkbox-grid">
            <t-checkbox
              :checked="allRoleSelected"
              @change="toggleAllRoles"
              class="select-all-checkbox"
            >
              全选
            </t-checkbox>
            <t-checkbox-group v-model="formData.targetRoles" class="role-list">
              <t-checkbox v-for="role in roleList" :key="role.id" :value="role.id">
                {{ role.name }}
              </t-checkbox>
            </t-checkbox-group>
          </div>
        </t-form-item>

        <t-form-item label="状态">
          <t-radio-group v-model="formData.status">
            <t-radio :value="0">存为草稿</t-radio>
            <t-radio :value="1">立即发布</t-radio>
          </t-radio-group>
        </t-form-item>
      </t-form>

      <div class="dialog-footer">
        <t-button @click="dialogVisible = false">取消</t-button>
        <t-button theme="primary" :loading="dialogLoading" @click="handleSubmit">
          {{ formData.status === 1 ? '发布' : '保存' }}
        </t-button>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.notice-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card {
  padding: 4px 0;
}

.table-card {
  flex: 1;
}

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

/* 表格样式优化 */
::deep(.t-table) {
  font-size: 14px;
}

::deep(.t-table th),
::deep(.t-table td) {
  text-align: center;
  vertical-align: middle;
}

::deep(.t-table td:first-child),
::deep(.t-table th:first-child) {
  text-align: center;
}

/* 公告表单样式 */
.notice-form {
  padding: 16px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  margin-top: 8px;
  border-top: 1px solid #e7e7e7;
}

/* 发布范围复选项网格布局 */
.role-checkbox-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.select-all-checkbox {
  margin-bottom: 8px;
}

.role-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

/* 操作按钮对齐 - 使用flex布局确保对齐 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  min-width: 150px;
}

.action-placeholder {
  display: inline-block;
  width: 40px; /* 与"发布"按钮宽度一致 */
}
</style>
