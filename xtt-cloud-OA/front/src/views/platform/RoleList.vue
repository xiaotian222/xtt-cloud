<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索角色" style="width:240px" />
      <el-button type="primary" @click="openEdit()">新建角色</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="260">
        <template #default="{row}">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" @click="openGrant(row)">授予权限</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id?'编辑角色':'新建角色'" width="520">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantVisible" title="授予权限" width="560">
      <el-select v-model="grantPermIds" multiple placeholder="选择权限" style="width:100%">
        <el-option v-for="p in permOptions" :key="p.id" :label="p.name + '(' + p.code + ')'" :value="p.id" />
      </el-select>
      <template #footer>
        <el-button @click="grantVisible=false">取消</el-button>
        <el-button type="primary" @click="doGrant">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listRoles, createRole, updateRole, deleteRole, listPerms, grantRolePerms } from '@/api/platform'

const list = ref([])
const query = ref({ keyword: '' })
const visible = ref(false)
const form = ref({ id: null, code: '', name: '', description: '' })
const grantVisible = ref(false)
const grantPermIds = ref([])
const permOptions = ref([])

const openEdit = (row) => {
  form.value = row ? { ...row } : { id: null, code: '', name: '', description: '' }
  visible.value = true
}
const fetch = async () => { list.value = (await listRoles()).data || [] }
const save = async () => {
  const payload = { ...form.value }
  if (payload.id) await updateRole(payload.id, payload)
  else await createRole(payload)
  ElMessage.success('保存成功'); visible.value=false; fetch()
}
const remove = async (row) => { await deleteRole(row.id); ElMessage.success('删除成功'); fetch() }
onMounted(fetch)

const openGrant = async (row) => {
  form.value = { ...row }
  permOptions.value = (await listPerms()).data || []
  grantPermIds.value = (row.permissions || []).map(p => p.id)
  grantVisible.value = true
}

const doGrant = async () => {
  await grantRolePerms(form.value.id, grantPermIds.value)
  ElMessage.success('授权成功')
  grantVisible.value = false
  fetch()
}
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:12px; }
</style>


