const { get, post } = require('../../../utils/request')

Page({
  data: {
    voteId: null,
    voteDetail: null,
    optionsWithState: [],   // 带选中状态的选项列表（用于渲染）
    selectedOptions: [],
    scoreValues: {},
    loading: false,
    submitting: false,

    // 结果视图相关（提交后或已投票时使用）
    hasVoted: false,
    showResult: false,
    resultData: [],
    totalVotes: 0
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ voteId: options.id })
      this.loadVoteDetail()
    }
  },

  // 加载投票详情
  async loadVoteDetail() {
    this.setData({ loading: true })
    try {
      const res = await get(`/mini/vote/detail/${this.data.voteId}`)
      if (res.code === 200 && res.data) {
        const voted = res.data.hasVoted || false
        // 给每个选项加上 _selected 状态字段，用于 WXML 直接判断（避免调用方法）
        // 同时预计算投票类型文本
        const typeMap = { 'SINGLE': '单选', 'MULTI': '多选', 'SCORE': '评分', 'RANK': '排序' }
        const optionsWithState = (res.data.options || []).map(opt => ({
          ...opt,
          _selected: false
        }))
        const voteDetail = { ...res.data, _typeName: typeMap[res.data.type] || '单选' }

        this.setData({
          voteDetail: voteDetail,
          optionsWithState: optionsWithState,
          hasVoted: voted,
          showResult: voted,
          loading: false
        })

        if (voted) {
          this.loadVoteResult()
        }
      }
    } catch (error) {
      console.error('获取投票详情失败:', error)
      this.setData({ loading: false })
    }
  },

  // 加载投票结果
  async loadVoteResult() {
    try {
      const res = await get(`/mini/vote/result/${this.data.voteId}`)
      if (res.code === 200 && res.data) {
        const options = res.data.options || []
        const totalVotes = options.reduce((sum, item) => sum + (item.voteCount || 0), 0)

        options.forEach(item => {
          item.percentage = totalVotes > 0 ? Math.round((item.voteCount / totalVotes) * 100) : 0
        })
        options.sort((a, b) => b.voteCount - a.voteCount)

        this.setData({
          resultData: options,
          totalVotes: totalVotes
        })
      }
    } catch (error) {
      console.error('获取投票结果失败:', error)
    }
  },

  // 单选
  onSingleSelect(e) {
    const clickedId = e.currentTarget.dataset.id
    const { optionsWithState } = this.data

    // 更新每个选项的 _selected 状态
    const updated = optionsWithState.map(opt => ({
      ...opt,
      _selected: String(opt.id) === String(clickedId)
    }))

    this.setData({
      optionsWithState: updated,
      selectedOptions: [String(clickedId)]
    })
  },

  // 多选
  onMultiSelect(e) {
    const clickedId = String(e.currentTarget.dataset.id)
    const { optionsWithState, voteDetail } = this.data

    // 找出当前点击项是否已选中
    const currentOpt = optionsWithState.find(opt => String(opt.id) === clickedId)
    if (!currentOpt) return

    const newSelected = currentOpt._selected
      ? this.data.selectedOptions.filter(id => id !== clickedId)
      : [...this.data.selectedOptions, clickedId]

    // 检查是否超过最大选择数
    if (!currentOpt._selected) {
      const maxSelect = voteDetail.maxSelections || optionsWithState.length
      if (newSelected.length > maxSelect) {
        wx.showToast({
          title: `最多选择${maxSelect}项`,
          icon: 'none'
        })
        return
      }
    }

    // 更新每个选项的 _selected 状态
    const updated = optionsWithState.map(opt => ({
      ...opt,
      _selected: newSelected.includes(String(opt.id))
    }))

    this.setData({
      optionsWithState: updated,
      selectedOptions: newSelected
    })
  },

  // 评分
  onScoreChange(e) {
    const optionId = String(e.currentTarget.dataset.id)
    const score = e.detail.value
    this.setData({
      [`scoreValues.${optionId}`]: score
    })
  },

  // 提交投票
  async onSubmit() {
    const { voteDetail, selectedOptions, scoreValues } = this.data

    // 验证
    if (voteDetail.type === 'SINGLE' || voteDetail.type === 'MULTI') {
      if (selectedOptions.length === 0) {
        wx.showToast({ title: '请选择选项', icon: 'none' })
        return
      }
    } else if (voteDetail.type === 'SCORE') {
      const allScored = voteDetail.options.every(opt => scoreValues[opt.id] !== undefined)
      if (!allScored) {
        wx.showToast({ title: '请为所有选项评分', icon: 'none' })
        return
      }
    }

    this.setData({ submitting: true })

    try {
      let submitData = { voteId: this.data.voteId }

      if (voteDetail.type === 'SINGLE' || voteDetail.type === 'MULTI') {
        submitData.optionIds = selectedOptions.map(id => Number(id))
      } else if (voteDetail.type === 'SCORE') {
        submitData.scores = Object.keys(scoreValues).map(key => ({
          optionId: key,
          score: scoreValues[key]
        }))
      }

      const res = await post('/mini/vote/submit', submitData)

      if (res.code === 200) {
        wx.showToast({
          title: '投票成功',
          icon: 'success',
          complete: () => {
            setTimeout(() => {
              this.setData({
                hasVoted: true,
                showResult: true,
                submitting: false
              })
              this.loadVoteResult()
            }, 800)
          }
        })
      }
    } catch (error) {
      console.error('提交投票失败:', error)
      this.setData({ submitting: false })
    }
  },

  goBack() {
    wx.navigateBack()
  }
})
