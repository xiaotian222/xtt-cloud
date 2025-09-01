# OA系统微服务架构设计

## 🏗️ 微服务划分

### 1. 基础网关与认证
- **Gateway**：统一入口、路由、限流、鉴权。
- **Auth**：认证中心，登录、令牌管理、单点登录。

### 2. 平台与核心业务
- **Platform**：
  - RBAC 权限管理（用户、角色、菜单、权限）。
  - 组织架构（部门、岗位、人员）。
- **Document**：
  - 公文起草、编辑、签收、归档。
  - 公文元数据管理（标题、文号、等级等）。
- **Workflow**：
  - 审批流引擎（支持自定义流程节点）。
  - 审批、退回、加签、传阅。
- **File**：
  - 公文附件存储、下载、预览。
  - 支持对接 MinIO/OSS。

### 3. 异步解耦层
- **MQ**（统一消息队列服务）
  - **Notification Consumer**：
    - 负责接收 MQ 消息，发送短信、邮件、WebSocket 推送。
  - **Audit/Log Consumer**：
    - 负责接收 MQ 消息，写入日志数据库/ES，满足合规审计。

### 4. 报表与分析
- **Report**
  - 不消费 MQ 消息。
  - 定期或实时从 **Document / Workflow / Platform** 拉取数据。
  - 存入 **Elasticsearch**，提供全文检索、统计报表、趋势分析。

---

## 🔧 架构优化建议

1. **API 网关做统一认证与审计**
   - 在 Gateway 层做统一认证拦截 + 操作日志收集，避免每个服务重复开发。
   - 日志异步写入 MQ，由 Audit/Log Consumer 处理。

2. **服务之间通信要规范**
   - 内部调用用 Feign（REST）或 gRPC，统一熔断、重试策略。
   - 不要直接访问别的服务数据库（Report 除外，可用 ETL/CDC 同步）。

3. **Report 的数据源设计**
   - 避免直接高频查询业务库，建议：
     - 定时 ETL 抽取数据到 ES。
     - 或使用 CDC（Canal、Debezium）实时同步业务库 → ES。

4. **MQ 的角色划分更明确**
   - 定义 Topic 规范：
     - `document.approved`
     - `document.archived`
     - `user.login`
   - 便于扩展新的消费者（如大数据分析）。

5. **Workflow 建议独立持久化**
   - 使用独立表存储流程定义、实例、任务、节点日志。
   - 可考虑使用 Flowable/Activiti 减少造轮子。

6. **可观测性（Observability）**
   - 日志集中收集到 ELK。
   - 指标监控：Prometheus + Grafana。
   - 链路追踪：SkyWalking / Jaeger。

---

## 🔗 服务关系示意

```
[Gateway]
   ↓
[Auth] → [Platform]
   ↓
[Document] → [Workflow] → [Platform]
   ↓
[File]

[MQ] → (Notification Consumer)
     → (Audit/Log Consumer)

[Report (Elasticsearch)]
   ↑
   └── 从 Document / Workflow / Platform 拉取或订阅数据
```
