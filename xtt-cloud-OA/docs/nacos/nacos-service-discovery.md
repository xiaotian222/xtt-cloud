# Nacos 服务发现配置说明

## 概述

本项目使用 Nacos 作为服务注册中心，实现微服务之间的自动发现和调用。所有服务都注册到 `xtt-cloud-oa` 组中，通过服务名进行调用。

## 服务配置

### 1. 认证服务 (Auth Service)

**应用配置：**
```yaml
spring:
  application:
    name: auth
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
      config:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
        file-extension: yaml
```

**Java 配置：**
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "xtt.cloud.oa.auth.client")
public class AuthServiceApplication {
    // ...
}
```

### 2. 平台服务 (Platform Service)

**应用配置：**
```yaml
spring:
  application:
    name: platform
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
      config:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
        file-extension: yaml
```

**Java 配置：**
```java
@SpringBootApplication
@EnableDiscoveryClient
public class PlatformApplication {
    // ...
}
```

### 3. 网关服务 (Gateway Service)

**应用配置：**
```yaml
spring:
  application:
    name: gateway
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
      config:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
        file-extension: yaml
```

## Feign 客户端配置

### Platform 客户端

```java
@FeignClient(name = "platform")
public interface PlatformClient {
    @GetMapping("/api/platform/external/users/username/{username}")
    UserInfoDto getUserByUsername(@PathVariable("username") String username);
    
    @GetMapping("/api/platform/external/users/validate")
    boolean validateUserPassword(@RequestParam("username") String username, 
                                @RequestParam("password") String password);
}
```

**关键点：**
- 只配置 `name = "platform"`，不配置 URL
- Nacos 会自动解析服务名到实际地址
- 支持负载均衡和健康检查

## 服务发现流程

### 1. 服务注册
1. 服务启动时自动注册到 Nacos
2. 注册信息包括：服务名、IP、端口、健康状态
3. 定期发送心跳保持注册状态

### 2. 服务发现
1. FeignClient 调用时通过服务名查找服务
2. Nacos 返回可用的服务实例列表
3. 支持负载均衡策略（轮询、随机等）

### 3. 健康检查
1. Nacos 定期检查服务健康状态
2. 不健康的服务实例会被标记为不可用
3. 自动从可用列表中移除

## 优势

### 1. 动态配置
- **无需硬编码**：服务地址可以动态变化
- **环境隔离**：不同环境使用不同的 Nacos 实例
- **配置集中**：所有服务配置统一管理

### 2. 高可用
- **服务冗余**：同一服务可以有多个实例
- **故障转移**：自动切换到健康的服务实例
- **负载均衡**：自动分发请求到不同实例

### 3. 运维友好
- **服务监控**：可以查看所有服务的状态
- **配置管理**：统一管理服务配置
- **版本控制**：支持配置版本管理

## 配置示例

### 本地开发环境
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        group: xtt-cloud-oa
      config:
        server-addr: localhost:8848
        group: xtt-cloud-oa
```

### Docker 环境
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
      config:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
```

### 生产环境
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-cluster:8848
        group: xtt-cloud-oa
        namespace: production
      config:
        server-addr: nacos-cluster:8848
        group: xtt-cloud-oa
        namespace: production
```

## 故障排查

### 1. 服务注册失败
- 检查 Nacos 服务器是否可访问
- 检查网络连接和防火墙设置
- 查看服务启动日志

### 2. 服务发现失败
- 检查服务是否已注册到 Nacos
- 检查服务名是否正确
- 查看 Nacos 控制台的服务列表

### 3. 调用超时
- 检查目标服务是否健康
- 检查网络延迟
- 调整超时配置

## 监控和运维

### 1. Nacos 控制台
- 访问 `http://nacos-server:8848/nacos`
- 查看服务列表和健康状态
- 管理服务配置

### 2. 服务监控
- 监控服务注册状态
- 监控服务调用成功率
- 监控服务响应时间

### 3. 日志分析
- 查看服务发现日志
- 分析调用失败原因
- 监控异常情况

这样的配置使得微服务架构更加灵活和可维护！
