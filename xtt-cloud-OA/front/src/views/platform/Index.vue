<template>
  <div class="platform-page">
    <el-page-header content="平台管理">
      <template #extra>
        <el-button type="primary" @click="active='user'">用户</el-button>
        <el-button @click="active='role'">角色</el-button>
        <el-button @click="active='perm'">权限</el-button>
        <el-button @click="active='dept'">部门</el-button>
        <el-button @click="active='app'">应用</el-button>
        <el-button @click="active='menu'">菜单</el-button>
        <el-button @click="active='log'">日志</el-button>
      </template>
    </el-page-header>

    <div class="content">
      <component :is="tabComp" />
    </div>
  </div>
  
</template>

<script setup>
import { computed, ref } from 'vue'

const active = ref('user')

const tabComp = computed(() => ({
  user: () => import('./UserList.vue'),
  role: () => import('./RoleList.vue'),
  perm: () => import('./PermList.vue'),
  dept: () => import('./DeptTree.vue'),
  app: () => import('./AppList.vue'),
  menu: () => import('./MenuList.vue'),
  log: () => import('./AuditList.vue')
}[active.value]))
</script>

<style scoped>
.platform-page { padding: 16px; }
.content { margin-top: 16px; background: #fff; padding: 16px; border: 1px solid #eef0f3; border-radius: 8px; }
</style>


