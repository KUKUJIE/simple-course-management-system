<template>
  <el-card>
    <div slot="header">用户管理</div>
    <div style="margin-bottom:12px">
      <el-form :model="newUser" inline>
        <el-form-item>
          <el-input v-model="newUser.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="newUser.password" placeholder="密码" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="newUser.role" placeholder="角色" style="width:140px">
            <el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="createUser">创建用户</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="users" style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="primary" size="mini" @click="editUser(row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="deleteUser(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const newUser = ref({ username: '', password: 'pass123', role: 'student' })

async function load() {
  const res = await axios.get('/admin/users')
  users.value = res.data.data
}

onMounted(load)

async function createUser() {
  try {
    await axios.post('/admin/users', newUser.value)
    ElMessage.success('创建成功')
    newUser.value.username = ''
    load()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

function editUser(row) {
  const password = prompt('新密码(留空表示不修改)', '')
  const role = prompt('角色(student/teacher/admin)', row.role)
  if (password === null) return
  axios.put(`/admin/users/${row.id}`, { password: password || undefined, role }).then(() => { ElMessage.success('更新成功'); load() }).catch(() => ElMessage.error('更新失败'))
}

function deleteUser(row) {
  ElMessageBox.confirm('确认删除此用户？', '删除确认', { type: 'warning' }).then(() => {
    axios.delete(`/admin/users/${row.id}`).then(() => { ElMessage.success('删除成功'); load() }).catch(() => ElMessage.error('删除失败'))
  }).catch(()=>{})
}
</script>
