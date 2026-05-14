const { get } = require('../../../utils/request')
const { maskPhone, maskIdCard } = require('../../../utils/util')

Page({
  data: {
    userInfo: null,
    loading: false
  },

  onLoad() {
    this.loadUserInfo()
  },

  // 加载用户信息
  async loadUserInfo() {
    this.setData({ loading: true })
    try {
      const res = await get('/mini/user/profile')
      if (res.code === 200 && res.data) {
        // 预处理脱敏字段（数据驱动，避免WXML调用方法）
        const u = res.data
        u._maskedPhone = maskPhone(u.phone)
        u._maskedIdCard = maskIdCard(u.idCard)
        u._maskedEmergencyPhone = maskPhone(u.emergencyPhone)

        this.setData({
          userInfo: u,
          loading: false
        })
        // 更新本地存储
        wx.setStorageSync('userInfo', res.data)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      this.setData({ loading: false })
    }
  },

  // 下拉刷新
  async onPullDownRefresh() {
    await this.loadUserInfo()
    wx.stopPullDownRefresh()
  }
})
