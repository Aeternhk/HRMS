<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'

// 字典类型
interface DictType {
  id: number; name: string; code: string; description: string; status: number; dataCount: number; createTime: string
}
// 字典数据
interface DictData {
  id: number; dictTypeId: number; label: string; value: string; sort: number; status: number; createTime: string
}

const dictTypeList = ref<DictType[]>([])
const selectedType = ref<DictType | null>(null)
const dataList = ref<DictData[]>([])

// 模拟字典类型
const loadTypes = () => {
  dictTypeList.value = [
    { id: 1, name: '性别', code: 'gender', description: '性别字典', status: 1, dataCount: 2, createTime: '2026-04-08' },
    { id: 2, name: '民族', code: 'nation', description: '民族字典', status: 1, dataCount: 0, createTime: '2026-04-08' },
    { id: 3, name: '学历', code: 'education', description: '学历字典', status: 1, dataCount: 7, createTime: '2026-04-08' },
    { id: 4, name: '婚姻状况', code: 'marital_status', description: '婚姻状况字典', status: 1, dataCount: 4, createTime: '2026-04-08' },
    { id: 5, name: '员工状态', code: 'employee_status', description: '员工状态字典', status: 1, dataCount: 5, createTime: '2026-04-08' },
    { id: 6, name: '请假类型', code: 'leave_type', description: '请假类型字典', status: 1, dataCount: 7, createTime: '2026-04-08' },
    { id: 7, name: '合同类型', code: 'contract_type', description: '合同类型字典', status: 1, dataCount: 3, createTime: '2026-04-08' }
  ]
}

// 加载选中类型的字典数据
const loadData = (type: DictType) => {
  selectedType.value = type
  const allData: Record<number, DictData[]> = {
    1: [
      { id: 1, dictTypeId: 1, label: '男', value: '男', sort: 1, status: 1, createTime: '' },
      { id: 2, dictTypeId: 1, label: '女', value: '女', sort: 2, status: 1, createTime: '' }
    ],
    3: [
      { id: 3, dictTypeId: 3, label: '初中', value: '初中', sort: 1, status: 1, createTime: '' },
      { id: 4, dictTypeId: 3, label: '高中', value: '高中', sort: 2, status: 1, createTime: '' },
      { id: 5, dictTypeId: 3, label: '中专', value: '中专', sort: 3, status: 1, createTime: '' },
      { id: 6, dictTypeId: 3, label: '大专', value: '大专', sort: 4, status: 1, createTime: '' },
      { id: 7, dictTypeId: 3, label: '本科', value: '本科', sort: 5, status: 1, createTime: '' },
      { id: 8, dictTypeId: 3, label: '硕士', value: '硕士', sort: 6, status: 1, createTime: '' },
      { id: 9, dictTypeId: 3, label: '博士', value: '博士', sort: 7, status: 1, createTime: '' }
    ],
    4: [
      { id: 10, dictTypeId: 4, label: '未婚', value: '未婚', sort: 1, status: 1, createTime: '' },
      { id: 11, dictTypeId: 4, label: '已婚', value: '已婚', sort: 2, status: 1, createTime: '' },
      { id: 12, dictTypeId: 4, label: '离异', value: '离异', sort: 3, status: 1, createTime: '' },
      { id: 13, dictTypeId: 4, label: '丧偶', value: '丧偶', sort: 4, status: 1, createTime: '' }
    ],
    5: [
      { id: 14, dictTypeId: 5, label: '正式', value: '1', sort: 1, status: 1, createTime: '' },
      { id: 15, dictTypeId: 5, label: '试用', value: '2', sort: 2, status: 1, createTime: '' },
      { id: 16, dictTypeId: 5, label: '实习', value: '3', sort: 3, status: 1, createTime: '' },
      { id: 17, dictTypeId: 5, label: '离职', value: '4', sort: 4, status: 1, createTime: '' },
      { id: 18, dictTypeId: 5, label: '退休', value: '5', sort: 5, status: 1, createTime: '' }
    ],
    6: [
      { id: 19, dictTypeId: 6, label: '年假', value: 'annual', sort: 1, status: 1, createTime: '' },
      { id: 20, dictTypeId: 6, label: '病假', value: 'sick', sort: 2, status: 1, createTime: '' },
      { id: 21, dictTypeId: 6, label: '事假', value: 'personal', sort: 3, status: 1, createTime: '' },
      { id: 22, dictTypeId: 6, label: '婚假', value: 'marriage', sort: 4, status: 1, createTime: '' },
      { id: 23, dictTypeId: 6, label: '产假', value: 'maternity', sort: 5, status: 1, createTime: '' },
      { id: 24, dictTypeId: 6, label: '陪产假', value: 'paternity', sort: 6, status: 1, createTime: '' },
      { id: 25, dictTypeId: 6, label: '丧假', value: 'bereavement', sort: 7, status: 1, createTime: '' }
    ],
    7: [
      { id: 26, dictTypeId: 7, label: '劳动合同', value: 'labor', sort: 1, status: 1, createTime: '' },
      { id: 27, dictTypeId: 7, label: '劳务合同', value: 'service', sort: 2, status: 1, createTime: '' },
      { id: 28, dictTypeId: 7, label: '实习协议', value: 'internship', sort: 3, status: 1, createTime: '' }
    ]
  }
  dataList.value = allData[type.id] || []
}

// 新增字典数据弹窗
const dataDialogVisible = ref(false)
const isEditing = ref(false)
const formRef = ref()
const formData = ref({ label: '', value: '', sort: 0 })

const openAddDataDialog = () => {
  isEditing.value = false; formData.value = { label: '', value: '', sort: 0 }; dataDialogVisible.value = true
}

const openEditDataDialog = (row: DictData) => {
  isEditing.value = true; formData.value = { label: row.label, value: row.value, sort: row.sort }
  ; (formData.value as any).id = row.id; dataDialogVisible.value = true
}

const submitData = () => {
  if (isEditing.value) {
    const idx = dataList.value.findIndex(d => d.id === (formData.value as any).id)
    if (idx !== -1) Object.assign(dataList.value[idx], formData.value)
    MessagePlugin.success('更新成功')
  } else {
    dataList.value.push({
      id: Date.now(), dictTypeId: selectedType.value!.id,
      label: formData.value.label, value: formData.value.value,
      sort: formData.value.sort || dataList.value.length + 1, status: 1, createTime: new Date().toISOString().slice(0, 16).replace('T', ' ')
    })
    MessagePlugin.success('新增成功')
  }
  dataDialogVisible.value = false
}

const deleteData = (row: DictData) => {
  DialogPlugin.confirm({ title: '确认删除', content: `删除「${row.label}」？`, theme: 'danger', onConfirm: () => {
    dataList.value = dataList.value.filter(d => d.id !== row.id); MessagePlugin.success('已删除')
  }})
}

onMounted(() => loadTypes())
</script>

<template>
  <div class="dict-page">
    <div class="dict-layout">
      <!-- 左侧：字典类型列表 -->
      <t-card title="字典类型" :bordered="false" class="type-panel">
        <div v-for="type in dictTypeList" :key="type.id"
          class="type-item" :class="{ active: selectedType?.id === type.id }"
          @click="loadData(type)">
          <span class="type-name">{{ type.name }}</span>
          <span class="type-code">{{ type.code }}</span>
          <t-tag size="small" variant="light" theme="default">{{ type.dataCount }}项</t-tag>
        </div>
        <div v-if="!selectedType" class="type-empty">← 请选择一个字典类型查看数据</div>
      </t-card>

      <!-- 右侧：字典数据 -->
      <t-card :header="selectedType ? `【${selectedType.name}】(${selectedType.code})` : '字典数据'" :bordered="false" class="data-panel">
        <template #toolbar v-if="selectedType">
          <t-button theme="primary" size="small" @click="openAddDataDialog"><t-icon name="add-circle" /> 新增</t-button>
        </template>

        <div v-if="!selectedType" class="data-empty">
          <p>请先从左侧选择一个字典类型</p>
        </div>

        <t-table v-else :data="dataList" :hover="true" :row-key="'id'" stripe size="small"
          :columns="[
            { colKey: 'sort', title: '排序', width: 60, align: 'center' },
            { colKey: 'label', title: '标签名称', width: 150 },
            { colKey: 'value', title: '字典值', width: 150 },
            { colKey: 'status', title: '状态', width: 70, align: 'center' },
            { colKey: 'action', title: '操作', width: 120, align: 'center' }
          ]">
          <template #status="{ row }">
            <t-tag :theme="row.status === 1 ? 'success' : 'default'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</t-tag>
          </template>
          <template #action="{ row }">
            <t-button variant="text" size="small" @click="openEditDataDialog(row)">编辑</t-button>
            <t-button variant="text" size="small" theme="danger" @click="deleteData(row)">删除</t-button>
          </template>
        </t-table>
      </t-card>
    </div>

    <!-- 新增/编辑字典数据弹窗 -->
    <t-dialog v-model:visible="dataDialogVisible" :header="isEditing ? '编辑字典数据' : '新增字典数据'" width="480px" :footer="false" placement="center" destroy-on-close>
      <t-form ref="formRef" :data="formData" label-width="80px" layout="vertical" class="dict-form">
        <t-form-item label="标签名" :rules="[{required:true}]">
          <t-input v-model="formData.label" placeholder="如：男/女/本科..." />
        </t-form-item>
        <t-form-item label="字典值" :rules="[{required:true}]">
          <t-input v-model="formData.value" placeholder="存储在数据库中的实际值" />
        </t-form-item>
        <t-form-item label="排序号">
          <t-input-number v-model="formData.sort" :min="0" style="width:160px" />
        </t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="dataDialogVisible=false">取消</t-button>
        <t-button theme="primary" @click="submitData">确定</t-button>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.dict-page { display: flex; flex-direction: column; gap: 16px; height: calc(100vh - 140px); }

.dict-layout { display: flex; gap: 16px; height: 100%; min-height: 400px; }
.type-panel { width: 240px; flex-shrink: 0; overflow-y: auto; }
.type-panel :deep(.t-card__body) { padding: 12px; }
.type-item { padding: 10px 12px; border-radius: 8px; cursor: pointer; display: flex; align-items: center; gap: 8px;
  margin-bottom: 6px; transition: background .15s; border: 1px solid transparent; }
.type-item:hover { background: #f5f7fa; }
.type-item.active { background: #e6f0ff; border-color: #0052d9; color: #0052d9; font-weight: 500; }
.type-name { flex: 1; font-size: 14px; }
.type-code { font-size: 11px; color: #999; font-family: monospace; }
.type-empty { text-align: center; padding: 40px 0; color: #999; font-size: 13px; }

.data-panel { flex: 1; min-width: 0; overflow: hidden; display: flex; flex-direction: column; }
.data-panel :deep(.t-card__body) { flex: 1; overflow: auto; }
.data-empty { text-align: center; padding: 60px 0; color: #999; }

:::deep(.t-table th), :::deep(.t-table td) { text-align: center; vertical-align: middle; }
.dict-form { padding: 16px 0; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 20px; margin-top: 8px; border-top: 1px solid #e7e7e7; }
</style>
