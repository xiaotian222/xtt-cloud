### XTT Cloud OA 模块与功能总览

本文件对 `xtt-cloud-OA` 工程的现有模块与主要功能进行概览性说明，便于团队成员快速理解系统架构与职责边界。

---

## 模块结构

- **gateway**: API 网关与路由层，进行统一入口、转发、基础鉴权与过滤。
- **auth**: 认证服务，负责登录、JWT 签发与校验、用户信息提取等。
- **common**: 公共库，提供统一返回体、错误码与通用异常。
- **platform**: 平台与 RBAC 核心，提供用户、角色、权限等内部与对外查询接口。
- **front**: 前端工程（Vite + Vue），OA 管理端 UI。
- **config-init**: 初始化脚本与示例配置（如 `auth.yaml`、`gateway.yaml`、`platform.yaml`、数据源与初始化 SQL）。
- **docs**: 项目文档。
- 启动脚本: `start.sh`、`start.bat` 用于一键启动/本地开发便利化。

---

## gateway 模块

- **职责**:
  - 路由转发：将前端与外部请求分发到后端各微服务。
  - 基础鉴权/过滤：结合 `auth` 的 JWT 进行请求放行或拦截（详见项目中的 `GatewayConfig`、`AuthConfig`）。
  - 可扩展限流、灰度、审计日志等能力。

- **关键点**:
  - 与 `auth` 协作完成统一认证。
  - 对接下游 `platform`、`auth` 等服务接口。

---

## auth 模块

- **定位**: 基于 JWT 的认证中心。
- **功能**:
  - 登录与 Token 签发：`POST /auth/login`。
  - Token 刷新：`POST /auth/refresh`。
  - Token 校验：`GET /auth/validate`。
  - 获取当前用户：`GET /auth/user`（需 Bearer Token）。
  - 登出：`POST /auth/logout`。
  - 用户管理与示例数据：`/auth/users`（增删改查示例）。
  - 健康与测试接口：`/test/health`、`/test/public`、`/test/protected`。

- **技术要点**:
  - Spring Security + JWT，无状态会话，便于水平扩展。
  - 默认示例用户：`admin/user/manager` 等（详见 `auth/README.md`）。
  - 可集成 Nacos 注册与配置中心。

---

## common 模块

- **定位**: 公共基础库，供所有服务复用。
- **内容**:
  - 统一响应封装：`Result<T>`。
  - 结果枚举：`ResultEnum`。
  - 业务异常：`BusinessException`。
  - 结果接口：`IResult`。

- **约定**:
  - 对外/对内 API 均建议返回 `Result<T>` 统一结构。
  - 业务错误通过 `BusinessException` 抛出并转换为统一响应。

---

## platform 模块（RBAC 核心）

- **定位**: 提供用户、角色、权限、部门等平台级能力；对内对外暴露查询接口。

- **内部接口**:
  - 典型增删改查控制器：`UserController`、`RoleController`、`PermissionController` 等。

- **对外接口前缀**: `/api/platform/external/`
  - 用户：`/users`（按用户名/ID 查询、批量查询、获取角色/权限、是否存在等）。
  - 角色：`/roles`（按代码/ID 查询、批量查询、获取权限、是否存在、查询全部）。
  - 权限：`/permissions`（按代码/ID 查询、按类型查询、批量查询、用户是否拥有权限/任意权限/全部权限、查询全部）。
  - 详细字段与示例响应请参考 `platform/EXTERNAL_API.md`。

- **使用场景**:
  - `auth` 登录后需要查询用户信息、角色和权限。
  - 业务服务做接口级权限控制与数据权限。
  - `gateway` 做统一鉴权与拦截时的用户校验与权限核验。

- **实现要点**:
  - 建议结合缓存（如 Redis）降低查询压力（文档中已有说明）。
  - 支持批量查询，减少 N+1 与网络往返。

---

## front 前端

- **技术栈**: Vite + Vue。
- **定位**: OA 管理端 UI，承载用户登录、菜单与权限路由控制、平台与业务功能操作界面。
- **目录**: `xtt-cloud-OA/front`，包含 `src`、`public`、`index.html` 等。

---

## config-init 配置与初始化

- **配置文件**: `auth.yaml`、`gateway.yaml`、`platform.yaml`、`datasource-config.yaml` 等。
- **初始化 SQL**: `sql/init.sql`，用于初始化基础表与数据。
- **脚本**: `scripts` 下提供快速初始化脚本（如一键推送配置到配置中心等）。

---

## 典型调用关系

1. 前端请求 → `gateway`（统一入口）
2. 认证相关请求 → `auth`（签发/校验 JWT）
3. 业务/权限查询 → `platform`（RBAC 核心查询能力，对外接口位于 `/api/platform/external/`）
4. 公共返回/异常与 DTO → 来自 `common`

---

## 开发与部署提示

- 所有服务建议统一通过网关访问，携带 `Authorization: Bearer <token>`。
- 推荐在 `gateway` 侧聚合认证与日志审计，业务服务保持无状态。
- 可按需引入注册配置中心（如 Nacos）与缓存（如 Redis）。

---

## 参考文档

- `xtt-cloud-OA/auth/README.md`（认证 API 详解）
- `xtt-cloud-OA/common/README.md`（统一返回与异常）
- `xtt-cloud-OA/platform/EXTERNAL_API.md`（对外接口清单）


