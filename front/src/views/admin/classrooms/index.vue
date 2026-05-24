<template>
  <div class="classrooms-container">
    <div class="page-header"><h2>教室管理</h2></div>
    <div class="action-bar">
      <el-input v-model="searchKeyword" placeholder="搜索教室名称" style="width: 240px" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增教室</el-button>
    </div>
    <el-card class="data-card" v-loading="loading">
      <el-table :data="filteredData" style="width: 100%" empty-text="暂无教室数据">
        <el-table-column prop="classroomId" label="编号" min-width="100" align="center" />
        <el-table-column label="教室名称" min-width="200">
          <template #default="{ row }">{{ row.building }}{{ row.roomNo }}</template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" min-width="120" align="center" />
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

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增教室' : '编辑教室'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="教学楼" prop="building">
          <el-input v-model="formData.building" placeholder="请输入教学楼" />
        </el-form-item>
        <el-form-item label="房间号" prop="roomNo">
          <el-input v-model="formData.roomNo" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="formData.capacity" :min="1" :max="999" style="width: 100%" />
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
const formRef = ref(null), searchKeyword = ref(''), tableData = ref([])
const formData = reactive({ classroomId: null, building: '', roomNo: '', capacity: 60, status: 1 })
const rules = {
  building: [{ required: true, message: '请输入教学楼', trigger: 'blur' }],
  roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

const filteredData = computed(() => {
  if (!searchKeyword.value) return tableData.value
  const kw = searchKeyword.value.toLowerCase()
  return tableData.value.filter(r => ((r.building||'')+(r.roomNo||'')).toLowerCase().includes(kw))
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminNewApi.getClassroomList()
    if (res?.status === 200) tableData.value = res.data || []
    else ElMessage.error(res?.msg || '获取失败')
  } catch (e) { ElMessage.error('获取教室列表失败') }
  finally { loading.value = false }
}

const resetForm = () => { formRef.value?.resetFields(); Object.assign(formData, { classroomId: null, building: '', roomNo: '', capacity: 60, status: 1 }) }
const handleAdd = () => { dialogType.value = 'add'; resetForm(); dialogVisible.value = true }
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  formData.classroomId = row.classroomId
  formData.building = row.building || ''
  formData.roomNo = row.roomNo || ''
  formData.capacity = row.capacity || 60
  formData.status = row.status
  dialogVisible.value = true
}
const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = { building: formData.building, roomNo: formData.roomNo, capacity: formData.capacity, status: formData.status }
    let res
    if (dialogType.value === 'add') res = await adminNewApi.addClassroom(payload)
    else res = await adminNewApi.updateClassroom(formData.classroomId, payload)
    if (res?.status === 200) { ElMessage.success(res.msg || '保存成功'); dialogVisible.value = false; fetchData() }
    else ElMessage.error(res?.msg || '保存失败')
  } catch (e) { ElMessage.error('保存失败') }
  finally { submitting.value = false }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.classrooms-container { padding: 24px; }
.page-header { margin-bottom: 20px; h2 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); } }
.action-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.data-card { border: 1px solid var(--el-border-color-darker); }
:deep(.el-select) { --el-fill-color-blank: var(--input-bg, #313346); }
</style>
