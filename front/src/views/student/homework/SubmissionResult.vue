<template>
  <el-card>
    <div slot="header">作业批改结果</div>

    <div style="margin-bottom:12px">
      <el-descriptions :column="2">
        <el-descriptions-item label="提交时间">{{ submission.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="是否逾期">{{ submission.isLate ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ submission.status }}</el-descriptions-item>
        <el-descriptions-item label="最终得分">{{ submission.finalScore }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <el-divider>逐题得分</el-divider>
    <el-table :data="perQuestion" style="width:100%">
      <el-table-column prop="questionId" label="题目ID" width="90"/>
      <el-table-column prop="content" label="题干"/>
      <el-table-column prop="maxScore" label="满分" width="100"/>
      <el-table-column prop="scoreGiven" label="得分" width="100"/>
      <el-table-column prop="comment" label="教师评语"/>
    </el-table>

    <el-divider>知识点掌握</el-divider>
    <el-table :data="perKnowledge" style="width:100%">
      <el-table-column prop="name" label="知识点" width="200"/>
      <el-table-column prop="studentScore" label="得分" width="120"/>
      <el-table-column prop="maxScore" label="满分" width="120"/>
      <el-table-column prop="percent" label="掌握率(%)" width="120"/>
      <el-table-column label="是否掌握" width="120">
        <template #default="{ row }">
          <el-tag :type="row.mastery ? 'success' : 'warning'">{{ row.mastery ? '掌握' : '未掌握' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from '@/api/axios'

const route = useRoute()
const submissionId = route.params.submissionId

const submission = ref({})
const perQuestion = ref([])
const perKnowledge = ref([])

onMounted(async () => {
  try {
    const res = await axios.get(`/submissions/${submissionId}/detail`)
    const data = res.data.data
    submission.value = data.submission
    perQuestion.value = data.perQuestion
    perKnowledge.value = data.perKnowledge
  } catch (e) {
    console.error(e)
  }
})
</script>
