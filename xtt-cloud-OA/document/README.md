# Document Service

公文管理系统是OA系统的重要组成部分，提供公文起草、审批、归档等全流程管理功能。

## 🎯 功能特性

- **公文管理**: 公文起草、编辑、提交、发布
- **流程审批**: 灵活的审批流程配置和执行
- **待办中心**: 待办事项和已办事项管理
- **权限控制**: 基于RBAC的细粒度权限管理
- **文档归档**: 公文分类存储和检索
- **统一流程管理**: 支持发文、收文等多种公文流程

## 📁 项目结构

```
document/
├── src/main/java/xtt/cloud/oa/document/
│   ├── application/     # 应用服务层
│   ├── domain/          # 领域层
│   ├── infrastructure/  # 基础设施层
│   ├── interfaces/      # 接口层
│   └── DocumentApplication.java # 启动类
├── src/main/resources/
│   ├── mapper/          # MyBatis映射文件
│   └── application.yaml # 配置文件
├── docs/                # 设计文档
│   ├── system-design.md # 系统设计文档
│   ├── database-design.md # 数据库设计
│   ├── flow-design.md   # 流程设计
│   ├── api-document.md  # API接口文档
│   └── ...              # 其他文档
├── pom.xml              # Maven配置
└── README.md            # 模块说明
```

## 🚀 技术栈

- **Spring Boot 2.7.x** - 应用框架
- **MyBatis Plus** - ORM框架
- **MySQL** - 关系型数据库
- **Nacos** - 服务发现和配置中心
- **Redis** - 缓存（可选）
- **RocketMQ** - 消息队列（可选）

## 📋 核心模块

### 1. 公文管理模块
- 公文创建、编辑、删除
- 公文版本管理
- 公文分类和标签

### 2. 流程管理模块
- 审批流程定义
- 流程实例执行
- 节点状态跟踪

### 3. 待办管理模块
- 待办事项生成
- 待办事项处理
- 已办事项记录

### 4. 权限管理模块
- 数据权限控制
- 功能权限分配
- 部门级别权限

### 5. 统一流程模块
- 统一流程管理（发文、收文等）
- 流程扩展信息管理
- 外单位协作处理
- 承办任务管理

## 🛠️ 配置说明

### 服务端口
- **默认端口**: 8086

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xtt_cloud_oa?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### Nacos配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
      config:
        server-addr: nacos-server:8848
        group: xtt-cloud-oa
        file-extension: yaml
```

## 📞 API接口

### 公文管理接口
- `POST /api/document/documents` - 创建公文
- `GET /api/document/documents/{id}` - 获取公文详情
- `PUT /api/document/documents/{id}` - 更新公文
- `DELETE /api/document/documents/{id}` - 删除公文
- `GET /api/document/documents` - 查询公文列表

### 流程管理接口
- `POST /api/document/flows` - 创建流程实例
- `POST /api/document/flows/issuance-info` - 创建发文流程扩展信息
- `POST /api/document/flows/receipt-info` - 创建收文流程扩展信息
- `POST /api/document/flows/{id}/start-issuance` - 启动发文流程
- `POST /api/document/flows/{id}/start-receipt` - 启动收文流程
- `POST /api/document/flows/external-receipts` - 外单位签收登记
- `POST /api/document/flows/handlings` - 创建承办记录
- `PUT /api/document/flows/handlings/{id}` - 更新承办记录

### 待办管理接口
- `GET /api/document/todo/list` - 获取待办事项列表
- `GET /api/document/todo/done` - 获取已办事项列表
- `POST /api/document/todo/{id}/process` - 处理待办事项

## 📚 文档目录

所有设计文档都保存在 `docs/` 目录下：

- [系统设计文档](docs/system-design.md) - 系统整体架构设计
- [数据库设计](docs/database-design.md) - 数据库表结构设计
- [流程设计](docs/flow-design.md) - 审批流程详细设计
- [接口文档](docs/api-document.md) - RESTful API接口说明

## 🚀 启动方式

### 本地开发
```bash
cd xtt-cloud-OA/document
mvn spring-boot:run
```

### Docker部署
```bash
# 构建镜像
mvn clean package -DskipTests
docker build -t document-service .

# 运行容器
docker run -p 8086:8086 document-service
```

## 📝 开发指南

### 添加新功能
1. 在 `domain` 层添加领域模型
2. 在 `application` 层实现业务逻辑
3. 在 `interfaces` 层添加REST接口
4. 在 `infrastructure` 层添加数据访问实现

### 数据库操作
1. 在 `src/main/resources/mapper/` 目录下添加MyBatis映射文件
2. 在 `domain/entity/` 目录下添加实体类
3. 在 `domain/mapper/` 目录下添加Mapper接口

### 配置管理
1. 在Nacos配置中心添加配置文件
2. 在 `src/main/resources/application.yaml` 中引用配置