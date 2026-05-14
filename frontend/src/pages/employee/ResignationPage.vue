<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'

// Tab: resignation / regularization
const activeTab = ref('resignation')

// ==================== 离职管理 ====================
const resData = ref([
  { id: 1, userId: 99, userName: '周小明', deptName: '生产部', position: '操作员', entryDate: '2023-06-01', resignDate: '2026-03-31', type: '主动离职', reason: '个人发展', status: 1, statusName: '已审批', handoverStatus: '已完成' },
  { id: 2, userId: 100, userName: '孙丽华', deptName: '品质部', position: '检验员', entryDate: '2022-11-15', resignDate: '', type: '合同到期不续签', reason: '合同到期', status: 0, statusName: '待审批', handoverStatus: '未开始' },
  { id: 3, userId: 101, userName: '黄大伟', deptName: '研发部', position: '前端工程师', entryDate: '2024-02-20', resignDate: '', type: '主动离职', reason: '薪资不满意', status: 0, statusName: '待审批', handoverStatus: '未开始' }
])

const resColumns = [
  { colKey: 'userName', title: '姓名', width: 80 },
  { colKey: 'deptName', title: '部门', width: 90 },
  { colKey: 'position', title: '职位', ellipsis: true, width: 110 },
  { colKey: 'entryDate', title: '入职日期', width: 110 },
  { colKey: 'resignDate', title: '预计离职日期', width: 120 },
  { colKey: 'type', title: '离职类型', width: 120 },
  { colKey: 'reason', title: '原因', ellipsis: true, width: 130 },
  { colKey: 'statusName', title: '状态', width: 80, align: 'center' },
  { colKey: 'action', title: '操作', width: 180, align: 'center' }
]

const openResignDialog = () => {
  // 新增离职申请
}

const approveResignation = (row: any) => {
  DialogPlugin.confirm({ title: '审批通过', content: `确认「${row.userName}」的离职申请？`, theme: 'warning', onConfirm: () => {
    row.status = 1; row.statusName = '已审批'; row.resignDate = dayjs().format('YYYY-MM-DD')
    MessagePlugin.success('已批准')
  }})
}

const rejectResignation = (row: any) => {
  DialogPlugin.confirm({ title: '拒绝申请', content: `拒绝「${row.userName}」的离职申请？`, onConfirm: () => {
    MessagePlugin.success('已拒绝')
  }})
}

// ==================== 转正管理 ====================
const regData = ref([
  { id: 1, userId: 6, userName: '张三', deptName: '生产部', position: 'SMT操作员', entryDate: '2025-10-15', positiveDate: '2026-04-15', probationMonths: 6, score: 88, status: 0, statusName: '待评估', remark: '' },
  { id: 2, userId: 102, userName: '刘芳', deptName: '仓储部', position: '仓管员', entryDate: '2025-11-20', positiveDate: '2026-05-20', probationMonths: 6, score: null, status: 0, statusName: '待评估', remark: '' },
  { id: 3, userId: 103, userName: '陈建国', deptName: '采购部', position: '采购专员', entryDate: '2025-08-01', positiveDate: '2026-02-01', probationMonths: 6, score: 92, status: 1, statusName: '已转正', remark: '表现优异，提前转正' },
  { id: 4, userId: 104, userName: '赵丽', deptName: '财务部', position: '会计助理', entryDate: '2026-01-05', positiveDate: '2026-07-05', probationMonths: 6, score: null, status: 0, statusName: '待评估', remark: '' }
])

const regColumns = [
  { colKey: 'userName', title: '姓名', width: 80 },
  { colKey: 'deptName', title: '部门', width: 90 },
  { colKey: 'position', title: '职位', ellipsis: true, width: 110 },
  { colKey: 'entryDate', title: '入职日期', width: 110 },
  { colKey: 'positiveDate', title: '预计转正日期', width: 120 },
  { colKey: 'probationMonths', title: '试用期(月)', width: 90, align: 'center' },
  { colKey: 'score', title: '评分', width: 70, align: 'center' },
  { colKey: 'statusName', title: '状态', width: 80, align: 'center' },
  { colKey: 'action', title: '操作', width: 160, align: 'center' }
]

// 转正评估弹窗
const evalDialogVisible = ref(false)
const currentReg = ref<any>(null)
const evalFormRef = ref()
const evalFormData = ref({
  score: 0,
  result: 1 as number,
  newRank: '',
  newSalary: undefined as number | undefined,
  remark: ''
})

const openEvalDialog = (row: any) => {
  currentReg.value = row
  evalFormData.value = { score: 80, result: 1, newRank: '', newSalary: undefined, remark: '' }
  evalDialogVisible.value = true
}

const submitEval = async () => {
  try {
    await evalFormRef.value?.validate()
    if (currentReg.value) {
      currentReg.value.score = evalFormData.value.score
      currentReg.value.status = evalFormData.value.result === 1 ? 1 : 2
      currentReg.value.statusName = evalFormData.value.result === 1 ? '已转正' : evalFormData.value.result === 2 ? '延长试用期' : '未通过'
      currentReg.value.remark = evalFormData.value.remark
    }
    MessagePlugin.success('评估完成')
    evalDialogVisible.value = false
  } catch {}
}
</script>

<template>
  <div class="resign-page">
    <t-card :bordered="false" class="tab-card">
      <t-tabs v-model:value="activeTab">
        <!-- 离职管理 -->
        <t-tab-panel value="resignation" label="离职管理">
          <template #toolbar>
            <t-button theme="primary"><template #icon><t-icon name="add-circle" /></template>新增离职申请</t-button>
          </template>
          <t-table :data="resData" :columns="resColumns" :hover="true" :row-key="'id'" stripe size="small">
            <template #statusName="{ row }">
              <t-tag :theme="row.status === 1 ? 'danger' : 'warning'" size="small">{{ row.statusName }}</t-tag>
            </template>
            <template #action="{ row }">
              <div class="action-buttons">
                <t-button v-if="row.status === 0" variant="text" size="small" theme="success" @click="approveResignation(row)">批准</t-button>
                <t-button v-if="row.status === 0" variant="text" size="small" theme="danger" @click="rejectResignation(row)">拒绝</t-button>
                <t-button variant="text" size="small">查看详情</t-button>
              </div>
            </template>
          </t-table>
        </t-tab-panel>

        <!-- 转正管理 -->
        <t-tab-panel value="regularization" label="转正管理">
          <template #toolbar>
            <t-space><t-tag variant="light" theme="primary">即将到期：{{ regData.filter(r => r.status === 0).length }} 人</t-tag></t-space>
          </template>
          <t-table :data="regData" :columns="regColumns" :hover="true" :row-key="'id'" stripe size="small">
            <template #score="{ row }">
              <span v-if="row.score !== null">{{ row.score }}</span>
              <t-tag v-else theme="default" size="small">未评</t-tag>
            </template>
            <template #statusName="{ row }">
              <t-tag :theme="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'default'" size="small">{{ row.statusName }}</t-tag>
            </template>
            <template #action="{ row }">
              <div class="action-buttons">
                <t-button v-if="row.status === 0" variant="text" size="small" theme="primary" @click="openEvalDialog(row)">评估</t-button>
                <t-button variant="text" size="small">交接清单</t-button>
              </div>
            </template>
          </t-table>
        </t-tab-panel>
      </t-tabs>
    </t-card>

    <!-- 转正评估弹窗 -->
    <t-dialog v-model:visible="evalDialogVisible" header="转正评估" width="550px" :footer="false" placement="center" destroy-on-close>
      <div v-if="currentReg" class="eval-info">
        <p><strong>员工：</strong>{{ currentReg.userName }} | {{ currentReg.deptName }} - {{ currentReg.position }}</p>
        <p><strong>入职日期：</strong>{{ currentReg.entryDate }} | <strong>预计转正：</strong>{{ currentReg.positiveDate }}</p>
      </div>
      <t-form ref="evalFormRef" :data="evalFormData" label-width="90px" layout="vertical" class="eval-form">
        <t-form-item label="综合评分" :rules="[{required:true}]">
          <t-input-number v-model="evalFormData.score" :min="0" :max="100" style="width:200px" />
        </t-form-item>
        <t-form-item label="评估结果">
          <t-radio-group v-model="evalFormData.result">
            <t-radio :value="1">同意转正</t-radio>
            <t-radio :value="2">延长试用</t-radio>
            <t-radio :value="3">不予转正</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="新职级"><t-input v-model="evalFormData.newRank" placeholder="如 P6" /></t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="调整后月薪"><t-input-number v-model="evalFormData.newSalary" :min="0" /></t-form-item>
          </t-col>
        </t-row>
        <t-form-item label="评估备注"><t-textarea v-model="evalFormData.remark" :autosize="{minRows:2,maxRows:4}" placeholder="请填写转正评估意见" /></t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="evalDialogVisible=false">取消</t-button>
        <t-button theme="primary" @click="submitEval">提交评估</t-button>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.resign-page { display: flex; flex-direction: column; gap: 16px; }
.tab-card { flex: 1; overflow: hidden; }
.tab-card :deep(.t-card__body) { padding: 0; }
:::deep(.t-table th), :::deep(.t-table td) { text-align: center; vertical-align: middle; }
.action-buttons { display: flex; justify-content: center; align-items: center; gap: 2px; min-width: 140px; }

.eval-info p { margin: 8px 0; font-size: 14px; color: #333; }
.eval-info { padding: 12px 0; border-bottom: 1px solid #f0f0f0; margin-bottom: 16px; }
.eval-form { padding: 16px 0; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 20px; margin-top: 8px; border-top: 1px solid #e7e7e7; }
</style>
