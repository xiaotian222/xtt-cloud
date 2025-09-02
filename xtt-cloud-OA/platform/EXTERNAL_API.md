# Platform 服务对外接口文档

## 概述

Platform 服务作为 RBAC 核心服务，为其他微服务提供用户、角色、权限等查询接口。所有对外接口都位于 `/api/platform/external/` 路径下。

## DTO 和依赖

所有对外接口使用的 DTO 类都定义在 `xtt-cloud-oa-common` 模块中，其他服务可以直接引用：

```xml
<dependency>
    <groupId>xtt.cloud.oa</groupId>
    <artifactId>common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 主要 DTO 类：
- `xtt.cloud.oa.common.dto.UserInfoDto` - 用户信息
- `xtt.cloud.oa.common.dto.RoleInfoDto` - 角色信息  
- `xtt.cloud.oa.common.dto.PermissionInfoDto` - 权限信息
- `xtt.cloud.oa.common.dto.DeptInfoDto` - 部门信息

## 接口分类

### 1. 用户服务接口 (`/api/platform/external/users`)

#### 1.1 根据用户名获取用户信息
```http
GET /api/platform/external/users/username/{username}
```

**响应示例：**
```json
{
  "id": 1,
  "username": "admin",
  "nickname": "系统管理员",
  "email": "admin@xtt.com",
  "phone": "13800138000",
  "status": 1,
  "createdAt": "2023-01-01T00:00:00",
  "updatedAt": "2023-01-01T00:00:00",
  "roles": [
    {
      "id": 1,
      "code": "ADMIN",
      "name": "系统管理员",
      "description": "拥有系统所有权限"
    }
  ],
  "permissions": ["user:list", "user:create", "role:list"],
  "departments": [
    {
      "id": 1,
      "name": "总公司",
      "parentId": null
    }
  ]
}
```

#### 1.2 根据用户ID获取用户信息
```http
GET /api/platform/external/users/{id}
```

#### 1.3 根据用户名获取用户权限
```http
GET /api/platform/external/users/username/{username}/permissions
```

**响应示例：**
```json
["user:list", "user:create", "role:list", "role:create"]
```

#### 1.4 根据用户ID获取用户权限
```http
GET /api/platform/external/users/{id}/permissions
```

#### 1.5 根据用户名获取用户角色
```http
GET /api/platform/external/users/username/{username}/roles
```

**响应示例：**
```json
["ADMIN", "MANAGER"]
```

#### 1.6 根据用户ID获取用户角色
```http
GET /api/platform/external/users/{id}/roles
```

#### 1.7 验证用户是否存在
```http
GET /api/platform/external/users/username/{username}/exists
```

**响应示例：**
```json
true
```

#### 1.8 批量获取用户信息（根据ID列表）
```http
POST /api/platform/external/users/batch
Content-Type: application/json

[1, 2, 3]
```

#### 1.9 批量获取用户信息（根据用户名列表）
```http
POST /api/platform/external/users/batch/usernames
Content-Type: application/json

["admin", "zhangsan", "lisi"]
```

### 2. 角色服务接口 (`/api/platform/external/roles`)

#### 2.1 根据角色代码获取角色信息
```http
GET /api/platform/external/roles/code/{code}
```

**响应示例：**
```json
{
  "id": 1,
  "code": "ADMIN",
  "name": "系统管理员",
  "description": "拥有系统所有权限",
  "permissions": [
    {
      "id": 1,
      "code": "user:list",
      "name": "用户列表",
      "type": "api"
    }
  ]
}
```

#### 2.2 根据角色ID获取角色信息
```http
GET /api/platform/external/roles/{id}
```

#### 2.3 根据角色代码获取角色权限
```http
GET /api/platform/external/roles/code/{code}/permissions
```

#### 2.4 根据角色ID获取角色权限
```http
GET /api/platform/external/roles/{id}/permissions
```

#### 2.5 验证角色是否存在
```http
GET /api/platform/external/roles/code/{code}/exists
```

#### 2.6 批量获取角色信息（根据代码列表）
```http
POST /api/platform/external/roles/batch/codes
Content-Type: application/json

["ADMIN", "MANAGER", "USER"]
```

#### 2.7 批量获取角色信息（根据ID列表）
```http
POST /api/platform/external/roles/batch
Content-Type: application/json

[1, 2, 3]
```

#### 2.8 获取所有角色列表
```http
GET /api/platform/external/roles/all
```

### 3. 权限服务接口 (`/api/platform/external/permissions`)

#### 3.1 根据权限代码获取权限信息
```http
GET /api/platform/external/permissions/code/{code}
```

**响应示例：**
```json
{
  "id": 1,
  "code": "user:list",
  "name": "用户列表",
  "type": "api",
  "createdAt": "2023-01-01T00:00:00"
}
```

#### 3.2 根据权限ID获取权限信息
```http
GET /api/platform/external/permissions/{id}
```

#### 3.3 验证权限是否存在
```http
GET /api/platform/external/permissions/code/{code}/exists
```

#### 3.4 批量获取权限信息（根据代码列表）
```http
POST /api/platform/external/permissions/batch/codes
Content-Type: application/json

["user:list", "user:create", "role:list"]
```

#### 3.5 批量获取权限信息（根据ID列表）
```http
POST /api/platform/external/permissions/batch
Content-Type: application/json

[1, 2, 3]
```

#### 3.6 根据权限类型获取权限列表
```http
GET /api/platform/external/permissions/type/{type}
```

#### 3.7 获取所有权限列表
```http
GET /api/platform/external/permissions/all
```

#### 3.8 验证用户是否拥有指定权限
```http
GET /api/platform/external/permissions/user/{username}/has/{permissionCode}
```

**响应示例：**
```json
true
```

#### 3.9 验证用户是否拥有指定权限列表中的任意一个
```http
POST /api/platform/external/permissions/user/{username}/has-any
Content-Type: application/json

["user:list", "user:create"]
```

#### 3.10 验证用户是否拥有指定权限列表中的所有权限
```http
POST /api/platform/external/permissions/user/{username}/has-all
Content-Type: application/json

["user:list", "user:create"]
```

## 使用场景

### 1. 认证服务调用
- 登录时验证用户是否存在
- 获取用户角色和权限信息
- 权限验证

### 2. 业务服务调用
- 获取用户基本信息
- 权限控制
- 数据权限过滤

### 3. 网关服务调用
- 用户信息验证
- 权限拦截

## 注意事项

1. **缓存机制**：所有查询接口都支持 Redis 缓存，提高性能
2. **批量查询**：支持批量查询，减少网络请求次数
3. **权限验证**：提供多种权限验证方式，满足不同场景需求
4. **错误处理**：统一的错误响应格式
5. **性能优化**：使用懒加载和缓存策略，避免 N+1 查询问题

## 错误响应格式

```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

## 认证要求

所有对外接口都需要通过 Gateway 的认证拦截，需要在请求头中携带有效的 JWT token：

```http
Authorization: Bearer <jwt_token>
```
