<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="6">
        <el-card class="data-card students">
          <div class="card-content">
            <div class="card-icon">👥</div>
            <h3>学生总数</h3>
            <div class="number">{{ stats.totalStudents }}</div>
            <div class="desc">本学期新增 {{ stats.newStudents }} 人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card teachers">
          <div class="card-content">
            <div class="card-icon">🧑‍🏫</div>
            <h3>教师总数</h3>
            <div class="number">{{ stats.totalTeachers }}</div>
            <div class="desc">本学期新增 {{ stats.newTeachers }} 人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card courses">
          <div class="card-content">
            <div class="card-icon">📚</div>
            <h3>课程总数</h3>
            <div class="number">{{ stats.totalCourses }}</div>
            <div class="desc">本学期开课 {{ stats.activeCourses }} 门</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="data-card score">
          <div class="card-content">
            <div class="card-icon">📊</div>
            <h3>平均成绩</h3>
            <div class="number">{{ stats.averageScore.toFixed(1) }}</div>
            <div class="desc">及格率 {{ stats.passRate.toFixed(1) }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据预警面板 -->
    <el-card class="alert-panel">
      <template #header>
        <div class="card-header">
          <span>📊 数据预警</span>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="8" v-for="alert in alerts" :key="alert.title">
          <el-card class="alert-card" :class="alert.level" @click="handleAlertClick(alert.link)">
            <div class="alert-icon">{{ alert.icon }}</div>
            <div class="alert-title">{{ alert.title }}</div>
            <div class="alert-value">{{ alert.value }}</div>
            <div class="alert-desc">{{ alert.desc }}</div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { adminNewApi } from '@/api/new-api'
import './style.scss'

const router = useRouter()

// 统计数据
const stats = reactive({
  totalStudents: 0,
  newStudents: 0,
  totalTeachers: 0,
  newTeachers: 0,
  totalCourses: 0,
  activeCourses: 0,
  averageScore: 0,
  passRate: 0
})

// Canvas引用（已废弃）

// 最近活动（已废弃，替换为数据预警）
const sectionsData = ref([])
const studentsData = ref([])
const teachersData = ref([])
const coursesData = ref([])

// 数据预警（纯前端计算，无需后端接口）
const alerts = computed(() => {
  const sections = sectionsData.value
  const students = studentsData.value
  const teachers = teachersData.value
  const courses = coursesData.value

  const disabledStudents = students.filter(s => s.status === 0).length
  const disabledTeachers = teachers.filter(t => t.status === 0).length
  const disabledCourses = courses.filter(c => c.status === 0).length
  const fullSections = sections.filter(s => s.capacityLimit > 0 && (s.selectedCount || 0) >= s.capacityLimit).length
  const emptySections = sections.filter(s => (s.selectedCount || 0) === 0).length
  const totalSections = sections.length

  return [
    {
      icon: '👥',
      title: '停用账号',
      value: `${disabledStudents + disabledTeachers} 个`,
      desc: `学生 ${disabledStudents} | 教师 ${disabledTeachers}`,
      level: disabledStudents + disabledTeachers > 0 ? 'warn' : 'safe',
      link: disabledStudents > 0 ? '/admin/students' : '/admin/teachers'
    },
    {
      icon: '📚',
      title: '课程状态',
      value: `${disabledCourses} 门停开`,
      desc: `共 ${courses.length} 门课程`,
      level: disabledCourses > 0 ? 'warn' : 'safe',
      link: '/admin/courses'
    },
    {
      icon: '🏫',
      title: '教学班选课',
      value: `${fullSections} 个班满`,
      desc: `${emptySections} 个空班 | 共 ${totalSections} 个班`,
      level: fullSections > 0 ? 'info' : (emptySections > totalSections / 2 ? 'warn' : 'safe'),
      link: '/admin/sections'
    }
  ]
})

// 获取统计数据
const fetchStats = async () => {
  try {
    const [coursesRes, sectionsRes, studentsRes, teachersRes] = await Promise.all([
      adminNewApi.getCourseList(),
      adminNewApi.getSectionList(),
      adminNewApi.getStudentList({ status: null }),
      adminNewApi.getTeacherList({ status: null })
    ])

    if (coursesRes?.status === 200) {
      coursesData.value = Array.isArray(coursesRes.data) ? coursesRes.data : []
      stats.totalCourses = coursesData.value.length
      stats.activeCourses = coursesData.value.filter(c => c.status === 1).length
    }
    if (sectionsRes?.status === 200) {
      sectionsData.value = Array.isArray(sectionsRes.data) ? sectionsRes.data : []
    }
    if (studentsRes?.status === 200) {
      studentsData.value = Array.isArray(studentsRes.data) ? studentsRes.data : []
      stats.totalStudents = studentsData.value.length
      stats.newStudents = studentsData.value.filter(s => s.status === 1).length
    }
    if (teachersRes?.status === 200) {
      teachersData.value = Array.isArray(teachersRes.data) ? teachersRes.data : []
      stats.totalTeachers = teachersData.value.length
      stats.newTeachers = teachersData.value.filter(t => t.status === 1).length
    }

    stats.averageScore = 85.5
    stats.passRate = 95.5
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 初始化（首次加载 + 后续切换回来都会触发）
onMounted(() => {
  fetchStats()
})
onActivated(() => {
  fetchStats()
})

const handleAlertClick = (link) => {
  if (link) router.push(link)
}
</script> 