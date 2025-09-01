# 前端配置管理说明

## 📋 配置文件结构

```
src/config/
├── api.config.js      # API接口配置
├── env.config.js      # 环境配置
└── README.md          # 配置说明
```

## 🔧 配置文件说明

### 1. api.config.js - API接口配置
管理所有的后端服务接口地址和配置信息：

```javascript
export const API_CONFIG = {
  // 基础配置
  BASE_URL: 'http://localhost:8020',
  GATEWAY_URL: 'http://localhost:8080',
  
  // 认证服务
  AUTH_SERVICE: {
    BASE_URL: 'http://localhost:8020',
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    // ... 其他接口
  },
  
  // 中间件服务
  MIDDLEWARE: {
    NACOS: 'http://localhost:8848',
    MYSQL: 'localhost:3306',
    // ... 其他服务
  }
}
```

### 2. env.config.js - 环境配置
管理不同环境（开发、测试、生产）的配置：

```javascript
export const ENV_CONFIG = {
  development: {
    API_BASE_URL: 'http://localhost:8020',
    GATEWAY_URL: 'http://localhost:8080',
    // ... 开发环境配置
  },
  production: {
    API_BASE_URL: 'https://api.yourdomain.com',
    GATEWAY_URL: 'https://gateway.yourdomain.com',
    // ... 生产环境配置
  }
}
```

## 🚀 使用方法

### 1. 在组件中使用
```javascript
import { API_CONFIG } from '@/config/api.config'
import { getApiBaseUrl } from '@/config/env.config'

// 使用API配置
const loginUrl = API_CONFIG.AUTH_SERVICE.LOGIN

// 使用环境配置
const baseUrl = getApiBaseUrl()
```

### 2. 在Vite配置中使用
```javascript
import { getApiBaseUrl } from './src/config/env.config.js'

export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: getApiBaseUrl(), // 动态获取API地址
        // ... 其他配置
      }
    }
  }
})
```

## 🔄 修改配置

### 1. 修改开发环境地址
编辑 `src/config/env.config.js` 文件中的 `development` 部分：

```javascript
development: {
  API_BASE_URL: 'http://localhost:8020',  // 修改为你的地址
  GATEWAY_URL: 'http://localhost:8080',   // 修改为你的地址
  // ... 其他配置
}
```

### 2. 添加新的服务配置
在 `src/config/api.config.js` 中添加新的服务：

```javascript
export const API_CONFIG = {
  // ... 现有配置
  
  // 新增服务
  NEW_SERVICE: {
    BASE_URL: 'http://localhost:8081',
    ENDPOINT1: '/api/endpoint1',
    ENDPOINT2: '/api/endpoint2'
  }
}
```

## 📱 环境切换

### 1. 开发环境
```bash
npm run dev
# 使用 development 配置
```

### 2. 生产环境
```bash
npm run build
# 使用 production 配置
```

### 3. 测试环境
```bash
npm run build --mode test
# 使用 test 配置
```

## 🎯 优势

- ✅ **集中管理**: 所有配置集中在一个地方
- ✅ **环境隔离**: 不同环境使用不同配置
- ✅ **易于维护**: 修改配置只需改一个文件
- ✅ **类型安全**: 支持TypeScript类型检查
- ✅ **动态配置**: 支持运行时动态获取配置

## 🔍 注意事项

1. **修改配置后需要重启服务**: 特别是Vite配置相关的修改
2. **环境变量优先级**: 环境变量会覆盖配置文件
3. **生产环境安全**: 生产环境的敏感信息不要硬编码
4. **版本控制**: 配置文件应该纳入版本控制
