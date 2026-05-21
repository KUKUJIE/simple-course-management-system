<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="8">
        <el-card class="data-card courses">
          <div class="card-content">
            <div class="card-icon">📖</div>
            <h3>已选课程</h3>
            <div class="number">{{ stats.selectedCourses }}</div>
            <div class="desc">总课程 {{ stats.totalCourses }} 门</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="data-card credits">
          <div class="card-content">
            <div class="card-icon">⭐</div>
            <h3>已修学分</h3>
            <div class="number">{{ stats.earnedCredits }}</div>
            <div class="desc">总学分 {{ stats.requiredCredits }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="data-card gpa">
          <div class="card-content">
            <div class="card-icon">📊</div>
            <h3>平均绩点</h3>
            <div class="number">{{ stats.gpa.toFixed(2) }}</div>
            <div class="desc">较上学期 {{ stats.gpaChange > 0 ? '+' : '' }}{{ stats.gpaChange.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近课程表 -->
    <el-card class="schedule-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>近期课程</span>
          <el-button text @click="$router.push('/student/my-courses')">
            查看完整课表
          </el-button>
        </div>
      </template>
      <el-table :data="recentCourses" style="width: 100%">
        <el-table-column prop="term" label="学期" width="180" />
        <el-table-column prop="name" label="课程名称" min-width="180" />
        <el-table-column prop="teacherName" label="教师" width="120" />
        <el-table-column prop="credit" label="学分" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'">
              {{ row.status === 'active' ? '进行中' : '未开课' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { enrollmentApi, sectionApi } from '@/api/new-api'
import './style.scss'

// 统计数据
const stats = reactive({
  selectedCourses: 0,
  totalCourses: 0,
  earnedCredits: 0,
  requiredCredits: 120,
  gpa: 0,
  gpaChange: 0
})

// 课程列表
const recentCourses = ref([])
const loading = ref(false)

// Canvas 动画（已废弃）

// 安全获取 studentId
const getStudentId = () => {
  const sid = localStorage.getItem('studentId')
  if (sid) return parseInt(sid, 10)
  return null
}

// 获取统计数据（使用新版 enrollments 接口）
const fetchStats = async () => {
  try {
    const studentId = getStudentId()
    if (!studentId) { console.warn('未获取到 studentId'); return }

    // 新版：获取所有教学班 + 我的选课
    const [sectionsRes, enrollRes] = await Promise.all([
      sectionApi.getSections({ page: 1, pageSize: 100 }),
      enrollmentApi.getMyEnrollments(studentId)
    ])

    // 全部教学班数量
    const allSections = sectionsRes?.data?.list || sectionsRes?.data || []
    stats.totalCourses = Array.isArray(allSections) ? allSections.length : (allSections.total || 0)

    // 已选课程（从 enrollments）
    const enrollments = enrollRes?.data || []
    const enrolledList = Array.isArray(enrollments) ? enrollments : []

    stats.selectedCourses = enrolledList.length

    // 计算已修学分（通过的课程）
    const passedEnrolls = enrolledList.filter(e => e.isPassed === 1 || e.isPassed === true)
    stats.earnedCredits = passedEnrolls.reduce((sum, e) => sum + (e.credit || 0), 0)

    // 计算 GPA（根据绩点）
    const gpaEnrolls = enrolledList.filter(e => e.gpaPoint != null && e.gpaPoint !== undefined)
    if (gpaEnrolls.length > 0) {
      const totalGpa = gpaEnrolls.reduce((sum, e) => sum + (e.gpaPoint || 0), 0)
      stats.gpa = totalGpa / gpaEnrolls.length
    } else {
      stats.gpa = 0
    }
    stats.gpaChange = 0 // 新版暂无上学期对比
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取近期课程（使用新版 enrollments 接口）
const fetchRecentCourses = async () => {
  try {
    loading.value = true
    const studentId = getStudentId()
    if (!studentId) return

    const enrollRes = await enrollmentApi.getMyEnrollments(studentId)
    const enrollments = enrollRes?.data || []
    const enrolledList = Array.isArray(enrollments) ? enrollments : []

    recentCourses.value = enrolledList.slice(0, 5).map(e => ({
      term: e.semester || '',
      name: e.courseName || '',
      teacherName: e.teacherName || '',
      credit: e.credit || 0,
      status: e.status === 1 ? 'active' : 'inactive'
    }))
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化（首次加载）
onMounted(async () => {
  await fetchStats()
  await fetchRecentCourses()
})
</script>

<style lang="scss" scoped>
// 删除所有样式代码，移至 style.scss
</style> 