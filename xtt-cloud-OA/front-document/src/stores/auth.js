import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/token'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const userInfo = ref(null)

  const isAuthenticated = computed(() => !!token.value)

  const loginUser = async (username, password) => {
    try {
      const response = await login(username, password)
      if (response.code === 200 && response.data) {
        token.value = response.data.token || response.data
        setToken(token.value)
        await fetchUserInfo()
        return { success: true }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error) {
      console.error('Login error:', error)
      return { success: false, message: error.message || '登录失败，请检查网络连接' }
    }
  }

  const fetchUserInfo = async () => {
    try {
      const response = await getUserInfo()
      if (response.code === 200 && response.data) {
        userInfo.value = response.data
      }
    } catch (error) {
      console.error('Fetch user info error:', error)
    }
  }

  const logoutUser = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = null
      userInfo.value = null
      removeToken()
    }
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    loginUser,
    logoutUser,
    fetchUserInfo
  }
})

