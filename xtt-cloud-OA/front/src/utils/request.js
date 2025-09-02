import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 创建axios实例
const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers['Authorization'] = `Bearer ${authStore.token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 适配后端响应格式（兼容 code=0 / code=200 / success=true 等）
    const isSuccess =
      response.status === 2001 ||
      response.status === 200 ||
      res?.success === true ||
      res?.status === 200 ||
      res?.code === 200 ||
      res?.code === 0 ||
      // 登录这类接口有时直接返回 token 放在 data 或根节点
      !!res?.token || !!res?.data?.token

    if (isSuccess) {
      return res
    }

    ElMessage.error(res?.message || res?.msg || '请求失败')
    return Promise.reject(new Error(res?.message || res?.msg || '请求失败'))
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          const authStore = useAuthStore()
          authStore.logout()
          // 跳转到登录页
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data.message || data.msg || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default service
