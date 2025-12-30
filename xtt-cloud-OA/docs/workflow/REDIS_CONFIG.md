# Redis 配置说明

## 概述

Workflow 模块使用 Redis 作为缓存和分布式锁的存储。Redis 配置通过 Spring Boot 自动配置机制加载。

## 配置方式

### 1. 本地配置文件（application.yaml）

在 `src/main/resources/application.yaml` 中配置：

```yaml
spring:
  redis:
    host: localhost          # Redis 服务器地址
    port: 6379               # Redis 端口
    password:                # Redis 密码（如果有）
    database: 0              # Redis 数据库索引（0-15）
    timeout: 3000ms          # 连接超时时间
    lettuce:
      pool:
        max-active: 8       # 连接池最大连接数
        max-idle: 8          # 连接池最大空闲连接数
        min-idle: 0          # 连接池最小空闲连接数
        max-wait: -1ms       # 连接池最大阻塞等待时间（-1 表示没有限制）
```

### 2. Nacos 配置中心

在 Nacos 配置中心创建 `workflow.yaml` 配置文件：

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

**注意**：配置会从 Nacos 自动加载，因为 `application.yaml` 中已经配置了：

```yaml
spring:
  config:
    import:
      - optional:nacos:workflow.yaml
```

## 配置说明

### 基础配置

| 配置项 | 说明 | 默认值 | 示例 |
|--------|------|--------|------|
| `spring.redis.host` | Redis 服务器地址 | localhost | localhost, 192.168.1.100 |
| `spring.redis.port` | Redis 端口 | 6379 | 6379 |
| `spring.redis.password` | Redis 密码 | 无 | mypassword |
| `spring.redis.database` | 数据库索引 | 0 | 0-15 |
| `spring.redis.timeout` | 连接超时时间 | 2000ms | 3000ms |

### 连接池配置（Lettuce）

| 配置项 | 说明 | 默认值 | 推荐值 |
|--------|------|--------|--------|
| `spring.redis.lettuce.pool.max-active` | 最大连接数 | 8 | 根据并发量调整 |
| `spring.redis.lettuce.pool.max-idle` | 最大空闲连接数 | 8 | 与 max-active 相同 |
| `spring.redis.lettuce.pool.min-idle` | 最小空闲连接数 | 0 | 2-4 |
| `spring.redis.lettuce.pool.max-wait` | 最大等待时间 | -1ms | 5000ms |

## 配置验证

应用启动时，`xtt-cloud-starter-redis` 会自动打印 Redis 配置信息，用于验证配置是否正确加载：

```
=== Redis 配置信息 ===
Redis Host: localhost
Redis Port: 6379
Redis Database: 0
Redis Timeout: 3000ms
Redis Pool Max-Active: 8
Redis Pool Max-Idle: 8
Redis Pool Min-Idle: 0
====================
```

## 环境变量配置

支持通过环境变量覆盖配置：

```bash
# 设置 Redis 主机
export REDIS_HOST=192.168.1.100

# 设置 Redis 端口
export REDIS_PORT=6379

# 设置 Redis 密码
export REDIS_PASSWORD=mypassword
```

在配置文件中使用：

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
```

## 不同环境配置

### 开发环境

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 测试环境

```yaml
spring:
  redis:
    host: test-redis.example.com
    port: 6379
    password: test-password
    database: 1
```

### 生产环境

```yaml
spring:
  redis:
    host: prod-redis.example.com
    port: 6379
    password: ${REDIS_PASSWORD}  # 从环境变量读取
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 16
        max-idle: 16
        min-idle: 4
```

## Redis 集群配置

如果使用 Redis 集群，配置如下：

```yaml
spring:
  redis:
    cluster:
      nodes:
        - 192.168.1.100:6379
        - 192.168.1.101:6379
        - 192.168.1.102:6379
      max-redirects: 3
    password: mypassword
    timeout: 3000ms
```

## Redis 哨兵配置

如果使用 Redis 哨兵模式，配置如下：

```yaml
spring:
  redis:
    sentinel:
      master: mymaster
      nodes:
        - 192.168.1.100:26379
        - 192.168.1.101:26379
        - 192.168.1.102:26379
    password: mypassword
    timeout: 3000ms
```

## 故障排查

### 1. 连接失败

**问题**：无法连接到 Redis 服务器

**排查步骤**：
1. 检查 Redis 服务器是否启动
2. 检查网络连接（ping Redis 服务器）
3. 检查防火墙设置
4. 检查配置的 host 和 port 是否正确

### 2. 认证失败

**问题**：Redis 认证失败

**排查步骤**：
1. 检查 Redis 密码配置是否正确
2. 检查 Redis 服务器是否启用了密码认证

### 3. 配置未生效

**问题**：配置修改后未生效

**排查步骤**：
1. 检查配置文件位置是否正确
2. 检查 Nacos 配置是否已更新
3. 重启应用服务
4. 查看启动日志中的 Redis 配置信息

## 性能优化建议

### 1. 连接池配置

根据实际并发量调整连接池大小：

- **低并发**（< 100 QPS）：max-active: 8, min-idle: 2
- **中并发**（100-1000 QPS）：max-active: 16, min-idle: 4
- **高并发**（> 1000 QPS）：max-active: 32, min-idle: 8

### 2. 超时设置

- **timeout**：建议设置为 3000-5000ms，避免连接超时
- **max-wait**：建议设置为 5000ms，避免长时间等待

### 3. 网络优化

- 使用 Redis 集群或哨兵模式提高可用性
- 使用 Redis 持久化保证数据安全
- 监控 Redis 性能指标

## 总结

Redis 配置通过 Spring Boot 自动配置机制加载，支持：

1. **本地配置文件**：`application.yaml`
2. **Nacos 配置中心**：`workflow.yaml`
3. **环境变量**：通过 `${ENV_VAR:default}` 语法

配置会在应用启动时自动加载，并在日志中打印配置信息用于验证。

