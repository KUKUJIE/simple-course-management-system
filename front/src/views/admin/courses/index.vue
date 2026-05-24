<template>
  <div class="courses-container">
    <div class="page-header"><h2>课程目录管理</h2></div>

    <div class="action-bar">
      <el-input v-model="searchKeyword" placeholder="搜索课程名/编码" style="width: 240px" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增课程</el-button>
    </div>

    <el-card class="data-card" v-loading="loading">
      <el-table :data="pagedData" style="width: 100%" empty-text="暂无课程数据">
        <el-table-column prop="courseCode" label="课程编码" min-width="130" />
        <el-table-column prop="name" label="课程名称" min-width="180" />
        <el-table-column prop="credit" label="学分" min-width="80" align="center" />
        <el-table-column prop="departmentName" label="所属院系" min-width="140" />
        <el-table-column label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType==='add'?'新增课程':'编辑课程'" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="课程编码" prop="courseCode">
          <el-input v-model="formData.courseCode" :disabled="dialogType==='edit'" placeholder="如 CS101" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="如 高等数学" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="formData.credit" :min="0.5" :max="20" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="所属院系" prop="departmentId">
          <el-select v-model="formData.departmentId" style="width: 100%" placeholder="请选择院系">
            <el-option v-for="d in deptOptions" :key="d.departmentId" :label="d.departmentName" :value="d.departmentId" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" style="width: 100%">
            <el-option label="启用" :value="1" /><el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button plain @click="dialogVisible=false">取消</el-button>
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

const loading = ref(false), submitting = ref(false), dialogVisible = ref(false), dialogType = ref('add')
const formRef = ref(null), searchKeyword = ref(''), tableData = ref([]), deptOptions = ref([])
const getOriginalStatus = ref(null)
const formData = reactive({ id: null, courseCode: '', name: '', credit: 2, departmentId: null, status: 1 })
const rules = {
  courseCode: [{ required: true, message: '请输入课程编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请选择学分', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择所属院系', trigger: 'change' }]
}

const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(r => (r.name||'').toLowerCase().includes(kw) || (r.courseCode||'').toLowerCase().includes(kw))
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

const fetchData = async () => {
  loading.value = true
  currentPage.value = 1
  try {
    const res = await adminNewApi.getCourseList()
    if (res?.status === 200) tableData.value = res.data || []
    else ElMessage.error(res?.msg || '获取课程列表失败')
  } catch (e) { ElMessage.error('获取课程列表失败') }
  finally { loading.value = false }
}

const loadDepts = async () => {
  try {
    const res = await adminNewApi.getDepartmentOptions()
    if (res?.status === 200) deptOptions.value = res.data || []
  } catch { /* ignore */ }
}

const resetForm = () => { formRef.value?.resetFields(); Object.assign(formData, { id: null, courseCode: '', name: '', credit: 2, departmentId: null, status: 1 }); getOriginalStatus.value = null }
const handleAdd = () => { dialogType.value = 'add'; resetForm(); dialogVisible.value = true }
const handleEdit = (row) => {
  dialogType.value = 'edit'
  formData.id = row.id
  formData.courseCode = row.courseCode || ''
  formData.name = row.name || ''
  formData.credit = row.credit || 2
  formData.departmentId = row.departmentId || null
  formData.status = row.status
  getOriginalStatus.value = row.status
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  if (dialogType.value === 'edit' && getOriginalStatus.value !== formData.status) {
    const newStatusText = formData.status === 1 ? '启用' : '停用'
    try {
      await ElMessageBox.confirm(`确认将该课程状态改为"${newStatusText}"？`, '状态变更确认', {
        confirmButtonText: '确认变更', cancelButtonText: '取消', type: 'warning'
      })
    } catch { return }
  }
  submitting.value = true
  try {
    const payload = { courseCode: formData.courseCode, name: formData.name, credit: formData.credit, departmentId: formData.departmentId, status: formData.status }
    let res
    if (dialogType.value === 'add') res = await adminNewApi.addCourse(payload)
    else res = await adminNewApi.updateCourse(formData.id, payload)
    if (res?.status === 200) { ElMessage.success(res.msg || '保存成功'); dialogVisible.value = false; fetchData() }
    else ElMessage.error(res?.msg || '保存失败')
  } catch (e) { ElMessage.error('保存失败') }
  finally { submitting.value = false }
}

onMounted(() => { fetchData(); loadDepts() })
</script>

<style lang="scss" scoped>
@use './style.scss';
.courses-container { padding: 24px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); } }
.action-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.data-card { border: 1px solid var(--el-border-color-darker); }
:deep(.el-select) { --el-fill-color-blank: var(--input-bg, #313346); }
</style>
