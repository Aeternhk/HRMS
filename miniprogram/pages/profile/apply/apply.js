const { get, post } = require('../../../utils/request')
const { getToday } = require('../../../utils/util')

Page({
  data: {
    activeTab: 'leave',
    leaveTypes: [
      { value: 'ANNUAL', label: '年假' },
      { value: 'PERSONAL', label: '事假' },
      { value: 'SICK', label: '病假' },
      { value: 'MARRIAGE', label: '婚假' },
      { value: 'MATERNITY', label: '产假' },
      { value: 'FUNERAL', label: '丧假' }
    ],
    leaveTypeLabel: '年假',
    leaveForm: {
      type: 'ANNUAL',
      startDate: '',
      endDate: '',
      days: 1,
      reason: ''
    },
    overtimeForm: {
      date: '',
      startTime: '',
      endTime: '',
      hours: 0,
      reason: ''
    },
    applications: [],
    loading: false,
    submitting: false
  },

  onLoad() {
    const today = getToday()
    this.setData({
      'leaveForm.startDate': today,
      'leaveForm.endDate': today,
      'overtimeForm.date': today
    })
    this.loadApplications()
  },

  // 切换标签
  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    if (tab === 'history') {
      this.loadApplications()
    }
  },

  // 加载申请记录
  async loadApplications() {
    this.setData({ loading: true })
    try {
      const res = await get('/mini/application/list')
      if (res.code === 200 && res.data) {
        // 预处理类型和状态文本（数据驱动，避免WXML调用方法）
        const typeMap = { 'LEAVE': '请假', 'OVERTIME': '加班' }
        const statusMap = { 'PENDING': '待审批', 'APPROVED': '已通过', 'REJECTED': '已拒绝' }
        const list = (res.data.list || []).map(item => ({
          ...item,
          _typeText: typeMap[item.type] || item.type,
          _statusText: statusMap[item.status] || item.status
        }))
        this.setData({
          applications: list,
          loading: false
        })
      }
    } catch (error) {
      console.error('获取申请记录失败:', error)
      this.setData({ loading: false })
    }
  },

  // 请假类型选择
  onLeaveTypeChange(e) {
    const index = e.detail.value
    const type = this.data.leaveTypes[index].value
    const label = this.data.leaveTypes[index].label
    this.setData({ 
      'leaveForm.type': type,
      leaveTypeLabel: label
    })
  },

  // 请假开始日期
  onStartDateChange(e) {
    this.setData({ 'leaveForm.startDate': e.detail.value })
    this.calculateLeaveDays()
  },

  // 请假结束日期
  onEndDateChange(e) {
    this.setData({ 'leaveForm.endDate': e.detail.value })
    this.calculateLeaveDays()
  },

  // 计算请假天数
  calculateLeaveDays() {
    const { startDate, endDate } = this.data.leaveForm
    if (startDate && endDate) {
      const start = new Date(startDate)
      const end = new Date(endDate)
      const diffTime = Math.abs(end - start)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
      this.setData({ 'leaveForm.days': diffDays })
    }
  },

  // 请假原因输入
  onLeaveReasonInput(e) {
    this.setData({ 'leaveForm.reason': e.detail.value })
  },

  // 加班日期
  onOvertimeDateChange(e) {
    this.setData({ 'overtimeForm.date': e.detail.value })
  },

  // 加班开始时间
  onOvertimeStartChange(e) {
    this.setData({ 'overtimeForm.startTime': e.detail.value })
    this.calculateOvertimeHours()
  },

  // 加班结束时间
  onOvertimeEndChange(e) {
    this.setData({ 'overtimeForm.endTime': e.detail.value })
    this.calculateOvertimeHours()
  },

  // 计算加班时长
  calculateOvertimeHours() {
    const { startTime, endTime } = this.data.overtimeForm
    if (startTime && endTime) {
      const start = new Date(`2000-01-01 ${startTime}`)
      const end = new Date(`2000-01-01 ${endTime}`)
      const diffTime = Math.abs(end - start)
      const diffHours = Math.round(diffTime / (1000 * 60 * 60) * 10) / 10
      this.setData({ 'overtimeForm.hours': diffHours })
    }
  },

  // 加班原因输入
  onOvertimeReasonInput(e) {
    this.setData({ 'overtimeForm.reason': e.detail.value })
  },

  // 提交请假申请
  async submitLeave() {
    const { type, startDate, endDate, days, reason } = this.data.leaveForm

    if (!reason.trim()) {
      wx.showToast({ title: '请输入请假原因', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    try {
      const res = await post('/mini/leave/apply', {
        type,
        startDate,
        endDate,
        days,
        reason: reason.trim()
      })

      if (res.code === 200) {
        wx.showToast({
          title: '申请提交成功',
          icon: 'success',
          complete: () => {
            setTimeout(() => {
              this.setData({
                activeTab: 'history',
                'leaveForm.reason': ''
              })
              this.loadApplications()
            }, 1500)
          }
        })
      }
    } catch (error) {
      console.error('提交请假申请失败:', error)
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 提交加班申请
  async submitOvertime() {
    const { date, startTime, endTime, hours, reason } = this.data.overtimeForm

    if (!reason.trim()) {
      wx.showToast({ title: '请输入加班原因', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    try {
      const res = await post('/mini/overtime/apply', {
        date,
        startTime,
        endTime,
        hours,
        reason: reason.trim()
      })

      if (res.code === 200) {
        wx.showToast({
          title: '申请提交成功',
          icon: 'success',
          complete: () => {
            setTimeout(() => {
              this.setData({
                activeTab: 'history',
                'overtimeForm.reason': ''
              })
              this.loadApplications()
            }, 1500)
          }
        })
      }
    } catch (error) {
      console.error('提交加班申请失败:', error)
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 获取申请类型文本
  getApplicationTypeText(type) {
    const map = {
      'LEAVE': '请假',
      'OVERTIME': '加班'
    }
    return map[type] || type
  },

  // 获取状态文本
  getStatusText(status) {
    const map = {
      'PENDING': '待审批',
      'APPROVED': '已通过',
      'REJECTED': '已拒绝'
    }
    return map[status] || status
  }
})
