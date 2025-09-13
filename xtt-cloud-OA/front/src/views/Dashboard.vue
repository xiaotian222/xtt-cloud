<template>
  <div class="oa-home-container">
    <div class="oa-topbar">
      <div class="oa-title">OA系统</div>
      <div class="oa-user" @click="toggleUserMenu">
        <el-avatar :size="36">
          {{ authStore.user.username?.charAt(0)?.toUpperCase() || 'U' }}
        </el-avatar>
      </div>
    </div>
    <div class="oa-body">
      <aside class="oa-sider">
        <ul class="oa-menu">
          <li class="oa-menu-item active">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </li>
          <li class="oa-menu-item">
            <el-icon><Document /></el-icon>
            <span>公文管理</span>
          </li>
          <li class="oa-menu-item">
            <el-icon><Calendar /></el-icon>
            <span>会议管理</span>
          </li>
          <li class="oa-menu-item">
            <el-icon><CircleCheck /></el-icon>
            <span>流程审批</span>
          </li>
          <li class="oa-menu-item">
            <el-icon><Bell /></el-icon>
            <span>公告</span>
          </li>
          <li class="oa-menu-item">
            <el-icon><Notebook /></el-icon>
            <span>日程</span>
          </li>
          <li class="oa-menu-item">
            <el-icon><Collection /></el-icon>
            <span>通讯录</span>
          </li>
          <li class="oa-menu-item" @click="goToPlatform">
            <el-icon><Platform /></el-icon>
            <span>支撑平台</span>
          </li>
          <li class="oa-menu-item">
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </li>
        </ul>
      </aside>
      <main class="oa-content">
        <div class="card-grid">
          <div class="feature-card" @click="go('document')">
            <div class="feature-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="feature-title">公文管理</div>
          </div>
          <div class="feature-card" @click="go('meeting')">
            <div class="feature-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="feature-title">会议管理</div>
          </div>
          <div class="feature-card" @click="go('flow')">
            <div class="feature-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="feature-title">流程审批</div>
          </div>
          <div class="feature-card" @click="go('notice')">
            <div class="feature-icon">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="feature-title">公告</div>
          </div>
          <div class="feature-card" @click="go('schedule')">
            <div class="feature-icon">
              <el-icon><Notebook /></el-icon>
            </div>
            <div class="feature-title">日程</div>
          </div>
          <div class="feature-card" @click="go('contacts')">
            <div class="feature-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="feature-title">通讯录</div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Platform } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const go = (name) => {
  ElMessage.info('功能开发中...')
}

const goToPlatform = () => {
  router.push('/platform')
}

const toggleUserMenu = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning'
    })
    authStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch (_) {}
}
</script>

<style scoped>
.oa-home-container { height: 100vh; display: flex; flex-direction: column; background: #f6f8fb; }
.oa-topbar { height: 64px; background: linear-gradient(180deg,#4f8df3 0%, #4f8df3 60%, #4f8df3 100%); display: flex; align-items: center; justify-content: space-between; padding: 0 24px; color: #fff; }
.oa-title { font-size: 28px; font-weight: 700; letter-spacing: 1px; }
.oa-user { cursor: pointer; }
.oa-body { flex: 1; display: flex; }
.oa-sider { width: 240px; background: #fff; border-right: 1px solid #eef0f3; padding-top: 16px; }
.oa-menu { list-style: none; margin: 0; padding: 0; }
.oa-menu-item { display: flex; align-items: center; height: 48px; padding: 0 16px; color: #2f3b52; cursor: pointer; border-radius: 8px; margin: 6px 12px; transition: background-color .2s; }
.oa-menu-item .el-icon { color: #4f8df3; margin-right: 12px; }
.oa-menu-item:hover, .oa-menu-item.active { background: #f4f7ff; }
.oa-content { flex: 1; padding: 24px; }
.card-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; max-width: 980px; }
.feature-card { background: #fff; border: 1px solid #eef0f3; border-radius: 12px; height: 180px; display: flex; flex-direction: column; align-items: center; justify-content: center; box-shadow: 0 2px 6px rgba(0,0,0,.04); transition: transform .15s ease, box-shadow .15s ease; cursor: pointer; }
.feature-card:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(79,141,243,.15); }
.feature-icon { width: 64px; height: 64px; border-radius: 14px; background: #f4f7ff; color: #4f8df3; display: flex; align-items: center; justify-content: center; font-size: 30px; margin-bottom: 14px; }
.feature-title { color: #2f3b52; font-size: 18px; font-weight: 600; }

@media (max-width: 1024px) { .card-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .oa-sider { display: none; } .card-grid { grid-template-columns: 1fr; } }
</style>
