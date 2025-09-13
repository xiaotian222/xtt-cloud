# 网关路由测试指南

## 服务端口配置
- **网关 (Gateway)**: `http://localhost:30010`
- **认证服务 (Auth)**: `http://localhost:8020`
- **平台服务 (Platform)**: `http://localhost:8085`

## 路由配置验证

### 1. 认证服务路由测试
网关路由: `/api/auth/**` → `http://localhost:8020/auth/**`

#### 测试登录接口
```bash
# 通过网关访问登录接口
curl -X POST http://localhost:30010/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# 直接访问认证服务（对比）
curl -X POST http://localhost:8020/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'
```

#### 测试其他认证接口
```bash
# 刷新token
curl -X POST "http://localhost:30010/api/auth/refresh?refreshToken=your_refresh_token"

# 验证token
curl -X GET "http://localhost:30010/api/auth/validate?token=your_jwt_token"

# 获取用户信息
curl -X GET http://localhost:30010/api/auth/user \
  -H "Authorization: Bearer your_jwt_token"

# 登出
curl -X POST http://localhost:30010/api/auth/logout \
  -H "Authorization: Bearer your_jwt_token"
```

### 2. 平台服务路由测试
网关路由: `/api/platform/**` → `http://localhost:8085/platform/**`

#### 测试平台接口
```bash
# 通过网关访问平台接口
curl -X GET http://localhost:30010/api/platform/users \
  -H "Authorization: Bearer your_jwt_token"

# 直接访问平台服务（对比）
curl -X GET http://localhost:8085/platform/users \
  -H "Authorization: Bearer your_jwt_token"
```

## 启动顺序
1. 启动认证服务: `mvn -pl auth -am -D"spring-boot.run.profiles=local" spring-boot:run`
2. 启动平台服务: `mvn -pl platform -am -D"spring-boot.run.profiles=local" spring-boot:run`
3. 启动网关服务: `mvn -pl gateway -am -D"spring-boot.run.profiles=local" spring-boot:run`

## 预期结果
- 网关应该正确将 `/api/auth/**` 请求转发到 `http://localhost:8020/auth/**`
- 网关应该正确将 `/api/platform/**` 请求转发到 `http://localhost:8085/platform/**`
- 登录接口应该返回 JWT token 和用户信息
- 其他需要认证的接口应该正确验证 JWT token
