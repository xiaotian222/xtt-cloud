# 数据库初始化说明

## 📋 概述

本文档说明如何使用 `database-init.sql` 脚本初始化 Document 项目的数据库。

## 🚀 快速开始

### 1. 前置条件

- MySQL 5.7 或更高版本
- 具有创建数据库和表的权限

### 2. 执行步骤

#### 方式一：使用命令行

```bash
# 登录 MySQL
mysql -u root -p

# 执行 SQL 脚本
source /path/to/database-init.sql

# 或者直接执行
mysql -u root -p < database-init.sql
```

#### 方式二：使用 MySQL Workbench

1. 打开 MySQL Workbench
2. 连接到 MySQL 服务器
3. 打开 `database-init.sql` 文件
4. 执行整个脚本（或分段执行）

#### 方式三：使用 Navicat 等工具

1. 连接到 MySQL 服务器
2. 打开 `database-init.sql` 文件
3. 执行脚本

## 📊 数据库结构

脚本会创建以下内容：

### 数据库
- `document_db` - 主数据库（字符集：utf8mb4）

### 数据表（共15张）

#### 文档相关表（3张）
1. `document` - 文档表
2. `issuance_info` - 发文信息表
3. `receipt_info` - 收文信息表

#### 流程定义相关表（2张）
4. `flow_definition` - 流程定义表
5. `flow_node` - 流程节点定义表

#### 流程实例相关表（2张）
6. `flow_instance` - 流程实例表
7. `flow_node_instance` - 节点实例表

#### 自由流相关表（4张）
8. `flow_action` - 发送动作表
9. `flow_action_rule` - 动作规则表
10. `approver_scope` - 审批人选择范围表
11. `free_flow_node_instance` - 自由流节点实例扩展表

#### 任务相关表（2张）
12. `todo_task` - 待办任务表
13. `done_task` - 已办任务表

#### 扩展功能表（2张）
14. `handling` - 承办记录表
15. `external_sign_receipt` - 外单位签收登记表

## 🔍 验证安装

执行以下 SQL 验证数据库和表是否创建成功：

```sql
-- 检查数据库
SHOW DATABASES LIKE 'document_db';

-- 使用数据库
USE document_db;

-- 检查表
SHOW TABLES;

-- 检查表结构
DESC document;
DESC flow_definition;
DESC flow_instance;
```

## 📝 初始化数据

脚本中已包含部分初始化数据：

### 发送动作数据
- 单位内办理（UNIT_HANDLE）
- 核稿（REVIEW）
- 转外单位办理（EXTERNAL）
- 返回（RETURN）

## ⚠️ 注意事项

1. **备份现有数据**：如果数据库中已有数据，请先备份
2. **字符集**：确保使用 utf8mb4 字符集，支持 emoji 等特殊字符
3. **外键约束**：脚本中未创建物理外键约束，使用逻辑外键（通过代码保证）
4. **索引**：所有必要的索引已创建，可根据实际查询需求调整
5. **时间字段**：`created_at` 和 `updated_at` 使用 MySQL 的自动时间戳功能

## 🔧 自定义配置

如果需要修改数据库名称或字符集，可以编辑脚本开头的部分：

```sql
-- 修改数据库名称
CREATE DATABASE IF NOT EXISTS `your_database_name` 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

## 📚 相关文档

- [数据库设计文档](./DATABASE_DOCUMENTATION.md) - 详细的表结构设计说明
- [流程架构文档](./FLOW_ARCHITECTURE_DOCUMENTATION.md) - 流程引擎架构说明
- [项目总结文档](./PROJECT_SUMMARY.md) - 项目整体说明

## 🐛 常见问题

### Q1: 执行脚本时提示表已存在
**A**: 脚本中已包含 `DROP TABLE IF EXISTS` 语句，会先删除已存在的表。如果仍有问题，请检查权限。

### Q2: 字符集问题
**A**: 确保 MySQL 服务器支持 utf8mb4 字符集。可以通过以下命令检查：
```sql
SHOW CHARACTER SET LIKE 'utf8mb4';
```

### Q3: 时间字段自动更新不生效
**A**: 确保 MySQL 版本 >= 5.6.5，并且 `sql_mode` 包含 `ONLY_FULL_GROUP_BY`。

### Q4: 索引创建失败
**A**: 检查字段长度是否超过索引限制（MySQL 单列索引最大长度 767 字节）。

## 🔄 更新脚本

如果需要更新数据库结构：

1. **备份现有数据**
   ```sql
   mysqldump -u root -p document_db > backup.sql
   ```

2. **修改 SQL 脚本**
   - 添加新表：在脚本末尾添加 CREATE TABLE 语句
   - 修改表结构：使用 ALTER TABLE 语句

3. **执行更新脚本**
   ```sql
   source /path/to/update.sql
   ```

## 📞 技术支持

如有问题，请联系：
- 项目维护团队：XTT Cloud Team
- 文档版本：1.0
- 最后更新：2024年

---

**提示**：建议在生产环境执行前，先在测试环境验证脚本的正确性。

