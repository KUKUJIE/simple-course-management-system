<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="sidebar-logo">
        <AppLogo small />
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        background-color="transparent"
        text-color="var(--el-text-color-primary)"
        active-text-color="var(--el-color-primary)"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/admin/students">
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/teachers">
          <el-icon><UserFilled /></el-icon>
          <span>教师管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/courses">
          <el-icon><Collection /></el-icon>
          <span>课程管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/sections">
          <el-icon><Tickets /></el-icon>
          <span>教学班管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/departments">
          <el-icon><OfficeBuilding /></el-icon>
          <span>院系管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/majors">
          <el-icon><Notebook /></el-icon>
          <span>专业管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/classrooms">
          <el-icon><School /></el-icon>
          <span>教室管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>{{ currentRoute.meta.title }}</h2>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="32" icon="UserFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main>
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, User, UserFilled, Collection, Tickets, OfficeBuilding, Notebook, School } from '@element-plus/icons-vue'
import AppLogo from '@/components/AppLogo.vue'

const route = useRoute()
const router = useRouter()

const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('uid')
    localStorage.removeItem('role')
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  gap: 8px;
  padding: 8px 8px 0 0;
  
  .sidebar-logo {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 18px 0 10px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }
  
  .sidebar-menu {
    height: calc(100% - 90px);
    border-right: 1px solid rgba(255, 255, 255, 0.1);
  }
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    width: 100%;
    padding: 0 20px;
    
    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
      background: linear-gradient(45deg, #409EFF, #67C23A);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
}
</style> 