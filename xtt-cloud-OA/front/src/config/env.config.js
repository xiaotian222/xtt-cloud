// 环境配置文件
export const ENV_CONFIG = {
  // 开发环境
  development: {
    API_BASE_URL: 'http://localhost:8020',
    GATEWAY_URL: 'http://localhost:8080',
    AUTH_SERVICE_URL: 'http://localhost:8020',
    NACOS_URL: 'http://localhost:8848',
    MYSQL_HOST: 'localhost',
    MYSQL_PORT: '3306',
    SEATA_HOST: 'localhost',
    SEATA_PORT: '8091',
    ROCKETMQ_HOST: 'localhost',
    ROCKETMQ_PORT: '9876'
  },
  
  // 生产环境
  production: {
    API_BASE_URL: 'https://api.yourdomain.com',
    GATEWAY_URL: 'https://gateway.yourdomain.com',
    AUTH_SERVICE_URL: 'https://auth.yourdomain.com',
    NACOS_URL: 'https://nacos.yourdomain.com',
    MYSQL_HOST: 'mysql.yourdomain.com',
    MYSQL_PORT: '3306',
    SEATA_HOST: 'seata.yourdomain.com',
    SEATA_PORT: '8091',
    ROCKETMQ_HOST: 'rocketmq.yourdomain.com',
    ROCKETMQ_PORT: '9876'
  },
  
  // 测试环境
  test: {
    API_BASE_URL: 'http://test-api.yourdomain.com',
    GATEWAY_URL: 'http://test-gateway.yourdomain.com',
    AUTH_SERVICE_URL: 'http://test-auth.yourdomain.com',
    NACOS_URL: 'http://test-nacos.yourdomain.com',
    MYSQL_HOST: 'test-mysql.yourdomain.com',
    MYSQL_PORT: '3306',
    SEATA_HOST: 'test-seata.yourdomain.com',
    SEATA_PORT: '8091',
    ROCKETMQ_HOST: 'test-rocketmq.yourdomain.com',
    ROCKETMQ_PORT: '9876'
  }
}

// 获取当前环境配置
export const getCurrentEnvConfig = () => {
  // 安全地获取环境变量
  try {
    if (typeof import.meta !== 'undefined' && import.meta.env) {
      const env = import.meta.env.MODE || 'development'
      return ENV_CONFIG[env] || ENV_CONFIG.development
    }
  } catch (error) {
    console.warn('无法获取环境变量，使用默认开发环境配置:', error)
  }
  return ENV_CONFIG.development
}

// 获取特定配置项
export const getConfig = (key) => {
  const config = getCurrentEnvConfig()
  return config[key]
}

// 获取API基础URL
export const getApiBaseUrl = () => {
  return getConfig('API_BASE_URL')
}

// 获取网关URL
export const getGatewayUrl = () => {
  return getConfig('GATEWAY_URL')
}

// 获取认证服务URL
export const getAuthServiceUrl = () => {
  return getConfig('AUTH_SERVICE_URL')
}
