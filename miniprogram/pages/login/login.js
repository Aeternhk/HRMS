const { post } = require('../../utils/request')

Page({
  data: {
    username: '',
    password: '',
    rememberMe: true,
    loading: false
  },

  onLoad() {
    // 检查是否已登录
    const token = wx.getStorageSync('token')
    if (token) {
      wx.switchTab({ url: '/pages/index/index' })
    }
  },

  // 输入工号/手机号
  onUsernameInput(e) {
    this.setData({ username: e.detail.value })
  },

  // 输入密码
  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  // 切换记住我
  onRememberChange(e) {
    this.setData({ rememberMe: e.detail.value })
  },

  // 登录
  async onLogin() {
    const { username, password } = this.data

    if (!username.trim()) {
      wx.showToast({ title: '请输入工号或手机号', icon: 'none' })
      return
    }

    if (!password.trim()) {
      wx.showToast({ title: '请输入密码', icon: 'none' })
      return
    }

    this.setData({ loading: true })

    try {
      const res = await post('/mini/login', {
        username: username.trim(),
        password: password.trim()
      })

      if (res.code === 200 && res.data) {
        // 保存token
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('userInfo', res.data.userInfo)

        wx.showToast({
          title: '登录成功',
          icon: 'success',
          duration: 1500,
          complete: () => {
            setTimeout(() => {
              wx.switchTab({ url: '/pages/index/index' })
            }, 1500)
          }
        })
      }
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      this.setData({ loading: false })
    }
  }
})
