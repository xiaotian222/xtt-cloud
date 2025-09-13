<template>
  <div>
    <h3>API接口测试</h3>
    
    <div style="margin-bottom: 20px;">
      <h4>当前配置：</h4>
      <p><strong>网关地址：</strong>{{ config.gatewayUrl }}</p>
      <p><strong>API基础地址：</strong>{{ config.apiBaseUrl }}</p>
    </div>
    
    <div style="margin-bottom: 20px;">
      <el-button @click="testUserApi" :loading="loading">测试用户API</el-button>
      <el-button @click="testRoleApi" :loading="loading">测试角色API</el-button>
      <el-button @click="testExternalUserAPI" :loading="loading">测试外部用户API</el-button>
    </div>
    
    <div v-if="result" style="margin-top: 20px;">
      <h4>测试结果：</h4>
      <pre>{{ JSON.stringify(result, null, 2) }}</pre>
    </div>
    
    <div v-if="logs.length > 0" style="margin-top: 20px;">
      <h4>请求日志：</h4>
      <div v-for="(log, index) in logs" :key="index" style="margin-bottom: 10px; padding: 10px; background: #f5f5f5; border-radius: 4px;">
        <strong>{{ log.time }}</strong> - {{ log.message }}
        <div v-if="log.details" style="margin-top: 5px; font-size: 12px; color: #666;">
          <pre>{{ JSON.stringify(log.details, null, 2) }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listUsers, listRoles, getUserPermsByUsername, getUserByUsername } from '@/api/platform'
import { ENV_CONFIG } from '@/config/env.config.js'

const loading = ref(false)
const result = ref(null)
const logs = ref([])
const config = ref({
  gatewayUrl: ENV_CONFIG.GATEWAY_URL,
  apiBaseUrl: ENV_CONFIG.API_BASE_URL
})

// 添加日志
const addLog = (message, details = null) => {
  logs.value.unshift({
    time: new Date().toLocaleTimeString(),
    message,
    details
  })
}

// 组件挂载时打印配置信息
onMounted(() => {
  console.log('API接口测试页面加载完成')
  addLog('页面加载完成')
})


const testUserApi = async () => {
  loading.value = true
  addLog('开始测试用户API...')
  try {
    console.log('测试用户API...')
    const response = await listUsers()
    console.log('用户API响应:', response)
    
    addLog('用户API测试成功', response)
    result.value = { 
      api: 'users', 
      success: true, 
      data: response
    }
    ElMessage.success('用户API测试成功')
  } catch (error) {
    console.error('用户API测试失败:', error)
    addLog('用户API测试失败', { message: error.message })
    result.value = { api: 'users', success: false, error: error.message }
    ElMessage.error('用户API测试失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const testRoleApi = async () => {
  loading.value = true
  addLog('开始测试角色API...')
  try {
    console.log('测试角色API...')
    const response = await listRoles()
    console.log('角色API响应:', response)
    
    addLog('角色API测试成功', response)
    result.value = { 
      api: 'roles', 
      success: true, 
      data: response
    }
    ElMessage.success('角色API测试成功')
  } catch (error) {
    console.error('角色API测试失败:', error)
    addLog('角色API测试失败', { message: error.message })
    result.value = { api: 'roles', success: false, error: error.message }
    ElMessage.error('角色API测试失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const testExternalUserAPI = async () => {
  loading.value = true
  addLog('开始测试外部用户API...')
  try {
    console.log('测试获取用户权限API...')
    const permsResponse = await getUserPermsByUsername('admin')
    addLog('用户权限API测试成功', permsResponse)
    
    console.log('测试获取用户信息API...')
    const userResponse = await getUserByUsername('admin')
    addLog('用户信息API测试成功', userResponse)
    
    result.value = { 
      api: 'external-user', 
      success: true, 
      data: {
        permissions: permsResponse,
        user: userResponse
      }
    }
    ElMessage.success('外部用户API测试成功')
  } catch (error) {
    console.error('外部用户API测试失败:', error)
    addLog('外部用户API测试失败', { message: error.message })
    result.value = { api: 'external-user', success: false, error: error.message }
    ElMessage.error('外部用户API测试失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}
</style>