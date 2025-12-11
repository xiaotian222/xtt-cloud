import request from '@/utils/request'

// 获取文档列表
export const getDocumentList = (params) => {
  return request({
    url: '/document/list',
    method: 'get',
    params
  })
}

// 获取文档详情
export const getDocumentDetail = (id) => {
  return request({
    url: `/document/${id}`,
    method: 'get'
  })
}

// 创建文档
export const createDocument = (data) => {
  return request({
    url: '/document',
    method: 'post',
    data
  })
}

// 更新文档
export const updateDocument = (id, data) => {
  return request({
    url: `/document/${id}`,
    method: 'put',
    data
  })
}

// 删除文档
export const deleteDocument = (id) => {
  return request({
    url: `/document/${id}`,
    method: 'delete'
  })
}

// 获取流程列表
export const getFlowList = (params) => {
  return request({
    url: '/document/flows',
    method: 'get',
    params
  })
}

// 启动流程
export const startFlow = (documentId, flowDefId) => {
  return request({
    url: '/document/flows/start',
    method: 'post',
    data: { documentId, flowDefId }
  })
}

