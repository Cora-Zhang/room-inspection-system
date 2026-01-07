import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { LoginForm, UserInfo } from '@/api/types'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)

  // Actions
  const loginAction = async (loginForm: LoginForm) => {
    const res = await login(loginForm)
    token.value = res.token
    userInfo.value = res.userInfo
    localStorage.setItem('token', res.token)
    if (res.refreshToken) {
      localStorage.setItem('refreshToken', res.refreshToken)
    }
    return res
  }

  const logoutAction = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    }
  }

  const getUserInfoAction = async () => {
    const res = await getUserInfo()
    userInfo.value = res
    return res
  }

  const checkLogin = async () => {
    if (token.value && !userInfo.value) {
      try {
        await getUserInfoAction()
      } catch (error) {
        console.error('Check login error:', error)
        logoutAction()
      }
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    loginAction,
    logoutAction,
    getUserInfoAction,
    checkLogin
  }
})
