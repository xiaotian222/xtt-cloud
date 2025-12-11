# 流程引擎核心逻辑设计文档

## 📋 概述

本文档详细设计流程引擎的核心逻辑，包括节点流转、审批处理、并行流程、条件分支等关键功能。

---

## 🏗️ 架构设计

### 核心组件

```
流程引擎
├── FlowDefinitionService      # 流程定义管理
├── FlowNodeService            # 流程节点管理
├── FlowEngineService          # 流程引擎核心（节点流转）
├── FlowApprovalService        # 审批处理服务
├── TodoService                # 待办事项服务
└── DoneService                # 已办事项服务
```

---

## 📊 数据模型

### 核心实体关系

```
FlowDefinition (流程定义)
    ↓ 1:N
FlowNode (流程节点定义)
    ↓ 1:N
FlowInstance (流程实例)
    ↓ 1:N
FlowNodeInstance (节点实例)
    ↓ 1:N
TodoItem (待办事项)
    ↓ 1:1
DoneItem (已办事项)
```

---

## 🔄 流程执行流程

### 1. 流程启动流程

```
1. 创建流程实例 (FlowInstance)
   ↓
2. 根据流程定义加载节点列表
   ↓
3. 创建第一个节点实例 (FlowNodeInstance)
   ↓
4. 根据节点定义分配审批人
   ↓
5. 生成待办事项 (TodoItem)
   ↓
6. 更新流程实例状态为"进行中"
```

### 2. 节点流转流程

```
当前节点处理完成
   ↓
判断节点类型和并行模式
   ↓
串行节点 → 创建下一个节点实例
并行节点 → 创建多个并行节点实例
条件节点 → 根据条件判断下一个节点
   ↓
分配审批人并生成待办事项
   ↓
更新流程实例当前节点
```

### 3. 审批处理流程

```
审批人处理待办事项
   ↓
记录审批意见和操作类型
   ↓
更新节点实例状态
   ↓
创建已办事项记录
   ↓
判断是否所有节点完成
   ↓
是 → 流程结束，更新公文状态
否 → 流转到下一个节点
```

---

## 🎯 核心功能设计

### 1. 流程定义管理

#### FlowDefinition 实体

```java
public class FlowDefinition {
    private Long id;
    private String name;           // 流程名称
    private String code;           // 流程编码（唯一）
    private Long docTypeId;        // 适用公文类型ID
    private String description;    // 流程描述
    private Integer version;       // 版本号
    private Integer status;        // 状态（0:停用,1:启用）
    private Long creatorId;        // 创建人ID
    private List<FlowNode> nodes;  // 节点列表
}
```

#### 功能需求

- [ ] 创建流程定义
- [ ] 更新流程定义
- [ ] 启用/停用流程定义
- [ ] 查询流程定义列表
- [ ] 根据公文类型匹配流程定义

---

### 2. 流程节点定义管理

#### FlowNode 实体

```java
public class FlowNode {
    private Long id;
    private Long flowDefId;        // 流程定义ID
    private String nodeName;       // 节点名称
    private Integer nodeType;      // 节点类型
    private Integer approverType;  // 审批人类型
    private String approverValue;  // 审批人值
    private Integer orderNum;      // 节点顺序
    private String skipCondition;  // 跳过条件
    private Integer required;      // 是否必须
    private Integer parallelMode;  // 并行模式
    private Long nextNodeId;       // 下一个节点ID（串行）
    private List<Long> nextNodeIds; // 下一个节点ID列表（并行/条件）
}
```

#### 节点类型常量

```java
public static final int NODE_TYPE_APPROVAL = 1;  // 审批节点
public static final int NODE_TYPE_NOTIFY = 2;   // 抄送节点
public static final int NODE_TYPE_CONDITION = 3; // 条件节点
public static final int NODE_TYPE_AUTO = 4;      // 自动节点
```

#### 审批人类型常量

```java
public static final int APPROVER_TYPE_USER = 1;        // 指定人员
public static final int APPROVER_TYPE_ROLE = 2;        // 指定角色
public static final int APPROVER_TYPE_DEPT_LEADER = 3; // 指定部门负责人
public static final int APPROVER_TYPE_INITIATOR = 4;   // 发起人指定
```

#### 并行模式常量

```java
public static final int PARALLEL_MODE_SERIAL = 0;   // 串行
public static final int PARALLEL_MODE_PARALLEL = 1;  // 并行
```

---

### 3. 节点实例管理

#### FlowNodeInstance 实体

```java
public class FlowNodeInstance {
    private Long id;
    private Long flowInstanceId;   // 流程实例ID
    private Long nodeId;            // 节点定义ID
    private Long approverId;        // 审批人ID
    private Long approverDeptId;    // 审批人部门ID
    private Integer status;          // 节点状态
    private String comments;         // 审批意见
    private LocalDateTime handledAt; // 处理时间
}
```

#### 节点状态常量

```java
public static final int STATUS_PENDING = 0;    // 待处理
public static final int STATUS_PROCESSING = 1; // 处理中
public static final int STATUS_COMPLETED = 2;  // 已完成
public static final int STATUS_REJECTED = 3;    // 已拒绝
public static final int STATUS_SKIPPED = 4;     // 已跳过
```

---

### 4. 流程引擎核心逻辑

#### FlowEngineService 核心方法

```java
@Service
public class FlowEngineService {
    
    /**
     * 启动流程
     * 1. 创建流程实例
     * 2. 加载流程定义和节点列表
     * 3. 创建第一个节点实例
     * 4. 分配审批人
     * 5. 生成待办事项
     */
    public FlowInstance startFlow(Long documentId, Long flowDefId);
    
    /**
     * 处理节点审批
     * 1. 验证审批权限
     * 2. 更新节点实例状态
     * 3. 创建已办事项
     * 4. 判断是否流转到下一个节点
     */
    public void processNodeApproval(Long nodeInstanceId, String action, String comments, Long approverId);
    
    /**
     * 流转到下一个节点
     * 1. 判断当前节点类型和并行模式
     * 2. 创建下一个节点实例（串行/并行）
     * 3. 分配审批人
     * 4. 生成待办事项
     * 5. 更新流程实例当前节点
     */
    private void moveToNextNode(Long flowInstanceId, Long currentNodeInstanceId);
    
    /**
     * 判断流程是否结束
     * 检查是否所有节点都已完成
     */
    private boolean isFlowCompleted(Long flowInstanceId);
    
    /**
     * 结束流程
     * 1. 更新流程实例状态为"已完成"
     * 2. 更新公文状态为"已发布"
     */
    private void completeFlow(Long flowInstanceId);
}
```

---

### 5. 审批处理逻辑

#### 审批操作类型

```java
public static final String ACTION_APPROVE = "approve";  // 同意
public static final String ACTION_REJECT = "reject";    // 拒绝
public static final String ACTION_FORWARD = "forward";  // 转发
public static final String ACTION_RETURN = "return";   // 退回
public static final String ACTION_DELEGATE = "delegate"; // 委派
```

#### 审批处理流程

```
1. 验证审批权限
   - 检查审批人是否是当前节点实例的审批人
   - 检查节点实例状态是否为"待处理"
   
2. 更新节点实例
   - 设置审批意见
   - 更新状态（已完成/已拒绝）
   - 记录处理时间
   
3. 创建已办事项
   - 记录处理操作、意见、时间
   
4. 判断操作类型
   - 同意 → 流转到下一个节点
   - 拒绝 → 流程终止或退回
   - 转发 → 创建新的节点实例
   - 退回 → 流转到上一个节点
```

---

### 6. 并行流程处理

#### 并行节点启动

```
1. 识别并行节点
   - 检查节点的 parallelMode = 1
   
2. 创建多个并行节点实例
   - 根据审批人列表创建多个节点实例
   - 每个节点实例分配不同的审批人
   
3. 生成多个待办事项
   - 为每个节点实例生成待办事项
   
4. 等待所有并行节点完成
   - 检查所有并行节点实例的状态
   - 所有节点都完成后，流转到下一个节点
```

#### 并行节点汇聚

```
所有并行节点实例状态检查
   ↓
全部完成 → 流转到下一个节点
部分完成 → 继续等待
有拒绝 → 根据规则处理（全部拒绝/部分拒绝）
```

---

### 7. 条件分支处理

#### 条件节点逻辑

```
1. 解析跳过条件
   - 条件表达式（如：secretLevel > 1）
   - 基于公文属性或流程变量
   
2. 评估条件
   - 执行条件表达式
   - 返回 true/false
   
3. 选择下一个节点
   - true → 流转到节点A
   - false → 流转到节点B
```

#### 条件表达式示例

```java
// 示例条件
"secretLevel > 1"              // 密级大于1
"urgencyLevel == 2"            // 紧急程度为特急
"deptId == 1"                  // 部门ID为1
"wordCount > 1000"              // 字数大于1000
```

---

### 8. 审批人分配逻辑

#### 根据审批人类型分配

```java
private List<Long> assignApprovers(FlowNode node, FlowInstance flowInstance) {
    switch (node.getApproverType()) {
        case APPROVER_TYPE_USER:
            // 指定人员：直接返回用户ID列表
            return parseUserIds(node.getApproverValue());
            
        case APPROVER_TYPE_ROLE:
            // 指定角色：查询该角色下的所有用户
            return userService.getUserIdsByRole(node.getApproverValue());
            
        case APPROVER_TYPE_DEPT_LEADER:
            // 指定部门负责人：查询部门负责人
            return userService.getDeptLeaderIds(node.getApproverValue());
            
        case APPROVER_TYPE_INITIATOR:
            // 发起人指定：从流程实例中获取发起人指定的审批人
            return getApproversFromFlowInstance(flowInstance);
            
        default:
            throw new BusinessException("不支持的审批人类型");
    }
}
```

---

### 9. 待办事项生成

#### TodoItem 实体

```java
public class TodoItem {
    private Long id;
    private Long documentId;      // 公文ID
    private Long flowInstanceId;   // 流程实例ID
    private Long nodeInstanceId;   // 节点实例ID
    private Long assigneeId;       // 处理人ID
    private String title;          // 待办标题
    private String content;        // 待办内容
    private Integer priority;      // 优先级
    private Integer status;         // 状态
    private LocalDateTime dueDate;  // 截止时间
}
```

#### 生成逻辑

```java
private void createTodoItem(FlowNodeInstance nodeInstance, Long approverId) {
    TodoItem todo = new TodoItem();
    todo.setDocumentId(flowInstance.getDocumentId());
    todo.setFlowInstanceId(flowInstance.getId());
    todo.setNodeInstanceId(nodeInstance.getId());
    todo.setAssigneeId(approverId);
    todo.setTitle("审批公文: " + document.getTitle());
    todo.setContent("请审批公文《" + document.getTitle() + "》");
    todo.setPriority(document.getUrgencyLevel());
    todo.setStatus(TodoItem.STATUS_PENDING);
    todo.setDueDate(calculateDueDate(nodeInstance));
    todoService.create(todo);
}
```

---

### 10. 已办事项记录

#### DoneItem 实体

```java
public class DoneItem {
    private Long id;
    private Long documentId;      // 公文ID
    private Long flowInstanceId;  // 流程实例ID
    private Long nodeInstanceId;  // 节点实例ID
    private Long handlerId;       // 处理人ID
    private String title;         // 已办标题
    private String action;        // 操作类型
    private String comments;      // 处理意见
    private LocalDateTime handledAt; // 处理时间
}
```

#### 创建逻辑

```java
private void createDoneItem(FlowNodeInstance nodeInstance, String action, String comments) {
    DoneItem done = new DoneItem();
    done.setDocumentId(flowInstance.getDocumentId());
    done.setFlowInstanceId(flowInstance.getId());
    done.setNodeInstanceId(nodeInstance.getId());
    done.setHandlerId(nodeInstance.getApproverId());
    done.setTitle("审批公文: " + document.getTitle());
    done.setAction(action);
    done.setComments(comments);
    done.setHandledAt(LocalDateTime.now());
    doneService.create(done);
}
```

---

## 🔀 节点流转算法

### 串行流转

```
当前节点完成
   ↓
查找下一个节点（orderNum + 1）
   ↓
创建节点实例
   ↓
分配审批人
   ↓
生成待办事项
```

### 并行流转

```
当前节点完成
   ↓
识别并行节点组（相同 orderNum）
   ↓
为每个审批人创建节点实例
   ↓
为每个节点实例生成待办事项
   ↓
等待所有并行节点完成
   ↓
汇聚到下一个节点
```

### 条件流转

```
当前节点完成
   ↓
评估条件表达式
   ↓
根据条件结果选择下一个节点
   ↓
创建节点实例
   ↓
分配审批人
   ↓
生成待办事项
```

---

## 📝 关键算法伪代码

### 1. 流程启动算法

```java
public FlowInstance startFlow(Long documentId, Long flowDefId) {
    // 1. 加载流程定义
    FlowDefinition flowDef = flowDefinitionService.getById(flowDefId);
    List<FlowNode> nodes = flowNodeService.getByFlowDefId(flowDefId);
    
    // 2. 创建流程实例
    FlowInstance instance = new FlowInstance();
    instance.setDocumentId(documentId);
    instance.setFlowDefId(flowDefId);
    instance.setStatus(FlowInstance.STATUS_PROCESSING);
    instance.setStartTime(LocalDateTime.now());
    flowInstanceMapper.insert(instance);
    
    // 3. 创建第一个节点实例
    FlowNode firstNode = nodes.stream()
        .min(Comparator.comparing(FlowNode::getOrderNum))
        .orElseThrow();
    
    createNodeInstance(instance, firstNode);
    
    return instance;
}
```

### 2. 节点流转算法

```java
private void moveToNextNode(Long flowInstanceId, Long currentNodeInstanceId) {
    FlowInstance instance = flowInstanceMapper.selectById(flowInstanceId);
    FlowNodeInstance currentNode = nodeInstanceMapper.selectById(currentNodeInstanceId);
    FlowNode currentNodeDef = flowNodeMapper.selectById(currentNode.getNodeId());
    
    // 判断节点类型
    if (currentNodeDef.getParallelMode() == PARALLEL_MODE_PARALLEL) {
        // 并行节点：检查所有并行节点是否完成
        if (allParallelNodesCompleted(flowInstanceId, currentNodeDef)) {
            // 所有并行节点完成，流转到下一个节点
            FlowNode nextNode = getNextNode(currentNodeDef);
            if (nextNode != null) {
                createNodeInstance(instance, nextNode);
            } else {
                // 没有下一个节点，流程结束
                completeFlow(flowInstanceId);
            }
        }
    } else {
        // 串行节点：直接流转到下一个节点
        FlowNode nextNode = getNextNode(currentNodeDef);
        if (nextNode != null) {
            createNodeInstance(instance, nextNode);
        } else {
            completeFlow(flowInstanceId);
        }
    }
}
```

### 3. 审批处理算法

```java
public void processNodeApproval(Long nodeInstanceId, String action, String comments, Long approverId) {
    // 1. 验证权限
    FlowNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
    if (!nodeInstance.getApproverId().equals(approverId)) {
        throw new BusinessException("无权审批此节点");
    }
    if (nodeInstance.getStatus() != FlowNodeInstance.STATUS_PENDING) {
        throw new BusinessException("节点状态不正确");
    }
    
    // 2. 更新节点实例
    nodeInstance.setStatus(
        ACTION_APPROVE.equals(action) ? FlowNodeInstance.STATUS_COMPLETED : 
        FlowNodeInstance.STATUS_REJECTED
    );
    nodeInstance.setComments(comments);
    nodeInstance.setHandledAt(LocalDateTime.now());
    nodeInstanceMapper.updateById(nodeInstance);
    
    // 3. 创建已办事项
    createDoneItem(nodeInstance, action, comments);
    
    // 4. 更新待办事项状态
    todoService.markAsHandled(nodeInstanceId, approverId);
    
    // 5. 判断操作类型
    if (ACTION_APPROVE.equals(action)) {
        // 同意：流转到下一个节点
        moveToNextNode(nodeInstance.getFlowInstanceId(), nodeInstanceId);
    } else if (ACTION_REJECT.equals(action)) {
        // 拒绝：流程终止或退回
        handleReject(nodeInstance.getFlowInstanceId());
    } else if (ACTION_FORWARD.equals(action)) {
        // 转发：创建新的节点实例
        handleForward(nodeInstance, comments);
    }
}
```

---

## 🎯 实现优先级

### 第一阶段：基础功能（必须实现）

1. ✅ **流程定义管理**
   - 创建、查询流程定义
   - 节点定义管理

2. ✅ **流程启动**
   - 创建流程实例
   - 创建第一个节点实例
   - 生成待办事项

3. ✅ **串行节点流转**
   - 节点完成后的流转逻辑
   - 下一个节点的创建

4. ✅ **审批处理**
   - 同意/拒绝操作
   - 节点状态更新
   - 已办事项记录

### 第二阶段：高级功能（重要）

5. ⚠️ **并行流程处理**
   - 并行节点创建
   - 并行节点汇聚
   - 并行节点完成判断

6. ⚠️ **条件分支处理**
   - 条件表达式解析
   - 条件评估
   - 条件分支选择

7. ⚠️ **审批人自动分配**
   - 根据角色分配
   - 根据部门负责人分配
   - 发起人指定

### 第三阶段：增强功能（可选）

8. ⏸️ **转发和委派**
   - 审批转发
   - 审批委派

9. ⏸️ **退回和撤回**
   - 退回上一节点
   - 流程撤回

10. ⏸️ **超时处理**
    - 超时提醒
    - 超时自动处理

---

## 🔧 技术实现要点

### 1. 事务管理

- 流程启动、节点流转、审批处理都需要在事务中执行
- 使用 `@Transactional` 保证数据一致性

### 2. 并发控制

- 使用数据库锁或乐观锁防止并发问题
- 节点实例状态更新时加锁

### 3. 异步处理

- 待办事项生成可以异步化
- 通知发送可以异步化
- 使用消息队列解耦

### 4. 性能优化

- 流程定义和节点定义可以缓存
- 审批人分配结果可以缓存
- 使用批量操作减少数据库访问

---

## 📊 状态机设计

### 流程实例状态机

```
创建 → 进行中 → 已完成
         ↓
       已终止
```

### 节点实例状态机

```
创建 → 待处理 → 处理中 → 已完成
              ↓
            已拒绝
              ↓
            已跳过
```

---

## 🚀 下一步实现计划

1. **创建实体类**
   - FlowDefinition
   - FlowNode
   - FlowNodeInstance
   - TodoItem
   - DoneItem

2. **创建 Mapper 接口**
   - FlowDefinitionMapper
   - FlowNodeMapper
   - FlowNodeInstanceMapper
   - TodoItemMapper
   - DoneItemMapper

3. **实现核心服务**
   - FlowEngineService（流程引擎核心）
   - FlowApprovalService（审批处理）
   - TodoService（待办管理）
   - DoneService（已办管理）

4. **实现 REST API**
   - 流程启动接口
   - 审批处理接口
   - 待办查询接口
   - 已办查询接口

---

**设计时间**: 2023.0.3.3  
**设计人**: XTT Cloud Team

