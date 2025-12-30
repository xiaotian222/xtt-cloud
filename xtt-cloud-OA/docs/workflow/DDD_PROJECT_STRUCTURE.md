# DDD 项目结构指南

## 📁 标准 DDD 分层架构

DDD 采用**分层架构**，将代码按照职责分为四层：

```
workflow/
├── interfaces/          # 用户接口层（Interface Layer）
├── application/         # 应用层（Application Layer）
├── domain/              # 领域层（Domain Layer）
└── infrastructure/      # 基础设施层（Infrastructure Layer）
```

---

## 🏗️ 详细目录结构

### 完整项目结构

```
xtt-cloud-OA/workflow/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── xtt/cloud/oa/workflow/
│   │   │       │
│   │   │       ├── interfaces/                    # 用户接口层
│   │   │       │   ├── rest/                      # REST API
│   │   │       │   │   ├── flow/
│   │   │       │   │   │   ├── FlowController.java
│   │   │       │   │   │   ├── FreeFlowController.java
│   │   │       │   │   │   └── dto/               # 接口层DTO
│   │   │       │   │   │       ├── StartFlowRequest.java
│   │   │       │   │   │       ├── ApproveRequest.java
│   │   │       │   │   │       ├── RejectRequest.java
│   │   │       │   │   │       └── FlowResponse.java
│   │   │       │   │   └── gw/
│   │   │       │   └── rpc/                        # RPC接口（可选）
│   │   │       │       └── FlowRpcService.java
│   │   │       │
│   │   │       ├── application/                   # 应用层
│   │   │       │   ├── flow/                      # 流程应用服务
│   │   │       │   │   ├── FlowApplicationService.java
│   │   │       │   │   ├── FlowApprovalApplicationService.java
│   │   │       │   │   ├── FlowQueryService.java
│   │   │       │   │   ├── command/                # 命令对象（CQRS）
│   │   │       │   │   │   ├── StartFlowCommand.java
│   │   │       │   │   │   ├── ApproveCommand.java
│   │   │       │   │   │   ├── RejectCommand.java
│   │   │       │   │   │   └── WithdrawCommand.java
│   │   │       │   │   ├── query/                  # 查询对象（CQRS）
│   │   │       │   │   │   ├── FlowInstanceQuery.java
│   │   │       │   │   │   ├── TodoTaskQuery.java
│   │   │       │   │   │   └── FlowHistoryQuery.java
│   │   │       │   │   ├── dto/                    # 应用层DTO
│   │   │       │   │   │   ├── FlowInstanceDTO.java
│   │   │       │   │   │   ├── FlowNodeInstanceDTO.java
│   │   │       │   │   │   └── TodoTaskDTO.java
│   │   │       │   │   └── assembler/              # 组装器（Entity <-> DTO）
│   │   │       │   │       ├── FlowInstanceAssembler.java
│   │   │       │   │       └── FlowNodeInstanceAssembler.java
│   │   │       │   ├── task/                       # 任务应用服务
│   │   │       │   │   └── TaskApplicationService.java
│   │   │       │   └── history/                    # 历史应用服务
│   │   │       │       └── FlowHistoryApplicationService.java
│   │   │       │
│   │   │       ├── domain/                         # 领域层（核心）
│   │   │       │   ├── flow/                      # 流程领域
│   │   │       │   │   ├── model/                  # 领域模型
│   │   │       │   │   │   ├── aggregate/         # 聚合根
│   │   │       │   │   │   │   ├── FlowInstance.java
│   │   │       │   │   │   │   └── FlowDefinition.java
│   │   │       │   │   │   ├── entity/            # 实体
│   │   │       │   │   │   │   ├── FlowNodeInstance.java
│   │   │       │   │   │   │   ├── FlowNode.java
│   │   │       │   │   │   │   └── Document.java
│   │   │       │   │   │   ├── valueobject/        # 值对象
│   │   │       │   │   │   │   ├── FlowStatus.java
│   │   │       │   │   │   │   ├── NodeStatus.java
│   │   │       │   │   │   │   ├── FlowType.java
│   │   │       │   │   │   │   ├── FlowMode.java
│   │   │       │   │   │   │   ├── Approver.java
│   │   │       │   │   │   │   ├── ProcessVariables.java
│   │   │       │   │   │   │   └── FlowInstanceId.java
│   │   │       │   │   │   └── factory/            # 工厂
│   │   │       │   │   │       ├── FlowInstanceFactory.java
│   │   │       │   │   │       └── FlowNodeInstanceFactory.java
│   │   │       │   │   ├── service/                # 领域服务
│   │   │       │   │   │   ├── NodeRoutingService.java
│   │   │       │   │   │   ├── ConditionEvaluationService.java
│   │   │       │   │   │   └── ApproverAssignmentService.java
│   │   │       │   │   ├── repository/              # 仓储接口（领域层定义）
│   │   │       │   │   │   ├── FlowInstanceRepository.java
│   │   │       │   │   │   ├── FlowDefinitionRepository.java
│   │   │       │   │   │   ├── FlowNodeInstanceRepository.java
│   │   │       │   │   │   └── FlowNodeRepository.java
│   │   │       │   │   ├── event/                  # 领域事件
│   │   │       │   │   │   ├── DomainEvent.java
│   │   │       │   │   │   ├── FlowStartedEvent.java
│   │   │       │   │   │   ├── FlowCompletedEvent.java
│   │   │       │   │   │   ├── NodeCompletedEvent.java
│   │   │       │   │   │   └── FlowTerminatedEvent.java
│   │   │       │   │   ├── exception/              # 领域异常
│   │   │       │   │   │   ├── FlowException.java
│   │   │       │   │   │   ├── FlowDefinitionDisabledException.java
│   │   │       │   │   │   ├── NodeCompletionException.java
│   │   │       │   │   │   └── FlowWithdrawException.java
│   │   │       │   │   └── specification/          # 规格模式（可选）
│   │   │       │   │       ├── FlowCanBeWithdrawnSpec.java
│   │   │       │   │       └── FlowCanRollbackSpec.java
│   │   │       │   ├── task/                       # 任务领域
│   │   │       │   │   ├── model/
│   │   │       │   │   │   ├── entity/
│   │   │       │   │   │   │   ├── TodoTask.java
│   │   │       │   │   │   │   └── DoneTask.java
│   │   │       │   │   │   └── valueobject/
│   │   │       │   │   │       └── TaskStatus.java
│   │   │       │   │   └── repository/
│   │   │       │   │       ├── TodoTaskRepository.java
│   │   │       │   │       └── DoneTaskRepository.java
│   │   │       │   └── history/                    # 历史领域
│   │   │       │       ├── model/
│   │   │       │       │   ├── entity/
│   │   │       │       │   │   ├── FlowInstanceHistory.java
│   │   │       │       │   │   ├── ActivityHistory.java
│   │   │       │       │   │   └── TaskHistory.java
│   │   │       │       │   └── valueobject/
│   │   │       │       │       └── ActivityType.java
│   │   │       │       └── repository/
│   │   │       │           ├── FlowInstanceHistoryRepository.java
│   │   │       │           └── ActivityHistoryRepository.java
│   │   │       │
│   │   │       └── infrastructure/                 # 基础设施层
│   │   │           ├── persistence/                # 持久化实现
│   │   │           │   ├── repository/              # 仓储实现
│   │   │           │   │   ├── FlowInstanceRepositoryImpl.java
│   │   │           │   │   ├── FlowDefinitionRepositoryImpl.java
│   │   │           │   │   ├── FlowNodeInstanceRepositoryImpl.java
│   │   │           │   │   └── FlowNodeRepositoryImpl.java
│   │   │           │   ├── mapper/                  # MyBatis Mapper
│   │   │           │   │   ├── FlowInstanceMapper.java
│   │   │           │   │   ├── FlowDefinitionMapper.java
│   │   │           │   │   └── FlowNodeMapper.java
│   │   │           │   └── converter/               # 实体转换器
│   │   │           │       ├── FlowInstanceConverter.java
│   │   │           │       └── FlowNodeInstanceConverter.java
│   │   │           ├── messaging/                   # 消息实现
│   │   │           │   ├── event/                   # 事件发布
│   │   │           │   │   ├── DomainEventPublisher.java
│   │   │           │   │   └── SpringEventPublisher.java
│   │   │           │   └── handler/                 # 事件处理器
│   │   │           │       ├── FlowStartedEventHandler.java
│   │   │           │       └── FlowCompletedEventHandler.java
│   │   │           ├── external/                    # 外部服务调用
│   │   │           │   ├── UserServiceClient.java
│   │   │           │   └── DeptServiceClient.java
│   │   │           ├── cache/                       # 缓存实现
│   │   │           │   └── FlowDefinitionCache.java
│   │   │           └── config/                      # 配置
│   │   │               ├── MyBatisPlusConfig.java
│   │   │               └── EventConfig.java
│   │   │
│   │   └── resources/
│   │       ├── mapper/                              # MyBatis XML
│   │       │   ├── FlowInstanceMapper.xml
│   │       │   └── FlowDefinitionMapper.xml
│   │       └── application.yaml
│   │
│   └── test/
│       └── java/
│           └── xtt/cloud/oa/document/
│               ├── domain/                          # 领域层测试
│               │   └── flow/
│               │       └── model/
│               │           └── aggregate/
│               │               └── FlowInstanceTest.java
│               ├── application/                     # 应用层测试
│               │   └── flow/
│               │       └── FlowApplicationServiceTest.java
│               └── infrastructure/                  # 基础设施层测试
│                   └── persistence/
│                       └── repository/
│                           └── FlowInstanceRepositoryImplTest.java
│
└── pom.xml
```

---

## 📋 各层职责说明

### 1. **interfaces** - 用户接口层

**职责**：
- 接收用户请求
- 参数校验
- 调用应用服务
- 返回响应

**特点**：
- 不包含业务逻辑
- 只负责协议转换（HTTP -> 应用服务调用）
- 使用 DTO 进行数据传输

**示例**：
```java
@RestController
@RequestMapping("/api/flow")
public class FlowController {
    
    private final FlowApplicationService flowApplicationService;
    
    @PostMapping("/start")
    public Result<FlowInstanceDTO> startFlow(@RequestBody StartFlowRequest request) {
        // 1. 参数校验
        validateRequest(request);
        
        // 2. 转换为命令对象
        StartFlowCommand command = StartFlowCommand.builder()
            .documentId(request.getDocumentId())
            .flowDefId(request.getFlowDefId())
            .build();
        
        // 3. 调用应用服务
        FlowInstanceId instanceId = flowApplicationService.startFlow(command);
        
        // 4. 返回结果
        FlowInstanceDTO dto = flowApplicationService.getFlowInstance(instanceId);
        return Result.success(dto);
    }
}
```

---

### 2. **application** - 应用层

**职责**：
- 协调领域对象完成业务流程
- 事务管理
- 调用基础设施服务（如发送消息、记录日志）
- 不包含业务逻辑，只负责编排

**特点**：
- 使用 Command/Query 对象（CQRS）
- 使用 Assembler 进行 Entity <-> DTO 转换
- 一个应用服务方法对应一个用例

**示例**：
```java
@Service
@Transactional
public class FlowApplicationService {
    
    private final FlowInstanceRepository flowInstanceRepository;
    private final FlowDefinitionRepository flowDefinitionRepository;
    private final DocumentRepository documentRepository;
    private final DomainEventPublisher eventPublisher;
    
    /**
     * 启动流程
     * 应用服务方法：协调领域对象完成业务流程
     */
    public FlowInstanceId startFlow(StartFlowCommand command) {
        // 1. 加载聚合根
        FlowDefinition flowDef = flowDefinitionRepository.findById(
            FlowDefinitionId.of(command.getFlowDefId())
        );
        Document document = documentRepository.findById(
            DocumentId.of(command.getDocumentId())
        );
        
        // 2. 创建聚合根
        FlowInstance flowInstance = FlowInstance.create(
            DocumentId.of(command.getDocumentId()),
            FlowDefinitionId.of(command.getFlowDefId())
        );
        
        // 3. 调用领域方法
        flowInstance.start(flowDef, document);
        
        // 4. 保存聚合根
        flowInstanceRepository.save(flowInstance);
        
        // 5. 处理副作用（通过领域事件）
        // 事件已在领域方法中发布，这里不需要额外处理
        
        return flowInstance.getId();
    }
    
    /**
     * 查询流程实例
     */
    @Transactional(readOnly = true)
    public FlowInstanceDTO getFlowInstance(FlowInstanceId id) {
        FlowInstance instance = flowInstanceRepository.findById(id);
        return FlowInstanceAssembler.toDTO(instance);
    }
}
```

---

### 3. **domain** - 领域层（核心）

**职责**：
- 包含所有业务逻辑
- 定义领域模型（聚合根、实体、值对象）
- 定义领域服务
- 定义仓储接口
- 定义领域事件

**特点**：
- **不依赖其他层**（这是最重要的原则）
- 使用领域语言
- 业务规则显式化

#### 3.1 **model/aggregate** - 聚合根

```java
/**
 * 流程实例聚合根
 * 管理流程的生命周期和状态转换
 */
public class FlowInstance {
    private FlowInstanceId id;
    private DocumentId documentId;
    private FlowDefinitionId flowDefId;
    private FlowStatus status;  // 值对象
    private List<FlowNodeInstance> nodeInstances;  // 实体集合
    
    // ========== 业务方法 ==========
    
    public void start(FlowDefinition flowDef, Document document) {
        // 业务规则验证
        if (!flowDef.isEnabled()) {
            throw new FlowDefinitionDisabledException();
        }
        
        // 状态转换
        this.status = FlowStatus.PROCESSING;
        // ...
        
        // 发布领域事件
        DomainEventPublisher.publish(new FlowStartedEvent(this.id));
    }
    
    public void completeNode(NodeInstanceId nodeInstanceId, ApproverId approverId) {
        // 业务逻辑...
    }
}
```

#### 3.2 **model/valueobject** - 值对象

```java
/**
 * 流程状态值对象
 * 不可变对象，封装状态相关的业务规则
 */
public class FlowStatus {
    public static final FlowStatus PROCESSING = new FlowStatus(0, "进行中");
    public static final FlowStatus COMPLETED = new FlowStatus(1, "已完成");
    
    private final int value;
    private final String description;
    
    public boolean canTransitionTo(FlowStatus target) {
        // 状态转换规则
        if (this == PROCESSING) {
            return target == COMPLETED || target == TERMINATED;
        }
        return false;
    }
}
```

#### 3.3 **service** - 领域服务

```java
/**
 * 节点路由领域服务
 * 处理跨聚合的节点路由逻辑
 */
@Service
public class NodeRoutingService {
    
    private final FlowNodeRepository flowNodeRepository;
    
    public List<FlowNode> getNextNodes(FlowDefinitionId flowDefId, FlowNode currentNode) {
        // 领域逻辑...
    }
}
```

#### 3.4 **repository** - 仓储接口

```java
/**
 * 流程实例仓储接口
 * 在领域层定义，在基础设施层实现
 */
public interface FlowInstanceRepository {
    FlowInstance findById(FlowInstanceId id);
    void save(FlowInstance instance);
    void update(FlowInstance instance);
    void delete(FlowInstanceId id);
}
```

#### 3.5 **event** - 领域事件

```java
/**
 * 流程启动事件
 */
public class FlowStartedEvent implements DomainEvent {
    private final FlowInstanceId flowInstanceId;
    private final DocumentId documentId;
    private final LocalDateTime occurredOn;
    
    // ...
}
```

---

### 4. **infrastructure** - 基础设施层

**职责**：
- 实现仓储接口
- 实现持久化（MyBatis、JPA）
- 实现消息发送
- 实现外部服务调用
- 实现缓存

**特点**：
- 依赖领域层
- 实现领域层定义的接口
- 处理技术细节

**示例**：
```java
/**
 * 流程实例仓储实现
 */
@Repository
public class FlowInstanceRepositoryImpl implements FlowInstanceRepository {
    
    private final FlowInstanceMapper mapper;
    private final FlowInstanceConverter converter;
    
    @Override
    public FlowInstance findById(FlowInstanceId id) {
        FlowInstancePO po = mapper.selectById(id.getValue());
        return converter.toDomain(po);
    }
    
    @Override
    public void save(FlowInstance instance) {
        FlowInstancePO po = converter.toPO(instance);
        mapper.insert(po);
    }
}
```

---

## 🔄 依赖关系

```
interfaces (依赖) -> application (依赖) -> domain
                                          ↑
infrastructure (依赖) --------------------┘
```

**重要原则**：
- **领域层不依赖任何其他层**
- **应用层依赖领域层**
- **基础设施层依赖领域层**
- **接口层依赖应用层**

---

## 📦 包命名规范

### 1. 聚合根
```
domain.flow.model.aggregate.FlowInstance
```

### 2. 实体
```
domain.flow.model.entity.FlowNodeInstance
```

### 3. 值对象
```
domain.flow.model.valueobject.FlowStatus
```

### 4. 领域服务
```
domain.flow.service.NodeRoutingService
```

### 5. 仓储接口
```
domain.flow.repository.FlowInstanceRepository
```

### 6. 领域事件
```
domain.flow.event.FlowStartedEvent
```

### 7. 应用服务
```
application.flow.FlowApplicationService
```

### 8. 仓储实现
```
infrastructure.persistence.repository.FlowInstanceRepositoryImpl
```

---

## 🎯 重构迁移建议

### 阶段一：创建新结构（不破坏现有代码）

1. **创建值对象**
   ```
   domain/flow/model/valueobject/
   ├── FlowStatus.java
   ├── NodeStatus.java
   └── Approver.java
   ```

2. **创建领域服务**
   ```
   domain/flow/service/
   ├── NodeRoutingService.java
   └── ConditionEvaluationService.java
   ```

3. **创建领域事件**
   ```
   domain/flow/event/
   ├── FlowStartedEvent.java
   └── FlowCompletedEvent.java
   ```

### 阶段二：重构聚合根

1. **重构 FlowInstance**
   - 将业务逻辑从 Service 移到 FlowInstance
   - 使用值对象替代基本类型

2. **重构 FlowNodeInstance**
   - 添加业务方法
   - 使用值对象

### 阶段三：重构应用服务

1. **创建应用服务**
   ```
   application/flow/
   ├── FlowApplicationService.java
   └── FlowApprovalApplicationService.java
   ```

2. **简化应用服务逻辑**
   - 只负责协调
   - 调用领域方法

### 阶段四：重构基础设施层

1. **创建仓储实现**
   ```
   infrastructure/persistence/repository/
   └── FlowInstanceRepositoryImpl.java
   ```

2. **实现事件发布**
   ```
   infrastructure/messaging/event/
   └── SpringEventPublisher.java
   ```

---

## ✅ 检查清单

- [ ] 领域层不依赖其他层
- [ ] 每个聚合根有明确的边界
- [ ] 业务逻辑在领域层，不在应用层
- [ ] 使用值对象替代基本类型
- [ ] 仓储接口在领域层，实现在基础设施层
- [ ] 领域事件用于跨聚合通信
- [ ] 应用服务只负责协调，不包含业务逻辑

---

## 📚 参考资源

1. 《领域驱动设计》- Eric Evans
2. 《实现领域驱动设计》- Vaughn Vernon
3. 《架构整洁之道》- Robert C. Martin


