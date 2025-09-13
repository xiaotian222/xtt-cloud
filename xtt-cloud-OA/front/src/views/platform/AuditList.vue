<template>
  <div>
    <div class="toolbar">
      <el-date-picker v-model="range" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
      <el-input v-model="query.keyword" placeholder="用户/动作/资源" style="width:260px" />
      <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="action" label="动作" />
      <el-table-column prop="resource" label="资源" />
      <el-table-column prop="method" label="方法" width="100" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="result" label="结果" width="100" />
      <el-table-column prop="message" label="说明" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAuditLogs } from '@/api/platform'

const list = ref([])
const query = ref({ keyword: '' })
const range = ref([])
const loading = ref(false)

const fetch = async () => {
  try {
    loading.value = true
    console.log('获取审计日志...')
    const params = {
      keyword: query.value.keyword,
      startTime: range.value?.[0] ? range.value[0].toISOString() : null,
      endTime: range.value?.[1] ? range.value[1].toISOString() : null
    }
    console.log('查询参数:', params)
    const response = await listAuditLogs(params)
    console.log('审计日志响应:', response)
    list.value = Array.isArray(response) ? response : (response.data || [])
    console.log('审计日志数据:', list.value)
  } catch (error) {
    console.error('获取审计日志失败:', error)
    ElMessage.error('获取审计日志失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetch()
}

onMounted(() => {
  console.log('🚀 AuditList组件已挂载，开始获取数据...')
  console.log('📊 当前审计日志列表:', list.value)
  console.log('🔧 组件状态:', { loading: loading.value, list: list.value })
  fetch()
})
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:12px; align-items:center; }
</style>


