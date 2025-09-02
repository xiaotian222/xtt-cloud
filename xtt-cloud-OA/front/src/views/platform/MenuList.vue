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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const list = ref([])
const visible = ref(false)
const form = ref({ id:null, name:'', path:'', type:'menu', permission:'' })
const openEdit=(row)=>{ form.value=row?{...row}:{ id:null, name:'', path:'', type:'menu', permission:'' }; visible.value=true }
const save=()=>{ ElMessage.success('已保存(演示)'); visible.value=false }
const remove=()=>{ ElMessage.success('已删除(演示)') }
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>


