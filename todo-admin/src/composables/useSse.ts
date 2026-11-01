import { onBeforeUnmount, onMounted } from 'vue'
import { ElNotification } from 'element-plus'

/**
 * SSE 实时通知：订阅后端 /api/events，
 * 其他用户（或自己）增删改任务时收到 task-changed 事件并刷新列表。
 */
export function useSse(onTaskChanged: () => void) {
  let source: EventSource | null = null

  onMounted(() => {
    source = new EventSource('/api/events')

    source.addEventListener('task-changed', (event) => {
      try {
        const data = JSON.parse((event as MessageEvent).data)
        const actionText: Record<string, string> = {
          created: '新增',
          updated: '更新',
          deleted: '删除'
        }
        ElNotification({
          title: '任务实时动态',
          message: `${data.by} ${actionText[data.action] ?? '变更'}了一个任务，列表已刷新`,
          type: 'info',
          duration: 3000
        })
      } catch {
        // 忽略非 JSON 心跳等数据
      }
      onTaskChanged()
    })

    source.onerror = () => {
      // EventSource 自带自动重连，无需手动处理
    }
  })

  onBeforeUnmount(() => {
    source?.close()
  })
}
