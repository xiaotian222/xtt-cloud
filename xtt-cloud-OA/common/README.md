# Common Module

OA 系统的公共模块，提供通用的工具类和响应格式。

## 📋 功能特性

- **统一响应格式**: `Result<T>` 类提供统一的 API 响应格式
- **结果枚举**: `ResultEnum` 定义标准的结果码和消息
- **业务异常**: `BusinessException` 用于业务逻辑异常处理
- **结果接口**: `IResult` 接口定义结果的基本结构

## 📁 项目结构

```
common/
├── src/main/java/xtt/cloud/oa/common/
│   ├── Result.java           # 统一响应结果类
│   ├── ResultEnum.java       # 结果枚举类
│   ├── IResult.java          # 结果接口
│   └── BusinessException.java # 业务异常类
└── pom.xml                   # Maven 配置
```

## 🔧 使用示例

### 成功响应
```java
// 返回成功结果
return Result.success(data);

// 返回成功结果和自定义消息
return Result.success("操作成功", data);
```

### 失败响应
```java
// 返回默认失败结果
return Result.failed();

// 返回自定义失败消息
return Result.failed("操作失败");

// 返回自定义错误结果
return Result.failed(customErrorResult);
```

### 业务异常
```java
// 抛出业务异常
throw new BusinessException("用户不存在");
```

## 📦 依赖说明

- **Spring Boot Web**: 提供 Web 相关功能
- **Spring Boot Validation**: 提供验证功能
- **Jackson**: JSON 序列化支持

## 🔗 相关模块

- **auth**: 认证服务，使用 common 模块的响应格式
- **gateway**: 网关服务，使用 common 模块的响应格式

## 📝 注意事项

1. 所有 API 响应都应该使用 `Result<T>` 格式
2. 业务异常应该使用 `BusinessException`
3. 自定义结果码应该在 `ResultEnum` 中定义
