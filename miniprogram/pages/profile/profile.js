const { get } = require('../../utils/request')
const { formatMoney } = require('../../utils/util')

/**
 * 预格式化工资条金额字段（数据驱动，避免WXML调用方法）
 */
function formatLatestSalary(s) {
  if (!s) return null
  return {
    ...s,
    _fmtNetSalary: formatMoney(s.netSalary),
    _fmtGrossSalary: formatMoney(s.grossSalary),
    _fmtDeductions: formatMoney(s.deductions)
  }
}

Page({
  data: {
    userInfo: null,
    latestSalary: null,
    salaryLoaded: false,
    pendingApplications: 0,
    approvedApplications: 0,
    rejectedApplications: 0
  },

  onLoad() {
    this.loadUserInfo()
  },

  onShow() {
    this.loadUserInfo()
    this.loadLatestSalary()
    this.loadApplicationStats()
    this.getTabBar().setData({ value: 'profile' })
  },

  // 加载用户信息
  loadUserInfo() {
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.setData({ userInfo })
    }
  },

  // 加载最新工资条
  async loadLatestSalary() {
    try {
      const res = await get('/mini/salary/latest')
      if (res.code === 200 && res.data) {
        this.setData({ latestSalary: formatLatestSalary(res.data), salaryLoaded: true })
      } else {
        this.setData({ salaryLoaded: true })
      }
    } catch (error) {
      console.error('获取工资条失败:', error)
      this.setData({ salaryLoaded: true })
    }
  },

  // 加载申请统计（待审批/已通过/未通过）
  async loadApplicationStats() {
    try {
      const res = await get('/mini/application/list')
      if (res.code === 200 && res.data && res.data.list) {
        const list = res.data.list
        const pending  = list.filter(i => i.status === 'PENDING').length
        const approved = list.filter(i => i.status === 'APPROVED').length
        const rejected = list.filter(i => i.status === 'REJECTED').length
        this.setData({ pendingApplications: pending, approvedApplications: approved, rejectedApplications: rejected })
      }
    } catch (error) {
      console.error('获取申请统计失败:', error)
    }
  },

  // 跳转到个人信息
  goToInfo() {
    wx.navigateTo({ url: '/pages/profile/info/info' })
  },

  // 跳转到工资条
  goToSalary() {
    wx.navigateTo({ url: '/pages/profile/salary/salary' })
  },

  // 跳转到申请页面
  goToApply() {
    wx.navigateTo({ url: '/pages/profile/apply/apply' })
  },

  // 跳转到设置
  goToSettings() {
    wx.navigateTo({ url: '/pages/profile/settings/settings' })
  },

  // 退出登录
  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.showToast({
            title: '已退出登录',
            icon: 'success',
            complete: () => {
              setTimeout(() => {
                wx.redirectTo({ url: '/pages/login/login' })
              }, 1500)
            }
          })
        }
      }
    })
  },
})
