# Document 工程日志记录实现

## 📋 概述

为 Document 工程添加了完整的日志记录功能，使用 SLF4J + Logback（Spring Boot 默认日志框架），覆盖所有关键业务操作。

---

## ✅ 已实现的日志记录

### 1. FlowService（流程服务）

#### 日志级别说明
- **INFO**: 关键业务操作（创建、启动流程等）
- **DEBUG**: 详细调试信息（查询操作、状态信息）
- **WARN**: 警告信息（数据不存在、状态异常）
- **ERROR**: 错误信息（异常捕获）

#### 已添加日志的方法

| 方法 | 日志内容 | 级别 |
|------|---------|------|
| `createFlow()` | 创建流程实例的开始和结果 | INFO |
| `createIssuanceInfo()` | 创建发文流程扩展信息 | INFO |
| `createReceiptInfo()` | 创建收文流程扩展信息 | INFO |
| `startIssuanceFlow()` | 启动发文流程的详细过程 | INFO/DEBUG |
| `startReceiptFlow()` | 启动收文流程的详细过程 | INFO/DEBUG |
| `registerExternalReceipt()` | 外单位签收登记 | INFO |
| `createHandling()` | 创建承办记录 | INFO |
| `updateHandling()` | 更新承办记录 | INFO/DEBUG |
| `getFlowInstance()` | 查询流程实例 | DEBUG |
| `getFlowInstanceByDocumentId()` | 根据公文ID查询 | DEBUG |
| `getIssuanceInfoByFlowId()` | 查询发文扩展信息 | DEBUG |
| `getReceiptInfoByFlowId()` | 查询收文扩展信息 | DEBUG |
| `getExternalReceiptsByFlowId()` | 查询外单位签收记录 | DEBUG |
| `getHandlingsByFlowId()` | 查询承办记录列表 | DEBUG |

#### 日志示例

```java
// 创建流程实例
log.info("创建流程实例，公文ID: {}, 流程类型: {}, 流程定义ID: {}", 
        flowInstance.getDocumentId(), flowInstance.getFlowType(), flowInstance.getFlowDefId());
log.info("流程实例创建成功，ID: {}, 公文ID: {}", flowInstance.getId(), flowInstance.getDocumentId());

// 启动流程
log.info("启动发文流程，流程实例ID: {}", flowInstanceId);
log.debug("流程实例当前状态: {}, 公文ID: {}", flowInstance.getStatus(), flowInstance.getDocumentId());
log.info("发文流程启动成功，流程实例ID: {}, 公文ID: {}, 开始时间: {}", 
        flowInstanceId, flowInstance.getDocumentId(), flowInstance.getStartTime());

// 异常处理
log.error("创建流程实例失败，公文ID: {}, 流程类型: {}", 
        flowInstance.getDocumentId(), flowInstance.getFlowType(), e);
```

---

### 2. FlowController（流程控制器）

#### 已添加日志的方法

| 方法 | 日志内容 | 级别 |
|------|---------|------|
| `createFlow()` | 接收请求和响应结果 | INFO |
| `startIssuanceFlow()` | 启动发文流程请求 | INFO |
| `startReceiptFlow()` | 启动收文流程请求 | INFO |
| `updateHandling()` | 更新承办记录请求 | INFO |
| `getFlowInstance()` | 查询流程实例请求 | DEBUG |
| `getFlowInstanceByDocumentId()` | 根据公文ID查询请求 | DEBUG |

#### 日志示例

```java
log.info("收到创建流程实例请求，公文ID: {}, 流程类型: {}", 
        flowInstance.getDocumentId(), flowInstance.getFlowType());
log.info("创建流程实例成功，ID: {}, 公文ID: {}", created.getId(), created.getDocumentId());
log.error("创建流程实例失败，公文ID: {}", flowInstance.getDocumentId(), e);
```

---

### 3. DocumentService 和 DocumentController

已添加日志框架，为后续实现做准备：

```java
private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
```

---

## ⚙️ 日志配置

### 本地配置（application.yaml）

```yaml
logging:
  level:
    root: INFO
    xtt.cloud.oa.document: INFO
    xtt.cloud.oa.document.application: INFO
    xtt.cloud.oa.document.interfaces: DEBUG
    org.springframework: WARN
    com.baomidou.mybatisplus: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
```

### Nacos 配置（document.yaml）

已在 `config-init/config/document.yaml` 中添加相同的日志配置，支持通过 Nacos 动态调整日志级别。

---

## 📊 日志级别说明

### INFO 级别
记录关键业务操作，包括：
- 流程实例的创建、启动
- 扩展信息的创建
- 承办记录的创建和更新
- 外单位签收登记

### DEBUG 级别
记录详细调试信息，包括：
- 查询操作的参数和结果
- 流程状态信息
- 数据验证信息

### WARN 级别
记录警告信息，包括：
- 数据不存在的情况
- 状态异常的情况

### ERROR 级别
记录错误信息，包括：
- 异常堆栈信息
- 操作失败的原因

---

## 🔍 日志输出示例

### 正常流程创建

```
2023-12-01 10:00:00.123 [http-nio-8086-exec-1] INFO  xtt.cloud.oa.document.interfaces.rest.FlowController - 收到创建流程实例请求，公文ID: 1, 流程类型: 1
2023-12-01 10:00:00.125 [http-nio-8086-exec-1] INFO  xtt.cloud.oa.document.application.flow.FlowService - 创建流程实例，公文ID: 1, 流程类型: 1, 流程定义ID: 1
2023-12-01 10:00:00.200 [http-nio-8086-exec-1] INFO  xtt.cloud.oa.document.application.flow.FlowService - 流程实例创建成功，ID: 1, 公文ID: 1
2023-12-01 10:00:00.201 [http-nio-8086-exec-1] INFO  xtt.cloud.oa.document.interfaces.rest.FlowController - 创建流程实例成功，ID: 1, 公文ID: 1
```

### 流程启动

```
2023-12-01 10:05:00.123 [http-nio-8086-exec-2] INFO  xtt.cloud.oa.document.interfaces.rest.FlowController - 收到启动发文流程请求，流程实例ID: 1
2023-12-01 10:05:00.125 [http-nio-8086-exec-2] INFO  xtt.cloud.oa.document.application.flow.FlowService - 启动发文流程，流程实例ID: 1
2023-12-01 10:05:00.130 [http-nio-8086-exec-2] DEBUG xtt.cloud.oa.document.application.flow.FlowService - 流程实例当前状态: 0, 公文ID: 1
2023-12-01 10:05:00.200 [http-nio-8086-exec-2] INFO  xtt.cloud.oa.document.application.flow.FlowService - 发文流程启动成功，流程实例ID: 1, 公文ID: 1, 开始时间: 2023-12-01T10:05:00
2023-12-01 10:05:00.201 [http-nio-8086-exec-2] INFO  xtt.cloud.oa.document.interfaces.rest.FlowController - 启动发文流程成功，流程实例ID: 1
```

### 异常情况

```
2023-12-01 10:10:00.123 [http-nio-8086-exec-3] INFO  xtt.cloud.oa.document.application.flow.FlowService - 启动发文流程，流程实例ID: 999
2023-12-01 10:10:00.130 [http-nio-8086-exec-3] WARN  xtt.cloud.oa.document.application.flow.FlowService - 启动发文流程失败，流程实例不存在，ID: 999
2023-12-01 10:10:00.131 [http-nio-8086-exec-3] ERROR xtt.cloud.oa.document.application.flow.FlowService - 启动发文流程失败，流程实例ID: 999
java.lang.RuntimeException: 流程实例不存在
    at xtt.cloud.oa.document.application.flow.FlowService.startIssuanceFlow(FlowService.java:82)
    ...
2023-12-01 10:10:00.132 [http-nio-8086-exec-3] ERROR xtt.cloud.oa.document.interfaces.rest.FlowController - 启动发文流程失败，流程实例ID: 999
```

---

## 🎯 日志记录最佳实践

### 1. 日志内容
- ✅ 包含关键业务参数（ID、状态等）
- ✅ 记录操作开始和结束
- ✅ 异常时记录完整堆栈
- ✅ 使用占位符 `{}` 而不是字符串拼接

### 2. 日志级别选择
- **INFO**: 业务关键操作，需要监控
- **DEBUG**: 开发调试信息，生产环境可关闭
- **WARN**: 异常但可恢复的情况
- **ERROR**: 需要立即关注的错误

### 3. 性能考虑
- 使用 SLF4J 的占位符，避免不必要的字符串拼接
- DEBUG 级别日志在生产环境关闭，减少性能开销
- 异常日志包含完整堆栈，便于问题排查

---

## 📝 后续优化建议

1. **日志文件输出**
   - 配置日志文件输出路径
   - 设置日志文件滚动策略（按大小、按时间）
   - 配置日志文件保留天数

2. **日志聚合**
   - 集成 ELK（Elasticsearch + Logstash + Kibana）
   - 或使用其他日志聚合工具（如 Loki）

3. **日志监控告警**
   - 设置 ERROR 级别日志告警
   - 监控关键业务操作的成功率

4. **结构化日志**
   - 考虑使用 JSON 格式输出日志
   - 便于日志分析和查询

---

## ✅ 完成状态

- [x] FlowService 所有方法添加日志
- [x] FlowController 关键接口添加日志
- [x] DocumentService 和 DocumentController 添加日志框架
- [x] 本地日志配置（application.yaml）
- [x] Nacos 日志配置（document.yaml）

---

**实现时间**: 2023.0.3.3  
**实现人**: XTT Cloud Team

