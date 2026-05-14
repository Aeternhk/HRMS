const { get } = require('../../utils/request')

Page({
  data: {
    activeTab: 'ongoing',
    ongoingVotes: [],
    endedVotes: [],
    loading: false,
    page: 1,
    pageSize: 20,
    hasMore: true
  },

  onLoad() {
    this.loadVotes()
  },

  onShow() {
    // 每次显示都重新拉取，确保数据最新
    this.setData({ page: 1, ongoingVotes: [], endedVotes: [], hasMore: true })
    this.loadVotes()
    this.getTabBar().setData({ value: 'vote' })
  },

  // 切换标签
  onTabChange(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.activeTab) return
    this.setData({
      activeTab: tab,
      page: 1,
      ongoingVotes: [],
      endedVotes: [],
      hasMore: true
    })
    this.loadVotes()
  },

  // 加载投票列表
  async loadVotes() {
    if (this.data.loading) return
    this.setData({ loading: true })

    try {
      const res = await get('/mini/vote/list', {
        status: this.data.activeTab,
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      if (res.code === 200 && res.data) {
        const list = res.data.list || []
        const isFirstPage = this.data.page === 1

        if (this.data.activeTab === 'ongoing') {
          this.setData({
            ongoingVotes: isFirstPage ? list : [...this.data.ongoingVotes, ...list]
          })
        } else {
          this.setData({
            endedVotes: isFirstPage ? list : [...this.data.endedVotes, ...list]
          })
        }
        this.setData({
          hasMore: list.length === this.data.pageSize,
          loading: false
        })
      } else {
        this.setData({ loading: false })
      }
    } catch (error) {
      console.error('获取投票列表失败:', error)
      this.setData({ loading: false })
    }
  },

  // 查看投票详情
  goToDetail(e) {
    const id = e.currentTarget.dataset.id
    const vote = this.data.activeTab === 'ongoing'
      ? this.data.ongoingVotes.find(v => v.id === id)
      : this.data.endedVotes.find(v => v.id === id)

    if (vote && vote.hasVoted) {
      wx.navigateTo({ url: `/pages/vote/result/result?id=${id}` })
    } else {
      wx.navigateTo({ url: `/pages/vote/detail/detail?id=${id}` })
    }
  },

  // 下拉刷新
  async onPullDownRefresh() {
    this.setData({ page: 1, ongoingVotes: [], endedVotes: [], hasMore: true })
    await this.loadVotes()
    wx.stopPullDownRefresh()
  },

  // 加载更多
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ page: this.data.page + 1 })
    this.loadVotes()
  }
})
