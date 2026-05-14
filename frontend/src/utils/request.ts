import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'
import router from '@/router'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加Token（仅当token是有效JWT格式时才添加）
    const token = localStorage.getItem('token')
    if (token && token.includes('.') && token.split('.').length === 3) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    
    // 如果是文件流，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 根据业务状态码处理
    if (res.code === 200 || res.code === undefined) {
      return res.data !== undefined ? res.data : res
    }
    
    // Token过期
    if (res.code === 401) {
      MessagePlugin.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      return Promise.reject(new Error(res.message || '登录已过期'))
    }
    
    // 其他错误
    MessagePlugin.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error: AxiosError) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          MessagePlugin.error('未授权，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          MessagePlugin.error('拒绝访问')
          break
        case 404:
          MessagePlugin.error('请求资源不存在')
          break
        case 500:
          MessagePlugin.error('服务器错误')
          break
        default:
          MessagePlugin.error('请求失败')
      }
    } else if (error.request) {
      MessagePlugin.error('网络连接失败，请检查网络')
    } else {
      MessagePlugin.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default service

// 封装请求方法
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}
