const { post } = require('../../../utils/request')

Page({
  data: {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
    submitting: false
  },

  // 旧密码输入
  onOldPasswordInput(e) {
    this.setData({ oldPassword: e.detail.value })
  },

  // 新密码输入
  onNewPasswordInput(e) {
    this.setData({ newPassword: e.detail.value })
  },

  // 确认密码输入
  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  // 修改密码
  async onChangePassword() {
    const { oldPassword, newPassword, confirmPassword } = this.data

    if (!oldPassword.trim()) {
      wx.showToast({ title: '请输入原密码', icon: 'none' })
      return
    }

    if (!newPassword.trim()) {
      wx.showToast({ title: '请输入新密码', icon: 'none' })
      return
    }

    if (newPassword.length < 6) {
      wx.showToast({ title: '新密码至少6位', icon: 'none' })
      return
    }

    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入的密码不一致', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    try {
      const res = await post('/mini/user/change-password', {
        oldPassword: oldPassword.trim(),
        newPassword: newPassword.trim()
      })

      if (res.code === 200) {
        wx.showToast({
          title: '密码修改成功',
          icon: 'success',
          complete: () => {
            setTimeout(() => {
              this.setData({
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
              })
              wx.navigateBack()
            }, 1500)
          }
        })
      }
    } catch (error) {
      console.error('修改密码失败:', error)
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 关于我们
  onAbout() {
    wx.showModal({
      title: '关于 HRMS',
      content: 'HRMS 人力资源管理系统\n版本：v1.0.0\n\n面向生产制造企业的人力资源管理系统，提供员工自助服务、考勤打卡、投票参与等功能。',
      showCancel: false
    })
  },

  // 帮助中心
  onHelp() {
    wx.showModal({
      title: '帮助中心',
      content: '如遇问题，请联系：\n\n人事部：ext.8888\nIT支持：ext.9999\n\n工作时间：周一至周五 8:30-17:30',
      showCancel: false
    })
  }
})
