# 认证服务重构说明

## 概述

本次重构将认证服务（auth）从维护自己的用户实体改为从平台服务（platform）获取用户信息，实现了微服务架构中的职责分离。

## 主要变更

### 1. 删除本地用户实体
- 删除了 `xtt.cloud.oa.auth.entity.User` 类
- 认证服务不再维护用户数据

### 2. 创建 Platform 客户端
- 新增 `xtt.cloud.oa.auth.client.PlatformClient` 接口
- 使用 OpenFeign 调用 Platform 服务
- 提供用户信息查询和密码验证接口

### 3. 重构 UserService
- 修改 `xtt.cloud.oa.auth.service.UserService` 使用 Platform 客户端
- 使用 `xtt.cloud.oa.common.dto.UserInfoDto` 替代本地 User 实体
- 添加本地缓存机制，减少对 Platform 服务的调用
- 提供用户角色获取方法

### 4. 更新 AuthService
- 修改 `xtt.cloud.oa.auth.service.AuthService` 使用新的 UserService
- 使用 `UserInfoDto` 替代本地 User 实体
- 简化用户信息提取逻辑

### 5. 更新 AuthController
- 修改 `xtt.cloud.oa.auth.controller.AuthController` 返回 `UserInfoDto`
- 优化用户信息获取流程

### 6. 扩展 Platform 服务
- 在 `xtt.cloud.oa.platform.interfaces.rest.external.ExternalUserController` 中添加密码验证接口
- 在 `xtt.cloud.oa.platform.application.UserService` 中实现密码验证逻辑
- 支持用户状态检查

## 技术细节

### 接口设计
```java
// Platform 客户端接口 - 使用 Nacos 服务发现
@FeignClient(name = "platform")
public interface PlatformClient {
    @GetMapping("/api/platform/external/users/username/{username}")
    UserInfoDto getUserByUsername(@PathVariable("username") String username);
    
    @GetMapping("/api/platform/external/users/validate")
    boolean validateUserPassword(@RequestParam("username") String username, 
                                @RequestParam("password") String password);
}
```

### 缓存机制
- 用户信息本地缓存，避免频繁调用 Platform 服务
- 提供缓存清理方法，支持用户信息更新

### 错误处理
- Platform 服务不可用时，记录日志但不抛出异常
- 提供降级处理机制

## 配置要求

### 服务发现
- 使用 Nacos 作为服务注册中心
- 所有服务都注册到 `xtt-cloud-oa` 组
- FeignClient 通过服务名进行调用，无需硬编码 URL

### 服务端口
- Platform 服务：8085
- Auth 服务：8020
- Gateway 服务：30010

### 依赖关系
- Auth 服务依赖 Platform 服务
- 通过 Nacos 服务发现自动解析服务地址
- 支持服务健康检查和负载均衡

## 测试建议

1. 启动 Platform 服务
2. 启动 Auth 服务
3. 测试登录接口：`POST /auth/login`
4. 测试用户信息获取：`GET /auth/user`
5. 测试登出接口：`POST /auth/logout`

## 优势

1. **职责分离**：认证服务专注于认证，用户管理由平台服务负责
2. **数据一致性**：用户数据统一由平台服务管理
3. **可扩展性**：其他服务可以直接使用平台服务的用户接口
4. **维护性**：减少代码重复，便于维护

## 注意事项

1. 确保 Platform 服务的高可用性
2. 监控服务间调用的性能
3. 考虑添加熔断机制
4. 定期清理用户缓存
