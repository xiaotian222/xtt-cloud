# Platform DTO 使用指南

## 概述

本文档介绍如何在其他微服务中使用 Platform 服务提供的 DTO 类。

## 依赖配置

### 1. 添加 common 模块依赖

在需要使用 Platform DTO 的服务中添加依赖：

```xml
<dependency>
    <groupId>xtt.cloud.oa</groupId>
    <artifactId>common</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 添加 OpenFeign 依赖（用于服务间调用）

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

## 使用示例

### 1. 创建 Platform 服务客户端

```java
@FeignClient(name = "platform", path = "/api/platform/external")
public interface PlatformClient {
    
    @GetMapping("/users/username/{username}")
    UserInfoDto getUserByUsername(@PathVariable("username") String username);
    
    @GetMapping("/users/username/{username}/permissions")
    Set<String> getUserPermissionsByUsername(@PathVariable("username") String username);
    
    @GetMapping("/permissions/user/{username}/has/{permissionCode}")
    Boolean userHasPermission(@PathVariable("username") String username, 
                             @PathVariable("permissionCode") String permissionCode);
}
```

### 2. 在服务中使用

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final PlatformClient platformClient;
    
    public boolean validateUserPermission(String username, String permission) {
        // 直接使用 Platform 服务返回的 DTO
        UserInfoDto userInfo = platformClient.getUserByUsername(username);
        
        if (userInfo == null) {
            return false;
        }
        
        // 使用权限验证接口
        return platformClient.userHasPermission(username, permission);
    }
    
    public UserInfoDto getUserInfo(String username) {
        return platformClient.getUserByUsername(username);
    }
}
```

### 3. 启用 Feign 客户端

在主应用类上添加注解：

```java
@SpringBootApplication
@EnableFeignClients
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

## DTO 类说明

### UserInfoDto
```java
public class UserInfoDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联信息
    private Set<RoleInfoDto> roles;
    private Set<String> permissions;
    private Set<DeptInfoDto> departments;
}
```

### RoleInfoDto
```java
public class RoleInfoDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联信息
    private Set<PermissionInfoDto> permissions;
}
```

### PermissionInfoDto
```java
public class PermissionInfoDto {
    private Long id;
    private String code;
    private String name;
    private String type;
    private LocalDateTime createdAt;
}
```

### DeptInfoDto
```java
public class DeptInfoDto {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

## 最佳实践

### 1. 缓存策略
- 对于频繁查询的用户信息，建议在调用方进行缓存
- 权限信息变化较少，可以设置较长的缓存时间

### 2. 错误处理
```java
@Service
public class UserService {
    
    @Autowired
    private PlatformClient platformClient;
    
    public UserInfoDto getUserInfo(String username) {
        try {
            return platformClient.getUserByUsername(username);
        } catch (FeignException.NotFound e) {
            log.warn("User not found: {}", username);
            return null;
        } catch (Exception e) {
            log.error("Failed to get user info for: {}", username, e);
            throw new ServiceException("获取用户信息失败");
        }
    }
}
```

### 3. 批量查询优化
```java
// 推荐：使用批量查询接口
List<UserInfoDto> users = platformClient.getUsersByUsernames(usernames);

// 不推荐：循环调用单个查询接口
List<UserInfoDto> users = usernames.stream()
    .map(platformClient::getUserByUsername)
    .collect(Collectors.toList());
```

## 注意事项

1. **版本兼容性**：确保 common 模块版本与 Platform 服务版本一致
2. **网络超时**：配置合适的 Feign 超时时间
3. **重试机制**：对于关键接口，建议配置重试机制
4. **监控告警**：监控服务间调用的成功率和响应时间

## 配置示例

### application.yml
```yaml
feign:
  client:
    config:
      platform:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: basic
  hystrix:
    enabled: true

hystrix:
  command:
    default:
      execution:
        timeout:
          enabled: true
        isolation:
          thread:
            timeoutInMilliseconds: 15000
```
