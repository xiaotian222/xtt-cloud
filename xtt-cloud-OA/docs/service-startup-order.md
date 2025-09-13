# 服务启动顺序说明

## 问题描述

认证服务启动时出现 `UnsatisfiedDependencyException` 错误，这是因为 Feign 客户端无法找到 Platform 服务。

## 错误原因

1. **服务依赖**：认证服务依赖 Platform 服务
2. **服务发现**：Feign 客户端通过 Nacos 服务发现查找 Platform 服务
3. **启动顺序**：如果 Platform 服务未启动，认证服务无法创建 Feign 客户端

## 解决方案

### 1. 正确的启动顺序

```bash
# 1. 启动 Nacos 服务器
docker-compose up -d nacos-server

# 2. 启动 Platform 服务
mvn -pl platform -am spring-boot:run

# 3. 启动 Auth 服务
mvn -pl auth -am spring-boot:run

# 4. 启动 Gateway 服务
mvn -pl gateway -am spring-boot:run
```

### 2. 服务依赖关系

```
Nacos Server
    ↓
Platform Service (注册到 Nacos)
    ↓
Auth Service (通过 Nacos 发现 Platform Service)
    ↓
Gateway Service (通过 Nacos 发现 Auth Service)
```

### 3. 配置优化

#### Feign 客户端配置
```java
@FeignClient(name = "platform", configuration = FeignConfig.class)
public interface PlatformClient {
    // ...
}
```

#### 错误处理
```java
public Optional<UserInfoDto> findByUsername(String username) {
    try {
        // 调用 Platform 服务
        return Optional.of(platformClient.getUserByUsername(username));
    } catch (Exception e) {
        // 记录错误但不抛出异常
        System.err.println("Failed to get user: " + e.getMessage());
        return Optional.empty();
    }
}
```

## 启动检查清单

### 1. 环境准备
- [ ] Nacos 服务器已启动
- [ ] 数据库已启动（MySQL）
- [ ] Redis 已启动（如果使用）

### 2. 服务启动
- [ ] Platform 服务已启动并注册到 Nacos
- [ ] Auth 服务已启动并注册到 Nacos
- [ ] Gateway 服务已启动并注册到 Nacos

### 3. 服务验证
- [ ] 访问 Nacos 控制台查看服务列表
- [ ] 测试 Platform 服务接口
- [ ] 测试 Auth 服务接口
- [ ] 测试 Gateway 服务接口

## 故障排查

### 1. 服务发现失败
```bash
# 检查 Nacos 服务状态
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=platform

# 检查服务注册
curl http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=10
```

### 2. Feign 客户端错误
```yaml
# 添加 Feign 配置
feign:
  client:
    config:
      default:
        connect-timeout: 5000
        read-timeout: 10000
        logger-level: basic
```

### 3. 网络连接问题
```yaml
# 检查服务地址配置
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
```

## 最佳实践

### 1. 服务启动脚本
```bash
#!/bin/bash
# start-services.sh

echo "Starting Nacos Server..."
docker-compose up -d nacos-server

echo "Waiting for Nacos to be ready..."
sleep 30

echo "Starting Platform Service..."
mvn -pl platform -am spring-boot:run &
PLATFORM_PID=$!

echo "Waiting for Platform Service to be ready..."
sleep 30

echo "Starting Auth Service..."
mvn -pl auth -am spring-boot:run &
AUTH_PID=$!

echo "Waiting for Auth Service to be ready..."
sleep 30

echo "Starting Gateway Service..."
mvn -pl gateway -am spring-boot:run &
GATEWAY_PID=$!

echo "All services started!"
echo "Platform PID: $PLATFORM_PID"
echo "Auth PID: $AUTH_PID"
echo "Gateway PID: $GATEWAY_PID"
```

### 2. 健康检查
```yaml
# 添加健康检查端点
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

### 3. 监控和日志
```yaml
# 添加详细日志
logging:
  level:
    org.springframework.cloud.openfeign: DEBUG
    org.springframework.cloud.nacos: DEBUG
    xtt.cloud.oa.auth: DEBUG
```

## 注意事项

1. **启动顺序**：必须按照依赖关系顺序启动
2. **等待时间**：服务启动需要时间，建议添加等待机制
3. **错误处理**：实现优雅的错误处理和降级机制
4. **监控**：监控服务状态和调用成功率

遵循这些步骤可以避免服务启动时的依赖问题！
