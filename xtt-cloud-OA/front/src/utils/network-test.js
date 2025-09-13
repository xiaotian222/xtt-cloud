// 网络连接测试工具
export const testNetworkConnection = async (url) => {
  try {
    console.log(`测试网络连接: ${url}`)
    const response = await fetch(url, {
      method: 'GET',
      mode: 'cors',
      cache: 'no-cache'
    })
    
    console.log(`网络连接测试结果:`, {
      url,
      status: response.status,
      statusText: response.statusText,
      ok: response.ok,
      headers: Object.fromEntries(response.headers.entries())
    })
    
    return {
      success: response.ok,
      status: response.status,
      statusText: response.statusText,
      url
    }
  } catch (error) {
    console.error(`网络连接测试失败: ${url}`, error)
    return {
      success: false,
      error: error.message,
      url
    }
  }
}

// 测试多个端点
export const testMultipleEndpoints = async (baseUrl) => {
  const endpoints = [
    '/api/platform/healthz',
    '/api/platform/users',
    '/api/platform/roles',
    '/api/platform/permissions'
  ]
  
  const results = []
  
  for (const endpoint of endpoints) {
    const fullUrl = `${baseUrl}${endpoint}`
    const result = await testNetworkConnection(fullUrl)
    results.push(result)
    
    // 添加延迟避免过快请求
    await new Promise(resolve => setTimeout(resolve, 100))
  }
  
  return results
}

// 检查网关是否可达
export const checkGatewayHealth = async (gatewayUrl) => {
  try {
    const response = await fetch(`${gatewayUrl}/actuator/health`, {
      method: 'GET',
      mode: 'cors'
    })
    
    if (response.ok) {
      const data = await response.json()
      return {
        success: true,
        data,
        message: '网关健康检查通过'
      }
    } else {
      return {
        success: false,
        message: `网关健康检查失败: ${response.status} ${response.statusText}`
      }
    }
  } catch (error) {
    return {
      success: false,
      message: `网关连接失败: ${error.message}`
    }
  }
}
