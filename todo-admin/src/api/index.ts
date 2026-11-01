import request from './request'

export interface User {
  id: number
  username: string
}

export type Priority = 'high' | 'medium' | 'low'

export interface Task {
  id: number
  userId: number
  assigneeId: number | null
  text: string
  remark: string | null
  attachment: string | null
  completed: boolean
  priority: Priority
  dueDate: string | null
  createdAt: string | null
  updatedAt: string | null
}

export interface TaskPayload {
  text?: string
  remark?: string | null
  attachment?: string | null
  completed?: boolean
  priority?: Priority
  dueDate?: string | null
  assigneeId?: number | null
}

/** 认证 */
export const loginApi = (username: string, password: string) =>
  request.post<never, User>('/login', { username, password })

export const registerApi = (username: string, password: string) =>
  request.post<never, null>('/register', { username, password })

export const logoutApi = () => request.post<never, null>('/logout')

export const meApi = () => request.get<never, User>('/me')

/** 用户 */
export const usersApi = () => request.get<never, User[]>('/users')

/** 任务 */
export const tasksApi = () => request.get<never, Task[]>('/tasks')

export const createTaskApi = (payload: TaskPayload) =>
  request.post<never, Task>('/tasks', payload)

export const updateTaskApi = (id: number, payload: TaskPayload) =>
  request.put<never, Task>(`/tasks/${id}`, payload)

export const deleteTaskApi = (id: number) =>
  request.delete<never, null>(`/tasks/${id}`)

/** 文件上传 */
export const uploadApi = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return request.post<never, { url: string; name: string; size: number }>('/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
