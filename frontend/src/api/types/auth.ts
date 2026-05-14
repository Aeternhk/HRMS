export interface LoginDTO {
  username: string
  password: string
  verifyCode: string
}

export interface LoginVO {
  token: string
  refreshToken: string
  userId: number
  username: string
  name: string
  avatar?: string
  role: string
  roleName: string
}

export interface UserInfo {
  id: number
  username: string
  name: string
  avatar?: string
  role: string
  roleName: string
  phone?: string
  email?: string
  department?: string
}

export interface CaptchaVO {
  captchaId: string
  captchaImage: string
}
