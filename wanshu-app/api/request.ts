/**
 * API 基址：通过环境变量 VUE_APP_API_BASE 配置（见根目录 .env.development）。
 *
 * 微信小程序注意：真机/开发者工具里访问 127.0.0.1 指向手机自身，无法访问你电脑上的后端，
 * 会报 net::ERR_CONNECTION_REFUSED。请改为电脑局域网 IP，例如：
 *   VUE_APP_API_BASE=http://192.168.1.100:8080/api
 * 并保证后端已启动、防火墙放行 8080；开发者工具可勾选「不校验合法域名」。
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

/** 去掉 undefined / null，避免 GET 查询串出现 keyword=undefined（小程序会原样序列化） */
function omitEmpty(data?: Record<string, unknown>): Record<string, unknown> | undefined {
  if (!data) return undefined
  const out: Record<string, unknown> = {}
  for (const key of Object.keys(data)) {
    const v = data[key]
    if (v !== undefined && v !== null) {
      out[key] = v
    }
  }
  return Object.keys(out).length ? out : undefined
}

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
      data: omitEmpty(options.data),
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
