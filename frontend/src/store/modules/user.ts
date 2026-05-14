import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/api/types'
import * as authApi from '@/api/modules/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.name || '')
  const userRole = computed(() => userInfo.value?.role || '')
  const isAdmin = computed(() => userRole.value === 'admin' || userRole.value === 'super_admin')

  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const setPermissions = (perms: string[]) => {
    permissions.value = perms
  }

  const login = async (username: string, password: string, verifyCode: string) => {
    const res = await authApi.login({ username, password, verifyCode })
    setToken(res.token)
    setUserInfo({
      id: res.userId,
      username: res.username,
      name: res.name,
      avatar: res.avatar,
      role: res.role,
      roleName: res.roleName
    })
    return res
  }

  const logout = async () => {
    try {
      await authApi.logout()
    } catch (e) {
      // 忽略错误
    }
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const fetchUserInfo = async () => {
    try {
      const res = await authApi.getUserInfo()
      setUserInfo(res)
      return res
    } catch (e) {
      logout()
      throw e
    }
  }

  // 初始化用户信息
  const initUserInfo = () => {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (e) {
        localStorage.removeItem('userInfo')
      }
    }
  }

  // 检查是否有权限
  const hasPermission = (permission: string) => {
    if (isAdmin.value) return true
    return permissions.value.includes(permission)
  }

  return {
    // State
    token,
    userInfo,
    permissions,
    // Getters
    isLoggedIn,
    userName,
    userRole,
    isAdmin,
    // Actions
    setToken,
    setUserInfo,
    setPermissions,
    login,
    logout,
    fetchUserInfo,
    initUserInfo,
    hasPermission
  }
})
