# XTT Cloud OA - 企业级办公自动化系统

基于 Spring Cloud Alibaba + Vue 3 的微服务架构办公自动化系统，包含完整的 RBAC 权限管理、组织架构管理、多服务治理等功能。

## 🎯 项目概述

本项目是一个企业级的 OA 系统，采用微服务架构，包含：

- **认证服务**: JWT 认证、用户登录、权限验证
- **平台服务**: 用户管理、角色管理、权限管理、部门管理
- **网关服务**: API 路由、负载均衡、服务发现
- **前端应用**: Vue 3 + Element Plus + 现代化 UI
- **配置中心**: Nacos 配置管理和服务发现

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │    │   网关服务      │    │   Auth服务      │    │  Platform服务   │
│   Vue 3         │───▶│   Gateway       │───▶│   JWT认证       │    │   RBAC管理      │
│   Port: 3000    │    │   Port: 30010   │    │   Port: 8081    │    │   Port: 8085    │
└─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   Nacos         │
                       │   配置中心      │
                       │   Port: 8848    │
                       └─────────────────┘
```

## 🚀 快速开始

### 前置要求

- **Java**: JDK 8 或以上
- **Maven**: 3.6 或以上
- **Node.js**: 16 或以上
- **MySQL**: 5.7 或以上
- **Nacos**: 2.0 或以上

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

#### 1. 启动 Nacos 配置中心
```bash
# 下载并启动 Nacos
# 访问 http://localhost:8848/nacos
# 用户名/密码: nacos/nacos
```

#### 2. 启动 Gateway 服务
```bash
cd gateway
mvn spring-boot:run
```

#### 3. 启动 Auth 服务
```bash
cd auth
mvn spring-boot:run
```

#### 4. 启动 Platform 服务
```bash
cd platform
mvn spring-boot:run
```

#### 5. 启动前端服务
```bash
cd front
npm install
npm run dev
```

## 📱 访问地址

- **前端应用**: http://localhost:3000
- **网关服务**: http://localhost:30010
- **Auth 服务**: http://localhost:8081
- **Platform 服务**: http://localhost:8085
- **Nacos 控制台**: http://localhost:8848/nacos

## 👤 测试账号

| 用户名 | 密码 | 角色 | 权限 | 部门 |
|--------|------|------|------|------|
| admin | 123456 | ADMIN | 系统管理员权限 | 总公司 |
| zhangsan | 123456 | MANAGER | 部门经理权限 | 技术部 |
| lisi | 123456 | MANAGER | 部门经理权限 | 市场部 |
| wangwu | 123456 | USER | 普通用户权限 | 开发组 |
| zhaoliu | 123456 | AUDITOR | 审计员权限 | 技术部 |

## 🔐 认证流程

### 1. 用户登录
```
前端 → 网关 → Auth服务 → Platform服务
     ← 返回JWT Token ←
```

### 2. API 访问
```
前端 → 携带Token → 网关 → 验证Token → 目标服务
```

### 3. Token 刷新
```
前端 → 使用Refresh Token → Auth服务 → 返回新Token
```

### 4. 权限验证
```
前端 → 请求资源 → 网关 → Platform服务 → 验证权限 → 返回数据
```

## 📋 功能特性

### 认证服务 (Auth)
- ✅ JWT 认证和授权
- ✅ 用户登录和登出
- ✅ Token 自动刷新
- ✅ 密码加密存储
- ✅ 用户状态管理

### 平台服务 (Platform)
- ✅ 用户管理 (CRUD)
- ✅ 角色管理 (CRUD)
- ✅ 权限管理 (CRUD)
- ✅ 部门管理 (树形结构)
- ✅ 应用管理 (CRUD)
- ✅ 菜单管理 (层级结构)
- ✅ 审计日志记录
- ✅ 外部 API 接口

### 网关服务 (Gateway)
- ✅ API 路由和负载均衡
- ✅ 服务发现和注册
- ✅ 请求转发和代理
- ✅ 跨域处理
- ✅ 统一入口管理

### 前端应用 (Front)
- ✅ 现代化 UI 界面 (Element Plus)
- ✅ 完整的登录流程
- ✅ 平台管理界面
- ✅ 用户管理界面
- ✅ 角色权限管理
- ✅ 部门树形管理
- ✅ 响应式设计
- ✅ 状态管理 (Pinia)
- ✅ 路由守卫和权限控制

## 🔧 技术栈

### 后端技术
- **Spring Boot 2.7.x** - 应用框架
- **Spring Cloud Alibaba** - 微服务框架
- **Spring Security** - 安全框架
- **Spring Cloud Gateway** - API 网关
- **JWT (jjwt)** - JWT 实现
- **MyBatis** - ORM 框架
- **MySQL** - 关系型数据库
- **Nacos** - 服务发现和配置中心
- **Maven** - 构建工具

### 前端技术
- **Vue 3.3.4** - 渐进式 JavaScript 框架
- **Vite 4.4.9** - 下一代前端构建工具
- **Element Plus 2.3.9** - Vue 3 组件库
- **Vue Router 4.2.4** - Vue.js 官方路由管理器
- **Pinia 2.1.6** - Vue 的状态管理库
- **Axios 1.5.0** - 基于 Promise 的 HTTP 客户端
- **ES6+** - 现代 JavaScript 语法

## 📁 项目结构

```
xtt-cloud-OA/
├── auth/                    # 认证服务
│   ├── src/main/java/xtt/cloud/oa/auth/
│   │   ├── client/         # 外部服务客户端
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── dto/           # 数据传输对象
│   │   ├── entity/        # 实体类
│   │   ├── service/       # 服务类
│   │   └── util/          # 工具类
│   ├── src/main/resources/
│   │   └── application.yaml
│   └── pom.xml
├── platform/               # 平台服务
│   ├── src/main/java/xtt/cloud/oa/platform/
│   │   ├── application/    # 应用服务层
│   │   ├── domain/        # 领域层
│   │   ├── infrastructure/# 基础设施层
│   │   └── interfaces/    # 接口层
│   ├── src/main/resources/
│   │   ├── mapper/        # MyBatis 映射文件
│   │   ├── schema.sql     # 数据库结构
│   │   └── application.yaml
│   ├── docs/              # 文档
│   └── pom.xml
├── gateway/                # 网关服务
│   ├── src/main/java/xtt/cloud/oa/gateway/
│   │   ├── config/        # 配置类
│   │   ├── filter/        # 过滤器
│   │   └── handler/       # 处理器
│   ├── src/main/resources/
│   │   └── application.yaml
│   └── pom.xml
├── common/                 # 公共模块
│   ├── src/main/java/xtt/cloud/oa/common/
│   │   ├── dto/           # 公共 DTO
│   │   ├── mapper/        # 公共 Mapper
│   │   └── util/          # 公共工具
│   └── pom.xml
├── front/                  # 前端应用
│   ├── src/
│   │   ├── api/           # API 接口
│   │   ├── config/        # 配置文件
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # 状态管理
│   │   ├── utils/         # 工具函数
│   │   ├── views/         # 页面组件
│   │   │   └── platform/  # 平台管理页面
│   │   ├── App.vue        # 根组件
│   │   └── main.js        # 入口文件
│   ├── package.json
│   └── vite.config.js
├── config-init/            # 配置初始化
│   ├── config/            # Nacos 配置
│   ├── scripts/           # 脚本文件
│   └── sql/               # SQL 脚本
├── docs/                  # 项目文档
├── start.sh               # Linux/Mac 启动脚本
├── start.bat              # Windows 启动脚本
└── README.md              # 项目说明
```

## 🔧 配置说明

### 服务端口配置
- **前端应用**: 3000
- **网关服务**: 30010
- **认证服务**: 8081
- **平台服务**: 8085
- **Nacos**: 8848

### Nacos 配置中心
所有服务的配置都通过 Nacos 进行管理，配置文件位于 `config-init/config/` 目录下：

- `auth.yaml` - 认证服务配置
- `platform.yaml` - 平台服务配置
- `gateway.yaml` - 网关服务配置
- `datasource-config.yaml` - 数据源配置

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xtt_cloud_oa?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 前端代理配置
```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:30010',  // 网关地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 🧪 API 接口

### 认证服务接口 (Auth)
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/user` - 获取当前用户信息
- `POST /api/auth/refresh` - 刷新 Token
- `GET /api/auth/validate` - 验证 Token

### 平台服务接口 (Platform)

#### 内部管理接口
- `GET /api/platform/users` - 获取用户列表
- `POST /api/platform/users` - 创建用户
- `PUT /api/platform/users/{id}` - 更新用户
- `DELETE /api/platform/users/{id}` - 删除用户
- `GET /api/platform/roles` - 获取角色列表
- `POST /api/platform/roles` - 创建角色
- `GET /api/platform/permissions` - 获取权限列表
- `GET /api/platform/departments` - 获取部门列表
- `POST /api/platform/departments` - 创建部门

#### 外部服务接口
- `GET /api/platform/external/users/username/{username}` - 根据用户名获取用户
- `GET /api/platform/external/users/{id}` - 根据ID获取用户
- `GET /api/platform/external/users/username/{username}/permissions` - 获取用户权限
- `GET /api/platform/external/users/username/{username}/departments` - 获取用户部门
- `GET /api/platform/external/departments` - 获取所有部门
- `GET /api/platform/external/departments/tree` - 获取部门树结构

### 网关路由配置
- `/api/auth/**` → Auth 服务
- `/api/platform/**` → Platform 服务

## 🚀 部署指南

### 开发环境
1. 确保安装了 Node.js、Java、Maven、MySQL、Nacos
2. 导入数据库脚本：`config-init/sql/platform/schema.sql`
3. 启动 Nacos 配置中心
4. 运行启动脚本或手动启动服务
5. 访问 http://localhost:3000

### 生产环境
1. 构建前端：`cd front && npm run build`
2. 构建后端：`mvn clean package`
3. 配置 Nacos 生产环境配置
4. 部署到服务器
5. 配置 Nginx 反向代理

### Docker 部署
```bash
# 构建镜像
docker build -t xtt-cloud-oa-auth ./auth
docker build -t xtt-cloud-oa-platform ./platform
docker build -t xtt-cloud-oa-gateway ./gateway
docker build -t xtt-cloud-oa-front ./front

# 运行容器
docker-compose up -d
```

## 🐛 常见问题

### 1. 端口冲突
- 确保 3000、30010、8081、8085、8848 端口未被占用
- 可以在配置文件中修改端口

### 2. 服务启动顺序
- 必须先启动 Nacos 配置中心
- 然后启动 Gateway 网关服务
- 最后启动 Auth 和 Platform 服务

### 3. 数据库连接问题
- 确保 MySQL 服务已启动
- 检查数据库连接配置
- 确认数据库和表已创建

### 4. Nacos 配置问题
- 确保 Nacos 服务已启动
- 检查配置文件是否正确上传到 Nacos
- 验证服务注册是否成功

### 5. 跨域问题
- 开发环境已配置代理
- 生产环境需要配置 CORS

### 6. Token 过期
- 系统会自动刷新 Token
- 如果刷新失败会跳转到登录页

### 7. 权限问题
- 确保用户角色正确
- 检查路由权限配置
- 验证用户部门关联

## 📝 开发指南

### 添加新功能
1. **后端服务**：
   - 在对应的服务中添加新的控制器和服务
   - 更新数据库表结构和映射文件
   - 配置 Nacos 配置中心
2. **前端应用**：
   - 在 `front/src/views/platform/` 中添加新的页面组件
   - 在 `front/src/api/` 中添加新的 API 接口
   - 更新路由配置和权限控制
3. **网关配置**：
   - 在 `config-init/config/gateway.yaml` 中添加新的路由规则

### 修改配置
1. **Nacos 配置**：修改 `config-init/config/` 目录下的配置文件
2. **前端配置**：修改 `vite.config.js` 和 `front/src/config/`
3. **环境变量**：创建 `.env` 文件

### 数据库操作
1. **表结构修改**：更新 `platform/src/main/resources/schema.sql`
2. **测试数据**：在 `platform/src/main/resources/` 中添加测试数据脚本
3. **映射文件**：更新 MyBatis 映射文件

### 文档更新
1. **API 文档**：更新 `platform/EXTERNAL_API.md`
2. **数据库文档**：更新 `platform/docs/database-schema-analysis.md`
3. **项目文档**：更新 `docs/` 目录下的相关文档

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

MIT License

## 📞 联系方式

- **项目地址**: https://github.com/your-org/xtt-cloud-oa
- **问题反馈**: https://github.com/your-org/xtt-cloud-oa/issues
- **开发团队**: XTT Cloud Team

## 🎯 项目亮点

- ✅ **微服务架构**: 基于 Spring Cloud Alibaba 的完整微服务解决方案
- ✅ **RBAC 权限管理**: 完整的用户-角色-权限管理体系
- ✅ **组织架构管理**: 支持部门树形结构和用户部门关联
- ✅ **现代化前端**: Vue 3 + Element Plus 的现代化用户界面
- ✅ **配置中心**: 基于 Nacos 的集中化配置管理
- ✅ **API 网关**: 统一的 API 入口和路由管理
- ✅ **完整文档**: 详细的 API 文档和数据库设计文档
- ✅ **Docker 支持**: 支持容器化部署

---

**XTT Cloud OA** - 企业级办公自动化系统，让管理更简单！
