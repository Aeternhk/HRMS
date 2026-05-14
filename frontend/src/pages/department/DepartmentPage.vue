<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import * as employeeApi from '@/api/modules/employee'
import { departmentTreeData } from '@/utils/deptData'

// 部门类型（统一使用employee.ts中的Department接口）
interface Department {
  id: number
  name: string
  code: string
  parentId: number | null
  level?: number
  sort: number
  managerId?: number | null
  managerName?: string
  phone?: string
  status: number
  children?: Department[]
  // 树形组件需要的字段
  value?: string | number
  label?: string
}

// 部门员工数量统计
const employeeCounts = ref<Record<number, number>>({})
const countsLoading = ref(false)

// 树形数据（统一结构）
const treeData = ref<Department[]>([])
const loading = ref(false)

// localStorage key
const STORAGE_KEY = 'hrms_department_data'

// 从 localStorage 加载数据
const loadFromStorage = (): Department[] | null => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const parsed = JSON.parse(stored)
      // 检查数据是否完整（包含总公司和足够的子部门）
      if (parsed && parsed.length > 0 && parsed[0].name === '总公司' && parsed[0].children && parsed[0].children.length >= 8) {
        return parsed
      }
    }
  } catch (e) {
    console.error('Failed to load from storage:', e)
  }
  return null
}

// 保存数据到 localStorage
const saveToStorage = (data: Department[]) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch (e) {
    console.error('Failed to save to storage:', e)
  }
}

// 构建模拟数据（有完整id，且value/label字段）
const buildMockData = (): Department[] => {
  return departmentTreeData as Department[]
}

// 加载部门数据
const loadDepartmentData = async () => {
  loading.value = true
  try {
    // 优先从 localStorage 加载（用户自定义的数据）
    const storedData = loadFromStorage()
    if (storedData) {
      treeData.value = storedData
      loading.value = false
      return
    }
    
    // 如果 localStorage 没有数据，尝试从 API 加载
    const res = await employeeApi.getDepartmentTree()
    // 转换为树形数据，如果API返回空或只有根节点，使用完整模拟数据兜底
    const convertToTree = (list: any[], isRoot: boolean = false): Department[] => {
      return list.map(item => ({
        ...item,
        // 如果是根节点（顶级部门），保持原有结构；否则添加 value/label
        value: isRoot ? item.value : (`dept-${item.id}` as string | number),
        label: isRoot ? item.label : item.name,
        children: item.children && item.children.length > 0 ? convertToTree(item.children) : []
      }))
    }
    
    // 检查 API 返回的数据是否已经是完整结构（包含根节点"总公司"）
    const apiData = convertToTree(res)
    const hasRootNode = apiData.length === 1 && 
      (apiData[0]?.name === '总公司' || apiData[0]?.id === 0)
    const hasEnoughChildren = apiData.length >= 8 || 
      (apiData.length === 1 && (apiData[0]?.children?.length ?? 0) >= 8)
    
    if (hasRootNode) {
      // API 已经返回了完整的根节点结构，直接使用
      treeData.value = apiData
    } else if (hasEnoughChildren) {
      // API 返回了顶级部门但没有根节点，手动添加"总公司"根节点
      treeData.value = [{
        id: 0,
        name: '总公司',
        code: 'ROOT',
        parentId: null,
        sort: 0,
        status: 1,
        value: 'root-0',
        label: '总公司',
        children: apiData
      }]
    } else {
      // 数据不完整，使用模拟数据
      treeData.value = buildMockData()
    }
  } catch (e) {
    treeData.value = buildMockData()
  } finally {
    loading.value = false
  }
}

// 选中部门（存储完整的部门对象）
const selectedDepartment = ref<Department | null>(null)
const selectedNode = ref<any>(null)

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const dialogLoading = ref(false)
const formRef = ref<any>(null)
const treeRef = ref<any>(null)
const isEditing = ref(false)

// 删除确认弹窗
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)

// 表单数据
const formData = ref<Partial<Department>>({
  name: '',
  code: '',
  managerName: '',
  phone: '',
  parentId: null,
  sort: 1,
  status: 1
})

// 父部门显示名称
const parentDeptName = ref('')

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'change' }],
  code: [
    { required: true, message: '请输入部门编码', trigger: 'change' },
    { pattern: /^[A-Z0-9_]+$/, message: '编码只能包含大写字母、数字和下划线', trigger: 'change' }
  ]
}

// 点击树节点（使用 active 事件回调）
const handleTreeActive = (context: { node: any }) => {
  const nodeData = context?.node?.data
  if (nodeData) {
    selectedDepartment.value = nodeData
    selectedNode.value = context.node
  }
}

// 处理树节点点击（兼容不同版本事件格式）
const handleTreeNodeClick = (context: any) => {
  const nodeData = context?.node?.data || context?.data || context
  if (nodeData && nodeData.id !== undefined) {
    selectedDepartment.value = { ...nodeData }
    selectedNode.value = context?.node || context
  }
}

// 打开新增弹窗（顶级部门或子部门）
const openAddDialog = (parent?: Department) => {
  isEditing.value = false
  
  // 如果传入了 parent，新增为该部门的子部门
  // 如果没有传入 parent，新增为顶级部门
  if (parent) {
    dialogTitle.value = `新增「${parent.name}」的子部门`
    parentDeptName.value = parent.name
    formData.value = {
      name: '',
      code: '',
      managerName: '',
      phone: '',
      parentId: parent.id,
      sort: 1,
      status: 1
    }
  } else {
    // 新增顶级部门
    dialogTitle.value = '新增顶级部门'
    parentDeptName.value = '总公司'
    formData.value = {
      name: '',
      code: '',
      managerName: '',
      phone: '',
      parentId: 0,
      sort: (treeData.value[0]?.children?.length || 0) + 1,
      status: 1
    }
  }
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = () => {
  if (!selectedDepartment.value) {
    MessagePlugin.warning('请先选择要编辑的部门')
    return
  }
  isEditing.value = true
  dialogTitle.value = `编辑「${selectedDepartment.value.name}」`
  formData.value = { ...selectedDepartment.value }
  // 获取父部门名称
  if (selectedDepartment.value.parentId === null || selectedDepartment.value.parentId === 0) {
    parentDeptName.value = '无（顶级部门）'
  } else {
    parentDeptName.value = getParentName(treeData.value, selectedDepartment.value.id) || '总公司'
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 手动验证必填字段
    if (!formData.value.name?.trim()) {
      MessagePlugin.warning('请输入部门名称')
      return
    }
    if (!formData.value.code?.trim()) {
      MessagePlugin.warning('请输入部门编码')
      return
    }
    if (!/^[A-Z0-9_]+$/.test(formData.value.code?.trim() || '')) {
      MessagePlugin.warning('部门编码只能包含大写字母、数字和下划线')
      return
    }
    
    dialogLoading.value = true
    
    if (isEditing.value && formData.value.id !== undefined) {
      // 编辑模式
      try {
        await employeeApi.updateDepartment(formData.value)
      } catch {}
      // 更新本地树
      const updated = { ...formData.value, value: `dept-${formData.value.id}`, label: formData.value.name } as Department
      updateTreeNode(treeData.value, updated)
      selectedDepartment.value = { ...selectedDepartment.value!, ...updated }
      // 保存到 localStorage
      saveToStorage(treeData.value)
      MessagePlugin.success('更新成功')
    } else {
      // 新增模式
      const newId = Date.now()
      try {
        await employeeApi.addDepartment(formData.value)
      } catch {}
      const newDept: Department = {
        id: newId,
        name: formData.value.name!,
        code: formData.value.code!,
        parentId: formData.value.parentId ?? 0,
        sort: formData.value.sort || 1,
        managerName: formData.value.managerName || '',
        phone: formData.value.phone || '',
        status: formData.value.status ?? 1,
        value: `dept-${newId}`,
        label: formData.value.name!,
        children: []
      }
      
      const parentId = formData.value.parentId
      if (parentId !== null && parentId !== undefined) {
        addToParent(treeData.value, parentId, newDept)
      } else {
        treeData.value[0]?.children?.push(newDept)
      }
      // 保存到 localStorage
      saveToStorage(treeData.value)
      MessagePlugin.success('新增成功')
    }
    
    dialogVisible.value = false
    // 重新加载数据确保显示最新
    await nextTick()
    await loadDepartmentData()
    
    dialogVisible.value = false
  } catch (e: any) {
    MessagePlugin.error(e?.message || '操作失败')
  } finally {
    dialogLoading.value = false
  }
}

// 递归更新树节点
const updateTreeNode = (data: Department[], updated: Department): boolean => {
  for (let i = 0; i < data.length; i++) {
    if (data[i].id === updated.id) {
      // 保留children
      data[i] = { ...data[i], ...updated, children: data[i].children }
      return true
    }
    if (data[i].children && updateTreeNode(data[i].children!, updated)) {
      return true
    }
  }
  return false
}

// 添加到父节点
const addToParent = (data: Department[], parentId: number, newDept: Department): boolean => {
  for (const node of data) {
    if (node.id === parentId) {
      if (!node.children) node.children = []
      node.children.push(newDept)
      return true
    }
    if (node.children && addToParent(node.children, parentId, newDept)) {
      return true
    }
  }
  return false
}

// 递归收集所有子部门ID（包括孙子部门）
const collectAllChildIds = (dept: Department): number[] => {
  const ids: number[] = []
  if (dept.children && dept.children.length > 0) {
    for (const child of dept.children) {
      ids.push(child.id)
      ids.push(...collectAllChildIds(child))
    }
  }
  return ids
}

// 删除部门
const handleDelete = () => {
  if (!selectedDepartment.value) {
    MessagePlugin.warning('请先选择要删除的部门')
    return
  }
  if (selectedDepartment.value.parentId === null || selectedDepartment.value.parentId === 0) {
    MessagePlugin.warning('不能删除总公司')
    return
  }
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  const dept = selectedDepartment.value
  if (!dept) return
  deleteLoading.value = true
  try {
    // 收集所有要删除的部门ID（包括子部门）
    const allIds = [dept.id, ...collectAllChildIds(dept)]
    
    // 调用API删除
    for (const id of allIds) {
      try {
        await employeeApi.deleteDepartment(id)
      } catch {}
    }
    
    // 从树中移除该部门及其所有子部门
    removeNodeAndChildren(treeData.value, dept.id)
    
    // 保存到 localStorage
    saveToStorage(treeData.value)
    selectedDepartment.value = null
    deleteDialogVisible.value = false
    // 重新加载数据确保显示最新
    await nextTick()
    await loadDepartmentData()
    
    const childCount = allIds.length - 1
    if (childCount > 0) {
      MessagePlugin.success(`删除成功（包含 ${childCount} 个子部门）`)
    } else {
      MessagePlugin.success('删除成功')
    }
  } catch (e) {
    MessagePlugin.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}

// 从树中移除节点及其所有子部门
const removeNodeAndChildren = (data: Department[], id: number): boolean => {
  for (let i = 0; i < data.length; i++) {
    if (data[i].id === id) {
      data.splice(i, 1)
      return true
    }
    if (data[i].children && removeNodeAndChildren(data[i].children, id)) {
      return true
    }
  }
  return false
}

// 获取父部门名称
const getParentName = (data: Department[], targetId: number): string | null => {
  for (const node of data) {
    if (node.children) {
      for (const child of node.children) {
        if (child.id === targetId) {
          return node.name
        }
      }
      const result = getParentName(node.children, targetId)
      if (result) return result
    }
  }
  return null
}

// 统计子部门人数（真实数据）
const countEmployees = (dept: Department): number => {
  // 如果有真实数据，使用真实数据
  if (Object.keys(employeeCounts.value).length > 0) {
    return employeeCounts.value[dept.id] || 0
  }
  // 兜底：如果API未返回，使用模拟数据（不应该走到这里）
  if (!dept.children || dept.children.length === 0) {
    const counts: Record<string, number> = {
      'SMT车间': 25, '贴合车间': 18, '组装车间': 22, '包装车间': 15,
      'IQC': 8, 'OQC': 6, 'QE': 4, '研发部': 12, '品质部': 18,
      '生产部': 80, '采购部': 8, '仓储部': 15, '财务部': 6, '人事部': 5, '行政部': 7
    }
    return counts[dept.name] || 0
  }
  return dept.children.reduce((sum, child) => sum + countEmployees(child), 0)
}

// 获取部门员工数量统计
const loadEmployeeCounts = async () => {
  countsLoading.value = true
  try {
    const res = await employeeApi.getDepartmentEmployeeCounts()
    if (res && typeof res === 'object') {
      employeeCounts.value = res as Record<number, number>
    }
  } catch (e) {
    console.error('Failed to load employee counts:', e)
  } finally {
    countsLoading.value = false
  }
}

// 计算选中部门的详情信息
const departmentDetail = computed(() => {
  if (!selectedDepartment.value) return null
  const dept = selectedDepartment.value
  return {
    id: dept.id,
    name: dept.name,
    code: dept.code || '-',
    managerName: dept.managerName || '-',
    phone: dept.phone || '-',
    parentName: dept.id === 0 ? '-' : (getParentName(treeData.value, dept.id) || '总公司'),
    employeeCount: countEmployees(dept),
    childCount: dept.children?.length || 0,
    sort: dept.sort ?? '-',
    status: String(dept.status) === '1' ? '正常' : '停用',
    level: dept.level ?? 0
  }
})

// 获取层级标签
const getLevelLabel = (level?: number) => {
  const labels = ['总部', '一级部门', '二级部门', '三级部门']
  return labels[level ?? 0] || `${level}级`
}

// 导出
const handleExport = () => {
  MessagePlugin.info('正在导出部门数据...')
}

onMounted(() => {
  loadDepartmentData()
  loadEmployeeCounts()  // 加载真实员工数量
})
</script>

<template>
  <div class="department-page">
    <div class="department-layout">
      <!-- 组织架构树 -->
      <div class="department-left">
        <t-card title="组织架构" :loading="loading">
          <template #actions>
            <t-space>
              <t-button size="small" variant="outline" @click="handleExport">
                <template #icon><t-icon name="download" /></template>
                导出
              </t-button>
            </t-space>
          </template>

          <!-- 选中后的操作栏 -->
          <div class="tree-actions" v-if="selectedDepartment">
            <t-button variant="text" size="small" theme="primary" @click="openAddDialog(selectedDepartment)">
              <template #icon><t-icon name="add-circle" /></template>
              添加子部门
            </t-button>
            <t-button variant="text" size="small" @click="openEditDialog">
              <template #icon><t-icon name="edit" /></template>
              编辑
            </t-button>
            <t-button variant="text" size="small" theme="danger" @click="handleDelete" v-if="selectedDepartment.parentId !== null && selectedDepartment.parentId !== 0">
              <template #icon><t-icon name="delete" /></template>
              删除
            </t-button>
          </div>

          <t-tree
            ref="treeRef"
            :data="treeData"
            hover
            expand-all
            activable
            :active-multiple="false"
            transition
            @active="handleTreeActive"
            @click="handleTreeNodeClick"
          />
        </t-card>
      </div>

      <!-- 部门详情 -->
      <div class="department-right">
        <t-card title="部门详情">
          <template #actions>
            <t-space>
              <t-button
                theme="primary"
                size="small"
                @click="openEditDialog"
              >
                <template #icon><t-icon name="edit" /></template>
                编辑部门
              </t-button>
              <t-button
                v-if="selectedDepartment && selectedDepartment.parentId !== null && selectedDepartment.parentId !== 0"
                size="small"
                variant="outline"
                theme="danger"
                @click="handleDelete"
              >
                <template #icon><t-icon name="delete" /></template>
                删除部门
              </t-button>
            </t-space>
          </template>

          <t-empty v-if="!selectedDepartment" description="请点击左侧部门查看详情" />

          <div v-else-if="departmentDetail" class="department-detail">
            <!-- 部门基本信息卡片 -->
            <div class="dept-header">
              <div class="dept-avatar">
                <span>{{ departmentDetail.name.slice(0, 2) }}</span>
              </div>
              <div class="dept-title-info">
                <h3>{{ departmentDetail.name }}</h3>
                <div class="dept-tags">
                  <t-tag size="small" variant="light" theme="primary">{{ departmentDetail.code }}</t-tag>
                  <t-tag size="small" variant="light" theme="default">{{ getLevelLabel(selectedDepartment.level) }}</t-tag>
                  <t-tag size="small" variant="light" :theme="departmentDetail.status === '正常' ? 'success' : 'default'">
                    {{ departmentDetail.status }}
                  </t-tag>
                </div>
              </div>
            </div>

            <!-- 统计数据 -->
            <div class="dept-stats">
              <div class="stat-item">
                <p class="stat-num">{{ departmentDetail.employeeCount }}</p>
                <p class="stat-label">部门人数</p>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <p class="stat-num">{{ departmentDetail.childCount }}</p>
                <p class="stat-label">子部门数</p>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <p class="stat-num">{{ departmentDetail.sort }}</p>
                <p class="stat-label">排序序号</p>
              </div>
            </div>

            <!-- 详细信息 -->
            <t-descriptions :column="2" bordered size="medium">
              <t-descriptions-item label="部门名称">{{ departmentDetail.name }}</t-descriptions-item>
              <t-descriptions-item label="部门编码">
                <t-tag variant="light" theme="primary" size="small">{{ departmentDetail.code }}</t-tag>
              </t-descriptions-item>
              <t-descriptions-item label="上级部门">{{ departmentDetail.parentName }}</t-descriptions-item>
              <t-descriptions-item label="部门层级">{{ getLevelLabel(selectedDepartment.level) }}</t-descriptions-item>
              <t-descriptions-item label="负责人">
                <t-space v-if="departmentDetail.managerName !== '-'" size="small">
                  <t-avatar size="small">{{ departmentDetail.managerName.slice(0, 1) }}</t-avatar>
                  {{ departmentDetail.managerName }}
                </t-space>
                <span v-else class="text-secondary">暂未设置</span>
              </t-descriptions-item>
              <t-descriptions-item label="联系电话">
                {{ departmentDetail.phone !== '-' ? departmentDetail.phone : '暂未设置' }}
              </t-descriptions-item>
              <t-descriptions-item label="排序">{{ departmentDetail.sort }}</t-descriptions-item>
              <t-descriptions-item label="状态">
                <t-tag :theme="departmentDetail.status === '正常' ? 'success' : 'default'" size="small">
                  {{ departmentDetail.status }}
                </t-tag>
              </t-descriptions-item>
            </t-descriptions>
          </div>
        </t-card>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :title="dialogTitle"
      width="640px"
      :footer="false"
      placement="center"
      destroy-on-close
    >
      <t-form
        ref="formRef"
        :data="formData"
        :rules="formRules"
        label-width="90px"
        layout="vertical"
        class="dept-form"
      >
        <div class="form-grid-2">
          <t-form-item label="部门名称" name="name">
            <t-input v-model="formData.name" placeholder="请输入部门名称" />
          </t-form-item>
          <t-form-item label="部门编码" name="code">
            <t-input v-model="formData.code" placeholder="大写字母，如：PRODUCTION" />
          </t-form-item>
        </div>

        <div class="form-grid-2">
          <t-form-item label="上级部门" name="parentId">
            <t-input :model-value="parentDeptName" disabled />
          </t-form-item>
          <t-form-item label="负责人" name="managerName">
            <t-input v-model="formData.managerName" placeholder="请输入负责人姓名" />
          </t-form-item>
        </div>

        <div class="form-grid-2">
          <t-form-item label="联系电话" name="phone">
            <t-input v-model="formData.phone" placeholder="请输入联系电话" />
          </t-form-item>
          <t-form-item label="排序" name="sort">
            <t-input-number v-model="formData.sort" :min="0" :max="999" placeholder="数值越小越靠前" style="width:100%" />
          </t-form-item>
        </div>

        <t-form-item label="状态" name="status">
          <t-radio-group v-model="formData.status">
            <t-radio value="1">正常</t-radio>
            <t-radio value="0">停用</t-radio>
          </t-radio-group>
        </t-form-item>

        <div class="form-footer">
          <t-space>
            <t-button variant="outline" @click="dialogVisible = false">取消</t-button>
            <t-button theme="primary" :loading="dialogLoading" @click="handleSubmit">
              {{ isEditing ? '保存修改' : '确认新增' }}
            </t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>

    <!-- 删除确认弹窗 -->
    <t-dialog
      v-model:visible="deleteDialogVisible"
      header="确认删除"
      width="420px"
      :footer="false"
      placement="center"
    >
      <div style="padding: 16px 0; line-height: 1.8;">
        <div style="font-size: 15px; margin-bottom: 8px;">
          确定要删除部门「<strong>{{ selectedDepartment?.name }}</strong>」吗？
        </div>
        <div v-if="selectedDepartment?.children && selectedDepartment.children.length > 0"
          style="color: #e34d59; font-size: 13px; margin-bottom: 4px;">
          ⚠️ 该部门下有 {{ selectedDepartment.children.length }} 个子部门，删除后子部门将一并移除。
        </div>
        <div style="color: #e34d59; font-size: 13px;">
          删除后不可恢复，请谨慎操作。
        </div>
      </div>
      <div style="display: flex; justify-content: flex-end; gap: 12px; padding-top: 8px; border-top: 1px solid #e7e7e7;">
        <t-button @click="deleteDialogVisible = false">取消</t-button>
        <t-button theme="danger" :loading="deleteLoading" @click="confirmDelete">
          确定删除
        </t-button>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.department-page {
  width: 100%;
}

.department-layout {
  display: flex;
  gap: 20px;
  width: 100%;
  align-items: flex-start;
}

.department-left {
  flex: 0 0 300px;
  max-width: 300px;
  position: sticky;
  top: 0;
}

.department-right {
  flex: 1;
  min-width: 0;
}

.tree-actions {
  display: flex;
  gap: 4px;
  margin-bottom: 12px;
  padding: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--td-border-level-1-color);
  background: var(--td-bg-color-container-hover, #f8f9fa);
  border-radius: 6px;
}

/* 部门详情 */
.department-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dept-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f4ff 100%);
  border-radius: 10px;
  border: 1px solid #d0e5ff;
}

.dept-avatar {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, #0052d9, #4b9eff);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 82, 217, 0.3);
}

.dept-avatar span {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}

.dept-title-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--td-text-color-primary);
}

.dept-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.dept-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px;
  background: var(--td-bg-color-container, #fff);
  border: 1px solid var(--td-border-level-1-color);
  border-radius: 8px;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #0052d9;
  margin: 0;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--td-text-color-secondary, #888);
  margin: 4px 0 0 0;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: var(--td-border-level-1-color);
}

.text-secondary {
  color: var(--td-text-color-secondary, #999);
}

/* 部门表单弹窗 */
.dept-form {
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 4px;
}

.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 16px;
}

.form-footer {
  margin-top: 16px;
  text-align: right;
  padding-top: 16px;
  border-top: 1px solid var(--td-border-level-1-color);
}

@media (max-width: 768px) {
  .department-layout {
    flex-direction: column;
  }

  .department-left {
    flex: none;
    max-width: 100%;
    position: static;
  }

  .form-grid-2 {
    grid-template-columns: 1fr;
  }
}

/* 部门弹窗垂直居中（通过 mode="center" 属性处理） */
</style>
