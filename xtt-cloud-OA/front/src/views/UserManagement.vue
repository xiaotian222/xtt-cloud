<template>
  <div class="user-management">
    <el-card class="user-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="showAddUserDialog = true">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </div>
      </template>

      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ getRoleName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加用户对话框 -->
    <el-dialog v-model="showAddUserDialog" title="添加用户" width="500px">
      <el-form :model="addUserForm" :rules="addUserRules" ref="addUserFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addUserForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="addUserForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="addUserForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="用户" value="ROLE_USER" />
            <el-option label="经理" value="ROLE_MANAGER" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addUserForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddUserDialog = false">取消</el-button>
          <el-button type="primary" @click="handleAddUser" :loading="addLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAllUsers, addUser } from '@/api/auth'

const loading = ref(false)
const addLoading = ref(false)
const showAddUserDialog = ref(false)
const addUserFormRef = ref()
const users = ref([])

// 添加用户表单
const addUserForm = reactive({
  username: '',
  password: '',
  role: '',
  email: ''
})

// 表单验证规则
const addUserRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    const response = await getAllUsers()
    if (response.code === 200 || response.success || response.status === 200) {
      const userData = response.data || response
      users.value = Object.values(userData)
    }
  } catch (error) {
    console.error('获取用户列表错误:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 添加用户
const handleAddUser = async () => {
  try {
    await addUserFormRef.value.validate()
    addLoading.value = true
    
    const response = await addUser(addUserForm)
    if (response.code === 200 || response.success || response.status === 200) {
      ElMessage.success('添加用户成功')
      showAddUserDialog.value = false
      resetAddUserForm()
      fetchUsers()
    }
  } catch (error) {
    console.error('添加用户错误:', error)
    ElMessage.error('添加用户失败')
  } finally {
    addLoading.value = false
  }
}

// 重置添加用户表单
const resetAddUserForm = () => {
  addUserForm.username = ''
  addUserForm.password = ''
  addUserForm.role = ''
  addUserForm.email = ''
  addUserFormRef.value?.resetFields()
}

// 编辑用户
const editUser = (user) => {
  ElMessage.info('编辑功能开发中...')
}

// 删除用户
const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.info('删除功能开发中...')
  } catch {
    // 用户取消删除
  }
}

// 获取角色类型
const getRoleType = (role) => {
  switch (role) {
    case 'ROLE_ADMIN':
      return 'danger'
    case 'ROLE_MANAGER':
      return 'warning'
    case 'ROLE_USER':
      return 'info'
    default:
      return 'info'
  }
}

// 获取角色名称
const getRoleName = (role) => {
  switch (role) {
    case 'ROLE_ADMIN':
      return '管理员'
    case 'ROLE_MANAGER':
      return '经理'
    case 'ROLE_USER':
      return '用户'
    default:
      return role
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.user-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
