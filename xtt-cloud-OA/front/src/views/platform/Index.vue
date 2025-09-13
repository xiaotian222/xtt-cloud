<template>
  <div class="platform-page">
    <div class="platform-layout">
      <!-- 左侧菜单 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <h3>平台管理</h3>
        </div>
        <el-menu 
          :default-active="active" 
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="user">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="role">
            <el-icon><UserFilled /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="perm">
            <el-icon><Key /></el-icon>
            <span>权限管理</span>
          </el-menu-item>
          <el-menu-item index="dept">
            <el-icon><OfficeBuilding /></el-icon>
            <span>部门管理</span>
          </el-menu-item>
          <el-menu-item index="app">
            <el-icon><Grid /></el-icon>
            <span>应用管理</span>
          </el-menu-item>
          <el-menu-item index="menu">
            <el-icon><Menu /></el-icon>
            <span>菜单管理</span>
          </el-menu-item>
          <el-menu-item index="log">
            <el-icon><Document /></el-icon>
            <span>审计日志</span>
          </el-menu-item>
          <el-menu-item index="test">
            <el-icon><Tools /></el-icon>
            <span>API测试</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 右侧内容区 -->
      <div class="main-content">
        <div class="content-header">
          <div class="header-left">
            <el-button 
              type="primary" 
              :icon="ArrowLeft" 
              @click="goHome"
              class="back-home-btn"
            >
              返回主页
            </el-button>
          </div>
          <div class="header-center">
            <h2>{{ getPageTitle() }}</h2>
          </div>
          <div class="header-right">
            <!-- 预留右侧按钮位置 -->
          </div>
        </div>
        <div class="content-body">
          <component :is="tabComp" :key="active" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { 
  User, 
  UserFilled, 
  Key, 
  OfficeBuilding, 
  Grid, 
  Menu, 
  Document, 
  Tools,
  ArrowLeft
} from '@element-plus/icons-vue'
import TestApi from './TestApi.vue'
import UserList from './UserList.vue'
import RoleList from './RoleList.vue'
import PermList from './PermList.vue'
import DeptTree from './DeptTree.vue'
import AppList from './AppList.vue'
import MenuList from './MenuList.vue'
import AuditList from './AuditList.vue'

const active = ref('user')

// 菜单选择处理
const handleMenuSelect = (index) => {
  console.log('🔄 选择菜单项:', index)
  active.value = index
}

// 返回主页
const goHome = () => {
  console.log('🏠 返回主页')
  // 使用路由跳转到主页
  window.location.href = '/'
  // 或者使用 Vue Router（如果配置了的话）
  // router.push('/')
}

// 获取页面标题
const getPageTitle = () => {
  const titles = {
    user: '用户管理',
    role: '角色管理', 
    perm: '权限管理',
    dept: '部门管理',
    app: '应用管理',
    menu: '菜单管理',
    log: '审计日志',
    test: 'API测试'
  }
  return titles[active.value] || '平台管理'
}

const tabComp = computed(() => {
  console.log('🔄 切换标签页:', active.value)
  const components = {
    user: UserList,    // 直接返回组件
    role: RoleList,    // 直接返回组件
    perm: PermList,    // 直接返回组件
    dept: DeptTree,    // 直接返回组件
    app: AppList,      // 直接返回组件
    menu: MenuList,    // 直接返回组件
    log: AuditList,    // 直接返回组件
    test: TestApi      // 直接返回组件
  }
  const component = components[active.value]
  console.log('📦 返回的组件:', component)
  console.log('📦 组件类型:', typeof component)
  console.log('📦 是否为函数:', typeof component === 'function')
  return component
})
</script>

<style scoped>
.platform-page {
  height: 100vh;
  background: #f5f5f5;
  margin: 0;
  padding: 0;
}

.platform-layout {
  display: flex;
  height: 100%;
}

/* 左侧菜单 */
.sidebar {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.sidebar-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  flex: 1;
  border: none;
  background: #fff;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  margin: 0;
  border-radius: 0;
  color: #606266;
  transition: all 0.3s;
}

.sidebar-menu .el-menu-item:hover {
  background: #f0f9ff;
  color: #409eff;
}

.sidebar-menu .el-menu-item.is-active {
  background: #e6f7ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.sidebar-menu .el-menu-item .el-icon {
  margin-right: 8px;
  font-size: 16px;
}

/* 右侧内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.content-header {
  background: #fff;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  flex: 0 0 auto;
}

.header-center {
  flex: 1;
  text-align: center;
}

.header-right {
  flex: 0 0 auto;
}

.content-header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.back-home-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  transform: translateX(-2px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.content-body {
  flex: 1;
  padding: 16px;
  overflow: auto;
}

.content-body > * {
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  padding: 16px;
}
</style>


