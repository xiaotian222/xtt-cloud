# 流程引擎单元测试

## 测试结构

本目录包含流程引擎核心服务的单元测试，使用 JUnit 5 和 Mockito 框架。

## 测试文件列表

### 1. FlowServiceTest
- **位置**: `xtt/cloud/oa/document/application/flow/FlowServiceTest.java`
- **测试内容**: 
  - 流程实例创建和管理
  - 流程启动
  - 审批操作（同意、拒绝、转发、退回）
  - 承办记录管理
  - 固定流和自由流整合功能

### 2. FlowEngineServiceTest
- **位置**: `xtt/cloud/oa/document/application/flow/core/FlowEngineServiceTest.java`
- **测试内容**:
  - 流程启动逻辑
  - 节点审批处理
  - 流程流转
  - 异常情况处理

### 3. FlowApprovalServiceTest
- **位置**: `xtt/cloud/oa/document/application/flow/core/FlowApprovalServiceTest.java`
- **测试内容**:
  - 审批同意
  - 审批拒绝
  - 审批转发
  - 审批退回

### 4. TaskServiceTest
- **位置**: `xtt/cloud/oa/document/application/flow/core/TaskServiceTest.java`
- **测试内容**:
  - 待办任务创建和管理
  - 已办任务创建和管理
  - 任务查询（按审批人、流程实例、任务类型）
  - 分页查询

### 5. FreeFlowEngineServiceTest
- **位置**: `xtt/cloud/oa/document/application/flow/core/FreeFlowEngineServiceTest.java`
- **测试内容**:
  - 获取可用动作
  - 获取审批人选择范围
  - 执行发送动作
  - 动作可用性验证
  - 异常情况处理

## 运行测试

### 使用 Maven 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
mvn test -Dtest=FlowServiceTest
```

### 运行特定测试方法
```bash
mvn test -Dtest=FlowServiceTest#testStartFlow_Success
```

### 使用 IDE 运行
- IntelliJ IDEA: 右键点击测试类或方法，选择 "Run 'TestName'"
- Eclipse: 右键点击测试类，选择 "Run As" -> "JUnit Test"

## 测试覆盖率

建议使用 JaCoCo 插件生成测试覆盖率报告：

```bash
mvn clean test jacoco:report
```

报告将生成在 `target/site/jacoco/index.html`

## 注意事项

1. **Mock 对象**: 所有测试使用 Mockito 模拟依赖，不依赖真实数据库
2. **事务**: 测试中的 `@Transactional` 注解在测试环境中可能不会真正回滚，这是正常的
3. **类型安全警告**: MyBatis Plus 的 LambdaQueryWrapper 在使用时会产生类型安全警告，这是框架特性，不影响测试运行
4. **私有方法**: 私有方法（如 `moveToNextNode`、`isActionAvailable`）通过公共方法间接测试

## 扩展测试

添加新测试时，请遵循以下规范：

1. 使用 `@DisplayName` 注解提供清晰的测试描述
2. 使用 Given-When-Then 模式组织测试代码
3. 为每个测试方法提供成功和失败场景
4. 使用有意义的测试数据
5. 验证 Mock 对象的调用次数和参数

## 测试最佳实践

1. **独立性**: 每个测试应该独立运行，不依赖其他测试的执行顺序
2. **可重复性**: 测试结果应该一致，无论运行多少次
3. **快速执行**: 单元测试应该快速执行，避免长时间等待
4. **清晰命名**: 测试方法名应该清晰描述测试场景
5. **完整覆盖**: 覆盖正常流程、边界情况和异常情况

