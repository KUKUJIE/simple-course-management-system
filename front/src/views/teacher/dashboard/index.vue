<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="dashboard-cards">
      <el-col :span="8">
        <el-card class="data-card courses">
          <div class="card-content">
            <div class="card-icon">📚</div>
            <h3>开设课程</h3>
            <div class="number">{{ stats.totalCourses }}</div>
            <div class="desc">本学期 {{ stats.currentTermCourses }} 门</div>
            <el-button type="text" @click="$router.push('/teacher/courses')">课程管理</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="data-card students">
          <div class="card-content">
            <div class="card-icon">👥</div>
            <h3>授课学生</h3>
            <div class="number">{{ stats.totalStudents }}</div>
            <div class="desc">平均每课 {{ stats.averageStudents }} 人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="data-card grades">
          <div class="card-content">
            <div class="card-icon">📊</div>
            <h3>课程成绩</h3>
            <div class="number">{{ stats.averageScore.toFixed(1) }}</div>
            <div class="desc">及格率 {{ stats.passRate.toFixed(1) }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课程列表 -->
    <el-card class="course-list" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>本学期课程</span>
          <el-button text @click="$router.push('/teacher/courses')">
            查看全部课程
          </el-button>
        </div>
      </template>

      <el-table :data="dashboardPagedData" style="width: 100%">
        <el-table-column prop="name" label="课程名称" min-width="180" />
        <el-table-column prop="credit" label="学分" width="80" align="center" />
        <el-table-column label="选课人数" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getCapacityTagType(row.selectedCount, row.studentLimit)">
              {{ row.selectedCount }}/{{ row.studentLimit }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="成绩录入" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getGradeStatusTag(row)">
              {{ getGradeStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link
              @click="handleGrades(row)"
            >
              成绩管理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section">
        <el-pagination
          v-model:current-page="dashboardCurrentPage"
          v-model:page-size="dashboardPageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="courseList.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { teacherNewApi } from '@/api/new-api'
import './style.scss'

const router = useRouter()
const loading = ref(false)

// 统计数据
const stats = reactive({
  totalCourses: 0,
  currentTermCourses: 0,
  totalStudents: 0,
  averageStudents: 0,
  averageScore: 0,
  passRate: 0
})

// 课程列表
const courseList = ref([])
const dashboardPageSize = ref(10)
const dashboardCurrentPage = ref(1)

const dashboardPagedData = computed(() => {
  const start = (dashboardCurrentPage.value - 1) * dashboardPageSize.value
  return courseList.value.slice(start, start + dashboardPageSize.value)
})

// Canvas引用（已废弃）

// 安全获取 enrolledCount
const getEnrolledCount = (s) => s.totalStudents || s.enrolledCount || s.selectedCount || 0

// 成绩录入状态：三态 — 待录入 / 录入中 / 已录入
const getGradeStatusText = (row) => {
  const graded = row.gradedCount || 0
  const total = getEnrolledCount(row)
  if (total === 0 || graded === 0) return '待录入'
  if (graded >= total) return '已录入'
  return '录入中'
}

const getGradeStatusTag = (row) => {
  const graded = row.gradedCount || 0
  const total = getEnrolledCount(row)
  if (total === 0 || graded === 0) return 'warning'
  if (graded >= total) return 'success'
  return ''
}

// 获取教师课程数据（含成绩统计）
const fetchTeacherData = async () => {
  try {
    loading.value = true
    const teacherId = localStorage.getItem('teacherId')
    if (!teacherId) return
    const res = await teacherNewApi.getSections(parseInt(teacherId, 10))

    if (res && res.data) {
      // 为每个 section 获取成绩统计
      const sectionsRaw = await Promise.all(
        res.data.map(async (section) => {
          const sectionId = section.sectionId || section.id
          let gradedCount = section.gradedCount || 0
          let averageScore = section.averageScore || 0
          let passedCount = section.passedCount || 0
          try {
            const scoreRes = await teacherNewApi.getSectionScores(sectionId, parseInt(teacherId, 10))
            if (scoreRes && (scoreRes.status === 200 || scoreRes.code === 200) && scoreRes.data) {
              const sc = scoreRes.data
              gradedCount = sc.gradedCount || gradedCount
            }
          } catch (_) { /* 忽略单个失败 */ }
          return {
            ...section,
            id: sectionId,
            name: section.courseName || section.sectionCode,
            credit: section.credit || 0,
            selectedCount: section.enrolledCount || section.selectedCount || 0,
            studentLimit: section.capacityLimit || section.selectedCount || 0,
            gradedCount,
            gradesEntered: gradedCount > 0
          }
        })
      )
      courseList.value = sectionsRaw

      // 更新统计数据
      let totalStudents = 0
      let totalScore = 0
      let passedCount = 0
      let scoreCount = 0

      courseList.value.forEach(section => {
        const enrolled = section.enrolledCount || section.selectedCount || 0
        const graded = section.gradedCount || 0
        totalStudents += enrolled

        const sectionAvg = section.averageScore || 0
        const sectionPassed = section.passedCount || 0
        if (graded > 0 && sectionAvg >= 0) {
          totalScore += sectionAvg * graded
          scoreCount += graded
          passedCount += (sectionPassed || 0)
        }
      })

      stats.totalCourses = courseList.value.length
      stats.currentTermCourses = courseList.value.length
      stats.totalStudents = totalStudents
      stats.averageStudents = courseList.value.length > 0 ? Math.round(totalStudents / courseList.value.length) : 0
      stats.averageScore = scoreCount > 0 ? totalScore / scoreCount : 0
      stats.passRate = scoreCount > 0 ? (passedCount / scoreCount) * 100 : 0
    }
  } catch (error) {
    console.error('获取教师数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 绘制圆形进度（已废弃）

// 获取容量标签类型
const getCapacityTagType = (selected, limit) => {
  const percentage = selected / limit
  if (percentage >= 0.9) return 'danger'
  if (percentage >= 0.7) return 'warning'
  return 'success'
}

// 处理成绩管理（传入 sectionId）
const handleGrades = (course) => {
  router.push(`/teacher/grades?sectionId=${course.id || course.sectionId}`)
}

// 初始化
onMounted(async () => {
  await fetchTeacherData()
})
</script> 