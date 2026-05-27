<template>
  <div class="courses-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-group">
        <el-input
          v-model="searchKey"
          placeholder="搜索课程"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
    </div>

    <!-- 课程列表 -->
    <el-card class="course-list" v-loading="loading">
      <el-table :data="pagedData" style="width: 100%">
        <el-table-column prop="id" label="课程代码" min-width="100" />
        <el-table-column prop="name" label="课程名称" min-width="140" />
        <el-table-column prop="sectionCode" label="教学班" min-width="160" />
        <el-table-column prop="credit" label="学分" min-width="70" align="center" />
        <el-table-column label="学期" min-width="130">
          <template #default="{ row }">
            {{ formatDate(row.term) }}
          </template>
        </el-table-column>
        <el-table-column prop="studentLimit" label="选课人数" min-width="110" align="center">
          <template #default="{ row }">
            <div class="capacity-info">
              <div class="capacity-text">
                <span class="current">{{ row.selectedCount || 0 }}</span>
                <span class="separator">/</span>
                <span class="total">{{ row.studentLimit || 0 }}</span>
              </div>
              <el-progress 
                :percentage="getCapacityPercentage(row.selectedCount, row.studentLimit)"
                :status="getCapacityStatus(row.selectedCount, row.studentLimit)"
                :stroke-width="8"
                :show-text="false"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleGrades(row)">成绩管理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-section">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { teacherNewApi } from '@/api/new-api'
import './style.scss'

const router = useRouter()
const loading = ref(false)
const searchKey = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 课程列表（原始数据）
const courseList = ref([])

// 搜索过滤后的数据
const filteredData = computed(() => {
  if (!searchKey.value) return courseList.value
  const keyword = searchKey.value.toLowerCase()
  return courseList.value.filter(s =>
    (s.name && s.name.toLowerCase().includes(keyword)) ||
    (s.sectionCode && s.sectionCode.toLowerCase().includes(keyword))
  )
})

// 分页数据
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

// 获取容量百分比
const getCapacityPercentage = (selected = 0, limit = 1) => {
  if (!selected || !limit) return 0
  const percentage = Math.min((selected / limit) * 100, 100)
  return Math.round(percentage * 10) / 10  // 保留一位小数
}

// 获取容量状态
const getCapacityStatus = (selected = 0, limit = 1) => {
  if (!selected || !limit) return 'success'
  const percentage = selected / limit
  if (percentage >= 0.9) return 'exception'
  if (percentage >= 0.7) return 'warning'
  return 'success'
}

// 获取课程列表（使用 /api/teachers/{teacherId}/sections）
const fetchCourses = async () => {
  try {
    loading.value = true
    const teacherId = localStorage.getItem('teacherId')
    if (!teacherId) {
      ElMessage.error('未获取到教师信息，请重新登录')
      return
    }
    const res = await teacherNewApi.getSections(parseInt(teacherId, 10))
    
    if (res && (res.status === 200 || res.code === 200)) {
      const data = res.data || []
      courseList.value = data.map(s => ({
        id: s.sectionId,
        name: s.courseName,
        sectionCode: s.sectionCode,
        credit: s.credit || 0,
        term: s.semester,
        selectedCount: s.enrolledCount || s.selectedCount || 0,
        studentLimit: s.capacityLimit || 0,
        sectionCount: 1,
        status: s.status  // 使用后端真实 status 字段
      }))
    } else {
      ElMessage.warning(res?.msg || '获取课程列表失败')
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 处理成绩管理（跳转到成绩管理页，传入 sectionId）
const handleGrades = (course) => {
  router.push(`/teacher/grades?sectionId=${course.id}`)
}

// 初始化
onMounted(() => {
  fetchCourses()
})

// 格式化学期显示
const formatDate = (semester) => {
  if (!semester) return ''
  // semester 格式如 "2025-2026-1"（学年-学期）
  return semester
}
</script> 

<style lang="scss" scoped>
.capacity-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
  
  .capacity-text {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    
    .current {
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
    
    .separator {
      color: var(--el-text-color-secondary);
    }
    
    .total {
      color: var(--el-text-color-secondary);
    }
  }
  
  :deep(.el-progress-bar__outer) {
    border-radius: 4px;
    background-color: rgba(255, 255, 255, 0.1);
  }
  
  :deep(.el-progress-bar__inner) {
    border-radius: 4px;
    transition: all 0.3s ease;
  }
}
</style> 