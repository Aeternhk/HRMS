const { get } = require('../../../utils/request')

Page({
  data: {
    voteId: null,
    voteDetail: null,
    resultData: [],
    loading: false,
    totalVotes: 0
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ voteId: options.id })
      this.loadVoteResult()
    }
  },

  // 加载投票结果
  async loadVoteResult() {
    this.setData({ loading: true })
    try {
      const res = await get(`/mini/vote/result/${this.data.voteId}`)
      if (res.code === 200 && res.data) {
        const typeMap = { 'SINGLE': '单选', 'MULTI': '多选', 'SCORE': '评分', 'RANK': '排序' }
        const resultData = res.data.options || []
        const totalVotes = resultData.reduce((sum, item) => sum + (item.voteCount || 0), 0)

        // 计算百分比并排序
        resultData.forEach(item => {
          item.percentage = totalVotes > 0 ? Math.round((item.voteCount / totalVotes) * 100) : 0
        })
        resultData.sort((a, b) => b.voteCount - a.voteCount)

        const voteDetail = { ...res.data, _typeName: typeMap[res.data.type] || '单选' }

        this.setData({
          voteDetail: voteDetail,
          resultData: resultData,
          totalVotes: totalVotes,
          loading: false
        })
      }
    } catch (error) {
      console.error('获取投票结果失败:', error)
      this.setData({ loading: false })
    }
  },

  // 返回投票列表
  goBack() {
    wx.navigateBack()
  }
})
