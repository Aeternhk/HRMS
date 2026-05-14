const { get, post } = require('../../utils/request')
const { getToday, getNowTime } = require('../../utils/util')

Page({
  data: {
    today: '',
    currentTime: '',
    location: {
      address: '模拟定位：公司总部',
      latitude: 23.129163,
      longitude: 113.264435
    },
    // 多时段考勤记录（数组）
    shiftRecords: [],
    // 所有班次定义
    shiftList: [],
    // 当前应打时段
    currentShift: null,
    // 全部完成？
    allDone: false,
    // 当前时段的记录（用于按钮显示）
    currentRecord: { checkedIn: false, checkedOut: false },
    loading: false,
    // 定时器ID（用于onUnload时清理）
    _timeTimer: null
  },

  onLoad() {
    this.setData({ today: getToday() })
    this.updateTime()
    this.data._timeTimer = setInterval(() => this.updateTime(), 1000)
  },

  onUnload() {
    if (this.data._timeTimer) {
      clearInterval(this.data._timeTimer)
      this.data._timeTimer = null
    }
  },

  onShow() {
    this.loadShiftInfo()
    this.loadTodayAttendance()
    this.mockLocation()
    this.getTabBar().setData({ value: 'clock' })
  },

  updateTime() {
    this.setData({ currentTime: getNowTime() })
  },

  mockLocation() {
    this.setData({
      location: { address: '模拟定位：公司总部大楼', latitude: 23.129163, longitude: 113.264435 }
    })
  },

  // 加载班次配置（多时段）
  async loadShiftInfo() {
    try {
      const res = await get('/mini/attendance/shift')
      if (res.code === 200 && res.data) {
        // 找出当前时段
        const current = res.data.find(s => s.isCurrent) || res.data[0]
        this.setData({ shiftList: res.data, currentShift: current || {} })
      }
    } catch (e) {
      console.error('获取班次信息失败:', e)
    }
  },

  // 加载今日考勤（多时段）
  async loadTodayAttendance() {
    try {
      const res = await get('/mini/attendance/today')
      if (res.code === 200 && res.data) {
        const records = res.data.records || []
        const curCode = res.data.currentShift || ''
        // 找当前时段的记录
        let curRec = {}
        for (let i = 0; i < records.length; i++) {
          if (records[i].shiftCode === curCode) { curRec = records[i]; break; }
        }
        this.setData({
          shiftRecords: records,
          allDone: res.data.allDone || false,
          currentShift: res.data.currentShift || this.data.currentShift?.code || '',
          currentRecord: curRec || { checkedIn: false, checkedOut: false }
        })
      }
    } catch (e) {
      console.error('获取今日考勤失败:', e)
    }
  },

  // 获取当前时段的记录
  getCurrentRecord() {
    const code = typeof this.data.currentShift === 'string'
      ? this.data.currentShift
      : (this.data.currentShift?.code || '')
    return this.data.shiftRecords.find(r => r.shiftCode === code) || {}
  },

  // 上班打卡
  async onCheckIn() {
    const rec = this.getCurrentRecord()
    if (rec.checkedIn) {
      wx.showToast({ title: `${rec.shiftName || ''}已上班打卡`, icon: 'none' })
      return
    }

    this.setData({ loading: true })

    try {
      const res = await post('/mini/clockIn', {
        type: 'IN',
        location: this.data.location.address,
        latitude: this.data.location.latitude,
        longitude: this.data.location.longitude,
        time: getNowTime(),
        shiftType: this.data.currentShift?.code || ''
      })

      if (res.code === 200) {
        wx.showToast({ title: `${res.data.shiftName}上班打卡成功`, icon: 'success' })
        this.loadTodayAttendance()
      }
    } catch (error) {
      console.error('打卡失败:', error)
    } finally {
      this.setData({ loading: false })
    }
  },

  // 下班打卡
  async onCheckOut() {
    const rec = this.getCurrentRecord()
    if (!rec.checkedIn) {
      wx.showToast({ title: `请先${rec.shiftName || ''}上班打卡`, icon: 'none' })
      return
    }
    if (rec.checkedOut) {
      wx.showToast({ title: `${rec.shiftName || ''}已下班打卡`, icon: 'none' })
      return
    }

    // 早退判定弹窗
    const curShiftDef = this.data.shiftList.find(s => s.code === rec.shiftCode)
    if (curShiftDef && curShiftDef.earlyThreshold) {
      const now = new Date()
      const h = String(now.getHours()).padStart(2, '0')
      const m = String(now.getMinutes()).padStart(2, '0')
      const currentTimeStr = `${h}:${m}`

      if (currentTimeStr < curShiftDef.earlyThreshold) {
        wx.showModal({
          title: '⚠️ 提前下班',
          content: `当前时间 ${currentTimeStr}，早于${rec.shiftName}下班时间 ${curShiftDef.offTime}。确认要提前打卡下班吗？`,
          confirmText: '确认打卡',
          confirmColor: '#E34D59',
          success: (modalRes) => {
            if (modalRes.confirm) this.doCheckOut(rec)
          }
        })
        return
      }
    }

    this.doCheckOut(rec)
  },

  async doCheckOut(rec) {
    this.setData({ loading: true })

    try {
      const res = await post('/mini/clockIn', {
        type: 'OUT',
        location: this.data.location.address,
        latitude: this.data.location.latitude,
        longitude: this.data.location.longitude,
        time: getNowTime(),
        shiftType: rec.shiftCode || (this.data.currentShift?.code || '')
      })

      if (res.code === 200) {
        const status = res.data.status
        wx.showToast({
          title: status === '早退'
            ? `${res.data.shiftName}下班成功（已记录早退）`
            : `${res.data.shiftName}下班打卡成功`,
          icon: status === '早退' ? 'none' : 'success',
          duration: status === '早退' ? 2000 : 1500
        })
        this.loadTodayAttendance()
      }
    } catch (error) {
      console.error('打卡失败:', error)
    } finally {
      this.setData({ loading: false })
    }
  },

  refreshLocation() {
    wx.showLoading({ title: '定位中...' })
    setTimeout(() => {
      this.mockLocation()
      wx.hideLoading()
      wx.showToast({ title: '定位已更新', icon: 'success' })
    }, 1000)
  }
})
