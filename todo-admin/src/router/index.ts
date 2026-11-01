import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { guest: true } },
    { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue'), meta: { guest: true } },
    { path: '/', name: 'todo', component: () => import('../views/TodoListView.vue'), meta: { auth: true } }
  ]
})

// 登录态路由守卫：未登录访问受保护页面时先尝试用会话恢复，失败则跳登录页
router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (!userStore.loaded) {
    await userStore.fetchMe()
  }
  if (to.meta.auth && !userStore.user) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guest && userStore.user) {
    return { name: 'todo' }
  }
  return true
})

export default router
