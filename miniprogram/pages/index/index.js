const { get } = require('../../utils/request')
const { getToday } = require('../../utils/util')

Page({
  data: {
    userInfo: null,
    today: '',
    // 公告列表
    notices: [],
    // 待投票数
    pendingVotes: 0,
    // 数据概览
    overview: {
      workDays: 0,
      normalDays: 0,
      lateCount: 0,
      absentCount: 0
    },
    // 今日打卡状态（多时段）
    todayStatus: {
      checkedIn: false,
      checkedOut: false,
      checkInTime: null,
      checkOutTime: null,
      // 上午+下午是否全部完成
      allDone: false
    }
  },

  onLoad() {
    this.setData({
      today: getToday(),
      userInfo: wx.getStorageSync('userInfo')
    })
  },

  onShow() {
    this.loadNotices()
    this.loadOverview()
    this.loadPendingVotes()
    this.loadTodayStatus()
    this.getTabBar().setData({ value: 'index' })
  },

  onUnload() {
    // 无需清理定时器
  },

  // 加载公告
  async loadNotices() {
    try {
      const res = await get('/mini/notice/list', { limit: 5 })
      if (res.code === 200 && res.data) {
        this.setData({ notices: res.data })
      }
    } catch (e) {
      console.error('获取公告失败:', e)
    }
  },

  // 加载数据概览（本月考勤统计）
  async loadOverview() {
    try {
      const res = await get('/mini/attendance/stats')
      if (res.code === 200 && res.data) {
        this.setData({ overview: res.data })
      }
    } catch (e) {
      console.error('获取概览数据失败:', e)
    }
  },

  // 加载待投票数
  async loadPendingVotes() {
    try {
      const res = await get('/mini/vote/pending-count')
      if (res.code === 200) {
        this.setData({ pendingVotes: res.data || 0 })
      }
    } catch (e) {
      console.error('获取待投票数失败:', e)
    }
  },

  // 加载今日打卡状态（多时段）
  async loadTodayStatus() {
    try {
      const res = await get('/mini/attendance/today')
      if (res.code === 200 && res.data) {
        const records = res.data.records || []
        const allDone = res.data.allDone || false

        // 取上午记录的打卡时间用于展示
        let morningRec = records.find(r => r.shiftCode === 'MORNING') || records[0] || {}

        this.setData({
          todayStatus: {
            checkedIn: !!morningRec.checkedIn,
            checkedOut: !!morningRec.checkedOut,
            checkInTime: morningRec.checkInTime || null,
            checkOutTime: morningRec.checkOutTime || null,
            allDone: allDone
          }
        })
      }
    } catch (e) {
      console.error('获取今日打卡状态失败:', e)
    }
  },

  // 跳转公告详情
  goToNoticeDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/notice/detail?id=${id}` })
  },

  // 跳转打卡
  goToClock() {
    wx.switchTab({ url: '/pages/clock/clock' })
  },

  // 跳转投票
  goToVote() {
    wx.switchTab({ url: '/pages/vote/vote' })
  },

  // 下拉刷新
  async onPullDownRefresh() {
    await Promise.all([
      this.loadNotices(),
      this.loadOverview(),
      this.loadPendingVotes(),
      this.loadTodayStatus()
    ])
    wx.stopPullDownRefresh()
  }
})
