import request from '@/utils/request'

// 登录接口
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 登出接口
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/auth/user',
    method: 'get'
  })
}

// 刷新token
export const refreshToken = (refreshToken) => {
  return request({
    url: '/auth/refresh',
    method: 'post',
    params: { refreshToken }
  })
}

// 验证token
export const validateToken = (token) => {
  return request({
    url: '/auth/validate',
    method: 'get',
    params: { token }
  })
}

// 获取所有用户（管理员功能）
export const getAllUsers = () => {
  return request({
    url: '/auth/users',
    method: 'get'
  })
}

// 添加用户（管理员功能）
export const addUser = (userData) => {
  return request({
    url: '/auth/users',
    method: 'post',
    params: userData
  })
}
