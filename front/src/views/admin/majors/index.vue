<template>
  <div class="majors-container">
    <div class="page-header"><h2>专业管理</h2></div>
    <div class="action-bar">
      <el-input v-model="searchKeyword" placeholder="搜索专业名称" style="width: 240px" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增专业</el-button>
    </div>
    <el-card class="data-card" v-loading="loading">
      <el-table :data="filteredData" style="width: 100%" empty-text="暂无专业数据">
        <el-table-column prop="major_id" label="编号" min-width="100" align="center" />
        <el-table-column prop="major_name" label="专业名称" min-width="200" />
        <el-table-column prop="department_name" label="所属院系" min-width="160" />
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增专业' : '编辑专业'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="formData.majorName" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="所属院系" prop="departmentId">
          <el-select v-model="formData.departmentId" placeholder="请选择院系" style="width: 100%" filterable>
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
        <el-button plain @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { adminNewApi } from '@/api/new-api'

const loading = ref(false), submitting = ref(false), dialogVisible = ref(false), dialogType = ref('add')
const formRef = ref(null), searchKeyword = ref(''), tableData = ref([]), deptOptions = ref([])
const formData = reactive({ majorId: null, majorName: '', departmentId: null, status: 1 })
const rules = {
  majorName: [{ required: true, message: '请输入专业名称', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择院系', trigger: 'change' }]
}

const filteredData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(r => (r.major_name || '').toLowerCase().includes(kw))
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminNewApi.getMajorList()
    if (res?.status === 200) tableData.value = res.data || []
    else ElMessage.error(res?.msg || '获取失败')
  } catch (e) { ElMessage.error('获取专业列表失败') }
  finally { loading.value = false }
}

const loadDepts = async () => {
  try {
    const res = await adminNewApi.getDepartmentList()
    if (res?.status === 200) deptOptions.value = res.data || []
  } catch {}
}

const resetForm = () => { formRef.value?.resetFields(); Object.assign(formData, { majorId: null, majorName: '', departmentId: null, status: 1 }) }
const handleAdd = () => { dialogType.value = 'add'; resetForm(); dialogVisible.value = true }
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  formData.majorId = row.major_id
  formData.majorName = row.major_name
  formData.departmentId = row.department_id
  formData.status = row.status
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = { majorName: formData.majorName, departmentId: formData.departmentId, status: formData.status }
    let res
    if (dialogType.value === 'add') res = await adminNewApi.addMajor(payload)
    else res = await adminNewApi.updateMajor(formData.majorId, payload)
    if (res?.status === 200) { ElMessage.success(res.msg || '保存成功'); dialogVisible.value = false; fetchData() }
    else ElMessage.error(res?.msg || '保存失败')
  } catch (e) { ElMessage.error('保存失败') }
  finally { submitting.value = false }
}

onMounted(() => { fetchData(); loadDepts() })
</script>

<style lang="scss" scoped>
.majors-container { padding: 24px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); } }
.action-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.data-card { border: 1px solid var(--el-border-color-darker); }
:deep(.el-select) { --el-fill-color-blank: var(--input-bg, #313346); }
</style>
