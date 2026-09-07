<template>
  <div class="homework-list">
    <el-card>
      <div slot="header">学生作业列表</div>
      <el-table :data="homeworks" style="width: 100%">
        <el-table-column prop="homeworkId" label="ID" width="80"/>
        <el-table-column prop="title" label="标题"/>
        <el-table-column prop="deadline" label="截止时间"/>
        <el-table-column label="操作" width="240">
          <template #default="{row}">
            <el-button size="mini" type="primary" @click="goSubmit(row)">提交作业</el-button>
            <el-button size="mini" @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listHomeworks } from '@/api/homework'
import { useRouter } from 'vue-router'

const homeworks = ref([])
const router = useRouter()

onMounted(async () => {
  try {
    const res = await listHomeworks({})
    homeworks.value = res.data
  } catch (e) {
    console.error(e)
  }
})

function goSubmit(row) {
  router.push({ path: `/student/homeworks/${row.homeworkId}/submit` })
}

function viewDetail(row) {
  router.push({ path: `/student/homeworks/${row.homeworkId}/result` })
}
</script>
