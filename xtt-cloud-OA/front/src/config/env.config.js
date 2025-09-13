// 环境配置管理
export const ENV_CONFIG = {
  // 从环境变量读取配置，如果没有则使用默认值
  GATEWAY_URL: import.meta.env.VITE_GATEWAY_URL || 'http://localhost:30010',
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:30010',
  LOG_LEVEL: import.meta.env.VITE_LOG_LEVEL || 'debug',
  ENABLE_MOCK: import.meta.env.VITE_ENABLE_MOCK === 'true' || true,
  TIMEOUT: parseInt(import.meta.env.VITE_TIMEOUT) || 10000,
  
  // 中间件服务配置
  MIDDLEWARE: {
    NACOS: import.meta.env.VITE_NACOS_URL || 'http://localhost:8848',
    MYSQL: import.meta.env.VITE_MYSQL_URL || 'localhost:3306',
    SEATA: import.meta.env.VITE_SEATA_URL || 'localhost:8091',
    ROCKETMQ: import.meta.env.VITE_ROCKETMQ_URL || 'localhost:9876'
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
  return ENV_CONFIG.API_BASE_URL
}

// 获取网关地址
export const getGatewayUrl = () => {
  return ENV_CONFIG.GATEWAY_URL
}

// 获取完整的API地址
export const getFullApiUrl = (endpoint) => {
  return `${getApiBaseUrl()}${endpoint}`
}

// 打印配置信息
export const printConfig = () => {
  if (isDevelopment()) {
    console.group('🔧 Environment Configuration')
    console.log('🌐 Gateway URL:', ENV_CONFIG.GATEWAY_URL)
    console.log('🔗 API Base URL:', ENV_CONFIG.API_BASE_URL)
    console.log('📝 Log Level:', ENV_CONFIG.LOG_LEVEL)
    console.log('🎭 Enable Mock:', ENV_CONFIG.ENABLE_MOCK)
    console.log('⏱️ Timeout:', ENV_CONFIG.TIMEOUT)
    console.groupEnd()
  }
}