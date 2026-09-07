<template>
  <el-card>
    <div slot="header">课程管理</div>
    <div style="margin-bottom:12px">
      <el-form :model="newCourse" inline>
        <el-form-item>
          <el-input v-model="newCourse.courseCode" placeholder="课程编码" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="newCourse.name" placeholder="课程名称" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="newCourse.teacherId" placeholder="教师ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="createCourse">创建课程</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="courses" style="width:100%">
      <el-table-column prop="courseId" label="ID" width="80" />
      <el-table-column prop="courseCode" label="编码" width="160" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="teacherId" label="教师ID" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" size="mini" @click="editCourse(row)">编辑</el-button>
          <el-button type="danger" size="mini" @click="deleteCourse(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/api/axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const courses = ref([])
const newCourse = ref({ courseCode: '', name: '', description: '', teacherId: null })

async function load() {
  const res = await axios.get('/admin/courses')
  courses.value = res.data.data
}

onMounted(load)

async function createCourse() {
  try {
    await axios.post('/admin/courses', newCourse.value)
    ElMessage.success('创建成功')
    newCourse.value = { courseCode: '', name: '', description: '', teacherId: null }
    load()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

function editCourse(row) {
  const name = prompt('课程名称', row.name)
  const code = prompt('课程编码', row.courseCode)
  const teacherId = prompt('教师ID', row.teacherId)
  if (name === null) return
  axios.put(`/admin/courses/${row.courseId}`, { name, courseCode: code, teacherId: teacherId ? parseInt(teacherId) : null }).then(() => { ElMessage.success('更新成功'); load() }).catch(() => ElMessage.error('更新失败'))
}

function deleteCourse(row) {
  ElMessageBox.confirm('确认删除此课程？', '删除确认', { type: 'warning' }).then(() => {
    axios.delete(`/admin/courses/${row.courseId}`).then(() => { ElMessage.success('删除成功'); load() }).catch(() => ElMessage.error('删除失败'))
  }).catch(()=>{})
}
</script>
