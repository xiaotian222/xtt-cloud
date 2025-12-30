# Workflow Starter

工作流 Starter - 可集成到其他 Spring Boot 应用中。

## 功能特性

- ✅ 完整的 DDD 架构实现
- ✅ 固定流程和自由流程支持
- ✅ 自动配置，开箱即用
- ✅ 支持独立部署或集成部署
- ✅ 数据隔离（通过应用独立数据库实现）

## 使用方式

### 1. 添加依赖

在应用的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>xtt.cloud.oa</groupId>
    <artifactId>workflow</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 配置数据源

在应用的 `application.yml` 中配置数据源（workflow 会使用应用的主数据源）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_app_db
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 配置 Workflow（可选）

```yaml
xtt:
  workflow:
    enabled: true                    # 是否启用 workflow（默认：true）
    enable-web-api: true            # 是否启用默认 REST API（默认：true）
    enable-async: true              # 是否启用异步支持（默认：true）
    mapper-scan-package: xtt.cloud.oa.workflow.infrastructure.persistence.mapper  # Mapper 扫描包（可选）
```

### 4. 应用启动类配置

如果应用没有全局配置 `@MapperScan`，需要在启动类中添加：

```java
@SpringBootApplication
@MapperScan("xtt.cloud.oa.workflow.infrastructure.persistence.mapper")
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

如果应用已经配置了全局 `@MapperScan`（包含 workflow 的 mapper 包），则无需额外配置。

### 5. 数据库表初始化

执行 workflow 模块的数据库初始化脚本，创建所需的表结构。

## 独立部署模式

如果需要将 workflow 作为独立服务运行，可以使用 `WorkflowApplication` 启动类：

```java
// 使用 WorkflowApplication 作为启动类
@SpringBootApplication
public class WorkflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
```

## 数据隔离

每个应用独立部署，使用独立数据库，天然实现数据隔离。无需在代码层面处理租户隔离。

## 配置说明

### WorkflowProperties

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `xtt.workflow.enabled` | boolean | true | 是否启用 workflow 功能 |
| `xtt.workflow.enable-web-api` | boolean | true | 是否启用默认 REST API |
| `xtt.workflow.enable-async` | boolean | true | 是否启用异步支持 |
| `xtt.workflow.mapper-scan-package` | String | `xtt.cloud.oa.workflow.infrastructure.persistence.mapper` | MyBatis Mapper 扫描包 |

## 自动配置

Workflow Starter 使用 Spring Boot 自动配置机制，会自动：

1. 扫描并注册所有 workflow 相关的 Bean（domain、application、infrastructure、interfaces）
2. 配置 MyBatis Mapper 扫描
3. 配置 Feign 客户端扫描
4. 配置异步支持
5. 启用 REST API（如果 `enable-web-api=true`）

## 注意事项

1. **数据源配置**：workflow 使用应用的主数据源，确保数据源已正确配置
2. **数据库表**：需要先执行数据库初始化脚本
3. **Mapper 扫描**：如果应用已有全局 `@MapperScan`，确保包含 workflow 的 mapper 包
4. **Feign 客户端**：如果应用需要调用外部服务，确保已配置 Feign 客户端
5. **Redis 配置**：workflow 使用 Redis 缓存，确保 Redis 已配置（通过 `xtt-cloud-starter-redis`）

## 依赖说明

Workflow Starter 依赖以下组件：

- Spring Boot Web
- MyBatis Plus
- MySQL Connector
- Redis Starter
- RabbitMQ Starter（可选，用于消息队列）
- Nacos Discovery/Config（可选，用于服务发现和配置）

## 示例

### 集成到应用 A

```yaml
# application-a/application.yml
spring:
  application:
    name: application-a
  datasource:
    url: jdbc:mysql://localhost:3306/app_a_db
    username: app_a_user
    password: app_a_pass

xtt:
  workflow:
    enabled: true
    enable-web-api: true
```

### 集成到应用 B

```yaml
# application-b/application.yml
spring:
  application:
    name: application-b
  datasource:
    url: jdbc:mysql://localhost:3306/app_b_db
    username: app_b_user
    password: app_b_pass

xtt:
  workflow:
    enabled: true
    enable-web-api: true
```

每个应用使用独立的数据库，实现数据隔离。

