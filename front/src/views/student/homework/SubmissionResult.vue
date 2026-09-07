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

    <div v-if="perKnowledge.length === 0" style="padding:20px;text-align:center;color:#999">暂无统计数据（请等待教师批阅）</div>

    <div v-else>
      <div style="display:flex; gap:20px; align-items:flex-start; margin-bottom:20px; flex-wrap:wrap">
        <div ref="chartKP" style="width:60%;min-width:320px;height:360px;border:1px solid #f0f0f0;padding:8px"></div>
        <div ref="chartCompare" style="width:35%;min-width:260px;height:360px;border:1px solid #f0f0f0;padding:8px"></div>
      </div>

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
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from '@/api/axios'
import * as echarts from 'echarts'

const route = useRoute()
const submissionId = route.params.submissionId

const submission = ref({})
const perQuestion = ref([])
const perKnowledge = ref([])

const chartKP = ref(null)
const chartCompare = ref(null)
let kpChart = null
let compareChart = null

function renderCharts() {
  if (!perKnowledge.value || perKnowledge.value.length === 0) return

  const names = perKnowledge.value.map(k => k.name)
  const percents = perKnowledge.value.map(k => Number(k.percent))
  const studentScores = perKnowledge.value.map(k => Number(k.studentScore))
  const maxScores = perKnowledge.value.map(k => Number(k.maxScore))

  // Knowledge point mastery bar chart
  if (!kpChart) kpChart = echarts.init(chartKP.value)
  const kpOption = {
    title: { text: '知识点掌握率 (%)', left: 'center' },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [
      {
        name: '掌握率',
        type: 'bar',
        data: percents,
        itemStyle: { color: '#4caf50' }
      }
    ]
  }
  kpChart.setOption(kpOption)

  // Knowledge point student vs max radar/bar
  if (!compareChart) compareChart = echarts.init(chartCompare.value)
  const compareOption = {
    title: { text: '知识点得分对比', left: 'center' },
    tooltip: { },
    legend: { data: ['得分', '满分'], bottom: 0 },
    xAxis: { type: 'category', data: names },
    yAxis: { type: 'value' },
    series: [
      { name: '得分', type: 'bar', data: studentScores, itemStyle: { color: '#2196f3' } },
      { name: '满分', type: 'bar', data: maxScores, itemStyle: { color: '#9e9e9e' } }
    ]
  }
  compareChart.setOption(compareOption)
}

onMounted(async () => {
  try {
    const res = await axios.get(`/submissions/${submissionId}/detail`)
    const data = res.data.data
    submission.value = data.submission
    perQuestion.value = data.perQuestion
    perKnowledge.value = data.perKnowledge || []

    // render charts if data exists
    if (perKnowledge.value.length > 0) {
      // small timeout to ensure DOM is ready
      setTimeout(renderCharts, 50)
    }
  } catch (e) {
    console.error(e)
  }
})

// re-render when perKnowledge changes (in case of async updates)
watch(perKnowledge, (nv) => {
  setTimeout(renderCharts, 50)
})
</script>
