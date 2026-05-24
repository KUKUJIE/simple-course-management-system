<template>
  <div class="students-container">
    <div class="page-header"><h2>学生管理</h2></div>

    <div class="action-bar">
      <el-input v-model="searchKeyword" placeholder="搜索学号/姓名" style="width: 260px" clearable @keyup.enter="fetchData" @clear="fetchData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterMajorId" placeholder="按专业筛选" style="width: 180px" clearable @change="fetchData">
        <el-option v-for="m in majorOptions" :key="m.majorId || m.id" :label="m.majorName || m.name" :value="m.majorId || m.id" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="按状态筛选" style="width: 140px" clearable @change="fetchData">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加学生
      </el-button>
    </div>

    <el-card class="data-card" v-loading="loading">
      <el-table :data="pagedData" style="width: 100%" empty-text="暂无学生数据">
        <el-table-column prop="studentNo" label="学号" min-width="120" align="center" />
        <el-table-column prop="studentName" label="姓名" min-width="140" />
        <el-table-column prop="gender" label="性别" width="70" align="center" />
        <el-table-column prop="majorName" label="专业" min-width="150" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50, 100]"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '添加学生' : '编辑学生'" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="formData.studentName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学号" v-if="dialogType === 'add'">
          <el-input v-model="formData.studentNo" placeholder="留空则自动生成学号" />
          <div style="font-size:12px;color:var(--el-text-color-secondary);margin-top:4px">
            建议留空，系统将按专业+年份自动生成唯一学号
          </div>
        </el-form-item>
        <el-form-item label="专业" prop="majorId">
          <el-select v-model="formData.majorId" placeholder="请选择专业" style="width: 100%" filterable>
            <el-option v-for="m in majorOptions" :key="m.majorId || m.id" :label="m.majorName || m.name" :value="m.majorId || m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="formData.gender" style="width: 100%">
            <el-option label="男" value="M" />
            <el-option label="女" value="F" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
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
const filterMajorId = ref(null)
const filterStatus = ref(null)
const tableData = ref([])
const majorOptions = ref([])

const formData = reactive({
  studentId: null, studentNo: '', studentName: '', majorId: null, gender: 'M', status: 1
})

const rules = {
  studentName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  let list = tableData.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r =>
      String(r.studentNo || '').toLowerCase().includes(kw) ||
      (r.studentName || '').toLowerCase().includes(kw)
    )
  }
  return list
})

// 前端分页切片
const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

const loadMajors = async () => {
  try {
    const res = await adminNewApi.getMajorOptions()
    if (res?.status === 200) majorOptions.value = res.data || []
  } catch { /* 下拉加载失败不阻塞 */ }
}

const fetchData = async () => {
  try {
    loading.value = true
    currentPage.value = 1
    const params = {}
    if (filterMajorId.value) params.majorId = filterMajorId.value
    if (filterStatus.value !== null && filterStatus.value !== '') params.status = filterStatus.value
    const res = await adminNewApi.getStudentList(params)
    if (res?.status === 200) tableData.value = res.data || []
    else ElMessage.error(res?.msg || '获取学生列表失败')
  } catch (e) {
    console.error('获取学生列表失败:', e)
    ElMessage.error('获取学生列表失败')
  } finally { loading.value = false }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, { studentId: null, studentNo: '', studentName: '', majorId: null, gender: 'M', status: 1 })
}

const handleAdd = () => { dialogType.value = 'add'; resetForm(); dialogVisible.value = true }

const handleEdit = async (row) => {
  try {
    const res = await adminNewApi.getStudentById(row.studentId)
    if (res?.status === 200) {
      const d = res.data
      dialogType.value = 'edit'
      formData.studentId = d.studentId
      formData.studentNo = d.studentNo || ''
      formData.studentName = d.studentName || d.name || ''
      formData.majorId = d.majorId
      formData.gender = d.gender || 'M'
      formData.status = d.status
      dialogVisible.value = true
    }
  } catch (e) { ElMessage.error('获取学生详情失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除学生「${row.studentName}（${row.studentNo}）」？\n\n⚠ 删除后数据不可恢复，选课及成绩记录将被一并清除。`,
      '⚠ 删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
    const res = await adminNewApi.deleteStudent(row.studentId)
    if (res?.status === 200) { ElMessage.success('已删除'); fetchData() }
    else ElMessage.warning(res?.msg || '无法删除，请先解除关联')
  } catch { }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = {
      studentName: formData.studentName,
      majorId: formData.majorId,
      gender: formData.gender,
      status: formData.status
    }
    if (dialogType.value === 'add' && formData.studentNo) {
      payload.studentNo = String(formData.studentNo)
    }
    let res
    if (dialogType.value === 'add') {
      res = await adminNewApi.addStudent(payload)
    } else {
      res = await adminNewApi.updateStudent(formData.studentId, payload)
    }
    if (res?.status === 200) {
      ElMessage.success(dialogType.value === 'add' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res?.msg || '保存失败')
    }
  } catch (e) {
    console.error('保存学生失败:', e)
    ElMessage.error('保存失败')
  } finally { submitting.value = false }
}

onMounted(() => { loadMajors(); fetchData() })
</script>

<style lang="scss" scoped>
.students-container { padding: 24px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); } }
.action-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.data-card { border: 1px solid var(--el-border-color-darker); }
:deep(.el-select) { --el-fill-color-blank: var(--input-bg, #313346); }
</style>
