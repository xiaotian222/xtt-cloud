# 前端配置管理

## 概述

前端项目采用分层配置管理，支持环境变量和配置文件两种方式。

## 配置文件结构

```
src/config/
├── env.config.js      # 环境配置管理
├── api.config.js      # API配置管理
└── README.md         # 配置说明文档
```

## 配置方式

### 1. 环境变量配置（推荐）

在项目根目录创建环境变量文件：

#### 开发环境 (.env.development)
```bash
# 网关地址
VITE_GATEWAY_URL=http://localhost:30010

# API基础地址
VITE_API_BASE_URL=http://localhost:30010

# 日志级别
VITE_LOG_LEVEL=debug

# 是否启用Mock
VITE_ENABLE_MOCK=true

# 请求超时时间（毫秒）
VITE_TIMEOUT=10000

# 中间件服务地址
VITE_NACOS_URL=http://localhost:8848
VITE_MYSQL_URL=localhost:3306
VITE_SEATA_URL=localhost:8091
VITE_ROCKETMQ_URL=localhost:9876
```

#### 生产环境 (.env.production)
```bash
# 生产环境网关地址
VITE_GATEWAY_URL=https://api.yourdomain.com

# 生产环境API基础地址
VITE_API_BASE_URL=https://api.yourdomain.com

# 生产环境日志级别
VITE_LOG_LEVEL=error

# 生产环境不启用Mock
VITE_ENABLE_MOCK=false

# 生产环境超时时间
VITE_TIMEOUT=30000
```

### 2. 配置文件方式

直接修改 `src/config/env.config.js` 文件中的默认值。

## 配置优先级

1. 环境变量（.env文件）
2. 配置文件默认值

## 使用示例

### 在组件中使用配置

```javascript
import { API_CONFIG, getGatewayUrl, getApiBaseUrl } from '@/config/api.config.js'

// 获取网关地址
const gatewayUrl = getGatewayUrl()

// 获取API基础地址
const apiBaseUrl = getApiBaseUrl()

// 使用配置
console.log('Gateway URL:', API_CONFIG.GATEWAY_URL)
console.log('API Base URL:', API_CONFIG.BASE_URL)
```

### 在vite.config.js中使用配置

```javascript
import { getGatewayUrl } from './src/config/env.config.js'

export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: getGatewayUrl(), // 从配置文件读取网关地址
        changeOrigin: true
      }
    }
  }
})
```

## 配置项说明

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| 网关地址 | VITE_GATEWAY_URL | http://localhost:30010 | 后端网关服务地址 |
| API基础地址 | VITE_API_BASE_URL | http://localhost:30010 | API请求基础地址 |
| 日志级别 | VITE_LOG_LEVEL | debug | 控制台日志级别 |
| 启用Mock | VITE_ENABLE_MOCK | true | 是否启用Mock数据 |
| 请求超时 | VITE_TIMEOUT | 10000 | 请求超时时间（毫秒） |
| Nacos地址 | VITE_NACOS_URL | http://localhost:8848 | Nacos服务地址 |
| MySQL地址 | VITE_MYSQL_URL | localhost:3306 | MySQL服务地址 |
| Seata地址 | VITE_SEATA_URL | localhost:8091 | Seata服务地址 |
| RocketMQ地址 | VITE_ROCKETMQ_URL | localhost:9876 | RocketMQ服务地址 |

## 注意事项

1. 环境变量必须以 `VITE_` 开头才能在Vite中使用
2. 修改环境变量后需要重启开发服务器
3. 生产环境配置会覆盖开发环境配置
4. 敏感信息不要放在环境变量中，应该通过后端接口获取

## 调试配置

在开发环境中，可以通过以下方式查看当前配置：

```javascript
import { printConfig } from '@/config/env.config.js'

// 打印当前配置
printConfig()
```

这将在控制台输出当前的所有配置信息。