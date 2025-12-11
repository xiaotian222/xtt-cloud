<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalDocuments }}</div>
              <div class="stat-label">文档总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingApproval }}</div>
              <div class="stat-label">待审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inProgress }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近文档</span>
              <el-button type="primary" text @click="$router.push('/documents')">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentDocuments" style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>待办事项</span>
          </template>
          <el-empty v-if="todos.length === 0" description="暂无待办事项" />
          <ul v-else class="todo-list">
            <li v-for="todo in todos" :key="todo.id" class="todo-item">
              <el-icon><CircleCheckFilled /></el-icon>
              <span>{{ todo.title }}</span>
            </li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document, CircleCheck, Clock, Finished, CircleCheckFilled } from '@element-plus/icons-vue'

const stats = ref({
  totalDocuments: 0,
  pendingApproval: 0,
  inProgress: 0,
  completed: 0
})

const recentDocuments = ref([])
const todos = ref([])

const getStatusType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '草稿',
    1: '审批中',
    2: '已完成',
    3: '已拒绝'
  }
  return statusMap[status] || '未知'
}

onMounted(() => {
  // 模拟数据，实际应该从 API 获取
  stats.value = {
    totalDocuments: 128,
    pendingApproval: 12,
    inProgress: 8,
    completed: 108
  }
  
  recentDocuments.value = [
    { id: 1, title: '关于2024年度工作计划的通知', status: 1, createTime: '2024-01-15 10:30' },
    { id: 2, title: '部门会议纪要', status: 2, createTime: '2024-01-14 15:20' },
    { id: 3, title: '项目进度报告', status: 0, createTime: '2024-01-13 09:15' }
  ]
  
  todos.value = [
    { id: 1, title: '审批《关于2024年度工作计划的通知》' },
    { id: 2, title: '审核《部门会议纪要》' }
  ]
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 0;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-item .el-icon {
  color: #409EFF;
}
</style>

