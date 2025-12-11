// API 配置
export const API_CONFIG = {
  BASE_URL: '/api',
  TIMEOUT: 10000
}

// 环境配置
export const ENV_CONFIG = {
  // 开发环境
  development: {
    GATEWAY_URL: 'http://localhost:30010'
  },
  // 生产环境
  production: {
    GATEWAY_URL: process.env.VITE_GATEWAY_URL || 'http://localhost:30010'
  }
}

