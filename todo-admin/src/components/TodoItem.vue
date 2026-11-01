<template>
  <div class="todo-item">
    <el-descriptions :column="2" size="small" border>
      <el-descriptions-item label="创建人">{{ taskStore.userName(task.userId) }}</el-descriptions-item>
      <el-descriptions-item label="执行人">{{ taskStore.userName(task.assigneeId) }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatDate(task.createdAt) }}</el-descriptions-item>
      <el-descriptions-item label="最后更新">{{ formatDate(task.updatedAt) }}</el-descriptions-item>
    </el-descriptions>

    <div v-if="task.remark" class="remark-block">
      <div class="block-title">任务备注（Markdown 渲染）</div>
      <MarkdownView :content="task.remark" />
    </div>

    <div v-if="task.attachment" class="attachment-block">
      <div class="block-title">任务附件</div>
      <!-- 图片附件直接预览，其他类型提供下载链接 -->
      <el-image
        v-if="isImage"
        :src="attachmentUrl"
        :preview-src-list="[attachmentUrl]"
        fit="contain"
        class="attachment-image"
        preview-teleported
      >
        <template #error>
          <el-link :href="attachmentUrl" target="_blank" type="primary">查看附件</el-link>
        </template>
      </el-image>
      <el-link v-else :href="attachmentUrl" target="_blank" type="primary" :icon="Paperclip">
        {{ fileName }}
      </el-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Paperclip } from '@element-plus/icons-vue'
import type { Task } from '../api'
import { useTaskStore } from '../store/task'
import { formatDate } from '../utils/format'
import MarkdownView from './MarkdownView.vue'

const props = defineProps<{ task: Task }>()
const taskStore = useTaskStore()

const attachmentUrl = computed(() => (props.task.attachment ? `/${props.task.attachment}` : ''))
const fileName = computed(() => props.task.attachment?.split('/').pop() ?? '附件')
const isImage = computed(() => /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(props.task.attachment ?? ''))
</script>

<style scoped>
.todo-item {
  padding: 8px 16px;
}

.block-title {
  font-size: 12px;
  color: #909399;
  margin: 12px 0 6px;
}

.attachment-image {
  max-width: 360px;
  max-height: 220px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}
</style>
