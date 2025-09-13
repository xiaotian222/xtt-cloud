<template>
  <div class="dept-wrap">
    <div class="toolbar">
      <el-button type="primary" @click="openEdit()">新建部门</el-button>
    </div>
    <el-tree :data="tree" node-key="id" :props="{ label:'name', children:'children' }" default-expand-all>
      <template #default="{ node, data }">
        <span>{{ data.name }}</span>
        <span class="ops">
          <el-button link type="primary" @click.stop="openEdit(data)">编辑</el-button>
          <el-button link type="danger" @click.stop="remove(data)">删除</el-button>
        </span>
      </template>
    </el-tree>

    <el-dialog v-model="visible" :title="form.id?'编辑部门':'新建部门'" width="420">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="0" /></el-form-item>
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
import { listDepts, createDept, updateDept, deleteDept } from '@/api/platform'

const tree = ref([])
const visible = ref(false)
const form = ref({ id: null, name: '', sortNo: 0, parentId: null })
const loading = ref(false)

const openEdit = (row) => {
  form.value = row ? { ...row } : { id: null, name: '', sortNo: 0, parentId: null }
  visible.value = true
}

const fetch = async () => {
  try {
    loading.value = true
    console.log('获取部门列表...')
    const response = await listDepts()
    console.log('部门列表响应:', response)
    tree.value = Array.isArray(response) ? response : (response.data || [])
    console.log('部门树数据:', tree.value)
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

const save = async () => {
  try {
    const payload = { ...form.value }
    if (payload.id) {
      await updateDept(payload.id, payload)
    } else {
      await createDept(payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    fetch()
  } catch (error) {
    console.error('保存部门失败:', error)
    ElMessage.error('保存部门失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该部门吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDept(row.id)
    ElMessage.success('删除成功')
    fetch()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
      ElMessage.error('删除部门失败')
    }
  }
}

onMounted(() => {
  console.log('🚀 DeptTree组件已挂载，开始获取数据...')
  console.log('📊 当前部门树:', tree.value)
  console.log('🔧 组件状态:', { loading: loading.value, tree: tree.value })
  fetch()
})
</script>

<style scoped>
.dept-wrap .ops { margin-left: 8px; }
.toolbar { margin-bottom: 12px; }
</style>


