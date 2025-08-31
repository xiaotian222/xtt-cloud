# xtt-cloud

基于 Spring Cloud Alibaba 的微服务云原生解决方案，提供完整的微服务架构组件和示例应用。

## 🚀 项目概述

xtt-cloud 是一个基于 Spring Cloud Alibaba 的微服务框架，集成了 Nacos、Sentinel、Seata、RocketMQ 等主流组件，提供开箱即用的微服务解决方案。

## 🏗️ 模块结构

### 📦 核心启动器 (xtt-cloud-starters)

#### 🔧 配置管理
- **xtt-alibaba-nacos-config**: Nacos 配置中心启动器
  - 支持配置热更新
  - 配置变更监听
  - 配置数据解析器
  - 健康检查端点

#### 🔍 服务发现
- **xtt-cloud-starter-nacos-discovery**: Nacos 服务发现启动器
  - 服务注册与发现
  - 负载均衡
  - 服务健康检查
  - 服务实例管理

#### 🛡️ 熔断限流
- **xtt-cloud-starter-sentinel**: Sentinel 熔断限流启动器
  - 流量控制
  - 熔断降级
  - 系统保护
  - 实时监控

- **xtt-cloud-circuitbreaker-sentinel**: Sentinel 断路器启动器
  - 响应式断路器
  - Feign 集成
  - 规则动态配置

#### 🚪 网关集成
- **xtt-cloud-sentinel-gateway**: Sentinel 网关启动器
  - 网关流量控制
  - 限流降级
  - 动态规则配置

#### 💾 数据源管理
- **xtt-cloud-sentinel-datasource**: Sentinel 数据源启动器
  - 支持多种数据源（Nacos、Redis、Zookeeper等）
  - 动态规则配置
  - 规则转换器

#### 🔄 分布式事务
- **xtt-cloud-starter-seata**: Seata 分布式事务启动器
  - 分布式事务管理
  - Feign 集成
  - REST 模板集成
  - 事务拦截器

#### 📅 任务调度
- **xtt-cloud-starter-schedulerx**: 任务调度启动器
  - SchedulerX 集成
  - ShedLock 支持
  - 定时任务管理

#### 🚀 消息队列
- **xtt-cloud-starter-stream-rocketmq**: RocketMQ 流式启动器
  - 消息发送与消费
  - 流式处理
  - 消息转换器
  - 健康检查

#### 🔌 事件总线
- **xtt-cloud-starter-bus-rocketmq**: RocketMQ 事件总线启动器
  - 配置刷新事件
  - 环境后处理器

#### 🐳 服务代理
- **xtt-cloud-starter-sidecar**: Sidecar 启动器
  - 多语言服务集成
  - 健康检查
  - 服务发现集成

#### 🛠️ 通用工具
- **xtt-cloud-commons**: 通用工具模块
  - 字符串工具
  - 文件工具
  - IO 工具
  - 属性源工具

### 🎯 示例应用 (xtt-cloud-examples)

#### 🏪 微服务示例
- **integrated-account**: 账户服务
  - 用户账户管理
  - 余额操作
  - 账户信息查询

- **integrated-storage**: 库存服务
  - 商品库存管理
  - 库存扣减
  - 库存查询

- **integrated-order**: 订单服务
  - 订单创建与管理
  - 服务间调用示例
  - 分布式事务演示

- **integrated-gateway**: 网关服务
  - 路由转发
  - 限流降级
  - 统一入口

#### 📨 消息服务示例
- **integrated-praise-provider**: 点赞消息生产者
  - 消息发送
  - 业务逻辑处理

- **integrated-praise-consumer**: 点赞消息消费者
  - 消息消费
  - 业务处理
  - 数据持久化

#### 🎨 前端示例
- **integrated-frontend**: 前端应用
  - Vue.js 单页应用
  - 微服务调用示例
  - 用户界面

#### 🔧 配置初始化
- **config-init**: 配置初始化
  - 数据库初始化脚本
  - 配置文件模板
  - 部署脚本

## 🛠️ 技术栈

- **框架**: Spring Boot, Spring Cloud, Spring Cloud Alibaba
- **注册中心**: Nacos
- **配置中心**: Nacos
- **熔断限流**: Sentinel
- **分布式事务**: Seata
- **消息队列**: RocketMQ
- **网关**: Spring Cloud Gateway
- **数据库**: MySQL
- **前端**: Vue.js, Vite
- **容器化**: Docker, Docker Compose
- **编排**: Kubernetes (Helm Charts)

## 🚀 快速开始

### 环境要求
- JDK 8+
- Maven 3.6+
- Docker & Docker Compose
- MySQL 5.7+

### 本地开发
```bash
# 克隆项目
git clone https://github.com/xiaotian222/xtt-cloud.git
cd xtt-cloud

# 编译项目
mvn clean install -DskipTests

# 启动服务
docker-compose -f xtt-cloud-examples/docker-compose/docker-compose-env.yml up -d
docker-compose -f xtt-cloud-examples/docker-compose/docker-compose-service.yml up -d
```

### 配置说明
1. 修改 `xtt-cloud-examples/config-init/config/` 下的配置文件
2. 执行 `xtt-cloud-examples/config-init/scripts/nacos-config-quick.sh` 初始化配置
3. 启动各个微服务应用

## 📚 文档

- [本地部署指南](xtt-cloud-examples/docs/zh/local-deployment-zh.md)
- [Docker Compose 部署](xtt-cloud-examples/docs/zh/docker-compose-deploy-zh.md)
- [Kubernetes 部署](xtt-cloud-examples/docs/zh/kubernetes-deployment-zh.md)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目基于 Apache License 2.0 开源协议。
