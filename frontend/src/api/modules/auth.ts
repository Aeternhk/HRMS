import { request } from '@/utils/request'
import type { LoginDTO, LoginVO, UserInfo, CaptchaVO } from '@/api/types'

// 获取验证码
export const getCaptcha = () => {
  return request.get<CaptchaVO>('/auth/captcha')
}

// 登录
export const login = (data: LoginDTO) => {
  return request.post<LoginVO>('/auth/login', data)
}

// 刷新Token
export const refreshToken = (refreshToken: string) => {
  return request.post<LoginVO>('/auth/refresh', { refreshToken })
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get<UserInfo>('/auth/userinfo')
}

// 登出
export const logout = () => {
  return request.post('/auth/logout')
}

// 修改密码
export const updatePassword = (oldPassword: string, newPassword: string) => {
  return request.post('/auth/password', { oldPassword, newPassword })
}
