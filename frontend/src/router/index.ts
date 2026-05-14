import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/login/LoginPage.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/pages/dashboard/DashboardPage.vue'),
        meta: { title: '数据看板', icon: 'dashboard' }
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/pages/employee/EmployeePage.vue'),
        meta: { title: '员工管理', icon: 'user' }
      },
      {
        path: 'department',
        name: 'Department',
        component: () => import('@/pages/department/DepartmentPage.vue'),
        meta: { title: '部门管理', icon: 'office' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/pages/attendance/AttendancePage.vue'),
        meta: { title: '考勤管理', icon: 'attendance' }
      },
      {
        path: 'salary',
        name: 'Salary',
        component: () => import('@/pages/salary/SalaryPage.vue'),
        meta: { title: '薪资管理', icon: 'salary' }
      },
      {
        path: 'performance',
        name: 'Performance',
        component: () => import('@/pages/performance/PerformancePage.vue'),
        meta: { title: '绩效考核', icon: 'chart' }
      },
      {
        path: 'vote',
        name: 'Vote',
        component: () => import('@/pages/vote/VotePage.vue'),
        meta: { title: '投票管理', icon: 'vote' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('@/pages/notice/NoticePage.vue'),
        meta: { title: '公告管理', icon: 'notification' }
      },
      {
        path: 'leave',
        name: 'Leave',
        component: () => import('@/pages/leave/LeavePage.vue'),
        meta: { title: '请假管理', icon: 'calendar' }
      },
      {
        path: 'overtime',
        name: 'Overtime',
        component: () => import('@/pages/overtime/OvertimePage.vue'),
        meta: { title: '加班管理', icon: 'timer' }
      },
      {
        path: 'contract',
        name: 'Contract',
        component: () => import('@/pages/contract/ContractPage.vue'),
        meta: { title: '合同管理', icon: 'file-text' }
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/pages/system/LogPage.vue'),
        meta: { title: '操作日志', icon: 'file-paste' }
      },
      {
        path: 'recruit',
        name: 'Recruit',
        component: () => import('@/pages/recruit/RecruitPage.vue'),
        meta: { title: '招聘管理', icon: 'user-add' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/pages/report/ReportPage.vue'),
        meta: { title: '统计报表', icon: 'chart-bar' }
      },
      {
        path: 'resignation',
        name: 'Resignation',
        component: () => import('@/pages/employee/ResignationPage.vue'),
        meta: { title: '离职转正', icon: 'swap' }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/pages/system/DictPage.vue'),
        meta: { title: '数据字典', icon: 'book' }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/pages/system/SystemPage.vue'),
        meta: { title: '系统管理', icon: 'setting' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
