<template>
  <el-card>
    <div slot="header">提交作业</div>

    <el-form :model="form" label-width="90px">
      <el-form-item label="在线作答">
        <el-input type="textarea" v-model="form.submitText" rows="6" />
      </el-form-item>

      <el-form-item label="附件上传">
        <el-upload
          :before-upload="beforeUpload"
          :on-change="handleChange"
          :show-file-list="false"
          :limit="1"
        >
          <el-button size="small">选择文件上传</el-button>
        </el-upload>
        <div v-if="fileName">已选择：{{ fileName }}</div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submit">提交</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { submitHomework } from '@/api/homework'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const homeworkId = route.params.id

const form = ref({ submitText: '' })
let currentFile = null
const fileName = ref('')

function beforeUpload(file) {
  currentFile = file
  fileName.value = file.name
  return false // prevent auto-upload
}

function handleChange(file) {
  currentFile = file
}

async function submit() {
  const fd = new FormData()
  fd.append('studentId', localStorage.getItem('uid') || '2')
  fd.append('submitText', form.value.submitText)
  if (currentFile) fd.append('file', currentFile)
  try {
    await submitHomework(homeworkId, fd)
    this.$message.success('提交成功')
    router.push('/student/homeworks')
  } catch (e) {
    console.error(e)
    this.$message.error('提交失败')
  }
}
</script>
