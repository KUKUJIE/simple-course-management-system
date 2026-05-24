<template>
  <div class="grades-container">
    <!-- 成绩统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.gpa.toFixed(2) }}</div>
            <div class="stat-label">平均绩点</div>
            <div class="stat-trend" :class="{ 'up': stats.gpaChange > 0 }">
              {{ stats.gpaChange > 0 ? '+' : '' }}{{ stats.gpaChange.toFixed(2) }}
              <el-icon><ArrowUp v-if="stats.gpaChange > 0" /><ArrowDown v-else /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCredits }}</div>
            <div class="stat-label">已修学分</div>
            <div class="stat-progress">
              <el-progress 
                :percentage="(stats.totalCredits / stats.requiredCredits) * 100"
                :format="format"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.passRate.toFixed(1) }}%</div>
            <div class="stat-label">通过率</div>
            <div class="stat-desc">已通过 {{ stats.passedCourses }}/{{ stats.totalCourses }} 门</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ stats.excellentRate.toFixed(1) }}%</div>
            <div class="stat-label">优秀率</div>
            <div class="stat-desc">优秀 {{ stats.excellentCourses }} 门</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成绩详情表格 -->
    <el-card class="grade-list">
      <template #header>
        <div class="card-header">
          <span>成绩详情</span>
        </div>
      </template>

      <el-table
        :data="pagedData"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="暂无成绩记录"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)' }"
      >
        <el-table-column prop="term" label="学期" width="180" align="center" />
        <el-table-column prop="courseId" label="课程编号" width="110" align="center" />
        <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sectionCode" label="教学班号" width="110" align="center" />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="usualScore" label="平时成绩" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.usualScore != null">{{ row.usualScore }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="examScore" label="考试成绩" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.examScore != null">{{ row.examScore }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="综合成绩" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.score != null" :class="getGradeClass(row.score)">
              {{ row.score }}
            </span>
            <el-tag v-else type="info" size="small">未评分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="gpa" label="GPA绩点" width="90" align="center">
          <template #default="{ row }">
            <span v-if="row.gpa != null">{{ row.gpa }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="是否通过" width="100" align="center">
          <template #default="{ row }">
            <template v-if="row.score != null">
              <el-tag v-if="row.isPassed === 1" type="success" size="small">通过</el-tag>
              <el-tag v-else type="danger" size="small">未通过</el-tag>
            </template>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="gradeList.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { enrollmentApi } from '@/api/new-api'
import './style.scss'

// 统计数据
const stats = reactive({
  gpa: 0,
  gpaChange: 0,
  totalCredits: 0,
  requiredCredits: 120,
  passRate: 0,
  passedCourses: 0,
  totalCourses: 0,
  excellentRate: 0,
  excellentCourses: 0
})

const currentPage = ref(1)
const pageSize = ref(10)

// 成绩列表
const gradeList = ref([])
const loading = ref(false)

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return gradeList.value.slice(start, start + pageSize.value)
})

// 格式化进度
const format = (percentage) => `${stats.totalCredits}/${stats.requiredCredits}`

// 安全获取 studentId
const getStudentId = () => {
  const sid = localStorage.getItem('studentId')
  if (sid) return parseInt(sid, 10)
  return null
}

// 获取成绩样式
const getGradeClass = (grade) => {
  if (grade >= 90) return 'grade-excellent'
  if (grade >= 80) return 'grade-good'
  if (grade >= 60) return 'grade-pass'
  return 'grade-fail'
}

// 获取成绩列表（使用新版 enrollments 接口）
const fetchGrades = async () => {
  try {
    loading.value = true
    const studentId = getStudentId()
    if (!studentId) {
      ElMessage.error('未获取到学生信息，请重新登录')
      return
    }

    const res = await enrollmentApi.getMyEnrollments(studentId, { status: '' })
    const enrollments = res?.data || []
    const enrolledList = Array.isArray(enrollments) ? enrollments : []

    gradeList.value = enrolledList.map(e => ({
      id: e.enrollmentId,
      term: e.semester || '',
      courseId: e.courseCode || '',
      courseName: e.courseName || '',
      sectionCode: e.sectionCode || '',
      credit: e.credit || 0,
      usualScore: e.usualScore ?? null,
      examScore: e.examScore ?? null,
      score: e.finalScore ?? null,
      gpa: e.gpaPoint ?? null,
      isPassed: e.isPassed
    }))

    // 更新统计信息
    const hasScore = gradeList.value.filter(g => g.score != null)
    stats.totalCourses = gradeList.value.length
    stats.passedCourses = hasScore.filter(g => g.score >= 60).length
    stats.excellentCourses = hasScore.filter(g => g.score >= 90).length
    stats.passRate = gradeList.value.length > 0 ? (stats.passedCourses / gradeList.value.length) * 100 : 0
    stats.excellentRate = gradeList.value.length > 0 ? (stats.excellentCourses / gradeList.value.length) * 100 : 0
    stats.totalCredits = hasScore.filter(g => g.score >= 60).reduce((sum, g) => sum + (g.credit || 0), 0)

    // GPA：后端直接返回 gpaPoint
    const gpaList = hasScore.filter(g => g.gpa != null)
    stats.gpa = gpaList.length > 0 ? gpaList.reduce((sum, g) => sum + (g.gpa || 0), 0) / gpaList.length : 0
    stats.gpaChange = 0
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchGrades()
})
</script> 