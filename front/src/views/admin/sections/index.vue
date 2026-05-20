<template>
  <div class="sections-container">
    <div class="page-header"><h2>教学班管理</h2></div>

    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="课程名/教师名搜索" style="width: 200px" clearable @keyup.enter="fetchData" @clear="fetchData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="searchStatus" placeholder="状态筛选" style="width: 140px" clearable @change="fetchData">
        <el-option label="启用" :value="1" />
        <el-option label="关闭" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchData"><el-icon><Search /></el-icon>查询</el-button>
      <el-button type="success" @click="handleAdd"><el-icon><Plus /></el-icon>新增教学班</el-button>
    </div>

    <el-card class="data-card" v-loading="loading">
      <el-table :data="filteredData" style="width: 100%" empty-text="暂无教学班数据">
        <el-table-column prop="sectionCode" label="教学班编号" min-width="130" />
        <el-table-column prop="courseName" label="课程名" min-width="160" />
        <el-table-column prop="teacherName" label="教师名" min-width="100" />
        <el-table-column prop="classroomName" label="教室名" min-width="120" />
        <el-table-column prop="semester" label="学期" min-width="120" align="center" />
        <el-table-column prop="capacityLimit" label="容量" min-width="80" align="center" />
        <el-table-column prop="selectedCount" label="已选" min-width="70" align="center" />
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '关闭' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleClose(row)">关闭</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增教学班' : '编辑教学班'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item v-if="dialogType === 'add'" label="教学班编码" prop="sectionCode">
          <el-input v-model="formData.sectionCode" placeholder="请输入唯一的教学班编码，如 SEC-2025-001" maxlength="50" />
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="formData.courseId" placeholder="请选择课程" style="width: 100%" filterable>
            <el-option v-for="c in courseOptions" :key="c.id || c.courseId" :label="c.name || c.courseName" :value="c.id || c.courseId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师" prop="teacherId">
          <el-select v-model="formData.teacherId" placeholder="请选择教师" style="width: 100%" filterable>
            <el-option v-for="t in teacherOptions" :key="t.teacherId" :label="t.teacherName" :value="t.teacherId" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室" prop="classroomId">
          <el-select v-model="formData.classroomId" placeholder="请选择教室" style="width: 100%" filterable>
            <el-option v-for="r in classroomOptions" :key="r.classroomId" :label="`${r.building||''}${r.roomNo||''}`" :value="r.classroomId" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="formData.semester" placeholder="如 2025-2026-1" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="容量" prop="capacityLimit">
              <el-input-number v-model="formData.capacityLimit" :min="1" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="formData.status" style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="关闭" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button plain @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { adminNewApi } from '@/api/new-api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const searchKeyword = ref('')
const searchStatus = ref(null)
const tableData = ref([])
const courseOptions = ref([])
const teacherOptions = ref([])
const classroomOptions = ref([])

const formData = reactive({
  sectionId: null, sectionCode: '', courseId: null, teacherId: null, classroomId: null,
  semester: '', capacityLimit: 30, status: 1
})

const rules = {
  sectionCode: [{ required: true, message: '请输入教学班编码', trigger: 'blur' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  classroomId: [{ required: true, message: '请选择教室', trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  capacityLimit: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const filteredData = computed(() => {
  let list = tableData.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r => (r.courseName || '').toLowerCase().includes(kw) || (r.teacherName || '').toLowerCase().includes(kw))
  }
  if (searchStatus.value !== null && searchStatus.value !== '') {
    list = list.filter(r => r.status === searchStatus.value)
  }
  return list
})

const loadOptions = async () => {
  try {
    const [cRes, tRes, rRes] = await Promise.all([
      adminNewApi.getCourseOptions(),
      adminNewApi.getTeacherOptions(),
      adminNewApi.getClassroomOptions()
    ])
    if (cRes?.status === 200) courseOptions.value = cRes.data || []
    if (tRes?.status === 200) teacherOptions.value = tRes.data || []
    if (rRes?.status === 200) classroomOptions.value = rRes.data || []
  } catch (e) { console.error('加载下拉选项失败:', e) }
}

const fetchData = async () => {
  try {
    loading.value = true
    const res = await adminNewApi.getSectionList()
    if (res?.status === 200) tableData.value = res.data || []
    else ElMessage.error(res?.msg || '获取教学班列表失败')
  } catch (e) {
    console.error('获取教学班列表失败:', e)
    ElMessage.error('获取教学班列表失败')
  } finally { loading.value = false }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, { sectionId: null, sectionCode: '', courseId: null, teacherId: null, classroomId: null, semester: '', capacityLimit: 30, status: 1 })
}

const handleAdd = () => { dialogType.value = 'add'; resetForm(); dialogVisible.value = true }

const handleEdit = async (row) => {
  try {
    const res = await adminNewApi.getSectionById(row.sectionId)
    if (res?.status === 200) {
      const d = res.data
      dialogType.value = 'edit'
      formData.sectionId = d.sectionId
      formData.courseId = d.courseId
      formData.teacherId = d.teacherId
      formData.classroomId = d.classroomId
      formData.semester = d.semester || ''
      formData.capacityLimit = d.capacityLimit || 30
      formData.status = d.status
      dialogVisible.value = true
    }
  } catch (e) { ElMessage.error('获取教学班详情失败') }
}

const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm(`确定关闭教学班「${row.courseName || row.sectionCode}」？关闭后学生将无法选课。`, '关闭确认', { type: 'warning' })
    const res = await adminNewApi.closeSection(row.sectionId)
    if (res?.status === 200) { ElMessage.success('关闭成功'); fetchData() }
    else ElMessage.error(res?.msg || '关闭失败')
  } catch {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除教学班「${row.courseName || row.sectionCode}」？`, '删除确认', { type: 'warning' })
    const res = await adminNewApi.deleteSection(row.sectionId)
    if (res?.status === 200) { ElMessage.success('删除成功'); fetchData() }
    else ElMessage.error(res?.msg || '删除失败')
  } catch {}
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = {
      courseId: formData.courseId,
      teacherId: formData.teacherId,
      classroomId: formData.classroomId,
      semester: formData.semester,
      capacityLimit: formData.capacityLimit,
      status: formData.status
    }
    if (dialogType.value === 'add') {
      payload.sectionCode = formData.sectionCode
    }
    let res
    if (dialogType.value === 'add') {
      res = await adminNewApi.addSection(payload)
    } else {
      res = await adminNewApi.updateSection(formData.sectionId, payload)
    }
    if (res?.status === 200) {
      ElMessage.success(dialogType.value === 'add' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res?.msg || '保存失败')
    }
  } catch (e) {
    console.error('保存教学班失败:', e)
    ElMessage.error('保存失败')
  } finally { submitting.value = false }
}

onMounted(() => { loadOptions(); fetchData() })
</script>

<style lang="scss" scoped>
.sections-container { padding: 24px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); } }
.search-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.data-card { border: 1px solid var(--el-border-color-darker); }
:deep(.el-select) { --el-fill-color-blank: var(--input-bg, #313346); }
</style>
