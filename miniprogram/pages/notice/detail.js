const { get } = require('../../utils/request')

Page({
  data: {
    notice: null,
    loading: true
  },

  onLoad(options) {
    const id = options.id
    if (id) {
      this.loadDetail(id)
    }
  },

  async loadDetail(id) {
    try {
      const res = await get(`/mini/notice/detail/${id}`)
      if (res.code === 200 && res.data) {
        this.setData({ notice: res.data, loading: false })
        wx.setNavigationBarTitle({ title: res.data.title || '公告详情' })
      }
    } catch (e) {
      console.error('获取公告详情失败:', e)
      this.setData({ loading: false })
    }
  }
})
