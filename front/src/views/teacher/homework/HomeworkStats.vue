<template>
  <el-card>
    <div slot="header">作业统计</div>
    <div style="display:flex; gap:20px; flex-wrap:wrap">
      <div style="width:60%">
        <div ref="chartBar" style="width:100%;height:400px"></div>
      </div>
      <div style="width:35%">
        <div ref="chartPie" style="width:100%;height:400px"></div>
      </div>
    </div>
    <div style="margin-top:20px">
      <el-table :data="perQuestionList" style="width:100%">
        <el-table-column prop="questionId" label="题目ID" width="120"/>
        <el-table-column prop="avg" label="平均得分"/>
      </el-table>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import axios from '@/api/axios'
import { useRoute } from 'vue-router'

const route = useRoute()
const homeworkId = route.params.id || route.params.homeworkId

const chartBar = ref(null)
const chartPie = ref(null)
let barChart = null
let pieChart = null

const perQuestionList = ref([])

onMounted(async () => {
  try {
    const res = await axios.get(`/statistics/homework/${homeworkId}/overview`)
    const data = res.data.data
    const distribution = data.distribution || [0,0,0,0,0]
    const categories = ['0-59','60-69','70-79','80-89','90-100']

    barChart = echarts.init(chartBar.value)
    const barOpt = {
      title: { text: '分数分布' },
      tooltip: {},
      xAxis: { type: 'category', data: categories },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: distribution }]
    }
    barChart.setOption(barOpt)

    pieChart = echarts.init(chartPie.value)
    const passRate = data.passRate || 0
    const pieOpt = {
      title: { text: '及格率' },
      tooltip: { trigger: 'item' },
      series: [
        {
          name: '及格',
          type: 'pie',
          radius: '50%',
          data: [
            { value: Math.round(passRate), name: '及格(%)' },
            { value: Math.round(100 - passRate), name: '不及格(%)' }
          ]
        }
      ]
    }
    pieChart.setOption(pieOpt)

    const perQuestion = data.perQuestionAvg || {}
    perQuestionList.value = Object.keys(perQuestion).map(k => ({ questionId: k, avg: perQuestion[k] }))
  } catch (e) {
    console.error(e)
  }
})
</script>
