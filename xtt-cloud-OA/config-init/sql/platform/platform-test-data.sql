-- Platform 服务测试数据

-- 1. 插入用户数据
INSERT INTO sys_user (username, password, nickname, email, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '系统管理员', 'admin@xtt.com', '13800138000', 1),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '张三', 'zhangsan@xtt.com', '13800138001', 1),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '李四', 'lisi@xtt.com', '13800138002', 1),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '王五', 'wangwu@xtt.com', '13800138003', 1),
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '赵六', 'zhaoliu@xtt.com', '13800138004', 0);

-- 2. 插入角色数据
INSERT INTO sys_role (code, name, description) VALUES
('ADMIN', '系统管理员', '拥有系统所有权限'),
('MANAGER', '部门经理', '部门管理权限'),
('USER', '普通用户', '基础用户权限'),
('AUDITOR', '审计员', '审计查看权限');

-- 3. 插入权限数据
INSERT INTO sys_permission (code, name, type) VALUES
-- 用户管理权限
('user:list', '用户列表', 'api'),
('user:create', '创建用户', 'api'),
('user:update', '更新用户', 'api'),
('user:delete', '删除用户', 'api'),
('user:grant', '用户授权', 'api'),

-- 角色管理权限
('role:list', '角色列表', 'api'),
('role:create', '创建角色', 'api'),
('role:update', '更新角色', 'api'),
('role:delete', '删除角色', 'api'),
('role:grant', '角色授权', 'api'),

-- 权限管理权限
('perm:list', '权限列表', 'api'),
('perm:create', '创建权限', 'api'),
('perm:update', '更新权限', 'api'),
('perm:delete', '删除权限', 'api'),

-- 部门管理权限
('dept:list', '部门列表', 'api'),
('dept:create', '创建部门', 'api'),
('dept:update', '更新部门', 'api'),
('dept:delete', '删除部门', 'api'),

-- 应用管理权限
('app:list', '应用列表', 'api'),
('app:create', '创建应用', 'api'),
('app:update', '更新应用', 'api'),
('app:delete', '删除应用', 'api'),

-- 菜单管理权限
('menu:list', '菜单列表', 'api'),
('menu:create', '创建菜单', 'api'),
('menu:update', '更新菜单', 'api'),
('menu:delete', '删除菜单', 'api'),

-- 审计日志权限
('audit:list', '审计日志', 'api'),

-- 菜单权限
('platform:user', '用户管理', 'menu'),
('platform:role', '角色管理', 'menu'),
('platform:perm', '权限管理', 'menu'),
('platform:dept', '部门管理', 'menu'),
('platform:app', '应用管理', 'menu'),
('platform:menu', '菜单管理', 'menu'),
('platform:audit', '审计日志', 'menu');

-- 4. 插入用户角色关系
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), -- admin -> ADMIN
(2, 2), -- zhangsan -> MANAGER
(3, 2), -- lisi -> MANAGER
(4, 3), -- wangwu -> USER
(5, 4); -- zhaoliu -> AUDITOR

-- 5. 插入角色权限关系
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
-- ADMIN 角色拥有所有权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), -- 用户管理
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10), -- 角色管理
(1, 11), (1, 12), (1, 13), (1, 14), -- 权限管理
(1, 15), (1, 16), (1, 17), (1, 18), -- 部门管理
(1, 19), (1, 20), (1, 21), (1, 22), -- 应用管理
(1, 23), (1, 24), (1, 25), (1, 26), -- 菜单管理
(1, 27), -- 审计日志
(1, 28), (1, 29), (1, 30), (1, 31), (1, 32), (1, 33), (1, 34), -- 菜单权限

-- MANAGER 角色权限
(2, 1), (2, 2), (2, 3), (2, 4), -- 用户管理（除授权）
(2, 6), (2, 7), (2, 8), (2, 9), -- 角色管理（除授权）
(2, 11), (2, 12), (2, 13), (2, 14), -- 权限管理
(2, 15), (2, 16), (2, 17), (2, 18), -- 部门管理
(2, 19), (2, 20), (2, 21), (2, 22), -- 应用管理
(2, 23), (2, 24), (2, 25), (2, 26), -- 菜单管理
(2, 27), -- 审计日志
(2, 28), (2, 29), (2, 30), (2, 31), (2, 32), (2, 33), (2, 34), -- 菜单权限

-- USER 角色权限
(3, 1), -- 用户列表
(3, 6), -- 角色列表
(3, 11), -- 权限列表
(3, 15), -- 部门列表
(3, 19), -- 应用列表
(3, 23), -- 菜单列表
(3, 28), (3, 29), (3, 30), (3, 31), (3, 32), (3, 33), (3, 34), -- 菜单权限

-- AUDITOR 角色权限
(4, 1), -- 用户列表
(4, 6), -- 角色列表
(4, 11), -- 权限列表
(4, 15), -- 部门列表
(4, 19), -- 应用列表
(4, 23), -- 菜单列表
(4, 27), -- 审计日志
(4, 28), (4, 29), (4, 30), (4, 31), (4, 32), (4, 33), (4, 34); -- 菜单权限

-- 6. 插入部门数据
INSERT INTO sys_dept (parent_id, name, sort_no) VALUES
(NULL, '总公司', 1),
(1, '技术部', 1),
(1, '市场部', 2),
(1, '财务部', 3),
(2, '开发组', 1),
(2, '测试组', 2),
(3, '销售组', 1),
(3, '推广组', 2);

-- 7. 插入用户部门关系
INSERT INTO sys_user_dept (user_id, dept_id) VALUES
(1, 1), -- admin -> 总公司
(2, 2), -- zhangsan -> 技术部
(3, 3), -- lisi -> 市场部
(4, 5), -- wangwu -> 开发组
(5, 2); -- zhaoliu -> 技术部

-- 8. 插入应用数据
INSERT INTO sys_app (code, name, enabled) VALUES
('oa-platform', 'OA平台', 1),
('oa-auth', '认证服务', 1),
('oa-gateway', '网关服务', 1),
('oa-frontend', '前端应用', 1);

-- 9. 插入菜单数据
INSERT INTO sys_menu (app_id, parent_id, name, path, permission, type, icon, sort_no, visible) VALUES
-- OA平台菜单
(1, NULL, '系统管理', '/platform', NULL, 'catalog', 'system', 1, 1),
(1, 1, '用户管理', '/platform/user', 'platform:user', 'menu', 'user', 1, 1),
(1, 1, '角色管理', '/platform/role', 'platform:role', 'menu', 'role', 2, 1),
(1, 1, '权限管理', '/platform/perm', 'platform:perm', 'menu', 'permission', 3, 1),
(1, 1, '部门管理', '/platform/dept', 'platform:dept', 'menu', 'dept', 4, 1),
(1, 1, '应用管理', '/platform/app', 'platform:app', 'menu', 'app', 5, 1),
(1, 1, '菜单管理', '/platform/menu', 'platform:menu', 'menu', 'menu', 6, 1),
(1, 1, '审计日志', '/platform/audit', 'platform:audit', 'menu', 'audit', 7, 1),

-- 用户管理子菜单
(1, 2, '新增用户', NULL, 'user:create', 'button', NULL, 1, 1),
(1, 2, '编辑用户', NULL, 'user:update', 'button', NULL, 2, 1),
(1, 2, '删除用户', NULL, 'user:delete', 'button', NULL, 3, 1),
(1, 2, '用户授权', NULL, 'user:grant', 'button', NULL, 4, 1),

-- 角色管理子菜单
(1, 3, '新增角色', NULL, 'role:create', 'button', NULL, 1, 1),
(1, 3, '编辑角色', NULL, 'role:update', 'button', NULL, 2, 1),
(1, 3, '删除角色', NULL, 'role:delete', 'button', NULL, 3, 1),
(1, 3, '角色授权', NULL, 'role:grant', 'button', NULL, 4, 1);

-- 10. 插入审计日志数据
INSERT INTO sys_audit_log (username, action, resource, method, ip, result, message) VALUES
('admin', '用户登录', '/api/auth/login', 'POST', '127.0.0.1', 'SUCCESS', '用户登录成功'),
('admin', '创建用户', '/api/platform/users', 'POST', '127.0.0.1', 'SUCCESS', '创建用户：testuser'),
('zhangsan', '查看用户列表', '/api/platform/users', 'GET', '127.0.0.1', 'SUCCESS', '查询用户列表'),
('lisi', '更新角色', '/api/platform/roles/2', 'PUT', '127.0.0.1', 'SUCCESS', '更新角色信息'),
('admin', '删除权限', '/api/platform/permissions/5', 'DELETE', '127.0.0.1', 'SUCCESS', '删除权限：user:delete'),
('wangwu', '查看部门列表', '/api/platform/departments', 'GET', '127.0.0.1', 'SUCCESS', '查询部门列表'),
('zhaoliu', '查看审计日志', '/api/platform/audit-logs', 'GET', '127.0.0.1', 'SUCCESS', '查询审计日志');
