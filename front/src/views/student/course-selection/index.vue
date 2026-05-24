<template>
  <div class="course-selection">
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="课程名称">
          <el-input 
            v-model="filterForm.courseName" 
            placeholder="搜索课程" 
            clearable
          />
        </el-form-item>
        <el-form-item label="教师姓名">
          <el-input 
            v-model="filterForm.teacherName" 
            placeholder="搜索教师" 
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button type="primary" plain @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程列表 -->
    <el-card class="course-list" v-loading="loading">
      <template #header>
        <div class="list-header">
          <span>可选教学班列表</span>
          <div class="selection-info">
            已选：{{ selectedCount }}/{{ maxSelection }} 门
          </div>
        </div>
      </template>

      <el-table :data="sectionList" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" min-width="140" />
        <el-table-column prop="sectionCode" label="教学班编号" width="130" />
        <el-table-column prop="teacherName" label="任课教师" width="110" />
        <el-table-column prop="credit" label="学分" width="70" align="center" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="classroom" label="教室" min-width="120" />
        <el-table-column label="容纳/已选" width="110" align="center">
          <template #default="{ row }">
            <span>{{ row.selectedCount }}/{{ row.capacityLimit }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.selected" size="small" type="success">已选</el-tag>
            <el-tag v-else-if="row.remaining <= 0" size="small" type="danger">满员</el-tag>
            <el-tag v-else size="small" type="info">可选</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.selected"
              type="danger"
              link
              @click="handleDrop(row)"
            >退选</el-button>
            <el-button
              v-else
              type="primary"
              :disabled="row.remaining <= 0 || selectedCount >= maxSelection"
              link
              @click="handleEnroll(row)"
            >选课</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { sectionApi, enrollmentApi } from '@/api/new-api'
import './style.scss'

// 筛选表单
const filterForm = reactive({
  courseName: '',
  teacherName: ''
})

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const maxSelection = 6

// 教学班列表
const sectionList = ref([])
const selectedCount = computed(() => sectionList.value.filter(s => s.selected).length)

// 获取教学班列表（真正使用分页参数）
const fetchSections = async (resetPage = false) => {
  try {
    loading.value = true
    if (resetPage) currentPage.value = 1
    const res = await sectionApi.getSections({
      page: currentPage.value,
      pageSize: pageSize.value,
      status: 1
    })
    if (res && (res.code === 200 || res.status === 200)) {
      const list = res.data?.list || res.data || []
      sectionList.value = list.map(s => ({
        sectionId: s.sectionId,
        sectionCode: s.sectionCode,
        semester: s.semester,
        courseName: s.courseName,
        teacherName: s.teacherName,
        credit: s.credit,
        classroom: (s.building || '') + (s.roomNo ? ' ' + s.roomNo : ''),
        capacityLimit: s.capacityLimit,
        selectedCount: s.selectedCount,
        remaining: s.remaining,
        selected: false
      }))
      // 使用后端返回的真实 total
      total.value = res.data?.total ?? list.length
    }
  } catch (error) {
    console.error('获取教学班列表失败:', error)
    ElMessage.error('获取教学班列表失败')
  } finally {
    loading.value = false
  }
}

// 获取已选教学班（标记已选状态）
const fetchMySelected = async () => {
  const studentId = localStorage.getItem('studentId')
  if (!studentId) return
  try {
    const res = await enrollmentApi.getMyEnrollments(studentId)
    if (res && (res.code === 200 || res.status === 200)) {
      const enrollments = res.data?.list || res.data || []
      const map = {}
      enrollments.filter(e => e.status === 1).forEach(e => { map[e.sectionId] = e.enrollmentId })
      sectionList.value = sectionList.value.map(s => ({
        ...s, selected: !!map[s.sectionId], enrollmentId: map[s.sectionId] || null
      }))
    }
  } catch (e) { console.error(e) }
}

// 选课
const handleEnroll = async (section) => {
  if (selectedCount.value >= maxSelection) { ElMessage.warning(`每学期最多选择${maxSelection}门课程`); return }
  const studentId = localStorage.getItem('studentId')
  if (!studentId) { ElMessage.error('未获取到学生信息，请重新登录'); return }
  try {
    const res = await enrollmentApi.enroll(Number(studentId), section.sectionId)
    if (res && (res.code === 200 || res.status === 200) && res.data?.result === 1) {
      section.selected = true; section.enrollmentId = res.data.enrollmentId
      section.selectedCount++; section.remaining--
      ElMessage.success(`已选：${section.courseName}（${section.sectionCode}）`)
    } else if (res.data?.result === 2) { ElMessage.warning('已选过该课程，不可重复选') }
    else if (res.data?.result === 0) { ElMessage.warning('容量已满') }
    else { ElMessage.error(res?.msg || res?.message || '选课失败') }
  } catch (e) { console.error(e); ElMessage.error('选课失败') }
}

// 退课
const handleDrop = async (section) => {
  try {
    await ElMessageBox.confirm(
      `确定要退选"${section.courseName}（${section.sectionCode}）"吗？`,
      '退课确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    if (!section.enrollmentId) { ElMessage.error('缺少选课记录ID'); return }
    const res = await enrollmentApi.drop(section.enrollmentId)
    if (res && (res.code === 200 || res.status === 200)) {
      section.selected = false; section.enrollmentId = null
      section.selectedCount--; section.remaining++
      ElMessage.success(`已退选：${section.courseName}`)
    } else { ElMessage.error(res?.msg || res?.message || '退课失败') }
  } catch (e) { if (e !== 'cancel') { console.error(e); ElMessage.error('退课失败') } }
}

// 搜索（前端筛选，先取全部数据再过滤）
const handleSearch = async () => {
  if (!filterForm.courseName && !filterForm.teacherName) {
    currentPage.value = 1
    await fetchSections()
    await fetchMySelected()
    return
  }
  loading.value = true
  try {
    // 搜索时拉取全量数据进行前端筛选
    const res = await sectionApi.getSections({ page: 1, pageSize: 9999, status: 1 })
    let data = res.data?.list || res.data || []
    if (filterForm.courseName) data = data.filter(s => (s.courseName || '').includes(filterForm.courseName))
    if (filterForm.teacherName) data = data.filter(s => (s.teacherName || '').includes(filterForm.teacherName))
    sectionList.value = data.map(s => ({
      sectionId: s.sectionId, sectionCode: s.sectionCode, semester: s.semester,
      courseName: s.courseName, teacherName: s.teacherName, credit: s.credit,
      classroom: (s.building || '') + (s.roomNo ? ' ' + s.roomNo : ''),
      capacityLimit: s.capacityLimit, selectedCount: s.selectedCount,
      remaining: s.remaining, selected: false, enrollmentId: null
    }))
    total.value = data.length
    currentPage.value = 1
  } finally { loading.value = false }
  await fetchMySelected()
}

const resetForm = () => { filterForm.courseName = ''; filterForm.teacherName = ''; handleSearch() }
const handleSizeChange = () => { currentPage.value = 1; fetchSections() }
const handleCurrentChange = () => { fetchSections() }

onMounted(async () => { await fetchSections(); await fetchMySelected() })
</script> 