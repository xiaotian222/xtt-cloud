// 网关路由测试工具
export const testGatewayRoute = async (path, method = 'GET', data = null) => {
  try {
    console.log(`测试网关路由: ${method} ${path}`)
    
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json'
      }
    }
    
    if (data) {
      options.body = JSON.stringify(data)
    }
    
    const response = await fetch(`/api${path}`, options)
    
    const result = {
      success: response.ok,
      status: response.status,
      statusText: response.statusText,
      path: `/api${path}`,
      method,
      headers: Object.fromEntries(response.headers.entries())
    }
    
    if (response.ok) {
      try {
        result.data = await response.json()
      } catch (e) {
        result.text = await response.text()
      }
    } else {
      try {
        result.error = await response.json()
      } catch (e) {
        result.errorText = await response.text()
      }
    }
    
    console.log('网关路由测试结果:', result)
    return result
  } catch (error) {
    console.error('网关路由测试失败:', error)
    return {
      success: false,
      error: error.message,
      path: `/api${path}`,
      method
    }
  }
}

export const testPlatformRoutes = async () => {
  const routes = [
    // 基础API
    { path: '/platform/healthz', method: 'GET' },
    { path: '/platform/users', method: 'GET' },
    { path: '/platform/roles', method: 'GET' },
    { path: '/platform/permissions', method: 'GET' },
    
    // 外部API
    { path: '/platform/external/users/username/admin', method: 'GET' },
    { path: '/platform/external/users/username/admin/permissions', method: 'GET' },
    { path: '/platform/external/users/username/admin/roles', method: 'GET' },
    
    // 直接后端API（应该404）
    { path: '/platform/external/users/username/admin/permissions', method: 'GET' }
  ]
  
  const results = []
  
  for (const route of routes) {
    const result = await testGatewayRoute(route.path, route.method)
    results.push({
      ...route,
      ...result
    })
    
    // 添加延迟避免过快请求
    await new Promise(resolve => setTimeout(resolve, 200))
  }
  
  return results
}

export const compareDirectVsGateway = async (path) => {
  const directUrl = `http://localhost:8085/api${path}`
  const gatewayUrl = `/api${path}`
  
  console.log('比较直接访问 vs 网关访问')
  console.log('直接URL:', directUrl)
  console.log('网关URL:', gatewayUrl)
  
  try {
    // 测试直接访问
    const directResponse = await fetch(directUrl)
    const directData = await directResponse.json()
    
    // 测试网关访问
    const gatewayResponse = await fetch(gatewayUrl)
    const gatewayData = await gatewayResponse.json()
    
    return {
      direct: {
        success: directResponse.ok,
        status: directResponse.status,
        data: directData
      },
      gateway: {
        success: gatewayResponse.ok,
        status: gatewayResponse.status,
        data: gatewayData
      },
      comparison: {
        sameStatus: directResponse.status === gatewayResponse.status,
        sameData: JSON.stringify(directData) === JSON.stringify(gatewayData)
      }
    }
  } catch (error) {
    return {
      error: error.message,
      direct: null,
      gateway: null
    }
  }
}
