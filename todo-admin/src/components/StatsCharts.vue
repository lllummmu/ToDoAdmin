<template>
  <el-row :gutter="16" class="charts-row">
    <el-col :xs="24" :md="12">
      <el-card shadow="never">
        <template #header><span class="chart-title">任务完成情况</span></template>
        <div ref="pieRef" class="chart"></div>
      </el-card>
    </el-col>
    <el-col :xs="24" :md="12">
      <el-card shadow="never">
        <template #header><span class="chart-title">优先级分布</span></template>
        <div ref="barRef" class="chart"></div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { storeToRefs } from 'pinia'
import { useTaskStore } from '../store/task'

const taskStore = useTaskStore()
const { tasks } = storeToRefs(taskStore)

const pieRef = ref<HTMLDivElement>()
const barRef = ref<HTMLDivElement>()
let pie: echarts.ECharts | null = null
let bar: echarts.ECharts | null = null

function render() {
  const list = tasks.value
  const done = list.filter((t) => t.completed).length
  const undone = list.length - done

  pie?.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        label: { formatter: '{b}: {c}' },
        data: [
          { name: '已完成', value: done, itemStyle: { color: '#67c23a' } },
          { name: '未完成', value: undone, itemStyle: { color: '#e6a23c' } }
        ]
      }
    ]
  })

  const priorities = [
    { key: 'high', name: '高', color: '#f56c6c' },
    { key: 'medium', name: '中', color: '#e6a23c' },
    { key: 'low', name: '低', color: '#67c23a' }
  ]
  bar?.setOption({
    tooltip: {},
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: priorities.map((p) => p.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'bar',
        barWidth: 48,
        data: priorities.map((p) => ({
          value: list.filter((t) => t.priority === p.key).length,
          itemStyle: { color: p.color, borderRadius: [6, 6, 0, 0] }
        }))
      }
    ]
  })
}

function resize() {
  pie?.resize()
  bar?.resize()
}

onMounted(() => {
  if (pieRef.value) pie = echarts.init(pieRef.value)
  if (barRef.value) bar = echarts.init(barRef.value)
  render()
  window.addEventListener('resize', resize)
})

watch(tasks, render, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  pie?.dispose()
  bar?.dispose()
})
</script>

<style scoped>
.charts-row {
  row-gap: 12px;
}

.chart {
  height: 240px;
}

.chart-title {
  font-size: 14px;
  color: #606266;
}
</style>
