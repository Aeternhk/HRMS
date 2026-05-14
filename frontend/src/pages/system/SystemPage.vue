<script setup lang="ts">
import { ref, computed } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'

// 角色选项
const roleOptions = [
  { value: 1, label: '超级管理员' },
  { value: 2, label: '人事主管' },
  { value: 3, label: '部门主管' },
  { value: 4, label: '普通员工' }
]

// 状态选项
const statusOptions = [
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' }
]

// 权限选项
const permissionOptions = [
  { value: 'employee:*', label: '员工管理（全部）' },
  { value: 'employee:read', label: '员工管理（查看）' },
  { value: 'employee:self', label: '员工管理（本人）' },
  { value: 'department:*', label: '部门管理（全部）' },
  { value: 'department:read', label: '部门管理（查看）' },
  { value: 'attendance:*', label: '考勤管理（全部）' },
  { value: 'attendance:read', label: '考勤管理（查看）' },
  { value: 'attendance:approve', label: '考勤管理（审批）' },
  { value: 'attendance:clock', label: '考勤管理（打卡）' },
  { value: 'salary:*', label: '薪资管理（全部）' },
  { value: 'salary:read', label: '薪资管理（查看）' },
  { value: 'performance:*', label: '绩效考核（全部）' },
  { value: 'performance:read', label: '绩效考核（查看）' },
  { value: 'vote:*', label: '投票管理（全部）' },
  { value: 'vote:create', label: '投票管理（创建）' },
  { value: 'vote:participate', label: '投票管理（参与）' },
  { value: 'report:*', label: '报表查看（全部）' }
]

// ==================== 角色权限 ====================

// 角色数据类型
interface Role {
  id: number
  name: string
  code: string
  description: string
  userCount: number
  status: number
  statusName: string
  createTime: string
  permissions: string[]
}

// 角色数据
const roleData = ref<Role[]>([
  { id: 1, name: '超级管理员', code: 'SUPER_ADMIN', description: '系统全部功能', userCount: 1, status: 1, statusName: '正常', createTime: '2020-01-01', permissions: ['*'] },
  { id: 2, name: '人事主管', code: 'HR_MANAGER', description: '人事管理全模块、报表查看', userCount: 1, status: 1, statusName: '正常', createTime: '2020-03-15', permissions: ['employee:*', 'department:*', 'attendance:read', 'salary:read', 'performance:*', 'report:read'] },
  { id: 3, name: '部门主管', code: 'DEPT_MANAGER', description: '本部门人员管理、考勤审批', userCount: 1, status: 1, statusName: '正常', createTime: '2021-06-01', permissions: ['employee:read', 'attendance:approve', 'vote:create'] },
  { id: 4, name: '普通员工', code: 'EMPLOYEE', description: '查看个人信息、考勤打卡、参与投票', userCount: 4, status: 1, statusName: '正常', createTime: '2020-01-01', permissions: ['employee:self', 'attendance:clock', 'vote:participate'] }
])

// 角色表格列
const roleColumns = [
  { colKey: 'name', title: '角色名称', width: '120', ellipsis: true },
  { colKey: 'code', title: '角色编码', width: '130', ellipsis: true },
  { colKey: 'description', title: '描述', width: '180', ellipsis: true },
  { colKey: 'userCount', title: '用户数', width: '70', align: 'center', ellipsis: true },
  { colKey: 'status', title: '状态', width: '70', ellipsis: true },
  { colKey: 'createTime', title: '创建时间', width: '110', ellipsis: true },
  { colKey: 'action', title: '操作', width: '150', align: 'center', fixed: 'right' }
]

// 新增角色弹窗
const addRoleDialogVisible = ref(false)
const addRoleFormRef = ref()
const addRoleLoading = ref(false)
const addRoleFormData = ref({
  name: '',
  code: '',
  description: '',
  permissions: [] as string[]
})

const addRoleRules = {
  name: [{ required: true, message: '请输入角色名称', type: 'error' }],
  code: [{ required: true, message: '请输入角色编码', type: 'error' }]
}

const openAddRoleDialog = () => {
  addRoleFormData.value = {
    name: '',
    code: '',
    description: '',
    permissions: []
  }
  addRoleDialogVisible.value = true
}

const handleAddRole = async () => {
  try {
    await addRoleFormRef.value?.validate()
    addRoleLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const newRole: Role = {
      id: Date.now(),
      name: addRoleFormData.value.name,
      code: addRoleFormData.value.code,
      description: addRoleFormData.value.description,
      userCount: 0,
      status: 1,
      statusName: '正常',
      createTime: dayjs().format('YYYY-MM-DD'),
      permissions: addRoleFormData.value.permissions
    }
    
    roleData.value.push(newRole)
    
    MessagePlugin.success('新增成功')
    addRoleDialogVisible.value = false
  } catch (e: any) {
    if (e !== true) {
      MessagePlugin.error(e.message || '操作失败')
    }
  } finally {
    addRoleLoading.value = false
  }
}

// 编辑角色弹窗
const editRoleDialogVisible = ref(false)
const editRoleFormRef = ref()
const editRoleLoading = ref(false)
const editRoleFormData = ref<Partial<Role>>({})

const openEditRoleDialog = (row: Role) => {
  editRoleFormData.value = { ...row }
  editRoleDialogVisible.value = true
}

const handleEditRole = async () => {
  try {
    await editRoleFormRef.value?.validate()
    editRoleLoading.value = true
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const index = roleData.value.findIndex(r => r.id === editRoleFormData.value.id)
    if (index !== -1) {
      roleData.value[index] = { ...roleData.value[index], ...editRoleFormData.value }
    }
    
    MessagePlugin.success('更新成功')
    editRoleDialogVisible.value = false
  } catch (e: any) {
    if (e !== true) {
      MessagePlugin.error(e.message || '操作失败')
    }
  } finally {
    editRoleLoading.value = false
  }
}

// 删除角色
const deleteRole = (row: Role) => {
  if (row.code === 'SUPER_ADMIN') {
    MessagePlugin.warning('不能删除超级管理员角色')
    return
  }
  if (row.userCount > 0) {
    MessagePlugin.warning('该角色下有用户，不能删除')
    return
  }
  const dialog = DialogPlugin.confirm({
    title: '确认删除',
    content: `确定要删除角色 "${row.name}" 吗？`,
    confirmBtn: '确定',
    cancelBtn: '取消',
    onConfirm: async () => {
      try {
        await new Promise(resolve => setTimeout(resolve, 300))
        const index = roleData.value.findIndex(r => r.id === row.id)
        if (index !== -1) {
          roleData.value.splice(index, 1)
        }
        MessagePlugin.success('删除成功')
        dialog.destroy()
      } catch (e) {
        MessagePlugin.error('删除失败')
      }
    }
  })
}

// 全选/取消全选权限
const allPermissionSelected = computed(() => {
  return editRoleFormData.value.permissions?.length === permissionOptions.length
})

const toggleAllPermissions = (checked: boolean) => {
  if (!editRoleFormData.value.permissions) {
    editRoleFormData.value.permissions = []
  }
  if (checked) {
    editRoleFormData.value.permissions = permissionOptions.map(p => p.value)
  } else {
    editRoleFormData.value.permissions = []
  }
}
</script>

<template>
  <div class="system-page">
    <!-- 角色权限（用户管理已合并到员工管理） -->
    <t-card :bordered="false">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <span>角色列表</span>
            <t-tag theme="primary" variant="light" size="small">{{ roleData.length }} 个</t-tag>
          </div>
          <t-button theme="primary" @click="openAddRoleDialog">
            <template #icon>
              <t-icon name="add-circle" />
            </template>
            新增角色
          </t-button>
        </div>
      </template>

      <!-- 表格 -->
      <t-table
        :data="roleData"
        :columns="roleColumns"
        row-key="id"
        :hover="true"
        :stripe="true"
      >
        <template #permissions="{ row }">
          <t-space :size="4" style="flex-wrap: wrap">
            <t-tag v-if="row.permissions[0] === '*'" theme="primary" size="small">全部权限</t-tag>
            <t-tag v-for="perm in row.permissions.slice(0, 3)" :key="perm" size="small">
              {{ permissionOptions.find(p => p.value === perm)?.label || perm }}
            </t-tag>
            <t-tag v-if="row.permissions.length > 3" size="small">+{{ row.permissions.length - 3 }}</t-tag>
          </t-space>
        </template>
        <!-- 状态 -->
        <template #status="{ row }">
          <t-tag :theme="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.statusName }}</t-tag>
        </template>
        <!-- 操作 -->
        <template #action="{ row }">
          <t-space>
            <t-button variant="text" size="small" @click="openEditRoleDialog(row)">编辑</t-button>
            <t-button variant="text" size="small" theme="danger" @click="deleteRole(row)">删除</t-button>
          </t-space>
        </template>
      </t-table>
    </t-card>

    <!-- 新增角色弹窗 -->
    <t-dialog
      v-model:visible="addRoleDialogVisible"
      title="新增角色"
      width="600px"
      :footer="false"
    >
      <t-form
        ref="addRoleFormRef"
        :model="addRoleFormData"
        :rules="addRoleRules"
        label-width="90px"
        layout="vertical"
      >
        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="角色名称" name="name">
              <t-input v-model="addRoleFormData.name" placeholder="请输入角色名称" />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="角色编码" name="code">
              <t-input v-model="addRoleFormData.code" placeholder="请输入角色编码，如：HR_MANAGER" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="角色描述">
          <t-textarea v-model="addRoleFormData.description" placeholder="请输入角色描述" :rows="2" />
        </t-form-item>
        <t-form-item label="权限配置">
          <t-checkbox-group v-model="addRoleFormData.permissions">
            <t-checkbox v-for="item in permissionOptions" :key="item.value" :value="item.value" :label="item.label" style="margin-bottom: 8px" />
          </t-checkbox-group>
        </t-form-item>
        <div class="form-footer">
          <t-space>
            <t-button theme="primary" :loading="addRoleLoading" @click="handleAddRole">确定</t-button>
            <t-button variant="outline" @click="addRoleDialogVisible = false">取消</t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>

    <!-- 编辑角色弹窗 -->
    <t-dialog
      v-model:visible="editRoleDialogVisible"
      title="编辑角色"
      width="580px"
      :footer="false"
      placement="center"
      :destroy-on-close="true"
      :close-on-overlay-click="true"
    >
      <t-form
        ref="editRoleFormRef"
        :model="editRoleFormData"
        :rules="addRoleRules"
        label-width="90px"
        layout="vertical"
        class="role-form"
      >
        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="角色名称" name="name">
              <t-input v-model="editRoleFormData.name" placeholder="请输入角色名称" />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="角色编码" name="code">
              <t-input v-model="editRoleFormData.code" placeholder="请输入角色编码" />
            </t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="角色描述">
          <t-textarea v-model="editRoleFormData.description" placeholder="请输入角色描述" :rows="2" />
        </t-form-item>
        <t-form-item label="权限配置">
          <div class="permission-grid">
            <t-checkbox
              :checked="allPermissionSelected"
              @change="toggleAllPermissions"
              class="select-all-perm"
            >
              全选
            </t-checkbox>
            <t-checkbox-group v-model="editRoleFormData.permissions" class="permission-list">
              <t-checkbox v-for="item in permissionOptions" :key="item.value" :value="item.value" :label="item.label" />
            </t-checkbox-group>
          </div>
        </t-form-item>
        <div class="form-footer">
          <t-space>
            <t-button theme="primary" :loading="editRoleLoading" @click="handleEditRole">保存</t-button>
            <t-button variant="outline" @click="editRoleDialogVisible = false">取消</t-button>
          </t-space>
        </div>
      </t-form>
    </t-dialog>
  </div>
</template>

<style scoped>
.system-page {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
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

/* 角色表单样式 */
.role-form {
  padding: 16px 0;
  width: 100%;
}

/* 权限复选项网格布局 */
.permission-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.select-all-perm {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e7e7e7;
}

.permission-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px 16px;
  width: 100%;
}

/* 编辑弹窗输入框对齐 */
.role-form .t-input,
.role-form .t-textarea,
.role-form .t-select {
  width: 100%;
  min-width: 0;
}
</style>
