<template>
  <div class="layout">
    <el-header class="header">
      <div class="brand">
        <span class="brand-name">TodoAdmin</span>
        <el-tag size="small" effect="plain">Vue3 + Element Plus</el-tag>
      </div>
      <div class="user-area">
        <span class="username">欢迎，{{ userStore.user?.username }}</span>
        <el-button size="small" type="danger" plain @click="onLogout">退出登录</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <TaskStats />
      <StatsCharts />
      <TaskFilter v-model="filter" />

      <el-card shadow="never">
        <template #header>
          <div class="table-header">
            <span>任务列表（{{ filteredTasks.length }} 条）</span>
            <el-button type="primary" :icon="Plus" @click="onCreate">新增任务</el-button>
          </div>
        </template>

        <el-table v-loading="taskStore.loading" :data="pagedTasks" :row-class-name="rowClass">
          <el-table-column type="expand">
            <template #default="{ row }">
              <TodoItem :task="row" />
            </template>
          </el-table-column>
          <el-table-column prop="text" label="任务内容" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="{ 'done-text': row.completed }">{{ row.text }}</span>
              <el-tag v-if="isOverdue(row)" size="small" type="danger" class="ml-6">已过期</el-tag>
              <el-tag v-else-if="isDueSoon(row)" size="small" type="warning" class="ml-6">即将到期</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="优先级" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="PRIORITY_TAG_TYPE[row.priority]" size="small">
                {{ PRIORITY_TEXT[row.priority] }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="执行人" width="110" align="center">
            <template #default="{ row }">{{ taskStore.userName(row.assigneeId) }}</template>
          </el-table-column>
          <el-table-column label="截止时间" width="160" align="center">
            <template #default="{ row }">{{ formatDate(row.dueDate) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.completed ? 'success' : 'info'" size="small">
                {{ row.completed ? '已完成' : '未完成' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="230" align="center" fixed="right">
            <template #default="{ row }">
              <el-button
                size="small"
                :type="row.completed ? 'warning' : 'success'"
                plain
                @click="onToggle(row)"
              >
                {{ row.completed ? '标记未完成' : '完成' }}
              </el-button>
              <el-button size="small" type="primary" plain @click="onEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该任务？" @confirm="onDelete(row)">
                <template #reference>
                  <el-button size="small" type="danger" plain>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无匹配任务" />
          </template>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="filteredTasks.length"
            layout="total, prev, pager, next"
            background
          />
        </div>
      </el-card>
    </el-main>

    <TaskEditDialog v-model:visible="dialogVisible" :task="editingTask" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import type { Task } from '../api'
import { useTaskStore } from '../store/task'
import { useUserStore } from '../store/user'
import { formatDate, isOverdue, isDueSoon, PRIORITY_TEXT, PRIORITY_TAG_TYPE } from '../utils/format'
import { useSse } from '../composables/useSse'
import TaskStats from '../components/TaskStats.vue'
import StatsCharts from '../components/StatsCharts.vue'
import TaskFilter from '../components/TaskFilter.vue'
import type { FilterModel } from '../components/TaskFilter.vue'
import TodoItem from '../components/TodoItem.vue'
import TaskEditDialog from '../components/TaskEditDialog.vue'

const router = useRouter()
const taskStore = useTaskStore()
const userStore = useUserStore()

const filter = ref<FilterModel>({ keyword: '', status: 'all', priority: 'all', sort: 'date-asc' })
const page = ref(1)
const pageSize = 6
const dialogVisible = ref(false)
const editingTask = ref<Task | null>(null)

/** 关键词 + 状态 + 优先级叠加过滤，再按所选方式排序 */
const filteredTasks = computed(() => {
  const uid = userStore.user?.id ?? -1
  const list = taskStore.tasks.filter((t) => {
    const kw = filter.value.keyword.trim().toLowerCase()
    if (kw && !t.text.toLowerCase().includes(kw)) return false
    if (filter.value.priority !== 'all' && t.priority !== filter.value.priority) return false
    switch (filter.value.status) {
      case 'done':
        return t.completed
      case 'undone':
        return !t.completed
      case 'myCreate':
        return t.userId === uid
      case 'myAssign':
        return t.assigneeId === uid
      default:
        return true
    }
  })
  const priorityOrder = ['high', 'medium', 'low']
  return [...list].sort((a, b) => {
    switch (filter.value.sort) {
      case 'date-asc':
        return new Date(a.dueDate ?? '9999-12-31').getTime() - new Date(b.dueDate ?? '9999-12-31').getTime()
      case 'date-desc':
        return new Date(b.dueDate ?? '9999-12-31').getTime() - new Date(a.dueDate ?? '9999-12-31').getTime()
      case 'pri-desc':
        return priorityOrder.indexOf(a.priority) - priorityOrder.indexOf(b.priority)
      case 'create-desc':
        return new Date(b.createdAt ?? 0).getTime() - new Date(a.createdAt ?? 0).getTime()
      default:
        return 0
    }
  })
})

const pagedTasks = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredTasks.value.slice(start, start + pageSize)
})

// 过滤条件变化时回到第一页，避免停留在空白页
watch(filter, () => { page.value = 1 }, { deep: true })

function rowClass({ row }: { row: Task }) {
  return isOverdue(row) ? 'overdue-row' : ''
}

function onCreate() {
  editingTask.value = null
  dialogVisible.value = true
}

function onEdit(task: Task) {
  editingTask.value = task
  dialogVisible.value = true
}

async function onToggle(task: Task) {
  await taskStore.updateTask(task.id, { completed: !task.completed })
}

async function onDelete(task: Task) {
  await taskStore.deleteTask(task.id)
}

async function onLogout() {
  await userStore.logout()
  router.push('/login')
}

onMounted(() => {
  taskStore.fetchTasks()
  taskStore.fetchUsers()
})

// SSE：任务变更时静默刷新列表（提示由 composable 内 ElNotification 负责）
useSse(() => taskStore.fetchTasks())
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  color: #409eff;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main > :deep(.el-card) {
  margin-bottom: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.done-text {
  color: #a8abb2;
  text-decoration: line-through;
}

.ml-6 {
  margin-left: 6px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

:deep(.overdue-row) {
  background: #fef0f0;
}
</style>
