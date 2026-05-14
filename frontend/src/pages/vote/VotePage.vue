<script setup lang="ts">
import { ref } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import * as voteApi from '@/api/modules/vote'

// 投票数据类型（对齐后端返回）
interface Vote {
  id: number
  title: string
  description: string
  type: string   // SINGLE/MULTI/SCORE/RANK
  typeName: string
  startTime: string
  endTime: string
  status: number  // 0=未开始 1=进行中 2=已结束
  statusText: string
  participantCount: number
  optionCount: number
  anonymous: number
  createTime?: string
}

interface VoteOption {
  id: number
  content: string
  sort: number
}

// 查询参数
const queryParams = ref({
  keyword: '',
  status: undefined as number | undefined,
  page: 1,
  pageSize: 10
})

// 投票数据
const loading = ref(false)
const voteData = ref<Vote[]>([])
const total = ref(0)

// 状态选项（对齐后端：0=未开始 1=进行中 2=已结束）
const statusOptions = [
  { value: 0, label: '未开始' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已结束' }
]

// 投票类型选项（创建时用，后端存储为数字）
const voteTypeOptions = [
  { value: 1, label: '单选投票' },
  { value: 2, label: '多选投票' },
  { value: 3, label: '评分投票' },
  { value: 4, label: '排序投票' }
]

// 表格列配置
const columns = [
  { colKey: 'title', title: '投票主题', width: '200', ellipsis: true },
  { colKey: 'typeName', title: '投票类型', width: '90', ellipsis: true },
  { colKey: 'status', title: '状态', width: '80', ellipsis: true },
  { colKey: 'participantCount', title: '参与人数', width: '80', align: 'center', ellipsis: true },
  { colKey: 'optionCount', title: '选项数', width: '70', align: 'center', ellipsis: true },
  { colKey: 'startTime', title: '开始时间', width: '140', ellipsis: true },
  { colKey: 'endTime', title: '结束时间', width: '140', ellipsis: true },
  { colKey: 'action', title: '操作', width: '180', align: 'center', fixed: 'right' }
]

// 加载投票列表
const fetchVotes = async () => {
  loading.value = true
  try {
    const res: any = await voteApi.getVotePage({
      keyword: queryParams.value.keyword || undefined,
      status: queryParams.value.status,
      page: queryParams.value.page,
      pageSize: queryParams.value.pageSize
    })
    if (res && res.records) {
      voteData.value = res.records
      total.value = res.total || 0
    }
  } catch (e) {
    console.error('加载投票列表失败:', e)
    // fallback 空数组
    voteData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 首次加载
fetchVotes()

// 搜索
const handleSearch = () => {
  queryParams.value.page = 1
  fetchVotes()
}

// 重置
const handleReset = () => {
  queryParams.value = {
    keyword: '',
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
  fetchVotes()
}

// 创建投票弹窗
const createDialogVisible = ref(false)
const createFormRef = ref()
const createLoading = ref(false)
const createFormData = ref({
  title: '',
  voteType: 1,
  description: '',
  startTime: dayjs().format('YYYY-MM-DD HH:mm'),
  endTime: dayjs().add(7, 'day').format('YYYY-MM-DD HH:mm'),
  anonymous: false,
  maxSelections: 1,
  options: [{ content: '' }, { content: '' }]
})

const createFormRules = {
  title: [{ required: true, message: '请输入投票主题', type: 'error' }],
  startTime: [{ required: true, message: '请选择开始时间', type: 'error' }],
  endTime: [{ required: true, message: '请选择结束时间', type: 'error' }]
}

// 打开创建弹窗
const openCreateDialog = () => {
  createFormData.value = {
    title: '',
    voteType: 1,
    description: '',
    startTime: dayjs().format('YYYY-MM-DD HH:mm'),
    endTime: dayjs().add(7, 'day').format('YYYY-MM-DD HH:mm'),
    anonymous: false,
    maxSelections: 1,
    options: [{ content: '' }, { content: '' }]
  }
  createDialogVisible.value = true
}

// 添加选项
const addOption = () => {
  createFormData.value.options.push({ content: '' })
}

// 移除选项
const removeOption = (index: number) => {
  if (createFormData.value.options.length > 2) {
    createFormData.value.options.splice(index, 1)
  }
}

// 提交创建
const handleCreate = async () => {
  try {
    await createFormRef.value?.validate()

    // 验证选项
    const validOptions = createFormData.value.options.filter(o => o.content.trim())
    if (validOptions.length < 2) {
      MessagePlugin.warning('请至少添加2个投票选项')
      return
    }

    createLoading.value = true
    await voteApi.createVote({
      title: createFormData.value.title,
      voteType: createFormData.value.voteType,
      description: createFormData.value.description,
      startTime: createFormData.value.startTime,
      endTime: createFormData.value.endTime,
      anonymous: createFormData.value.anonymous ? 1 : 0,
      maxSelections: createFormData.value.maxSelections,
      targetType: 1, // 默认全体员工
      options: validOptions.map(o => ({ content: o.content }))
    })

    MessagePlugin.success('创建成功')
    createDialogVisible.value = false
    fetchVotes() // 刷新列表
  } catch (e: any) {
    if (e !== true) {
      MessagePlugin.error(e.message || '操作失败')
    }
  } finally {
    createLoading.value = false
  }
}

// 详情弹窗
const detailDialogVisible = ref(false)
const detailData = ref<any>(null)

const openDetailDialog = async (row: Vote) => {
  try {
    const res: any = await voteApi.getVoteDetail(row.id)
    detailData.value = res
    detailDialogVisible.value = true
  } catch (e) {
    detailData.value = row
    detailDialogVisible.value = true
  }
}

// 编辑弹窗
const editDialogVisible = ref(false)
const editFormRef = ref()
const editFormData = ref<Partial<any>>({})
const editLoading = ref(false)

const openEditDialog = async (row: Vote) => {
  try {
    const res: any = await voteApi.getVoteDetail(row.id)
    editFormData.value = {
      id: res.id,
      title: res.title,
      description: res.description,
      voteType: res.type === 'SINGLE' ? 1 : res.type === 'MULTI' ? 2 : 3,
      startTime: res.startTime ? res.startTime.replace(' ', 'T') : '',
      endTime: res.endTime ? res.endTime.replace(' ', 'T') : '',
      options: (res.options || []).map((o: any) => ({ id: o.id, content: o.content }))
    }
    editDialogVisible.value = true
  } catch (e) {
    editFormData.value = { ...row, startTime: '', endTime: '' }
    editDialogVisible.value = true
  }
}

const handleEdit = async () => {
  try {
    editLoading.value = true

    const payload: any = {
      title: editFormData.value.title,
      description: editFormData.value.description,
      voteType: editFormData.value.voteType,
      startTime: editFormData.value.startTime?.replace('T', ' ') || '',
      endTime: editFormData.value.endTime?.replace('T', ' ') || '',
      options: editFormData.value.options || []
    }

    await voteApi.updateVote(editFormData.value.id!, payload)

    MessagePlugin.success('更新成功')
    editDialogVisible.value = false
    fetchVotes() // 刷新列表
  } catch (e: any) {
    MessagePlugin.error(e.message || '更新失败')
  } finally {
    editLoading.value = false
  }
}

// 删除
const handleDelete = (row: Vote) => {
  const dialog = DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除投票 "${row.title}" 吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        await voteApi.deleteVote(row.id)
        total.value--
        MessagePlugin.success('删除成功')
        dialog.destroy()
        fetchVotes() // 刷新列表
      } catch (e) {
        MessagePlugin.error('删除失败')
      }
    }
  })
}
</script>

<template>
  <div class="vote-page">
    <t-card title="投票管理">
      <template #actions>
        <t-button theme="primary" @click="openCreateDialog">
          <template #icon>
            <t-icon name="add" />
          </template>
          创建投票
        </t-button>
      </template>

      <!-- 搜索区域 -->
      <div class="search-bar mb-4">
        <t-form layout="inline">
          <t-form-item label="关键词">
            <t-input
              v-model="queryParams.keyword"
              placeholder="投票主题"
              clearable
              style="width: 200px"
              @enter="handleSearch"
            />
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
            搜索
          </t-button>
          <t-button variant="outline" @click="handleReset">重置</t-button>
        </div>
      </div>

      <!-- 表格 -->
      <t-table
        :data="voteData"
        :columns="columns"
        row-key="id"
        :hover="true"
        :loading="loading"
        :stripe="true"
      >
        <!-- 状态 -->
        <template #status="{ row }">
          <t-tag v-if="row.status === 0" theme="default" size="small">{{ row.statusText }}</t-tag>
          <t-tag v-else-if="row.status === 1" theme="success" size="small">{{ row.statusText }}</t-tag>
          <t-tag v-else theme="warning" size="small">{{ row.statusText }}</t-tag>
        </template>
        <!-- 开始时间 -->
        <template #startTime="{ row }">{{ row.startTime || '-' }}</template>
        <!-- 结束时间 -->
        <template #endTime="{ row }">{{ row.endTime || '-' }}</template>
        <!-- 操作 -->
        <template #action="{ row }">
          <t-space>
            <t-button variant="text" size="small" @click="openDetailDialog(row)">详情</t-button>
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

    <!-- 创建投票弹窗 -->
    <t-dialog
      v-model:visible="createDialogVisible"
      title="创建投票"
      width="800px"
      :footer="false"
      placement="center"
      :destroy-on-close="true"
      :close-on-overlay-click="true"
    >
      <t-form
        ref="createFormRef"
        :model="createFormData"
        :rules="createFormRules"
        label-width="100px"
        layout="vertical"
        class="vote-form"
      >
        <t-form-item label="投票主题" name="title">
          <t-input v-model="createFormData.title" placeholder="请输入投票主题" />
        </t-form-item>

        <t-form-item label="投票类型">
          <t-select v-model="createFormData.voteType" placeholder="请选择投票类型">
            <t-option v-for="item in voteTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
          </t-select>
        </t-form-item>

        <t-form-item label="投票说明">
          <t-textarea v-model="createFormData.description" placeholder="请输入投票说明" :rows="2" />
        </t-form-item>

        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="开始时间" name="startTime">
              <t-date-picker
                v-model="createFormData.startTime"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                show-time
                allow-input
              />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="结束时间" name="endTime">
              <t-date-picker
                v-model="createFormData.endTime"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                show-time
                allow-input
              />
            </t-form-item>
          </t-col>
        </t-row>

        <t-form-item label="投票设置">
          <t-space direction="vertical">
            <t-checkbox v-model="createFormData.anonymous">匿名投票</t-checkbox>
            <div style="display: flex; align-items: center; gap: 12px;">
              <t-checkbox v-model="(createFormData as any).multiSelect" @change="(val: boolean) => { if (!val) createFormData.maxSelections = 1 }">
                允许多选
              </t-checkbox>
              <t-input-number
                v-if="(createFormData as any).multiSelect"
                v-model="createFormData.maxSelections"
                :min="2"
                :max="10"
                style="width: 160px"
                suffix="项"
              />
            </div>
          </t-space>
        </t-form-item>

        <t-form-item label="投票选项">
          <div class="options-list">
            <div v-for="(option, index) in createFormData.options" :key="index" class="option-item">
              <t-input
                v-model="option.content"
                :placeholder="`选项${index + 1}`"
                style="flex: 1"
              />
              <t-button
                v-if="createFormData.options.length > 2"
                variant="text"
                theme="danger"
                on-click="() => removeOption(index)"
              >
                <t-icon name="remove" />
              </t-button>
            </div>
            <t-button variant="outline" size="small" @click="addOption">
              <t-icon name="add" /> 添加选项
            </t-button>
          </div>
        </t-form-item>

        <div class="form-footer">
          <t-space>
            <t-button theme="primary" :loading="createLoading" @click="handleCreate">创建</t-button>
            <t-button variant="outline" @click="createDialogVisible = false">取消</t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>

    <!-- 详情弹窗 -->
    <t-dialog
      v-model:visible="detailDialogVisible"
      title="投票详情"
      width="700px"
      :footer="false"
      placement="center"
      :destroy-on-close="true"
      :close-on-overlay-click="true"
    >
      <template v-if="detailData">
        <t-descriptions :column="2" bordered>
          <t-descriptions-item label="投票主题" :span="2">{{ detailData.title }}</t-descriptions-item>
          <t-descriptions-item label="投票类型">{{ detailData.typeName }}</t-descriptions-item>
          <t-descriptions-item label="状态">
            <t-tag :theme="detailData.status === 1 ? 'success' : detailData.status === 0 ? 'default' : 'warning'">
              {{ detailData.statusText }}
            </t-tag>
          </t-descriptions-item>
          <t-descriptions-item label="开始时间">{{ detailData.startTime || '-' }}</t-descriptions-item>
          <t-descriptions-item label="结束时间">{{ detailData.endTime || '-' }}</t-descriptions-item>
          <t-descriptions-item label="参与人数">{{ detailData.participantCount || 0 }}</t-descriptions-item>
          <t-descriptions-item label="匿名投票">{{ detailData.anonymous ? '是' : '否' }}</t-descriptions-item>
          <t-descriptions-item label="投票说明" :span="2">{{ detailData.description || '-' }}</t-descriptions-item>
        </t-descriptions>

        <div class="vote-options">
          <h4>投票选项</h4>
          <div v-for="option in (detailData.options || [])" :key="option.id" class="vote-option-item">
            <div class="option-info">
              <span class="option-name">{{ option.content }}</span>
            </div>
          </div>
        </div>
      </template>
    </t-dialog>

    <!-- 编辑弹窗 -->
    <t-dialog
      v-model:visible="editDialogVisible"
      title="编辑投票"
      width="580px"
      :footer="false"
      placement="center"
      :destroy-on-close="true"
      :close-on-overlay-click="true"
    >
      <t-form
        ref="editFormRef"
        :model="editFormData"
        label-width="100px"
        layout="vertical"
        class="vote-form"
      >
        <t-form-item label="投票主题">
          <t-input v-model="editFormData.title" placeholder="请输入投票主题" />
        </t-form-item>
        <t-form-item label="投票说明">
          <t-textarea v-model="editFormData.description" placeholder="请输入投票说明" :rows="2" />
        </t-form-item>
        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="开始时间">
              <t-date-picker
                v-model="editFormData.startTime"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                show-time
                allow-input
              />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="结束时间">
              <t-date-picker
                v-model="editFormData.endTime"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm"
                show-time
                allow-input
              />
            </t-form-item>
          </t-col>
        </t-row>
        <div class="form-footer">
          <t-space>
            <t-button theme="primary" :loading="editLoading" @click="handleEdit">保存</t-button>
            <t-button variant="outline" @click="editDialogVisible = false">取消</t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>
  </div>
</template>

<style scoped>
.vote-page {
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

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.form-footer {
  margin-top: 24px;
  text-align: center;
}

/* 弹窗表单样式 */
.vote-form {
  width: 100%;
}

.vote-form .t-input,
.vote-form .t-textarea,
.vote-form .t-date-picker,
.vote-form .t-select,
.vote-form .t-input-number {
  width: 100%;
  min-width: 0;
}

.options-list {
  width: 100%;
}

.option-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.vote-options {
  margin-top: 24px;
}

.vote-options h4 {
  font-size: 14px;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e7e7e7;
}

.vote-option-item {
  margin-bottom: 16px;
}

.option-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

.option-name {
  font-weight: 500;
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
