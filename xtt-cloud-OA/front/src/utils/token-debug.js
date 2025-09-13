// Token管理调试工具
import { useAuthStore } from '@/stores/auth'

export const debugTokenStatus = () => {
  const authStore = useAuthStore()
  
  console.log('=== Token状态调试 ===')
  console.log('当前token:', authStore.token)
  console.log('当前refreshToken:', authStore.refreshTokenValue)
  console.log('当前用户:', authStore.user)
  console.log('是否已认证:', authStore.isAuthenticated)
  console.log('本地存储token:', localStorage.getItem('token'))
  console.log('本地存储refreshToken:', localStorage.getItem('refreshToken'))
  console.log('本地存储user:', localStorage.getItem('user'))
  console.log('本地存储perms:', localStorage.getItem('perms'))
  
  return {
    token: authStore.token,
    refreshToken: authStore.refreshTokenValue,
    user: authStore.user,
    isAuthenticated: authStore.isAuthenticated,
    localStorage: {
      token: localStorage.getItem('token'),
      refreshToken: localStorage.getItem('refreshToken'),
      user: localStorage.getItem('user'),
      perms: localStorage.getItem('perms')
    }
  }
}

export const validateTokenFormat = (token) => {
  if (!token) {
    return { valid: false, reason: 'Token为空' }
  }
  
  // 检查JWT格式 (header.payload.signature)
  const parts = token.split('.')
  if (parts.length !== 3) {
    return { valid: false, reason: 'Token格式不正确，不是有效的JWT' }
  }
  
  try {
    // 尝试解析header
    const header = JSON.parse(atob(parts[0]))
    const payload = JSON.parse(atob(parts[1]))
    
    // 检查过期时间
    if (payload.exp && payload.exp < Date.now() / 1000) {
      return { valid: false, reason: 'Token已过期', expiredAt: new Date(payload.exp * 1000) }
    }
    
    return { 
      valid: true, 
      header, 
      payload,
      expiresAt: payload.exp ? new Date(payload.exp * 1000) : null
    }
  } catch (error) {
    return { valid: false, reason: 'Token解析失败: ' + error.message }
  }
}

export const testTokenWithAPI = async () => {
  try {
    const authStore = useAuthStore()
    
    if (!authStore.token) {
      return { success: false, error: '没有token' }
    }
    
    // 测试token是否有效
    const response = await fetch('/api/auth/validate', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${authStore.token}`,
        'Content-Type': 'application/json'
      }
    })
    
    const data = await response.json()
    
    return {
      success: response.ok,
      status: response.status,
      data: data,
      token: authStore.token
    }
  } catch (error) {
    return {
      success: false,
      error: error.message
    }
  }
}

export const clearAllAuthData = () => {
  const authStore = useAuthStore()
  
  // 清除store
  authStore.logout()
  
  // 清除localStorage
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  localStorage.removeItem('perms')
  
  console.log('所有认证数据已清除')
}

export const simulateLogin = (mockToken = 'mock.jwt.token') => {
  const authStore = useAuthStore()
  
  // 模拟登录数据
  const mockUser = {
    username: 'testuser',
    role: 'ROLE_ADMIN',
    email: 'test@example.com'
  }
  
  // 设置token和用户信息
  authStore.token = mockToken
  authStore.refreshTokenValue = 'mock.refresh.token'
  authStore.user = mockUser
  
  // 保存到localStorage
  localStorage.setItem('token', mockToken)
  localStorage.setItem('refreshToken', 'mock.refresh.token')
  localStorage.setItem('user', JSON.stringify(mockUser))
  localStorage.setItem('perms', JSON.stringify(['user:read', 'user:write', 'role:read', 'role:write']))
  
  console.log('模拟登录完成')
  return { token: mockToken, user: mockUser }
}
