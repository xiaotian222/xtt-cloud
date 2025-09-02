<template>
  <div>
    <div class="toolbar">
      <el-date-picker v-model="range" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
      <el-input v-model="query.keyword" placeholder="用户/动作/资源" style="width:260px" />
      <el-button type="primary">查询</el-button>
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
import { ref } from 'vue'
const list = ref([])
const query = ref({ keyword:'' })
const range = ref([])
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:12px; align-items:center; }
</style>


