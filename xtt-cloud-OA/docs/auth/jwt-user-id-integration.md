# JWT Token 中集成用户 ID 的说明

## 概述

本次更新在 JWT token 中添加了用户 ID 字段，提供了更完整的用户身份信息，提高了系统的性能和可维护性。

## 主要变更

### 1. JwtUtil 增强
- 新增 `generateToken(String username, String role, Long userId)` 方法
- 新增 `generateRefreshToken(String username, Long userId)` 方法
- 新增 `extractUserId(String token)` 方法
- 支持从 token 中提取用户 ID

### 2. AuthService 更新
- 登录时在 JWT token 中包含用户 ID
- 刷新 token 时也包含用户 ID
- 提供更完整的用户身份信息

### 3. JwtAuthenticationFilter 增强
- 从 token 中提取用户 ID
- 将用户 ID 存储在认证详情中
- 便于后续获取用户身份信息

### 4. SecurityContextUtil 工具类
- 提供便捷的方法获取当前用户信息
- 支持获取用户 ID、用户名、角色等
- 提供权限检查方法

## 技术细节

### JWT Token 结构
```json
{
  "sub": "username",
  "username": "username",
  "role": "ADMIN",
  "userId": 123,
  "iat": 1694567890,
  "exp": 1694654290
}
```

### 使用示例

#### 1. 生成包含用户 ID 的 Token
```java
// 登录时生成 token
String token = jwtUtil.generateToken(username, role, userId);
String refreshToken = jwtUtil.generateRefreshToken(username, userId);
```

#### 2. 从 Token 中提取用户 ID
```java
// 从 token 中提取用户 ID
Long userId = jwtUtil.extractUserId(token);
String username = jwtUtil.extractUsername(token);
String role = jwtUtil.extractRole(token);
```

#### 3. 从 SecurityContext 获取用户信息
```java
// 获取当前用户 ID
Long currentUserId = SecurityContextUtil.getCurrentUserId();

// 获取当前用户名
String currentUsername = SecurityContextUtil.getCurrentUsername();

// 获取当前用户角色
String currentRole = SecurityContextUtil.getCurrentUserRole();

// 检查用户是否已认证
boolean isAuthenticated = SecurityContextUtil.isAuthenticated();

// 检查用户是否具有指定角色
boolean hasAdminRole = SecurityContextUtil.hasRole("ADMIN");
```

## 优势

### 1. 性能提升
- **减少数据库查询**：不需要通过用户名查询用户 ID
- **快速用户识别**：直接从 token 中获取用户 ID
- **缓存友好**：用户 ID 作为缓存键更稳定

### 2. 数据一致性
- **用户 ID 不变**：即使用户名改变，ID 仍然有效
- **唯一标识**：用户 ID 是数据库主键，更可靠
- **审计友好**：日志中可以直接记录用户 ID

### 3. 开发便利
- **简化代码**：不需要额外的用户查询
- **统一接口**：所有服务都可以使用相同的用户 ID
- **类型安全**：用户 ID 是 Long 类型，避免字符串比较

### 4. 安全性
- **Token 完整性**：用户 ID 包含在签名中，不可篡改
- **身份验证**：结合用户名和用户 ID 双重验证
- **权限控制**：基于用户 ID 进行细粒度权限控制

## 使用场景

### 1. 用户信息查询
```java
@GetMapping("/profile")
public Result<UserProfile> getProfile() {
    Long userId = SecurityContextUtil.getCurrentUserId();
    // 直接使用用户 ID 查询，无需通过用户名
    return userService.getProfileById(userId);
}
```

### 2. 权限检查
```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public Result<List<User>> getUsers() {
    Long currentUserId = SecurityContextUtil.getCurrentUserId();
    // 记录操作日志
    logService.logAction(currentUserId, "VIEW_USERS");
    return userService.getAllUsers();
}
```

### 3. 数据关联
```java
@PostMapping("/orders")
public Result<Order> createOrder(@RequestBody OrderRequest request) {
    Long userId = SecurityContextUtil.getCurrentUserId();
    request.setUserId(userId); // 自动设置用户 ID
    return orderService.createOrder(request);
}
```

## 注意事项

1. **向后兼容**：旧的 token 仍然有效，但不会包含用户 ID
2. **Token 大小**：添加用户 ID 会略微增加 token 大小
3. **安全性**：用户 ID 包含在签名中，确保不可篡改
4. **性能**：首次登录时仍需要查询用户信息获取 ID

## 测试建议

1. **登录测试**：验证 token 中包含用户 ID
2. **Token 解析**：测试从 token 中提取用户 ID
3. **权限测试**：验证基于用户 ID 的权限控制
4. **性能测试**：对比使用用户 ID 前后的性能差异

## 迁移指南

### 现有 Token 处理
- 检查 token 中是否包含用户 ID
- 如果没有，则通过用户名查询用户 ID
- 逐步迁移到新的 token 格式

### 代码更新
- 使用 `SecurityContextUtil` 获取用户信息
- 将基于用户名的查询改为基于用户 ID
- 更新日志记录使用用户 ID

这样的设计使得系统更加高效、安全和易于维护！
