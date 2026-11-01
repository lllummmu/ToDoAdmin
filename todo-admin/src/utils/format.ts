/** 日期与时间工具：纯函数，便于单元测试 */

export function formatDate(iso: string | null): string {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}/${pad(d.getMonth() + 1)}/${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 已过期：有截止时间、早于当前、且未完成 */
export function isOverdue(task: { dueDate: string | null; completed: boolean }, now = Date.now()): boolean {
  return !task.completed && !!task.dueDate && new Date(task.dueDate).getTime() < now
}

/** 即将到期：24 小时内且尚未过期 */
export function isDueSoon(task: { dueDate: string | null; completed: boolean }, now = Date.now()): boolean {
  if (task.completed || !task.dueDate) return false
  const t = new Date(task.dueDate).getTime()
  return t >= now && t - now < 24 * 3600 * 1000
}

export const PRIORITY_TEXT: Record<string, string> = {
  high: '高',
  medium: '中',
  low: '低'
}

export const PRIORITY_TAG_TYPE: Record<string, 'danger' | 'warning' | 'success'> = {
  high: 'danger',
  medium: 'warning',
  low: 'success'
}
