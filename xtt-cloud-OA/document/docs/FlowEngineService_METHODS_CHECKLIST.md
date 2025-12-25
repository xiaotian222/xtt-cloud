# FlowEngineService 方法清单

## 📋 当前已有方法

### ✅ 流程生命周期管理
1. **`startFlow(Document, FlowDefinition)`** - 启动流程
   - 创建流程实例
   - 创建第一个节点实例
   - 分配审批人
   - 生成待办任务
   - 记录历史

2. **`moveToNextNode(FlowInstance, FlowNodeInstance)`** - 流转到下一个节点
   - 支持串行节点流转
   - 支持并行节点流转（会签/或签）
   - 支持条件节点流转
   - 支持节点跳过
   - 支持汇聚节点检查

3. **`terminateFlow(FlowInstance, String)`** - 终止流程
   - 更新流程状态为已终止
   - 记录历史

4. **`completeFlow(FlowInstance)`** - 完成流程（私有）
   - 判断流程是否结束
   - 更新流程状态
   - 更新文档状态

### ✅ 辅助方法（私有）
5. **`isFlowCompleted(FlowInstance)`** - 判断流程是否结束（私有）
6. **`determineFlowType(Long)`** - 确定流程类型（私有）

---

## ❌ 缺失的方法

### 🔴 高优先级（核心功能）

#### 1. 流程撤回（Withdraw）
```java
/**
 * 撤回流程
 * 发起人可以撤回已提交但尚未完成的流程
 * 
 * @param flowInstanceId 流程实例ID
 * @param initiatorId 发起人ID
 * @param reason 撤回原因
 */
@Transactional
public void withdrawFlow(Long flowInstanceId, Long initiatorId, String reason) {
    // 1. 验证发起人权限
    // 2. 检查流程状态（只能撤回进行中的流程）
    // 3. 检查当前节点是否允许撤回
    // 4. 取消所有待办任务
    // 5. 更新流程状态为已撤回
    // 6. 记录活动历史
}
```

#### 2. 流程回退（Rollback）
```java
/**
 * 回退到指定节点
 * 将流程回退到之前的某个节点，重新审批
 * 
 * @param flowInstance 流程实例
 * @param targetNodeId 目标节点ID
 * @param operatorId 操作人ID
 * @param reason 回退原因
 */
@Transactional
public void rollbackToNode(FlowInstance flowInstance, Long targetNodeId, 
                          Long operatorId, String reason) {
    // 1. 验证回退权限
    // 2. 验证目标节点是否存在且在当前节点之前
    // 3. 取消当前节点及之后的所有待办任务
    // 4. 更新流程实例当前节点
    // 5. 创建目标节点的节点实例
    // 6. 分配审批人
    // 7. 生成待办任务
    // 8. 记录活动历史
}
```

#### 3. 流程挂起/激活（Suspend/Resume）
```java
/**
 * 挂起流程
 * 暂停流程执行，不取消待办任务
 * 
 * @param flowInstanceId 流程实例ID
 * @param operatorId 操作人ID
 * @param reason 挂起原因
 */
@Transactional
public void suspendFlow(Long flowInstanceId, Long operatorId, String reason) {
    // 1. 验证流程状态
    // 2. 更新流程状态为已挂起
    // 3. 记录活动历史
}

/**
 * 恢复流程
 * 恢复挂起的流程，继续执行
 * 
 * @param flowInstanceId 流程实例ID
 * @param operatorId 操作人ID
 */
@Transactional
public void resumeFlow(Long flowInstanceId, Long operatorId) {
    // 1. 验证流程状态（必须是已挂起）
    // 2. 更新流程状态为进行中
    // 3. 记录活动历史
}
```

#### 4. 条件流转评估（Condition Evaluation）
```java
/**
 * 评估条件表达式
 * 根据流程变量评估条件节点或条件流转的条件表达式
 * 
 * @param conditionExpression 条件表达式（SpEL）
 * @param flowInstance 流程实例
 * @return 评估结果
 */
public boolean evaluateCondition(String conditionExpression, FlowInstance flowInstance) {
    // 1. 解析 SpEL 表达式
    // 2. 获取流程变量
    // 3. 评估表达式
    // 4. 返回结果
}
```

#### 5. 汇聚节点检查（Convergence Check）
```java
/**
 * 检查汇聚节点是否可以流转
 * 检查所有指向该汇聚节点的分支是否都已完成
 * 
 * @param flowInstance 流程实例
 * @param convergenceNode 汇聚节点
 * @return 是否可以流转
 */
public boolean canConverge(FlowInstance flowInstance, FlowNode convergenceNode) {
    // 1. 查找所有指向该汇聚节点的节点（通过 nextNodeId）
    // 2. 检查这些节点的所有实例是否都已完成
    // 3. 返回结果
}
```

### 🟡 中优先级（增强功能）

#### 6. 流程跳转（Jump）
```java
/**
 * 跳转到指定节点
 * 管理员或特殊权限用户可以跳转到任意节点
 * 
 * @param flowInstance 流程实例
 * @param targetNodeId 目标节点ID
 * @param operatorId 操作人ID
 * @param reason 跳转原因
 */
@Transactional
public void jumpToNode(FlowInstance flowInstance, Long targetNodeId, 
                      Long operatorId, String reason) {
    // 1. 验证跳转权限（管理员）
    // 2. 验证目标节点是否存在
    // 3. 取消当前节点及之后的所有待办任务
    // 4. 更新流程实例当前节点
    // 5. 创建目标节点的节点实例
    // 6. 分配审批人
    // 7. 生成待办任务
    // 8. 记录活动历史
}
```

#### 7. 流程取消（Cancel）
```java
/**
 * 取消流程
 * 取消尚未开始的流程或已完成的流程
 * 
 * @param flowInstanceId 流程实例ID
 * @param operatorId 操作人ID
 * @param reason 取消原因
 */
@Transactional
public void cancelFlow(Long flowInstanceId, Long operatorId, String reason) {
    // 1. 验证流程状态（只能取消未开始或已完成的流程）
    // 2. 取消所有待办任务
    // 3. 更新流程状态为已取消
    // 4. 记录活动历史
}
```

#### 8. 流程变量管理（Process Variables）
```java
/**
 * 设置流程变量
 * 
 * @param flowInstanceId 流程实例ID
 * @param variableName 变量名
 * @param variableValue 变量值
 */
@Transactional
public void setProcessVariable(Long flowInstanceId, String variableName, Object variableValue) {
    // 1. 获取或创建流程实例扩展信息
    // 2. 存储流程变量（JSON格式）
    // 3. 更新流程实例
}

/**
 * 获取流程变量
 * 
 * @param flowInstanceId 流程实例ID
 * @param variableName 变量名
 * @return 变量值
 */
public Object getProcessVariable(Long flowInstanceId, String variableName) {
    // 1. 获取流程实例扩展信息
    // 2. 解析流程变量
    // 3. 返回变量值
}

/**
 * 获取所有流程变量
 * 
 * @param flowInstanceId 流程实例ID
 * @return 流程变量Map
 */
public Map<String, Object> getProcessVariables(Long flowInstanceId) {
    // 1. 获取流程实例扩展信息
    // 2. 解析所有流程变量
    // 3. 返回Map
}
```

#### 9. 流程状态查询
```java
/**
 * 检查流程是否可以撤回
 * 
 * @param flowInstanceId 流程实例ID
 * @param initiatorId 发起人ID
 * @return 是否可以撤回
 */
public boolean canWithdraw(Long flowInstanceId, Long initiatorId) {
    // 1. 验证发起人权限
    // 2. 检查流程状态
    // 3. 检查当前节点是否允许撤回
    // 4. 返回结果
}

/**
 * 检查流程是否可以回退
 * 
 * @param flowInstanceId 流程实例ID
 * @param targetNodeId 目标节点ID
 * @param operatorId 操作人ID
 * @return 是否可以回退
 */
public boolean canRollback(Long flowInstanceId, Long targetNodeId, Long operatorId) {
    // 1. 验证回退权限
    // 2. 检查流程状态
    // 3. 检查目标节点是否存在且在当前节点之前
    // 4. 返回结果
}
```

### 🟢 低优先级（可选功能）

#### 10. 流程超时处理
```java
/**
 * 检查并处理超时的流程
 * 定时任务调用，检查超时的节点并自动处理
 * 
 * @param timeoutHours 超时小时数
 */
@Transactional
public void processTimeoutFlows(int timeoutHours) {
    // 1. 查询超时的节点实例
    // 2. 根据配置的自动处理规则处理
    // 3. 发送超时通知
    // 4. 记录活动历史
}
```

#### 11. 流程统计
```java
/**
 * 获取流程统计信息
 * 
 * @param flowInstanceId 流程实例ID
 * @return 统计信息
 */
public FlowStatistics getFlowStatistics(Long flowInstanceId) {
    // 1. 统计总节点数
    // 2. 统计已完成节点数
    // 3. 统计待处理节点数
    // 4. 统计流程耗时
    // 5. 返回统计信息
}
```

---

## 📊 方法分类总结

### 流程生命周期管理
- ✅ `startFlow` - 启动流程
- ✅ `moveToNextNode` - 流转到下一个节点
- ✅ `terminateFlow` - 终止流程
- ✅ `completeFlow` - 完成流程（私有）
- ❌ `withdrawFlow` - 撤回流程
- ❌ `rollbackToNode` - 回退到指定节点
- ❌ `suspendFlow` - 挂起流程
- ❌ `resumeFlow` - 恢复流程
- ❌ `cancelFlow` - 取消流程

### 流程控制
- ❌ `jumpToNode` - 跳转到指定节点
- ❌ `evaluateCondition` - 评估条件表达式
- ❌ `canConverge` - 检查汇聚节点

### 流程变量管理
- ❌ `setProcessVariable` - 设置流程变量
- ❌ `getProcessVariable` - 获取流程变量
- ❌ `getProcessVariables` - 获取所有流程变量

### 流程状态查询
- ✅ `isFlowCompleted` - 判断流程是否结束（私有）
- ❌ `canWithdraw` - 检查是否可以撤回
- ❌ `canRollback` - 检查是否可以回退

### 辅助功能
- ✅ `determineFlowType` - 确定流程类型（私有）
- ❌ `processTimeoutFlows` - 处理超时流程
- ❌ `getFlowStatistics` - 获取流程统计信息

---

## 🎯 推荐实现顺序

### 第一阶段（核心功能）
1. `withdrawFlow` - 流程撤回（发起人常用）
2. `rollbackToNode` - 流程回退（审批人常用）
3. `evaluateCondition` - 条件评估（支持条件流转）
4. `canConverge` - 汇聚节点检查（完善并行流程）

### 第二阶段（增强功能）
5. `suspendFlow` / `resumeFlow` - 流程挂起/恢复
6. `setProcessVariable` / `getProcessVariable` - 流程变量管理
7. `canWithdraw` / `canRollback` - 状态查询方法

### 第三阶段（高级功能）
8. `jumpToNode` - 流程跳转（管理员功能）
9. `cancelFlow` - 流程取消
10. `processTimeoutFlows` - 超时处理
11. `getFlowStatistics` - 流程统计

---

## 📝 注意事项

1. **事务管理**：所有修改流程状态的方法都应该使用 `@Transactional`
2. **权限验证**：所有操作都应该验证操作人权限
3. **历史记录**：所有状态变更都应该记录活动历史
4. **待办任务**：状态变更时应该同步更新待办任务状态
5. **异常处理**：应该抛出有意义的业务异常
6. **日志记录**：所有操作都应该记录日志

