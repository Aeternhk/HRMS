const { get } = require('../../../utils/request')
const { formatDate } = require('../../../utils/util')

Page({
  data: {
    records: [],
    currentMonth: '',
    monthStats: {
      totalDays: 0,
      normalDays: 0,
      lateDays: 0,
      earlyDays: 0,
      absentDays: 0
    },
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 20
  },

  onLoad() {
    const now = new Date()
    this.setData({
      currentMonth: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    })
    this.loadRecords()
    this.loadMonthStats()
  },

  // 加载打卡记录
  async loadRecords() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const res = await get('/mini/attendance/record', {
        month: this.data.currentMonth,
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      if (res.code === 200 && res.data) {
        const newRecords = res.data.list || []
        this.setData({
          records: this.data.page === 1 ? newRecords : [...this.data.records, ...newRecords],
          hasMore: newRecords.length === this.data.pageSize,
          loading: false
        })
      }
    } catch (error) {
      console.error('获取打卡记录失败:', error)
      this.setData({ loading: false })
    }
  },

  // 加载月度统计
  async loadMonthStats() {
    try {
      const res = await get('/mini/attendance/month-stats', {
        month: this.data.currentMonth
      })

      if (res.code === 200 && res.data) {
        this.setData({ monthStats: res.data })
      }
    } catch (error) {
      console.error('获取月度统计失败:', error)
    }
  },

  // 切换月份
  onMonthChange(e) {
    this.setData({
      currentMonth: e.detail.value,
      page: 1,
      records: []
    })
    this.loadRecords()
    this.loadMonthStats()
  },

  // 加载更多
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ page: this.data.page + 1 })
    this.loadRecords()
  }
})
