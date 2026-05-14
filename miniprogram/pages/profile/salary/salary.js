const { get } = require('../../../utils/request')
const { formatMoney } = require('../../../utils/util')

/**
 * 预处理工资数据，将所有金额字段格式化
 * （避免在WXML中调用方法，改用数据驱动）
 */
function formatSalaryFields(s) {
  if (!s) return null
  return {
    ...s,
    _fmtNetSalary: formatMoney(s.netSalary),
    _fmtGrossSalary: formatMoney(s.grossSalary),
    _fmtBaseSalary: formatMoney(s.baseSalary),
    _fmtPositionAllowance: formatMoney(s.positionAllowance),
    _fmtPerformanceSalary: formatMoney(s.performanceSalary),
    _fmtOvertimePay: formatMoney(s.overtimePay),
    _fmtFullAttendanceBonus: formatMoney(s.fullAttendanceBonus),
    _fmtOtherBonus: formatMoney(s.otherBonus),
    _fmtDeductions: formatMoney(s.deductions),
    _fmtSocialInsurance: formatMoney(s.socialInsurance),
    _fmtHousingFund: formatMoney(s.housingFund),
    _fmtLateDeduction: formatMoney(s.lateDeduction),
    _fmtLeaveDeduction: formatMoney(s.leaveDeduction),
    _fmtOtherDeduction: formatMoney(s.otherDeduction)
  }
}

Page({
  data: {
    salaryList: [],
    currentMonth: '',
    currentSalary: null,   // 已格式化的工资对象（含_fmtXxx字段）
    loading: false,
    hasMore: true,
    page: 1,
    pageSize: 10
  },

  onLoad() {
    const now = new Date()
    this.setData({
      currentMonth: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    })
    this.loadSalaryList()
  },

  // 加载工资列表
  async loadSalaryList() {
    if (this.data.loading) return

    this.setData({ loading: true })

    try {
      const res = await get('/mini/salary/list', {
        page: this.data.page,
        pageSize: this.data.pageSize
      })

      if (res.code === 200 && res.data) {
        const list = (res.data.list || []).map(formatSalaryFields)
        this.setData({
          salaryList: this.data.page === 1 ? list : [...this.data.salaryList, ...list],
          hasMore: list.length === this.data.pageSize,
          loading: false
        })

        // 设置当前月份工资（已含格式化字段）
        if (this.data.page === 1 && list.length > 0) {
          this.setData({ currentSalary: list[0] })
        }
      }
    } catch (error) {
      console.error('获取工资列表失败:', error)
      this.setData({ loading: false })
    }
  },

  // 选择月份
  onMonthSelect(e) {
    const index = e.currentTarget.dataset.index
    const salary = this.data.salaryList[index]
    this.setData({ currentSalary: salary })
  },

  // 加载更多
  onLoadMore() {
    if (!this.data.hasMore || this.data.loading) return
    this.setData({ page: this.data.page + 1 })
    this.loadSalaryList()
  }
})
