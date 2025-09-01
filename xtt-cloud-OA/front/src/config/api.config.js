// 后端服务配置管理
export const API_CONFIG = {
  // 基础配置
  BASE_URL: 'http://localhost:8020',
  GATEWAY_URL: 'http://localhost:8080',
  
  // 服务地址
  AUTH_SERVICE: {
    BASE_URL: 'http://localhost:8020',
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    VALIDATE: '/auth/validate',
    USER_INFO: '/auth/user',
    USERS: '/auth/users'
  },
  
  // 中间件服务
  MIDDLEWARE: {
    NACOS: 'http://localhost:8848',
    MYSQL: 'localhost:3306',
    SEATA: 'localhost:8091',
    ROCKETMQ: 'localhost:9876'
  },
  
  // 开发环境配置
  DEV: {
    ENABLE_MOCK: true,
    LOG_LEVEL: 'debug',
    TIMEOUT: 10000
  }
}

// 环境检测
export const isDevelopment = () => {
  return import.meta.env.DEV
}

export const isProduction = () => {
  return import.meta.env.PROD
}

// 获取当前环境的API地址
export const getApiBaseUrl = () => {
  if (isDevelopment()) {
    return API_CONFIG.BASE_URL
  }
  // 生产环境可以配置不同的地址
  return API_CONFIG.BATE_URL
}

// 获取完整的API地址
export const getFullApiUrl = (endpoint: string) => {
  return `${getApiBaseUrl()}${endpoint}`
}
