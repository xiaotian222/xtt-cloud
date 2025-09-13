import request from '@/utils/request'

// 用户管理
export const listUsers = () => request({ url: '/platform/users', method: 'get' })
export const getUser = (id) => request({ url: `/platform/users/${id}`, method: 'get' })
export const createUser = (data) => request({ url: '/platform/users', method: 'post', data })
export const updateUser = (id, data) => request({ url: `/platform/users/${id}`, method: 'put', data })
export const deleteUser = (id) => request({ url: `/platform/users/${id}`, method: 'delete' })
export const grantUserRoles = (id, roleIds) => request({ url: `/platform/users/${id}/roles`, method: 'post', data: roleIds })
export const getUserPermissions = (id) => request({ url: `/platform/users/${id}/permissions`, method: 'get' })

// 角色管理
export const listRoles = () => request({ url: '/platform/roles', method: 'get' })
export const getRole = (id) => request({ url: `/platform/roles/${id}`, method: 'get' })
export const createRole = (data) => request({ url: '/platform/roles', method: 'post', data })
export const updateRole = (id, data) => request({ url: `/platform/roles/${id}`, method: 'put', data })
export const deleteRole = (id) => request({ url: `/platform/roles/${id}`, method: 'delete' })
export const grantRolePerms = (id, permIds) => request({ url: `/platform/roles/${id}/permissions`, method: 'post', data: permIds })

// 权限管理
export const listPerms = () => request({ url: '/platform/permissions', method: 'get' })
export const getPerm = (id) => request({ url: `/platform/permissions/${id}`, method: 'get' })
export const createPerm = (data) => request({ url: '/platform/permissions', method: 'post', data })
export const updatePerm = (id, data) => request({ url: `/platform/permissions/${id}`, method: 'put', data })
export const deletePerm = (id) => request({ url: `/platform/permissions/${id}`, method: 'delete' })

// 部门管理
export const listDepts = () => request({ url: '/platform/departments', method: 'get' })
export const getDept = (id) => request({ url: `/platform/departments/${id}`, method: 'get' })
export const createDept = (data) => request({ url: '/platform/departments', method: 'post', data })
export const updateDept = (id, data) => request({ url: `/platform/departments/${id}`, method: 'put', data })
export const deleteDept = (id) => request({ url: `/platform/departments/${id}`, method: 'delete' })

// 应用管理
export const listApps = () => request({ url: '/platform/applications', method: 'get' })
export const getApp = (id) => request({ url: `/platform/applications/${id}`, method: 'get' })
export const createApp = (data) => request({ url: '/platform/applications', method: 'post', data })
export const updateApp = (id, data) => request({ url: `/platform/applications/${id}`, method: 'put', data })
export const deleteApp = (id) => request({ url: `/platform/applications/${id}`, method: 'delete' })

// 菜单管理
export const listMenus = () => request({ url: '/platform/menus', method: 'get' })
export const getMenu = (id) => request({ url: `/platform/menus/${id}`, method: 'get' })
export const createMenu = (data) => request({ url: '/platform/menus', method: 'post', data })
export const updateMenu = (id, data) => request({ url: `/platform/menus/${id}`, method: 'put', data })
export const deleteMenu = (id) => request({ url: `/platform/menus/${id}`, method: 'delete' })

// 审计日志
export const listAuditLogs = (params) => request({ url: '/platform/audit-logs', method: 'get', params })
export const getAuditLog = (id) => request({ url: `/platform/audit-logs/${id}`, method: 'get' })

// 外部接口（供其他服务调用）
export const getUserByUsername = (username) => request({ url: `/platform/external/users/username/${username}`, method: 'get' })
export const getUserPermsByUsername = (username) => request({ url: `/platform/external/users/username/${username}/permissions`, method: 'get' })
export const getUserRolesByUsername = (username) => request({ url: `/platform/external/users/username/${username}/roles`, method: 'get' })
export const validatePermission = (username, permission) => request({ url: `/platform/external/users/username/${username}/permissions/${permission}`, method: 'get' })


