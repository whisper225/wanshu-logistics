/** 开发默认指向本地后端；可在 vite 环境变量中设置 VITE_API_BASE */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const BASE = ((import.meta as any).env && (import.meta as any).env.VITE_API_BASE) || 'http://127.0.0.1:8080/api'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: Record<string, unknown>
  header?: Record<string, string>
}

interface ApiEnvelope<T> {
  code: number
  message?: string
  data: T
}

export function request<T>(options: RequestOptions): Promise<T> {
  const token = uni.getStorageSync('token') as string | undefined
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE.replace(/\/$/, '') + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: token } : {}),
        ...options.header
      },
      success: (res) => {
        const body = res.data as ApiEnvelope<T> | T
        if (body && typeof body === 'object' && 'code' in body && (body as ApiEnvelope<T>).code !== undefined) {
          const b = body as ApiEnvelope<T>
          if (b.code === 200) {
            resolve(b.data)
            return
          }
          reject(new Error(b.message || `code ${b.code}`))
          return
        }
        resolve(body as T)
      },
      fail: (err) => reject(err)
    })
  })
}

export { BASE as API_BASE }
