<template>
  <div class="departments-container">
    <div class="page-header"><h2>院系管理</h2></div>
    <div class="action-bar">
      <el-input v-model="searchKeyword" placeholder="搜索院系名称" style="width: 240px" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增院系</el-button>
    </div>
    <el-card class="data-card" v-loading="loading">
      <el-table :data="filteredData" style="width: 100%" empty-text="暂无院系数据">
        <el-table-column prop="departmentId" label="编号" min-width="100" align="center" />
        <el-table-column prop="departmentName" label="院系名称" min-width="200" />
        <el-table-column label="状态" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" type="danger" link @click="handleDisable(row)">停用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增院系' : '编辑院系'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="院系名称" prop="departmentName">
          <el-input v-model="formData.departmentName" placeholder="请输入院系名称" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { adminNewApi } from '@/api/new-api'

const loading = ref(false), submitting = ref(false), dialogVisible = ref(false), dialogType = ref('add')
const formRef = ref(null), searchKeyword = ref(''), tableData = ref([])
const formData = reactive({ departmentId: null, departmentName: '', status: 1 })
const rules = { departmentName: [{ required: true, message: '请输入院系名称', trigger: 'blur' }] }

const filteredData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(r => (r.departmentName || '').toLowerCase().includes(kw))
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminNewApi.getDepartmentList()
    if (res?.status === 200) tableData.value = res.data || []
    else ElMessage.error(res?.msg || '获取失败')
  } catch (e) { ElMessage.error('获取院系列表失败') }
  finally { loading.value = false }
}

const resetForm = () => { formRef.value?.resetFields(); Object.assign(formData, { departmentId: null, departmentName: '', status: 1 }) }
const handleAdd = () => { dialogType.value = 'add'; resetForm(); dialogVisible.value = true }
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  formData.departmentId = row.departmentId
  formData.departmentName = row.departmentName
  formData.status = row.status
  dialogVisible.value = true
}
const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(`确定停用院系「${row.departmentName}」？`, '停用确认', { type: 'warning' })
    const res = await adminNewApi.disableDepartment(row.departmentId)
    if (res?.status === 200) { ElMessage.success('停用成功'); fetchData() }
    else ElMessage.error(res?.msg || '停用失败')
  } catch {}
}
const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = { departmentName: formData.departmentName, status: formData.status }
    let res
    if (dialogType.value === 'add') res = await adminNewApi.addDepartment(payload)
    else res = await adminNewApi.updateDepartment(formData.departmentId, payload)
    if (res?.status === 200) { ElMessage.success(res.msg || '保存成功'); dialogVisible.value = false; fetchData() }
    else ElMessage.error(res?.msg || '保存失败')
  } catch (e) { ElMessage.error('保存失败') }
  finally { submitting.value = false }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.departments-container { padding: 24px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); } }
.action-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.data-card { border: 1px solid var(--el-border-color-darker); }
:deep(.el-select) { --el-fill-color-blank: var(--input-bg, #313346); }
</style>
