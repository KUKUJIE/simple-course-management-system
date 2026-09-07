<template>
  <el-card>
    <div slot="header">批阅提交</div>

    <div>学生ID：{{ submission.studentId }}</div>
    <div>提交时间：{{ submission.submitTime }}</div>
    <div v-if="submission.attachmentPath">
      <a :href="downloadUrl">下载附件</a>
    </div>
    <el-form :model="gradeForm">
      <el-form-item v-for="(q, idx) in questions" :key="idx" :label="`题目 ${q.seq}`">
        <div>题目内容：{{ q.content }}</div>
        <el-input-number v-model="gradeForm.scores[idx]" :min="0" :max="q.score" />
        <el-input type="textarea" v-model="gradeForm.comments[idx]" rows="3" placeholder="评语" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitGrade">提交批阅</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const homeworkId = route.params.homeworkId
const submissionId = route.params.submissionId

const submission = ref({})
const questions = ref([])

const gradeForm = ref({ scores: [], comments: [] })

onMounted(async () => {
  try {
    const res = await axios.get(`/api/homeworks/submissions/${submissionId}`)
    submission.value = res.data
    // fetch questions for the homework
    const qres = await axios.get(`/api/homeworks/${homeworkId}`)
    // assume questions are available in homeworkDetail.questions (currently backend doesn't return questions yet)
    questions.value = qres.data.questions || []
    gradeForm.value.scores = questions.value.map(q => 0)
    gradeForm.value.comments = questions.value.map(q => '')
  } catch (e) {
    console.error(e)
  }
})

function submitGrade() {
  const payload = {
    graderId: localStorage.getItem('uid') || 1,
    answers: questions.value.map((q, idx) => ({
      questionId: q.questionId,
      scoreGiven: gradeForm.value.scores[idx],
      comment: gradeForm.value.comments[idx]
    }))
  }
  axios.post(`/api/submissions/${submissionId}/grade`, payload).then(() => {
    this.$message.success('批阅已保存')
    router.push(`/teacher/homeworks/${homeworkId}/submissions`)
  }).catch(err => {
    console.error(err)
    this.$message.error('批阅失败')
  })
}

const downloadUrl = `/api/files/download?path=${encodeURIComponent(submission.value.attachmentPath || '')}`
</script>
