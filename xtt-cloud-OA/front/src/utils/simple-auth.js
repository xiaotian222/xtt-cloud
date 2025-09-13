// 简化的认证管理工具，避免复杂的store依赖
export const getToken = () => {
  return localStorage.getItem('token') || ''
}

export const setToken = (token) => {
  if (token) {
    localStorage.setItem('token', token)
    console.log('Token已设置:', token.substring(0, 20) + '...')
  } else {
    localStorage.removeItem('token')
    console.log('Token已清除')
  }
}

export const getRefreshToken = () => {
  return localStorage.getItem('refreshToken') || ''
}

export const setRefreshToken = (refreshToken) => {
  if (refreshToken) {
    localStorage.setItem('refreshToken', refreshToken)
  } else {
    localStorage.removeItem('refreshToken')
  }
}

export const getUser = () => {
  try {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : {}
  } catch (error) {
    console.error('解析用户信息失败:', error)
    return {}
  }
}

export const setUser = (user) => {
  if (user) {
    localStorage.setItem('user', JSON.stringify(user))
    console.log('用户信息已设置:', user)
  } else {
    localStorage.removeItem('user')
    console.log('用户信息已清除')
  }
}

export const getPermissions = () => {
  try {
    const permsStr = localStorage.getItem('perms')
    return permsStr ? JSON.parse(permsStr) : []
  } catch (error) {
    console.error('解析权限信息失败:', error)
    return []
  }
}

export const setPermissions = (permissions) => {
  if (permissions) {
    localStorage.setItem('perms', JSON.stringify(permissions))
    console.log('权限信息已设置:', permissions)
  } else {
    localStorage.removeItem('perms')
    console.log('权限信息已清除')
  }
}

export const isAuthenticated = () => {
  return !!getToken()
}

export const clearAll = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  localStorage.removeItem('perms')
  console.log('所有认证信息已清除')
}

export const hasPermission = (permission) => {
  if (!permission) return true
  const permissions = getPermissions()
  return permissions.includes(permission)
}

// 检查token是否即将过期（提前5分钟刷新）
export const isTokenExpiringSoon = () => {
  const token = getToken()
  if (!token) return true
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp
    if (!exp) return false
    
    const now = Date.now() / 1000
    const fiveMinutes = 5 * 60
    return exp - now < fiveMinutes
  } catch (error) {
    console.error('检查token过期时间失败:', error)
    return true
  }
}

// 获取token过期时间
export const getTokenExpiration = () => {
  const token = getToken()
  if (!token) return null
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp ? new Date(payload.exp * 1000) : null
  } catch (error) {
    console.error('获取token过期时间失败:', error)
    return null
  }
}
