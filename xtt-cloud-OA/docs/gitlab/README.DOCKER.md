# XTT Cloud OA Docker 部署指南

## 概述

本文档介绍如何使用 Docker 和 Docker Compose 构建和运行整个 `xtt-cloud-OA` 工程。

## 系统架构

```
┌─────────┐
│ Gateway │ (30010)
└────┬────┘
     │
     ├─── Auth Service (8020)
     ├─── Platform Service (8085)
     └─── Workflow Service (8091)
           │
           ├─── MySQL (3306)
           ├─── Redis (6379)
           └─── Nacos (8848)
```

## 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 至少 4GB 可用内存
- 至少 10GB 可用磁盘空间

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd xtt-cloud-OA
```

### 2. 构建并启动所有服务

```bash
# 构建并启动所有服务（包括基础设施和业务服务）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 验证服务

```bash
# 检查 Nacos
curl http://localhost:8848/nacos/

# 检查 Gateway
curl http://localhost:30010/actuator/health

# 检查 Auth
curl http://localhost:8020/actuator/health

# 检查 Platform
curl http://localhost:8085/actuator/health

# 检查 Workflow
curl http://localhost:8091/actuator/health
```

## 服务说明

### 基础设施服务

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库服务 |
| Redis | 6379 | 缓存服务 |
| Nacos | 8848 | 服务注册与配置中心 |

### 业务服务

| 服务 | 端口 | 说明 | 依赖 |
|------|------|------|------|
| Gateway | 30010 | API 网关 | Nacos |
| Auth | 8020 | 认证服务 | Nacos, Redis |
| Platform | 8085 | 平台服务 | MySQL, Nacos, Redis |
| Workflow | 8091 | 工作流服务 | MySQL, Nacos, Redis, Platform |

## 单独构建服务

### 构建单个服务镜像

```bash
# 从项目根目录执行
cd xtt-cloud-OA

# 构建 Gateway
docker build -f gateway/Dockerfile -t xtt-cloud-oa/gateway:latest .

# 构建 Auth
docker build -f auth/Dockerfile -t xtt-cloud-oa/auth:latest .

# 构建 Platform
docker build -f platform/Dockerfile -t xtt-cloud-oa/platform:latest .

# 构建 Workflow
docker build -f workflow/Dockerfile -t xtt-cloud-oa/workflow:latest .
```

### 运行单个服务

```bash
# 运行 Gateway
docker run -d \
  --name gateway \
  -p 30010:30010 \
  -e SPRING_PROFILES_ACTIVE=dev \
  --network xtt-cloud-oa-network \
  xtt-cloud-oa/gateway:latest
```

## 环境变量配置

### 通用环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | Spring 环境配置 | dev |
| JAVA_OPTS | JVM 参数 | -Xms512m -Xmx1024m -XX:+UseG1GC |

### 服务特定环境变量

在 `docker-compose.yml` 中已配置各服务的环境变量，可根据需要修改。

## 数据持久化

Docker Compose 使用命名卷来持久化数据：

- `mysql-data`: MySQL 数据
- `redis-data`: Redis 数据
- `nacos-data`: Nacos 日志和数据

### 备份数据

```bash
# 备份 MySQL
docker exec xtt-cloud-oa-mysql mysqldump -u root -proot123456 --all-databases > backup.sql

# 备份 Redis
docker exec xtt-cloud-oa-redis redis-cli --rdb /data/dump.rdb
```

### 恢复数据

```bash
# 恢复 MySQL
docker exec -i xtt-cloud-oa-mysql mysql -u root -proot123456 < backup.sql
```

## 日志管理

日志文件存储在 `./logs/<service-name>/` 目录下。

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f gateway
docker-compose logs -f auth
docker-compose logs -f platform
docker-compose logs -f workflow

# 查看容器内日志
docker logs -f xtt-cloud-oa-gateway
```

## 服务管理

### 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 启动特定服务
docker-compose up -d gateway auth
```

### 停止服务

```bash
# 停止所有服务
docker-compose stop

# 停止特定服务
docker-compose stop gateway
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart gateway
```

### 删除服务

```bash
# 停止并删除所有服务（保留数据卷）
docker-compose down

# 停止并删除所有服务（包括数据卷）
docker-compose down -v
```

## 健康检查

所有服务都配置了健康检查，可以通过以下方式查看：

```bash
# 查看服务健康状态
docker-compose ps

# 手动检查健康端点
curl http://localhost:30010/actuator/health
curl http://localhost:8020/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8091/actuator/health
```

## GitLab CI/CD

项目已配置 GitLab CI/CD，自动执行以下流程：

1. **构建阶段**: 编译所有模块
2. **测试阶段**: 运行单元测试
3. **打包阶段**: 打包所有 JAR 文件
4. **Docker 构建阶段**: 构建所有服务的 Docker 镜像并推送到镜像仓库
5. **部署阶段**: 部署到开发/生产环境（手动触发）

### CI/CD 变量配置

在 GitLab 项目设置中配置以下变量：

- `CI_REGISTRY`: Docker 镜像仓库地址
- `CI_REGISTRY_USER`: 镜像仓库用户名
- `CI_REGISTRY_PASSWORD`: 镜像仓库密码

### 触发 CI/CD

- **自动触发**: 推送到 `develop`、`main`、`master` 分支
- **手动部署**: 在 GitLab Pipeline 界面手动触发部署任务

## 生产环境部署

### 1. 使用 Docker Compose

```bash
# 使用生产环境配置
docker-compose -f docker-compose.prod.yml up -d
```

### 2. 使用 Kubernetes

```bash
# 应用 Kubernetes 配置
kubectl apply -f k8s/

# 更新服务镜像
kubectl set image deployment/gateway gateway=xtt-cloud-oa/gateway:latest
```

### 3. 生产环境建议

1. **资源限制**: 为容器设置 CPU 和内存限制
2. **日志管理**: 配置日志轮转和集中式日志收集（ELK、Loki 等）
3. **监控告警**: 集成 Prometheus、Grafana 等监控工具
4. **安全加固**: 
   - 使用非 root 用户运行（已配置）
   - 定期更新基础镜像
   - 扫描镜像漏洞
   - 配置网络安全策略
5. **高可用**: 
   - 使用 Kubernetes 实现多实例部署
   - 配置负载均衡
   - 实现服务熔断和降级
6. **备份策略**: 
   - 定期备份数据库
   - 配置数据恢复流程

## 故障排查

### 服务无法启动

1. 检查日志：`docker-compose logs <service-name>`
2. 检查端口占用：`netstat -tuln | grep <port>`
3. 检查依赖服务是否正常运行
4. 检查环境变量配置

### 服务无法连接数据库

1. 检查数据库连接配置
2. 确认网络连接：`docker network inspect xtt-cloud-oa-network`
3. 检查数据库服务状态：`docker ps | grep mysql`
4. 检查数据库用户权限

### 服务无法注册到 Nacos

1. 检查 Nacos 服务是否正常运行
2. 检查 Nacos 连接配置
3. 查看 Nacos 控制台：http://localhost:8848/nacos
4. 检查网络连接

### 内存不足

调整 JVM 参数：

```yaml
environment:
  - JAVA_OPTS=-Xms256m -Xmx512m
```

## 开发环境

### 本地开发

1. 启动基础设施服务：
   ```bash
   docker-compose up -d mysql redis nacos
   ```

2. 在 IDE 中运行业务服务

3. 服务会自动连接到 Docker 中的基础设施服务

### 热重载

使用 Spring Boot DevTools 或 IDE 的热重载功能进行开发。

## 相关文档

- [Gateway 启动指南](../gateway/gateway-startup-and-verify.md)
- [服务启动顺序](../service-startup-order.md)
- [Nacos 服务发现](../nacos/nacos-service-discovery.md)

## 常见问题

### Q: 如何重置所有数据？

A: 
```bash
docker-compose down -v
docker-compose up -d
```

### Q: 如何查看服务日志？

A: 
```bash
docker-compose logs -f <service-name>
```

### Q: 如何更新服务？

A: 
```bash
# 重新构建镜像
docker-compose build <service-name>

# 重启服务
docker-compose up -d <service-name>
```

### Q: 端口冲突怎么办？

A: 修改 `docker-compose.yml` 中的端口映射，例如：
```yaml
ports:
  - "30011:30010"  # 将主机端口改为 30011
```

## 支持

如有问题，请提交 Issue 或联系开发团队。

