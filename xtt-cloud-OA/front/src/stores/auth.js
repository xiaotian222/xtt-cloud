import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi, refreshToken as refreshTokenApi } from '@/api/auth'
import { /* permissions api */ } from '@/api/platform'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshTokenValue = ref(localStorage.getItem('refreshToken') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  // 登录
  const login = async (credentials) => {
    try {
      loading.value = true
      const response = await loginApi(credentials)
      
      // 适配后端响应格式
      if (response.code === 2001 || response.success || response.status === 200) {
        const data = response.data || response
        token.value = data.token
        refreshTokenValue.value = data.refreshToken
        user.value = {
          username: data.username,
          role: data.role,
          email: data.email
        }
        
        // 保存到本地存储
        localStorage.setItem('token', data.token)
        localStorage.setItem('refreshToken', data.refreshToken)
        localStorage.setItem('user', JSON.stringify(user.value))
        
        // 拉取权限集合（基于用户名）并缓存到本地，供前端控制显隐
        try {
          const { getUserPermsByUsername } = await import('@/api/platform')
          const permsResp = await getUserPermsByUsername(data.username)
          if (permsResp && permsResp.data) {
            localStorage.setItem('perms', JSON.stringify(permsResp.data))
            console.log('权限集合已缓存:', permsResp.data)
          }
        } catch (e) {
          console.warn('获取权限集合失败(忽略)：', e)
          // 设置默认权限
          localStorage.setItem('perms', JSON.stringify(['user:read', 'user:write', 'role:read', 'role:write']))
        }
        return { success: true }
      } else {
        return { success: false, message: response.message || response.msg || '登录失败' }
      }
    } catch (error) {
      console.error('登录错误:', error)
      return { success: false, message: error.message || '登录失败' }
    } finally {
      loading.value = false
    }
  }

  // 登出
  const logout = async () => {
    try {
      if (token.value) {
        await logoutApi()
      }
    } catch (error) {
      console.error('登出错误:', error)
    } finally {
      // 清除本地数据
      token.value = ''
      refreshTokenValue.value = ''
      user.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    try {
      const response = await getUserInfoApi()
      if (response.code === 200 || response.success || response.status === 200) {
        const userData = response.data || response
        user.value = userData
        localStorage.setItem('user', JSON.stringify(userData))
        return userData
      }
    } catch (error) {
      console.error('获取用户信息错误:', error)
      throw error
    }
  }

  // 刷新token
  const refreshToken = async () => {
    try {
      if (!refreshTokenValue.value) {
        throw new Error('没有刷新token')
      }
      
      const response = await refreshTokenApi(refreshTokenValue.value)
      if (response.code === 200 || response.success || response.status === 200) {
        const data = response.data || response
        token.value = data.token
        refreshTokenValue.value = data.refreshToken
        
        localStorage.setItem('token', data.token)
        localStorage.setItem('refreshToken', data.refreshToken)
        
        return data.token
      } else {
        throw new Error(response.message || response.msg || '刷新token失败')
      }
    } catch (error) {
      console.error('刷新token错误:', error)
      // 刷新失败，清除所有认证信息
      logout()
      throw error
    }
  }

  // 更新用户信息
  const updateUser = (userData) => {
    user.value = { ...user.value, ...userData }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  // 检查token是否有效
  const checkToken = () => {
    return !!token.value
  }

  return {
    token,
    refreshTokenValue,
    user,
    // 读取本地权限集合的便捷 getter
    loading,
    isAuthenticated,
    login,
    logout,
    fetchUserInfo,
    refreshToken,
    updateUser,
    checkToken
  }
})
