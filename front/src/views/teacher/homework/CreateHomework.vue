<template>
  <el-card>
    <div slot="header">创建作业</div>

    <el-form :model="form" label-width="120px">
      <el-form-item label="标题">
        <el-input v-model="form.title" />
      </el-form-item>

      <el-form-item label="描述">
        <el-input type="textarea" v-model="form.description" rows="4" />
      </el-form-item>

      <el-form-item label="截止时间">
        <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="允许逾期提交">
        <el-switch v-model="form.allowLate" active-value="1" inactive-value="0" />
      </el-form-item>

      <el-form-item label="提交类型">
        <el-select v-model="form.allowSubmissionTypes" placeholder="提交类型">
          <el-option label="文件或文本" value="both" />
          <el-option label="仅文件" value="file" />
          <el-option label="仅在线文本" value="text" />
        </el-select>
      </el-form-item>

      <el-form-item label="总分">
        <el-input-number v-model="form.totalScore" :min="0" />
      </el-form-item>

      <el-divider>题目</el-divider>

      <div v-for="(q, idx) in form.questions" :key="idx" style="border:1px solid #eee;padding:12px;margin-bottom:8px">
        <div style="display:flex;gap:8px;align-items:center;margin-bottom:8px">
          <el-input-number v-model="q.seq" :min="1" />
          <el-select v-model="q.qtype" placeholder="题型" style="width:160px">
            <el-option label="主观题" value="essay" />
            <el-option label="选择题" value="mcq" />
          </el-select>
          <el-input-number v-model="q.score" :min="0" />
          <el-button type="danger" size="mini" @click="removeQuestion(idx)">删除</el-button>
        </div>
        <el-form-item label="题干">
          <el-input type="textarea" v-model="q.content" rows="3" />
        </el-form-item>
        <el-form-item label="知识点（可选）">
          <el-input v-model="q.knowledgePointName" placeholder="例如：矩阵运算" />
        </el-form-item>
      </div>

      <el-button type="primary" @click="addQuestion">添加题目</el-button>

      <el-form-item style="margin-top:16px">
        <el-button type="primary" @click="submit">创建</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { createHomework } from '@/api/homework'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = ref({
  title: '',
  description: '',
  deadline: null,
  allowLate: 1,
  allowSubmissionTypes: 'both',
  totalScore: 100,
  questions: []
})

function addQuestion() {
  form.value.questions.push({ seq: form.value.questions.length + 1, qtype: 'essay', content: '', score: 0, knowledgePointName: '' })
}

function removeQuestion(idx) {
  form.value.questions.splice(idx, 1)
}

async function submit() {
  // prepare payload
  const payload = {
    sectionId: null,
    title: form.value.title,
    description: form.value.description,
    deadline: form.value.deadline ? new Date(form.value.deadline).toISOString() : null,
    allowLate: form.value.allowLate,
    totalScore: form.value.totalScore,
    allowSubmissionTypes: form.value.allowSubmissionTypes,
    questions: form.value.questions.map(q => ({ seq: q.seq, qtype: q.qtype, content: q.content, score: q.score, knowledgePointName: q.knowledgePointName }))
  }

  try {
    const res = await createHomework(payload)
    ElMessage.success('作业创建成功')
    const hwId = res.data.data.homeworkId
    router.push(`/teacher/homeworks/${hwId}/submissions`)
  } catch (e) {
    console.error(e)
    const msg = e.response && e.response.data && e.response.data.msg ? e.response.data.msg : '创建失败'
    ElMessage.error(msg)
  }
}
</script>
