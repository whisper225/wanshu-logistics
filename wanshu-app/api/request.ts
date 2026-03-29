/**
 * API 基址：vue-cli（HBuilderX）下用 process.env.VUE_APP_API_BASE；
 * 可在项目根目录建 `.env` 写 `VUE_APP_API_BASE=https://你的域名/api`
 * 未配置时默认本地后端。
 */
function getApiBase(): string {
  try {
    const env = (typeof process !== 'undefined' && process.env) ? process.env as Record<string, string | undefined> : {}
    if (env.VUE_APP_API_BASE) return env.VUE_APP_API_BASE.replace(/\/$/, '')
  } catch {
    /* ignore */
  }
  return 'http://127.0.0.1:8080/api'
}

const BASE = getApiBase()

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
