<template>
  <div class="dashboard-container">
    <el-container>
      <!-- 头部 -->
      <el-header class="dashboard-header">
        <div class="header-left">
          <h2>OA系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="authStore.user.avatar">
                {{ authStore.user.username?.charAt(0)?.toUpperCase() }}
              </el-avatar>
              <span class="username">{{ authStore.user.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏 -->
        <el-aside width="200px" class="dashboard-sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="dashboard">
              <el-icon><Monitor /></el-icon>
              <span>仪表板</span>
            </el-menu-item>
            <el-menu-item index="users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="storage">
              <el-icon><Box /></el-icon>
              <span>库存管理</span>
            </el-menu-item>
            <el-menu-item index="reports">
              <el-icon><PieChart /></el-icon>
              <span>报表统计</span>
            </el-menu-item>
            <el-menu-item index="settings">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main class="dashboard-main">
          <div class="welcome-section">
            <h3>欢迎回来，{{ authStore.user.username || '用户' }}！</h3>
            <p>今天是 {{ currentDate }}</p>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-grid">
            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon users-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">1,234</div>
                  <div class="stat-label">总用户数</div>
                </div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon orders-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">567</div>
                  <div class="stat-label">今日订单</div>
                </div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon revenue-icon">
                  <el-icon><Money /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">¥89,456</div>
                  <div class="stat-label">本月收入</div>
                </div>
              </div>
            </el-card>

            <el-card class="stat-card">
              <div class="stat-content">
                <div class="stat-icon storage-icon">
                  <el-icon><Box /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-number">2,890</div>
                  <div class="stat-label">库存商品</div>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 最近活动 -->
          <el-card class="recent-activities">
            <template #header>
              <span>最近活动</span>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in recentActivities"
                :key="index"
                :timestamp="activity.time"
                :type="activity.type"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const activeMenu = ref('dashboard')

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 最近活动数据
const recentActivities = ref([
  {
    content: '用户张三登录了系统',
    time: '10分钟前',
    type: 'primary'
  },
  {
    content: '新订单 #12345 已创建',
    time: '30分钟前',
    type: 'success'
  },
  {
    content: '库存预警：商品A库存不足',
    time: '1小时前',
    type: 'warning'
  },
  {
    content: '系统维护完成',
    time: '2小时前',
    type: 'info'
  }
])

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
  // 这里可以添加路由跳转逻辑
  console.log('选择菜单:', index)
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中...')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中...')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        authStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      } catch {
        // 用户取消
      }
      break
  }
}

onMounted(() => {
  console.log('仪表板页面已加载')
})
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.dashboard-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left h2 {
  margin: 0;
  color: #409eff;
  font-size: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin: 0 8px;
  color: #333;
}

.dashboard-sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
}

.sidebar-menu {
  border-right: none;
}

.dashboard-main {
  background: #f5f7fa;
  padding: 20px;
}

.welcome-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.welcome-section h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.welcome-section p {
  margin: 0;
  color: #666;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.users-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.orders-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.revenue-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.storage-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.recent-activities {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .dashboard-sidebar {
    width: 100% !important;
  }
  
  .dashboard-header {
    padding: 0 15px;
  }
}
</style>
