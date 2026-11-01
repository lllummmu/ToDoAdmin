import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '../api'
import { loginApi, logoutApi, meApi, registerApi } from '../api'

/** 用户认证状态：登录、注册、会话恢复、退出 */
export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  // 是否已做过首次会话探测（供路由守卫使用）
  const loaded = ref(false)

  async function login(username: string, password: string) {
    user.value = await loginApi(username, password)
    loaded.value = true
  }

  async function register(username: string, password: string) {
    await registerApi(username, password)
  }

  async function fetchMe() {
    try {
      user.value = await meApi()
    } catch {
      user.value = null
    } finally {
      loaded.value = true
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      user.value = null
    }
  }

  return { user, loaded, login, register, fetchMe, logout }
})
