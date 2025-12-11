# 包结构重组说明

## 📋 重组概述

为了保持代码目录的整洁，将自由流相关的代码细分到 `flow` 子包中。

---

## 📁 新的包结构

### 实体类（Entity）

**原位置：** `xtt.cloud.oa.document.domain.entity`  
**新位置：** `xtt.cloud.oa.document.domain.entity.flow`

移动的实体类：
- ✅ `FlowAction` - 发送动作实体
- ✅ `FlowActionRule` - 动作规则实体
- ✅ `ApproverScope` - 审批人选择范围实体
- ✅ `FreeFlowNodeInstance` - 自由流节点实例扩展实体
- ✅ `FlowNode` - 流程节点定义实体
- ✅ `FlowNodeInstance` - 节点实例实体

### Mapper 接口

**原位置：** `xtt.cloud.oa.document.domain.mapper`  
**新位置：** `xtt.cloud.oa.document.domain.mapper.flow`

移动的 Mapper：
- ✅ `FlowActionMapper`
- ✅ `FlowActionRuleMapper`
- ✅ `ApproverScopeMapper`
- ✅ `FreeFlowNodeInstanceMapper`
- ✅ `FlowNodeMapper`
- ✅ `FlowNodeInstanceMapper`

### 服务类（Service）

**原位置：** `xtt.cloud.oa.document.application`  
**新位置：** `xtt.cloud.oa.document.application.flow`

移动的服务：
- ✅ `FreeFlowService` - 自由流服务

### 控制器（Controller）

**原位置：** `xtt.cloud.oa.document.interfaces.rest`  
**新位置：** `xtt.cloud.oa.document.interfaces.rest.flow`

移动的控制器：
- ✅ `FreeFlowController` - 自由流控制器

### DTO 类

**原位置：** `xtt.cloud.oa.document.interfaces.rest.dto`  
**新位置：** `xtt.cloud.oa.document.interfaces.rest.flow.dto`

移动的 DTO：
- ✅ `ExecuteActionRequest` - 执行发送动作请求DTO

---

## 🔄 更新的引用

所有相关的 import 语句已更新：

### FreeFlowService.java
```java
// 更新前
import xtt.cloud.oa.document.domain.entity.*;
import xtt.cloud.oa.document.domain.mapper.*;

// 更新后
import xtt.cloud.oa.document.domain.entity.flow.*;
import xtt.cloud.oa.document.domain.mapper.flow.*;
```

### FreeFlowController.java

```java
// 更新前

import xtt.cloud.oa.document.application.FreeFlowService;
import xtt.cloud.oa.document.domain.entity.FlowAction;
import xtt.cloud.oa.document.domain.entity.FlowNodeInstance;
import xtt.cloud.oa.document.interfaces.rest.dto.ExecuteActionRequest;

// 更新后

```

### 所有 Mapper 接口
```java
// 更新前
import xtt.cloud.oa.document.domain.entity.FlowAction;

// 更新后
import xtt.cloud.oa.document.domain.entity.flow.FlowAction;
```

---

## 📊 目录结构对比

### 重组前
```
document/
├── domain/
│   ├── entity/
│   │   ├── Document.java
│   │   ├── FlowAction.java          ← 自由流相关
│   │   ├── FlowNode.java            ← 自由流相关
│   │   ├── FlowNodeInstance.java    ← 自由流相关
│   │   └── ...
│   └── mapper/
│       ├── FlowActionMapper.java    ← 自由流相关
│       ├── FlowNodeMapper.java      ← 自由流相关
│       └── ...
├── application/
│   ├── DocumentService.java
│   ├── FlowService.java
│   └── FreeFlowService.java         ← 自由流相关
└── interfaces/
    └── rest/
        ├── DocumentController.java
        ├── FlowController.java
        ├── FreeFlowController.java   ← 自由流相关
        └── dto/
            └── ExecuteActionRequest.java  ← 自由流相关
```

### 重组后
```
document/
├── domain/
│   ├── entity/
│   │   ├── Document.java
│   │   ├── FlowInstance.java
│   │   └── flow/                     ← 新增子包
│   │       ├── FlowAction.java
│   │       ├── FlowActionRule.java
│   │       ├── ApproverScope.java
│   │       ├── FreeFlowNodeInstance.java
│   │       ├── FlowNode.java
│   │       └── FlowNodeInstance.java
│   └── mapper/
│       ├── DocumentMapper.java
│       ├── FlowInstanceMapper.java
│       └── flow/                     ← 新增子包
│           ├── FlowActionMapper.java
│           ├── FlowActionRuleMapper.java
│           ├── ApproverScopeMapper.java
│           ├── FreeFlowNodeInstanceMapper.java
│           ├── FlowNodeMapper.java
│           └── FlowNodeInstanceMapper.java
├── application/
│   ├── DocumentService.java
│   ├── FlowService.java
│   └── flow/                         ← 新增子包
│       └── FreeFlowService.java
└── interfaces/
    └── rest/
        ├── DocumentController.java
        ├── FlowController.java
        └── flow/                     ← 新增子包
            ├── FreeFlowController.java
            └── dto/
                └── ExecuteActionRequest.java
```

---

## ✅ 完成状态

- [x] 实体类移动到 `domain.entity.flow` 子包
- [x] Mapper 接口移动到 `domain.mapper.flow` 子包
- [x] 服务类移动到 `application.flow` 子包
- [x] 控制器移动到 `interfaces.rest.flow` 子包
- [x] DTO 类移动到 `interfaces.rest.flow.dto` 子包
- [x] 更新所有 import 语句
- [x] 验证编译通过

---

## 🎯 优势

1. **代码组织更清晰**：自由流相关代码集中在一个子包中
2. **易于维护**：相关功能模块化，便于查找和修改
3. **避免命名冲突**：通过包结构区分不同模块的类
4. **符合单一职责原则**：每个包只包含相关的类

---

## 📝 注意事项

1. **其他模块引用**：如果其他模块（如 `FlowService`）需要引用这些类，需要更新 import 语句
2. **数据库映射**：MyBatis 的 XML 映射文件中的类型别名可能需要更新
3. **测试代码**：单元测试中的 import 语句也需要更新

---

**重组时间**: 2023.0.3.3  
**重组人**: XTT Cloud Team

