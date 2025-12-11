# 自由流（Free Flow）设计文档

## 📋 概述

自由流是一种动态流程模式，允许审批人根据当前文件状态和自身角色，动态选择下一步操作和审批人，而不是按照预定义的流程节点顺序执行。

---

## 🎯 核心特性

### 1. 动态发送动作

根据当前文件状态和用户角色，系统提供可用的发送动作：

- **单位内办理**：发送给单位内部部门或人员
- **核稿**：发送给核稿组或核稿人员
- **转外单位办理**：发送给外单位
- **返回**：退回给上一节点或发起人

### 2. 动态审批人选择

每个发送动作对应一个审批人选择范围：

- **部门**：选择部门，系统自动分配给部门负责人或部门内人员
- **人员**：直接选择具体人员
- **部门+人员**：选择部门，然后从该部门中选择具体人员

### 3. 状态和角色驱动

- 根据文件当前状态（草稿、审核中、已发布等）
- 根据用户角色（管理员、部门经理、普通用户等）
- 动态决定可用的发送动作

---

## 🏗️ 数据模型设计

### 1. 发送动作定义表 (doc_flow_action)

```sql
CREATE TABLE doc_flow_action (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  action_code     VARCHAR(32) NOT NULL UNIQUE COMMENT '动作编码',
  action_name     VARCHAR(64) NOT NULL COMMENT '动作名称',
  action_type     TINYINT NOT NULL COMMENT '动作类型(1:单位内办理,2:核稿,3:转外单位,4:返回)',
  description     VARCHAR(255) COMMENT '动作描述',
  icon            VARCHAR(128) COMMENT '图标',
  enabled         TINYINT DEFAULT 1 COMMENT '是否启用',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2. 动作规则表 (doc_flow_action_rule)

```sql
CREATE TABLE doc_flow_action_rule (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  action_id       BIGINT NOT NULL COMMENT '动作ID',
  document_status TINYINT COMMENT '文件状态(0:草稿,1:审核中,2:已发布,3:已归档)',
  user_role       VARCHAR(64) COMMENT '用户角色(支持多个，逗号分隔)',
  dept_id         BIGINT COMMENT '部门ID(可选，限制特定部门)',
  priority        INT DEFAULT 0 COMMENT '优先级',
  enabled         TINYINT DEFAULT 1 COMMENT '是否启用',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3. 审批人选择范围表 (doc_flow_approver_scope)

```sql
CREATE TABLE doc_flow_approver_scope (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  action_id       BIGINT NOT NULL COMMENT '动作ID',
  scope_type      TINYINT NOT NULL COMMENT '范围类型(1:部门,2:人员,3:部门+人员)',
  dept_ids        TEXT COMMENT '可选部门ID列表(JSON数组)',
  user_ids        TEXT COMMENT '可选人员ID列表(JSON数组)',
  role_codes      VARCHAR(255) COMMENT '可选角色编码(逗号分隔)',
  allow_custom    TINYINT DEFAULT 0 COMMENT '是否允许自定义选择',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 4. 自由流节点实例扩展表 (doc_flow_free_node_instance)

```sql
CREATE TABLE doc_flow_free_node_instance (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  node_instance_id BIGINT NOT NULL COMMENT '节点实例ID',
  action_id       BIGINT NOT NULL COMMENT '发送动作ID',
  action_name     VARCHAR(64) COMMENT '动作名称',
  selected_dept_ids TEXT COMMENT '选择的部门ID列表(JSON数组)',
  selected_user_ids TEXT COMMENT '选择的人员ID列表(JSON数组)',
  custom_comment  VARCHAR(512) COMMENT '自定义备注',
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 📊 实体类设计

### 1. FlowAction（发送动作）

```java
public class FlowAction {
    private Long id;
    private String actionCode;      // 动作编码（UNIT_HANDLE, REVIEW, EXTERNAL, RETURN）
    private String actionName;       // 动作名称
    private Integer actionType;      // 动作类型
    private String description;      // 描述
    private String icon;            // 图标
    private Integer enabled;        // 是否启用
    
    // 动作类型常量
    public static final int TYPE_UNIT_HANDLE = 1;    // 单位内办理
    public static final int TYPE_REVIEW = 2;          // 核稿
    public static final int TYPE_EXTERNAL = 3;        // 转外单位办理
    public static final int TYPE_RETURN = 4;         // 返回
}
```

### 2. FlowActionRule（动作规则）

```java
public class FlowActionRule {
    private Long id;
    private Long actionId;          // 动作ID
    private Integer documentStatus; // 文件状态
    private String userRole;        // 用户角色（支持多个，逗号分隔）
    private Long deptId;           // 部门ID（可选）
    private Integer priority;      // 优先级
    private Integer enabled;        // 是否启用
}
```

### 3. ApproverScope（审批人选择范围）

```java
public class ApproverScope {
    private Long id;
    private Long actionId;          // 动作ID
    private Integer scopeType;      // 范围类型
    private List<Long> deptIds;     // 可选部门ID列表
    private List<Long> userIds;     // 可选人员ID列表
    private List<String> roleCodes; // 可选角色编码列表
    private Integer allowCustom;    // 是否允许自定义选择
    
    // 范围类型常量
    public static final int SCOPE_TYPE_DEPT = 1;      // 部门
    public static final int SCOPE_TYPE_USER = 2;      // 人员
    public static final int SCOPE_TYPE_DEPT_USER = 3; // 部门+人员
}
```

### 4. FreeFlowNodeInstance（自由流节点实例扩展）

```java
public class FreeFlowNodeInstance {
    private Long id;
    private Long nodeInstanceId;    // 节点实例ID
    private Long actionId;          // 发送动作ID
    private String actionName;      // 动作名称
    private List<Long> selectedDeptIds;  // 选择的部门ID列表
    private List<Long> selectedUserIds;  // 选择的人员ID列表
    private String customComment;   // 自定义备注
}
```

---

## 🔄 自由流执行流程

### 1. 获取可用发送动作

```
1. 获取当前文件状态
2. 获取当前用户角色
3. 查询匹配的动作规则
4. 返回可用的发送动作列表
```

### 2. 获取审批人选择范围

```
1. 根据选择的发送动作
2. 查询对应的审批人选择范围
3. 根据范围类型返回：
   - 部门列表
   - 人员列表
   - 部门+人员组合
```

### 3. 执行发送动作

```
1. 验证发送动作是否可用
2. 验证选择的审批人是否在范围内
3. 创建新的节点实例
4. 分配审批人
5. 生成待办事项
6. 记录自由流节点扩展信息
```

---

## 💻 核心服务设计

### FreeFlowService（自由流服务）

```java
@Service
public class FreeFlowService {
    
    /**
     * 获取当前用户可用的发送动作
     * 根据文件状态和用户角色动态计算
     */
    public List<FlowAction> getAvailableActions(Long documentId, Long userId);
    
    /**
     * 获取发送动作对应的审批人选择范围
     */
    public ApproverScope getApproverScope(Long actionId);
    
    /**
     * 执行发送动作
     * 创建新的节点实例并分配审批人
     */
    public FlowNodeInstance executeAction(
        Long currentNodeInstanceId,
        Long actionId,
        List<Long> selectedDeptIds,
        List<Long> selectedUserIds,
        String comment,
        Long operatorId
    );
    
    /**
     * 验证发送动作是否可用
     */
    private boolean isActionAvailable(Long actionId, Long documentId, Long userId);
    
    /**
     * 验证选择的审批人是否在范围内
     */
    private boolean validateApproverScope(
        Long actionId,
        List<Long> deptIds,
        List<Long> userIds
    );
}
```

---

## 🎯 关键算法

### 算法1：获取可用发送动作

```java
public List<FlowAction> getAvailableActions(Long documentId, Long userId) {
    // 1. 获取文件信息
    Document document = documentMapper.selectById(documentId);
    
    // 2. 获取用户信息
    UserInfoDto user = userService.getUserById(userId);
    List<String> userRoles = user.getRoles().stream()
        .map(RoleInfoDto::getCode)
        .collect(Collectors.toList());
    
    // 3. 查询匹配的动作规则
    List<FlowActionRule> rules = actionRuleMapper.selectList(
        new LambdaQueryWrapper<FlowActionRule>()
            .eq(FlowActionRule::getDocumentStatus, document.getStatus())
            .in(FlowActionRule::getUserRole, userRoles)
            .eq(FlowActionRule::getEnabled, 1)
            .orderByDesc(FlowActionRule::getPriority)
    );
    
    // 4. 获取对应的动作
    List<Long> actionIds = rules.stream()
        .map(FlowActionRule::getActionId)
        .distinct()
        .collect(Collectors.toList());
    
    return actionMapper.selectBatchIds(actionIds);
}
```

### 算法2：获取审批人选择范围

```java
public ApproverScope getApproverScope(Long actionId) {
    ApproverScope scope = approverScopeMapper.selectOne(
        new LambdaQueryWrapper<ApproverScope>()
            .eq(ApproverScope::getActionId, actionId)
    );
    
    if (scope == null) {
        // 如果没有配置，返回默认范围（所有部门和人员）
        scope = createDefaultScope(actionId);
    }
    
    return scope;
}
```

### 算法3：执行发送动作

```java
@Transactional
public FlowNodeInstance executeAction(
        Long currentNodeInstanceId,
        Long actionId,
        List<Long> selectedDeptIds,
        List<Long> selectedUserIds,
        String comment,
        Long operatorId) {
    
    // 1. 验证权限
    FlowNodeInstance currentNode = validateCurrentNode(currentNodeInstanceId, operatorId);
    
    // 2. 验证动作可用性
    if (!isActionAvailable(actionId, currentNode.getFlowInstanceId(), operatorId)) {
        throw new BusinessException("该发送动作不可用");
    }
    
    // 3. 验证审批人范围
    ApproverScope scope = getApproverScope(actionId);
    if (!validateApproverScope(scope, selectedDeptIds, selectedUserIds)) {
        throw new BusinessException("选择的审批人不在允许范围内");
    }
    
    // 4. 根据范围类型分配审批人
    List<Long> approverIds = assignApprovers(scope, selectedDeptIds, selectedUserIds);
    
    // 5. 创建新的节点实例（自由流节点）
    FlowInstance flowInstance = flowInstanceMapper.selectById(currentNode.getFlowInstanceId());
    FlowAction action = actionMapper.selectById(actionId);
    
    // 创建自由流节点定义（动态创建）
    FlowNode freeNode = createFreeFlowNode(action, flowInstance);
    
    // 为每个审批人创建节点实例
    List<FlowNodeInstance> nodeInstances = new ArrayList<>();
    for (Long approverId : approverIds) {
        FlowNodeInstance nodeInstance = new FlowNodeInstance();
        nodeInstance.setFlowInstanceId(flowInstance.getId());
        nodeInstance.setNodeId(freeNode.getId());
        nodeInstance.setApproverId(approverId);
        nodeInstance.setStatus(FlowNodeInstance.STATUS_PENDING);
        nodeInstanceMapper.insert(nodeInstance);
        nodeInstances.add(nodeInstance);
        
        // 创建自由流节点扩展信息
        FreeFlowNodeInstance freeNodeInstance = new FreeFlowNodeInstance();
        freeNodeInstance.setNodeInstanceId(nodeInstance.getId());
        freeNodeInstance.setActionId(actionId);
        freeNodeInstance.setActionName(action.getActionName());
        freeNodeInstance.setSelectedDeptIds(selectedDeptIds);
        freeNodeInstance.setSelectedUserIds(selectedUserIds);
        freeNodeInstance.setCustomComment(comment);
        freeFlowNodeInstanceMapper.insert(freeNodeInstance);
        
        // 生成待办事项
        createTodoItem(nodeInstance, approverId, action);
    }
    
    // 6. 更新当前节点实例状态为"已完成"
    currentNode.setStatus(FlowNodeInstance.STATUS_COMPLETED);
    currentNode.setComments(comment);
    currentNode.setHandledAt(LocalDateTime.now());
    nodeInstanceMapper.updateById(currentNode);
    
    // 7. 创建已办事项
    createDoneItem(currentNode, "forward", comment);
    
    // 8. 更新流程实例当前节点
    flowInstance.setCurrentNodeId(freeNode.getId());
    flowInstanceMapper.updateById(flowInstance);
    
    return nodeInstances.get(0);
}
```

### 算法4：根据范围类型分配审批人

```java
private List<Long> assignApprovers(
        ApproverScope scope,
        List<Long> selectedDeptIds,
        List<Long> selectedUserIds) {
    
    List<Long> approverIds = new ArrayList<>();
    
    switch (scope.getScopeType()) {
        case ApproverScope.SCOPE_TYPE_DEPT:
            // 部门类型：获取部门负责人或部门内所有人员
            if (selectedDeptIds != null && !selectedDeptIds.isEmpty()) {
                for (Long deptId : selectedDeptIds) {
                    // 获取部门负责人
                    Long deptLeaderId = userService.getDeptLeaderId(deptId);
                    if (deptLeaderId != null) {
                        approverIds.add(deptLeaderId);
                    } else {
                        // 如果没有负责人，获取部门内所有人员
                        List<Long> deptUserIds = userService.getUserIdsByDeptId(deptId);
                        approverIds.addAll(deptUserIds);
                    }
                }
            }
            break;
            
        case ApproverScope.SCOPE_TYPE_USER:
            // 人员类型：直接使用选择的人员
            if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
                approverIds.addAll(selectedUserIds);
            }
            break;
            
        case ApproverScope.SCOPE_TYPE_DEPT_USER:
            // 部门+人员类型：先选择部门，再从部门中选择人员
            if (selectedDeptIds != null && !selectedDeptIds.isEmpty()) {
                for (Long deptId : selectedDeptIds) {
                    if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
                        // 从该部门中选择指定人员
                        List<Long> deptUserIds = userService.getUserIdsByDeptId(deptId);
                        List<Long> validUserIds = selectedUserIds.stream()
                            .filter(deptUserIds::contains)
                            .collect(Collectors.toList());
                        approverIds.addAll(validUserIds);
                    } else {
                        // 没有指定人员，获取部门负责人
                        Long deptLeaderId = userService.getDeptLeaderId(deptId);
                        if (deptLeaderId != null) {
                            approverIds.add(deptLeaderId);
                        }
                    }
                }
            }
            break;
    }
    
    return approverIds.stream().distinct().collect(Collectors.toList());
}
```

---

## 🔧 动态节点创建

### 自由流节点定义

自由流节点是动态创建的，不存储在流程定义中，而是根据发送动作动态生成：

```java
private FlowNode createFreeFlowNode(FlowAction action, FlowInstance flowInstance) {
    // 创建临时节点定义（或使用特殊标记）
    FlowNode node = new FlowNode();
    node.setFlowDefId(flowInstance.getFlowDefId());
    node.setNodeName(action.getActionName());
    node.setNodeType(FlowNode.NODE_TYPE_APPROVAL);
    node.setApproverType(FlowNode.APPROVER_TYPE_USER);
    node.setOrderNum(9999); // 使用特殊顺序号标识自由流节点
    node.setParallelMode(FlowNode.PARALLEL_MODE_SERIAL);
    node.setCreatedAt(LocalDateTime.now());
    node.setUpdatedAt(LocalDateTime.now());
    
    // 保存节点定义（或使用缓存）
    flowNodeMapper.insert(node);
    
    return node;
}
```

---

## 📝 REST API 设计

### 1. 获取可用发送动作

```
GET /api/document/flows/{flowInstanceId}/available-actions
```

**响应示例：**
```json
{
  "code": 2001,
  "message": "接口调用成功",
  "data": [
    {
      "id": 1,
      "actionCode": "UNIT_HANDLE",
      "actionName": "单位内办理",
      "actionType": 1,
      "icon": "handle",
      "description": "发送给单位内部部门或人员办理"
    },
    {
      "id": 2,
      "actionCode": "REVIEW",
      "actionName": "核稿",
      "actionType": 2,
      "icon": "review",
      "description": "发送给核稿组或核稿人员"
    }
  ]
}
```

### 2. 获取审批人选择范围

```
GET /api/document/flows/actions/{actionId}/approver-scope
```

**响应示例：**
```json
{
  "code": 2001,
  "message": "接口调用成功",
  "data": {
    "actionId": 1,
    "scopeType": 3,
    "deptIds": [1, 2, 3],
    "userIds": [10, 11, 12],
    "roleCodes": ["MANAGER", "REVIEWER"],
    "allowCustom": 1,
    "deptList": [
      {"id": 1, "name": "技术部"},
      {"id": 2, "name": "市场部"}
    ],
    "userList": [
      {"id": 10, "name": "张三", "deptId": 1},
      {"id": 11, "name": "李四", "deptId": 2}
    ]
  }
}
```

### 3. 执行发送动作

```
POST /api/document/flows/node-instances/{nodeInstanceId}/execute-action
```

**请求参数：**
```json
{
  "actionId": 1,
  "selectedDeptIds": [1, 2],
  "selectedUserIds": [10, 11],
  "comment": "请相关部门办理"
}
```

**响应示例：**
```json
{
  "code": 2001,
  "message": "接口调用成功",
  "data": {
    "nodeInstanceId": 123,
    "actionName": "单位内办理",
    "approverCount": 2,
    "todoItemsCreated": 2
  }
}
```

---

## 🎯 业务规则示例

### 规则1：单位内办理

**触发条件：**
- 文件状态：审核中
- 用户角色：部门经理、管理员

**审批人范围：**
- 类型：部门+人员
- 可选部门：所有部门
- 可选人员：部门内人员

### 规则2：核稿

**触发条件：**
- 文件状态：审核中
- 用户角色：部门经理

**审批人范围：**
- 类型：角色
- 可选角色：REVIEWER（核稿员）

### 规则3：转外单位办理

**触发条件：**
- 文件状态：审核中
- 用户角色：管理员

**审批人范围：**
- 类型：自定义
- 允许选择外单位人员

### 规则4：返回

**触发条件：**
- 文件状态：审核中
- 用户角色：所有角色

**审批人范围：**
- 类型：固定
- 固定为：上一节点审批人或发起人

---

## 🔍 关键实现细节

### 1. 动作规则匹配算法

```java
private List<FlowActionRule> matchActionRules(
        Integer documentStatus,
        List<String> userRoles,
        Long deptId) {
    
    LambdaQueryWrapper<FlowActionRule> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(FlowActionRule::getDocumentStatus, documentStatus)
           .eq(FlowActionRule::getEnabled, 1);
    
    // 角色匹配（支持多个角色，逗号分隔）
    wrapper.and(w -> {
        for (String role : userRoles) {
            w.like(FlowActionRule::getUserRole, role).or();
        }
    });
    
    // 部门匹配（可选）
    if (deptId != null) {
        wrapper.and(w -> w.eq(FlowActionRule::getDeptId, deptId).or().isNull(FlowActionRule::getDeptId));
    }
    
    wrapper.orderByDesc(FlowActionRule::getPriority);
    
    return actionRuleMapper.selectList(wrapper);
}
```

### 2. 审批人范围验证

```java
private boolean validateApproverScope(
        ApproverScope scope,
        List<Long> selectedDeptIds,
        List<Long> selectedUserIds) {
    
    // 如果允许自定义，直接通过
    if (scope.getAllowCustom() == 1) {
        return true;
    }
    
    switch (scope.getScopeType()) {
        case ApproverScope.SCOPE_TYPE_DEPT:
            // 验证部门是否在允许范围内
            if (scope.getDeptIds() != null && !scope.getDeptIds().isEmpty()) {
                return scope.getDeptIds().containsAll(selectedDeptIds);
            }
            return true; // 如果没有限制，允许所有部门
            
        case ApproverScope.SCOPE_TYPE_USER:
            // 验证人员是否在允许范围内
            if (scope.getUserIds() != null && !scope.getUserIds().isEmpty()) {
                return scope.getUserIds().containsAll(selectedUserIds);
            }
            return true;
            
        case ApproverScope.SCOPE_TYPE_DEPT_USER:
            // 验证部门和人员组合
            boolean deptValid = scope.getDeptIds() == null || 
                               scope.getDeptIds().containsAll(selectedDeptIds);
            boolean userValid = scope.getUserIds() == null || 
                               scope.getUserIds().containsAll(selectedUserIds);
            return deptValid && userValid;
            
        default:
            return false;
    }
}
```

### 3. 返回操作处理

```java
private FlowNodeInstance handleReturnAction(
        Long currentNodeInstanceId,
        String comment,
        Long operatorId) {
    
    FlowNodeInstance currentNode = nodeInstanceMapper.selectById(currentNodeInstanceId);
    FlowInstance flowInstance = flowInstanceMapper.selectById(currentNode.getFlowInstanceId());
    
    // 查找上一个节点实例
    FlowNodeInstance previousNode = findPreviousNodeInstance(
        flowInstance.getId(),
        currentNodeInstanceId
    );
    
    if (previousNode == null) {
        // 没有上一个节点，退回给发起人
        Long creatorId = getDocumentCreatorId(flowInstance.getDocumentId());
        return createReturnNodeInstance(flowInstance, creatorId, comment);
    } else {
        // 退回给上一个节点
        return createReturnNodeInstance(flowInstance, previousNode.getApproverId(), comment);
    }
}
```

---

## 📊 数据流转示例

### 场景：单位内办理

```
1. 用户A（部门经理）处理待办事项
   ↓
2. 系统返回可用动作：["单位内办理", "核稿", "返回"]
   ↓
3. 用户选择"单位内办理"
   ↓
4. 系统返回审批人选择范围：
   - 可选部门：[技术部, 市场部, 财务部]
   - 可选人员：各部门内人员
   ↓
5. 用户选择：技术部 + 张三、李四
   ↓
6. 系统创建节点实例：
   - 节点实例1：审批人=张三
   - 节点实例2：审批人=李四
   ↓
7. 生成待办事项：
   - 待办1：分配给张三
   - 待办2：分配给李四
   ↓
8. 张三、李四分别处理待办事项
```

---

## 🚀 实现步骤

### 第一步：创建数据表和实体类

1. 创建 FlowAction 实体和 Mapper
2. 创建 FlowActionRule 实体和 Mapper
3. 创建 ApproverScope 实体和 Mapper
4. 创建 FreeFlowNodeInstance 实体和 Mapper

### 第二步：实现核心服务

1. FreeFlowService - 获取可用动作
2. FreeFlowService - 获取审批人范围
3. FreeFlowService - 执行发送动作
4. FreeFlowService - 验证逻辑

### 第三步：实现 REST API

1. 获取可用发送动作接口
2. 获取审批人选择范围接口
3. 执行发送动作接口

### 第四步：前端集成

1. 动态显示可用动作按钮
2. 动态显示审批人选择器（部门树+人员列表）
3. 提交发送动作

---

## 🔐 权限和安全

### 1. 动作权限验证

- 验证用户是否有权限执行该动作
- 验证文件状态是否允许该动作
- 验证用户角色是否匹配

### 2. 审批人范围验证

- 验证选择的部门是否在允许范围内
- 验证选择的人员是否在允许范围内
- 防止越权选择

### 3. 操作审计

- 记录所有发送动作
- 记录选择的审批人
- 记录操作时间和操作人

---

## 📝 配置示例

### 动作定义配置

```sql
INSERT INTO doc_flow_action (action_code, action_name, action_type, description) VALUES
('UNIT_HANDLE', '单位内办理', 1, '发送给单位内部部门或人员办理'),
('REVIEW', '核稿', 2, '发送给核稿组或核稿人员'),
('EXTERNAL', '转外单位办理', 3, '发送给外单位办理'),
('RETURN', '返回', 4, '退回给上一节点或发起人');
```

### 动作规则配置

```sql
-- 单位内办理：部门经理在审核中状态可以使用
INSERT INTO doc_flow_action_rule (action_id, document_status, user_role, priority) VALUES
(1, 1, 'MANAGER', 10);

-- 核稿：部门经理在审核中状态可以使用
INSERT INTO doc_flow_action_rule (action_id, document_status, user_role, priority) VALUES
(2, 1, 'MANAGER', 10);

-- 转外单位：管理员在审核中状态可以使用
INSERT INTO doc_flow_action_rule (action_id, document_status, user_role, priority) VALUES
(3, 1, 'ADMIN', 10);

-- 返回：所有角色在审核中状态都可以使用
INSERT INTO doc_flow_action_rule (action_id, document_status, user_role, priority) VALUES
(4, 1, '*', 5);
```

### 审批人范围配置

```sql
-- 单位内办理：部门+人员类型，允许所有部门
INSERT INTO doc_flow_approver_scope (action_id, scope_type, allow_custom) VALUES
(1, 3, 1);

-- 核稿：角色类型，只允许核稿员角色
INSERT INTO doc_flow_approver_scope (action_id, scope_type, role_codes) VALUES
(2, 2, 'REVIEWER');

-- 转外单位：自定义类型，允许选择外单位人员
INSERT INTO doc_flow_approver_scope (action_id, scope_type, allow_custom) VALUES
(3, 2, 1);
```

---

**设计时间**: 2023.0.3.3  
**设计人**: XTT Cloud Team

