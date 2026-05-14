<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)

// 用户信息
const userInfo = computed(() => ({
  name: userStore.userName || '管理员',
  role: userStore.userRole || '系统管理员'
}))

// 内联SVG图标映射（摆脱CDN依赖）
// 所有图标使用统一的 viewBox="0 0 24 24"，stroke="currentColor", fill="none", stroke-width=2, stroke-linecap="round"
const svgIcons: Record<string, string> = {
  dashboard: '<rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/>',
  employee: '<path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  attendance: '<circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/>',
  leave: '<rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>',
  overtime: '<circle cx="12" cy="12" r="10"/><polyline points="12,6 12,12 16,14"/><path d="M5 3l-2 2"/><path d="M19 3l2 2"/><path d="M5 21l-2 2"/><path d="M19 21l2 2"/>',
  salary: '<line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>',
  performance: '<line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>',
  contract: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>',
  recruit: '<path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="8.5" cy="7" r="4"/><line x1="20" y1="8" x2="20" y2="14"/><line x1="23" y1="11" x2="17" y2="11"/>',
  resignation: '<polyline points="17,1 21,5 17,9"/><path d="M3 11V9a4 4 0 014-4h14"/><polyline points="7,23 3,19 7,15"/><path d="M21 13v2a4 4 0 01-4 4H3"/>',
  report: '<path d="M21.21 15.89A10 10 0 118 2.83"/><path d="M22 12A10 10 0 0012 2v10z"/>',
  vote: '<path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/>',
  notice: '<path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>',
  department: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>',
  dict: '<path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/>',
  log: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><circle cx="10" cy="9" r="1" fill="currentColor" stroke="none"/>',
  system: '<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>',
  // 辅助图标
  menu: '<line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>',
  bell: '<path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/>',
  'check-circle': '<path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22,4 12,14.01 9,11.01"/>',
  chat: '<path d="M21 11.5a8.38 8.38 0 01-.9 3.8 8.5 8.5 0 01-7.6 4.7 8.38 8.38 0 01-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 01-.9-3.8 8.5 8.5 0 014.7-7.6 8.38 8.38 0 013.8-.9h.5a8.48 8.48 0 018 8v.5z"/>',
  'error-circle': '<circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>',
  'info-circle': '<circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>',
}

// 菜单配置 - 使用内联图标key
const menuList = [
  { value: 'dashboard', label: '数据看板', iconKey: 'dashboard', path: '/dashboard' },
  { value: 'employee', label: '员工管理', iconKey: 'employee', path: '/employee' },
  { value: 'attendance', label: '考勤管理', iconKey: 'attendance', path: '/attendance' },
  { value: 'leave', label: '请假管理', iconKey: 'leave', path: '/leave' },
  { value: 'overtime', label: '加班管理', iconKey: 'overtime', path: '/overtime' },
  { value: 'salary', label: '薪资管理', iconKey: 'salary', path: '/salary' },
  { value: 'performance', label: '绩效考核', iconKey: 'performance', path: '/performance' },
  { value: 'contract', label: '合同管理', iconKey: 'contract', path: '/contract' },
  { value: 'recruit', label: '招聘管理', iconKey: 'recruit', path: '/recruit' },
  { value: 'resignation', label: '离职转正', iconKey: 'resignation', path: '/resignation' },
  { value: 'report', label: '统计报表', iconKey: 'report', path: '/report' },
  { value: 'vote', label: '投票管理', iconKey: 'vote', path: '/vote' },
  { value: 'notice', label: '公告管理', iconKey: 'notice', path: '/notice' },
  { value: 'department', label: '部门管理', iconKey: 'department', path: '/department' },
  { value: 'dict', label: '数据字典', iconKey: 'dict', path: '/dict' },
  { value: 'log', label: '操作日志', iconKey: 'log', path: '/log' },
  { value: 'system', label: '系统管理', iconKey: 'system', path: '/system' }
]

// 获取完整SVG图标（包裹必要的svg标签）
const getSvgIcon = (key: string): string => {
  const content = svgIcons[key]
  if (!content) return ''
  return `<svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${content}</svg>`
}

// 当前激活菜单
const activeMenu = computed(() => route.path.replace('/', ''))

// 切换侧边栏
const toggleSidebar = () => {
  collapsed.value = !collapsed.value
}

// 登出
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

// 跳转到菜单
const navigateTo = (path: string) => {
  router.push(path)
}

// 消息通知相关
const notificationVisible = ref(false)
const notificationList = ref([
  { id: 1, title: '新员工入职申请', content: '张三提交了入职申请，请及时审批', time: dayjs().subtract(5, 'minute').toDate(), type: 'approval', read: false },
  { id: 2, title: '请假申请待审批', content: '李四提交了请假申请，时长3天', time: dayjs().subtract(30, 'minute').toDate(), type: 'approval', read: false },
  { id: 3, title: '新投票通知', content: '关于2024年度考核方案的投票已开始', time: dayjs().subtract(2, 'hour').toDate(), type: 'vote', read: false },
  { id: 4, title: '合同到期提醒', content: '王五的劳动合同将于30天后到期', time: dayjs().subtract(1, 'day').toDate(), type: 'warning', read: true },
  { id: 5, title: '系统更新通知', content: 'HRMS系统将于本周六进行版本更新', time: dayjs().subtract(2, 'day').toDate(), type: 'info', read: true }
])

const unreadCount = computed(() => notificationList.value.filter(n => !n.read).length)

// 打开通知弹窗
const openNotification = () => {
  notificationVisible.value = true
}

// 关闭通知弹窗
const closeNotification = () => {
  notificationVisible.value = false
}

// 标记单条已读
const markAsRead = (id: number) => {
  const item = notificationList.value.find(n => n.id === id)
  if (item) {
    item.read = true
  }
}

// 全部标记已读
const markAllAsRead = () => {
  notificationList.value.forEach(item => {
    item.read = true
  })
}

// 点击通知项
const handleNotificationClick = (item: any) => {
  markAsRead(item.id)
  closeNotification()
  if (item.type === 'approval') {
    router.push('/attendance')
  } else if (item.type === 'vote') {
    router.push('/vote')
  } else if (item.type === 'warning') {
    router.push('/employee')
  }
}

// 格式化时间
const formatTime = (date: Date) => {
  const now = dayjs()
  const diff = now.diff(dayjs(date), 'minute')
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return `${Math.floor(diff / 1440)}天前`
}
</script>

<template>
  <t-layout class="main-layout">
    <!-- 侧边栏 -->
    <t-aside :class="['sidebar', { 'sidebar-collapsed': collapsed }]">
      <!-- Logo区域 -->
      <div class="logo-area">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48">
            <circle cx="24" cy="24" r="22" fill="#0052d9" opacity="0.1"/>
            <path d="M24 4C12.95 4 4 12.95 4 24s8.95 20 20 20 20-8.95 20-20S35.05 4 24 4zm0 36c-8.82 0-16-7.18-16-16S15.18 8 24 8s16 7.18 16 16-7.18 16-16 16z" fill="#0052d9"/>
            <path d="M24 14c-5.52 0-10 4.48-10 10s4.48 10 10 10 10-4.48 10-10-4.48-10-10-10zm0 16c-3.31 0-6-2.69-6-6s2.69-6 6-6 6 2.69 6 6-2.69 6-6 6z" fill="#0052d9"/>
          </svg>
        </div>
        <span class="logo-text">HRMS</span>
      </div>

      <!-- 菜单 -->
      <t-menu
        :default-value="activeMenu"
        :collapsed="collapsed"
        @change="(val: string | number) => {
          const item = menuList.find(m => m.value === String(val))
          if (item) navigateTo(item.path)
        }"
      >
        <t-menu-item v-for="item in menuList" :key="item.value" :value="item.value">
          <template #icon>
            <span class="menu-icon-svg" v-html="getSvgIcon(item.iconKey)"></span>
          </template>
          <span>{{ item.label }}</span>
        </t-menu-item>
      </t-menu>
    </t-aside>

    <!-- 主内容区 -->
    <t-layout class="main-content">
      <!-- 顶部导航 -->
      <t-header height="56" class="header">
        <div class="header-left">
          <t-button variant="text" @click="toggleSidebar">
            <template #icon>
              <span class="menu-icon-svg" v-html="getSvgIcon('menu')"></span>
            </template>
          </t-button>
          <t-breadcrumb>
            <t-breadcrumb-item>{{ menuList.find(m => m.value === activeMenu)?.label || '首页' }}</t-breadcrumb-item>
          </t-breadcrumb>
        </div>
        <div class="header-right">
          <t-space>
            <t-badge :count="unreadCount" :max-count="99">
              <t-button variant="text" @click="openNotification">
                <template #icon>
                  <span class="menu-icon-svg" v-html="getSvgIcon('bell')"></span>
                </template>
              </t-button>
            </t-badge>
            <t-dropdown trigger="click">
              <t-space class="user-info">
                <t-avatar size="small">{{ userInfo.name.slice(0, 1) }}</t-avatar>
                <span class="user-name">{{ userInfo.name }}</span>
              </t-space>
              <template #dropdown>
                <t-dropdown-menu>
                  <t-dropdown-item @click="handleLogout">退出登录</t-dropdown-item>
                </t-dropdown-menu>
              </template>
            </t-dropdown>
          </t-space>
        </div>
      </t-header>

      <!-- 内容区域 -->
      <t-content class="content">
        <router-view />
      </t-content>

      <!-- 消息通知弹窗 -->
      <t-dialog
        v-model:visible="notificationVisible"
        header="消息通知"
        width="560px"
        :footer="false"
        placement="center"
      >
        <div class="notification-container">
          <div class="notification-header">
            <span class="notification-tip">您有 {{ unreadCount }} 条未读消息</span>
            <t-button variant="text" size="small" @click="markAllAsRead" :disabled="unreadCount === 0">
              全部已读
            </t-button>
          </div>
          <div class="notification-list">
            <div
              v-for="item in notificationList"
              :key="item.id"
              class="notification-item"
              :class="{ unread: !item.read }"
              @click="handleNotificationClick(item)"
            >
              <div class="notification-icon" :class="item.type">
                <span class="menu-icon-svg" :style="{ color: item.type === 'approval' ? '#1890ff' : item.type === 'vote' ? '#fa8c16' : item.type === 'warning' ? '#ff4d4f' : '#52c41a' }" v-html="getSvgIcon(item.type === 'approval' ? 'check-circle' : item.type === 'vote' ? 'chat' : item.type === 'warning' ? 'error-circle' : 'info-circle')"></span>
              </div>
              <div class="notification-content">
                <div class="notification-title">{{ item.title }}</div>
                <div class="notification-text">{{ item.content }}</div>
                <div class="notification-time">{{ formatTime(item.time) }}</div>
              </div>
              <div v-if="!item.read" class="unread-dot"></div>
            </div>
            <div v-if="notificationList.length === 0" class="notification-empty">
              <span class="menu-icon-svg" style="font-size: 48px;" v-html="getSvgIcon('check-circle')"></span>
              <span>暂无消息</span>
            </div>
          </div>
        </div>
      </t-dialog>
    </t-layout>
  </t-layout>
</template>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
  display: flex;
}

.sidebar {
  background: #ffffff;
  border-right: 1px solid #e7e7e7;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: visible;
  flex-shrink: 0;
  width: 240px;
  margin-left: 0;
}

.sidebar-collapsed {
  width: 72px !important;
}

.sidebar-collapsed .logo-area {
  padding: 0 20px;
  justify-content: center;
}

.sidebar-collapsed .logo-text {
  display: none;
}

.logo-area {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #e7e7e7;
  flex-shrink: 0;
  width: 100%;
  box-sizing: border-box;
}

.logo-icon {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 100%;
  height: 100%;
}

.logo-text {
  margin-left: 12px;
  font-size: 20px;
  font-weight: 700;
  color: #0052d9;
  white-space: nowrap;
  overflow: hidden;
  transition: opacity 0.3s ease;
}

/* 菜单内联SVG图标 */
.menu-icon-svg {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  margin-right: 18px;
  flex-shrink: 0;
}

.menu-icon-svg svg {
  width: 100%;
  height: 100%;
}

/* 菜单项样式 - 增大图标和文字 */
:deep(.t-menu) {
  font-size: 16px;
}

:deep(.t-menu-item) {
  height: 52px !important;
  line-height: 52px !important;
  padding: 0 24px !important;
  font-size: 15px !important;
}

::deep(.t-menu-item .menu-icon-svg) {
    font-size: 20px !important;
    margin-right: 18px !important;
    flex-shrink: 0;
}

::deep(.t-menu-item:hover) {
}

:deep(.t-menu-item.t-is-active) {
  background: #e6f0ff !important;
  color: #0052d9 !important;
  font-weight: 500 !important;
}

:deep(.t-menu-item.t-is-active .menu-icon-svg svg) {
  color: #0052d9 !important;
}

:deep(.t-menu__group-title) {
  font-size: 13px !important;
  padding: 16px 24px 8px !important;
}

:deep(.t-submenu__title) {
  height: 52px !important;
  line-height: 52px !important;
  padding: 0 24px !important;
  font-size: 15px !important;
}

:deep(.t-submenu__title .menu-icon-svg) {
  font-size: 22px !important;
  margin-right: 14px !important;
}

.header {
  background: #ffffff;
  border-bottom: 1px solid #e7e7e7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.content {
  margin: 16px;
  background: transparent;
  overflow-y: auto;
  flex: 1;
  overflow-x: hidden;
}

/* 响应式 */
@media (max-width: 768px) {
  .logo-text,
  .user-name {
    display: none;
  }

  .content {
    margin: 12px;
  }
}

/* 通知弹窗样式 */
.notification-container {
  max-height: 450px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #e7e7e7;
  margin-bottom: 8px;
  flex-shrink: 0;
}

.notification-tip {
  font-size: 13px;
  color: #666;
}

.notification-list {
  max-height: 380px;
  overflow-y: auto;
  overflow-x: hidden;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
  margin: 0 -8px;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.unread {
  background: #f0f7ff;
}

.notification-item.unread:hover {
  background: #e6f0ff;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon.approval {
  background: #e6f7ff;
  color: #1890ff;
}

.notification-icon.vote {
  background: #fff7e6;
  color: #fa8c16;
}

.notification-icon.warning {
  background: #fff1f0;
  color: #ff4d4f;
}

.notification-icon.info {
  background: #f6ffed;
  color: #52c41a;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.notification-text {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  position: absolute;
  top: 12px;
  right: 0;
}

.notification-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #999;
  gap: 8px;
}

.notification-empty .t-icon {
  font-size: 48px;
}
</style>