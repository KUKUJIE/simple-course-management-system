<template>
  <div class="teachers-container">
    <div class="page-header"><h2>教师管理</h2></div>

    <div class="action-bar">
      <el-input v-model="searchKeyword" placeholder="搜索工号/姓名" style="width: 260px" clearable @keyup.enter="fetchData" @clear="fetchData">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterDeptId" placeholder="按院系筛选" style="width: 180px" clearable @change="fetchData">
        <el-option v-for="d in deptOptions" :key="d.departmentId" :label="d.departmentName" :value="d.departmentId" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="按状态筛选" style="width: 140px" clearable @change="fetchData">
        <el-option label="启用" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加教师
      </el-button>
    </div>

    <el-card class="data-card" v-loading="loading">
      <el-table :data="filteredData" style="width: 100%" empty-text="暂无教师数据">
        <el-table-column prop="teacherNo" label="工号" min-width="120" align="center" />
        <el-table-column prop="teacherName" label="姓名" min-width="140" />
        <el-table-column prop="title" label="职称" min-width="120" />
        <el-table-column prop="departmentName" label="所属院系" min-width="150" />
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
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '添加教师' : '编辑教师'" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="teacherName">
          <el-input v-model="formData.teacherName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="工号" v-if="dialogType === 'add'">
          <el-input v-model="formData.teacherNo" placeholder="留空则自动生成工号" />
          <div style="font-size:12px;color:var(--el-text-color-secondary);margin-top:4px">
            建议留空，系统将按院系+年份自动生成唯一工号
          </div>
        </el-form-item>
        <el-form-item label="院系" prop="departmentId">
          <el-select v-model="formData.departmentId" placeholder="请选择院系" style="width: 100%" filterable>
            <el-option v-for="d in deptOptions" :key="d.departmentId" :label="d.departmentName" :value="d.departmentId" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="formData.title" style="width: 100%">
            <el-option label="教授" value="教授" />
            <el-option label="副教授" value="副教授" />
            <el-option label="讲师" value="讲师" />
            <el-option label="助教" value="助教" />
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
const filterDeptId = ref(null)
const filterStatus = ref(null)
const tableData = ref([])
const deptOptions = ref([])

const formData = reactive({
  teacherId: null, teacherNo: '', teacherName: '', departmentId: null, title: '讲师', status: 1
})

const rules = {
  teacherName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择院系', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const filteredData = computed(() => {
  let list = tableData.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(r =>
      String(r.teacherNo || '').includes(kw) ||
      (r.teacherName || '').toLowerCase().includes(kw)
    )
  }
  return list
})

const loadDepts = async () => {
  try {
    const res = await adminNewApi.getDepartmentOptions()
    if (res?.status === 200) deptOptions.value = res.data || []
  } catch { /* 下拉加载失败不阻塞 */ }
}

const fetchData = async () => {
  try {
    loading.value = true
    const params = {}
    if (filterDeptId.value) params.departmentId = filterDeptId.value
    if (filterStatus.value !== null && filterStatus.value !== '') params.status = filterStatus.value
    const res = await adminNewApi.getTeacherList(params)
    if (res?.status === 200) tableData.value = res.data || []
    else ElMessage.error(res?.msg || '获取教师列表失败')
  } catch (e) {
    console.error('获取教师列表失败:', e)
    ElMessage.error('获取教师列表失败')
  } finally { loading.value = false }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, { teacherId: null, teacherNo: '', teacherName: '', departmentId: null, title: '讲师', status: 1 })
}

const handleAdd = () => { dialogType.value = 'add'; resetForm(); dialogVisible.value = true }

const handleEdit = async (row) => {
  try {
    const res = await adminNewApi.getTeacherById(row.teacherId)
    if (res?.status === 200) {
      const d = res.data
      dialogType.value = 'edit'
      formData.teacherId = d.teacherId
      formData.teacherNo = d.teacherNo || ''
      formData.teacherName = d.teacherName || d.name || ''
      formData.departmentId = d.departmentId
      formData.title = d.title || '讲师'
      formData.status = d.status
      dialogVisible.value = true
    }
  } catch (e) { ElMessage.error('获取教师详情失败') }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除教师「${row.teacherName}（${row.teacherNo}）」？\n\n⚠ 删除后数据不可恢复，授课记录将被一并清除。`,
      '⚠ 删除确认',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'error' }
    )
    const res = await adminNewApi.deleteTeacher(row.teacherId)
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
      teacherName: formData.teacherName,
      departmentId: formData.departmentId,
      title: formData.title,
      status: formData.status
    }
    if (dialogType.value === 'add' && formData.teacherNo) {
      payload.teacherNo = String(formData.teacherNo)
    }
    let res
    if (dialogType.value === 'add') {
      res = await adminNewApi.addTeacher(payload)
    } else {
      res = await adminNewApi.updateTeacher(formData.teacherId, payload)
    }
    if (res?.status === 200) {
      ElMessage.success(dialogType.value === 'add' ? '新增成功' : '修改成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res?.msg || '保存失败')
    }
  } catch (e) {
    console.error('保存教师失败:', e)
    ElMessage.error('保存失败')
  } finally { submitting.value = false }
}

onMounted(() => { loadDepts(); fetchData() })
</script>

<style lang="scss" scoped>
.teachers-container { padding: 24px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); } }
.action-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.data-card { border: 1px solid var(--el-border-color-darker); }

// 下拉框暗色覆盖（与输入框背景一致）
:deep(.el-select) {
  --el-fill-color-blank: var(--input-bg, #313346);
}
</style>
