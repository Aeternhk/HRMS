App({
  globalData: {
    userInfo: null,
    token: null,
    baseUrl: 'http://localhost:8081/api'
  },

  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
  }
})
