import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

/** 后端统一响应结构 */
export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 响应拦截器：统一拆包 {code,msg,data}；401 跳登录；错误统一提示
request.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult<unknown>
    if (result && typeof result.code === 'number') {
      if (result.code === 0) return result.data as never
      ElMessage.error(result.msg || '请求失败')
      return Promise.reject(new Error(result.msg))
    }
    return response.data as never
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.msg || error.message || '网络异常'
    // 会话探测接口静默失败（由路由守卫决定跳转），避免首屏误报"登录已失效"
    const silent = String(error.config?.url || '').includes('/me')
    if (status === 401) {
      if (!silent && router.currentRoute.value.name !== 'login') {
        ElMessage.warning('登录已失效，请重新登录')
        router.push({ name: 'login' })
      }
    } else if (!silent) {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default request
