import request from '@/utils/request'

// 登录接口
export const login = (username, password) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data: { username, password }
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

