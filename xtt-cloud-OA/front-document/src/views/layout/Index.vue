<template>
  <div class="layout-container">
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <h2 class="title">文档管理系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="36" :src="userInfo?.avatar">
                {{ userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="240px" class="aside">
          <el-menu
            :default-active="activeMenu"
            router
            class="menu"
            background-color="#fff"
            text-color="#333"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>仪表板</span>
            </el-menu-item>
            <el-menu-item index="/documents">
              <el-icon><Document /></el-icon>
              <span>文档管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { House, Document, ArrowDown } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)
const userInfo = computed(() => authStore.userInfo)

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '退出登录',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await authStore.logoutUser()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {}
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中...')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.username {
  font-size: 14px;
}

.aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
}

.menu {
  border-right: none;
  height: 100%;
}

.main {
  background: #f5f7fa;
  padding: 20px;
  overflow-y: auto;
}
</style>

