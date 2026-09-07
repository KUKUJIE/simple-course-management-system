<template>
  <el-form :model="loginForm" label-width="80px">
    <el-form-item label="账号">
      <el-input v-model="loginForm.username" />
    </el-form-item>
    <el-form-item label="密码">
      <el-input type="password" v-model="loginForm.password" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="doLogin">登录</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'
import axios from '@/api/axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const loginForm = ref({ username: '', password: '' })

async function doLogin() {
  try {
    const res = await axios.post('/auth/login', loginForm.value)
    const data = res.data.data
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('uid', data.userId)
    // store entityId too e.g., studentId/teacherId
    if (data.entityId) localStorage.setItem('entityId', data.entityId)
    // redirect based on role
    const routes = { student: '/student/dashboard', teacher: '/teacher/dashboard', admin: '/admin/dashboard' }
    window.location.href = routes[data.role]
  } catch (e) {
    console.error(e)
    this.$message.error('登录失败')
  }
}
</script>
