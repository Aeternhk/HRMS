<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { MessagePlugin, DialogPlugin } from 'tdesign-vue-next'
import dayjs from 'dayjs'
import type { RecruitPosition, Candidate, Interview } from '@/api/modules/recruit'

// Tab切换
const activeTab = ref('position')

// ==================== 职位管理 ====================
const positionLoading = ref(false)
const positionList = ref<RecruitPosition[]>([])
const positionTotal = ref(0)

const positionColumns = [
  { colKey: 'title', title: '职位名称', width: '160' },
  { colKey: 'departmentName', title: '部门', width: '100' },
  { colKey: 'recruitCount', title: '人数', width: '60', align: 'center' },
  { colKey: 'jobType', title: '类型', width: '70', align: 'center' },
  { colKey: 'experience', title: '经验要求', width: '90' },
  { colKey: 'education', title: '学历', width: '70' },
  { colKey: 'salaryMin', title: '薪资范围', width: '120' },
  { colKey: 'statusName', title: '状态', width: '80', align: 'center' },
  { colKey: 'deadline', title: '截止日期', width: '110' },
  { colKey: 'action', title: '操作', width: '180', align: 'center', fixed: 'right' }
]

const loadPositions = async () => {
  positionLoading.value = true
  try {
    const res: any = await import('@/api/modules/recruit').then(m => m.getPositionList({ page: 1, pageSize: 50 }))
    if (res.data?.code === 200) {
      positionList.value = res.data.data.list || []
      positionTotal.value = res.data.data.total || 0
    } else {
      loadMockPositions()
    }
  } catch {
    loadMockPositions()
  } finally {
    positionLoading.value = false
  }
}

const loadMockPositions = () => {
  positionList.value = [
    { id: 1, title: 'Java后端工程师', departmentId: 4, departmentName: '研发部', recruitCount: 2, jobType: '全职', experience: '3-5年', education: '本科', salaryMin: 12000, salaryMax: 20000, location: '深圳南山区', status: 1, statusName: '招聘中', publishTime: '2026-04-08 10:00', deadline: '2026-06-30' },
    { id: 2, title: '前端开发工程师', departmentId: 4, departmentName: '研发部', recruitCount: 1, jobType: '全职', experience: '1-3年', education: '本科', salaryMin: 10000, salaryMax: 18000, location: '深圳南山区', status: 1, statusName: '招聘中', publishTime: '2026-04-08 10:00', deadline: '2026-05-31' },
    { id: 3, title: '品质工程师(IQC)', departmentId: 201, departmentName: 'IQC', recruitCount: 1, jobType: '全职', experience: '1-3年', education: '大专', salaryMin: 6000, salaryMax: 9000, location: '东莞塘厦', status: 1, statusName: '招聘中', publishTime: '2026-04-08 09:30', deadline: '2026-04-30' },
    { id: 4, title: '生产主管(SMT)', departmentId: 101, departmentName: 'SMT车间', recruitCount: 1, jobType: '全职', experience: '5年以上', education: '大专', salaryMin: 8000, salaryMax: 12000, location: '东莞塘厦', status: 2, statusName: '已暂停', publishTime: null, deadline: '2026-03-01' }
  ]
  positionTotal.value = positionList.value.length
}

// 职位弹窗
const posDialogVisible = ref(false)
const posDialogTitle = ref('新增职位')
const posFormRef = ref()
const isEditingPos = ref(false)
const formDataPos = ref({
  title: '', departmentId: undefined as number | undefined, recruitCount: 1,
  jobType: 'fulltime', experience: '', education: '',
  salaryMin: undefined as number | undefined, salaryMax: undefined as number | undefined,
  location: '', description: '', requirements: '', benefits: '',
  deadline: '' as string
})

const openAddPositionDialog = () => {
  posDialogTitle.value = '新增职位'; isEditingPos.value = false
  formDataPos.value = { title: '', departmentId: undefined, recruitCount: 1, jobType: 'fulltime', experience: '', education: '', salaryMin: undefined, salaryMax: undefined, location: '', description: '', requirements: '', benefits: '', deadline: '' }
  posDialogVisible.value = true
}

const submitPosition = async () => {
  try {
    await posFormRef.value?.validate()
    if (isEditingPos.value) {
      MessagePlugin.success('更新成功')
    } else {
      positionList.value.unshift({
        id: Date.now(), title: formDataPos.title, departmentId: formDataPos.departmentId || 0,
        departmentName: getDeptName(formDataPos.departmentId || 0), recruitCount: formDataPos.recruitCount,
        jobType: formDataPos.jobType === 'fulltime' ? '全职' : formDataPos.jobType === 'parttime' ? '兼职' : '实习',
        experience: formDataPos.experience, education: formDataPos.education,
        salaryMin: formDataPos.salaryMin || 0, salaryMax: formDataPos.salaryMax || 0,
        location: formDataPos.location, status: 0, statusName: '草稿',
        publishTime: null, deadline: formDataPos.deadline
      })
      positionTotal.value++
      MessagePlugin.success('创建成功')
    }
    posDialogVisible.value = false
  } catch {}
}

const publishPosition = (row: RecruitPosition) => {
  DialogPlugin.confirm({ title: '发布确认', content: `确定发布「${row.title}」？`, onConfirm: () => {
    row.status = 1; row.statusName = '招聘中'; row.publishTime = dayjs().format('YYYY-MM-DD HH:mm')
    MessagePlugin.success('已发布')
  }})
}

const deletePosition = (row: RecruitPosition) => {
  DialogPlugin.confirm({ title: '确认删除', content: `删除「${row.title}」？`, theme: 'danger', onConfirm: () => {
    positionList.value = positionList.value.filter(p => p.id !== row.id); positionTotal.value--
    MessagePlugin.success('已删除')
  }})
}

// ==================== 候选人管理 ====================
const candLoading = ref(false)
const candidateList = ref<Candidate[]>([])
const candTotal = ref(0)

const candidateColumns = [
  { colKey: 'name', title: '姓名', width: '80' },
  { colKey: 'positionTitle', title: '应聘职位', width: '140', ellipsis: true },
  { colKey: 'phone', title: '电话', width: '120' },
  { colKey: 'education', title: '学历', width: '70' },
  { colKey: 'experienceYears', title: '工作年限', width: '80', align: 'center' },
  { colKey: 'currentPosition', title: '目前职位', ellipsis: true, width: '130' },
  { colKey: 'expectedSalary', title: '期望薪资', width: '90', align: 'center' },
  { colKey: 'source', title: '来源', width: '70', align: 'center' },
  { colKey: 'statusName', title: '状态', width: '80', align: 'center' },
  { colKey: 'createTime', title: '投递时间', width: '150' },
  { colKey: 'action', title: '操作', width: '200', align: 'center', fixed: 'right' }
]

const loadCandidates = async () => {
  candLoading.value = true
  try {
    const res: any = await import('@/api/modules/recruit').then(m => m.getCandidateList({ page: 1, pageSize: 50 }))
    if (res.data?.code === 200) {
      candidateList.value = res.data.data.list || []; candTotal.value = res.data.data.total || 0
    } else { loadMockCandidates() }
  } catch { loadMockCandidates() } finally { candLoading.value = false }
}

const loadMockCandidates = () => {
  candidateList.value = [
    { id: 1, positionId: 1, positionTitle: 'Java后端工程师', name: '陈明', gender: '男', phone: '138****8010', email: 'chenming@email.com', age: 28, education: '本科', school: '深圳大学', major: '计算机科学', experienceYears: 6, currentCompany: '某科技公司', currentPosition: '高级开发工程师', expectedSalary: 18000, source: '网站投递', selfIntro: '5年后端开发经验，擅长微服务架构设计', status: 1, statusName: '初筛通过', remark: '', createTime: '2026-04-07 14:20' },
    { id: 2, positionId: 1, positionTitle: 'Java后端工程师', name: '林小红', gender: '女', phone: '138****8011', email: 'linxh@email.com', age: 26, education: '硕士', school: '华中科技大学', major: '软件工程', experienceYears: 3, currentCompany: '某互联网公司', currentPosition: 'Java开发工程师', expectedSalary: 15000, source: '校园招聘', selfIntro: '2025届硕士，在校期间参与多个项目', status: 0, statusName: '待筛选', remark: '', createTime: '2026-04-06 16:30' },
    { id: 3, positionId: 2, positionTitle: '前端开发工程师', name: '周杰', gender: '男', phone: '138****8012', email: 'zhoujie@email.com', age: 29, education: '本科', school: '武汉大学', major: '电子信息', experienceYears: 5, currentCompany: '某大厂', currentPosition: '前端技术专家', expectedSalary: 25000, source: '猎头推荐', selfIntro: '7年前端经验，React/Vue都精通', status: 1, statusName: '初筛通过', remark: '', createTime: '2026-04-05 11:15' },
    { id: 4, positionId: 3, positionTitle: '品质工程师(IQC)', name: '吴芳', gender: '女', phone: '138****8013', email: 'wufang@email.com', age: 24, education: '大专', school: '东莞理工学院', major: '质量管理', experienceYears: 2, currentCompany: '某电子厂', currentPosition: 'IQC检验员', expectedSalary: 5500, source: '网站投递', selfIntro: '2年品质工作经验，熟悉各种检测设备', status: 1, statusName: '初筛通过', remark: '', createTime: '2026-04-04 09:45' },
    { id: 5, positionId: 4, positionTitle: '生产主管(SMT)', name: '郑强', gender: '男', phone: '138****8014', email: 'zhengqiang@email.com', age: 35, education: '本科', school: '广东工业大学', major: '机械工程', experienceYears: 12, currentCompany: '某制造企业', currentPosition: '生产经理', expectedSalary: 11000, source: '内部推荐', selfIntro: '12年生产管理经验，熟悉精益生产', status: 2, statusName: '面试中', remark: '已完成初面', createTime: '2026-04-01 08:30' }
  ]
  candTotal.value = candidateList.value.length
}

// 候选人详情/状态操作
const detailDialogVisible = ref(false)
const currentCandidate = ref<Candidate | null>(null)

const viewCandidateDetail = (row: Candidate) => {
  currentCandidate.value = row
  detailDialogVisible.value = true
}

const updateCandStatus = (row: Candidate, newStatus: number) => {
  const names: Record<number, string> = { 1: '初筛通过', 2: '进入面试', 3: '录用', 4: '淘汰', 5: '放弃' }
  DialogPlugin.confirm({
    title: '状态变更', content: `确定将「${row.name}」的状态改为「${names[newStatus]}」吗？`,
    onConfirm: () => {
      row.status = newStatus
      row.statusName = names[newStatus] || '未知'
      MessagePlugin.success(`已${names[newStatus]}`)
      if (detailDialogVisible.value && currentCandidate.value?.id === row.id) {
        currentCandidate.value = row
      }
    }
  })
}

const deleteCandidate = (row: Candidate) => {
  DialogPlugin.confirm({ title: '确认删除', content: `删除「${row.name}」的简历？`, theme: 'danger', onConfirm: () => {
    candidateList.value = candidateList.value.filter(c => c.id !== row.id); candTotal.value--
    MessagePlugin.success('已删除')
  }})
}

// 面试安排弹窗
const ivtDialogVisible = ref(false)
const ivtFormRef = ref()
const ivtFormData = ref({
  candidateId: 0, positionId: 0, round: 1, interviewType: 'onsite',
  interviewDate: '', durationMinutes: 60, location: ''
})

const openInterviewDialog = (cand: Candidate) => {
  ivtFormData.value = { candidateId: cand.id, positionId: cand.positionId, round: 1, interviewType: 'onsite', interviewDate: '', durationMinutes: 60, location: '' }
  ivtDialogVisible.value = true
}

const submitInterview = async () => {
  try {
    await ivtFormRef.value?.validate()
    // 更新候选人为面试中
    if (currentCandidate.value) {
      currentCandidate.value.status = 2; currentCandidate.value.statusName = '面试中'
    }
    ivtDialogVisible.value = false; MessagePlugin.success('面试已安排')
  } catch {}
}

// ==================== 面试记录（在候选人详情中查看） ====================
const interviewList = ref<Interview[]>([])

// 辅助函数
const getDeptName = (deptId: number): string => {
  const map: Record<number, string> = { 1: '总公司', 2: '生产部', 3: '品质部', 4: '研发部', 201: 'IQC', 101: 'SMT车间' }
  return map[deptId] || ''
}

onMounted(() => { loadPositions(); loadCandidates(); interviewList.value = [
  { id: 1, candidateId: 1, candidateName: '陈明', positionId: 1, round: 1, roundName: '初面', interviewerId: 2, interviewerName: '李四', interviewType: '现场', interviewDate: '2026-04-15 14:00', durationMinutes: 60, location: '会议室A', score: 85, result: '通过', feedback: '技术扎实，项目经验丰富，沟通能力强', nextRound: 1 },
  { id: 2, candidateId: 3, candidateName: '周杰', positionId: 2, round: 1, roundName: '初面', interviewerId: 2, interviewerName: '李四', interviewType: '现场', interviewDate: '2026-04-16 15:00', durationMinutes: 60, location: '会议室B', score: 90, result: '通过', feedback: '前端功底深厚，对性能优化有深入理解', nextRound: 1 }
]})
</script>

<template>
  <div class="recruit-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <t-card :bordered="false" class="stat-card"><div class="stat-val stat-blue">{{ positionList.filter(p => p.status === 1).length }}</div><div class="stat-lbl">在招职位</div></t-card>
      <t-card :bordered="false" class="stat-card"><div class="stat-val stat-green">{{ candTotal }}</div><div class="stat-lbl">总候选人数</div></t-card>
      <t-card :bordered="false" class="stat-card"><div class="stat-val stat-orange">{{ candidateList.filter(c => c.status === 0).length }}</div><div class="stat-lbl">待筛选</div></t-card>
      <t-card :bordered="false" class="stat-card"><div class="stat-val stat-red">{{ candidateList.filter(c => c.status === 3).length }}</div><div class="stat-lbl">已录用</div></t-card>
    </div>

    <!-- Tabs -->
    <t-card :bordered="false" class="tab-card">
      <t-tabs v-model:value="activeTab">
        <!-- 职位管理 -->
        <t-tab-panel value="position" label="招聘职位">
          <template #toolbar>
            <t-button theme="primary" @click="openAddPositionDialog"><template #icon><t-icon name="add-circle" /></template>发布职位</t-button>
          </template>
          <t-table :data="positionList" :loading="positionLoading" :columns="positionColumns" :hover="true" :row-key="'id'" stripe size="small">
            <template #jobType="{ row }">
              <span>{{ row.jobType === 'fulltime' ? '全职' : row.jobType === 'parttime' ? '兼职' : '实习' }}</span>
            </template>
            <template #salaryMin="{ row }">
              <span>{{ row.salaryMin }}-{{ row.salaryMax }}</span>
            </template>
            <template #statusName="{ row }">
              <t-tag :theme="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'default'" size="small">{{ row.statusName }}</t-tag>
            </template>
            <template #action="{ row }">
              <div class="action-buttons">
                <t-button v-if="row.status === 0" variant="text" size="small" theme="success" @click="publishPosition(row)">发布</t-button>
                <t-button variant="text" size="small" @click="openAddPositionDialog">编辑</t-button>
                <t-button variant="text" size="small" theme="danger" @click="deletePosition(row)">删除</t-button>
              </div>
            </template>
          </t-table>
        </t-tab-panel>

        <!-- 候选人 -->
        <t-tab-panel value="candidate" label="候选人/简历">
          <template #toolbar>
            <t-button theme="primary" @click="() => {}"><template #icon><t-icon name="add-circle" /></template>添加简历</t-button>
          </template>
          <t-table :data="candidateList" :loading="candLoading" :columns="candidateColumns" :hover="true" :row-key="'id'" stripe size="small">
            <template #source="{ row }">
              <t-tag :theme="row.source === 'headhunter' ? 'primary' : row.source === 'campus' ? 'success' : 'default'" size="small" variant="light">
                {{ row.source === 'website' ? '网站' : row.source === 'referral' ? '内推' : row.source === 'headhunter' ? '猎头' : '校园' }}
              </t-tag>
            </template>
            <template #statusName="{ row }">
              <t-tag :theme="row.status === 3 ? 'success' : row.status === 4 ? 'danger' : row.status === 2 ? 'warning' : row.status === 1 ? 'primary' : 'default'" size="small">{{ row.statusName }}</t-tag>
            </template>
            <template #action="{ row }">
              <div class="action-buttons">
                <t-button variant="text" size="small" @click="viewCandidateDetail(row)">详情</t-button>
                <t-button v-if="row.status === 0 || row.status === 1" variant="text" size="small" theme="success" @click="openInterviewDialog(row)">安排面试</t-button>
                <t-button v-if="row.status <= 2" variant="text" size="small" @click="updateCandStatus(row, 3)" theme="success">录用</t-button>
                <t-button v-if="row.status <= 2" variant="text" size="small" theme="danger" @click="updateCandStatus(row, 4)">淘汰</t-button>
                <t-button variant="text" size="small" theme="danger" @click="deleteCandidate(row)">删除</t-button>
              </div>
            </template>
          </t-table>
        </t-tab-panel>

        <!-- 面试记录 -->
        <t-tab-panel value="interview" label="面试记录">
          <t-table :data="interviewList" :hover="true" :row-key="'id'" stripe size="small" :columns="[
            { colKey: 'candidateName', title: '候选人', width: 90 },
            { colKey: 'roundName', title: '轮次', width: 70, align: 'center' },
            { colKey: 'interviewerName', title: '面试官', width: 80 },
            { colKey: 'interviewType', title: '方式', width: 70 },
            { colKey: 'interviewDate', title: '时间', width: 150 },
            { colKey: 'score', title: '评分', width: 70, align: 'center' },
            { colKey: 'result', title: '结果', width: 70, align: 'center' }
          ]">
            <template #result="{ row }">
              <t-tag :theme="row.result === 'pass' ? 'success' : row.result === 'fail' ? 'danger' : 'warning'" size="small">
                {{ row.result === 'pass' ? '通过' : row.result === 'fail' ? '未通过' : '待定' }}
              </t-tag>
            </template>
          </t-table>
        </t-tab-panel>
      </t-tabs>
    </t-card>

    <!-- 新增/编辑职位弹窗 -->
    <t-dialog v-model:visible="posDialogVisible" :header="posDialogTitle" width="650px" :footer="false" placement="center" destroy-on-close>
      <t-form ref="posFormRef" :data="formDataPos" label-width="100px" layout="vertical" class="pos-form">
        <t-row :gutter="16">
          <t-col :span="12">
            <t-form-item label="职位名称" name="title" :rules="[{required:true,message:'请输入'}]">
              <t-input v-model="formDataPos.title" placeholder="如：Java后端工程师" />
            </t-form-item>
          </t-col>
          <t-col :span="12">
            <t-form-item label="所属部门" name="departmentId">
              <t-select v-model="formDataPos.departmentId" placeholder="请选择">
                <t-option :value="4" label="研发部" /><t-option :value="201" label="IQC" />
                <t-option :value="101" label="SMT车间" /><t-option :value="102" label="贴合车间" />
              </t-select>
            </t-form-item>
          </t-col>
        </t-row>
        <t-row :gutter="16">
          <t-col :span="8">
            <t-form-item label="招聘人数">
              <t-input-number v-model="formDataPos.recruitCount" :min="1" style="width:100%" />
            </t-form-item>
          </t-col>
          <t-col :span="8">
            <t-form-item label="工作类型">
              <t-select v-model="formDataPos.jobType">
                <t-option value="fulltime" label="全职" /><t-option value="parttime" label="兼职" /><t-option value="internship" label="实习" />
              </t-select>
            </t-form-item>
          </t-col>
          <t-col :span="8">
            <t-form-item label="学历要求">
              <t-select v-model="formDataPos.education" clearable>
                <t-option value="大专" /><t-option value="本科" /><t-option value="硕士" />
              </t-select>
            </t-form-item>
          </t-col>
        </t-row>
        <t-row :gutter="16">
          <t-col :span="12"><t-form-item label="最低月薪"><t-input-number v-model="formDataPos.salaryMin" :min="0" style="width:100%" /></t-form-item></t-col>
          <t-col :span="12"><t-form-item label="最高月薪"><t-input-number v-model="formDataPos.salaryMax" :min="0" style="width:100%" /></t-form-item></t-col>
        </t-row>
        <t-form-item label="截止日期"><t-date-picker v-model="formDataPos.deadline" enable-time-picker={false} format="YYYY-MM-DD" /></t-form-item>
        <t-form-item label="职位描述"><t-textarea v-model="formDataPos.description" :autosize="{minRows:2,maxRows:4}" /></t-form-item>
        <t-form-item label="任职要求"><t-textarea v-model="formDataPos.requirements" :autosize="{minRows:2,maxRows:4}" /></t-form-item>
        <t-form-item label="福利待遇"><t-textarea v-model="formDataPos.benefits" :autosize="{minRows:2,maxRows:4}" /></t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="posDialogVisible=false">取消</t-button>
        <t-button theme="primary" @click="submitPosition">{{ isEditingPos ? '保存' : '创建' }}</t-button>
      </div>
    </t-dialog>

    <!-- 候选人详情弹窗 -->
    <t-dialog v-model:visible="detailDialogVisible" header="候选人详情" width="700px" :footer="false" placement="center" destroy-on-close>
      <div v-if="currentCandidate" class="cand-detail">
        <t-descriptions :column="2" bordered size="small">
          <t-descriptions-item label="姓名">{{ currentCandidate.name }}</t-descriptions-item>
          <t-descriptions-item label="性别">{{ currentCandidate.gender || '-' }}</t-descriptions-item>
          <t-descriptions-item label="联系电话">{{ currentCandidate.phone }}</t-descriptions-item>
          <t-descriptions-item label="邮箱">{{ currentCandidate.email || '-' }}</t-descriptions-item>
          <t-descriptions-item label="年龄">{{ currentCandidate.age || '-' }}</t-descriptions-item>
          <t-descriptions-item label="学历">{{ currentCandidate.education }}</t-descriptions-item>
          <t-descriptions-item label="毕业院校">{{ currentCandidate.school || '-' }}</t-descriptions-item>
          <t-descriptions-item item label="专业">{{ currentCandidate.major || '-' }}</t-descriptions-item>
          <t-descriptions-item label="工作年限">{{ currentCandidate.experienceYears }}年</t-descriptions-item>
          <t-descriptions-item label="期望薪资">{{ currentCandidate.expectedSalary || '-' }}</t-descriptions-item>
          <t-descriptions-item label="当前公司">{{ currentCandidate.currentCompany || '-' }}</t-descriptions-item>
          <t-descriptions-item label="当前职位">{{ currentCandidate.currentPosition || '-' }}</t-descriptions-item>
          <t-descriptions-item label="来源">{{ currentCandidate.source || '-' }}</t-descriptions-item>
          <t-descriptions-item label="状态">
            <t-tag :theme="currentCandidate.status === 3 ? 'success' : 'primary'" size="small">{{ currentCandidate.statusName }}</t-tag>
          </t-descriptions-item>
        </t-descriptions>
        <div class="self-intro-section">
          <h4 style="margin: 12px 0 6px;">自我介绍</h4>
          <p>{{ currentCandidate.selfIntro || '暂无' }}</p>
        </div>
        <div class="detail-actions" style="margin-top: 16px;">
          <t-space>
            <t-button v-if="currentCandidate.status <= 1" theme="primary" size="small" @click="openInterviewDialog(currentCandidate)">安排面试</t-button>
            <t-button v-if="currentCandidate.status <= 2" theme="success" size="small" @click="updateCandStatus(currentCandidate!, 3)">录用</t-button>
            <t-button v-if="currentCandidate.status <= 2" theme="danger" size="small" @click="updateCandStatus(currentCandidate!, 4)">淘汰</t-button>
          </t-space>
        </div>
      </div>
    </t-dialog>

    <!-- 安排面试弹窗 -->
    <t-dialog v-model:visible="ivtDialogVisible" header="安排面试" width="520px" :footer="false" placement="center" destroy-on-close>
      <t-form ref="ivtFormRef" :data="ivtFormData" label-width="90px" layout="vertical" class="ivt-form">
        <t-form-item label="面试轮次">
          <t-radio-group v-model="ivtFormData.round">
            <t-radio :value="1">初面</t-radio><t-radio :value="2">复试</t-radio><t-radio :value="3">终面</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="面试方式">
          <t-radio-group v-model="ivtFormData.interviewType">
            <t-radio value="onsite">现场</t-radio><t-radio value="video">视频</t-radio><t-radio value="phone">电话</t-radio>
          </t-radio-group>
        </t-form-item>
        <t-form-item label="面试时间" :rules="[{required:true}]">
          <t-date-picker v-model="ivtFormData.interviewDate" enable-time-picker format="YYYY-MM-DD HH:mm" />
        </t-form-item>
        <t-form-item label="时长(分钟)">
          <t-input-number v-model="ivtFormData.durationMinutes" :min="15" :step="15" style="width:160px" />
        </t-form-item>
        <t-form-item label="地点/链接">
          <t-input v-model="ivtFormData.location" placeholder="如：会议室A 或 腾讯会议链接" />
        </t-form-item>
      </t-form>
      <div class="dialog-footer">
        <t-button @click="ivtDialogVisible=false">取消</t-button>
        <t-button theme="primary" @click="submitInterview">确认安排</t-button>
      </div>
    </t-dialog>
  </div>
</template>

<style scoped>
.recruit-page { display: flex; flex-direction: column; gap: 16px; }
.stats-row { display: flex; gap: 16px; }
.stat-card { flex: 1; text-align: center; padding: 16px !important; }
.stat-val { font-size: 28px; font-weight: 700; line-height: 1.2; }
.stat-lbl { font-size: 13px; color: #999; margin-top: 4px; }
.stat-blue { color: #0052d9; } .stat-green { color: #00a870; } .stat-orange { color: #e37318; } .stat-red { color: #e34d59; }

.tab-card { flex: 1; overflow: hidden; }
.tab-card :deep(.t-card__body) { padding: 0; }
:::deep(.t-table th), :::deep(.t-table td) { text-align: center; vertical-align: middle; }
.action-buttons { display: flex; justify-content: center; align-items: center; gap: 2px; min-width: 160px; }
.pos-form, .ivt-form { padding: 16px 0; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; padding-top: 20px; margin-top: 8px; border-top: 1px solid #e7e7e7; }
.cand-detail { max-height: 500px; overflow-y: auto; }
.self-intro-section p { background: #f5f7fa; padding: 12px; border-radius: 6px; margin: 0; color: #666; font-size: 13px; line-height: 1.6; }
.detail-actions { padding-top: 12px; border-top: 1px solid #f0f0f0; }
</style>
