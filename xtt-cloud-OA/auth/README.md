# Auth Service

基于 JWT 的认证服务，提供用户登录、token 验证、用户管理等功能。

## 🚀 功能特性

- **JWT 认证**: 基于 JWT 的无状态认证
- **用户管理**: 模拟用户数据，支持用户增删改查
- **Token 刷新**: 支持 refresh token 机制
- **安全配置**: Spring Security 配置，支持 CORS
- **服务注册**: 集成 Nacos 服务发现

## 📋 API 接口

### 认证接口

#### 1. 用户登录
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

#### 2. 刷新 Token
```http
POST /auth/refresh?refreshToken=your_refresh_token
```

#### 3. 验证 Token
```http
GET /auth/validate?token=your_jwt_token
```

#### 4. 获取用户信息
```http
GET /auth/user
Authorization: Bearer your_jwt_token
```

#### 5. 用户登出
```http
POST /auth/logout
Authorization: Bearer your_jwt_token
```

### 用户管理接口

#### 1. 获取所有用户
```http
GET /auth/users
Authorization: Bearer your_jwt_token
```

#### 2. 添加用户
```http
POST /auth/users?username=newuser&password=password&role=ROLE_USER&email=newuser@example.com
Authorization: Bearer your_jwt_token
```

### 测试接口

#### 1. 健康检查
```http
GET /test/health
```

#### 2. 公开接口
```http
GET /test/public
```

#### 3. 受保护接口
```http
GET /test/protected
Authorization: Bearer your_jwt_token
```

## 👥 默认用户

| 用户名 | 密码 | 角色 | 邮箱 |
|--------|------|------|------|
| admin | password | ROLE_ADMIN | admin@xtt.com |
| user | password | ROLE_USER | user@xtt.com |
| manager | password | ROLE_MANAGER | manager@xtt.com |

## 🔧 配置说明

### JWT 配置
```yaml
jwt:
  secret: xtt-cloud-oa-jwt-secret-key-2023
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天
```

### 服务配置
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
        group: xtt-cloud-oa
```

## 🚀 启动方式

### 本地开发
```bash
cd xtt-cloud-OA/auth
mvn spring-boot:run
```

### Docker 部署
```bash
# 构建镜像
mvn clean package -DskipTests
docker build -t auth-service .

# 运行容器
docker run -p 8020:8020 auth-service
```

## 📝 使用示例

### 1. 用户登录
```bash
curl -X POST http://localhost:8020/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### 2. 使用 Token 访问受保护接口
```bash
curl -X GET http://localhost:8020/auth/user \
  -H "Authorization: Bearer your_jwt_token"
```

### 3. 刷新 Token
```bash
curl -X POST "http://localhost:8020/auth/refresh?refreshToken=your_refresh_token"
```

## 🔒 安全说明

- 所有密码都经过 BCrypt 加密
- JWT token 使用 HS256 算法签名
- 支持 CORS 跨域请求
- 无状态认证，支持水平扩展

## 📚 技术栈

- Spring Boot 2.x
- Spring Security
- JWT (jjwt)
- Nacos (服务发现和配置中心)
- Maven
