# OA系统 - 完整登录认证系统

基于 Spring Boot + Vue 3 的完整办公自动化系统，包含 JWT 认证、用户管理、权限控制等功能。

## 🎯 项目概述

本项目是一个完整的 OA 系统，包含：

- **后端服务**: Spring Boot + JWT + Spring Security
- **前端应用**: Vue 3 + Element Plus + Pinia
- **认证系统**: JWT Token + Refresh Token
- **用户管理**: 角色权限控制
- **网关服务**: API 路由和代理

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │    │   网关服务      │    │   Auth服务      │
│   Vue 3         │───▶│   Gateway       │───▶│   JWT认证       │
│   Port: 3000    │    │   Port: 8080    │    │   Port: 8020    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 快速开始

### 方式一：使用启动脚本（推荐）

#### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

#### Windows
```bash
start.bat
```

### 方式二：手动启动

#### 1. 启动 Auth 服务
```bash
cd auth
mvn spring-boot:run
```

#### 2. 启动前端服务
```bash
cd front
npm install
npm run dev
```

## 📱 访问地址

- **前端应用**: http://localhost:3000
- **Auth 服务**: http://localhost:8020
- **网关服务**: http://localhost:8080

## 👤 测试账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | password | ROLE_ADMIN | 管理员权限 |
| user | password | ROLE_USER | 普通用户权限 |
| manager | password | ROLE_MANAGER | 经理权限 |

## 🔐 认证流程

### 1. 用户登录
```
前端 → 网关 → Auth服务
     ← 返回JWT Token ←
```

### 2. API 访问
```
前端 → 携带Token → 网关 → 验证Token → 后端服务
```

### 3. Token 刷新
```
前端 → 使用Refresh Token → Auth服务 → 返回新Token
```

## 📋 功能特性

### 后端功能
- ✅ JWT 认证和授权
- ✅ 用户管理和角色控制
- ✅ Token 自动刷新
- ✅ 安全配置和 CORS
- ✅ 服务注册和发现
- ✅ 模拟用户数据

### 前端功能
- ✅ 现代化 UI 界面
- ✅ 完整的登录流程
- ✅ 路由守卫和权限控制
- ✅ 用户管理界面
- ✅ 响应式设计
- ✅ 状态管理

## 🔧 技术栈

### 后端技术
- **Spring Boot 2.x** - 应用框架
- **Spring Security** - 安全框架
- **JWT (jjwt)** - JWT 实现
- **Nacos** - 服务发现和配置中心
- **Maven** - 构建工具

### 前端技术
- **Vue 3.3.4** - 渐进式 JavaScript 框架
- **Vite 4.4.9** - 下一代前端构建工具
- **Element Plus 2.3.9** - Vue 3 组件库
- **Vue Router 4.2.4** - Vue.js 官方路由管理器
- **Pinia 2.1.6** - Vue 的状态管理库
- **Axios 1.5.0** - 基于 Promise 的 HTTP 客户端

## 📁 项目结构

```
xtt-cloud-OA/
├── auth/                    # Auth 服务
│   ├── src/main/java/
│   │   └── xtt/cloud/oa/auth/
│   │       ├── config/     # 配置类
│   │       ├── controller/ # 控制器
│   │       ├── dto/        # 数据传输对象
│   │       ├── entity/     # 实体类
│   │       ├── service/    # 服务类
│   │       └── util/        # 工具类
│   ├── src/main/resources/
│   │   └── application.yaml
│   └── pom.xml
├── front/                   # 前端应用
│   ├── src/
│   │   ├── api/            # API 接口
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # 状态管理
│   │   ├── utils/          # 工具函数
│   │   ├── views/          # 页面组件
│   │   ├── App.vue         # 根组件
│   │   └── main.js         # 入口文件
│   ├── package.json
│   └── vite.config.js
├── config-init/            # 配置文件
├── start.sh               # Linux/Mac 启动脚本
├── start.bat              # Windows 启动脚本
└── README.md              # 项目说明
```

## 🔧 配置说明

### Auth 服务配置
```yaml
server:
  port: 8020

spring:
  application:
    name: auth-service
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
        group: oa

jwt:
  secret: xtt-cloud-oa-jwt-secret-key-2023
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天
```

### 前端代理配置
```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 🧪 API 接口

### 认证接口
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `GET /auth/user` - 获取用户信息
- `POST /auth/refresh` - 刷新 Token
- `GET /auth/validate` - 验证 Token

### 用户管理接口
- `GET /auth/users` - 获取所有用户（管理员）
- `POST /auth/users` - 添加用户（管理员）

### 测试接口
- `GET /test/health` - 健康检查
- `GET /test/public` - 公开接口
- `GET /test/protected` - 受保护接口

## 🚀 部署指南

### 开发环境
1. 确保安装了 Node.js、Java、Maven
2. 运行启动脚本或手动启动服务
3. 访问 http://localhost:3000

### 生产环境
1. 构建前端：`npm run build`
2. 构建后端：`mvn clean package`
3. 部署到服务器
4. 配置 Nginx 反向代理

## 🐛 常见问题

### 1. 端口冲突
- 确保 3000、8020、8080 端口未被占用
- 可以在配置文件中修改端口

### 2. 跨域问题
- 开发环境已配置代理
- 生产环境需要配置 CORS

### 3. Token 过期
- 系统会自动刷新 Token
- 如果刷新失败会跳转到登录页

### 4. 权限问题
- 确保用户角色正确
- 检查路由权限配置

## 📝 开发指南

### 添加新功能
1. 后端：在 auth 服务中添加新的控制器和服务
2. 前端：在 front 中添加新的页面和 API
3. 路由：配置新的路由和权限

### 修改配置
1. 后端配置：修改 `application.yaml`
2. 前端配置：修改 `vite.config.js`
3. 环境变量：创建 `.env` 文件

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

MIT License

## 📞 联系方式

如有问题，请提交 Issue 或联系开发团队。
