import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  const login = async (credentials) => {
    try {
      loading.value = true
      const response = await loginApi(credentials)
      
      if (response.success) {
        token.value = response.data.token
        user.value = response.data.user
        
        // 保存到本地存储
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        
        return { success: true }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    token.value = ''
    user.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const updateUser = (userData) => {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    login,
    logout,
    updateUser
  }
})
