# Gateway 启动与验证（集中配置 + Nacos）

## 依赖前置
- Nacos 服务已就绪（含配置组 `xtt-cloud-oa`，如需也可直接读取本地文件配置）。
- 数据库/Redis 等基础设施按需就绪（参考各模块 `application.yaml`）。

## 配置来源
- 网关通过下述方式导入集中配置：
```
spring:
  config:
    import: optional:file:config-init/config/gateway.yaml
```
- 路由规则在 `config-init/config/gateway.yaml` 中维护，使用 `lb://auth`、`lb://platform`，依赖 Nacos 服务发现。

## 启动顺序（PowerShell）
在 `xtt-cloud-OA` 目录执行：
```
# 1) 启动 platform（端口默认为 8085）
mvn -pl platform -am spring-boot:run

# 2) 启动 auth（端口默认为 8020）
mvn -pl auth -am spring-boot:run

# 3) 启动 gateway（端口默认为 30010）
mvn -pl gateway -am spring-boot:run
```
提示：如使用多窗口并行启动，请确认 Nacos 注册可见后再启动网关。

## 验证路由
- 认证服务（通过网关）
```
POST http://localhost:30010/api/auth/login
Content-Type: application/json

{"username":"admin","password":"password"}
```
- 平台服务（通过网关）
```
GET http://localhost:30010/api/platform/**
Authorization: Bearer <your_jwt_token>
```

更多命令行示例可参考 `gateway-test.md`。

## 常见问题
- 若本地仅调试且临时不启用 Nacos，可将 `gateway.yaml` 内的 `lb://...` 改为直连地址 `http://localhost:8020`、`http://localhost:8085`，或新增 `gateway-local.yaml` 并通过 profile 切换。
- 如遇认证拦截问题，检查 `gateway.yaml` 的 `gateway.auth.enabled` 与 `skip-paths` 配置，以及 `AuthGlobalFilter` 日志级别。
