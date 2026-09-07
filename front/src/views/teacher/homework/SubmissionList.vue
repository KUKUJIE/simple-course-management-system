<template>
  <el-card>
    <div slot="header">教师：作业提交列表</div>
    <el-table :data="submissions" style="width: 100%">
      <el-table-column prop="submissionId" label="ID" width="80"/>
      <el-table-column prop="studentId" label="学生ID"/>
      <el-table-column prop="submitTime" label="提交时间"/>
      <el-table-column prop="isLate" label="是否逾期"/>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="mini" type="primary" @click="grade(row)">批阅</el-button>
          <el-button size="mini" @click="download(row)">下载附件</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listSubmissions } from '@/api/homework'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const homeworkId = route.params.id
const submissions = ref([])

onMounted(async () => {
  try {
    const res = await listSubmissions(homeworkId)
    submissions.value = res.data
  } catch (e) {
    console.error(e)
  }
})

function grade(row) {
  router.push({ path: `/teacher/homeworks/${homeworkId}/submissions/${row.submissionId}` })
}

function download(row) {
  if (!row.attachmentPath) {
    this.$message.info('无附件')
    return
  }
  // url encode
  const url = `/api/files/download?path=${encodeURIComponent(row.attachmentPath)}`
  window.open(url, '_blank')
}
</script>
