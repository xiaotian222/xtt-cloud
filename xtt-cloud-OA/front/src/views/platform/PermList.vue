<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索权限编码/名称" style="width:260px" />
      <el-button type="primary" @click="openEdit()">新建权限</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id?'编辑权限':'新建权限'" width="520">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" style="width:200px"><el-option label="api" value="api" /><el-option label="menu" value="menu" /><el-option label="btn" value="btn" /></el-select></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPerms, createPerm, updatePerm, deletePerm } from '@/api/platform'
const list = ref([])
const query = ref({ keyword: '' })
const visible = ref(false)
const form = ref({ id: null, code: '', name: '', type: 'api' })
const loading = ref(false)
const openEdit = (row) => {
  form.value = row ? { ...row } : { id: null, code: '', name: '', type: 'api' }
  visible.value = true
}

const fetch = async () => {
  try {
    loading.value = true
    console.log('获取权限列表...')
    const response = await listPerms()
    console.log('权限列表响应:', response)
    list.value = Array.isArray(response) ? response : (response.data || [])
    console.log('权限列表数据:', list.value)
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败')
  } finally {
    loading.value = false
  }
}

const save = async () => {
  try {
    const payload = { ...form.value }
    if (payload.id) {
      await updatePerm(payload.id, payload)
    } else {
      await createPerm(payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    fetch()
  } catch (error) {
    console.error('保存权限失败:', error)
    ElMessage.error('保存权限失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该权限吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePerm(row.id)
    ElMessage.success('删除成功')
    fetch()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
      ElMessage.error('删除权限失败')
    }
  }
}
onMounted(() => {
  console.log('🚀 PermList组件已挂载，开始获取数据...')
  console.log('📊 当前权限列表:', list.value)
  console.log('🔧 组件状态:', { loading: loading.value, list: list.value })
  fetch()
})
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:12px; }
</style>


