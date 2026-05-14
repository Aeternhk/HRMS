// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化时间
const formatTime = (date) => {
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  const second = date.getSeconds().toString().padStart(2, '0')
  return `${hour}:${minute}:${second}`
}

// 格式化日期时间
const formatDateTime = (date) => {
  return `${formatDate(date)} ${formatTime(date)}`
}

// 获取当前日期
const getToday = () => {
  return formatDate(new Date())
}

// 获取当前时间
const getNowTime = () => {
  return formatTime(new Date())
}

// 脱敏处理 - 手机号
const maskPhone = (phone) => {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 脱敏处理 - 身份证号
const maskIdCard = (idCard) => {
  if (!idCard || idCard.length !== 18) return idCard
  return idCard.replace(/(\d{4})\d{10}(\d{4})/, '$1**********$2')
}

// 脱敏处理 - 银行卡号
const maskBankCard = (cardNo) => {
  if (!cardNo || cardNo.length < 8) return cardNo
  return cardNo.replace(/(\d{4})\d+(\d{4})/, '$1 **** **** $2')
}

// 金额格式化
const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return parseFloat(amount).toFixed(2)
}

// 显示加载中
const showLoading = (title = '加载中...') => {
  wx.showLoading({
    title,
    mask: true
  })
}

// 隐藏加载
const hideLoading = () => {
  wx.hideLoading()
}

// 显示成功提示
const showSuccess = (title = '操作成功') => {
  wx.showToast({
    title,
    icon: 'success'
  })
}

// 显示错误提示
const showError = (title = '操作失败') => {
  wx.showToast({
    title,
    icon: 'error'
  })
}

module.exports = {
  formatDate,
  formatTime,
  formatDateTime,
  getToday,
  getNowTime,
  maskPhone,
  maskIdCard,
  maskBankCard,
  formatMoney,
  showLoading,
  hideLoading,
  showSuccess,
  showError
}
