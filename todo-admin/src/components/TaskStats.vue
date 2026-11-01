<template>
  <el-row :gutter="16" class="stats-row">
    <el-col :xs="12" :sm="6" :md="3" v-for="card in cards" :key="card.label">
      <el-card shadow="hover" class="stat-card" :body-style="{ padding: '16px' }">
        <div class="stat-label">{{ card.label }}</div>
        <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
      </el-card>
    </el-col>
    <el-col :xs="24" :sm="12" :md="6">
      <el-card shadow="hover" class="stat-card" :body-style="{ padding: '16px' }">
        <div class="stat-label">我的概况（创建 / 被指派 / 完成率）</div>
        <div class="stat-value mine">
          {{ mine.created }} / {{ mine.assigned }} / {{ mine.percent }}%
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useTaskStore } from '../store/task'
import { useUserStore } from '../store/user'

const taskStore = useTaskStore()
const userStore = useUserStore()
const { stats } = storeToRefs(taskStore)

const mine = computed(() => taskStore.myStats(userStore.user?.id ?? -1))

const cards = computed(() => [
  { label: '总任务', value: stats.value.total, color: '#409eff' },
  { label: '已完成', value: stats.value.done, color: '#67c23a' },
  { label: '未完成', value: stats.value.undone, color: '#e6a23c' },
  { label: '即将到期', value: stats.value.dueSoon, color: '#f56c6c' },
  { label: '已过期', value: stats.value.overdue, color: '#f78989' }
])
</script>

<style scoped>
.stats-row {
  row-gap: 12px;
}

.stat-card {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
}

.stat-value.mine {
  font-size: 22px;
  color: #409eff;
}
</style>
