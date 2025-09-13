// API调用示例 - 展示如何使用增强的日志功能
import { ApiCaller, ApiLogger, API_CONFIG } from '@/config/api.config.js'

// 登录示例
export const loginExample = async (username, password) => {
  try {
    // 使用增强的API调用工具
    const result = await ApiCaller.post('/api/auth/login', {
      username,
      password
    })
    
    if (result.success) {
      console.log('登录成功:', result.data)
      return result.data
    } else {
      console.error('登录失败:', result.data)
      return null
    }
  } catch (error) {
    console.error('登录请求异常:', error)
    throw error
  }
}

// 获取用户信息示例
export const getUserInfoExample = async (token) => {
  try {
    const result = await ApiCaller.get('/api/auth/user', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    return result.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
    throw error
  }
}

// 手动日志记录示例
export const manualLoggingExample = () => {
  // 记录网关路由信息
  ApiLogger.logGatewayRoute(
    'http://localhost:3000/api/auth/login',
    'http://localhost:30010/api/auth/login',
    'auth-service'
  )
  
  // 记录请求信息
  ApiLogger.logRequest('POST', 'http://localhost:30010/api/auth/login', {
    username: 'admin',
    password: 'password'
  })
  
  // 记录响应信息
  ApiLogger.logResponse('POST', 'http://localhost:30010/api/auth/login', 200, {
    code: 200,
    message: '登录成功',
    data: {
      token: 'jwt-token-here',
      username: 'admin',
      role: 'ROLE_ADMIN'
    }
  }, 150)
}

// 在Vue组件中的使用示例
export const vueComponentExample = {
  methods: {
    async handleLogin() {
      try {
        // 登录前记录
        ApiLogger.logRequest('POST', API_CONFIG.AUTH_SERVICE.LOGIN, {
          username: this.loginForm.username,
          password: this.loginForm.password
        })
        
        const result = await ApiCaller.post(API_CONFIG.AUTH_SERVICE.LOGIN, {
          username: this.loginForm.username,
          password: this.loginForm.password
        })
        
        if (result.success) {
          // 登录成功，保存token
          localStorage.setItem('token', result.data.token)
          this.$router.push('/dashboard')
        } else {
          this.$message.error(result.data.message || '登录失败')
        }
      } catch (error) {
        this.$message.error('网络错误，请稍后重试')
      }
    }
  }
}

// 开发环境调试工具
export const debugTools = {
  // 打印当前API配置
  printConfig: () => {
    if (API_CONFIG.DEV.LOG_LEVEL === 'debug') {
      console.group('🔧 API Configuration')
      console.log('Base URL:', API_CONFIG.BASE_URL)
      console.log('Gateway URL:', API_CONFIG.GATEWAY_URL)
      console.log('Auth Service:', API_CONFIG.AUTH_SERVICE)
      console.log('Middleware:', API_CONFIG.MIDDLEWARE)
      console.groupEnd()
    }
  },
  
  // 测试所有API端点
  testAllEndpoints: async () => {
    const endpoints = [
      { method: 'POST', path: '/api/auth/login', data: { username: 'admin', password: 'password' } },
      { method: 'GET', path: '/api/auth/validate', params: { token: 'test-token' } },
      { method: 'GET', path: '/api/auth/user', headers: { 'Authorization': 'Bearer test-token' } }
    ]
    
    for (const endpoint of endpoints) {
      try {
        await ApiCaller.request(endpoint.method, endpoint.path, endpoint.data, {
          headers: endpoint.headers
        })
      } catch (error) {
        console.warn(`Endpoint test failed: ${endpoint.method} ${endpoint.path}`, error)
      }
    }
  }
}
