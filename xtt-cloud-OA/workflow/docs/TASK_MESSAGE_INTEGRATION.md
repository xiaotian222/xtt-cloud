# 待办已办消息集成方案

## 📋 问题

workflow 怎么集成待办已办消息这些，是通过订阅事件然后触发消息发送吗，消息生产是集成在 workflow 中还是单独出来当成一个服务？

## 🎯 架构设计建议

### 方案对比

| 方案 | 优点 | 缺点 | 推荐度 |
|------|------|------|--------|
| **方案A：事件订阅 + 集成在 workflow** ⭐ | 1. 符合 DDD 设计原则<br>2. 解耦业务逻辑<br>3. 易于扩展<br>4. 支持异步处理 | 1. 需要额外的事件处理器 | ⭐⭐⭐⭐⭐ |
| **方案B：直接调用 + 集成在 workflow** | 1. 实现简单<br>2. 同步处理 | 1. 耦合度高<br>2. 难以扩展<br>3. 阻塞主流程 | ⭐⭐⭐ |
| **方案C：独立消息服务** | 1. 服务职责清晰<br>2. 可独立部署 | 1. 增加系统复杂度<br>2. 需要跨服务调用<br>3. 增加网络开销 | ⭐⭐ |

### ✅ 推荐方案：事件订阅 + 集成在 workflow

**理由：**
1. ✅ **符合 DDD 设计原则**：领域事件用于解耦业务逻辑
2. ✅ **职责清晰**：待办已办是 workflow 的核心业务，应该集成在 workflow 中
3. ✅ **易于扩展**：可以轻松添加多个事件处理器（消息、通知、审计等）
4. ✅ **支持异步**：使用 `@Async` 异步处理，不阻塞主流程
5. ✅ **解耦**：消息发送逻辑与业务流程解耦

---

## 🏗️ 架构设计

### 整体架构

```
┌─────────────────────────────────────────────────────────┐
│              FlowApplicationService                      │
│  (应用服务层)                                            │
│                                                          │
│  1. 创建流程实例                                         │
│  2. 审批/拒绝/回退                                        │
│  3. 发布领域事件                                         │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│              FlowInstance (聚合根)                      │
│  (领域层)                                                │
│                                                          │
│  1. 业务逻辑处理                                         │
│  2. 添加领域事件到事件列表                               │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│           DomainEventPublisher                          │
│  (基础设施层)                                            │
│                                                          │
│  发布事件到 Spring 事件总线                              │
└─────────────────────────────────────────────────────────┘
                        ↓
        ┌───────────────┴───────────────┐
        ↓                               ↓
┌───────────────────┐         ┌───────────────────┐
│  TaskEventHandler  │         │  MessageEventHandler│
│  (事件处理器)      │         │  (消息事件处理器)   │
│                    │         │                     │
│  1. 创建待办任务   │         │  1. 发送待办消息     │
│  2. 创建已办任务   │         │  2. 发送已办消息     │
└───────────────────┘         └───────────────────┘
        ↓                               ↓
┌───────────────────┐         ┌───────────────────┐
│  TaskService      │         │  RabbitMessageSender│
│  (领域服务)       │         │  (消息发送器)      │
│                    │         │                     │
│  保存到数据库      │         │  发送到 RabbitMQ    │
└───────────────────┘         └───────────────────┘
```

### 事件流程

```
1. 流程启动/节点完成
   ↓
2. FlowInstance 添加领域事件（如 NodeCompletedEvent）
   ↓
3. FlowApplicationService 发布事件
   ↓
4. DomainEventPublisher 发布到 Spring 事件总线
   ↓
5. TaskEventHandler 监听事件
   ↓
6. 创建待办/已办任务（数据库）
   ↓
7. MessageEventHandler 监听事件
   ↓
8. 发送消息到 RabbitMQ
```

---

## 📝 实现方案

### 1. 创建待办已办相关领域事件

```java
// NodeInstanceCreatedEvent.java - 节点实例创建事件
public class NodeInstanceCreatedEvent implements DomainEvent {
    private final Long flowInstanceId;
    private final Long nodeInstanceId;
    private final Long nodeId;
    private final Long approverId;
    private final LocalDateTime occurredOn;
    // ... getters
}

// NodeCompletedEvent.java - 节点完成事件（已存在）
public class NodeCompletedEvent implements DomainEvent {
    private final Long flowInstanceId;
    private final Long nodeInstanceId;
    private final Long nodeId;
    private final Long approverId;
    private final TaskAction action;
    private final LocalDateTime occurredOn;
    // ... getters
}
```

### 2. 在聚合根中发布事件

```java
// FlowInstance.java
public void addNodeInstance(FlowNodeInstance nodeInstance) {
    // ... 业务逻辑 ...
    
    // 发布节点实例创建事件
    addDomainEvent(new NodeInstanceCreatedEvent(
            id != null ? id.getValue() : null,
            nodeInstance.getId(),
            nodeInstance.getNodeId(),
            nodeInstance.getApprover() != null ? nodeInstance.getApprover().getUserId() : null));
}

// FlowNodeInstance.java
public void complete(Long approverId, String comments) {
    // ... 业务逻辑 ...
    
    // 发布节点完成事件
    // 注意：这里需要通过 FlowInstance 发布事件
}
```

### 3. 创建事件处理器

#### TaskEventHandler - 处理待办已办任务

```java
@Component
public class TaskEventHandler {
    
    private final TaskService taskService;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    /**
     * 处理节点实例创建事件 - 创建待办任务
     */
    @EventListener
    @Async
    public void handleNodeInstanceCreated(NodeInstanceCreatedEvent event) {
        log.info("处理节点实例创建事件，节点实例ID: {}", event.getNodeInstanceId());
        
        // 1. 加载流程实例和节点实例
        FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在"));
        
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("节点实例不存在"));
        
        // 2. 创建待办任务
        taskService.createTodoTask(nodeInstance, event.getApproverId(), flowInstance);
    }
    
    /**
     * 处理节点完成事件 - 创建已办任务
     */
    @EventListener
    @Async
    public void handleNodeCompleted(NodeCompletedEvent event) {
        log.info("处理节点完成事件，节点实例ID: {}", event.getNodeInstanceId());
        
        // 1. 加载流程实例和节点实例
        FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在"));
        
        FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                .orElseThrow(() -> new IllegalArgumentException("节点实例不存在"));
        
        // 2. 创建已办任务
        taskService.createDoneTask(flowInstance, nodeInstance, event.getAction(), null);
    }
}
```

#### MessageEventHandler - 发送消息到 RabbitMQ

```java
@Component
public class MessageEventHandler {
    
    private final RabbitMessageSender messageSender;
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowNodeInstanceRepository flowNodeInstanceRepository;
    
    /**
     * 处理节点实例创建事件 - 发送待办消息
     */
    @EventListener
    @Async
    public void handleNodeInstanceCreated(NodeInstanceCreatedEvent event) {
        log.info("发送待办消息，节点实例ID: {}", event.getNodeInstanceId());
        
        try {
            // 1. 加载流程实例和节点实例
            FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException("流程实例不存在"));
            
            FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException("节点实例不存在"));
            
            // 2. 构建待办消息
            TodoTaskMessage message = TodoTaskMessage.builder()
                    .taskId(nodeInstance.getId())
                    .flowInstanceId(event.getFlowInstanceId())
                    .nodeId(event.getNodeId())
                    .approverId(event.getApproverId())
                    .documentId(flowInstance.getDocumentId())
                    .title(flowInstance.getProcessVariables().getVariable("title"))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            // 3. 发送消息到 RabbitMQ
            messageSender.send("workflow.exchange", "todo.task.created", message);
            
            log.info("待办消息发送成功，节点实例ID: {}", event.getNodeInstanceId());
        } catch (Exception e) {
            log.error("待办消息发送失败，节点实例ID: {}", event.getNodeInstanceId(), e);
            // 可以记录到失败队列，后续重试
        }
    }
    
    /**
     * 处理节点完成事件 - 发送已办消息
     */
    @EventListener
    @Async
    public void handleNodeCompleted(NodeCompletedEvent event) {
        log.info("发送已办消息，节点实例ID: {}", event.getNodeInstanceId());
        
        try {
            // 1. 加载流程实例和节点实例
            FlowInstance flowInstance = flowInstanceRepository.findById(event.getFlowInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException("流程实例不存在"));
            
            FlowNodeInstance nodeInstance = flowNodeInstanceRepository.findById(event.getNodeInstanceId())
                    .orElseThrow(() -> new IllegalArgumentException("节点实例不存在"));
            
            // 2. 构建已办消息
            DoneTaskMessage message = DoneTaskMessage.builder()
                    .taskId(nodeInstance.getId())
                    .flowInstanceId(event.getFlowInstanceId())
                    .nodeId(event.getNodeId())
                    .handlerId(event.getApproverId())
                    .action(event.getAction())
                    .documentId(flowInstance.getDocumentId())
                    .title(flowInstance.getProcessVariables().getVariable("title"))
                    .handledAt(LocalDateTime.now())
                    .build();
            
            // 3. 发送消息到 RabbitMQ
            messageSender.send("workflow.exchange", "done.task.created", message);
            
            log.info("已办消息发送成功，节点实例ID: {}", event.getNodeInstanceId());
        } catch (Exception e) {
            log.error("已办消息发送失败，节点实例ID: {}", event.getNodeInstanceId(), e);
            // 可以记录到失败队列，后续重试
        }
    }
}
```

### 4. 消息 DTO

```java
// TodoTaskMessage.java
@Data
@Builder
public class TodoTaskMessage {
    private Long taskId;
    private Long flowInstanceId;
    private Long nodeId;
    private Long approverId;
    private Long documentId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}

// DoneTaskMessage.java
@Data
@Builder
public class DoneTaskMessage {
    private Long taskId;
    private Long flowInstanceId;
    private Long nodeId;
    private Long handlerId;
    private TaskAction action;
    private Long documentId;
    private String title;
    private String comments;
    private LocalDateTime handledAt;
}
```

---

## 🔧 配置

### 1. 添加 RabbitMQ Starter 依赖

```xml
<!-- workflow/pom.xml -->
<dependency>
    <groupId>xtt.cloud</groupId>
    <artifactId>xtt-cloud-starter-rabbitmq</artifactId>
</dependency>
```

### 2. 配置 RabbitMQ

```yaml
# application.yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
```

### 3. 配置异步处理

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }
}
```

---

## 📊 优势总结

### ✅ 为什么选择事件订阅？

1. **解耦**：消息发送逻辑与业务流程解耦
2. **扩展性**：可以轻松添加多个事件处理器
3. **异步处理**：不阻塞主流程
4. **符合 DDD**：使用领域事件进行跨聚合通信

### ✅ 为什么集成在 workflow？

1. **职责清晰**：待办已办是 workflow 的核心业务
2. **数据一致性**：待办已办数据与流程数据在同一事务中
3. **性能**：避免跨服务调用，减少网络开销
4. **简化部署**：不需要额外的服务

---

## 🚀 实施步骤

1. ✅ 创建待办已办相关领域事件（`NodeInstanceCreatedEvent`）
2. ✅ 在聚合根中发布事件
3. ✅ 创建 `TaskEventHandler` 处理待办已办任务
4. ✅ 创建 `MessageEventHandler` 发送消息
5. ✅ 添加 RabbitMQ 依赖和配置
6. ✅ 配置异步处理
7. ✅ 测试消息发送和接收

---

## 📝 注意事项

1. **事务一致性**：待办已办任务创建应该在事务中完成
2. **消息可靠性**：使用 RabbitMQ 的确认机制保证消息不丢失
3. **失败重试**：消息发送失败应该记录到失败队列，后续重试
4. **幂等性**：消息处理应该保证幂等性，避免重复处理
5. **监控告警**：监控消息发送失败率，及时告警

---

## 🎯 总结

**推荐方案：事件订阅 + 集成在 workflow**

- ✅ 通过订阅领域事件触发消息发送
- ✅ 消息生产集成在 workflow 模块中
- ✅ 使用 `@EventListener` + `@Async` 异步处理
- ✅ 使用 `RabbitMessageSender` 发送消息到 RabbitMQ

这样既符合 DDD 设计原则，又保持了系统的简洁性和可维护性。

