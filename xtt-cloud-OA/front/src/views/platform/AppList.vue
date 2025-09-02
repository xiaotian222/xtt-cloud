<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="搜索应用" style="width:240px" />
      <el-button type="primary" @click="openEdit()">新建应用</el-button>
    </div>
    <el-table :data="list" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="enabled" label="启用" width="120">
        <template #default="{row}"><el-switch v-model="row.enabled" disabled /></template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{row}">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id?'编辑应用':'新建应用'" width="520">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const list = ref([])
const query = ref({ keyword: '' })
const visible = ref(false)
const form = ref({ id:null, code:'', name:'', enabled:true })
const openEdit=(row)=>{ form.value=row?{...row}:{ id:null, code:'', name:'', enabled:true }; visible.value=true }
const save=()=>{ ElMessage.success('已保存(演示)'); visible.value=false }
const remove=()=>{ ElMessage.success('已删除(演示)') }
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:12px; }
</style>


