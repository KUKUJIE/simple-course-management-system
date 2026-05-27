<template>
  <div class="grades-container">
    <!-- 教学班选择 -->
    <el-card class="section-card" v-if="!selectedSection">
      <template #header>
        <div class="card-header">
          <span>选择教学班</span>
        </div>
      </template>

      <el-table :data="pagedSectionList" style="width: 100%" v-loading="loadingSections">
        <el-table-column prop="sectionCode" label="教学班编号" min-width="140" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSectionStatusTag(row)">
              {{ getSectionStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleSelectSection(row)">
              管理成绩
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section">
        <el-pagination
          v-model:current-page="sectionCurrentPage"
          v-model:page-size="sectionPageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="sectionList.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <!-- 成绩管理 -->
    <template v-else>
      <div class="action-bar">
        <div class="section-info">
          <h3>{{ selectedSection.courseName }}</h3>
          <p>{{ selectedSection.sectionCode }} | {{ selectedSection.semester }}</p>
        </div>
        <el-button type="success" @click="handleBack">返回教学班列表</el-button>
      </div>

      <el-card class="section-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>学生成绩列表</span>
            <div class="header-actions">
              <el-button type="success" @click="handleSaveAll">
                保存全部
              </el-button>
            </div>
          </div>
        </template>

        <el-table :data="pagedStudentList" style="width: 100%">
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column prop="studentName" label="姓名" min-width="100" />
          <el-table-column prop="majorName" label="专业" min-width="140" />
          <el-table-column label="平时成绩" min-width="120" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.usualScore"
                :min="0"
                :max="100"
                :precision="0"
                size="small"
                @change="handleScoreChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="考试成绩" min-width="120" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.examScore"
                :min="0"
                :max="100"
                :precision="0"
                size="small"
                @change="handleScoreChange(row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="总评成绩" min-width="120" align="center">
            <template #default="{ row }">
              <span>{{ computeFinalScore(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.hasScore ? 'success' : 'info'">
                {{ row.hasScore ? '已录入' : '未录入' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" align="center">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                @click="handleSaveOne(row)"
              >
                保存
              </el-button>
              <el-button
                type="danger"
                link
                :disabled="!row.hasScore"
                @click="handleClearScore(row)"
              >
                清空
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-section">
          <el-pagination
            v-model:current-page="gradesCurrentPage"
            v-model:page-size="gradesPageSize"
            :page-sizes="[10, 20, 30, 50]"
            :total="studentList.length"
            layout="total, sizes, prev, pager, next"
            background
          />
        </div>

        <!-- 成绩统计 -->
        <div class="statistics">
          <div class="stat-item"><span class="stat-label">平均分</span><span class="stat-value">{{ stats.average.toFixed(1) }}</span></div>
          <div class="stat-item"><span class="stat-label">及格率</span><span class="stat-value">{{ stats.passRate.toFixed(1) }}%</span></div>
          <div class="stat-item"><span class="stat-label">优秀率</span><span class="stat-value">{{ stats.excellentRate.toFixed(1) }}%</span></div>
          <div class="stat-item"><span class="stat-label">最高分</span><span class="stat-value">{{ stats.highest }}</span></div>
        </div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { teacherNewApi } from '@/api/new-api'
import './style.scss'

const loading = ref(false)
const loadingSections = ref(false)
const selectedSection = ref(null)
const sectionList = ref([])
const studentList = ref([])
const sectionCurrentPage = ref(1)
const sectionPageSize = ref(10)
const gradesCurrentPage = ref(1)
const gradesPageSize = ref(10)

// 分页显示的教学班列表
const pagedSectionList = computed(() => {
  const start = (sectionCurrentPage.value - 1) * sectionPageSize.value
  return sectionList.value.slice(start, start + sectionPageSize.value)
})

// 分页显示的学生列表
const pagedStudentList = computed(() => {
  const start = (gradesCurrentPage.value - 1) * gradesPageSize.value
  return studentList.value.slice(start, start + gradesPageSize.value)
})

// 安全获取 teacherId
const getTeacherId = () => {
  const tid = localStorage.getItem('teacherId')
  if (tid) return parseInt(tid, 10)
  return null
}

// 统计数据
const stats = reactive({
  average: 0,
  passRate: 0,
  excellentRate: 0,
  highest: 0
})

// 是否有未保存的更改
const hasChanges = computed(() =>
  studentList.value.some(student => hasScoreChange(student))
)

// 安全性获取 enrolledCount 默认值
const getEnrolledCount = (s) => s.totalStudents || s.enrolledCount || s.selectedCount || 0

// 教学班状态文本（三态：待录入 / 录入中 / 已录入）
const getSectionStatusText = (row) => {
  const graded = row.gradedCount || 0
  const total = getEnrolledCount(row)
  if (total === 0 || graded === 0) return '待录入'
  if (graded >= total) return '已录入'
  return '录入中'
}

// 教学班状态标签类型
const getSectionStatusTag = (row) => {
  const graded = row.gradedCount || 0
  const total = getEnrolledCount(row)
  if (total === 0 || graded === 0) return 'warning'
  if (graded >= total) return 'success'
  return ''  // info / default
}
const computeFinalScore = (row) => {
  const usual = row.usualScore || 0
  const exam = row.examScore || 0
  if (row.hasScore && row.finalScore !== undefined && row.finalScore !== null) {
    return row.finalScore.toFixed(1)
  }
  return ((usual * 0.3 + exam * 0.7)).toFixed(1)
}

// 获取教师教学班列表
const fetchSections = async () => {
  try {
    loadingSections.value = true
    const teacherId = getTeacherId()
    const res = await teacherNewApi.getSections(teacherId)

    if (res && res.data) {
      // 为每个 section 获取成绩统计以确定录入状态
      const sections = res.data
      const sectionListRaw = await Promise.all(
        sections.map(async (s) => {
          const sectionId = s.sectionId || s.id
          let gradedCount = s.gradedCount || 0
          let totalStudents = s.totalStudents || s.enrolledCount || s.selectedCount || 0
          // 尝试获取该 section 的成绩汇总数据
          try {
            const scoreRes = await teacherNewApi.getSectionScores(sectionId, teacherId)
            if (scoreRes && (scoreRes.status === 200 || scoreRes.code === 200) && scoreRes.data) {
              const scData = scoreRes.data
              gradedCount = scData.gradedCount || gradedCount
              totalStudents = scData.totalStudents || totalStudents
            }
          } catch (_) { /* 忽略单个查询失败 */ }
          return {
            ...s,
            id: sectionId,
            gradedCount,
            totalStudents,
            hasScore: gradedCount > 0
          }
        })
      )
      sectionList.value = sectionListRaw
    }
  } catch (error) {
    console.error('获取教学班列表失败:', error)
    ElMessage.error('获取教学班列表失败')
  } finally {
    loadingSections.value = false
  }
}

// 获取教学班成绩汇总
const fetchSectionScores = async (sectionId) => {
  try {
    loading.value = true
    const teacherId = getTeacherId()
    const res = await teacherNewApi.getSectionScores(sectionId, teacherId)

    if (res && res.data) {
      const data = res.data
      // 先获取学生名单
      await fetchStudents(sectionId)
    }
  } catch (error) {
    console.error('获取成绩汇总失败:', error)
    ElMessage.error('获取成绩汇总失败')
  } finally {
    loading.value = false
  }
}

// 获取教学班学生名单
const fetchStudents = async (sectionId) => {
  try {
    loading.value = true
    const teacherId = getTeacherId()
    const res = await teacherNewApi.getStudents(sectionId, teacherId)

    if (res && res.data) {
      studentList.value = res.data.map(s => ({
        ...s,
        changed: false,
        originalUsualScore: s.usualScore || 0,
        originalExamScore: s.examScore || 0,
        originalHasScore: s.hasScore || false
      }))
      updateStats()
    }
  } catch (error) {
    console.error('获取学生名单失败:', error)
    ElMessage.error('获取学生名单失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStats = () => {
  const scores = studentList.value
    .map(s => {
      const finalScore = s.hasScore && s.finalScore !== undefined
        ? s.finalScore
        : ((s.usualScore || 0) * 0.3 + (s.examScore || 0) * 0.7)
      return finalScore
    })
    .filter(s => s > 0)

  if (scores.length === 0) {
    Object.assign(stats, { average: 0, passRate: 0, excellentRate: 0, highest: 0 })
    return
  }

  stats.average = scores.reduce((sum, s) => sum + s, 0) / scores.length
  stats.passRate = (scores.filter(s => s >= 60).length / scores.length) * 100
  stats.excellentRate = (scores.filter(s => s >= 90).length / scores.length) * 100
  stats.highest = Math.max(...scores)
}

// 选择教学班
const handleSelectSection = (section) => {
  selectedSection.value = section
  fetchStudents(section.id)
}

// 返回教学班列表
const handleBack = () => {
  if (hasChanges.value) {
    ElMessageBox.confirm(
      '有未保存的成绩，确定要返回吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      selectedSection.value = null
      studentList.value = []
    }).catch(() => {})
  } else {
    selectedSection.value = null
    studentList.value = []
  }
}

// 处理成绩变化
const handleScoreChange = (student) => {
  student.changed =
    (student.usualScore || 0) !== student.originalUsualScore ||
    (student.examScore || 0) !== student.originalExamScore
  updateStats()
}

// 判断该学生是否有需要保存的更改（分数变更 或 清空后已改）
const hasScoreChange = (student) => {
  if (student.changed) return true
  // 清空后 hasScore 已变 false，但分数为 0 即认为已清空待确认
  if (!student.hasScore && (student.usualScore !== 0 || student.examScore !== 0)) return true
  return false
}

// 保存单个学生成绩
const handleSaveOne = async (student) => {
  try {
    if (!hasScoreChange(student)) {
      ElMessage.info('当前成绩未变更，无需保存')
      return
    }
    const teacherId = getTeacherId()
    const res = await teacherNewApi.saveScore(
      student.enrollmentId,
      teacherId,
      student.usualScore || 0,
      student.examScore || 0
    )
    if (res && (res.status === 200 || res.code === 200)) {
      const returned = res.data || {}
      const wasScored = student.hasScore
      student.changed = false
      student.usualScore = returned.usualScore ?? student.usualScore
      student.examScore = returned.examScore ?? student.examScore
      student.finalScore = returned.finalScore ?? student.finalScore
      student.originalUsualScore = student.usualScore || 0
      student.originalExamScore = student.examScore || 0
      student.hasScore = (returned.finalScore != null) || (student.usualScore > 0 || student.examScore > 0)
      // 如果之前没有成绩，现在新增了一条 → gradedCount+1
      if (!wasScored && student.hasScore && selectedSection.value) {
        selectedSection.value.gradedCount = (selectedSection.value.gradedCount || 0) + 1
        const idx = sectionList.value.findIndex(s => s.id === selectedSection.value?.id)
        if (idx >= 0) sectionList.value[idx].gradedCount = (sectionList.value[idx].gradedCount || 0) + 1
      }
      ElMessage.success('保存成功')
      updateStats()
    } else {
      ElMessage.warning(res?.msg || '保存失败')
    }
  } catch (error) {
    console.error('保存成绩失败:', error)
    ElMessage.error('保存失败')
  }
}

// 清空学生成绩（调 saveScore 将成绩置 0，同时更新 section 录入状态）
const handleClearScore = async (student) => {
  try {
    await ElMessageBox.confirm(
      `确定要清空 ${student.studentName || student.studentNo} 的成绩吗？`,
      '清空成绩',
      { confirmButtonText: '确定清空', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  try {
    const teacherId = getTeacherId()
    const res = await teacherNewApi.saveScore(student.enrollmentId, teacherId, 0, 0)
    if (res && (res.status === 200 || res.code === 200)) {
      const returned = res.data || {}
      student.changed = false
      student.usualScore = 0
      student.examScore = 0
      student.finalScore = returned.finalScore ?? 0
      student.originalUsualScore = 0
      student.originalExamScore = 0
      student.hasScore = false
      // 更新当前 section 的 gradedCount
      if (selectedSection.value) {
        selectedSection.value.gradedCount = Math.max(0, (selectedSection.value.gradedCount || 1) - 1)
      }
      // 同步更新 sectionList 中对应的 section
      const idx = sectionList.value.findIndex(s => s.id === selectedSection.value?.id)
      if (idx >= 0) {
        sectionList.value[idx].gradedCount = Math.max(0, (sectionList.value[idx].gradedCount || 1) - 1)
      }
      ElMessage.success('成绩已清空，可重新录入')
      updateStats()
    } else {
      ElMessage.warning(res?.msg || '清空失败')
    }
  } catch (error) {
    console.error('清空成绩失败:', error)
    ElMessage.error('清空失败')
  }
}

// 保存所有更改
const handleSaveAll = async () => {
  try {
    const changedStudents = studentList.value.filter(s => hasScoreChange(s))
    if (changedStudents.length === 0) {
      ElMessage.info('没有需要保存的更改')
      return
    }
    const teacherId = getTeacherId()
    const savePromises = changedStudents.map(s =>
      teacherNewApi.saveScore(
        s.enrollmentId,
        parseInt(teacherId),
        s.usualScore || 0,
        s.examScore || 0
      )
    )

    const results = await Promise.all(savePromises)

    // 逐个回写后端返回的数据，并更新 gradedCount
    let newGradedDelta = 0
    changedStudents.forEach((s, i) => {
      const wasScored = s.hasScore
      const returned = (results[i] && (results[i].status === 200 || results[i].code === 200)) 
        ? (results[i].data || {}) : {}
      s.changed = false
      s.usualScore = returned.usualScore ?? s.usualScore
      s.examScore = returned.examScore ?? s.examScore
      s.finalScore = returned.finalScore ?? s.finalScore
      s.originalUsualScore = s.usualScore || 0
      s.originalExamScore = s.examScore || 0
      s.hasScore = (returned.finalScore != null) || (s.usualScore > 0 || s.examScore > 0)
      if (!wasScored && s.hasScore) newGradedDelta++
    })
    // 更新 section 录入状态
    if (newGradedDelta > 0 && selectedSection.value) {
      selectedSection.value.gradedCount = (selectedSection.value.gradedCount || 0) + newGradedDelta
      const idx = sectionList.value.findIndex(s => s.id === selectedSection.value?.id)
      if (idx >= 0) sectionList.value[idx].gradedCount = (sectionList.value[idx].gradedCount || 0) + newGradedDelta
    }
    ElMessage.success('全部保存成功')
    updateStats()
  } catch (error) {
    console.error('保存成绩失败:', error)
    ElMessage.error('部分保存失败，请重试')
  }
}

// 初始化
onMounted(() => {
  fetchSections()
})
</script>
