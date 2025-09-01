// 简单配置文件 - 避免环境检测问题
export const SIMPLE_CONFIG = {
  // 开发环境配置
  API_BASE_URL: 'http://localhost:8020',
  GATEWAY_URL: 'http://localhost:30010',
  AUTH_SERVICE_URL: 'http://localhost:8020',
  
  // 中间件服务
  NACOS_URL: 'http://localhost:8848',
  MYSQL_HOST: 'localhost',
  MYSQL_PORT: '3306',
  SEATA_HOST: 'localhost',
  SEATA_PORT: '8091',
  ROCKETMQ_HOST: 'localhost',
  ROCKETMQ_PORT: '9876',
  
  // 开发配置
  DEV_MODE: true,
  LOG_LEVEL: 'debug',
  TIMEOUT: 10000
}

// 获取配置的简单方法
export const getConfig = (key) => {
  return SIMPLE_CONFIG[key] || null
}

// 获取API基础URL
export const getApiBaseUrl = () => {
  return SIMPLE_CONFIG.API_BASE_URL
}

// 获取网关URL
export const getGatewayUrl = () => {
  return SIMPLE_CONFIG.GATEWAY_URL
}

// 获取认证服务URL
export const getAuthServiceUrl = () => {
  return SIMPLE_CONFIG.AUTH_SERVICE_URL
}
