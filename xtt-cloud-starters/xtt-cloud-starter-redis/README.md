# XTT Cloud Starter Redis

## 概述

`xtt-cloud-starter-redis` 是一个 Spring Boot Starter，用于简化 Redis 的配置和使用。

## 功能特性

- ✅ 自动配置 `StringRedisTemplate`
- ✅ 自动配置 `ObjectMapper`（用于 JSON 序列化/反序列化）
- ✅ 启动时打印 Redis 配置信息（用于调试）
- ✅ 支持 Spring Boot 自动配置机制
- ✅ 支持条件配置（如果已存在 Bean，则不创建）

## 依赖

```xml
<dependency>
    <groupId>xtt.cloud</groupId>
    <artifactId>xtt-cloud-starter-redis</artifactId>
</dependency>
```

## 配置

### 基础配置

在 `application.yaml` 或 Nacos 配置中心配置：

```yaml
spring:
  redis:
    host: localhost          # Redis 服务器地址
    port: 6379              # Redis 端口
    password:                # Redis 密码（如果有）
    database: 0              # Redis 数据库索引（0-15）
    timeout: 3000ms         # 连接超时时间
    lettuce:
      pool:
        max-active: 8       # 连接池最大连接数
        max-idle: 8         # 连接池最大空闲连接数
        min-idle: 0         # 连接池最小空闲连接数
```

### 环境变量配置

支持通过环境变量覆盖配置：

```bash
export REDIS_HOST=192.168.1.100
export REDIS_PORT=6379
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

## 使用示例

### 1. 注入 StringRedisTemplate

```java
@Service
public class CacheService {
    
    private final StringRedisTemplate redisTemplate;
    
    public CacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void setCache(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    public String getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
```

### 2. 注入 ObjectMapper

```java
@Service
public class JsonService {
    
    private final ObjectMapper objectMapper;
    
    public JsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
    
    public String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
}
```

## 自动配置说明

### 自动配置的 Bean

1. **StringRedisTemplate**
   - 条件：不存在 `StringRedisTemplate` Bean
   - 配置：自动设置序列化器为 `StringRedisSerializer`

2. **ObjectMapper**
   - 条件：不存在 `ObjectMapper` Bean
   - 配置：启用默认类型信息，支持多态序列化

3. **RedisConfigInfoPrinter**
   - 功能：启动时打印 Redis 配置信息
   - 用途：帮助验证配置是否正确加载

### 配置条件

- 需要存在 `RedisConnectionFactory`（由 Spring Boot 自动配置）
- 需要存在 `StringRedisTemplate` 类

## 启动日志

应用启动时会打印 Redis 配置信息：

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
StringRedisTemplate 配置完成，Redis 连接工厂: LettuceConnectionFactory
```

## 自定义配置

如果需要自定义 `StringRedisTemplate` 或 `ObjectMapper`，只需在应用中定义相应的 Bean，Starter 会自动跳过自动配置。

```java
@Configuration
public class CustomRedisConfig {
    
    @Bean
    public StringRedisTemplate customStringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        // 自定义配置...
        return template;
    }
}
```

## 版本要求

- Spring Boot 3.x
- Java 17+

## 许可证

Apache License 2.0

