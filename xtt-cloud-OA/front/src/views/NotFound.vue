<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>页面未找到</h1>
      <p>抱歉，您访问的页面不存在或已被删除。</p>
      <div class="actions">
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.not-found-content {
  text-align: center;
  background: white;
  padding: 60px 40px;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
  margin-bottom: 20px;
}

.not-found-content h1 {
  color: #333;
  margin-bottom: 20px;
  font-size: 28px;
}

.not-found-content p {
  color: #666;
  margin-bottom: 30px;
  font-size: 16px;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.actions .el-button {
  min-width: 120px;
}

@media (max-width: 480px) {
  .not-found-content {
    padding: 40px 20px;
  }
  
  .error-code {
    font-size: 80px;
  }
  
  .not-found-content h1 {
    font-size: 24px;
  }
  
  .actions {
    flex-direction: column;
    align-items: center;
  }
  
  .actions .el-button {
    width: 100%;
    max-width: 200px;
  }
}
</style>
