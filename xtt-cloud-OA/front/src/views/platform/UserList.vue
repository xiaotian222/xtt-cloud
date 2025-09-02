<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索用户名/昵称" style="width:240px" />
      <el-button type="primary" @click="openEdit()">新建用户</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'启用':'禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{row}">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" @click="openGrant(row)">授予角色</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id?'编辑用户':'新建用户'" width="520">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码" v-if="!form.id"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantVisible" title="授予角色" width="520">
      <el-select v-model="grantRoleIds" multiple placeholder="选择角色" style="width:100%">
        <el-option v-for="r in roleOptions" :key="r.id" :label="r.name" :value="r.id" />
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
import { listUsers, createUser, updateUser, deleteUser, listRoles, grantUserRoles } from '@/api/platform'

const list = ref([])
const query = ref({ keyword: '' })
const visible = ref(false)
const form = ref({ id: null, username: '', password: '', nickname: '', email: '', status: 1 })
const grantVisible = ref(false)
const grantRoleIds = ref([])
const roleOptions = ref([])

const openEdit = (row) => {
  form.value = row ? { ...row } : { id: null, username: '', password: '', nickname: '', email: '', status: 1 }
  visible.value = true
}

const fetch = async () => { list.value = (await listUsers()).data || [] }

const save = async () => {
  const payload = { ...form.value }
  if (payload.id) {
    await updateUser(payload.id, payload)
  } else {
    await createUser(payload)
  }
  ElMessage.success('保存成功')
  visible.value = false
  fetch()
}

const remove = async (row) => {
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetch()
}

onMounted(fetch)

const openGrant = async (row) => {
  form.value = { ...row }
  roleOptions.value = (await listRoles()).data || []
  grantRoleIds.value = (row.roles || []).map(r => r.id)
  grantVisible.value = true
}

const doGrant = async () => {
  await grantUserRoles(form.value.id, grantRoleIds.value)
  ElMessage.success('授权成功')
  grantVisible.value = false
  fetch()
}
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:12px; }
</style>


