import { ENV_CONFIG, getApiBaseUrl, getGatewayUrl, isDevelopment, isProduction } from './env.config.js'

// 后端服务配置管理
export const API_CONFIG = {
  // 基础配置 - 从环境配置读取
  BASE_URL: getApiBaseUrl(),
  GATEWAY_URL: getGatewayUrl(),
  
  // 服务地址
  AUTH_SERVICE: {
    BASE_URL: getApiBaseUrl(),
    LOGIN: '/api/auth/login',
    LOGOUT: '/api/auth/logout',
    REFRESH: '/api/auth/refresh',
    VALIDATE: '/api/auth/validate',
    USER_INFO: '/api/auth/user',
    USERS: '/api/auth/users'
  },
  
  // 中间件服务 - 从环境配置读取
  MIDDLEWARE: ENV_CONFIG.MIDDLEWARE,
  
  // 开发环境配置 - 从环境配置读取
  DEV: {
    ENABLE_MOCK: ENV_CONFIG.ENABLE_MOCK,
    LOG_LEVEL: ENV_CONFIG.LOG_LEVEL,
    TIMEOUT: ENV_CONFIG.TIMEOUT
  }
}

// 重新导出环境检测函数
export { isDevelopment, isProduction, getApiBaseUrl, getGatewayUrl } from './env.config.js'

// 获取完整的API地址
export const getFullApiUrl = (endpoint) => {
  return `${getApiBaseUrl()}${endpoint}`
}

// 日志工具类
export const ApiLogger = {
  // 打印请求信息
  logRequest: (method, url, data = null, headers = {}) => {
    if (isDevelopment() && API_CONFIG.DEV.LOG_LEVEL === 'debug') {
      console.group(`🚀 API Request: ${method.toUpperCase()}`)
      console.log(`📍 URL: ${url}`)
      console.log(`⏰ Time: ${new Date().toLocaleString()}`)
      if (data) {
        console.log(`📦 Request Data:`, data)
      }
      if (Object.keys(headers).length > 0) {
        console.log(`📋 Headers:`, headers)
      }
      console.groupEnd()
    }
  },

  // 打印响应信息
  logResponse: (method, url, status, data = null, duration = null) => {
    if (isDevelopment() && API_CONFIG.DEV.LOG_LEVEL === 'debug') {
      const statusIcon = status >= 200 && status < 300 ? '✅' : '❌'
      console.group(`${statusIcon} API Response: ${method.toUpperCase()}`)
      console.log(`📍 URL: ${url}`)
      console.log(`📊 Status: ${status}`)
      console.log(`⏰ Time: ${new Date().toLocaleString()}`)
      if (duration !== null) {
        console.log(`⏱️ Duration: ${duration}ms`)
      }
      if (data) {
        console.log(`📦 Response Data:`, data)
      }
      console.groupEnd()
    }
  },

  // 打印错误信息
  logError: (method, url, error, duration = null) => {
    if (isDevelopment()) {
      console.group(`❌ API Error: ${method.toUpperCase()}`)
      console.log(`📍 URL: ${url}`)
      console.log(`⏰ Time: ${new Date().toLocaleString()}`)
      if (duration !== null) {
        console.log(`⏱️ Duration: ${duration}ms`)
      }
      console.error(`💥 Error:`, error)
      console.groupEnd()
    }
  },

  // 打印网关路由信息
  logGatewayRoute: (originalUrl, targetUrl, service) => {
    if (isDevelopment() && API_CONFIG.DEV.LOG_LEVEL === 'debug') {
      console.group(`🌐 Gateway Route`)
      console.log(`📍 Original URL: ${originalUrl}`)
      console.log(`🎯 Target URL: ${targetUrl}`)
      console.log(`🔧 Service: ${service}`)
      console.log(`⏰ Time: ${new Date().toLocaleString()}`)
      console.groupEnd()
    }
  }
}

// 增强的API调用工具
export const ApiCaller = {
  // 基础请求方法
  request: async (method, endpoint, data = null, options = {}) => {
    const url = getFullApiUrl(endpoint)
    const startTime = Date.now()
    
    // 打印请求日志
    ApiLogger.logRequest(method, url, data, options.headers || {})
    
    try {
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          ...options.headers
        },
        body: data ? JSON.stringify(data) : null,
        ...options
      })
      
      const duration = Date.now() - startTime
      const responseData = await response.json()
      
      // 打印响应日志
      ApiLogger.logResponse(method, url, response.status, responseData, duration)
      
      return {
        success: response.ok,
        status: response.status,
        data: responseData,
        duration
      }
    } catch (error) {
      const duration = Date.now() - startTime
      ApiLogger.logError(method, url, error, duration)
      throw error
    }
  },

  // GET请求
  get: (endpoint, options = {}) => {
    return ApiCaller.request('GET', endpoint, null, options)
  },

  // POST请求
  post: (endpoint, data, options = {}) => {
    return ApiCaller.request('POST', endpoint, data, options)
  },

  // PUT请求
  put: (endpoint, data, options = {}) => {
    return ApiCaller.request('PUT', endpoint, data, options)
  },

  // DELETE请求
  delete: (endpoint, options = {}) => {
    return ApiCaller.request('DELETE', endpoint, null, options)
  }
}

