# XTT Cloud Starter RabbitMQ

## 概述

`xtt-cloud-starter-rabbitmq` 是一个 Spring Boot Starter，用于简化 RabbitMQ 的配置和使用。

## 功能特性

- ✅ 自动配置 `RabbitTemplate`（JSON 消息转换器）
- ✅ 自动配置 `MessageConverter`（Jackson2JsonMessageConverter）
- ✅ 自动配置 `RabbitListenerContainerFactory`（支持 JSON 消息）
- ✅ 提供 `RabbitMessageSender` 消息发送器工具类
- ✅ 支持消息确认和返回回调
- ✅ 启动时打印 RabbitMQ 配置信息（用于调试）
- ✅ 支持 Spring Boot 自动配置机制
- ✅ 支持条件配置（如果已存在 Bean，则不创建）

## 依赖

```xml
<dependency>
    <groupId>xtt.cloud</groupId>
    <artifactId>xtt-cloud-starter-rabbitmq</artifactId>
</dependency>
```

## 配置

### 基础配置

在 `application.yaml` 或 Nacos 配置中心配置：

```yaml
spring:
  rabbitmq:
    host: localhost              # RabbitMQ 服务器地址
    port: 5672                  # RabbitMQ 端口
    username: guest             # RabbitMQ 用户名
    password: guest             # RabbitMQ 密码
    virtual-host: /             # 虚拟主机
    connection-timeout: 60000   # 连接超时时间（毫秒）
    publisher-confirm-type: correlated  # 发布确认类型
    publisher-returns: true      # 启用发布返回
    listener:
      type: simple              # 监听器类型（simple 或 direct）
      simple:
        acknowledge-mode: auto  # 确认模式（auto, manual, none）
        concurrency: 1          # 并发消费者数量
        max-concurrency: 10     # 最大并发消费者数量
        prefetch: 10            # 预取数量
        retry:
          enabled: true          # 启用重试
          initial-interval: 1000  # 初始重试间隔（毫秒）
          max-attempts: 3       # 最大重试次数
          multiplier: 1.0       # 重试倍数
```

### 环境变量配置

支持通过环境变量覆盖配置：

```bash
export RABBITMQ_HOST=192.168.1.100
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=admin
export RABBITMQ_PASSWORD=admin123
```

在配置文件中使用：

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
```

## 使用示例

### 1. 注入 RabbitMessageSender 发送消息

```java
@Service
public class OrderService {
    
    private final RabbitMessageSender messageSender;
    
    public OrderService(RabbitMessageSender messageSender) {
        this.messageSender = messageSender;
    }
    
    public void createOrder(Order order) {
        // 发送消息到订单队列
        messageSender.send("order.exchange", "order.create", order);
    }
    
    public void sendDelayedMessage(Order order, long delayMillis) {
        // 发送延迟消息
        messageSender.sendDelayed("order.exchange", "order.delayed", order, delayMillis);
    }
}
```

### 2. 使用 RabbitTemplate 发送消息

```java
@Service
public class NotificationService {
    
    private final RabbitTemplate rabbitTemplate;
    
    public NotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void sendNotification(Notification notification) {
        rabbitTemplate.convertAndSend("notification.exchange", "notification.send", notification);
    }
}
```

### 3. 使用 @RabbitListener 接收消息

```java
@Component
public class OrderListener {
    
    @RabbitListener(queues = "order.queue")
    public void handleOrder(Order order) {
        System.out.println("收到订单消息: " + order);
        // 处理订单逻辑
    }
    
    @RabbitListener(queues = "order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderWithHeaders(Order order, @Header("orderId") String orderId) {
        System.out.println("收到订单消息，OrderId: " + orderId + ", Order: " + order);
    }
}
```

### 4. 使用 AmqpAdmin 管理队列和交换机

```java
@Service
public class QueueManager {
    
    private final AmqpAdmin amqpAdmin;
    
    public QueueManager(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }
    
    public void createQueue(String queueName) {
        Queue queue = new Queue(queueName, true, false, false);
        amqpAdmin.declareQueue(queue);
    }
    
    public void createExchange(String exchangeName) {
        TopicExchange exchange = new TopicExchange(exchangeName, true, false);
        amqpAdmin.declareExchange(exchange);
    }
    
    public void bindQueue(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder
            .bind(new Queue(queueName))
            .to(new TopicExchange(exchangeName))
            .with(routingKey);
        amqpAdmin.declareBinding(binding);
    }
}
```

## 自动配置说明

### 自动配置的 Bean

1. **MessageConverter**
   - 类型：`Jackson2JsonMessageConverter`
   - 条件：不存在 `MessageConverter` Bean
   - 配置：使用 `ObjectMapper` 进行 JSON 序列化/反序列化

2. **RabbitTemplate**
   - 条件：不存在 `RabbitTemplate` Bean
   - 配置：
     - 设置消息转换器为 `Jackson2JsonMessageConverter`
     - 启用消息确认（`setMandatory(true)`）
     - 配置消息返回回调
     - 配置消息确认回调

3. **SimpleRabbitListenerContainerFactory**
   - 条件：不存在名为 `rabbitListenerContainerFactory` 的 Bean
   - 配置：
     - 并发消费者数量：1
     - 最大并发消费者数量：10
     - 预取数量：10
     - 自动启动：true

4. **RabbitMessageSender**
   - 条件：不存在 `RabbitMessageSender` Bean
   - 功能：提供便捷的消息发送方法

5. **RabbitMQConfigInfoPrinter**
   - 功能：启动时打印 RabbitMQ 配置信息
   - 用途：帮助验证配置是否正确加载

### 配置条件

- 需要存在 `ConnectionFactory`（由 Spring Boot 自动配置）
- 需要存在 `RabbitTemplate` 类
- 需要存在 `ObjectMapper`（由 Spring Boot 自动配置或手动配置）

## 启动日志

应用启动时会打印 RabbitMQ 配置信息：

```
=== RabbitMQ 配置信息 ===
RabbitMQ Host: localhost
RabbitMQ Port: 5672
RabbitMQ Username: guest
RabbitMQ Virtual Host: /
Listener Type: simple
Simple Listener Concurrency: 1
Simple Listener Max Concurrency: 10
Simple Listener Prefetch: 10
========================
RabbitMQ 消息转换器配置完成（Jackson2JsonMessageConverter）
RabbitTemplate 配置完成，连接工厂: CachingConnectionFactory
RabbitListenerContainerFactory 配置完成
```

## 自定义配置

如果需要自定义 `RabbitTemplate` 或 `MessageConverter`，只需在应用中定义相应的 Bean，Starter 会自动跳过自动配置。

```java
@Configuration
public class CustomRabbitMQConfig {
    
    @Bean
    public RabbitTemplate customRabbitTemplate(ConnectionFactory factory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(converter);
        // 自定义配置...
        return template;
    }
}
```

## 消息发送示例

### 发送普通消息

```java
messageSender.send("exchange.name", "routing.key", messageObject);
```

### 发送延迟消息

```java
messageSender.sendDelayed("exchange.name", "routing.key", messageObject, 5000); // 延迟 5 秒
```

### 发送带过期时间的消息

```java
messageSender.sendWithExpiration("exchange.name", "routing.key", messageObject, 60000); // 60 秒后过期
```

### 发送带自定义 Headers 的消息

```java
Map<String, Object> headers = new HashMap<>();
headers.put("orderId", "12345");
headers.put("userId", "67890");
messageSender.sendWithHeaders("exchange.name", "routing.key", messageObject, headers);
```

## 版本要求

- Spring Boot 3.x
- Java 17+
- RabbitMQ 3.8+

## 许可证

Apache License 2.0

