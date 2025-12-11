# 流程引擎整合设计：固定流程 + 自由流

## 📋 概述

系统支持两种流程模式：
1. **固定流程**：按照预定义的流程节点顺序执行
2. **自由流**：根据文件状态和用户角色动态决定下一步操作

两种模式可以混合使用，提供灵活的流程管理能力。

---

## 🔄 流程模式选择

### 模式判断逻辑

```java
public class FlowModeDetector {
    
    /**
     * 判断流程实例应该使用哪种模式
     */
    public FlowMode detectFlowMode(Long flowInstanceId) {
        FlowInstance instance = flowInstanceMapper.selectById(flowInstanceId);
        FlowDefinition flowDef = flowDefinitionMapper.selectById(instance.getFlowDefId());
        
        // 如果流程定义标记为自由流，使用自由流模式
        if (flowDef.getFlowMode() == FlowDefinition.FLOW_MODE_FREE) {
            return FlowMode.FREE;
        }
        
        // 如果当前节点是自由流节点，使用自由流模式
        FlowNode currentNode = flowNodeMapper.selectById(instance.getCurrentNodeId());
        if (currentNode != null && currentNode.getIsFreeFlow() == 1) {
            return FlowMode.FREE;
        }
        
        // 默认使用固定流程模式
        return FlowMode.FIXED;
    }
}
```

### 流程模式常量

```java
public enum FlowMode {
    FIXED,  // 固定流程
    FREE    // 自由流
}
```

---

## 🏗️ 流程定义扩展

### FlowDefinition 扩展字段

```java
public class FlowDefinition {
    // ... 原有字段 ...
    
    private Integer flowMode;      // 流程模式(0:固定流程,1:自由流,2:混合模式)
    private Integer allowFreeFlow; // 是否允许自由流(0:不允许,1:允许)
}
```

### FlowNode 扩展字段

```java
public class FlowNode {
    // ... 原有字段 ...
    
    private Integer isFreeFlow;    // 是否为自由流节点(0:否,1:是)
    private Integer allowFreeFlow; // 是否允许在此节点使用自由流(0:不允许,1:允许)
}
```

---

## 🔀 混合流程执行

### 场景1：固定流程中插入自由流节点

```
固定节点1 → 固定节点2 → [自由流节点] → 固定节点3
```

**执行逻辑：**
1. 固定节点1、2按顺序执行
2. 节点2完成后，系统判断是否允许自由流
3. 如果允许，用户可以选择发送动作（自由流）
4. 自由流节点完成后，继续执行固定节点3

### 场景2：完全自由流

```
[自由流节点1] → [自由流节点2] → [自由流节点3]
```

**执行逻辑：**
1. 所有节点都是自由流节点
2. 每个节点完成后，用户选择下一步操作
3. 完全由用户决定流程走向

### 场景3：固定流程为主，自由流为辅

```
固定节点1 → 固定节点2 → [可选自由流] → 固定节点3
```

**执行逻辑：**
1. 固定节点按顺序执行
2. 在特定节点允许用户选择：
   - 继续固定流程
   - 或使用自由流发送
3. 根据用户选择决定下一步

---

## 💻 整合后的服务设计

### FlowEngineService（流程引擎服务）

```java
@Service
public class FlowEngineService {
    
    @Autowired
    private FlowModeDetector flowModeDetector;
    
    @Autowired
    private FixedFlowService fixedFlowService;  // 固定流程服务
    
    @Autowired
    private FreeFlowService freeFlowService;    // 自由流服务
    
    /**
     * 处理节点审批（统一入口）
     */
    public void processNodeApproval(
            Long nodeInstanceId,
            String action,
            String comments,
            Long approverId) {
        
        FlowNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        FlowInstance flowInstance = flowInstanceMapper.selectById(nodeInstance.getFlowInstanceId());
        
        // 判断流程模式
        FlowMode mode = flowModeDetector.detectFlowMode(flowInstance.getId());
        
        if (mode == FlowMode.FREE) {
            // 自由流模式
            if (ACTION_FORWARD.equals(action)) {
                // 转发操作，需要选择发送动作
                // 这个操作应该通过 executeAction 接口处理
                throw new BusinessException("自由流节点请使用 executeAction 接口");
            } else {
                // 其他操作（同意、拒绝等）
                freeFlowService.processApproval(nodeInstanceId, action, comments, approverId);
            }
        } else {
            // 固定流程模式
            fixedFlowService.processApproval(nodeInstanceId, action, comments, approverId);
        }
    }
    
    /**
     * 执行发送动作（自由流专用）
     */
    public FlowNodeInstance executeAction(
            Long nodeInstanceId,
            Long actionId,
            List<Long> selectedDeptIds,
            List<Long> selectedUserIds,
            String comment,
            Long operatorId) {
        
        FlowNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        FlowInstance flowInstance = flowInstanceMapper.selectById(nodeInstance.getFlowInstanceId());
        
        // 验证是否允许自由流
        FlowMode mode = flowModeDetector.detectFlowMode(flowInstance.getId());
        if (mode != FlowMode.FREE) {
            // 检查当前节点是否允许自由流
            FlowNode currentNode = flowNodeMapper.selectById(nodeInstance.getNodeId());
            if (currentNode.getAllowFreeFlow() != 1) {
                throw new BusinessException("当前节点不允许使用自由流");
            }
        }
        
        return freeFlowService.executeAction(
            nodeInstanceId, actionId, selectedDeptIds, selectedUserIds, comment, operatorId
        );
    }
    
    /**
     * 获取可用操作（统一入口）
     */
    public List<Object> getAvailableOperations(Long nodeInstanceId, Long userId) {
        FlowNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
        FlowInstance flowInstance = flowInstanceMapper.selectById(nodeInstance.getFlowInstanceId());
        
        FlowMode mode = flowModeDetector.detectFlowMode(flowInstance.getId());
        
        if (mode == FlowMode.FREE) {
            // 自由流模式：返回可用发送动作
            return freeFlowService.getAvailableActions(flowInstance.getDocumentId(), userId);
        } else {
            // 固定流程模式：返回标准操作（同意、拒绝、退回）
            FlowNode currentNode = flowNodeMapper.selectById(nodeInstance.getNodeId());
            if (currentNode.getAllowFreeFlow() == 1) {
                // 当前节点允许自由流，返回标准操作 + 发送动作
                List<Object> operations = new ArrayList<>();
                operations.addAll(getStandardActions());
                operations.addAll(freeFlowService.getAvailableActions(flowInstance.getDocumentId(), userId));
                return operations;
            } else {
                // 只返回标准操作
                return getStandardActions();
            }
        }
    }
    
    private List<Object> getStandardActions() {
        return Arrays.asList(
            Map.of("action", "approve", "name", "同意"),
            Map.of("action", "reject", "name", "拒绝"),
            Map.of("action", "return", "name", "退回")
        );
    }
}
```

---

## 📊 流程状态管理

### 节点实例状态

无论是固定流程还是自由流，节点实例的状态都是一致的：

```java
public static final int STATUS_PENDING = 0;    // 待处理
public static final int STATUS_PROCESSING = 1; // 处理中
public static final int STATUS_COMPLETED = 2;  // 已完成
public static final int STATUS_REJECTED = 3;    // 已拒绝
public static final int STATUS_SKIPPED = 4;     // 已跳过
```

### 流程实例状态

```java
public static final int STATUS_PROCESSING = 0;  // 进行中
public static final int STATUS_COMPLETED = 1;   // 已完成
public static final int STATUS_TERMINATED = 2;   // 已终止
```

---

## 🎯 前端交互设计

### 固定流程界面

```
[公文详情]
[审批意见输入框]
[同意] [拒绝] [退回]
```

### 自由流界面

```
[公文详情]
[审批意见输入框]
[发送动作选择]
  - [单位内办理] [核稿] [转外单位办理] [返回]
[审批人选择]
  - 部门树
  - 人员列表
[提交]
```

### 混合模式界面

```
[公文详情]
[审批意见输入框]
[标准操作]
  - [同意] [拒绝] [退回]
[发送动作]（如果允许）
  - [单位内办理] [核稿] [转外单位办理]
[审批人选择]（如果选择了发送动作）
[提交]
```

---

## 🔍 关键实现细节

### 1. 节点类型判断

```java
private boolean isFreeFlowNode(FlowNode node) {
    return node.getIsFreeFlow() == 1 || 
           node.getNodeType() == FlowNode.NODE_TYPE_FREE_FLOW;
}
```

### 2. 流程模式切换

```java
/**
 * 从固定流程切换到自由流
 */
private void switchToFreeFlow(Long flowInstanceId, Long actionId) {
    FlowInstance instance = flowInstanceMapper.selectById(flowInstanceId);
    
    // 创建自由流节点
    FlowNode freeNode = createFreeFlowNode(actionId, instance);
    
    // 更新流程实例当前节点
    instance.setCurrentNodeId(freeNode.getId());
    flowInstanceMapper.updateById(instance);
}
```

### 3. 自由流节点完成后的处理

```java
/**
 * 自由流节点完成后，判断下一步
 */
private void handleFreeFlowNodeComplete(Long nodeInstanceId) {
    FlowNodeInstance nodeInstance = nodeInstanceMapper.selectById(nodeInstanceId);
    FlowInstance flowInstance = flowInstanceMapper.selectById(nodeInstance.getFlowInstanceId());
    
    // 检查是否还有固定流程节点
    FlowNode nextFixedNode = findNextFixedNode(flowInstance);
    
    if (nextFixedNode != null) {
        // 还有固定流程节点，继续执行
        createNodeInstance(flowInstance, nextFixedNode);
    } else {
        // 没有固定流程节点，流程结束或继续自由流
        if (flowInstance.getFlowMode() == FlowDefinition.FLOW_MODE_FREE) {
            // 完全自由流，等待用户选择下一步
            // 不自动创建节点，等待用户主动发送
        } else {
            // 混合模式，流程结束
            completeFlow(flowInstance.getId());
        }
    }
}
```

---

## 📝 配置示例

### 混合流程配置

```sql
-- 流程定义：混合模式
UPDATE doc_flow_definition 
SET flow_mode = 2, allow_free_flow = 1 
WHERE id = 1;

-- 节点定义：允许自由流
UPDATE doc_flow_node 
SET allow_free_flow = 1 
WHERE id = 3;  -- 节点3允许自由流
```

---

## 🚀 实现优先级

### 第一阶段：基础整合

1. ✅ 流程模式检测
2. ✅ 统一审批处理入口
3. ✅ 固定流程和自由流分离处理

### 第二阶段：混合模式

4. ⚠️ 固定流程中插入自由流节点
5. ⚠️ 自由流节点完成后回到固定流程
6. ⚠️ 流程模式动态切换

### 第三阶段：高级功能

7. ⏸️ 流程模板支持混合模式
8. ⏸️ 流程可视化（固定+自由流）
9. ⏸️ 流程统计分析

---

## 📊 数据流转示例

### 混合流程示例

```
1. 固定节点1（起草） → 完成
2. 固定节点2（审核） → 完成
3. 固定节点3（允许自由流） → 用户选择"单位内办理"
   ↓
4. 自由流节点（单位内办理） → 创建节点实例 → 分配审批人
   ↓
5. 审批人处理 → 完成
   ↓
6. 固定节点4（领导签发） → 完成
7. 流程结束
```

---

**设计时间**: 2023.0.3.3  
**设计人**: XTT Cloud Team

