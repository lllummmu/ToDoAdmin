<template>
  <el-select
    :model-value="modelValue"
    placeholder="选择任务执行人"
    clearable
    class="w-full"
    @update:model-value="onChange"
  >
    <el-option v-for="u in users" :key="u.id" :label="u.username" :value="u.id" />
  </el-select>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useTaskStore } from '../store/task'

// 任务指派组件：独立封装用户下拉，供新增/编辑任务复用
defineProps<{ modelValue: number | null }>()
const emit = defineEmits<{ 'update:modelValue': [value: number | null] }>()

const taskStore = useTaskStore()
const { users } = storeToRefs(taskStore)

onMounted(() => {
  if (!users.value.length) taskStore.fetchUsers()
})

function onChange(value: number | null) {
  emit('update:modelValue', value ?? null)
}
</script>

<style scoped>
.w-full {
  width: 100%;
}
</style>
