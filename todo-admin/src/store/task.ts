import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { Task, TaskPayload, User } from '../api'
import {
  createTaskApi,
  deleteTaskApi,
  tasksApi,
  updateTaskApi,
  usersApi
} from '../api'

/** 任务状态：列表、用户列表、增删改查动作与统计派生数据 */
export const useTaskStore = defineStore('task', () => {
  const tasks = ref<Task[]>([])
  const users = ref<User[]>([])
  const loading = ref(false)

  async function fetchTasks() {
    loading.value = true
    try {
      tasks.value = await tasksApi()
    } finally {
      loading.value = false
    }
  }

  async function fetchUsers() {
    users.value = await usersApi()
  }

  async function createTask(payload: TaskPayload) {
    const created = await createTaskApi(payload)
    tasks.value.unshift(created)
    return created
  }

  async function updateTask(id: number, payload: TaskPayload) {
    const updated = await updateTaskApi(id, payload)
    const idx = tasks.value.findIndex((t) => t.id === id)
    if (idx >= 0) tasks.value[idx] = updated
    return updated
  }

  async function deleteTask(id: number) {
    await deleteTaskApi(id)
    tasks.value = tasks.value.filter((t) => t.id !== id)
  }

  function userName(id: number | null) {
    if (id == null) return '—'
    return users.value.find((u) => u.id === id)?.username ?? `#${id}`
  }

  /** 统计：24 小时内到期且未完成 = 即将到期 */
  const stats = computed(() => {
    const now = Date.now()
    const all = tasks.value
    return {
      total: all.length,
      done: all.filter((t) => t.completed).length,
      undone: all.filter((t) => !t.completed).length,
      dueSoon: all.filter(
        (t) =>
          !t.completed &&
          t.dueDate &&
          new Date(t.dueDate).getTime() > now &&
          new Date(t.dueDate).getTime() - now < 24 * 3600 * 1000
      ).length,
      overdue: all.filter(
        (t) => !t.completed && t.dueDate && new Date(t.dueDate).getTime() < now
      ).length
    }
  })

  /** 当前登录用户的个人概况 */
  const myStats = computed(() => {
    return (userId: number) => {
      const created = tasks.value.filter((t) => t.userId === userId)
      const assigned = tasks.value.filter((t) => t.assigneeId === userId)
      const doneCount = created.filter((t) => t.completed).length
      return {
        created: created.length,
        assigned: assigned.length,
        percent: created.length ? Math.round((doneCount / created.length) * 100) : 0
      }
    }
  })

  return {
    tasks,
    users,
    loading,
    stats,
    myStats,
    userName,
    fetchTasks,
    fetchUsers,
    createTask,
    updateTask,
    deleteTask
  }
})
