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
import { ElMessage } from 'element-plus'
import { listPerms, createPerm, updatePerm, deletePerm } from '@/api/platform'
const list = ref([])
const query = ref({ keyword: '' })
const visible = ref(false)
const form = ref({ id: null, code: '', name: '', type: 'api' })
const openEdit = (row)=>{ form.value = row? { ...row } : { id:null, code:'', name:'', type:'api' }; visible.value = true }
const fetch = async () => { list.value = (await listPerms()).data || [] }
const save = async ()=>{ const p={...form.value}; if(p.id) await updatePerm(p.id,p); else await createPerm(p); ElMessage.success('保存成功'); visible.value=false; fetch() }
const remove = async (row)=>{ await deletePerm(row.id); ElMessage.success('删除成功'); fetch() }
onMounted(fetch)
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:12px; }
</style>


