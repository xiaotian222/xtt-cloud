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
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
const tree = ref([])
const visible = ref(false)
const form = ref({ id:null, name:'', sortNo:0 })
const openEdit=(row)=>{ form.value=row?{...row}:{ id:null, name:'', sortNo:0 }; visible.value=true }
const save=()=>{ ElMessage.success('已保存(演示)'); visible.value=false }
const remove=()=>{ ElMessage.success('已删除(演示)') }
</script>

<style scoped>
.dept-wrap .ops { margin-left: 8px; }
.toolbar { margin-bottom: 12px; }
</style>


