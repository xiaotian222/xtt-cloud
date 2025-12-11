<template>
  <div class="document-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>文档列表</span>
          <el-button type="primary" @click="handleCreate">新建文档</el-button>
        </div>
      </template>
      
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文档标题"
          style="width: 300px;"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      
      <el-table
        :data="documentList"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleView(row)">查看</el-button>
            <el-button type="warning" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getDocumentList, deleteDocument } from '@/api/document'

const loading = ref(false)
const searchKeyword = ref('')
const documentList = ref([])
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

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

const fetchDocumentList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      keyword: searchKeyword.value
    }
    const response = await getDocumentList(params)
    if (response.code === 200) {
      documentList.value = response.data?.records || response.data?.list || []
      pagination.value.total = response.data?.total || 0
    }
  } catch (error) {
    console.error('获取文档列表失败:', error)
    // 模拟数据
    documentList.value = [
      { id: 1, title: '关于2024年度工作计划的通知', type: '通知', status: 1, createTime: '2024-01-15 10:30' },
      { id: 2, title: '部门会议纪要', type: '纪要', status: 2, createTime: '2024-01-14 15:20' },
      { id: 3, title: '项目进度报告', type: '报告', status: 0, createTime: '2024-01-13 09:15' }
    ]
    pagination.value.total = 3
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.page = 1
  fetchDocumentList()
}

const handleReset = () => {
  searchKeyword.value = ''
  pagination.value.page = 1
  fetchDocumentList()
}

const handleCreate = () => {
  ElMessage.info('新建文档功能开发中...')
}

const handleView = (row) => {
  ElMessage.info(`查看文档: ${row.title}`)
}

const handleEdit = (row) => {
  ElMessage.info(`编辑文档: ${row.title}`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除文档"${row.title}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    try {
      await deleteDocument(row.id)
      ElMessage.success('删除成功')
      fetchDocumentList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  } catch {}
}

const handleSizeChange = () => {
  fetchDocumentList()
}

const handlePageChange = () => {
  fetchDocumentList()
}

onMounted(() => {
  fetchDocumentList()
})
</script>

<style scoped>
.document-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

