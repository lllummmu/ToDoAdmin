<template>
  <el-card shadow="never" class="filter-card">
    <el-row :gutter="12">
      <el-col :xs="24" :sm="8" :md="6">
        <el-input
          v-model="model.keyword"
          placeholder="搜索任务内容关键词"
          clearable
          :prefix-icon="Search"
        />
      </el-col>
      <el-col :xs="12" :sm="8" :md="5">
        <el-select v-model="model.status" class="w-full">
          <el-option label="所有任务" value="all" />
          <el-option label="已完成任务" value="done" />
          <el-option label="未完成任务" value="undone" />
          <el-option label="自己创建的任务" value="myCreate" />
          <el-option label="指派给我的任务" value="myAssign" />
        </el-select>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-select v-model="model.priority" class="w-full">
          <el-option label="所有优先级" value="all" />
          <el-option label="高优先级" value="high" />
          <el-option label="中优先级" value="medium" />
          <el-option label="低优先级" value="low" />
        </el-select>
      </el-col>
      <el-col :xs="12" :sm="8" :md="5">
        <el-select v-model="model.sort" class="w-full">
          <el-option label="按截止时间升序" value="date-asc" />
          <el-option label="按截止时间降序" value="date-desc" />
          <el-option label="按优先级排序" value="pri-desc" />
          <el-option label="按创建时间排序" value="create-desc" />
        </el-select>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-button class="w-full" :icon="RefreshLeft" @click="onReset">重置</el-button>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup lang="ts">
import { Search, RefreshLeft } from '@element-plus/icons-vue'

export interface FilterModel {
  keyword: string
  status: string
  priority: string
  sort: string
}

// defineModel 双向绑定筛选条件，条件之间可叠加
const model = defineModel<FilterModel>({ required: true })

function onReset() {
  model.value = { keyword: '', status: 'all', priority: 'all', sort: 'date-asc' }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.w-full {
  width: 100%;
}
</style>
