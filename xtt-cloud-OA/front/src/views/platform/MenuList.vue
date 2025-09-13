<template>
  <div>
    <div class="toolbar">
      <el-button type="primary" @click="openEdit()">新建菜单</el-button>
    </div>
    <el-table :data="list" row-key="id" border default-expand-all>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="permission" label="权限标识" />
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id?'编辑菜单':'新建菜单'" width="560">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" style="width:200px"><el-option label="catalog" value="catalog" /><el-option label="menu" value="menu" /><el-option label="button" value="button" /></el-select></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.permission" /></el-form-item>
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
import { listMenus, createMenu, updateMenu, deleteMenu } from '@/api/platform'

const list = ref([])
const visible = ref(false)
const form = ref({ id: null, name: '', path: '', type: 'menu', permission: '', parentId: null, sortNo: 0 })
const loading = ref(false)

const openEdit = (row) => {
  form.value = row ? { ...row } : { id: null, name: '', path: '', type: 'menu', permission: '', parentId: null, sortNo: 0 }
  visible.value = true
}

const fetch = async () => {
  try {
    loading.value = true
    console.log('获取菜单列表...')
    const response = await listMenus()
    console.log('菜单列表响应:', response)
    list.value = Array.isArray(response) ? response : (response.data || [])
    console.log('菜单列表数据:', list.value)
  } catch (error) {
    console.error('获取菜单列表失败:', error)
    ElMessage.error('获取菜单列表失败')
  } finally {
    loading.value = false
  }
}

const save = async () => {
  try {
    const payload = { ...form.value }
    if (payload.id) {
      await updateMenu(payload.id, payload)
    } else {
      await createMenu(payload)
    }
    ElMessage.success('保存成功')
    visible.value = false
    fetch()
  } catch (error) {
    console.error('保存菜单失败:', error)
    ElMessage.error('保存菜单失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetch()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
    }
  }
}

onMounted(() => {
  console.log('🚀 MenuList组件已挂载，开始获取数据...')
  console.log('📊 当前菜单列表:', list.value)
  console.log('🔧 组件状态:', { loading: loading.value, list: list.value })
  fetch()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>


