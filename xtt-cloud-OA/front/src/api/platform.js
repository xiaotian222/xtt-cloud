import request from '@/utils/request'

// 用户
export const listUsers = () => request({ url: '/platform/users', method: 'get' })
export const getUser = (id) => request({ url: `/platform/users/${id}`, method: 'get' })
export const createUser = (data) => request({ url: '/platform/users', method: 'post', data })
export const updateUser = (id, data) => request({ url: `/platform/users/${id}`, method: 'put', data })
export const deleteUser = (id) => request({ url: `/platform/users/${id}`, method: 'delete' })
export const grantUserRoles = (id, roleIds) => request({ url: `/platform/users/${id}/roles`, method: 'post', data: roleIds })

// 角色
export const listRoles = () => request({ url: '/platform/roles', method: 'get' })
export const createRole = (data) => request({ url: '/platform/roles', method: 'post', data })
export const updateRole = (id, data) => request({ url: `/platform/roles/${id}`, method: 'put', data })
export const deleteRole = (id) => request({ url: `/platform/roles/${id}`, method: 'delete' })
export const grantRolePerms = (id, permIds) => request({ url: `/platform/roles/${id}/permissions`, method: 'post', data: permIds })

// 权限
export const listPerms = () => request({ url: '/platform/permissions', method: 'get' })
export const createPerm = (data) => request({ url: '/platform/permissions', method: 'post', data })
export const updatePerm = (id, data) => request({ url: `/platform/permissions/${id}`, method: 'put', data })
export const deletePerm = (id) => request({ url: `/platform/permissions/${id}`, method: 'delete' })


